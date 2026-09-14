package com.skinthesia.hardware.probe

import com.skinthesia.domain.model.MeasurementRegion
import com.skinthesia.domain.model.ProbeDevice
import com.skinthesia.domain.model.SensorType
import kotlinx.coroutines.flow.Flow

/** Context that lets a provider keep readings coherent across a session and a journey. */
data class SensorReadContext(
    val sessionSeed: Long,
    /** Programme week of the assessment; lets the simulator model gradual change. */
    val week: Int,
)

/** One frame of values while the sensor settles on the skin. */
data class SensorFrame(
    val fraction: Float,
    val values: Map<SensorType, Double>,
    val isFinal: Boolean,
    /** False when the sensor lost contact with the skin during this frame. */
    val inContact: Boolean = true,
)

/**
 * Reads sensor values from a connected probe. A real implementation decodes the
 * device's characteristic notifications (format to be defined by the hardware spec).
 */
interface SensorDataProvider {
    fun read(device: ProbeDevice, region: MeasurementRegion, context: SensorReadContext): Flow<SensorFrame>
}
