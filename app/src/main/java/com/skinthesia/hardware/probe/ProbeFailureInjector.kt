package com.skinthesia.hardware.probe

import com.skinthesia.domain.model.MeasurementRegion
import com.skinthesia.domain.repository.SettingsRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

/**
 * Demonstration aid behind Profile › Demo controls. When enabled, the simulated probe
 * fails its next connection attempt once and loses skin contact once during the
 * forehead reading, so the recovery paths can be seen. Each toggle re-arms it.
 */
class ProbeFailureInjector(scope: CoroutineScope, settings: SettingsRepository) {

    @Volatile private var enabled = false
    @Volatile private var connectionFailed = false
    @Volatile private var contactFailed = false

    init {
        scope.launch {
            settings.settings.map { it.simulateProbeFailure }.distinctUntilChanged().collect { on ->
                enabled = on
                connectionFailed = false
                contactFailed = false
            }
        }
    }

    fun shouldFailConnection(): Boolean {
        if (!enabled || connectionFailed) return false
        connectionFailed = true
        return true
    }

    fun contactLossRegion(): MeasurementRegion? {
        if (!enabled || contactFailed) return null
        contactFailed = true
        return MeasurementRegion.FOREHEAD
    }
}
