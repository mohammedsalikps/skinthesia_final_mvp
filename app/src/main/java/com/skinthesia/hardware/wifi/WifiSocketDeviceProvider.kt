package com.skinthesia.hardware.wifi

import android.util.Log
import com.skinthesia.domain.model.ProbeDevice
import com.skinthesia.domain.model.ProbeError
import com.skinthesia.domain.model.SensorType
import com.skinthesia.hardware.ble.BleDeviceProvider
import com.skinthesia.hardware.ble.ProbeException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.io.PrintWriter
import java.net.InetSocketAddress
import java.net.Socket
import java.net.SocketTimeoutException

// TEMPORARY DEBUGGING ONLY (Step 5) - remove this constant and every Log.* call
// tagged with it once the real ESP32 TCP failure is diagnosed.
private const val ESP32_LOG_TAG = "SKINTHESIA_ESP32"

/**
 * Owns the single raw TCP connection to the real Skinthesia ESP32 probe (see
 * `SRC_CODE.ino`): a plain `WiFiServer` on the ESP32's own Wi-Fi SoftAP
 * ("Detector_Device", 192.168.4.1:5000), one line-terminated command in, one
 * line-terminated response back - not BLE, not HTTP. Shared by
 * [WifiSocketDeviceProvider] (connection lifecycle) and
 * [com.skinthesia.hardware.probe.TcpSensorDataProvider] (sending commands),
 * since both need the same live socket. All I/O runs on [Dispatchers.IO] and
 * is serialized by [lock] so a command and its response can never interleave
 * with another one on the same connection.
 */
class EspProbeSocket(
    private val host: String = HOST,
    private val port: Int = PORT,
    private val connectTimeoutMs: Int = 4000,
    private val readTimeoutMs: Int = 4000,
) {
    private val lock = Mutex()
    private var socket: Socket? = null
    private var writer: PrintWriter? = null
    private var reader: BufferedReader? = null

    /** Opens the socket. Drains the firmware's unsolicited `AUTO_PH:<value>` greeting (sent immediately on connect, before any command) so it can never be mistaken for the answer to the first real command. Returns false on any connection failure - timeout, refused, unreachable host - rather than throwing. */
    suspend fun open(): Boolean = withContext(Dispatchers.IO) {
        lock.withLock {
            closeLocked()
            Log.d(ESP32_LOG_TAG, "CONNECT attempt $host:$port") // TEMPORARY (Step 5)
            try {
                val opened = Socket()
                opened.connect(InetSocketAddress(host, port), connectTimeoutMs)
                opened.soTimeout = readTimeoutMs
                socket = opened
                writer = PrintWriter(opened.getOutputStream(), true)
                reader = BufferedReader(InputStreamReader(opened.getInputStream()))
                Log.d(ESP32_LOG_TAG, "CONNECT success $host:$port") // TEMPORARY (Step 5)
                drainAutoPushLocked()
                true
            } catch (e: IOException) {
                Log.e(ESP32_LOG_TAG, "CONNECT failed $host:$port", e) // TEMPORARY (Step 5)
                closeLocked()
                false
            }
        }
    }

    /** Sends one command line and returns the next line the firmware sends back (as-is, unparsed), or null if the socket isn't open, the write failed, the read timed out, or the connection was closed. */
    suspend fun sendCommand(command: String): String? = withContext(Dispatchers.IO) {
        lock.withLock {
            val activeWriter = writer
            val activeReader = reader
            if (activeWriter == null || activeReader == null) {
                Log.d(ESP32_LOG_TAG, "SEND skipped, socket not open: $command") // TEMPORARY (Step 5)
                return@withLock null
            }
            Log.d(ESP32_LOG_TAG, "SEND: $command") // TEMPORARY (Step 5)
            try {
                activeWriter.println(command)
                if (activeWriter.checkError()) {
                    Log.e(ESP32_LOG_TAG, "WRITE failed (checkError) for: $command") // TEMPORARY (Step 5)
                    return@withLock null
                }
                val line = activeReader.readLine()
                Log.d(ESP32_LOG_TAG, "RECV: $line") // TEMPORARY (Step 5)
                line
            } catch (e: SocketTimeoutException) {
                Log.e(ESP32_LOG_TAG, "READ timeout waiting for response to: $command", e) // TEMPORARY (Step 5)
                null
            } catch (e: IOException) {
                Log.e(ESP32_LOG_TAG, "READ/WRITE failed for: $command", e) // TEMPORARY (Step 5)
                null
            }
        }
    }

    suspend fun close() = withContext(Dispatchers.IO) {
        lock.withLock { closeLocked() }
    }

    /** Best-effort: the firmware always sends this right after accepting the connection, but a short, bounded wait keeps a slow or missing greeting from blocking the real connect. */
    private fun drainAutoPushLocked() {
        val opened = socket ?: return
        try {
            opened.soTimeout = 500
            Log.d(ESP32_LOG_TAG, "DRAIN waiting for AUTO_PH greeting") // TEMPORARY (Step 5)
            val greeting = reader?.readLine()
            Log.d(ESP32_LOG_TAG, "DRAIN received: $greeting") // TEMPORARY (Step 5)
        } catch (e: IOException) {
            Log.d(ESP32_LOG_TAG, "DRAIN timeout - no greeting received within 500ms") // TEMPORARY (Step 5)
        } finally {
            opened.soTimeout = readTimeoutMs
        }
    }

    private fun closeLocked() {
        runCatching { reader?.close() }
        runCatching { writer?.close() }
        runCatching { socket?.close() }
        reader = null
        writer = null
        socket = null
    }

    companion object {
        const val HOST = "192.168.4.1"
        const val PORT = 5000
    }
}

/**
 * Real transport for the Skinthesia ESP32 probe. It implements [BleDeviceProvider]
 * - the interface name predates the real protocol being known - because that is
 * the exact seam [com.skinthesia.hardware.probe.MockSkinProbeManager] (reused
 * unchanged as the orchestrator) already expects; nothing above this layer
 * changes. The real probe is not discovered like a BLE device: it is always at
 * the same fixed Wi-Fi address once the phone has joined its "Detector_Device"
 * network, so [scan] reports the one device as soon as that address answers.
 *
 * [connect] deliberately accepts any [deviceId] rather than validating it
 * against [DEVICE.id]: there is only ever one reachable device on this
 * transport, and [com.skinthesia.hardware.probe.MockSkinProbeManager.reconnect]
 * (unmodified) resolves an unrecognised id against its own hardcoded
 * simulator device list before calling back into this provider, so a real
 * saved pairing would otherwise be rejected on that id mismatch alone.
 */
class WifiSocketDeviceProvider(
    private val socket: EspProbeSocket = EspProbeSocket(),
) : BleDeviceProvider {

    override val requiredPermissions: List<String> = emptyList()

    override fun isTransportAvailable(): Boolean = true

    override fun scan(): Flow<List<ProbeDevice>> = flow {
        emit(emptyList())
        delay(300)
        if (socket.open()) {
            emit(listOf(DEVICE))
            socket.close()
        } else {
            emit(emptyList())
        }
    }

    override suspend fun connect(deviceId: String): ProbeDevice {
        if (!socket.open()) throw ProbeException(ProbeError.CONNECTION_FAILED)
        return DEVICE
    }

    override suspend fun disconnect(deviceId: String) {
        socket.close()
    }

    companion object {
        /**
         * The firmware (`SRC_CODE.ino`) exposes no identity, firmware-version,
         * signal-strength or battery telemetry over its TCP protocol - those
         * fields are left null (the existing UI already renders null as
         * "Unknown" / an empty gauge) rather than invented.
         *
         * `capabilities` lists only PH and HYDRATION. TEMPERATURE and
         * SKIN_BARRIER have no firmware command at all and are left off so
         * the existing capability-filtered UI never shows them for this
         * device. SEBUM is also left off: the firmware's TEST_SEBUM answer
         * is a category (DRY/NORMAL/OILY), not a number, and SensorType.SEBUM
         * is defined as a Double - see TcpSensorDataProvider's doc comment
         * for why that reading is not wired into this measurement pipeline.
         */
        val DEVICE = ProbeDevice(
            id = "ESP32_SKIN_PROBE",
            name = "Skinthesia ESP32 Probe",
            model = "Skinthesia ESP32 Probe (Wi-Fi)",
            firmwareVersion = null,
            signalStrength = null,
            batteryPercent = null,
            capabilities = setOf(SensorType.PH, SensorType.HYDRATION),
            isSimulated = false,
        )
    }
}
