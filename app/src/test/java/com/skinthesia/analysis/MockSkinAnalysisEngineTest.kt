package com.skinthesia.analysis

import com.skinthesia.domain.model.SkinConcern
import com.skinthesia.domain.model.SkinHealthStatus
import com.skinthesia.domain.model.SkinProfile
import com.skinthesia.domain.model.SkinType
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class MockSkinAnalysisEngineTest {

    @Test
    fun `analysis is deterministic and within range`() = runTest {
        val engine = MockSkinAnalysisEngine(clock = { 1_000L })
        val request = SkinAnalysisRequest(
            profile = SkinProfile(skinType = SkinType.COMBINATION, concerns = setOf(SkinConcern.ACNE)),
            photo = null,
        )

        val first = engine.analyze(request)
        val second = engine.analyze(request)

        assertEquals(first.skinPrint.score, second.skinPrint.score)
        assertTrue(first.skinPrint.score in 0..100)
        assertEquals(8, first.skinPrint.metrics.size)
        assertEquals(SkinHealthStatus.fromScore(first.skinPrint.score), first.skinPrint.status)
        assertTrue(first.skinPrint.regionFindings.isNotEmpty())
        assertTrue(first.skinPrint.contributingFactors.isNotEmpty())
    }

    @Test
    fun `measurement stream completes with readings`() = runTest {
        val engine = MockSkinAnalysisEngine(measurementSteps = 5, stepDelayMillis = 0L, clock = { 42L })

        val frames = engine.measure().toList()

        assertEquals(6, frames.size)
        assertTrue(frames.last().isComplete)
        assertNotNull(frames.last().partial)
        assertTrue(engine.isSimulated)
    }
}
