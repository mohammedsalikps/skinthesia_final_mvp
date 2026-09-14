package com.skinthesia.domain.usecase

import com.skinthesia.domain.model.SkinGoal
import com.skinthesia.domain.model.TargetGoal

/**
 * Turns the user's chosen goals into the quote-style target shown on the
 * "Your 12-week goal" screen, e.g. "Clearer, brighter and healthier skin".
 */
class BuildTargetPhraseUseCase {

    operator fun invoke(goals: List<SkinGoal>, targets: Set<TargetGoal>): String {
        val adjectives = buildList {
            goals.forEach { add(it.adjective()) }
            TargetGoal.entries.filter { it in targets }.forEach { add(it.adjective()) }
        }.distinct().filterNot { it == HEALTHY }.take(MAX_ADJECTIVES)

        val descriptors = if (adjectives.isEmpty()) listOf(DEFAULT_ADJECTIVE) else adjectives
        val leading = descriptors.first().replaceFirstChar { it.uppercase() }
        val rest = descriptors.drop(1).map { it.lowercase() }

        return when (rest.size) {
            0 -> "$leading, $HEALTHY skin"
            else -> "$leading, ${rest.joinToString(", ")} and $HEALTHY skin"
        }
    }

    private fun SkinGoal.adjective(): String = when (this) {
        SkinGoal.CLEARER_SKIN, SkinGoal.FEWER_BREAKOUTS -> "clearer"
        SkinGoal.EVEN_SKIN_TONE -> "more even"
        SkinGoal.SMOOTHER_TEXTURE -> "smoother"
        SkinGoal.REDUCE_DARK_SPOTS, SkinGoal.HEALTHY_GLOW -> "brighter"
        SkinGoal.STRONGER_BARRIER -> "stronger"
        SkinGoal.LESS_REDNESS -> "calmer"
        SkinGoal.REDUCE_FINE_LINES -> "firmer"
    }

    private fun TargetGoal.adjective(): String = when (this) {
        TargetGoal.REDUCE_BREAKOUTS -> "clearer"
        TargetGoal.FADE_DARK_SPOTS -> "brighter"
        TargetGoal.IMPROVE_TEXTURE -> "smoother"
        TargetGoal.BOOST_HYDRATION -> "more hydrated"
    }

    private companion object {
        const val HEALTHY = "healthier"
        const val DEFAULT_ADJECTIVE = "clearer"
        const val MAX_ADJECTIVES = 2
    }
}
