package com.skinthesia.hardware.ble

import com.skinthesia.domain.model.ProbeDevice
import com.skinthesia.domain.model.ProbeError
import kotlinx.coroutines.flow.Flow

/**
 * Transport-level discovery and connection for Skinthesia probes.
 *
 * The real hardware specification (service UUIDs, characteristics, packet formats,
 * commands) has not been supplied, so no Bluetooth protocol is implemented here.
 * A future `AndroidBleDeviceProvider` implements this interface with the Android BLE
 * stack once the specification exists; nothing above this layer needs to change.
 */
interface BleDeviceProvider {
    /** Runtime permissions the transport needs. The simulator needs none. */
    val requiredPermissions: List<String>

    /** False when the phone has no usable Bluetooth adapter or it is switched off. */
    fun isTransportAvailable(): Boolean

    /** Emits the growing list of devices found during one scan window, then completes. */
    fun scan(): Flow<List<ProbeDevice>>

    /** Connects and returns the device with fresh battery and firmware details. */
    @Throws(ProbeException::class)
    suspend fun connect(deviceId: String): ProbeDevice

    suspend fun disconnect(deviceId: String)
}

/** Probe failures travel as typed errors so the UI can always offer a way forward. */
class ProbeException(val error: ProbeError) : Exception(error.title)
