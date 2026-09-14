package com.skinthesia.ai.vision

import com.skinthesia.domain.model.CameraAnalysisResult
import com.skinthesia.domain.model.SkinConcern
import com.skinthesia.domain.model.SkinGoal
import com.skinthesia.domain.model.SkinPhoto

/** What a vision model may use beyond the pixels. */
data class VisionContext(
    /** Stable per user, so repeated analyses of the same person stay coherent. */
    val userSeed: Long,
    val concerns: Set<SkinConcern>,
    val goals: Set<SkinGoal>,
    val week: Int,
)

/**
 * Estimates visual indicators from a selfie. The mock backs the demonstration;
 * an on-device model, a cloud model or a third-party API implements the same
 * interface later. The UI never knows which one is in use; results carry
 * [CameraAnalysisResult.isSimulated] so they are always labelled honestly.
 */
interface SkinVisionAnalyzer {
    val modelName: String
    suspend fun analyze(photo: SkinPhoto, context: VisionContext): CameraAnalysisResult
}
