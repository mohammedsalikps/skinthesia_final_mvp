package com.skinthesia.ai.quality

import com.skinthesia.domain.model.CheckStatus
import com.skinthesia.domain.model.QualityDimension
import com.skinthesia.domain.model.QualitySummaryLine
import com.skinthesia.domain.model.QualityVerdict
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class QualityScorerTest {

    private val scorer = QualityScorer()

    private fun stats(
        luminance: Float = 140f,
        shadow: Float = 0.01f,
        highlight: Float = 0.01f,
        sharpness: Float = 120f,
        face: FaceStats? = FaceStats(confidence = 0.62f, centerX = 0.5f, centerY = 0.46f, eyeDistanceRatio = 0.2f),
        detectionRan: Boolean = true,
        width: Int = 1080,
        height: Int = 1440,
    ) = ImageStats(width, height, luminance, shadow, highlight, sharpness, face, detectionRan)

    private fun score(stats: ImageStats) = scorer.score("photo", stats, 0L)

    @Test
    fun `a well lit, sharp, centred selfie passes every check`() {
        val result = score(stats())
        assertEquals(QualityVerdict.GOOD, result.verdict)
        assertNull(result.primaryIssue)
        assertNull(result.guidance)
        assertTrue(result.summary.all { it.status == CheckStatus.PASS })
    }

    @Test
    fun `a dark photo asks for better lighting`() {
        val result = score(stats(luminance = 40f))
        assertEquals(QualityVerdict.RETAKE_RECOMMENDED, result.verdict)
        assertEquals(QualityDimension.LIGHTING, result.primaryIssue)
        assertEquals("Your photo needs better lighting", result.guidance?.title)
    }

    @Test
    fun `a missing face takes priority and skips dependent checks`() {
        val result = score(stats(luminance = 40f, face = null))
        assertEquals(QualityDimension.FACE_VISIBILITY, result.primaryIssue)
        assertEquals(CheckStatus.NOT_EVALUATED, result.check(QualityDimension.FRAMING)?.status)
        assertEquals(CheckStatus.NOT_EVALUATED, result.check(QualityDimension.FACE_ANGLE)?.status)
        assertEquals(CheckStatus.FAIL, result.summary.first { it.line == QualitySummaryLine.FACE }.status)
    }

    @Test
    fun `an off-centre face is asked to center`() {
        val result = score(stats(face = FaceStats(0.7f, 0.85f, 0.46f, 0.2f)))
        assertEquals(QualityDimension.FRAMING, result.primaryIssue)
        assertEquals("Please center your face", result.guidance?.title)
    }

    @Test
    fun `a small face is asked to move closer`() {
        val result = score(stats(face = FaceStats(0.7f, 0.5f, 0.46f, 0.07f)))
        assertEquals(QualityDimension.FRAMING, result.primaryIssue)
        assertEquals("Move a little closer", result.guidance?.title)
    }

    @Test
    fun `a blurry photo fails sharpness and the image quality line`() {
        val result = score(stats(sharpness = 9f))
        assertEquals(QualityDimension.SHARPNESS, result.primaryIssue)
        assertEquals(CheckStatus.FAIL, result.summary.first { it.line == QualitySummaryLine.IMAGE }.status)
    }

    @Test
    fun `warnings alone keep the photo usable`() {
        val result = score(stats(luminance = 70f))
        assertEquals(QualityVerdict.ACCEPTABLE, result.verdict)
        assertEquals(QualityDimension.LIGHTING, result.primaryIssue)
    }

    @Test
    fun `when face detection cannot run the face checks are not evaluated`() {
        val result = score(stats(face = null, detectionRan = false))
        assertEquals(QualityVerdict.GOOD, result.verdict)
        assertEquals(CheckStatus.NOT_EVALUATED, result.check(QualityDimension.FACE_VISIBILITY)?.status)
    }

    @Test
    fun `tiny images fail resolution`() {
        val result = score(stats(width = 320, height = 400))
        assertEquals(CheckStatus.FAIL, result.check(QualityDimension.RESOLUTION)?.status)
    }
}
