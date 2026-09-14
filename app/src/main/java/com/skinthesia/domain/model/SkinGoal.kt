package com.skinthesia.domain.model

import kotlinx.serialization.Serializable

/** Improvement goals a user can choose (screen 06). */
@Serializable
enum class SkinGoal(val label: String) {
    ACNE("Acne"),
    DARK_SPOTS("Dark spots"),
    UNEVEN_TONE("Uneven tone"),
    TEXTURE("Texture"),
    PORES("Pores"),
    HYDRATION("Hydration"),
    REDNESS("Redness"),
    DARK_CIRCLES("Dark circles"),
    FINE_LINES("Fine lines"),
    OVERALL_HEALTH("Overall skin health"),
}

/** The user's 12-week intention: what they chose, what matters most, and how they put it. */
@Serializable
data class GoalPlan(
    val goals: List<SkinGoal> = emptyList(),
    /** Up to [MAX_PRIORITIES] goals marked as the main focus, in order. */
    val priorities: List<SkinGoal> = emptyList(),
    /** The user's own words, for example "Clearer, healthier-looking skin". */
    val statement: String = "",
    val durationWeeks: Int = DEFAULT_WEEKS,
    val startedAt: Long? = null,
) {
    /** Priorities first, then remaining goals, never empty once goals exist. */
    val ranked: List<SkinGoal> get() = (priorities + goals).distinct()

    companion object {
        const val MAX_PRIORITIES = 3
        const val DEFAULT_WEEKS = 12
    }
}
