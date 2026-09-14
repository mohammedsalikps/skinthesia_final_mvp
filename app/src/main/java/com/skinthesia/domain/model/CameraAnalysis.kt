package com.skinthesia.domain.model

import kotlinx.serialization.Serializable

/** Visual indicators a vision model estimates from a selfie. Estimates, never diagnoses. */
@Serializable
enum class VisualIndicator(val label: String, val plain: String) {
    ACNE_APPEARANCE("Acne appearance", "blemishes"),
    PIGMENTATION("Pigmentation", "dark spots"),
    TEXTURE("Texture", "texture"),
    PORES("Pores", "pore visibility"),
    REDNESS("Redness", "redness"),
    DARK_CIRCLES("Dark circles", "under-eye shadows"),
    FINE_LINES("Fine lines", "fine lines"),
}

/** Facial zones used by visual findings and the face map. */
@Serializable
enum class SkinRegion(val label: String) {
    FOREHEAD("Forehead"),
    UNDER_EYES("Under eyes"),
    LEFT_CHEEK("Left cheek"),
    RIGHT_CHEEK("Right cheek"),
    NOSE("Nose"),
    AROUND_MOUTH("Around mouth"),
    CHIN("Chin"),
    JAWLINE("Jawline"),
}

@Serializable
enum class VisibilityLevel(val label: String) {
    MINIMAL("Minimal"),
    MILD("Mild"),
    MODERATE("Moderate"),
    NOTICEABLE("Noticeable"),
    ;

    companion object {
        fun fromLevel(level: Int): VisibilityLevel = when {
            level < 25 -> MINIMAL
            level < 45 -> MILD
            level < 65 -> MODERATE
            else -> NOTICEABLE
        }
    }
}

@Serializable
data class IndicatorEstimate(
    val indicator: VisualIndicator,
    /** 0..100, how visible the indicator appears. Higher means more visible. */
    val level: Int,
    /** 0..1 model confidence. */
    val confidence: Float,
    val regions: List<SkinRegion>,
) {
    val visibility: VisibilityLevel get() = VisibilityLevel.fromLevel(level)
}

@Serializable
data class CameraAnalysisResult(
    val id: String,
    val photoId: String,
    val estimates: List<IndicatorEstimate>,
    val analyzedAt: Long,
    val modelVersion: String,
    /** True when produced by the development mock rather than a validated model. */
    val isSimulated: Boolean,
    val source: DataSource = DataSource.VISUAL_ESTIMATE,
) {
    fun level(indicator: VisualIndicator): Int? = estimates.firstOrNull { it.indicator == indicator }?.level
}
