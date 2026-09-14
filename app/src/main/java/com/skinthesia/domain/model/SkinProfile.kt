package com.skinthesia.domain.model

/** Everything Skinthesia knows about the user's skin, gathered during onboarding. */
data class SkinProfile(
    val ageRange: AgeRange? = null,
    val skinType: SkinType? = null,
    val concerns: Set<SkinConcern> = emptySet(),
    /** Ordered as selected; capped at [SkinGoal.MAX_SELECTION]. */
    val goals: List<SkinGoal> = emptyList(),
    val target: SkinTarget? = null,
    val lifestyleFactors: Set<LifestyleFactor> = emptySet(),
    val baselinePhoto: SkinPhoto? = null,
) {
    val hasBaselinePhoto: Boolean get() = baselinePhoto != null
    val hasGoals: Boolean get() = goals.isNotEmpty()
    val hasTarget: Boolean get() = target != null && target.goals.isNotEmpty()
    val hasQuestionnaire: Boolean get() = ageRange != null && skinType != null

    companion object {
        val Empty = SkinProfile()
    }
}
