package com.skinthesia.domain.usecase

import com.skinthesia.ai.progress.ProgressAnalyzer
import com.skinthesia.domain.model.AdherenceSummary
import com.skinthesia.domain.model.Assessment
import com.skinthesia.domain.model.AssessmentKind
import com.skinthesia.domain.model.JourneyMilestone
import com.skinthesia.domain.model.PersonalizedPlan
import com.skinthesia.domain.model.ProgressComparison
import com.skinthesia.domain.model.ProgressSnapshot
import com.skinthesia.domain.model.adherence
import com.skinthesia.domain.repository.AssessmentRepository
import com.skinthesia.domain.repository.PlanRepository
import com.skinthesia.domain.repository.SkinProbeRepository
import kotlinx.coroutines.flow.first
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

/** Everything the progress, comparison and journey screens need, computed from history. */
class ProgressUseCase(
    private val assessments: AssessmentRepository,
    private val probes: SkinProbeRepository,
    private val plans: PlanRepository,
    private val analyzer: ProgressAnalyzer,
    private val today: () -> LocalDate = { LocalDate.now() },
    private val zone: ZoneId = ZoneId.systemDefault(),
) {
    suspend fun snapshots(): List<ProgressSnapshot> =
        assessments.completedList().mapNotNull { snapshot(it) }

    suspend fun snapshot(assessment: Assessment): ProgressSnapshot? {
        val session = assessment.measurementSessionId?.let { probes.session(it) }
        return analyzer.snapshot(assessment, session)
    }

    /** Compares an assessment with the baseline; null for the baseline itself. */
    suspend fun comparisonFor(assessmentId: String): ProgressComparison? {
        val completed = assessments.completedList()
        val baseline = completed.firstOrNull { it.kind == AssessmentKind.BASELINE } ?: return null
        val current = assessments.get(assessmentId) ?: return null
        if (current.id == baseline.id) return null
        val from = snapshot(baseline) ?: return null
        val to = snapshot(current) ?: return null
        return analyzer.compare(from, to)
    }

    suspend fun latestComparison(): ProgressComparison? {
        val latest = assessments.completedList().lastOrNull { it.kind == AssessmentKind.CHECK_IN } ?: return null
        return comparisonFor(latest.id)
    }

    suspend fun adherence(plan: PersonalizedPlan, windowDays: Long = 14): AdherenceSummary {
        val to = today()
        val from = to.minusDays(windowDays - 1)
        val logs = plans.logsBetween(from, to).first()
        val planStart = Instant.ofEpochMilli(plan.createdAt).atZone(zone).toLocalDate()
        return adherence(plan, logs, from, to, planStart)
    }

    suspend fun milestones(programmeWeeks: Int = 12): List<JourneyMilestone> {
        val completed = assessments.completedList()
        val history = plans.history().first()
        val streak = plans.current()?.let { adherence(it, windowDays = 30).streakDays } ?: 0
        return analyzer.milestones(completed, history.size, streak, programmeWeeks)
    }
}
