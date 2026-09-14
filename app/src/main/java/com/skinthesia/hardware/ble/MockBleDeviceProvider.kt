package com.skinthesia.hardware.ble

import com.skinthesia.domain.model.ProbeDevice
import com.skinthesia.domain.model.ProbeError
import com.skinthesia.domain.model.SensorType
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

/**
 * Development stand-in for the probe transport. It "finds" simulated probes after a
 * realistic delay and connects to them. Every device it returns is flagged
 * [ProbeDevice.isSimulated] so readings are never mistaken for real measurements.
 */
class MockBleDeviceProvider(
    /** Consulted on each connection attempt; returning true makes that attempt fail. */
    private val shouldFailConnection: suspend () -> Boolean = { false },
    private val timeScale: Float = 1f,
) : BleDeviceProvider {

    override val requiredPermissions: List<String> = emptyList()

    override fun isTransportAvailable(): Boolean = true

    override fun scan(): Flow<List<ProbeDevice>> = flow {
        emit(emptyList())
        pause(1400)
        emit(listOf(PRIMARY))
        pause(1100)
        emit(listOf(PRIMARY, SECONDARY))
        pause(700)
    }

    override suspend fun connect(deviceId: String): ProbeDevice {
        pause(1500)
        if (shouldFailConnection()) throw ProbeException(ProbeError.CONNECTION_FAILED)
        return when (deviceId) {
            PRIMARY.id -> PRIMARY
            SECONDARY.id -> SECONDARY
            else -> throw ProbeException(ProbeError.NOT_FOUND)
        }
    }

    override suspend fun disconnect(deviceId: String) {
        pause(200)
    }

    private suspend fun pause(millis: Long) {
        if (timeScale > 0f) delay((millis * timeScale).toLong())
    }

    companion object {
        private val DEVELOPMENT_SENSORS = setOf(SensorType.PH, SensorType.HYDRATION, SensorType.TEMPERATURE)

        val PRIMARY = ProbeDevice(
            id = "SIM-01A3",
            name = "Skinthesia Probe",
            model = "Development simulator",
            firmwareVersion = "sim-0.9.2",
            signalStrength = -47,
            batteryPercent = 86,
            capabilities = DEVELOPMENT_SENSORS,
            isSimulated = true,
        )

        val SECONDARY = ProbeDevice(
            id = "SIM-7C19",
            name = "Skinthesia Probe",
            model = "Development simulator",
            firmwareVersion = "sim-0.9.2",
            signalStrength = -72,
            batteryPercent = 54,
            capabilities = DEVELOPMENT_SENSORS,
            isSimulated = true,
        )
    }
}
