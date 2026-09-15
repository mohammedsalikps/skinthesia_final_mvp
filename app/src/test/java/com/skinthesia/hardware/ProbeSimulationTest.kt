package com.skinthesia.hardware

import com.skinthesia.domain.model.DataSource
import com.skinthesia.domain.model.MeasurementRegion
import com.skinthesia.domain.model.ProbeError
import com.skinthesia.domain.model.ProbeState
import com.skinthesia.domain.model.RegionMeasurementEvent
import com.skinthesia.domain.model.SensorType
import com.skinthesia.hardware.ble.MockBleDeviceProvider
import com.skinthesia.hardware.probe.MockSkinProbeManager
import com.skinthesia.hardware.probe.SensorReadContext
import com.skinthesia.hardware.probe.SimulatedSensorDataProvider
import com.skinthesia.testing.Fixtures
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ProbeSimulationTest {

    private fun TestScope.manager(
        failConnection: Boolean = false,
        contactLoss: MeasurementRegion? = null,
    ) = MockSkinProbeManager(
        scope = this,
        ble = MockBleDeviceProvider(shouldFailConnection = { failConnection }, timeScale = 0f),
        sensors = SimulatedSensorDataProvider(frameMillis = 0, contactLossRegion = { contactLoss }),
        clock = { Fixtures.NOW },
        calibrationStepMillis = 0,
    )

    @Test
    fun `discovery finds simulated probes`() = runTest {
        val probe = manager()
        probe.startDiscovery()
        advanceUntilIdle()
        val state = probe.state.value as ProbeState.Scanning
        assertEquals(2, state.found.size)
        assertTrue(state.found.all { it.isSimulated })
    }

    @Test
    fun `connection, calibration and measurement produce labelled simulated readings`() = runTest {
        val probe = manager()
        assertTrue(probe.connect(MockBleDeviceProvider.PRIMARY))
        assertTrue(probe.state.value is ProbeState.Connected)
        assertTrue(probe.calibrate())
        assertTrue(probe.state.value is ProbeState.Ready)

        val events = probe.measure(MeasurementRegion.FOREHEAD, Fixtures.session(), week = 0).toList()
        assertTrue(events.first() is RegionMeasurementEvent.Progress)
        val completed = events.last() as RegionMeasurementEvent.Completed
        assertEquals(
            setOf(SensorType.PH, SensorType.HYDRATION, SensorType.TEMPERATURE, SensorType.SEBUM, SensorType.SKIN_BARRIER),
            completed.readings.map { it.sensor }.toSet(),
        )
        assertTrue(completed.readings.all { it.source == DataSource.SENSOR_SIMULATED && it.region == MeasurementRegion.FOREHEAD })
        assertTrue(completed.readings.all { it.sessionId == "ses-1" && it.userId == "user-test" })
    }

    @Test
    fun `a failed connection surfaces a recoverable error`() = runTest {
        val probe = manager(failConnection = true)
        assertTrue(!probe.connect(MockBleDeviceProvider.PRIMARY))
        val failed = probe.state.value as ProbeState.Failed
        assertEquals(ProbeError.CONNECTION_FAILED, failed.error)
        probe.acknowledgeError()
        assertEquals(ProbeState.Idle, probe.state.value)
    }

    @Test
    fun `losing skin contact fails only that region`() = runTest {
        val probe = manager(contactLoss = MeasurementRegion.FOREHEAD)
        probe.connect(MockBleDeviceProvider.PRIMARY)
        val first = probe.measure(MeasurementRegion.FOREHEAD, Fixtures.session(), 0).toList().last()
        assertEquals(RegionMeasurementEvent.Failed(ProbeError.POOR_CONTACT), first)
        val retry = probe.measure(MeasurementRegion.FOREHEAD, Fixtures.session(), 0).toList().last()
        assertTrue(retry is RegionMeasurementEvent.Completed)
    }

    @Test
    fun `simulated values stay in range and drift gently with the weeks`() {
        val provider = SimulatedSensorDataProvider(frameMillis = 0)
        val start = provider.targetValues(MeasurementRegion.LEFT_CHEEK, SensorReadContext(sessionSeed = 1_000_000L, week = 0))
        val later = provider.targetValues(MeasurementRegion.LEFT_CHEEK, SensorReadContext(sessionSeed = 1_000_000L, week = 8))
        assertTrue(start.getValue(SensorType.PH) in 4.5..6.5)
        assertTrue(start.getValue(SensorType.TEMPERATURE) in 30.0..35.0)
        assertTrue(later.getValue(SensorType.HYDRATION) > start.getValue(SensorType.HYDRATION))
        assertEquals(start, provider.targetValues(MeasurementRegion.LEFT_CHEEK, SensorReadContext(1_000_000L, 0)))
    }
}
