package com.skinthesia.ai.vision

import com.skinthesia.domain.model.SkinConcern
import com.skinthesia.domain.model.SkinGoal
import com.skinthesia.domain.model.VisualIndicator
import com.skinthesia.testing.Fixtures
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class MockSkinVisionAnalyzerTest {

    private val analyzer = MockSkinVisionAnalyzer(clock = { Fixtures.NOW }, processingMillis = 0)
    private val photo = Fixtures.photo()

    private fun context(concerns: Set<SkinConcern> = emptySet(), goals: Set<SkinGoal> = emptySet(), week: Int = 0) =
        VisionContext(userSeed = 42L, concerns = concerns, goals = goals, week = week)

    @Test
    fun `estimates are deterministic, complete and marked simulated`() = runTest {
        val first = analyzer.analyze(photo, context())
        val second = analyzer.analyze(photo, context())
        assertEquals(first.estimates, second.estimates)
        assertEquals(VisualIndicator.entries.size, first.estimates.size)
        assertTrue(first.isSimulated)
    }

    @Test
    fun `reported concerns raise the matching visual estimate`() {
        val plain = analyzer.estimate(VisualIndicator.ACNE_APPEARANCE, photo, context())
        val withBreakouts = analyzer.estimate(VisualIndicator.ACNE_APPEARANCE, photo, context(concerns = setOf(SkinConcern.BREAKOUTS)))
        assertEquals(plain.level + 12, withBreakouts.level)
    }

    @Test
    fun `focused indicators ease over the programme weeks`() {
        val goals = setOf(SkinGoal.DARK_SPOTS)
        val start = analyzer.estimate(VisualIndicator.PIGMENTATION, photo, context(goals = goals, week = 0))
        val later = analyzer.estimate(VisualIndicator.PIGMENTATION, photo, context(goals = goals, week = 12))
        assertTrue(later.level < start.level)
    }
}
