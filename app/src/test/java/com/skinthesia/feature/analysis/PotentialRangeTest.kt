package com.skinthesia.feature.analysis

import com.skinthesia.domain.model.PotentialState
import org.junit.Assert.assertEquals
import org.junit.Test

class PotentialRangeTest {

    private val state = PotentialState(
        currentOverall = 68,
        potentialLow = 73,
        potentialHigh = 78,
        horizonWeeks = 12,
        dimensions = emptyList(),
        assumptions = emptyList(),
    )

    @Test
    fun `the range starts at today's score`() {
        assertEquals(68 to 68, state.rangeAt(0))
    }

    @Test
    fun `the range reaches the full potential at the horizon`() {
        assertEquals(73 to 78, state.rangeAt(12))
    }

    @Test
    fun `the range widens linearly and clamps outside the programme`() {
        assertEquals(71 to 73, state.rangeAt(6))
        assertEquals(73 to 78, state.rangeAt(20))
        assertEquals(68 to 68, state.rangeAt(-3))
    }

    @Test
    fun `the midpoint projection never exceeds the upper bound`() {
        (0..12).forEach { week ->
            val (low, high) = state.rangeAt(week)
            assert(low <= high) { "week $week: $low > $high" }
            assert(state.overallAt(week) in state.currentOverall..high) { "week $week projection out of range" }
        }
    }
}
