package com.skinthesia.ai.analysis

import com.skinthesia.domain.model.AttentionLevel
import com.skinthesia.domain.model.ImprovementArea
import com.skinthesia.testing.Fixtures
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class MockSkinAnalysisEngineTest {

    private val engine = MockSkinAnalysisEngine()

    private suspend fun analyze(withSession: Boolean = true) = engine.analyze(
        AnalysisInput(
            assessment = Fixtures.assessment(),
            profile = Fixtures.profile(),
            session = if (withSession) Fixtures.session() else null,
            now = Fixtures.NOW,
        ),
    )

    @Test
    fun `low probe hydration leads the focus areas`() = runTest {
        val result = analyze()
        val first = result.focusAreas.first()
        assertEquals(ImprovementArea.HYDRATION, first.area)
        assertEquals(AttentionLevel.NEEDS_ATTENTION, first.level)
        assertTrue(first.isUserGoal)
        assertTrue(result.focusAreas.none { it.area == ImprovementArea.UNEVEN_TONE })
        assertNotNull(result.focusAreas.firstOrNull { it.area == ImprovementArea.DARK_SPOTS })
    }

    @Test
    fun `regional differences become a plain-language insight`() = runTest {
        val result = analyze()
        assertTrue(result.insights.any { it.title == "Your cheeks read drier than your forehead" })
        assertTrue(result.strengths.any { it.title == "Balanced surface pH" })
        assertTrue(result.usedSimulatedData)
        assertTrue(result.contextNotes.isNotEmpty())
    }

    @Test
    fun `without probe readings the engine suggests measuring`() = runTest {
        val result = analyze(withSession = false)
        assertTrue(result.sensorSummaries.isEmpty())
        assertTrue(result.insights.any { it.title == "Add probe readings for a fuller picture" })
    }

    @Test
    fun `report builder exposes measured values and visual estimates`() = runTest {
        val combined = analyze()
        val report = ReportBuilder().build(Fixtures.assessment(combined = combined))!!
        assertEquals(3, report.measuredValues.size)
        assertTrue(report.measuredValuesSimulated)
        assertTrue(report.visualAnalysisSimulated)
        assertEquals(50, report.visualAnalysis.first().level)
    }
}
