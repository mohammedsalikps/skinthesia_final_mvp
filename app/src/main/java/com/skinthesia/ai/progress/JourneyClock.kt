package com.skinthesia.ai.progress

/**
 * Decides which programme week a new check-in represents. With the demonstration
 * timeline enabled, each check-in advances to the next milestone week (4, 8, 12) so
 * the whole journey can be experienced in one sitting; the UI labels this clearly.
 * Otherwise weeks follow the calendar from the baseline.
 */
class JourneyClock(private val now: () -> Long = System::currentTimeMillis) {

    fun nextCheckInWeek(baselineStartedAt: Long?, completedWeeks: List<Int>, demoTimeline: Boolean): Int {
        val last = completedWeeks.maxOrNull() ?: 0
        if (demoTimeline) return MILESTONE_WEEKS.firstOrNull { it > last } ?: (last + 4)
        val start = baselineStartedAt ?: now()
        val calendarWeek = ((now() - start) / WEEK_MILLIS).toInt()
        return maxOf(last + 1, calendarWeek, 1)
    }

    companion object {
        val MILESTONE_WEEKS = listOf(4, 8, 12)
        private const val WEEK_MILLIS = 7L * 24 * 60 * 60 * 1000
    }
}
