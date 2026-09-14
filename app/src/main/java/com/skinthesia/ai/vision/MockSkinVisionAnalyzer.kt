package com.skinthesia.ai.vision

import com.skinthesia.domain.model.CameraAnalysisResult
import com.skinthesia.domain.model.Ids
import com.skinthesia.domain.model.IndicatorEstimate
import com.skinthesia.domain.model.SkinConcern
import com.skinthesia.domain.model.SkinGoal
import com.skinthesia.domain.model.SkinPhoto
import com.skinthesia.domain.model.SkinRegion
import com.skinthesia.domain.model.VisualIndicator
import kotlinx.coroutines.delay
import kotlin.math.roundToInt
import kotlin.random.Random

/**
 * Development vision model. It does not look at skin: it produces plausible,
 * deterministic visual estimates from the user's seed and self-reported concerns,
 * with a gentle improvement over the programme weeks for indicators the user is
 * working on. Every result is marked simulated.
 */
class MockSkinVisionAnalyzer(
    private val clock: () -> Long = System::currentTimeMillis,
    private val processingMillis: Long = 1400,
) : SkinVisionAnalyzer {

    override val modelName: String = "Development vision model"

    override suspend fun analyze(photo: SkinPhoto, context: VisionContext): CameraAnalysisResult {
        if (processingMillis > 0) delay(processingMillis)
        val estimates = VisualIndicator.entries.map { indicator -> estimate(indicator, photo, context) }
        return CameraAnalysisResult(
            id = Ids.new("cam"),
            photoId = photo.id,
            estimates = estimates,
            analyzedAt = clock(),
            modelVersion = MODEL_VERSION,
            isSimulated = true,
        )
    }

    fun estimate(indicator: VisualIndicator, photo: SkinPhoto, context: VisionContext): IndicatorEstimate {
        val base = Random(context.userSeed * 31 + indicator.ordinal)
        val jitter = Random(photo.id.hashCode().toLong() * 17 + indicator.ordinal)
        val (floor, span) = BASELINES.getValue(indicator)
        var level = floor + base.nextInt(span + 1) + concernBoost(indicator, context.concerns) + jitter.nextInt(-3, 4)
        val focused = indicator.goals.any { it in context.goals }
        val broadCare = SkinGoal.OVERALL_HEALTH in context.goals
        val weeklyRate = when {
            focused -> 0.030
            broadCare -> 0.018
            else -> 0.012
        }
        val cap = if (focused) 0.34 else 0.18
        level = (level * (1 - minOf(cap, weeklyRate * context.week))).roundToInt()
        return IndicatorEstimate(
            indicator = indicator,
            level = level.coerceIn(4, 92),
            confidence = 0.62f + base.nextFloat() * 0.22f,
            regions = REGIONS.getValue(indicator),
        )
    }

    private fun concernBoost(indicator: VisualIndicator, concerns: Set<SkinConcern>): Int =
        concerns.sumOf { concern -> BOOSTS[concern]?.get(indicator) ?: 0 }

    companion object {
        const val MODEL_VERSION = "mock-vision-0.3 (development)"

        /** Floor and random span for each indicator's visibility level. */
        private val BASELINES = mapOf(
            VisualIndicator.ACNE_APPEARANCE to (22 to 16),
            VisualIndicator.PIGMENTATION to (30 to 18),
            VisualIndicator.TEXTURE to (28 to 16),
            VisualIndicator.PORES to (34 to 14),
            VisualIndicator.REDNESS to (20 to 14),
            VisualIndicator.DARK_CIRCLES to (34 to 14),
            VisualIndicator.FINE_LINES to (16 to 14),
        )

        private val BOOSTS: Map<SkinConcern, Map<VisualIndicator, Int>> = mapOf(
            SkinConcern.BREAKOUTS to mapOf(VisualIndicator.ACNE_APPEARANCE to 12),
            SkinConcern.OILINESS to mapOf(VisualIndicator.ACNE_APPEARANCE to 4, VisualIndicator.PORES to 6),
            SkinConcern.DARK_SPOTS to mapOf(VisualIndicator.PIGMENTATION to 12),
            SkinConcern.UNEVEN_TONE to mapOf(VisualIndicator.PIGMENTATION to 7),
            SkinConcern.DULLNESS to mapOf(VisualIndicator.TEXTURE to 4, VisualIndicator.PIGMENTATION to 3),
            SkinConcern.TEXTURE to mapOf(VisualIndicator.TEXTURE to 10),
            SkinConcern.VISIBLE_PORES to mapOf(VisualIndicator.PORES to 10),
            SkinConcern.REDNESS to mapOf(VisualIndicator.REDNESS to 12),
            SkinConcern.SENSITIVITY to mapOf(VisualIndicator.REDNESS to 6),
            SkinConcern.DARK_CIRCLES to mapOf(VisualIndicator.DARK_CIRCLES to 10),
            SkinConcern.FINE_LINES to mapOf(VisualIndicator.FINE_LINES to 12),
            SkinConcern.DRYNESS to mapOf(VisualIndicator.TEXTURE to 5, VisualIndicator.FINE_LINES to 3),
        )

        private val REGIONS: Map<VisualIndicator, List<SkinRegion>> = mapOf(
            VisualIndicator.ACNE_APPEARANCE to listOf(SkinRegion.JAWLINE, SkinRegion.CHIN, SkinRegion.FOREHEAD),
            VisualIndicator.PIGMENTATION to listOf(SkinRegion.LEFT_CHEEK, SkinRegion.RIGHT_CHEEK, SkinRegion.FOREHEAD),
            VisualIndicator.TEXTURE to listOf(SkinRegion.FOREHEAD, SkinRegion.LEFT_CHEEK),
            VisualIndicator.PORES to listOf(SkinRegion.NOSE, SkinRegion.LEFT_CHEEK, SkinRegion.RIGHT_CHEEK),
            VisualIndicator.REDNESS to listOf(SkinRegion.NOSE, SkinRegion.LEFT_CHEEK, SkinRegion.RIGHT_CHEEK),
            VisualIndicator.DARK_CIRCLES to listOf(SkinRegion.UNDER_EYES),
            VisualIndicator.FINE_LINES to listOf(SkinRegion.UNDER_EYES, SkinRegion.FOREHEAD),
        )
    }
}

/** Goals a visual indicator speaks to. */
val VisualIndicator.goals: Set<SkinGoal>
    get() = when (this) {
        VisualIndicator.ACNE_APPEARANCE -> setOf(SkinGoal.ACNE)
        VisualIndicator.PIGMENTATION -> setOf(SkinGoal.DARK_SPOTS, SkinGoal.UNEVEN_TONE)
        VisualIndicator.TEXTURE -> setOf(SkinGoal.TEXTURE)
        VisualIndicator.PORES -> setOf(SkinGoal.PORES)
        VisualIndicator.REDNESS -> setOf(SkinGoal.REDNESS)
        VisualIndicator.DARK_CIRCLES -> setOf(SkinGoal.DARK_CIRCLES)
        VisualIndicator.FINE_LINES -> setOf(SkinGoal.FINE_LINES)
    }
