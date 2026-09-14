package com.skinthesia.domain.model

import kotlinx.serialization.Serializable

/** Areas the report can highlight (screen 23). */
@Serializable
enum class ImprovementArea(
    val label: String,
    val dimension: SkinPrintDimension,
    val goal: SkinGoal,
) {
    HYDRATION("Hydration", SkinPrintDimension.HYDRATION, SkinGoal.HYDRATION),
    UNEVEN_TONE("Uneven tone", SkinPrintDimension.EVEN_TONE, SkinGoal.UNEVEN_TONE),
    DARK_SPOTS("Dark spots", SkinPrintDimension.EVEN_TONE, SkinGoal.DARK_SPOTS),
    TEXTURE("Texture", SkinPrintDimension.TEXTURE, SkinGoal.TEXTURE),
    DARK_CIRCLES("Dark circles", SkinPrintDimension.EVEN_TONE, SkinGoal.DARK_CIRCLES),
    BLEMISHES("Blemishes", SkinPrintDimension.CLARITY, SkinGoal.ACNE),
    REDNESS("Redness", SkinPrintDimension.CLARITY, SkinGoal.REDNESS),
    PORES("Pores", SkinPrintDimension.PORE_APPEARANCE, SkinGoal.PORES),
    FINE_LINES("Fine lines", SkinPrintDimension.TEXTURE, SkinGoal.FINE_LINES),
}

@Serializable
data class Evidence(
    val label: String,
    val value: String,
    val source: DataSource,
)

@Serializable
data class FocusArea(
    val area: ImprovementArea,
    val level: AttentionLevel,
    /** 0..100, higher is better. */
    val score: Int,
    val summary: String,
    val evidence: List<Evidence>,
    val regions: List<SkinRegion>,
    val isUserGoal: Boolean,
)

@Serializable
data class Strength(
    val title: String,
    val detail: String,
    val source: DataSource,
)

@Serializable
enum class InsightKind { SENSOR, VISUAL, COMBINED, CONTEXT, PROGRESS }

@Serializable
data class Insight(
    val id: String,
    val kind: InsightKind,
    val title: String,
    val body: String,
    val sources: Set<DataSource>,
)

/** Averages and per-region values for one sensor within a session. */
@Serializable
data class SensorSummary(
    val sensor: SensorType,
    val average: Double,
    val byRegion: Map<MeasurementRegion, Double>,
    val source: DataSource,
)

/** The result of combining camera, probe, lifestyle and goals (screen 19). */
@Serializable
data class CombinedAnalysis(
    val assessmentId: String,
    val skinPrint: SkinPrint,
    val focusAreas: List<FocusArea>,
    val strengths: List<Strength>,
    val insights: List<Insight>,
    val sensorSummaries: List<SensorSummary>,
    /** Lifestyle context lines, phrased as context and never as causes. */
    val contextNotes: List<String>,
    val createdAt: Long,
    val engineVersion: String,
    val usedSimulatedData: Boolean,
)

/** A visualised goal range (screen 20). It is a potential, never a promise. */
@Serializable
data class PotentialDimension(
    val dimension: SkinPrintDimension,
    val current: Int,
    val potentialLow: Int,
    val potentialHigh: Int,
    val isFocus: Boolean,
)

@Serializable
data class PotentialState(
    val currentOverall: Int,
    val potentialLow: Int,
    val potentialHigh: Int,
    val horizonWeeks: Int,
    val dimensions: List<PotentialDimension>,
    val assumptions: List<String>,
) {
    /** Linear interpolation of the overall indicator at [week], used by the interactive slider. */
    fun overallAt(week: Int, optimistic: Boolean = false): Int {
        val target = if (optimistic) potentialHigh else (potentialLow + potentialHigh) / 2
        val t = (week.coerceIn(0, horizonWeeks)).toFloat() / horizonWeeks
        return (currentOverall + (target - currentOverall) * t).toInt()
    }
}

/** The human-readable report (screen 22), assembled by ReportBuilder. */
data class SkinReport(
    val assessmentId: String,
    val skinPrint: SkinPrint,
    val strengths: List<Strength>,
    val focusAreas: List<FocusArea>,
    val measuredValues: List<SensorSummary>,
    val measuredValuesSimulated: Boolean,
    val visualAnalysis: List<IndicatorEstimate>,
    val visualAnalysisSimulated: Boolean,
    val insights: List<Insight>,
    val contextNotes: List<String>,
)
