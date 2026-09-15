package com.skinthesia.domain.model

import kotlinx.serialization.Serializable

/**
 * Sensors a probe can report. The real hardware specification has not been supplied,
 * so these are the development sensors named in the product brief. A device declares
 * which ones it supports through [ProbeDevice.capabilities]; the UI renders only those.
 */
@Serializable
enum class SensorType(val label: String, val unit: String, val decimals: Int) {
    PH("pH", "", 1),
    HYDRATION("Hydration", "index", 0),
    TEMPERATURE("Skin temperature", "°C", 1),
    SEBUM("Sebum", "index", 0),
    SKIN_BARRIER("Skin barrier", "index", 0),
}

/** Where on the face a probe reading was taken. */
@Serializable
enum class MeasurementRegion(val label: String, val instruction: String) {
    FOREHEAD("Forehead", "Place the probe gently on your forehead."),
    LEFT_CHEEK("Left cheek", "Now rest the probe on your left cheek."),
    RIGHT_CHEEK("Right cheek", "Finally, rest the probe on your right cheek."),
}

@Serializable
data class ProbeDevice(
    val id: String,
    val name: String,
    val model: String,
    val firmwareVersion: String?,
    /** Received signal strength in dBm, when the transport reports it. */
    val signalStrength: Int?,
    val batteryPercent: Int?,
    val capabilities: Set<SensorType>,
    /** True for the development simulator. The UI labels simulated devices and readings. */
    val isSimulated: Boolean,
)

/** One reading, always tied to a user, session, region, sensor and time. */
@Serializable
data class SensorMeasurement(
    val id: String,
    val userId: String,
    val sessionId: String,
    val region: MeasurementRegion,
    val sensor: SensorType,
    val value: Double,
    val timestamp: Long,
    val source: DataSource,
)

@Serializable
data class MeasurementSession(
    val id: String,
    /** Human-friendly running number, shown as "Session 004". */
    val number: Int,
    val userId: String,
    val assessmentId: String?,
    val deviceId: String,
    val deviceName: String,
    val isSimulated: Boolean,
    val startedAt: Long,
    val completedAt: Long?,
    val readings: List<SensorMeasurement> = emptyList(),
) {
    val completedRegions: Set<MeasurementRegion>
        get() = readings.map { it.region }.toSet()

    val isComplete: Boolean get() = completedAt != null

    fun readingsFor(region: MeasurementRegion): List<SensorMeasurement> = readings.filter { it.region == region }

    fun value(region: MeasurementRegion, sensor: SensorType): Double? =
        readings.lastOrNull { it.region == region && it.sensor == sensor }?.value

    fun average(sensor: SensorType): Double? =
        readings.filter { it.sensor == sensor }.map { it.value }.takeIf { it.isNotEmpty() }?.average()

    val label: String get() = "Session " + number.toString().padStart(3, '0')
}

enum class ProbeError(val title: String, val message: String) {
    BLUETOOTH_UNAVAILABLE("Bluetooth is off", "Turn on Bluetooth so your phone can find the probe."),
    PERMISSION_DENIED("Nearby devices access needed", "Allow access to nearby devices to connect your probe."),
    NOT_FOUND("We couldn't find your probe", "Make sure it's charged, switched on and close to your phone."),
    CONNECTION_FAILED("Connection didn't complete", "Keep the probe near your phone and try again."),
    CALIBRATION_FAILED("Calibration was interrupted", "Hold the probe still in the air for a moment, then retry."),
    POOR_CONTACT("Keep the probe flat", "The sensor lost contact with your skin. Rest it gently and hold still."),
    DISCONNECTED("Probe disconnected", "The connection dropped. Reconnect to continue your session."),
}

/** Everything the probe layer can be doing, observed by the UI. */
sealed interface ProbeState {
    data object Idle : ProbeState
    data class Scanning(val found: List<ProbeDevice>) : ProbeState
    data class Connecting(val device: ProbeDevice) : ProbeState
    data class Connected(val device: ProbeDevice) : ProbeState
    data class Calibrating(val device: ProbeDevice, val progress: Float) : ProbeState
    data class Ready(val device: ProbeDevice) : ProbeState
    data class Failed(val error: ProbeError, val device: ProbeDevice?) : ProbeState
}

/** Progress of a single region measurement. */
sealed interface RegionMeasurementEvent {
    data class Progress(val fraction: Float, val live: Map<SensorType, Double>) : RegionMeasurementEvent
    data class Completed(val readings: List<SensorMeasurement>) : RegionMeasurementEvent
    data class Failed(val error: ProbeError) : RegionMeasurementEvent
}

data class CalibrationStep(val label: String, val done: Boolean)
