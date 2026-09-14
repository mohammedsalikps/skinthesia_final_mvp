package com.skinthesia.domain.model

/** Improvement goals a user can choose during onboarding (screen 03). */
enum class SkinGoal {
    CLEARER_SKIN,
    FEWER_BREAKOUTS,
    EVEN_SKIN_TONE,
    SMOOTHER_TEXTURE,
    REDUCE_DARK_SPOTS,
    HEALTHY_GLOW,
    STRONGER_BARRIER,
    LESS_REDNESS,
    REDUCE_FINE_LINES;

    companion object {
        /** Maximum number of goals a user may select at once. */
        const val MAX_SELECTION = 3
    }
}

/** Measurable 12-week outcomes the user commits to (screen 04). */
enum class TargetGoal {
    REDUCE_BREAKOUTS,
    FADE_DARK_SPOTS,
    IMPROVE_TEXTURE,
    BOOST_HYDRATION,
}

/** The user's stated 12-week target. */
data class SkinTarget(
    /** Human phrase such as "Clearer, healthier skin". */
    val phrase: String,
    val goals: Set<TargetGoal>,
    val durationWeeks: Int = DEFAULT_DURATION_WEEKS,
) {
    companion object {
        const val DEFAULT_DURATION_WEEKS = 12
    }
}
