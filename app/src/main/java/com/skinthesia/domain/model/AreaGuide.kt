package com.skinthesia.domain.model

/**
 * Plain-language education for one improvement area (screen 23 detail).
 * General skincare information only; never a diagnosis or treatment claim.
 */
data class AreaGuide(
    val area: ImprovementArea,
    /** One calm sentence that frames the area, for example "Hydration is about water, not oil." */
    val headline: String,
    val whatItMeans: String,
    /** How Skinthesia looks at this area: which estimates or readings feed it. */
    val howWeLook: String,
    val whatCanHelp: List<String>,
    val keyIngredients: List<String>,
    val dailyHabits: List<String>,
    val whenToSeeAnExpert: String,
)
