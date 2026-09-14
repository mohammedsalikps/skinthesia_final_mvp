package com.skinthesia.ai.recommendation

import com.skinthesia.domain.model.Assessment
import com.skinthesia.domain.model.PersonalizedPlan
import com.skinthesia.domain.model.Product
import com.skinthesia.domain.model.ProductRecommendation
import com.skinthesia.domain.model.SkinGoal
import com.skinthesia.domain.model.UserProfile

data class PlanInput(
    val profile: UserProfile,
    val assessment: Assessment,
    val catalog: List<Product>,
    val previous: PersonalizedPlan?,
    val now: Long,
    /** Set by the adaptive engine to steer the focus; otherwise chosen from goals and analysis. */
    val focusOverride: List<SkinGoal>? = null,
    /** Requests a lighter routine, for example after low adherence. */
    val simplify: Boolean = false,
)

/**
 * Builds personalised plans and explainable product recommendations from goals,
 * SkinPrint, camera and probe data, lifestyle context, existing products,
 * preferences and budget. Recommendation logic lives here, never in composables.
 */
interface RecommendationEngine {
    val engineName: String
    fun buildPlan(input: PlanInput): PersonalizedPlan

    /** Ranks the catalogue for browsing contexts such as the marketplace "For you" row. */
    fun rank(profile: UserProfile, assessment: Assessment?, catalog: List<Product>): List<ProductRecommendation>
}
