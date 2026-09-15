package com.skinthesia.hardware.probe

import com.skinthesia.domain.model.MeasurementRegion
import com.skinthesia.domain.model.ProbeDevice
import com.skinthesia.domain.model.SensorType
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.random.Random

/**
 * Development sensor simulator. Values sit in plausible ranges for the five
 * development sensors named in the brief and settle over a few seconds, like a probe
 * resting on skin. Output is deterministic for a session, and hydration drifts gently
 * upward with the programme week so check-ins show believable, labelled change.
 * These are simulated values, never measurements.
 */
class SimulatedSensorDataProvider(
    private val frames: Int = 26,
    private val frameMillis: Long = 125,
    /** Region whose first reading in a session loses contact once, to exercise recovery; null disables. */
    private val contactLossRegion: () -> MeasurementRegion? = { null },
) : SensorDataProvider {

    private val contactLostOnce = mutableSetOf<Long>()

    override fun read(device: ProbeDevice, region: MeasurementRegion, context: SensorReadContext): Flow<SensorFrame> = flow {
        val targets = targetValues(region, context).filterKeys { it in device.capabilities }
        val noise = Random(context.sessionSeed xor region.ordinal.toLong() * 7919L)
        val dropAt = if (contactLossRegion() == region && contactLostOnce.add(context.sessionSeed)) frames / 2 else -1

        for (i in 1..frames) {
            if (frameMillis > 0) delay(frameMillis)
            if (i == dropAt) {
                emit(SensorFrame(fraction = i.toFloat() / frames, values = emptyMap(), isFinal = false, inContact = false))
                return@flow
            }
            val t = i.toFloat() / frames
            val settle = 1f - (1f - t).pow(3)
            val jitter = (1f - settle) * 0.9f
            val values = targets.mapValues { (sensor, target) ->
                val start = startValue(sensor)
                val eased = start + (target - start) * settle
                val wobble = (noise.nextDouble() - 0.5) * jitterScale(sensor) * jitter
                round(sensor, eased + wobble)
            }
            emit(SensorFrame(fraction = t, values = if (i == frames) targets.mapValues { round(it.key, it.value) } else values, isFinal = i == frames))
        }
    }

    /** Deterministic final values for a region, with gentle week-based drift. */
    fun targetValues(region: MeasurementRegion, context: SensorReadContext): Map<SensorType, Double> {
        val r = Random(context.sessionSeed / 1000 xor region.ordinal.toLong())
        val weekLift = (context.week * 1.15).coerceAtMost(13.0)
        return when (region) {
            MeasurementRegion.FOREHEAD -> mapOf(
                SensorType.PH to 5.15 + r.nextDouble() * 0.25 - context.week * 0.004,
                SensorType.HYDRATION to 60.0 + r.nextDouble() * 6 + weekLift * 0.7,
                SensorType.TEMPERATURE to 32.7 + r.nextDouble() * 0.6,
                // The T-zone runs oilier than the cheeks; sebum eases slightly as a routine settles in.
                SensorType.SEBUM to 58.0 + r.nextDouble() * 10 - context.week * 0.3,
                SensorType.SKIN_BARRIER to 62.0 + r.nextDouble() * 6 + weekLift * 0.5,
            )
            MeasurementRegion.LEFT_CHEEK -> mapOf(
                SensorType.PH to 5.40 + r.nextDouble() * 0.25 - context.week * 0.006,
                SensorType.HYDRATION to 50.0 + r.nextDouble() * 7 + weekLift,
                SensorType.TEMPERATURE to 32.1 + r.nextDouble() * 0.5,
                SensorType.SEBUM to 36.0 + r.nextDouble() * 8 - context.week * 0.15,
                SensorType.SKIN_BARRIER to 58.0 + r.nextDouble() * 7 + weekLift * 0.6,
            )
            MeasurementRegion.RIGHT_CHEEK -> mapOf(
                SensorType.PH to 5.38 + r.nextDouble() * 0.25 - context.week * 0.006,
                SensorType.HYDRATION to 52.0 + r.nextDouble() * 7 + weekLift,
                SensorType.TEMPERATURE to 32.0 + r.nextDouble() * 0.5,
                SensorType.SEBUM to 38.0 + r.nextDouble() * 8 - context.week * 0.15,
                SensorType.SKIN_BARRIER to 58.0 + r.nextDouble() * 7 + weekLift * 0.6,
            )
        }.mapValues { (sensor, value) -> round(sensor, value.coerceIn(range(sensor))) }
    }

    private fun startValue(sensor: SensorType): Double = when (sensor) {
        SensorType.PH -> 6.4
        SensorType.HYDRATION -> 18.0
        SensorType.TEMPERATURE -> 29.4
        SensorType.SEBUM -> 20.0
        SensorType.SKIN_BARRIER -> 25.0
    }

    private fun jitterScale(sensor: SensorType): Double = when (sensor) {
        SensorType.PH -> 0.5
        SensorType.HYDRATION -> 9.0
        SensorType.TEMPERATURE -> 0.8
        SensorType.SEBUM -> 7.0
        SensorType.SKIN_BARRIER -> 6.0
    }

    private fun range(sensor: SensorType): ClosedFloatingPointRange<Double> = when (sensor) {
        SensorType.PH -> 4.5..6.5
        SensorType.HYDRATION -> 20.0..90.0
        SensorType.TEMPERATURE -> 30.0..35.0
        SensorType.SEBUM -> 0.0..100.0
        SensorType.SKIN_BARRIER -> 0.0..100.0
    }

    private fun round(sensor: SensorType, value: Double): Double {
        val factor = 10.0.pow(sensor.decimals)
        return (value * factor).roundToInt() / factor
    }
}
