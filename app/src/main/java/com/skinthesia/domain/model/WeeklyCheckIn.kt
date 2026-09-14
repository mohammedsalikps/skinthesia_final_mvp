package com.skinthesia.domain.model

/** How the user's skin felt during the week. */
enum class SkinFeeling { UNHAPPY, NEUTRAL, GOOD, GREAT }

/** A completed or pending weekly check-in (screen 15). */
data class WeeklyCheckIn(
    val week: Int,
    val feeling: SkinFeeling? = null,
    val metricDeltas: List<ProgressMetric> = emptyList(),
    val photo: SkinPhoto? = null,
    val completedAt: Long? = null,
) {
    val isComplete: Boolean get() = completedAt != null
}
