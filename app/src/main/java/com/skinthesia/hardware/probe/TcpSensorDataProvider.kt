package com.skinthesia.hardware.probe

import com.skinthesia.domain.model.MeasurementRegion
import com.skinthesia.domain.model.ProbeDevice
import com.skinthesia.domain.model.SensorType
import com.skinthesia.hardware.wifi.EspProbeSocket
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

/**
 * Reads the real Skinthesia ESP32 probe over the [EspProbeSocket] shared with
 * [com.skinthesia.hardware.wifi.WifiSocketDeviceProvider]. Sends `TEST_PH`
 * and `TEST_HYDRATION` (see `SRC_CODE.ino`) and parses each bare-decimal
 * response into the matching [SensorType]. [region] and [context] are
 * accepted only to satisfy this interface's existing signature: the firmware
 * has no region parameter at all, so which sensor is read never depends on
 * them - the existing measurement flow (unmodified) still tags each result
 * with whichever region the user is physically holding the probe against.
 *
 * IMPORTANT - sebum is intentionally not read here. The firmware's
 * `TEST_SEBUM` answer is one of the text categories `DRY` / `NORMAL` / `OILY`,
 * never a number, but `SensorType.SEBUM` is defined as a `Double` (see
 * `domain/model/Probe.kt`) and `SensorMeasurement.value` is a `Double` too.
 * There is no honest number to put there - mapping the category to an
 * invented 0-100 index was explicitly out of scope for this change - so
 * `TEST_SEBUM` is not sent, and [WifiSocketDeviceProvider.DEVICE] does not
 * advertise `SEBUM` as a capability. Representing the real sebum category
 * honestly would need either a domain-model change (a non-numeric sensor
 * value) or a separate, non-numeric UI surface - both are decisions for a
 * later step, not implemented here.
 *
 * A response that fails to parse (`HYDRATION_ERROR`, `INVALID_REQUEST`, an
 * empty line, or no response at all because the command timed out or the
 * connection dropped) is simply left out of the emitted values, exactly as
 * an unreachable sensor would be - never replaced with a fabricated number.
 * If every requested sensor fails this way, the frame is marked
 * `inContact = false`, which [MockSkinProbeManager] (unmodified) already
 * turns into a `ProbeError.POOR_CONTACT` failure with the existing retry UI -
 * the closest existing error this transport can signal through
 * [SensorFrame], since the interface has no other channel for a failed read.
 */
class TcpSensorDataProvider(
    private val socket: EspProbeSocket,
) : SensorDataProvider {

    override fun read(device: ProbeDevice, region: MeasurementRegion, context: SensorReadContext): Flow<SensorFrame> = flow {
        val requested = device.capabilities
        val values = mutableMapOf<SensorType, Double>()

        if (SensorType.PH in requested) {
            parsePh(socket.sendCommand(CMD_TEST_PH))?.let { values[SensorType.PH] = it }
            emit(SensorFrame(fraction = 0.5f, values = values.toMap(), isFinal = false))
        }
        if (SensorType.HYDRATION in requested) {
            parseHydration(socket.sendCommand(CMD_TEST_HYDRATION))?.let { values[SensorType.HYDRATION] = it }
        }

        if (values.isEmpty()) {
            emit(SensorFrame(fraction = 1f, values = emptyMap(), isFinal = true, inContact = false))
        } else {
            emit(SensorFrame(fraction = 1f, values = values.toMap(), isFinal = true))
        }
    }

    /** Firmware sends a bare decimal, e.g. "5.8" - a real number received from the real device, passed through unchanged. It is not derived from a physical pH electrode on the firmware side (see SRC_CODE.ino: `getRandomNormalPH()`); that is a firmware-truth gap, not something this parser can fix. */
    private fun parsePh(response: String?): Double? {
        val text = response?.trim()?.removePrefix("AUTO_PH:")?.trim() ?: return null
        return text.toDoubleOrNull()
    }

    /** Firmware sends a bare centimeter distance, e.g. "23.45", or "HYDRATION_ERROR". Passed through unconverted - no hydration-index formula exists in the firmware to reuse, so none is invented here. */
    private fun parseHydration(response: String?): Double? {
        val text = response?.trim() ?: return null
        if (text.equals("HYDRATION_ERROR", ignoreCase = true)) return null
        return text.toDoubleOrNull()
    }

    private companion object {
        const val CMD_TEST_PH = "TEST_PH"
        const val CMD_TEST_HYDRATION = "TEST_HYDRATION"
    }
}
