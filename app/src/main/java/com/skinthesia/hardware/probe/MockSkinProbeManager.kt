package com.skinthesia.hardware.probe

import com.skinthesia.domain.model.DataSource
import com.skinthesia.domain.model.Ids
import com.skinthesia.domain.model.MeasurementRegion
import com.skinthesia.domain.model.MeasurementSession
import com.skinthesia.domain.model.ProbeDevice
import com.skinthesia.domain.model.ProbeError
import com.skinthesia.domain.model.ProbeState
import com.skinthesia.domain.model.RegionMeasurementEvent
import com.skinthesia.domain.model.SensorMeasurement
import com.skinthesia.hardware.ble.BleDeviceProvider
import com.skinthesia.hardware.ble.MockBleDeviceProvider
import com.skinthesia.hardware.ble.ProbeException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch

/**
 * Simulates the whole probe lifecycle: discovery, connection, battery, calibration,
 * measurement and transfer. It composes the same [BleDeviceProvider] and
 * [SensorDataProvider] seams a real implementation would, so swapping to hardware is
 * a matter of injecting different providers.
 */
class MockSkinProbeManager(
    private val scope: CoroutineScope,
    private val ble: BleDeviceProvider = MockBleDeviceProvider(),
    private val sensors: SensorDataProvider = SimulatedSensorDataProvider(),
    private val clock: () -> Long = System::currentTimeMillis,
    private val calibrationStepMillis: Long = 800,
) : SkinProbeManager {

    private val _state = MutableStateFlow<ProbeState>(ProbeState.Idle)
    override val state: StateFlow<ProbeState> = _state.asStateFlow()

    override val requiredPermissions: List<String> get() = ble.requiredPermissions

    override var connectedDevice: ProbeDevice? = null
        private set

    private var scanJob: Job? = null

    override fun startDiscovery() {
        if (!ble.isTransportAvailable()) {
            _state.value = ProbeState.Failed(ProbeError.BLUETOOTH_UNAVAILABLE, null)
            return
        }
        scanJob?.cancel()
        _state.value = ProbeState.Scanning(emptyList())
        scanJob = scope.launch {
            var last = emptyList<ProbeDevice>()
            ble.scan().collect { found ->
                last = found
                if (_state.value is ProbeState.Scanning) _state.value = ProbeState.Scanning(found)
            }
            if (last.isEmpty() && _state.value is ProbeState.Scanning) {
                _state.value = ProbeState.Failed(ProbeError.NOT_FOUND, null)
            }
        }
    }

    override fun stopDiscovery() {
        scanJob?.cancel()
        scanJob = null
    }

    override suspend fun connect(device: ProbeDevice): Boolean {
        stopDiscovery()
        _state.value = ProbeState.Connecting(device)
        return try {
            val connected = ble.connect(device.id)
            connectedDevice = connected
            _state.value = ProbeState.Connected(connected)
            true
        } catch (e: ProbeException) {
            connectedDevice = null
            _state.value = ProbeState.Failed(e.error, device)
            false
        }
    }

    override suspend fun reconnect(deviceId: String): Boolean {
        val known = listOf(MockBleDeviceProvider.PRIMARY, MockBleDeviceProvider.SECONDARY).firstOrNull { it.id == deviceId }
            ?: MockBleDeviceProvider.PRIMARY
        return connect(known)
    }

    override suspend fun calibrate(): Boolean {
        val device = connectedDevice ?: run {
            _state.value = ProbeState.Failed(ProbeError.DISCONNECTED, null)
            return false
        }
        for (step in 1..CALIBRATION_STEPS) {
            _state.value = ProbeState.Calibrating(device, (step - 1).toFloat() / CALIBRATION_STEPS)
            delay(calibrationStepMillis)
        }
        _state.value = ProbeState.Ready(device)
        return true
    }

    override fun measure(region: MeasurementRegion, session: MeasurementSession, week: Int): Flow<RegionMeasurementEvent> = flow {
        val device = connectedDevice
        if (device == null) {
            emit(RegionMeasurementEvent.Failed(ProbeError.DISCONNECTED))
            return@flow
        }
        val context = SensorReadContext(sessionSeed = session.startedAt, week = week)
        var finalValues: Map<com.skinthesia.domain.model.SensorType, Double>? = null
        sensors.read(device, region, context).collect { frame ->
            if (!frame.inContact) {
                emit(RegionMeasurementEvent.Failed(ProbeError.POOR_CONTACT))
                return@collect
            }
            emit(RegionMeasurementEvent.Progress(frame.fraction, frame.values))
            if (frame.isFinal) finalValues = frame.values
        }
        val values = finalValues ?: return@flow
        // Transfer: the simulator hands readings over after a short, realistic pause.
        delay(250)
        val timestamp = clock()
        val source = if (device.isSimulated) DataSource.SENSOR_SIMULATED else DataSource.SENSOR_MEASURED
        val readings = values.map { (sensor, value) ->
            SensorMeasurement(
                id = Ids.new("rd"),
                userId = session.userId,
                sessionId = session.id,
                region = region,
                sensor = sensor,
                value = value,
                timestamp = timestamp,
                source = source,
            )
        }
        emit(RegionMeasurementEvent.Completed(readings))
    }

    override suspend fun disconnect() {
        stopDiscovery()
        connectedDevice?.let { ble.disconnect(it.id) }
        connectedDevice = null
        _state.value = ProbeState.Idle
    }

    override fun acknowledgeError() {
        val current = _state.value
        if (current is ProbeState.Failed) {
            _state.value = connectedDevice?.let { ProbeState.Connected(it) } ?: ProbeState.Idle
        }
    }

    private companion object {
        const val CALIBRATION_STEPS = 3
    }
}
