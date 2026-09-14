package com.skinthesia.domain.usecase

import com.skinthesia.domain.model.SkinGoal
import com.skinthesia.domain.model.TargetGoal

/**
 * Pre-selects measurable 12-week targets from the improvement goals the user
 * picked, so the target screen opens with a sensible default.
 */
class SuggestTargetGoalsUseCase {

    operator fun invoke(goals: List<SkinGoal>): Set<TargetGoal> {
        if (goals.isEmpty()) return TargetGoal.entries.toSet()
        return goals.map { it.toTarget() }.toSet()
    }

    private fun SkinGoal.toTarget(): TargetGoal = when (this) {
        SkinGoal.CLEARER_SKIN, SkinGoal.FEWER_BREAKOUTS -> TargetGoal.REDUCE_BREAKOUTS
        SkinGoal.EVEN_SKIN_TONE, SkinGoal.REDUCE_DARK_SPOTS -> TargetGoal.FADE_DARK_SPOTS
        SkinGoal.SMOOTHER_TEXTURE, SkinGoal.REDUCE_FINE_LINES -> TargetGoal.IMPROVE_TEXTURE
        SkinGoal.HEALTHY_GLOW, SkinGoal.STRONGER_BARRIER, SkinGoal.LESS_REDNESS -> TargetGoal.BOOST_HYDRATION
    }
}
