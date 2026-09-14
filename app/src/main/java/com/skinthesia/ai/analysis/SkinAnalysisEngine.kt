package com.skinthesia.ai.analysis

import com.skinthesia.domain.model.Assessment
import com.skinthesia.domain.model.CombinedAnalysis
import com.skinthesia.domain.model.MeasurementSession
import com.skinthesia.domain.model.UserProfile

data class AnalysisInput(
    val assessment: Assessment,
    val profile: UserProfile,
    val session: MeasurementSession?,
    val now: Long,
)

/**
 * Combines camera estimates, probe readings, lifestyle context and goals into a
 * [CombinedAnalysis] with the SkinPrint, focus areas, strengths and insights.
 * [MockSkinAnalysisEngine] is the development implementation; a cloud or on-device
 * model replaces it behind this interface.
 */
interface SkinAnalysisEngine {
    val engineName: String
    suspend fun analyze(input: AnalysisInput): CombinedAnalysis
}
