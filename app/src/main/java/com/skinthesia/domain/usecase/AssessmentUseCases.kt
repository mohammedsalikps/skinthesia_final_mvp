package com.skinthesia.domain.usecase

import com.skinthesia.ai.analysis.AnalysisInput
import com.skinthesia.ai.analysis.SkinAnalysisEngine
import com.skinthesia.ai.progress.AdaptationInput
import com.skinthesia.ai.progress.AdaptivePlanEngine
import com.skinthesia.ai.progress.JourneyClock
import com.skinthesia.ai.quality.PhotoQualityAnalyzer
import com.skinthesia.ai.recommendation.PlanInput
import com.skinthesia.ai.recommendation.RecommendationEngine
import com.skinthesia.ai.vision.SkinVisionAnalyzer
import com.skinthesia.ai.vision.VisionContext
import com.skinthesia.domain.model.Assessment
import com.skinthesia.domain.model.AssessmentKind
import com.skinthesia.domain.model.AssessmentStatus
import com.skinthesia.domain.model.CameraAnalysisResult
import com.skinthesia.domain.model.CombinedAnalysis
import com.skinthesia.domain.model.Ids
import com.skinthesia.domain.model.MeasurementSession
import com.skinthesia.domain.model.PersonalizedPlan
import com.skinthesia.domain.model.PhotoQualityResult
import com.skinthesia.domain.model.SkinPhoto
import com.skinthesia.domain.repository.AssessmentRepository
import com.skinthesia.domain.repository.PhotoStore
import com.skinthesia.domain.repository.PlanRepository
import com.skinthesia.domain.repository.ProductCatalogRepository
import com.skinthesia.domain.repository.SettingsRepository
import com.skinthesia.domain.repository.SkinProbeRepository
import com.skinthesia.domain.repository.UserProfileRepository

/** Starts a baseline or check-in assessment, or resumes the one already in progress. */
class StartAssessmentUseCase(
    private val profiles: UserProfileRepository,
    private val assessments: AssessmentRepository,
    private val settings: SettingsRepository,
    private val journeyClock: JourneyClock,
    private val clock: () -> Long = System::currentTimeMillis,
) {
    suspend operator fun invoke(kind: AssessmentKind): Assessment {
        val profile = profiles.current()
        profile.activeAssessmentId?.let { id ->
            val existing = assessments.get(id)
            if (existing != null && existing.kind == kind && existing.status == AssessmentStatus.IN_PROGRESS) return existing
        }
        val week = if (kind == AssessmentKind.BASELINE) {
            0
        } else {
            val completed = assessments.completedList()
            val baseline = completed.firstOrNull { it.kind == AssessmentKind.BASELINE }
            journeyClock.nextCheckInWeek(baseline?.startedAt, completed.map { it.week }, settings.current().demoTimeline)
        }
        val assessment = Assessment(id = Ids.new("as"), kind = kind, week = week, startedAt = clock())
        assessments.save(assessment)
        profiles.update { it.copy(activeAssessmentId = assessment.id) }
        return assessment
    }
}

/**
 * Attaches a captured or imported photo to an assessment and runs the quality
 * pipeline on it. A replaced photo is deleted so no orphaned images remain.
 */
class AttachPhotoUseCase(
    private val assessments: AssessmentRepository,
    private val qualityAnalyzer: PhotoQualityAnalyzer,
    private val photoStore: PhotoStore,
) {
    suspend operator fun invoke(assessmentId: String, photo: SkinPhoto): PhotoQualityResult {
        val previous = assessments.get(assessmentId)?.photo
        val quality = qualityAnalyzer.analyze(photo)
        assessments.update(assessmentId) {
            it.copy(photo = photo, quality = quality, cameraAnalysis = null, combined = null)
        }
        if (previous != null && previous.filePath != photo.filePath) photoStore.delete(previous.filePath)
        return quality
    }
}

/** Runs camera analysis and the combined analysis for an assessment, caching results. */
class RunAnalysisUseCase(
    private val profiles: UserProfileRepository,
    private val assessments: AssessmentRepository,
    private val probes: SkinProbeRepository,
    private val vision: SkinVisionAnalyzer,
    private val engine: SkinAnalysisEngine,
    private val clock: () -> Long = System::currentTimeMillis,
) {
    suspend fun camera(assessmentId: String): CameraAnalysisResult? {
        val assessment = assessments.get(assessmentId) ?: return null
        val photo = assessment.photo ?: return null
        assessment.cameraAnalysis?.takeIf { it.photoId == photo.id }?.let { return it }
        val profile = profiles.current()
        val result = vision.analyze(
            photo,
            VisionContext(
                userSeed = profile.id.hashCode().toLong(),
                concerns = profile.skin.concerns,
                goals = profile.goals.goals.toSet(),
                week = assessment.week,
            ),
        )
        assessments.update(assessmentId) { it.copy(cameraAnalysis = result, combined = null) }
        return result
    }

    suspend fun session(assessmentId: String): MeasurementSession? {
        val sessionId = assessments.get(assessmentId)?.measurementSessionId ?: return null
        return probes.session(sessionId)
    }

    suspend fun combine(assessmentId: String): CombinedAnalysis? {
        val assessment = assessments.get(assessmentId) ?: return null
        if (assessment.photo != null && assessment.cameraAnalysis == null) camera(assessmentId)
        val fresh = assessments.get(assessmentId) ?: return null
        val combined = engine.analyze(
            AnalysisInput(
                assessment = fresh,
                profile = profiles.current(),
                session = session(assessmentId),
                now = clock(),
            ),
        )
        assessments.update(assessmentId) { it.copy(combined = combined) }
        return combined
    }
}

/**
 * Completes an assessment. The baseline produces the first plan; each check-in
 * adapts the current plan using progress and adherence.
 */
class CompleteAssessmentUseCase(
    private val profiles: UserProfileRepository,
    private val assessments: AssessmentRepository,
    private val plans: PlanRepository,
    private val catalog: ProductCatalogRepository,
    private val recommendations: RecommendationEngine,
    private val adaptive: AdaptivePlanEngine,
    private val progress: ProgressUseCase,
    private val clock: () -> Long = System::currentTimeMillis,
) {
    suspend operator fun invoke(assessmentId: String): PersonalizedPlan? {
        val existing = assessments.get(assessmentId) ?: return null
        val assessment = if (existing.isComplete) {
            existing
        } else {
            assessments.update(assessmentId) { it.copy(status = AssessmentStatus.COMPLETE, completedAt = clock()) } ?: return null
        }
        val profile = profiles.update { p ->
            p.copy(
                activeAssessmentId = null,
                baselineAssessmentId = p.baselineAssessmentId ?: assessment.id.takeIf { assessment.kind == AssessmentKind.BASELINE },
            )
        }
        val previous = plans.current()
        if (previous?.basedOnAssessmentId == assessment.id) return previous
        val products = catalog.all()
        val plan = if (assessment.kind == AssessmentKind.BASELINE || previous == null) {
            recommendations.buildPlan(PlanInput(profile, assessment, products, previous, clock()))
        } else {
            adaptive.adapt(
                AdaptationInput(
                    profile = profile,
                    latest = assessment,
                    comparison = progress.comparisonFor(assessment.id),
                    previous = previous,
                    adherence = progress.adherence(previous),
                    catalog = products,
                    now = clock(),
                ),
            )
        }
        plans.save(plan)
        return plan
    }
}
