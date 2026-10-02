package com.skinthesia.hardware.probe

import com.skinthesia.domain.model.MeasurementRegion
import com.skinthesia.domain.model.MeasurementSession
import com.skinthesia.domain.model.ProbeDevice
import com.skinthesia.domain.model.ProbeState
import com.skinthesia.domain.model.RegionMeasurementEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn

/**
 * Forwards every [SkinProbeManager] call to whichever manager is currently
 * active - [initial] until [useSimulated] is called, [simulated] for the rest
 * of the app process after that. Exists only so a screen that started against
 * the real ESP32 transport can fall back to the app's existing, unmodified
 * simulated-probe experience (typically the same [MockSkinProbeManager] the
 * app already builds for mock mode) when the real probe can't be found -
 * without every other screen needing to know which one is live, since every
 * screen already shares this same [SkinProbeManager] instance from
 * `AppContainer`.
 *
 * Switching is one-way for the life of the process: "Simulate" is meant as a
 * deliberate, explicit user choice, not a per-attempt fallback, so there is
 * no "switch back to real" path.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class SwitchableSkinProbeManager(
    scope: CoroutineScope,
    private val initial: SkinProbeManager,
    private val simulated: SkinProbeManager,
) : SkinProbeManager {

    private val active = MutableStateFlow(initial)

    override val state: StateFlow<ProbeState> = active
        .flatMapLatest { it.state }
        .stateIn(scope, SharingStarted.Eagerly, initial.state.value)

    override val requiredPermissions: List<String> get() = active.value.requiredPermissions
    override val connectedDevice: ProbeDevice? get() = active.value.connectedDevice

    /** Switches this and every other screen sharing this manager over to [simulated] from now on. */
    fun useSimulated() {
        if (active.value === simulated) return
        active.value.stopDiscovery()
        active.value = simulated
    }

    override fun startDiscovery() = active.value.startDiscovery()
    override fun stopDiscovery() = active.value.stopDiscovery()
    override suspend fun connect(device: ProbeDevice): Boolean = active.value.connect(device)
    override suspend fun reconnect(deviceId: String): Boolean = active.value.reconnect(deviceId)
    override suspend fun calibrate(): Boolean = active.value.calibrate()

    override fun measure(region: MeasurementRegion, session: MeasurementSession, week: Int): Flow<RegionMeasurementEvent> =
        active.value.measure(region, session, week)

    override suspend fun disconnect() = active.value.disconnect()
    override fun acknowledgeError() = active.value.acknowledgeError()
}
