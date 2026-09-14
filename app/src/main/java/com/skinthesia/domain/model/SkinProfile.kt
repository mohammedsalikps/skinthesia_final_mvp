package com.skinthesia.domain.model

import kotlinx.serialization.Serializable

@Serializable
enum class SkinType(val label: String, val description: String) {
    NORMAL("Normal", "Balanced, rarely tight or shiny"),
    DRY("Dry", "Often feels tight or flaky"),
    OILY("Oily", "Shiny through the day"),
    COMBINATION("Combination", "Oilier T-zone, drier cheeks"),
    SENSITIVE("Sensitive", "Reacts easily to new products"),
}

/** Concerns a person reports in the questionnaire. */
@Serializable
enum class SkinConcern(val label: String) {
    BREAKOUTS("Breakouts"),
    DARK_SPOTS("Dark spots"),
    UNEVEN_TONE("Uneven tone"),
    TEXTURE("Texture"),
    VISIBLE_PORES("Visible pores"),
    DRYNESS("Dryness"),
    OILINESS("Oiliness"),
    REDNESS("Redness"),
    SENSITIVITY("Sensitivity"),
    DARK_CIRCLES("Dark circles"),
    FINE_LINES("Fine lines"),
    DULLNESS("Dullness"),
}

@Serializable
enum class RoutineLevel(val label: String, val description: String) {
    NONE("Just starting", "No regular routine yet"),
    MINIMAL("Minimal", "Cleanser and moisturizer"),
    BALANCED("Balanced", "A few steps, most days"),
    DEDICATED("Dedicated", "Serums, actives and daily SPF"),
}

/** Formula preferences and sensitivities that constrain recommendations. */
@Serializable
enum class Sensitivity(val label: String) {
    FRAGRANCE_FREE("Fragrance-free"),
    GENTLE_ONLY("Gentle formulas only"),
    VEGAN("Vegan"),
    CRUELTY_FREE("Cruelty-free"),
    NON_COMEDOGENIC("Non-comedogenic"),
    FEWER_STEPS("Fewer steps"),
}

@Serializable
enum class Budget(val label: String, val range: String, val maxPerProduct: Int) {
    ACCESSIBLE("Accessible", "Under ₹900 per product", 900),
    MID_RANGE("Mid-range", "₹900 – ₹1,800 per product", 1800),
    PREMIUM("Premium", "₹1,800 and above", Int.MAX_VALUE),
}

/** Everything the questionnaire gathers about the user's skin and habits. */
@Serializable
data class SkinProfile(
    val skinType: SkinType? = null,
    val concerns: Set<SkinConcern> = emptySet(),
    val routineLevel: RoutineLevel? = null,
    /** Categories the user already uses; recommendations respect these first. */
    val currentProducts: Set<ProductCategory> = emptySet(),
    val existingProductsNote: String = "",
    val preferences: Set<Sensitivity> = emptySet(),
    val budget: Budget = Budget.MID_RANGE,
) {
    val isSensitive: Boolean
        get() = skinType == SkinType.SENSITIVE ||
            SkinConcern.SENSITIVITY in concerns ||
            Sensitivity.GENTLE_ONLY in preferences
}
