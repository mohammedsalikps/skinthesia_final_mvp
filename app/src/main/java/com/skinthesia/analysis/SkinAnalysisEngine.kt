package com.skinthesia.analysis

import com.skinthesia.domain.model.SkinMeasurement
import com.skinthesia.domain.model.SkinPhoto
import com.skinthesia.domain.model.SkinPrint
import com.skinthesia.domain.model.SkinProfile
import kotlinx.coroutines.flow.Flow

/** Inputs an engine needs to produce a SkinPrint. */
data class SkinAnalysisRequest(
    val profile: SkinProfile,
    val photo: SkinPhoto?,
)

/** Everything an analysis run yields. */
data class SkinAnalysisResult(
    val skinPrint: SkinPrint,
    val measurement: SkinMeasurement,
)

/** A frame of the live measurement stream shown while the probe is reading. */
data class MeasurementProgress(
    /** 0f..1f */
    val fraction: Float,
    /** Readings so far; null until the first sample. */
    val partial: SkinMeasurement?,
) {
    val isComplete: Boolean get() = fraction >= 1f
}

/**
 * Boundary between the UI and skin analysis.
 *
 * [MockSkinAnalysisEngine] simulates a DermoScan device locally. A future
 * RealSkinAnalysisEngine can wrap hardware and a backend model behind the
 * same contract without any UI changes.
 */
interface SkinAnalysisEngine {
    /** True when readings are simulated rather than measured. */
    val isSimulated: Boolean

    /** Streams live probe readings until [MeasurementProgress.isComplete]. */
    fun measure(): Flow<MeasurementProgress>

    /** Produces a full SkinPrint for the given profile and photo. */
    suspend fun analyze(request: SkinAnalysisRequest): SkinAnalysisResult
}
