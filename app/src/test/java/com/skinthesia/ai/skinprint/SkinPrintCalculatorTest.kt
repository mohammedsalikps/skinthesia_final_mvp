package com.skinthesia.ai.skinprint

import com.skinthesia.domain.model.DataSource
import com.skinthesia.domain.model.GoalPlan
import com.skinthesia.domain.model.ScoreBand
import com.skinthesia.domain.model.SkinGoal
import com.skinthesia.domain.model.SkinPrintDimension
import com.skinthesia.testing.Fixtures
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SkinPrintCalculatorTest {

    private val calculator = SkinPrintCalculator()

    private fun input(withCamera: Boolean = true, withSession: Boolean = true) = SkinPrintInput(
        assessmentId = "as-1",
        week = 0,
        camera = if (withCamera) Fixtures.camera() else null,
        session = if (withSession) Fixtures.session(forehead = 64.0, leftCheek = 54.0, rightCheek = 56.0) else null,
        skin = Fixtures.profile().skin,
        now = Fixtures.NOW,
    )

    @Test
    fun `dimensions follow the published visual and sensor rules`() {
        val print = calculator.calculate(input())
        assertEquals(71, print.value(SkinPrintDimension.CLARITY))
        assertEquals(63, print.value(SkinPrintDimension.EVEN_TONE))
        assertEquals(70, print.value(SkinPrintDimension.TEXTURE))
        assertEquals(68, print.value(SkinPrintDimension.PORE_APPEARANCE))
        assertEquals(58, print.value(SkinPrintDimension.HYDRATION))
        assertEquals(66, print.overall)
        assertEquals(ScoreBand.GOOD, print.band)
    }

    @Test
    fun `hydration from simulated probe readings is labelled as simulated`() {
        val hydration = calculator.calculate(input()).score(SkinPrintDimension.HYDRATION)!!
        assertEquals(setOf(DataSource.SENSOR_SIMULATED), hydration.sources)
        assertTrue(hydration.explanation.contains("simulated"))
    }

    @Test
    fun `without the probe hydration is a low-confidence estimate from skin type`() {
        val print = calculator.calculate(input(withSession = false))
        val hydration = print.score(SkinPrintDimension.HYDRATION)!!
        assertEquals(52, hydration.value)
        assertEquals(setOf(DataSource.USER_REPORTED), hydration.sources)
        assertFalse(print.inputs.hasSensors)
        assertTrue(hydration.confidence < 0.5f)
    }

    @Test
    fun `without a photo visual dimensions fall back to self-report`() {
        val print = calculator.calculate(input(withCamera = false))
        val tone = print.score(SkinPrintDimension.EVEN_TONE)!!
        assertEquals(setOf(DataSource.USER_REPORTED), tone.sources)
        assertEquals(58, tone.value)
        assertFalse(print.inputs.hasCamera)
    }

    @Test
    fun `overall weights sum to one`() {
        assertEquals(1.0, SkinPrintCalculator.WEIGHTS.values.sum(), 1e-9)
    }

    @Test
    fun `potential ranges widen for focus dimensions and never exceed the ceiling`() {
        val print = calculator.calculate(input())
        val goals = GoalPlan(goals = listOf(SkinGoal.HYDRATION, SkinGoal.DARK_SPOTS), priorities = listOf(SkinGoal.HYDRATION))
        val potential = PotentialEstimator().estimate(print, goals)
        val hydration = potential.dimensions.first { it.dimension == SkinPrintDimension.HYDRATION }
        val clarity = potential.dimensions.first { it.dimension == SkinPrintDimension.CLARITY }
        assertTrue(hydration.isFocus)
        assertFalse(clarity.isFocus)
        assertEquals(75, hydration.potentialHigh)
        assertTrue(hydration.potentialHigh - hydration.current > clarity.potentialHigh - clarity.current)
        potential.dimensions.forEach {
            assertTrue(it.potentialHigh <= 92)
            assertTrue(it.potentialLow in it.current..it.potentialHigh)
        }
        assertEquals(print.overall, potential.overallAt(0))
        assertEquals((potential.potentialLow + potential.potentialHigh) / 2, potential.overallAt(12))
        assertTrue(potential.overallAt(6) in print.overall..potential.potentialHigh)
    }
}
