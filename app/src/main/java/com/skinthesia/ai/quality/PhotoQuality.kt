package com.skinthesia.ai.quality

import com.skinthesia.domain.model.CheckStatus
import com.skinthesia.domain.model.PhotoQualityResult
import com.skinthesia.domain.model.QualityCheck
import com.skinthesia.domain.model.QualityDimension
import com.skinthesia.domain.model.QualityVerdict
import com.skinthesia.domain.model.RetakeGuidance
import com.skinthesia.domain.model.SkinPhoto
import kotlin.math.hypot

/** Face geometry from a detector, normalised to the image (0..1). */
data class FaceStats(
    val confidence: Float,
    val centerX: Float,
    val centerY: Float,
    /** Distance between the eyes divided by image width; a proxy for face size. */
    val eyeDistanceRatio: Float,
)

/** Measurable properties of a captured image, computed on-device. */
data class ImageStats(
    val width: Int,
    val height: Int,
    /** Mean luma, 0..255. */
    val meanLuminance: Float,
    /** Share of pixels that are nearly black. */
    val shadowClipRatio: Float,
    /** Share of pixels that are nearly white. */
    val highlightClipRatio: Float,
    /** Variance of the Laplacian on a downscaled greyscale copy; higher is sharper. */
    val sharpness: Float,
    val face: FaceStats?,
    /** False when face detection could not run (the face checks are then not evaluated). */
    val faceDetectionRan: Boolean,
)

/** CapturedImage → PhotoQualityAnalyzer → PhotoQualityResult. */
interface PhotoQualityAnalyzer {
    suspend fun analyze(photo: SkinPhoto): PhotoQualityResult
}

/**
 * Pure, deterministic rules that turn [ImageStats] into a friendly quality result.
 * Kept free of Android types so it is fully unit-testable.
 */
class QualityScorer {

    fun score(photoId: String, stats: ImageStats, analyzedAt: Long): PhotoQualityResult {
        val checks = listOf(
            lighting(stats),
            exposure(stats),
            sharpness(stats),
            faceVisibility(stats),
            faceAngle(stats),
            framing(stats),
            resolution(stats),
        )
        val verdict = when {
            checks.any { it.status == CheckStatus.FAIL } -> QualityVerdict.RETAKE_RECOMMENDED
            checks.any { it.status == CheckStatus.WARN } -> QualityVerdict.ACCEPTABLE
            else -> QualityVerdict.GOOD
        }
        val primary = PRIORITY.firstOrNull { dimension ->
            checks.first { it.dimension == dimension }.status == CheckStatus.FAIL
        } ?: PRIORITY.firstOrNull { dimension ->
            checks.first { it.dimension == dimension }.status == CheckStatus.WARN
        }
        val guidance = primary?.let { guidanceFor(it, stats) }
        return PhotoQualityResult(
            photoId = photoId,
            checks = checks,
            verdict = verdict,
            primaryIssue = primary,
            guidance = guidance,
            analyzedAt = analyzedAt,
        )
    }

    private fun lighting(s: ImageStats): QualityCheck {
        val l = s.meanLuminance
        return when {
            l < 55f -> check(QualityDimension.LIGHTING, CheckStatus.FAIL, l / 110f, "Too dark", "Your face is hard to see in this light.")
            l < 80f -> check(QualityDimension.LIGHTING, CheckStatus.WARN, l / 110f, "A little dim", "More even light will make the analysis more reliable.")
            l > 222f -> check(QualityDimension.LIGHTING, CheckStatus.FAIL, 0.3f, "Too bright", "The light is washing out detail.")
            l > 205f -> check(QualityDimension.LIGHTING, CheckStatus.WARN, 0.6f, "Quite bright", "Softer light would show detail better.")
            else -> check(QualityDimension.LIGHTING, CheckStatus.PASS, 1f, "Good lighting", "Even, natural light.")
        }
    }

    private fun exposure(s: ImageStats): QualityCheck = when {
        s.highlightClipRatio > 0.10f -> check(QualityDimension.EXPOSURE, CheckStatus.FAIL, 0.3f, "Overexposed", "Bright areas have lost detail.")
        s.shadowClipRatio > 0.30f -> check(QualityDimension.EXPOSURE, CheckStatus.FAIL, 0.3f, "Underexposed", "Dark areas have lost detail.")
        s.highlightClipRatio > 0.04f -> check(QualityDimension.EXPOSURE, CheckStatus.WARN, 0.65f, "Some highlights", "A little glare on the skin.")
        s.shadowClipRatio > 0.15f -> check(QualityDimension.EXPOSURE, CheckStatus.WARN, 0.65f, "Some deep shadows", "Part of the face is in shadow.")
        else -> check(QualityDimension.EXPOSURE, CheckStatus.PASS, 1f, "Balanced exposure", "Highlights and shadows hold detail.")
    }

    private fun sharpness(s: ImageStats): QualityCheck = when {
        s.sharpness < 18f -> check(QualityDimension.SHARPNESS, CheckStatus.FAIL, s.sharpness / 60f, "Blurry", "The photo is out of focus or moved.")
        s.sharpness < 40f -> check(QualityDimension.SHARPNESS, CheckStatus.WARN, s.sharpness / 60f, "Slightly soft", "Holding still will sharpen detail.")
        else -> check(QualityDimension.SHARPNESS, CheckStatus.PASS, 1f, "Sharp", "Fine detail is clear.")
    }

    private fun faceVisibility(s: ImageStats): QualityCheck {
        if (!s.faceDetectionRan) return notEvaluated(QualityDimension.FACE_VISIBILITY)
        val face = s.face
        return when {
            face == null -> check(QualityDimension.FACE_VISIBILITY, CheckStatus.FAIL, 0f, "Face not found", "We couldn't find a face in this photo.")
            face.confidence < 0.35f -> check(QualityDimension.FACE_VISIBILITY, CheckStatus.WARN, face.confidence, "Face partly visible", "Part of your face may be covered or turned.")
            else -> check(QualityDimension.FACE_VISIBILITY, CheckStatus.PASS, face.confidence, "Face visible", "Your whole face is in view.")
        }
    }

    private fun faceAngle(s: ImageStats): QualityCheck {
        val face = s.face ?: return notEvaluated(QualityDimension.FACE_ANGLE)
        // The on-device detector only finds near-frontal faces, so its confidence is a
        // reasonable proxy for how straight on the camera the face is.
        return if (face.confidence >= 0.45f) {
            check(QualityDimension.FACE_ANGLE, CheckStatus.PASS, face.confidence, "Facing the camera", "Straight-on angle.")
        } else {
            check(QualityDimension.FACE_ANGLE, CheckStatus.WARN, face.confidence, "Slightly turned", "Look straight into the camera.")
        }
    }

    private fun framing(s: ImageStats): QualityCheck {
        val face = s.face ?: return notEvaluated(QualityDimension.FRAMING)
        val offset = hypot(face.centerX - 0.5f, face.centerY - 0.45f)
        return when {
            offset > 0.20f -> check(QualityDimension.FRAMING, CheckStatus.FAIL, 1f - offset, "Off-centre", "Your face is near the edge of the frame.")
            face.eyeDistanceRatio < 0.09f -> check(QualityDimension.FRAMING, CheckStatus.FAIL, 0.3f, "Too far away", "Your face is small in the frame.")
            offset > 0.12f -> check(QualityDimension.FRAMING, CheckStatus.WARN, 1f - offset, "Slightly off-centre", "Center your face in the guide.")
            face.eyeDistanceRatio < 0.12f -> check(QualityDimension.FRAMING, CheckStatus.WARN, 0.6f, "A little far", "Move a little closer.")
            face.eyeDistanceRatio > 0.36f -> check(QualityDimension.FRAMING, CheckStatus.WARN, 0.6f, "A little close", "Move back slightly.")
            else -> check(QualityDimension.FRAMING, CheckStatus.PASS, 1f, "Correct position", "Centered and well framed.")
        }
    }

    private fun resolution(s: ImageStats): QualityCheck {
        val minSide = minOf(s.width, s.height)
        return when {
            minSide < 480 -> check(QualityDimension.RESOLUTION, CheckStatus.FAIL, minSide / 720f, "Low resolution", "The image is too small to analyse.")
            minSide < 720 -> check(QualityDimension.RESOLUTION, CheckStatus.WARN, minSide / 720f, "Modest resolution", "A higher-quality photo gives more detail.")
            else -> check(QualityDimension.RESOLUTION, CheckStatus.PASS, 1f, "Good image quality", "Plenty of detail to work with.")
        }
    }

    private fun guidanceFor(dimension: QualityDimension, s: ImageStats): RetakeGuidance = when (dimension) {
        QualityDimension.FACE_VISIBILITY -> RetakeGuidance(
            title = "We couldn't see your face clearly",
            message = "Make sure your whole face sits inside the guide and nothing covers it.",
            tips = listOf("Tie back hair from your face", "Remove glasses or a mask", "Hold the phone at eye level"),
        )
        QualityDimension.LIGHTING -> if (s.meanLuminance > 205f) {
            RetakeGuidance(
                title = "Your photo is a little bright",
                message = "Softer light will show your skin's detail more accurately.",
                tips = listOf("Step out of direct sunlight", "Face a window with a sheer curtain", "Turn off the flash"),
            )
        } else {
            RetakeGuidance(
                title = "Your photo needs better lighting",
                message = "Soft, even daylight on your face gives the most reliable analysis.",
                tips = listOf("Face a window or soft daylight", "Avoid bright light behind you", "Turn off beauty filters"),
            )
        }
        QualityDimension.EXPOSURE -> RetakeGuidance(
            title = "The light is uneven",
            message = "Part of your face is washed out or in deep shadow.",
            tips = listOf("Turn so light falls evenly on both cheeks", "Avoid overhead spotlights", "Wipe away shine with a tissue"),
        )
        QualityDimension.SHARPNESS -> RetakeGuidance(
            title = "Your photo looks a little blurry",
            message = "A steady, focused photo lets us see texture properly.",
            tips = listOf("Rest your elbows to hold still", "Wipe the camera lens", "Wait a moment before tapping the shutter"),
        )
        QualityDimension.FRAMING -> {
            val face = s.face
            when {
                face != null && face.eyeDistanceRatio < 0.12f -> RetakeGuidance(
                    title = "Move a little closer",
                    message = "Your face should fill most of the guide.",
                    tips = listOf("Hold the phone about an arm's length away", "Keep your whole face inside the oval"),
                )
                face != null && face.eyeDistanceRatio > 0.36f -> RetakeGuidance(
                    title = "Move back slightly",
                    message = "Leave a little space around your face inside the guide.",
                    tips = listOf("Hold the phone a little further away", "Keep your chin and forehead in frame"),
                )
                else -> RetakeGuidance(
                    title = "Please center your face",
                    message = "Line your face up inside the oval guide.",
                    tips = listOf("Hold the phone at eye level", "Keep your nose near the center of the guide"),
                )
            }
        }
        QualityDimension.FACE_ANGLE -> RetakeGuidance(
            title = "Face the camera straight on",
            message = "A front-facing photo lets us compare both sides of your face fairly.",
            tips = listOf("Look directly into the lens", "Keep your head level"),
        )
        QualityDimension.RESOLUTION -> RetakeGuidance(
            title = "This image is too small",
            message = "Use the camera, or pick a higher-resolution photo from your gallery.",
            tips = listOf("Avoid screenshots or compressed images", "Use the rear camera for extra detail"),
        )
    }

    private fun check(dimension: QualityDimension, status: CheckStatus, score: Float, headline: String, detail: String) =
        QualityCheck(dimension, status, score.coerceIn(0f, 1f), headline, detail)

    private fun notEvaluated(dimension: QualityDimension) =
        QualityCheck(dimension, CheckStatus.NOT_EVALUATED, 0f, "Not checked", "Depends on finding your face first.")

    private companion object {
        val PRIORITY = listOf(
            QualityDimension.FACE_VISIBILITY,
            QualityDimension.LIGHTING,
            QualityDimension.EXPOSURE,
            QualityDimension.SHARPNESS,
            QualityDimension.FRAMING,
            QualityDimension.FACE_ANGLE,
            QualityDimension.RESOLUTION,
        )
    }
}
