package com.skinthesia.ai.skinprint

import com.skinthesia.domain.model.GoalPlan
import com.skinthesia.domain.model.PotentialDimension
import com.skinthesia.domain.model.PotentialState
import com.skinthesia.domain.model.SkinGoal
import com.skinthesia.domain.model.SkinPrint
import kotlin.math.roundToInt

/**
 * Visualised potential (screen 20): a goal range per dimension, never a promised
 * appearance. Focus dimensions close half of their remaining gap to a realistic
 * ceiling over the horizon; others close about a fifth. The low end of each range
 * assumes partial consistency.
 */
class PotentialEstimator {

    fun estimate(skinPrint: SkinPrint, goals: GoalPlan, horizonWeeks: Int = goals.durationWeeks): PotentialState {
        val focus = goals.ranked.take(3).mapNotNull { it.dimension }.toSet()
        val broad = SkinGoal.OVERALL_HEALTH in goals.goals
        val dimensions = skinPrint.scores.map { score ->
            val isFocus = score.dimension in focus
            val share = when {
                isFocus -> FOCUS_SHARE
                broad -> BROAD_SHARE
                else -> OTHER_SHARE
            }
            val gap = (CEILING - score.value).coerceAtLeast(0)
            val high = score.value + (gap * share).roundToInt()
            val low = score.value + ((high - score.value) * LOW_FRACTION).roundToInt()
            PotentialDimension(
                dimension = score.dimension,
                current = score.value,
                potentialLow = low,
                potentialHigh = high,
                isFocus = isFocus,
            )
        }
        return PotentialState(
            currentOverall = skinPrint.overall,
            potentialLow = SkinPrintCalculator.weightedOverall(dimensions.associate { it.dimension to it.potentialLow }),
            potentialHigh = SkinPrintCalculator.weightedOverall(dimensions.associate { it.dimension to it.potentialHigh }),
            horizonWeeks = horizonWeeks,
            dimensions = dimensions,
            assumptions = listOf(
                "Assumes you follow your plan on most days.",
                "Based on how skin commonly responds over 12 weeks in our development model.",
                "Everyone's skin is different, so this is a direction, not a promise.",
            ),
        )
    }

    private companion object {
        const val CEILING = 92
        const val FOCUS_SHARE = 0.5
        const val BROAD_SHARE = 0.3
        const val OTHER_SHARE = 0.22
        const val LOW_FRACTION = 0.55
    }
}
