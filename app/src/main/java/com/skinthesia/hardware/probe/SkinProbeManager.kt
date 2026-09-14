package com.skinthesia.hardware.probe

import com.skinthesia.domain.model.MeasurementRegion
import com.skinthesia.domain.model.MeasurementSession
import com.skinthesia.domain.model.ProbeDevice
import com.skinthesia.domain.model.ProbeState
import com.skinthesia.domain.model.RegionMeasurementEvent
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

/**
 * The single entry point the UI uses for the probe: discovery, connection,
 * calibration and measurement. [MockSkinProbeManager] backs the demonstration; a
 * `RealBleSkinProbeManager` built on a real [com.skinthesia.hardware.ble.BleDeviceProvider]
 * and [SensorDataProvider] replaces it once the hardware protocol is available.
 */
interface SkinProbeManager {
    val state: StateFlow<ProbeState>

    /** Runtime permissions needed before discovery. Empty for the simulator. */
    val requiredPermissions: List<String>

    val connectedDevice: ProbeDevice?

    fun startDiscovery()
    fun stopDiscovery()

    /** Connects to [device]; returns false and moves to [ProbeState.Failed] on error. */
    suspend fun connect(device: ProbeDevice): Boolean

    /** Reconnects to a previously paired device without a full scan. */
    suspend fun reconnect(deviceId: String): Boolean

    /** Runs the pre-measurement checks: connection, sensors, measurement readiness. */
    suspend fun calibrate(): Boolean

    /** Measures one region. Emits progress with live values, then a result or an error. */
    fun measure(region: MeasurementRegion, session: MeasurementSession, week: Int): Flow<RegionMeasurementEvent>

    suspend fun disconnect()

    /** Clears a failure so the user can try again. */
    fun acknowledgeError()
}
