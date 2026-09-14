package com.skinthesia.domain.model

import kotlinx.serialization.Serializable
import kotlin.math.abs
import kotlin.math.roundToInt

/** The state of the skin at one point in the journey, extracted from an assessment. */
@Serializable
data class ProgressSnapshot(
    val assessmentId: String,
    val week: Int,
    val date: Long,
    val overall: Int,
    val scores: Map<SkinPrintDimension, Int>,
    val sensorAverages: Map<SensorType, Double>,
    val photoPath: String?,
    val sensorsSimulated: Boolean,
) {
    val weekLabel: String get() = if (week == 0) "Day 1" else "Week $week"
}

@Serializable
enum class MetricKind { SKINPRINT, DIMENSION, SENSOR }

@Serializable
data class ProgressMetric(
    val key: String,
    val label: String,
    val kind: MetricKind,
    val from: Double,
    val to: Double,
    val unit: String,
    val decimals: Int,
    /** Null when there is no simple "better" direction, as with pH. */
    val higherIsBetter: Boolean?,
    val source: DataSource,
) {
    val delta: Double get() = to - from

    /** True when the change moves in the healthy direction; null when neutral or undefined. */
    val improved: Boolean?
        get() = when {
            higherIsBetter == null || abs(delta) < 0.05 -> null
            higherIsBetter -> delta > 0
            else -> delta < 0
        }

    val percentChange: Int?
        get() = if (from == 0.0) null else ((delta / from) * 100).roundToInt()
}

@Serializable
data class ProgressComparison(
    val baseline: ProgressSnapshot,
    val current: ProgressSnapshot,
    val metrics: List<ProgressMetric>,
    val headline: String,
    val summary: String,
)

@Serializable
enum class MilestoneKind { STARTED, FIRST_CHECK_IN, SCORE_GAIN, TARGET_REACHED, STREAK, PLAN_EVOLVED, PROGRAMME_COMPLETE, UPCOMING }

@Serializable
data class JourneyMilestone(
    val id: String,
    val week: Int,
    val kind: MilestoneKind,
    val title: String,
    val detail: String,
    val achievedAt: Long?,
) {
    val isUpcoming: Boolean get() = achievedAt == null
}
