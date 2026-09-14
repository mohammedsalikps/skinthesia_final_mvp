package com.skinthesia.ai.quality

import androidx.core.graphics.scale
import android.graphics.Bitmap
import android.graphics.PointF
import android.media.FaceDetector
import com.skinthesia.core.utils.BitmapDecoder
import com.skinthesia.domain.model.CheckStatus
import com.skinthesia.domain.model.PhotoQualityResult
import com.skinthesia.domain.model.QualityCheck
import com.skinthesia.domain.model.QualityDimension
import com.skinthesia.domain.model.QualityVerdict
import com.skinthesia.domain.model.RetakeGuidance
import com.skinthesia.domain.model.SkinPhoto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.math.roundToInt

/**
 * Real, on-device quality analysis. Lighting, exposure and sharpness are measured
 * from the pixels; face presence, position and angle come from Android's built-in
 * face detector. No image leaves the device and nothing is logged.
 */
class OnDevicePhotoQualityAnalyzer(
    private val scorer: QualityScorer = QualityScorer(),
    private val clock: () -> Long = System::currentTimeMillis,
) : PhotoQualityAnalyzer {

    override suspend fun analyze(photo: SkinPhoto): PhotoQualityResult = withContext(Dispatchers.Default) {
        val stats = runCatching { extract(photo.filePath) }.getOrNull()
            ?: return@withContext unreadable(photo.id)
        scorer.score(photo.id, stats, clock())
    }

    private suspend fun extract(path: String): ImageStats? {
        val (width, height) = BitmapDecoder.dimensions(path) ?: return null
        val bitmap = BitmapDecoder.decode(path, ANALYSIS_MAX_SIDE) ?: return null
        try {
            val luma = lumaStats(bitmap)
            val sharpness = laplacianVariance(bitmap)
            val (face, ran) = detectFace(bitmap)
            return ImageStats(
                width = width,
                height = height,
                meanLuminance = luma.first,
                shadowClipRatio = luma.second,
                highlightClipRatio = luma.third,
                sharpness = sharpness,
                face = face,
                faceDetectionRan = ran,
            )
        } finally {
            bitmap.recycle()
        }
    }

    /** Mean luma plus the share of near-black and near-white pixels. */
    private fun lumaStats(bitmap: Bitmap): Triple<Float, Float, Float> {
        val w = bitmap.width
        val h = bitmap.height
        val pixels = IntArray(w * h)
        bitmap.getPixels(pixels, 0, w, 0, 0, w, h)
        var sum = 0L
        var dark = 0
        var bright = 0
        for (p in pixels) {
            val y = luma(p)
            sum += y
            if (y < 16) dark++
            if (y > 242) bright++
        }
        val n = pixels.size.toFloat()
        return Triple(sum / n, dark / n, bright / n)
    }

    /** Variance of a 4-neighbour Laplacian on a 320 px wide greyscale copy. */
    private fun laplacianVariance(source: Bitmap): Float {
        val targetW = SHARPNESS_WIDTH
        val targetH = (source.height * (targetW.toFloat() / source.width)).roundToInt().coerceAtLeast(3)
        val small = source.scale(targetW, targetH)
        val pixels = IntArray(targetW * targetH)
        small.getPixels(pixels, 0, targetW, 0, 0, targetW, targetH)
        if (small !== source) small.recycle()
        val grey = IntArray(pixels.size) { luma(pixels[it]) }
        var sum = 0.0
        var sumSq = 0.0
        var count = 0
        for (y in 1 until targetH - 1) {
            for (x in 1 until targetW - 1) {
                val i = y * targetW + x
                val lap = 4 * grey[i] - grey[i - 1] - grey[i + 1] - grey[i - targetW] - grey[i + targetW]
                sum += lap
                sumSq += lap.toDouble() * lap
                count++
            }
        }
        if (count == 0) return 0f
        val mean = sum / count
        return (sumSq / count - mean * mean).toFloat()
    }

    /** Runs android.media.FaceDetector on an RGB_565 copy with an even width, as it requires. */
    private fun detectFace(source: Bitmap): Pair<FaceStats?, Boolean> = runCatching {
        var w = FACE_WIDTH.coerceAtMost(source.width)
        if (w % 2 != 0) w -= 1
        val h = (source.height * (w.toFloat() / source.width)).roundToInt()
        val scaled = source.scale(w, h)
        val rgb565 = scaled.copy(Bitmap.Config.RGB_565, false)
        if (scaled !== source) scaled.recycle()
        val faces = arrayOfNulls<FaceDetector.Face>(1)
        val found = FaceDetector(w, h, 1).findFaces(rgb565, faces)
        rgb565.recycle()
        val face = faces.firstOrNull()
        if (found == 0 || face == null) {
            null to true
        } else {
            val mid = PointF().also { face.getMidPoint(it) }
            FaceStats(
                confidence = face.confidence(),
                centerX = mid.x / w,
                centerY = mid.y / h,
                eyeDistanceRatio = face.eyesDistance() / w,
            ) to true
        }
    }.getOrElse { null to false }

    private fun unreadable(photoId: String) = PhotoQualityResult(
        photoId = photoId,
        checks = QualityDimension.entries.map {
            QualityCheck(it, CheckStatus.FAIL, 0f, "Couldn't read photo", "The image could not be opened.")
        },
        verdict = QualityVerdict.RETAKE_RECOMMENDED,
        primaryIssue = QualityDimension.RESOLUTION,
        guidance = RetakeGuidance(
            title = "We couldn't open that photo",
            message = "Please take a new photo or choose a different one.",
            tips = listOf("Use the in-app camera for best results"),
        ),
        analyzedAt = clock(),
    )

    private fun luma(argb: Int): Int {
        val r = (argb shr 16) and 0xFF
        val g = (argb shr 8) and 0xFF
        val b = argb and 0xFF
        return (r * 299 + g * 587 + b * 114) / 1000
    }

    private companion object {
        const val ANALYSIS_MAX_SIDE = 640
        const val SHARPNESS_WIDTH = 320
        const val FACE_WIDTH = 480
    }
}
