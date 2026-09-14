package com.skinthesia.domain.model

/** Self-reported age bracket (screen 05). */
enum class AgeRange(val minAge: Int, val maxAge: Int?) {
    UNDER_18(0, 17),
    AGE_18_24(18, 24),
    AGE_25_34(25, 34),
    AGE_35_44(35, 44),
    AGE_45_54(45, 54),
    AGE_55_PLUS(55, null),
}

/** Self-identified skin type (screen 05). */
enum class SkinType {
    NORMAL,
    DRY,
    OILY,
    COMBINATION,
    SENSITIVE,
}

/** Concerns the user wants addressed. Also used by analysis findings. */
enum class SkinConcern {
    ACNE,
    PIGMENTATION,
    TEXTURE,
    DRYNESS,
    REDNESS,
    FINE_LINES,
    DULLNESS,
    ENLARGED_PORES,
}

/** External influences that may affect skin (screen 06 and contributing factors). */
enum class LifestyleFactor {
    STRESS,
    SLEEP,
    DIET,
    SUN_EXPOSURE,
    WORK_ENVIRONMENT,
    HORMONAL_CHANGES,
    SKINCARE_PRODUCTS,
    CLIMATE,
    ILLNESS_MEDICATION,
}
