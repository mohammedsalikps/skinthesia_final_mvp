package com.skinthesia.domain.model

import kotlinx.serialization.Serializable

@Serializable
enum class SkinPrintDimension(val label: String, val description: String) {
    CLARITY("Clarity", "How clear skin looks, from blemish and redness estimates."),
    EVEN_TONE("Even tone", "How even colour looks, from pigmentation and under-eye estimates."),
    TEXTURE("Texture", "How smooth the surface looks, from texture and fine-line estimates."),
    HYDRATION("Hydration", "How hydrated skin reads, from probe readings when available."),
    PORE_APPEARANCE("Pore appearance", "How visible pores look in your photo."),
}

@Serializable
data class SkinScore(
    val dimension: SkinPrintDimension,
    /** 0..100, higher is better. A tracking indicator, not a medical measurement. */
    val value: Int,
    val sources: Set<DataSource>,
    /** 0..1, how much data supports this score. */
    val confidence: Float,
    val explanation: String,
)

@Serializable
enum class ScoreBand(val label: String) {
    BUILDING("Building"),
    DEVELOPING("Developing"),
    GOOD("Good"),
    EXCELLENT("Excellent"),
    ;

    companion object {
        fun fromScore(score: Int): ScoreBand = when {
            score >= 80 -> EXCELLENT
            score >= 65 -> GOOD
            score >= 50 -> DEVELOPING
            else -> BUILDING
        }
    }
}

/** Which kinds of input went into a SkinPrint, so the UI can explain it truthfully. */
@Serializable
data class SkinPrintInputs(
    val hasCamera: Boolean,
    val cameraSimulated: Boolean,
    val hasSensors: Boolean,
    val sensorsSimulated: Boolean,
    val hasSelfReport: Boolean,
)

/**
 * SKINPRINT™: the composite tracking indicator derived from camera estimates, probe
 * readings and self-reported context. Calculated by SkinPrintCalculator, never hardcoded.
 */
@Serializable
data class SkinPrint(
    val id: String,
    val assessmentId: String,
    val overall: Int,
    val scores: List<SkinScore>,
    val band: ScoreBand,
    val week: Int,
    val createdAt: Long,
    val inputs: SkinPrintInputs,
    val algorithmVersion: String,
) {
    fun score(dimension: SkinPrintDimension): SkinScore? = scores.firstOrNull { it.dimension == dimension }
    fun value(dimension: SkinPrintDimension): Int = score(dimension)?.value ?: 0
}
