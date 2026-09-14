package com.skinthesia.domain

import com.skinthesia.domain.model.DataSource
import com.skinthesia.domain.model.MetricKind
import com.skinthesia.domain.model.ProgressMetric
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ProgressMetricTest {

    private fun metric(from: Double, to: Double, higherIsBetter: Boolean?) = ProgressMetric(
        key = "k",
        label = "Label",
        kind = MetricKind.DIMENSION,
        from = from,
        to = to,
        unit = "",
        decimals = 0,
        higherIsBetter = higherIsBetter,
        source = DataSource.DERIVED,
    )

    @Test
    fun `a rise is an improvement when higher is better`() {
        assertEquals(true, metric(60.0, 66.0, higherIsBetter = true).improved)
        assertEquals(false, metric(66.0, 60.0, higherIsBetter = true).improved)
    }

    @Test
    fun `a fall is an improvement when lower is better`() {
        assertEquals(true, metric(40.0, 30.0, higherIsBetter = false).improved)
    }

    @Test
    fun `metrics without a healthy direction stay neutral`() {
        assertNull(metric(5.5, 5.4, higherIsBetter = null).improved)
    }

    @Test
    fun `tiny changes stay neutral`() {
        assertNull(metric(60.0, 60.01, higherIsBetter = true).improved)
    }

    @Test
    fun `percent change is relative to the starting value`() {
        assertEquals(10, metric(60.0, 66.0, higherIsBetter = true).percentChange)
        assertNull(metric(0.0, 5.0, higherIsBetter = true).percentChange)
    }
}
