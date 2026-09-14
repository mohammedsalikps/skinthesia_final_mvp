package com.skinthesia.ai.skinprint

import com.skinthesia.domain.model.CameraAnalysisResult
import com.skinthesia.domain.model.DataSource
import com.skinthesia.domain.model.Ids
import com.skinthesia.domain.model.MeasurementSession
import com.skinthesia.domain.model.ScoreBand
import com.skinthesia.domain.model.SensorType
import com.skinthesia.domain.model.SkinConcern
import com.skinthesia.domain.model.SkinGoal
import com.skinthesia.domain.model.SkinPrint
import com.skinthesia.domain.model.SkinPrintDimension
import com.skinthesia.domain.model.SkinPrintInputs
import com.skinthesia.domain.model.SkinProfile
import com.skinthesia.domain.model.SkinScore
import com.skinthesia.domain.model.SkinType
import com.skinthesia.domain.model.VisualIndicator
import kotlin.math.roundToInt

data class SkinPrintInput(
    val assessmentId: String,
    val week: Int,
    val camera: CameraAnalysisResult?,
    val session: MeasurementSession?,
    val skin: SkinProfile,
    val now: Long,
)

/**
 * Transparent, deterministic SkinPrint rules. Each dimension says exactly which
 * inputs it used; visual estimates, sensor readings and self-reported answers stay
 * distinguishable. The composite is a tracking indicator, not a medical measurement.
 *
 * Visual dimensions: score = 100 − 0.8 × weighted visibility level.
 * Hydration: the average probe hydration index when available, otherwise a low-confidence
 * estimate from skin type. Overall: fixed weighted mean of the five dimensions.
 */
class SkinPrintCalculator {

    fun calculate(input: SkinPrintInput): SkinPrint {
        val scores = listOf(
            visualScore(input, SkinPrintDimension.CLARITY, VisualIndicator.ACNE_APPEARANCE to 0.6, VisualIndicator.REDNESS to 0.4),
            visualScore(input, SkinPrintDimension.EVEN_TONE, VisualIndicator.PIGMENTATION to 0.65, VisualIndicator.DARK_CIRCLES to 0.35),
            visualScore(input, SkinPrintDimension.TEXTURE, VisualIndicator.TEXTURE to 0.7, VisualIndicator.FINE_LINES to 0.3),
            hydrationScore(input),
            visualScore(input, SkinPrintDimension.PORE_APPEARANCE, VisualIndicator.PORES to 1.0),
        )
        val overall = weightedOverall(scores.associate { it.dimension to it.value })
        val session = input.session
        return SkinPrint(
            id = Ids.new("sp"),
            assessmentId = input.assessmentId,
            overall = overall,
            scores = scores,
            band = ScoreBand.fromScore(overall),
            week = input.week,
            createdAt = input.now,
            inputs = SkinPrintInputs(
                hasCamera = input.camera != null,
                cameraSimulated = input.camera?.isSimulated ?: false,
                hasSensors = session?.average(SensorType.HYDRATION) != null,
                sensorsSimulated = session?.isSimulated ?: false,
                hasSelfReport = input.skin.skinType != null || input.skin.concerns.isNotEmpty(),
            ),
            algorithmVersion = ALGORITHM_VERSION,
        )
    }

    private fun visualScore(
        input: SkinPrintInput,
        dimension: SkinPrintDimension,
        vararg parts: Pair<VisualIndicator, Double>,
    ): SkinScore {
        val camera = input.camera
        val levels = camera?.let { c -> parts.map { (indicator, weight) -> (c.level(indicator) ?: return@let null) to weight } }
        if (levels != null) {
            val weighted = levels.sumOf { it.first * it.second } / levels.sumOf { it.second }
            val confidence = parts.mapNotNull { (indicator, _) ->
                camera.estimates.firstOrNull { it.indicator == indicator }?.confidence
            }.average().toFloat()
            return SkinScore(
                dimension = dimension,
                value = (100 - weighted * VISUAL_SCALE).roundToInt().coerceIn(0, 100),
                sources = setOf(DataSource.VISUAL_ESTIMATE),
                confidence = confidence,
                explanation = VISUAL_EXPLANATIONS.getValue(dimension),
            )
        }
        // No photo analysis: fall back to what the user told us, with low confidence.
        val penalty = input.skin.concerns.sumOf { SELF_REPORT_PENALTIES[it]?.get(dimension) ?: 0 }
        return SkinScore(
            dimension = dimension,
            value = (SELF_REPORT_BASE - penalty).coerceIn(30, 90),
            sources = setOf(DataSource.USER_REPORTED),
            confidence = 0.3f,
            explanation = "Estimated from your questionnaire answers. A photo gives a clearer picture.",
        )
    }

    private fun hydrationScore(input: SkinPrintInput): SkinScore {
        val session = input.session
        val average = session?.average(SensorType.HYDRATION)
        if (session != null && average != null) {
            val source = if (session.isSimulated) DataSource.SENSOR_SIMULATED else DataSource.SENSOR_MEASURED
            val areas = session.readings.filter { it.sensor == SensorType.HYDRATION }.map { it.region }.distinct().size
            val suffix = if (session.isSimulated) " (simulated readings)." else "."
            return SkinScore(
                dimension = SkinPrintDimension.HYDRATION,
                value = average.roundToInt().coerceIn(0, 100),
                sources = setOf(source),
                confidence = if (areas >= 3) 0.85f else 0.6f,
                explanation = "Average hydration index across $areas probe area" + (if (areas == 1) "" else "s") + suffix,
            )
        }
        val base = when (input.skin.skinType) {
            SkinType.DRY -> 48
            SkinType.SENSITIVE -> 54
            SkinType.COMBINATION -> 58
            SkinType.OILY -> 62
            SkinType.NORMAL -> 64
            null -> 58
        }
        val dryness = if (SkinConcern.DRYNESS in input.skin.concerns) 6 else 0
        return SkinScore(
            dimension = SkinPrintDimension.HYDRATION,
            value = base - dryness,
            sources = setOf(DataSource.USER_REPORTED),
            confidence = 0.35f,
            explanation = "Estimated from your skin type. Measure with the probe for a reading.",
        )
    }

    companion object {
        const val ALGORITHM_VERSION = "skinprint-1.0"
        private const val VISUAL_SCALE = 0.8
        private const val SELF_REPORT_BASE = 70

        /** Fixed weights for the overall indicator; they sum to 1. */
        val WEIGHTS: Map<SkinPrintDimension, Double> = mapOf(
            SkinPrintDimension.CLARITY to 0.22,
            SkinPrintDimension.EVEN_TONE to 0.20,
            SkinPrintDimension.TEXTURE to 0.20,
            SkinPrintDimension.HYDRATION to 0.22,
            SkinPrintDimension.PORE_APPEARANCE to 0.16,
        )

        fun weightedOverall(values: Map<SkinPrintDimension, Int>): Int {
            val total = WEIGHTS.entries.sumOf { (dimension, weight) -> (values[dimension] ?: 0) * weight }
            return total.roundToInt().coerceIn(0, 100)
        }

        private val VISUAL_EXPLANATIONS = mapOf(
            SkinPrintDimension.CLARITY to "Visual estimate from how visible blemishes and redness look in your photo.",
            SkinPrintDimension.EVEN_TONE to "Visual estimate from pigmentation and under-eye shadows.",
            SkinPrintDimension.TEXTURE to "Visual estimate from surface texture and fine lines.",
            SkinPrintDimension.PORE_APPEARANCE to "Visual estimate of how visible pores look.",
        )

        private val SELF_REPORT_PENALTIES: Map<SkinConcern, Map<SkinPrintDimension, Int>> = mapOf(
            SkinConcern.BREAKOUTS to mapOf(SkinPrintDimension.CLARITY to 12),
            SkinConcern.REDNESS to mapOf(SkinPrintDimension.CLARITY to 8),
            SkinConcern.DARK_SPOTS to mapOf(SkinPrintDimension.EVEN_TONE to 12),
            SkinConcern.UNEVEN_TONE to mapOf(SkinPrintDimension.EVEN_TONE to 8),
            SkinConcern.DARK_CIRCLES to mapOf(SkinPrintDimension.EVEN_TONE to 5),
            SkinConcern.TEXTURE to mapOf(SkinPrintDimension.TEXTURE to 10),
            SkinConcern.FINE_LINES to mapOf(SkinPrintDimension.TEXTURE to 8),
            SkinConcern.VISIBLE_PORES to mapOf(SkinPrintDimension.PORE_APPEARANCE to 12),
            SkinConcern.OILINESS to mapOf(SkinPrintDimension.PORE_APPEARANCE to 6),
        )
    }
}

/** The SkinPrint dimension a goal mainly moves; null for broad goals. */
val SkinGoal.dimension: SkinPrintDimension?
    get() = when (this) {
        SkinGoal.ACNE, SkinGoal.REDNESS -> SkinPrintDimension.CLARITY
        SkinGoal.DARK_SPOTS, SkinGoal.UNEVEN_TONE, SkinGoal.DARK_CIRCLES -> SkinPrintDimension.EVEN_TONE
        SkinGoal.TEXTURE, SkinGoal.FINE_LINES -> SkinPrintDimension.TEXTURE
        SkinGoal.PORES -> SkinPrintDimension.PORE_APPEARANCE
        SkinGoal.HYDRATION -> SkinPrintDimension.HYDRATION
        SkinGoal.OVERALL_HEALTH -> null
    }
