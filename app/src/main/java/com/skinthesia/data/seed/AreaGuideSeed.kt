package com.skinthesia.data.seed

import com.skinthesia.domain.model.AreaGuide
import com.skinthesia.domain.model.ImprovementArea

/**
 * Plain-language guides for each improvement area.
 * General skincare education only; never a diagnosis or a treatment claim.
 */
object AreaGuideSeed {

    private const val VISUAL_NOTE =
        "These are visual estimates from your selfie, which can shift with light and angle, not clinical measurements."

    val guides: Map<ImprovementArea, AreaGuide> = listOf(

        AreaGuide(
            area = ImprovementArea.HYDRATION,
            headline = "Hydration is about water, not oil.",
            whatItMeans = "Hydration describes how much water the outer layers of skin are holding. " +
                "Skin that is low on water can feel tight and look a little dull, even if it is also oily. " +
                "It tends to shift with the seasons, air conditioning and your routine.",
            howWeLook = "When you use the Skinthesia probe, we read its hydration index on your forehead and both cheeks and track it over time. " +
                "Without the probe, we rely on what you tell us, because a photo cannot measure water content.",
            whatCanHelp = listOf(
                "A gentle, non-stripping cleanser used morning and evening",
                "A humectant serum pressed into slightly damp skin",
                "A moisturizer matched to your skin type to help hold water in",
                "Daily sunscreen, so the barrier is not working harder than it needs to",
            ),
            keyIngredients = listOf("Hyaluronic acid", "Glycerin", "Ceramides", "Panthenol"),
            dailyHabits = listOf(
                "Apply moisturizer within a few minutes of cleansing",
                "Keep showers warm rather than hot",
                "Take probe readings at a similar time of day so they are easier to compare",
            ),
            whenToSeeAnExpert = "If skin stays tight, flaky or itchy despite a gentle routine, a dermatologist can help look at why.",
        ),

        AreaGuide(
            area = ImprovementArea.UNEVEN_TONE,
            headline = "Even tone is a gradual, patient kind of progress.",
            whatItMeans = "Uneven tone describes broader areas where colour looks patchy or different from the rest of the face. " +
                "It is very common, especially in medium and deeper skin tones, and often becomes more visible after sun exposure.",
            howWeLook = "We estimate how evenly colour is spread across regions of your selfie and compare it over time. " +
                VISUAL_NOTE,
            whatCanHelp = listOf(
                "Broad-spectrum SPF 50 every morning, reapplied when you are outdoors",
                "One tone-evening serum used consistently rather than several at once",
                "A gentle routine that keeps irritation low",
            ),
            keyIngredients = listOf("Niacinamide", "Vitamin C", "Tranexamic acid", "Azelaic acid"),
            dailyHabits = listOf(
                "Keep sunscreen by the door so it becomes part of leaving the house",
                "Check progress every four to six weeks rather than daily",
            ),
            whenToSeeAnExpert = "If patches appear suddenly, spread quickly or look very different from the rest of your skin, see a dermatologist.",
        ),

        AreaGuide(
            area = ImprovementArea.DARK_SPOTS,
            headline = "Dark spots tend to fade slowly, and sun care keeps them from settling in.",
            whatItMeans = "Dark spots are small, defined areas of extra pigment, such as marks left after a blemish or spots from sun exposure. " +
                "They are harmless in most cases, though they can take months to look lighter.",
            howWeLook = "We estimate the number and contrast of defined darker spots in your selfie and note which regions they sit in. " +
                VISUAL_NOTE,
            whatCanHelp = listOf(
                "Daily broad-spectrum sunscreen, which many people find is the single biggest help",
                "A vitamin C serum in the morning under sunscreen",
                "A tone-evening serum with tranexamic acid or niacinamide",
                "Leaving blemishes alone, so fewer new marks form",
            ),
            keyIngredients = listOf("Vitamin C", "Tranexamic acid", "Niacinamide", "Azelaic acid"),
            dailyHabits = listOf(
                "Reapply sunscreen when you spend time outdoors",
                "Wear a hat or seek shade in the middle of the day",
            ),
            whenToSeeAnExpert = "Any spot that changes in size, shape or colour, bleeds or looks unlike the others should be checked by a dermatologist promptly.",
        ),

        AreaGuide(
            area = ImprovementArea.TEXTURE,
            headline = "Smoother-looking skin usually comes from gentle, regular care.",
            whatItMeans = "Texture describes how smooth the surface of skin looks, including rough patches, small bumps and uneven areas. " +
                "It can vary with hydration, the natural pace of cell turnover and how skin is feeling that week.",
            howWeLook = "We estimate surface smoothness from how light falls across your selfie, region by region. " +
                VISUAL_NOTE,
            whatCanHelp = listOf(
                "A gentle exfoliating treatment two or three evenings a week",
                "Consistent hydration with a humectant serum and moisturizer",
                "A retinoid introduced slowly, if your skin tolerates it",
            ),
            keyIngredients = listOf("Lactic acid", "Salicylic acid", "Retinal", "Hyaluronic acid"),
            dailyHabits = listOf(
                "Pat skin dry rather than rubbing",
                "Leave at least one evening a week without exfoliating acids",
            ),
            whenToSeeAnExpert = "If roughness is persistent, itchy or spreading, a dermatologist can help work out what is going on.",
        ),

        AreaGuide(
            area = ImprovementArea.DARK_CIRCLES,
            headline = "Under-eye shadows have many contributors, and gentle care can help them look softer.",
            whatItMeans = "Dark circles can come from pigment, visible blood vessels under thin skin, or shadows from the natural shape of the eye area. " +
                "Genetics play a large part, so they are very common and often run in families.",
            howWeLook = "We estimate how much darker the under-eye area looks compared with the surrounding cheek in your selfie. " +
                VISUAL_NOTE,
            whatCanHelp = listOf(
                "A lightweight eye-area serum with caffeine or peptides",
                "Sunscreen taken gently up to the under-eye area",
                "A hydrating moisturizer so the area looks smoother",
            ),
            keyIngredients = listOf("Caffeine", "Peptides", "Niacinamide", "Hyaluronic acid"),
            dailyHabits = listOf(
                "Tap products in with your ring finger rather than rubbing",
                "Keep a regular wind-down routine, as part of general wellbeing",
                "Take progress photos in the same light each time",
            ),
            whenToSeeAnExpert = "If darkness or swelling appears suddenly, affects one side only or comes with discomfort, see a doctor or dermatologist.",
        ),

        AreaGuide(
            area = ImprovementArea.BLEMISHES,
            headline = "Blemish-prone skin responds best to calm, consistent care.",
            whatItMeans = "Blemishes include clogged pores, small bumps and occasional breakouts. " +
                "They are extremely common at every age, and for many people they come and go over the month.",
            howWeLook = "We estimate the number of visible blemishes in your selfie and where they cluster. " +
                VISUAL_NOTE,
            whatCanHelp = listOf(
                "A gentle gel cleanser that does not leave skin tight",
                "A salicylic acid treatment a few evenings a week",
                "A lightweight, non-comedogenic moisturizer",
                "Daily sunscreen to help marks from past blemishes look less noticeable",
            ),
            keyIngredients = listOf("Salicylic acid", "Niacinamide", "Azelaic acid", "Zinc PCA"),
            dailyHabits = listOf(
                "Change pillowcases regularly and keep phone screens clean",
                "Try to leave blemishes alone rather than squeezing them",
            ),
            whenToSeeAnExpert = "If breakouts are painful, deep, leaving marks or scars, or not settling after a few months, a dermatologist can offer options beyond a cosmetic routine.",
        ),

        AreaGuide(
            area = ImprovementArea.REDNESS,
            headline = "Redness is often a sign that skin would like things a little gentler.",
            whatItMeans = "Redness describes areas that look pink or flushed, often on the cheeks, nose or chin. " +
                "It can be temporary, from heat or a new product, or longer lasting in skin that is naturally more reactive.",
            howWeLook = "We estimate how much redder some regions look than others in your selfie. " +
                VISUAL_NOTE,
            whatCanHelp = listOf(
                "A fragrance-free, low-foam cleanser and lukewarm water",
                "A barrier-supporting moisturizer with ceramides",
                "Fewer active ingredients, introduced one at a time with a patch test",
                "Mineral sunscreen, which many sensitive skins find comfortable",
            ),
            keyIngredients = listOf("Centella asiatica", "Panthenol", "Ceramides", "Azelaic acid"),
            dailyHabits = listOf(
                "Patch test new products for a few days before using them on the face",
                "Notice what tends to come before a flush, such as heat or a new product, and note it in your journal",
            ),
            whenToSeeAnExpert = "If redness is persistent, burning, spreading or comes with bumps or visible vessels, see a dermatologist.",
        ),

        AreaGuide(
            area = ImprovementArea.PORES,
            headline = "Pores are a normal part of skin, and their appearance can be refined.",
            whatItMeans = "Everyone has pores. They tend to look more visible on the nose and cheeks, and when skin is oilier or pores are congested. " +
                "Their size is largely genetic, but how noticeable they look can change.",
            howWeLook = "We estimate how visible pores look in your selfie, mainly around the nose, cheeks and forehead. " +
                VISUAL_NOTE,
            whatCanHelp = listOf(
                "A gentle gel cleanser, morning and evening",
                "A niacinamide serum used consistently",
                "A salicylic acid treatment two or three evenings a week",
            ),
            keyIngredients = listOf("Niacinamide", "Salicylic acid", "Zinc PCA", "Retinal"),
            dailyHabits = listOf(
                "Remove sunscreen and makeup fully at the end of the day",
                "Choose non-comedogenic moisturizers and sunscreens",
            ),
            whenToSeeAnExpert = "If congestion is painful or keeps turning into deeper breakouts, a dermatologist can help.",
        ),

        AreaGuide(
            area = ImprovementArea.FINE_LINES,
            headline = "Fine lines are a natural part of skin, and care can help them look softer.",
            whatItMeans = "Fine lines are small, shallow lines that often appear around the eyes, forehead and mouth. " +
                "They can look more noticeable when skin is dehydrated, and they tend to become more visible with time and sun exposure.",
            howWeLook = "We estimate how visible fine lines look in your selfie, particularly around the eyes and forehead. " +
                VISUAL_NOTE,
            whatCanHelp = listOf(
                "Daily broad-spectrum sunscreen",
                "A retinoid or bakuchiol introduced gradually in the evening",
                "A hydrating serum and moisturizer, so skin looks plumper",
                "Peptides as a gentle, well-tolerated addition",
            ),
            keyIngredients = listOf("Retinal", "Bakuchiol", "Peptides", "Hyaluronic acid"),
            dailyHabits = listOf(
                "Wear sunglasses in bright light",
                "Keep your routine steady for at least eight to twelve weeks before judging it",
            ),
            whenToSeeAnExpert = "If you would like to explore options beyond a daily routine, a dermatologist can talk them through with you.",
        ),
    ).associateBy { it.area }

    fun guide(area: ImprovementArea): AreaGuide = guides.getValue(area)
}
