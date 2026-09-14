package com.skinthesia.ai.progress

import com.skinthesia.ai.analysis.AnalysisInput
import com.skinthesia.ai.analysis.MockSkinAnalysisEngine
import com.skinthesia.ai.recommendation.MockRecommendationEngine
import com.skinthesia.ai.recommendation.PlanInput
import com.skinthesia.domain.model.AdherenceSummary
import com.skinthesia.domain.model.AssessmentKind
import com.skinthesia.domain.model.MilestoneKind
import com.skinthesia.domain.model.PlanChangeKind
import com.skinthesia.domain.model.ProgressSnapshot
import com.skinthesia.domain.model.SensorType
import com.skinthesia.domain.model.SkinGoal
import com.skinthesia.domain.model.SkinPrintDimension
import com.skinthesia.testing.Fixtures
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ProgressAndAdaptationTest {

    private val analyzer = ProgressAnalyzer()

    private fun snapshot(week: Int, overall: Int, hydration: Int, sensorHydration: Double = 55.0, ph: Double = 5.5) = ProgressSnapshot(
        assessmentId = "as-$week",
        week = week,
        date = Fixtures.NOW,
        overall = overall,
        scores = SkinPrintDimension.entries.associateWith { if (it == SkinPrintDimension.HYDRATION) hydration else 66 },
        sensorAverages = mapOf(SensorType.HYDRATION to sensorHydration, SensorType.PH to ph),
        photoPath = null,
        sensorsSimulated = true,
    )

    @Test
    fun `comparison reports deltas with honest directions`() {
        val comparison = analyzer.compare(snapshot(0, 66, 55), snapshot(4, 72, 64, sensorHydration = 64.0, ph = 5.4))
        assertEquals("Your SkinPrint rose 6 points", comparison.headline)
        val hydration = comparison.metrics.first { it.key == ProgressAnalyzer.sensorKey(SensorType.HYDRATION) }
        assertEquals(true, hydration.improved)
        val ph = comparison.metrics.first { it.key == ProgressAnalyzer.sensorKey(SensorType.PH) }
        assertNull(ph.improved)
        assertTrue(comparison.summary.contains("hydration"))
    }

    @Test
    fun `a dip is framed calmly`() {
        val comparison = analyzer.compare(snapshot(0, 66, 55), snapshot(4, 64, 54))
        assertEquals("A small dip this time", comparison.headline)
    }

    @Test
    fun `milestones track achievements and what is next`() {
        val baseline = Fixtures.scoredAssessment("base", 0, overall = 66, hydration = 56, kind = AssessmentKind.BASELINE)
        val week4 = Fixtures.scoredAssessment("w4", 4, overall = 72, hydration = 66)
        val milestones = analyzer.milestones(listOf(baseline, week4), planVersions = 2, bestStreakDays = 3, programmeWeeks = 12)
        val kinds = milestones.map { it.kind }
        assertTrue(MilestoneKind.STARTED in kinds)
        assertTrue(MilestoneKind.FIRST_CHECK_IN in kinds)
        assertTrue(MilestoneKind.SCORE_GAIN in kinds)
        assertTrue(MilestoneKind.TARGET_REACHED in kinds)
        assertTrue(MilestoneKind.PLAN_EVOLVED in kinds)
        assertEquals(listOf(8, 12), milestones.filter { it.isUpcoming }.map { it.week })
        assertFalse(MilestoneKind.STREAK in kinds)
    }

    @Test
    fun `demo timeline advances through milestone weeks`() {
        val clock = JourneyClock { Fixtures.NOW }
        assertEquals(4, clock.nextCheckInWeek(Fixtures.NOW, listOf(0), demoTimeline = true))
        assertEquals(8, clock.nextCheckInWeek(Fixtures.NOW, listOf(0, 4), demoTimeline = true))
        assertEquals(12, clock.nextCheckInWeek(Fixtures.NOW, listOf(0, 4, 8), demoTimeline = true))
        assertEquals(16, clock.nextCheckInWeek(Fixtures.NOW, listOf(0, 4, 8, 12), demoTimeline = true))
    }

    @Test
    fun `calendar timeline follows real weeks`() {
        val day = 24L * 60 * 60 * 1000
        val clock = JourneyClock { Fixtures.NOW + 22 * day }
        assertEquals(3, clock.nextCheckInWeek(Fixtures.NOW, listOf(0), demoTimeline = false))
        val early = JourneyClock { Fixtures.NOW + 2 * day }
        assertEquals(1, early.nextCheckInWeek(Fixtures.NOW, listOf(0), demoTimeline = false))
    }

    @Test
    fun `improved focus goals hand over and every change is explained`() = runTest {
        val profile = Fixtures.profile()
        val combined = MockSkinAnalysisEngine().analyze(AnalysisInput(Fixtures.assessment(), profile, Fixtures.session(), Fixtures.NOW))
        val baseline = Fixtures.assessment(combined = combined)
        val recommender = MockRecommendationEngine()
        val previous = recommender.buildPlan(PlanInput(profile, baseline, Fixtures.catalog, null, Fixtures.NOW))
        val latest = baseline.copy(id = "as-4", week = 4, kind = AssessmentKind.CHECK_IN)
        val comparison = analyzer.compare(snapshot(0, 65, 55), snapshot(4, 71, 72, sensorHydration = 72.0))

        val adapted = AdaptivePlanEngine(recommender).adapt(
            AdaptationInput(profile, latest, comparison, previous, AdherenceSummary(1, 2, 7, 0), Fixtures.catalog, Fixtures.NOW),
        )
        assertEquals(2, adapted.version)
        assertFalse(SkinGoal.HYDRATION in adapted.focus)
        assertTrue(adapted.changes.any { it.kind == PlanChangeKind.FOCUS_SHIFT && it.title == "Hydration is responding well" })
        assertTrue(adapted.changes.any { it.kind == PlanChangeKind.PRODUCT_SWAPPED })
        // One day of data is not enough to judge adherence.
        assertTrue(adapted.changes.none { it.kind == PlanChangeKind.STEP_REMOVED })
    }

    @Test
    fun `steady progress keeps the plan and low adherence lightens it`() = runTest {
        val profile = Fixtures.profile()
        val combined = MockSkinAnalysisEngine().analyze(AnalysisInput(Fixtures.assessment(), profile, Fixtures.session(), Fixtures.NOW))
        val baseline = Fixtures.assessment(combined = combined)
        val recommender = MockRecommendationEngine()
        val previous = recommender.buildPlan(PlanInput(profile, baseline, Fixtures.catalog, null, Fixtures.NOW))
        val latest = baseline.copy(id = "as-4", week = 4, kind = AssessmentKind.CHECK_IN)
        val steady = analyzer.compare(snapshot(0, 65, 55), snapshot(4, 66, 57))
        val engine = AdaptivePlanEngine(recommender)

        val kept = engine.adapt(AdaptationInput(profile, latest, steady, previous, AdherenceSummary(10, 60, 70, 5), Fixtures.catalog, Fixtures.NOW))
        assertEquals(listOf(PlanChangeKind.KEEP), kept.changes.map { it.kind })

        val lighter = engine.adapt(AdaptationInput(profile, latest, steady, previous, AdherenceSummary(10, 20, 70, 0), Fixtures.catalog, Fixtures.NOW))
        assertTrue(lighter.changes.any { it.kind == PlanChangeKind.STEP_REMOVED })
    }
}
