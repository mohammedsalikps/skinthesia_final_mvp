package com.skinthesia.analysis

import com.skinthesia.domain.model.ContributingFactor
import com.skinthesia.domain.model.LifestyleFactor
import com.skinthesia.domain.model.ProgressMetric
import com.skinthesia.domain.model.RegionFinding
import com.skinthesia.domain.model.Severity
import com.skinthesia.domain.model.SkinConcern
import com.skinthesia.domain.model.SkinHealthStatus
import com.skinthesia.domain.model.SkinMeasurement
import com.skinthesia.domain.model.SkinMetric
import com.skinthesia.domain.model.SkinPrint
import com.skinthesia.domain.model.SkinRegion
import com.skinthesia.domain.model.SkinType
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.util.UUID
import kotlin.math.roundToInt
import kotlin.random.Random

/**
 * Local simulation of a DermoScan device and analysis model.
 *
 * Results are deterministic for a given profile and photo so the UI is stable
 * across recompositions, and are nudged by the user's self-reported concerns
 * and skin type so the report feels personal rather than random.
 */
class MockSkinAnalysisEngine(
    private val measurementSteps: Int = DEFAULT_STEPS,
    private val stepDelayMillis: Long = DEFAULT_STEP_DELAY_MS,
    private val clock: () -> Long = System::currentTimeMillis,
) : SkinAnalysisEngine {

    override val isSimulated: Boolean = true

    override fun measure(): Flow<MeasurementProgress> = flow {
        val target = baselineMeasurement(seed = clock())
        emit(MeasurementProgress(fraction = 0f, partial = null))
        for (step in 1..measurementSteps) {
            delay(stepDelayMillis)
            val fraction = step.toFloat() / measurementSteps
            val eased = 1f - (1f - fraction) * (1f - fraction)
            emit(MeasurementProgress(fraction = fraction, partial = target.scaledBy(eased)))
        }
    }

    override suspend fun analyze(request: SkinAnalysisRequest): SkinAnalysisResult {
        val seed = (request.photo?.filePath ?: "no-photo").hashCode().toLong() xor
            request.profile.hashCode().toLong()
        val random = Random(seed)
        val profile = request.profile
        val concerns = profile.concerns

        fun score(metric: SkinMetric): Int {
            val base = 62 + random.nextInt(0, 22)
            val penalty = when (metric) {
                SkinMetric.HYDRATION -> if (profile.skinType == SkinType.DRY || SkinConcern.DRYNESS in concerns) 12 else 0
                SkinMetric.SEBUM -> if (profile.skinType == SkinType.OILY) 10 else 0
                SkinMetric.PIGMENTATION -> if (SkinConcern.PIGMENTATION in concerns) 14 else 0
                SkinMetric.TEXTURE -> if (SkinConcern.TEXTURE in concerns) 10 else 0
                SkinMetric.PORES -> if (SkinConcern.ENLARGED_PORES in concerns || SkinConcern.ACNE in concerns) 9 else 0
                SkinMetric.REDNESS -> if (SkinConcern.REDNESS in concerns || profile.skinType == SkinType.SENSITIVE) 11 else 0
                SkinMetric.FINE_LINES -> if (SkinConcern.FINE_LINES in concerns) 12 else 0
                SkinMetric.BARRIER -> if (profile.skinType == SkinType.SENSITIVE) 8 else 0
                SkinMetric.ELASTICITY, SkinMetric.TEMPERATURE -> 0
            }
            return (base - penalty).coerceIn(35, 96)
        }

        val reported = listOf(
            SkinMetric.HYDRATION, SkinMetric.SEBUM, SkinMetric.PIGMENTATION, SkinMetric.TEXTURE,
            SkinMetric.PORES, SkinMetric.REDNESS, SkinMetric.FINE_LINES, SkinMetric.BARRIER,
        ).map { ProgressMetric(metric = it, value = score(it)) }

        val overall = reported.map { it.value }.average().roundToInt().coerceIn(0, 100)
        val status = SkinHealthStatus.fromScore(overall)
        val measurement = baselineMeasurement(seed).let { base ->
            base.copy(
                hydration = reported.first { it.metric == SkinMetric.HYDRATION }.value.toFloat(),
                sebum = (100 - reported.first { it.metric == SkinMetric.SEBUM }.value).toFloat().coerceIn(20f, 80f),
                texture = reported.first { it.metric == SkinMetric.TEXTURE }.value.toFloat(),
                measuredAt = clock(),
            )
        }

        val skinPrint = SkinPrint(
            id = UUID.randomUUID().toString(),
            score = overall,
            status = status,
            summary = summaryFor(status),
            metrics = reported,
            regionFindings = regionFindings(concerns, random),
            contributingFactors = contributingFactors(profile.lifestyleFactors, random),
            generatedAt = clock(),
        )
        return SkinAnalysisResult(skinPrint = skinPrint, measurement = measurement)
    }

    private fun baselineMeasurement(seed: Long): SkinMeasurement {
        val random = Random(seed)
        return SkinMeasurement(
            hydration = 52f + random.nextInt(0, 30),
            sebum = 30f + random.nextInt(0, 35),
            temperature = 32.4f + random.nextInt(0, 14) / 10f,
            texture = 55f + random.nextInt(0, 30),
            elasticity = 58f + random.nextInt(0, 30),
            measuredAt = seed,
        )
    }

    private fun SkinMeasurement.scaledBy(fraction: Float) = copy(
        hydration = hydration * fraction,
        sebum = sebum * fraction,
        temperature = 30f + (temperature - 30f) * fraction,
        texture = texture * fraction,
        elasticity = elasticity * fraction,
    )

    private fun summaryFor(status: SkinHealthStatus): String = when (status) {
        SkinHealthStatus.EXCELLENT -> "Your skin is in excellent condition with strong barrier and hydration."
        SkinHealthStatus.GOOD -> "Your skin is generally healthy with visible areas for improvement."
        SkinHealthStatus.FAIR -> "Your skin shows a few areas that will benefit from consistent care."
        SkinHealthStatus.NEEDS_ATTENTION -> "Your skin needs focused attention; a gentle, steady routine will help."
    }

    private fun regionFindings(concerns: Set<SkinConcern>, random: Random): List<RegionFinding> {
        val candidates = buildList {
            if (SkinConcern.PIGMENTATION in concerns) add(SkinRegion.LEFT_CHEEK to SkinConcern.PIGMENTATION)
            if (SkinConcern.ACNE in concerns) add(SkinRegion.JAWLINE to SkinConcern.ACNE)
            if (SkinConcern.DRYNESS in concerns) add(SkinRegion.AROUND_MOUTH to SkinConcern.DRYNESS)
            if (SkinConcern.FINE_LINES in concerns) add(SkinRegion.UNDER_EYES to SkinConcern.FINE_LINES)
            if (SkinConcern.ENLARGED_PORES in concerns) add(SkinRegion.NOSE to SkinConcern.ENLARGED_PORES)
            if (SkinConcern.REDNESS in concerns) add(SkinRegion.RIGHT_CHEEK to SkinConcern.REDNESS)
            if (SkinConcern.TEXTURE in concerns) add(SkinRegion.FOREHEAD to SkinConcern.TEXTURE)
            if (isEmpty()) {
                add(SkinRegion.FOREHEAD to SkinConcern.TEXTURE)
                add(SkinRegion.UNDER_EYES to SkinConcern.DRYNESS)
            }
        }
        return candidates.map { (region, concern) ->
            RegionFinding(
                region = region,
                concern = concern,
                severity = Severity.entries[random.nextInt(Severity.entries.size)],
                note = "Detected ${concern.name.lowercase().replace('_', ' ')} around the ${region.name.lowercase().replace('_', ' ')}.",
            )
        }
    }

    private fun contributingFactors(factors: Set<LifestyleFactor>, random: Random): List<ContributingFactor> {
        val chosen = if (factors.isEmpty()) setOf(LifestyleFactor.SUN_EXPOSURE, LifestyleFactor.STRESS) else factors
        return chosen.map { factor ->
            ContributingFactor(
                factor = factor,
                relevance = Severity.entries[random.nextInt(Severity.entries.size)],
                description = when (factor) {
                    LifestyleFactor.SUN_EXPOSURE -> "UV exposure can drive pigmentation and weaken the barrier over time."
                    LifestyleFactor.STRESS -> "Stress hormones can increase oil production and slow recovery."
                    LifestyleFactor.SLEEP -> "Short or irregular sleep reduces overnight repair."
                    LifestyleFactor.DIET -> "Diet influences inflammation and hydration levels."
                    LifestyleFactor.WORK_ENVIRONMENT -> "Indoor air and screens can dehydrate the skin surface."
                    LifestyleFactor.HORMONAL_CHANGES -> "Hormonal shifts often show up as breakouts along the jawline."
                    LifestyleFactor.SKINCARE_PRODUCTS -> "Layering strong actives can irritate and thin the barrier."
                    LifestyleFactor.CLIMATE -> "Dry or cold climates pull moisture from the skin."
                    LifestyleFactor.ILLNESS_MEDICATION -> "Some medications change oil balance and sensitivity."
                },
            )
        }
    }

    private companion object {
        const val DEFAULT_STEPS = 40
        const val DEFAULT_STEP_DELAY_MS = 90L
    }
}
