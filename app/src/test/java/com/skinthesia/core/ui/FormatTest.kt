package com.skinthesia.core.ui

import org.junit.Assert.assertEquals
import org.junit.Test

class FormatTest {

    @Test
    fun `prices are whole rupees with Indian digit grouping`() {
        assertEquals("₹850", formatPrice(850))
        assertEquals("₹1,250", formatPrice(1_250))
        assertEquals("₹1,25,000", formatPrice(125_000))
    }

    @Test
    fun `relative time reads naturally for recent activity`() {
        val now = 1_780_000_000_000L
        assertEquals("Just now", relativeTime(now - 20_000, now))
        assertEquals("5 min ago", relativeTime(now - 5 * 60_000L, now))
        assertEquals("3 h ago", relativeTime(now - 3 * 3_600_000L, now))
        assertEquals("2 d ago", relativeTime(now - 2 * 86_400_000L, now))
    }
}
