package com.skinthesia.domain.model

import kotlinx.serialization.Serializable

@Serializable
enum class PhotoSource { CAMERA, GALLERY }

@Serializable
enum class LensFacing { FRONT, BACK }

/** A selfie stored privately on the device. It is the CapturedImage the quality pipeline consumes. */
@Serializable
data class SkinPhoto(
    val id: String,
    /** Absolute path in app-private storage. Never logged. */
    val filePath: String,
    val capturedAt: Long,
    val source: PhotoSource,
    val lensFacing: LensFacing? = null,
    val mirrored: Boolean = false,
)

@Serializable
enum class QualityDimension(val label: String) {
    LIGHTING("Lighting"),
    EXPOSURE("Exposure"),
    SHARPNESS("Sharpness"),
    FACE_VISIBILITY("Face visibility"),
    FACE_ANGLE("Angle"),
    FRAMING("Position"),
    RESOLUTION("Image quality"),
}

@Serializable
enum class CheckStatus { PASS, WARN, FAIL, NOT_EVALUATED }

@Serializable
data class QualityCheck(
    val dimension: QualityDimension,
    val status: CheckStatus,
    /** 0..1, higher is better. */
    val score: Float,
    val headline: String,
    val detail: String,
)

@Serializable
enum class QualityVerdict { GOOD, ACCEPTABLE, RETAKE_RECOMMENDED }

@Serializable
data class RetakeGuidance(
    val title: String,
    val message: String,
    val tips: List<String>,
)

/** Output of the photo quality pipeline: CapturedImage → PhotoQualityAnalyzer → PhotoQualityResult. */
@Serializable
data class PhotoQualityResult(
    val photoId: String,
    val checks: List<QualityCheck>,
    val verdict: QualityVerdict,
    val primaryIssue: QualityDimension?,
    val guidance: RetakeGuidance?,
    val analyzedAt: Long,
) {
    fun check(dimension: QualityDimension): QualityCheck? = checks.firstOrNull { it.dimension == dimension }

    /** The four friendly lines shown on the quality screen. */
    val summary: List<QualitySummary>
        get() = QualitySummaryLine.entries.map { line ->
            val statuses = line.dimensions.mapNotNull { check(it)?.status }.filter { it != CheckStatus.NOT_EVALUATED }
            val status = when {
                statuses.isEmpty() -> CheckStatus.NOT_EVALUATED
                CheckStatus.FAIL in statuses -> CheckStatus.FAIL
                CheckStatus.WARN in statuses -> CheckStatus.WARN
                else -> CheckStatus.PASS
            }
            QualitySummary(line, status)
        }
}

@Serializable
enum class QualitySummaryLine(
    val goodLabel: String,
    val issueLabel: String,
    val dimensions: List<QualityDimension>,
) {
    LIGHTING("Good lighting", "Lighting needs work", listOf(QualityDimension.LIGHTING, QualityDimension.EXPOSURE)),
    FACE("Face visible", "Face not clearly visible", listOf(QualityDimension.FACE_VISIBILITY)),
    POSITION("Correct position", "Position needs adjusting", listOf(QualityDimension.FRAMING, QualityDimension.FACE_ANGLE)),
    IMAGE("Good image quality", "Image is not sharp enough", listOf(QualityDimension.SHARPNESS, QualityDimension.RESOLUTION)),
}

data class QualitySummary(val line: QualitySummaryLine, val status: CheckStatus)
