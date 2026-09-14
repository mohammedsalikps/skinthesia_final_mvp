package com.skinthesia.ai.analysis

import com.skinthesia.ai.skinprint.SkinPrintCalculator
import com.skinthesia.ai.skinprint.SkinPrintInput
import com.skinthesia.domain.model.AttentionLevel
import com.skinthesia.domain.model.CameraAnalysisResult
import com.skinthesia.domain.model.Climate
import com.skinthesia.domain.model.CombinedAnalysis
import com.skinthesia.domain.model.DataSource
import com.skinthesia.domain.model.Environment
import com.skinthesia.domain.model.Evidence
import com.skinthesia.domain.model.FocusArea
import com.skinthesia.domain.model.Habit
import com.skinthesia.domain.model.Ids
import com.skinthesia.domain.model.ImprovementArea
import com.skinthesia.domain.model.Insight
import com.skinthesia.domain.model.InsightKind
import com.skinthesia.domain.model.Level
import com.skinthesia.domain.model.LifestyleProfile
import com.skinthesia.domain.model.MeasurementRegion
import com.skinthesia.domain.model.MeasurementSession
import com.skinthesia.domain.model.SensorSummary
import com.skinthesia.domain.model.SensorType
import com.skinthesia.domain.model.SkinConcern
import com.skinthesia.domain.model.SkinGoal
import com.skinthesia.domain.model.SkinPrint
import com.skinthesia.domain.model.SkinPrintDimension
import com.skinthesia.domain.model.SkinRegion
import com.skinthesia.domain.model.SleepPattern
import com.skinthesia.domain.model.Strength
import com.skinthesia.domain.model.UserProfile
import com.skinthesia.domain.model.VisualIndicator
import kotlinx.coroutines.delay
import java.util.Locale
import kotlin.math.roundToInt

/**
 * Development analysis engine: deterministic, explainable rules on top of the
 * transparent [SkinPrintCalculator]. Language is non-clinical, lifestyle is framed as
 * context, and every insight lists the kinds of data it rests on.
 */
class MockSkinAnalysisEngine(
    private val calculator: SkinPrintCalculator = SkinPrintCalculator(),
    private val processingMillis: Long = 0,
) : SkinAnalysisEngine {

    override val engineName: String = "Skinthesia analysis (development)"

    override suspend fun analyze(input: AnalysisInput): CombinedAnalysis {
        if (processingMillis > 0) delay(processingMillis)
        val assessment = input.assessment
        val camera = assessment.cameraAnalysis
        val session = input.session?.takeIf { it.readings.isNotEmpty() }
        val skinPrint = calculator.calculate(
            SkinPrintInput(
                assessmentId = assessment.id,
                week = assessment.week,
                camera = camera,
                session = session,
                skin = input.profile.skin,
                now = input.now,
            ),
        )
        val summaries = session?.let(::sensorSummaries).orEmpty()
        val focusAreas = focusAreas(skinPrint, camera, summaries, input.profile)
        return CombinedAnalysis(
            assessmentId = assessment.id,
            skinPrint = skinPrint,
            focusAreas = focusAreas,
            strengths = strengths(skinPrint, summaries),
            insights = insights(skinPrint, camera, summaries, focusAreas, input.profile),
            sensorSummaries = summaries,
            contextNotes = contextNotes(input.profile.lifestyle),
            createdAt = input.now,
            engineVersion = ENGINE_VERSION,
            usedSimulatedData = (camera?.isSimulated ?: false) || (session?.isSimulated ?: false),
        )
    }

    fun sensorSummaries(session: MeasurementSession): List<SensorSummary> {
        val source = if (session.isSimulated) DataSource.SENSOR_SIMULATED else DataSource.SENSOR_MEASURED
        return SensorType.entries.mapNotNull { sensor ->
            val byRegion = MeasurementRegion.entries.mapNotNull { region ->
                session.value(region, sensor)?.let { region to it }
            }.toMap()
            if (byRegion.isEmpty()) null else SensorSummary(sensor, byRegion.values.average(), byRegion, source)
        }
    }

    private fun focusAreas(
        skinPrint: SkinPrint,
        camera: CameraAnalysisResult?,
        summaries: List<SensorSummary>,
        profile: UserProfile,
    ): List<FocusArea> {
        val goals = profile.goals.goals.toSet()
        val candidates = ImprovementArea.entries.filter { area ->
            when (area) {
                ImprovementArea.DARK_SPOTS -> SkinGoal.DARK_SPOTS in goals
                ImprovementArea.UNEVEN_TONE -> SkinGoal.DARK_SPOTS !in goals || SkinGoal.UNEVEN_TONE in goals
                else -> true
            }
        }
        return candidates.mapNotNull { area ->
            val score = areaScore(area, skinPrint, camera) ?: return@mapNotNull null
            val level = attention(score)
            if (level == AttentionLevel.GOOD) return@mapNotNull null
            FocusArea(
                area = area,
                level = level,
                score = score,
                summary = areaSummary(area, level, camera, summaries),
                evidence = evidence(area, camera, summaries, profile),
                regions = regions(area, camera),
                isUserGoal = area.goal in goals,
            )
        }.sortedWith(
            compareByDescending<FocusArea> { it.level.ordinal }
                .thenByDescending { it.isUserGoal }
                .thenBy { it.score },
        ).take(MAX_FOCUS_AREAS)
    }

    private fun areaScore(area: ImprovementArea, skinPrint: SkinPrint, camera: CameraAnalysisResult?): Int? {
        fun visual(indicator: VisualIndicator): Int? = camera?.level(indicator)?.let { (100 - it * 0.8).roundToInt() }
        return when (area) {
            ImprovementArea.HYDRATION -> skinPrint.value(SkinPrintDimension.HYDRATION)
            ImprovementArea.UNEVEN_TONE, ImprovementArea.DARK_SPOTS -> visual(VisualIndicator.PIGMENTATION) ?: skinPrint.value(SkinPrintDimension.EVEN_TONE)
            ImprovementArea.TEXTURE -> visual(VisualIndicator.TEXTURE) ?: skinPrint.value(SkinPrintDimension.TEXTURE)
            ImprovementArea.DARK_CIRCLES -> visual(VisualIndicator.DARK_CIRCLES)
            ImprovementArea.BLEMISHES -> visual(VisualIndicator.ACNE_APPEARANCE) ?: skinPrint.value(SkinPrintDimension.CLARITY)
            ImprovementArea.REDNESS -> visual(VisualIndicator.REDNESS)
            ImprovementArea.PORES -> visual(VisualIndicator.PORES) ?: skinPrint.value(SkinPrintDimension.PORE_APPEARANCE)
            ImprovementArea.FINE_LINES -> visual(VisualIndicator.FINE_LINES)
        }
    }

    private fun attention(score: Int): AttentionLevel = when {
        score < 58 -> AttentionLevel.NEEDS_ATTENTION
        score < 66 -> AttentionLevel.MODERATE
        score < 75 -> AttentionLevel.MILD
        else -> AttentionLevel.GOOD
    }

    private fun areaSummary(
        area: ImprovementArea,
        level: AttentionLevel,
        camera: CameraAnalysisResult?,
        summaries: List<SensorSummary>,
    ): String {
        if (area == ImprovementArea.HYDRATION) {
            val hydration = summaries.firstOrNull { it.sensor == SensorType.HYDRATION }
                ?: return "Estimated from your skin type. Measuring with the probe turns this into a reading."
            val cheeks = cheekAverage(hydration)
            val forehead = hydration.byRegion[MeasurementRegion.FOREHEAD]
            return if (cheeks != null && forehead != null && forehead - cheeks >= 5) {
                "Your cheeks read drier than your forehead, averaging ${fmt(cheeks, 0)} on the hydration index."
            } else {
                "Your skin averaged ${fmt(hydration.average, 0)} on the hydration index across ${hydration.byRegion.size} areas."
            }
        }
        val indicator = area.indicator ?: return "Based on your SkinPrint."
        val estimate = camera?.estimates?.firstOrNull { it.indicator == indicator }
            ?: return "Estimated from your answers; a photo gives a clearer picture."
        val where = estimate.regions.take(2).joinToString(" and ") { it.label.lowercase(Locale.ROOT) }
        val visibility = estimate.visibility.label.lowercase(Locale.ROOT)
        val lead = if (level == AttentionLevel.NEEDS_ATTENTION) "Your photo suggests" else "Your photo shows"
        return "$lead $visibility ${indicator.plain}, mostly around the $where."
    }

    private fun evidence(
        area: ImprovementArea,
        camera: CameraAnalysisResult?,
        summaries: List<SensorSummary>,
        profile: UserProfile,
    ): List<Evidence> = buildList {
        area.indicator?.let { indicator ->
            camera?.estimates?.firstOrNull { it.indicator == indicator }?.let { estimate ->
                add(
                    Evidence(
                        label = "Visual estimate",
                        value = estimate.visibility.label + " · " + estimate.regions.first().label.lowercase(Locale.ROOT),
                        source = DataSource.VISUAL_ESTIMATE,
                    ),
                )
            }
        }
        if (area == ImprovementArea.HYDRATION || area == ImprovementArea.TEXTURE) {
            summaries.firstOrNull { it.sensor == SensorType.HYDRATION }?.let { hydration ->
                val parts = hydration.byRegion.entries.joinToString(" · ") { (region, value) -> shortRegion(region) + " " + fmt(value, 0) }
                add(Evidence("Hydration index", fmt(hydration.average, 0) + " avg — " + parts, hydration.source))
            }
        }
        area.relatedConcerns.firstOrNull { it in profile.skin.concerns }?.let { concern ->
            add(Evidence("You mentioned", concern.label, DataSource.USER_REPORTED))
        }
    }

    private fun regions(area: ImprovementArea, camera: CameraAnalysisResult?): List<SkinRegion> {
        if (area == ImprovementArea.HYDRATION) return listOf(SkinRegion.LEFT_CHEEK, SkinRegion.RIGHT_CHEEK, SkinRegion.FOREHEAD)
        val indicator = area.indicator ?: return emptyList()
        return camera?.estimates?.firstOrNull { it.indicator == indicator }?.regions.orEmpty()
    }

    private fun strengths(skinPrint: SkinPrint, summaries: List<SensorSummary>): List<Strength> {
        val result = mutableListOf<Strength>()
        summaries.firstOrNull { it.sensor == SensorType.PH }?.let { ph ->
            if (ph.average in 4.7..5.7) {
                result += Strength(
                    title = "Balanced surface pH",
                    detail = "Your readings average pH ${fmt(ph.average, 1)}, within the typical range for skin's surface.",
                    source = ph.source,
                )
            }
        }
        skinPrint.scores.filter { it.value >= 72 }.sortedByDescending { it.value }.forEach { score ->
            result += Strength(
                title = "${score.dimension.label} looks strong",
                detail = "Your ${score.dimension.label.lowercase(Locale.ROOT)} indicator is ${score.value}, one of your best areas.",
                source = DataSource.DERIVED,
            )
        }
        if (result.isEmpty()) {
            val best = skinPrint.scores.maxBy { it.value }
            result += Strength(
                title = "${best.dimension.label} is your strongest area",
                detail = "At ${best.value}, it gives your plan a solid foundation to build on.",
                source = DataSource.DERIVED,
            )
        }
        return result.take(3)
    }

    private fun insights(
        skinPrint: SkinPrint,
        camera: CameraAnalysisResult?,
        summaries: List<SensorSummary>,
        focusAreas: List<FocusArea>,
        profile: UserProfile,
    ): List<Insight> = buildList {
        val hydration = summaries.firstOrNull { it.sensor == SensorType.HYDRATION }
        val hydrationScore = skinPrint.value(SkinPrintDimension.HYDRATION)

        if (hydration != null) {
            val cheeks = cheekAverage(hydration)
            val forehead = hydration.byRegion[MeasurementRegion.FOREHEAD]
            if (cheeks != null && forehead != null && forehead - cheeks >= 5) {
                add(
                    Insight(
                        id = Ids.new("ins"),
                        kind = InsightKind.SENSOR,
                        title = "Your cheeks read drier than your forehead",
                        body = "Cheeks averaged ${fmt(cheeks, 0)} on the hydration index, compared with ${fmt(forehead, 0)} on your forehead. A richer moisturizer on the cheeks is a simple place to start.",
                        sources = setOf(hydration.source),
                    ),
                )
            }
        }

        val textureLevel = camera?.level(VisualIndicator.TEXTURE)
        if (hydrationScore < 62 && textureLevel != null && textureLevel >= 38) {
            add(
                Insight(
                    id = Ids.new("ins"),
                    kind = InsightKind.COMBINED,
                    title = "Hydration and texture often move together",
                    body = "Your photo shows some texture and your hydration is on the lower side. Many people notice skin looks smoother as hydration improves, so your plan pairs the two.",
                    sources = setOfNotNull(DataSource.VISUAL_ESTIMATE, hydration?.source ?: DataSource.USER_REPORTED),
                ),
            )
        }

        profile.goals.ranked.firstOrNull()?.let { priority ->
            val match = focusAreas.firstOrNull { it.area.goal == priority }
            add(
                if (match != null) {
                    Insight(
                        id = Ids.new("ins"),
                        kind = InsightKind.COMBINED,
                        title = "Your priority matches what we see",
                        body = "You chose ${priority.label.lowercase(Locale.ROOT)} as a priority, and it's also an area our analysis highlighted. Your plan starts there.",
                        sources = setOf(DataSource.USER_REPORTED, DataSource.DERIVED),
                    )
                } else {
                    val next = focusAreas.firstOrNull()
                    Insight(
                        id = Ids.new("ins"),
                        kind = InsightKind.COMBINED,
                        title = "${priority.label} is already in a good place",
                        body = if (next != null) {
                            "Your ${priority.label.lowercase(Locale.ROOT)} looks relatively balanced. We'll protect it while giving ${next.area.label.lowercase(Locale.ROOT)} a little more attention."
                        } else {
                            "Your ${priority.label.lowercase(Locale.ROOT)} looks relatively balanced. Your plan focuses on keeping it that way."
                        },
                        sources = setOf(DataSource.USER_REPORTED, DataSource.DERIVED),
                    )
                },
            )
        }

        val sun = profile.lifestyle.sunExposure
        if (sun == Level.HIGH || (sun == Level.MODERATE && (SkinGoal.DARK_SPOTS in profile.goals.goals || SkinGoal.UNEVEN_TONE in profile.goals.goals))) {
            add(
                Insight(
                    id = Ids.new("ins"),
                    kind = InsightKind.CONTEXT,
                    title = "Daily sunscreen anchors your plan",
                    body = "You told us you spend time in the sun. Broad-spectrum SPF is the step that protects the progress everything else makes, especially for tone.",
                    sources = setOf(DataSource.USER_REPORTED),
                ),
            )
        }

        if (hydration == null) {
            add(
                Insight(
                    id = Ids.new("ins"),
                    kind = InsightKind.CONTEXT,
                    title = "Add probe readings for a fuller picture",
                    body = "Your hydration score is estimated from your answers. Measuring with the Skinthesia Probe turns it into a reading.",
                    sources = setOf(DataSource.USER_REPORTED),
                ),
            )
        }
    }.take(4)

    private fun contextNotes(lifestyle: LifestyleProfile): List<String> = buildList {
        if (lifestyle.sleep == SleepPattern.UNDER_6) add("You mentioned short nights, so your evening routine stays short and easy to keep.")
        if (lifestyle.stress == Level.HIGH) add("Busy weeks happen. Your plan keeps the essentials simple so consistency is easier.")
        if (lifestyle.sunExposure == Level.HIGH) add("You're outdoors a lot, so sunscreen reapplication is part of your plan.")
        if (lifestyle.environment == Environment.INDOOR_AC || lifestyle.climate == Climate.DRY || lifestyle.climate == Climate.COLD) {
            add("Dry air, indoors or out, can leave skin feeling tighter, so we've favoured hydrating textures.")
        }
        if (lifestyle.climate == Climate.HUMID) add("In humid weather we've kept textures light.")
        if (Habit.DAILY_MAKEUP in lifestyle.habits) add("With daily makeup, a thorough evening cleanse is part of your routine.")
    }.take(4)

    private fun cheekAverage(summary: SensorSummary): Double? =
        listOfNotNull(summary.byRegion[MeasurementRegion.LEFT_CHEEK], summary.byRegion[MeasurementRegion.RIGHT_CHEEK])
            .takeIf { it.isNotEmpty() }?.average()

    private fun shortRegion(region: MeasurementRegion): String = when (region) {
        MeasurementRegion.FOREHEAD -> "forehead"
        MeasurementRegion.LEFT_CHEEK -> "left cheek"
        MeasurementRegion.RIGHT_CHEEK -> "right cheek"
    }

    private fun fmt(value: Double, decimals: Int): String = String.format(Locale.US, "%.${decimals}f", value)

    companion object {
        const val ENGINE_VERSION = "mock-analysis-0.4 (development)"
        private const val MAX_FOCUS_AREAS = 5
    }
}

/** The visual indicator behind an improvement area, when there is one. */
val ImprovementArea.indicator: VisualIndicator?
    get() = when (this) {
        ImprovementArea.HYDRATION -> null
        ImprovementArea.UNEVEN_TONE, ImprovementArea.DARK_SPOTS -> VisualIndicator.PIGMENTATION
        ImprovementArea.TEXTURE -> VisualIndicator.TEXTURE
        ImprovementArea.DARK_CIRCLES -> VisualIndicator.DARK_CIRCLES
        ImprovementArea.BLEMISHES -> VisualIndicator.ACNE_APPEARANCE
        ImprovementArea.REDNESS -> VisualIndicator.REDNESS
        ImprovementArea.PORES -> VisualIndicator.PORES
        ImprovementArea.FINE_LINES -> VisualIndicator.FINE_LINES
    }

/** Questionnaire concerns that relate to an improvement area. */
val ImprovementArea.relatedConcerns: List<SkinConcern>
    get() = when (this) {
        ImprovementArea.HYDRATION -> listOf(SkinConcern.DRYNESS, SkinConcern.DULLNESS)
        ImprovementArea.UNEVEN_TONE -> listOf(SkinConcern.UNEVEN_TONE, SkinConcern.DULLNESS)
        ImprovementArea.DARK_SPOTS -> listOf(SkinConcern.DARK_SPOTS)
        ImprovementArea.TEXTURE -> listOf(SkinConcern.TEXTURE)
        ImprovementArea.DARK_CIRCLES -> listOf(SkinConcern.DARK_CIRCLES)
        ImprovementArea.BLEMISHES -> listOf(SkinConcern.BREAKOUTS, SkinConcern.OILINESS)
        ImprovementArea.REDNESS -> listOf(SkinConcern.REDNESS, SkinConcern.SENSITIVITY)
        ImprovementArea.PORES -> listOf(SkinConcern.VISIBLE_PORES, SkinConcern.OILINESS)
        ImprovementArea.FINE_LINES -> listOf(SkinConcern.FINE_LINES)
    }
