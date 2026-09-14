package com.skinthesia.data.seed

import com.skinthesia.domain.model.Ingredient
import com.skinthesia.domain.model.Product
import com.skinthesia.domain.model.ProductAttribute
import com.skinthesia.domain.model.ProductCategory
import com.skinthesia.domain.model.ProductForm
import com.skinthesia.domain.model.ProductTone
import com.skinthesia.domain.model.RoutineTime
import com.skinthesia.domain.model.SkinGoal
import com.skinthesia.domain.model.SkinType

/**
 * Local product catalogue. All brands and products are fictional, apart from the
 * Skinthesia Lab house range. Descriptions are general cosmetic information only.
 */
object ProductCatalogSeed {

    private const val HOUSE = "Skinthesia Lab"
    private const val KERNE = "Kerne Clinical"
    private const val MONSOON = "Monsoon Botanica"
    private const val HALCYON = "Halcyon Grove"

    private val BOTH = setOf(RoutineTime.MORNING, RoutineTime.EVENING)
    private val MORNING_ONLY = setOf(RoutineTime.MORNING)
    private val EVENING_ONLY = setOf(RoutineTime.EVENING)

    val products: List<Product> = listOf(

        // ---------------------------------------------------------------- Cleansers

        Product(
            id = "prd-cloud-cream-cleanser",
            name = "Cloud Cream Cleanser",
            brand = HOUSE,
            category = ProductCategory.CLEANSER,
            form = ProductForm.PUMP,
            tone = ProductTone.IVORY,
            size = "150 ml",
            price = 690,
            description = "A soft, milky cream that lifts away the day without leaving skin tight. " +
                "Ceramides and oat help skin feel comfortable and calm after every rinse.",
            howToUse = "Massage a pump over damp skin for thirty seconds, then rinse with lukewarm water. " +
                "Use morning and evening.",
            keyIngredients = listOf(
                Ingredient(name = "Ceramide NP", role = "Helps support the skin barrier while you cleanse"),
                Ingredient(name = "Glycerin", role = "Draws water into the upper layers of skin"),
                Ingredient(name = "Oat kernel extract", role = "Helps skin feel soothed and comfortable"),
                Ingredient(name = "Panthenol", role = "Helps skin feel soft and less tight after rinsing"),
            ),
            fullIngredients = "Aqua, Glycerin, Cetearyl Alcohol, Caprylic/Capric Triglyceride, Coco-Glucoside, " +
                "Avena Sativa Kernel Extract, Panthenol, Ceramide NP, Cholesterol, Sodium Lauroyl Lactylate, " +
                "Carbomer, Xanthan Gum, Sodium Hydroxide, Phenoxyethanol, Ethylhexylglycerin",
            supportsGoals = setOf(SkinGoal.HYDRATION, SkinGoal.REDNESS, SkinGoal.OVERALL_HEALTH),
            skinTypes = setOf(SkinType.DRY, SkinType.SENSITIVE, SkinType.NORMAL),
            attributes = setOf(
                ProductAttribute.FRAGRANCE_FREE,
                ProductAttribute.GENTLE,
                ProductAttribute.NON_COMEDOGENIC,
                ProductAttribute.CRUELTY_FREE,
            ),
            routineTimes = BOTH,
            rating = 4.7f,
            reviewCount = 1840,
        ),

        Product(
            id = "prd-clear-balance-gel-cleanser",
            name = "Clear Balance Gel Cleanser",
            brand = KERNE,
            category = ProductCategory.CLEANSER,
            form = ProductForm.TUBE,
            tone = ProductTone.SAGE,
            size = "150 ml",
            price = 540,
            description = "A clear, low-foam gel that rinses away excess oil and leaves skin feeling fresh, not stripped. " +
                "Zinc PCA and green tea keep it balanced for oilier zones.",
            howToUse = "Work a small amount into a light lather with water, focus on the T-zone, then rinse. " +
                "Use morning and evening.",
            keyIngredients = listOf(
                Ingredient(name = "Zinc PCA", role = "Helps skin look less shiny through the day"),
                Ingredient(name = "Green tea extract", role = "An antioxidant that helps skin feel refreshed"),
                Ingredient(name = "Glycerin", role = "Keeps skin comfortable after cleansing"),
            ),
            fullIngredients = "Aqua, Sodium Cocoyl Glycinate, Glycerin, Cocamidopropyl Betaine, Zinc PCA, " +
                "Camellia Sinensis Leaf Extract, Allantoin, Sodium Chloride, Citric Acid, Acrylates Copolymer, " +
                "Sodium Benzoate, Potassium Sorbate",
            supportsGoals = setOf(SkinGoal.ACNE, SkinGoal.PORES, SkinGoal.OVERALL_HEALTH),
            skinTypes = setOf(SkinType.OILY, SkinType.COMBINATION, SkinType.NORMAL),
            attributes = setOf(
                ProductAttribute.FRAGRANCE_FREE,
                ProductAttribute.NON_COMEDOGENIC,
                ProductAttribute.LIGHTWEIGHT,
                ProductAttribute.VEGAN,
            ),
            routineTimes = BOTH,
            rating = 4.5f,
            reviewCount = 2210,
        ),

        Product(
            id = "prd-amino-soft-foam-cleanser",
            name = "Amino Soft Foam Cleanser",
            brand = MONSOON,
            category = ProductCategory.CLEANSER,
            form = ProductForm.PUMP,
            tone = ProductTone.BLUSH,
            size = "160 ml",
            price = 1150,
            description = "A pillowy foam made with mild amino acid cleansers and a faint scent of green tea. " +
                "Skin feels clean and supple, with no squeak.",
            howToUse = "Dispense one pump of foam into your palm and press it over damp skin, then rinse. " +
                "Use morning and evening.",
            keyIngredients = listOf(
                Ingredient(name = "Amino acid cleansers", role = "Cleanse gently without leaving skin tight"),
                Ingredient(name = "Allantoin", role = "Helps skin feel smooth and soothed"),
                Ingredient(name = "Green tea extract", role = "An antioxidant that helps skin feel refreshed"),
            ),
            fullIngredients = "Aqua, Sodium Cocoyl Alaninate, Glycerin, Sodium Lauroyl Sarcosinate, Coco-Betaine, " +
                "Camellia Sinensis Leaf Extract, Allantoin, Panthenol, Citric Acid, Sodium Benzoate, Parfum",
            supportsGoals = setOf(SkinGoal.OVERALL_HEALTH, SkinGoal.PORES),
            skinTypes = setOf(SkinType.NORMAL, SkinType.COMBINATION, SkinType.OILY),
            attributes = setOf(
                ProductAttribute.GENTLE,
                ProductAttribute.VEGAN,
                ProductAttribute.CRUELTY_FREE,
                ProductAttribute.NON_COMEDOGENIC,
            ),
            routineTimes = BOTH,
            rating = 4.4f,
            reviewCount = 760,
        ),

        Product(
            id = "prd-silk-melt-cleansing-balm",
            name = "Silk Melt Cleansing Balm",
            brand = HALCYON,
            category = ProductCategory.CLEANSER,
            form = ProductForm.JAR,
            tone = ProductTone.SAND,
            size = "90 g",
            price = 1950,
            description = "A solid balm that melts into a silky oil on contact and turns milky with water. " +
                "It dissolves sunscreen and makeup while skin stays soft.",
            howToUse = "In the evening, massage a small scoop over dry skin, add water to emulsify, then rinse. " +
                "In the morning, a smaller amount on damp skin works as a soft single cleanse.",
            keyIngredients = listOf(
                Ingredient(name = "Squalane", role = "A lightweight lipid that helps skin feel supple"),
                Ingredient(name = "Sunflower seed oil", role = "Helps dissolve sunscreen and makeup"),
                Ingredient(name = "Tocopherol", role = "Vitamin E, an antioxidant that protects the formula"),
            ),
            fullIngredients = "Helianthus Annuus Seed Oil, Squalane, Polyglyceryl-4 Oleate, Caprylic/Capric Triglyceride, " +
                "Polyethylene, Sorbitan Sesquioleate, Oryza Sativa Bran Oil, Tocopherol, Aqua, " +
                "Camellia Oleifera Seed Oil",
            supportsGoals = setOf(SkinGoal.HYDRATION, SkinGoal.OVERALL_HEALTH),
            skinTypes = setOf(SkinType.DRY, SkinType.NORMAL, SkinType.COMBINATION),
            attributes = setOf(
                ProductAttribute.FRAGRANCE_FREE,
                ProductAttribute.GENTLE,
                ProductAttribute.VEGAN,
                ProductAttribute.RICH,
            ),
            routineTimes = BOTH,
            rating = 4.6f,
            reviewCount = 430,
        ),

        // ---------------------------------------------------------------- Serums

        Product(
            id = "prd-hyaluronic-dew-serum",
            name = "Hyaluronic Dew Serum",
            brand = HOUSE,
            category = ProductCategory.SERUM,
            form = ProductForm.DROPPER,
            tone = ProductTone.MIST,
            size = "30 ml",
            price = 850,
            description = "A water-light serum with three weights of hyaluronic acid that sinks in within seconds. " +
                "Skin feels plump and dewy, never sticky.",
            howToUse = "Press three or four drops into damp skin after cleansing, then follow with moisturizer. " +
                "Use morning and evening.",
            keyIngredients = listOf(
                Ingredient(name = "Hyaluronic acid, multi-weight", role = "Draws in and holds water at different depths of the surface layers"),
                Ingredient(name = "Glycerin", role = "A humectant that helps skin stay hydrated"),
                Ingredient(name = "Panthenol", role = "Helps skin feel soft and comfortable"),
            ),
            fullIngredients = "Aqua, Glycerin, Propanediol, Sodium Hyaluronate, Hydrolyzed Sodium Hyaluronate, " +
                "Sodium Hyaluronate Crosspolymer, Panthenol, Allantoin, Betaine, Pentylene Glycol, " +
                "Phenoxyethanol, Ethylhexylglycerin",
            supportsGoals = setOf(SkinGoal.HYDRATION, SkinGoal.TEXTURE, SkinGoal.OVERALL_HEALTH),
            skinTypes = setOf(SkinType.NORMAL, SkinType.DRY, SkinType.OILY, SkinType.COMBINATION, SkinType.SENSITIVE),
            attributes = setOf(
                ProductAttribute.FRAGRANCE_FREE,
                ProductAttribute.GENTLE,
                ProductAttribute.LIGHTWEIGHT,
                ProductAttribute.VEGAN,
                ProductAttribute.CRUELTY_FREE,
                ProductAttribute.NON_COMEDOGENIC,
            ),
            routineTimes = BOTH,
            rating = 4.8f,
            reviewCount = 2360,
        ),

        Product(
            id = "prd-niacinamide-clarity-serum",
            name = "Niacinamide 5% Clarity Serum",
            brand = HOUSE,
            category = ProductCategory.SERUM,
            form = ProductForm.DROPPER,
            tone = ProductTone.BLUSH,
            size = "30 ml",
            price = 790,
            description = "A silky, fast-absorbing serum built around a moderate 5% niacinamide. " +
                "It helps pores look refined and skin look more even, without the tingle of higher strengths.",
            howToUse = "Smooth two or three drops over the face after cleansing. " +
                "Use morning, evening or both, before moisturizer.",
            keyIngredients = listOf(
                Ingredient(name = "Niacinamide 5%", role = "Helps pores look refined and tone look more even"),
                Ingredient(name = "Zinc PCA 1%", role = "Helps skin look less shiny through the day"),
                Ingredient(name = "Panthenol", role = "Helps keep skin comfortable"),
            ),
            fullIngredients = "Aqua, Niacinamide, Propanediol, Glycerin, Zinc PCA, Panthenol, Sodium Hyaluronate, " +
                "Tamarindus Indica Seed Gum, Xanthan Gum, Pentylene Glycol, Phenoxyethanol, Ethylhexylglycerin",
            supportsGoals = setOf(SkinGoal.PORES, SkinGoal.UNEVEN_TONE, SkinGoal.ACNE, SkinGoal.TEXTURE),
            skinTypes = setOf(SkinType.OILY, SkinType.COMBINATION, SkinType.NORMAL, SkinType.SENSITIVE),
            attributes = setOf(
                ProductAttribute.FRAGRANCE_FREE,
                ProductAttribute.NON_COMEDOGENIC,
                ProductAttribute.LIGHTWEIGHT,
                ProductAttribute.VEGAN,
            ),
            routineTimes = BOTH,
            rating = 4.6f,
            reviewCount = 1920,
        ),

        Product(
            id = "prd-bright-c-12-serum",
            name = "Bright C 12 Serum",
            brand = KERNE,
            category = ProductCategory.SERUM,
            form = ProductForm.DROPPER,
            tone = ProductTone.AMBER,
            size = "30 ml",
            price = 1490,
            description = "A stable vitamin C serum in amber glass, with a clean, slightly silky finish. " +
                "Used daily under sunscreen, it helps dark spots look softer and skin look brighter over time.",
            howToUse = "Apply four drops to dry skin in the morning, let it settle for a minute, then moisturize and apply sunscreen.",
            keyIngredients = listOf(
                Ingredient(name = "Ethyl ascorbic acid 12%", role = "A stable vitamin C that helps skin look brighter and more even"),
                Ingredient(name = "Ferulic acid", role = "An antioxidant that supports vitamin C"),
                Ingredient(name = "Tocopherol", role = "Vitamin E, an antioxidant partner"),
            ),
            fullIngredients = "Aqua, 3-O-Ethyl Ascorbic Acid, Propanediol, Glycerin, Ferulic Acid, Tocopherol, " +
                "Sodium Hyaluronate, Hydroxyethylcellulose, Sodium Metabisulfite, Citric Acid, " +
                "Phenoxyethanol, Ethylhexylglycerin",
            supportsGoals = setOf(SkinGoal.DARK_SPOTS, SkinGoal.UNEVEN_TONE),
            skinTypes = setOf(SkinType.NORMAL, SkinType.OILY, SkinType.COMBINATION, SkinType.DRY),
            attributes = setOf(
                ProductAttribute.FRAGRANCE_FREE,
                ProductAttribute.VEGAN,
                ProductAttribute.CRUELTY_FREE,
            ),
            routineTimes = MORNING_ONLY,
            rating = 4.5f,
            reviewCount = 1380,
        ),

        Product(
            id = "prd-even-tone-tranexamic-serum",
            name = "Even Tone Tranexamic Serum",
            brand = MONSOON,
            category = ProductCategory.SERUM,
            form = ProductForm.DROPPER,
            tone = ProductTone.BLUSH,
            size = "30 ml",
            price = 1890,
            description = "A soft, milky serum pairing tranexamic acid with a gentle azelaic acid derivative. " +
                "It helps uneven tone and lingering marks look more even, and sits comfortably on reactive skin.",
            howToUse = "Apply three drops after cleansing, morning and evening. " +
                "Always follow with sunscreen during the day.",
            keyIngredients = listOf(
                Ingredient(name = "Tranexamic acid 3%", role = "Helps uneven tone and dark marks look more even"),
                Ingredient(name = "Potassium azeloyl diglycinate", role = "A soluble azelaic acid derivative that helps skin look calmer and more even"),
                Ingredient(name = "Niacinamide 4%", role = "Helps tone look more uniform"),
            ),
            fullIngredients = "Aqua, Niacinamide, Tranexamic Acid, Potassium Azeloyl Diglycinate, Glycerin, Butylene Glycol, " +
                "Glycyrrhiza Glabra Root Extract, Sodium Hyaluronate, Allantoin, Carbomer, Arginine, " +
                "1,2-Hexanediol, Phenoxyethanol",
            supportsGoals = setOf(SkinGoal.UNEVEN_TONE, SkinGoal.DARK_SPOTS, SkinGoal.REDNESS),
            skinTypes = setOf(SkinType.NORMAL, SkinType.DRY, SkinType.OILY, SkinType.COMBINATION, SkinType.SENSITIVE),
            attributes = setOf(
                ProductAttribute.FRAGRANCE_FREE,
                ProductAttribute.GENTLE,
                ProductAttribute.LIGHTWEIGHT,
                ProductAttribute.VEGAN,
            ),
            routineTimes = BOTH,
            rating = 4.6f,
            reviewCount = 980,
        ),

        Product(
            id = "prd-bright-eyes-peptide-serum",
            name = "Bright Eyes Peptide Serum",
            brand = HALCYON,
            category = ProductCategory.SERUM,
            form = ProductForm.TUBE,
            tone = ProductTone.CLAY,
            size = "15 ml",
            price = 2150,
            description = "A cool, weightless eye-area serum with a smooth metal tip. " +
                "Caffeine and peptides help the under-eye area look more rested and refreshed.",
            howToUse = "Dot a rice-grain amount along the orbital bone and tap in gently with your ring finger. " +
                "Use morning and evening.",
            keyIngredients = listOf(
                Ingredient(name = "Caffeine 3%", role = "Helps the under-eye area look less puffy and more awake"),
                Ingredient(name = "Peptide complex", role = "Helps the delicate eye area look smoother"),
                Ingredient(name = "Sodium hyaluronate", role = "Keeps thin under-eye skin hydrated"),
                Ingredient(name = "Green tea extract", role = "An antioxidant that helps skin look refreshed"),
            ),
            fullIngredients = "Aqua, Glycerin, Caffeine, Propanediol, Sodium Hyaluronate, Acetyl Tetrapeptide-5, " +
                "Palmitoyl Tripeptide-1, Palmitoyl Tetrapeptide-7, Camellia Sinensis Leaf Extract, Panthenol, " +
                "Hydroxyethylcellulose, Phenoxyethanol, Ethylhexylglycerin",
            supportsGoals = setOf(SkinGoal.DARK_CIRCLES, SkinGoal.HYDRATION, SkinGoal.FINE_LINES),
            skinTypes = setOf(SkinType.NORMAL, SkinType.DRY, SkinType.OILY, SkinType.COMBINATION, SkinType.SENSITIVE),
            attributes = setOf(
                ProductAttribute.FRAGRANCE_FREE,
                ProductAttribute.GENTLE,
                ProductAttribute.LIGHTWEIGHT,
                ProductAttribute.CRUELTY_FREE,
            ),
            routineTimes = BOTH,
            rating = 4.3f,
            reviewCount = 640,
        ),

        Product(
            id = "prd-bakuchiol-peptide-serum",
            name = "Bakuchiol Peptide Serum",
            brand = HALCYON,
            category = ProductCategory.SERUM,
            form = ProductForm.DROPPER,
            tone = ProductTone.AMBER,
            size = "30 ml",
            price = 1750,
            description = "A golden, satin-finish serum that pairs plant-derived bakuchiol with supportive peptides. " +
                "A gentle option for anyone who wants smoother-looking skin without a retinoid.",
            howToUse = "Warm three drops between your fingertips and press into skin after cleansing. " +
                "Use morning and evening, followed by moisturizer.",
            keyIngredients = listOf(
                Ingredient(name = "Bakuchiol 1%", role = "A plant-derived ingredient that helps skin look smoother"),
                Ingredient(name = "Peptide complex", role = "Helps skin look firmer and more supple"),
                Ingredient(name = "Squalane", role = "Keeps skin soft and helps prevent moisture loss"),
            ),
            fullIngredients = "Aqua, Squalane, Glycerin, Bakuchiol, Palmitoyl Tripeptide-1, Palmitoyl Tetrapeptide-7, " +
                "Caprylic/Capric Triglyceride, Sodium Hyaluronate, Tocopherol, Polyglyceryl-10 Laurate, " +
                "Xanthan Gum, Phenoxyethanol",
            supportsGoals = setOf(SkinGoal.FINE_LINES, SkinGoal.TEXTURE, SkinGoal.OVERALL_HEALTH),
            skinTypes = setOf(SkinType.NORMAL, SkinType.DRY, SkinType.COMBINATION, SkinType.SENSITIVE),
            attributes = setOf(
                ProductAttribute.FRAGRANCE_FREE,
                ProductAttribute.GENTLE,
                ProductAttribute.VEGAN,
                ProductAttribute.CRUELTY_FREE,
            ),
            routineTimes = BOTH,
            rating = 4.4f,
            reviewCount = 520,
        ),

        // ---------------------------------------------------------------- Moisturizers

        Product(
            id = "prd-aqua-gel-moisturizer",
            name = "Aqua Gel Moisturizer",
            brand = MONSOON,
            category = ProductCategory.MOISTURIZER,
            form = ProductForm.TUBE,
            tone = ProductTone.MIST,
            size = "50 ml",
            price = 620,
            description = "A cooling water-gel that melts into skin and leaves a soft, fresh finish. " +
                "Light enough for humid days and under sunscreen.",
            howToUse = "Smooth a pea-sized amount over the face and neck as the last step before sunscreen in the morning, " +
                "and as your final step in the evening.",
            keyIngredients = listOf(
                Ingredient(name = "Glycerin", role = "Draws water into the upper layers of skin"),
                Ingredient(name = "Sodium hyaluronate", role = "Helps skin hold on to hydration"),
                Ingredient(name = "Green tea extract", role = "An antioxidant that helps skin feel refreshed"),
                Ingredient(name = "Allantoin", role = "Helps skin feel smooth and comfortable"),
            ),
            fullIngredients = "Aqua, Glycerin, Dimethicone, Butylene Glycol, Sodium Hyaluronate, Camellia Sinensis Leaf Extract, " +
                "Allantoin, Betaine, Carbomer, Tromethamine, Ethylhexylglycerin, Phenoxyethanol",
            supportsGoals = setOf(SkinGoal.HYDRATION, SkinGoal.PORES, SkinGoal.OVERALL_HEALTH),
            skinTypes = setOf(SkinType.OILY, SkinType.COMBINATION, SkinType.NORMAL),
            attributes = setOf(
                ProductAttribute.FRAGRANCE_FREE,
                ProductAttribute.LIGHTWEIGHT,
                ProductAttribute.NON_COMEDOGENIC,
                ProductAttribute.VEGAN,
            ),
            routineTimes = BOTH,
            rating = 4.5f,
            reviewCount = 1640,
        ),

        Product(
            id = "prd-barrier-comfort-cream",
            name = "Barrier Comfort Cream",
            brand = HOUSE,
            category = ProductCategory.MOISTURIZER,
            form = ProductForm.JAR,
            tone = ProductTone.IVORY,
            size = "50 g",
            price = 1290,
            description = "A rich, cushiony cream with a ceramide complex that helps skin feel comforted from the first use. " +
                "It absorbs to a soft satin finish, not a greasy one.",
            howToUse = "Warm a small amount between your fingertips and press over the face morning and evening. " +
                "Use a little more on dry or tight areas.",
            keyIngredients = listOf(
                Ingredient(name = "Ceramide complex", role = "Helps support the skin barrier and reduce moisture loss"),
                Ingredient(name = "Squalane", role = "Keeps skin soft and supple"),
                Ingredient(name = "Panthenol 5%", role = "Helps skin feel calm and comfortable"),
                Ingredient(name = "Cholesterol", role = "Works with ceramides to support the barrier"),
            ),
            fullIngredients = "Aqua, Glycerin, Squalane, Cetearyl Alcohol, Butyrospermum Parkii Butter, Panthenol, " +
                "Ceramide NP, Ceramide AP, Ceramide EOP, Cholesterol, Phytosphingosine, Sodium Lauroyl Lactylate, " +
                "Carbomer, Xanthan Gum, Phenoxyethanol, Ethylhexylglycerin",
            supportsGoals = setOf(SkinGoal.HYDRATION, SkinGoal.REDNESS, SkinGoal.TEXTURE, SkinGoal.OVERALL_HEALTH),
            skinTypes = setOf(SkinType.DRY, SkinType.SENSITIVE, SkinType.NORMAL),
            attributes = setOf(
                ProductAttribute.FRAGRANCE_FREE,
                ProductAttribute.GENTLE,
                ProductAttribute.RICH,
                ProductAttribute.CRUELTY_FREE,
            ),
            routineTimes = BOTH,
            rating = 4.8f,
            reviewCount = 2140,
        ),

        Product(
            id = "prd-daily-balance-lotion",
            name = "Daily Balance Lotion",
            brand = KERNE,
            category = ProductCategory.MOISTURIZER,
            form = ProductForm.PUMP,
            tone = ProductTone.SAND,
            size = "75 ml",
            price = 990,
            description = "A fluid everyday lotion that hydrates without weight and wears well under sunscreen. " +
                "A small dose of niacinamide helps skin look balanced through the day.",
            howToUse = "Apply one pump over the face after serums, morning and evening.",
            keyIngredients = listOf(
                Ingredient(name = "Ceramide NP", role = "Helps support the skin barrier"),
                Ingredient(name = "Niacinamide 2%", role = "Helps skin look more balanced and even"),
                Ingredient(name = "Glycerin", role = "Draws water into the upper layers of skin"),
            ),
            fullIngredients = "Aqua, Glycerin, Caprylic/Capric Triglyceride, Niacinamide, Cetearyl Alcohol, Dimethicone, " +
                "Ceramide NP, Sodium Hyaluronate, Glyceryl Stearate, PEG-100 Stearate, Carbomer, " +
                "Sodium Hydroxide, Phenoxyethanol",
            supportsGoals = setOf(SkinGoal.HYDRATION, SkinGoal.PORES, SkinGoal.OVERALL_HEALTH),
            skinTypes = setOf(SkinType.NORMAL, SkinType.COMBINATION, SkinType.OILY),
            attributes = setOf(
                ProductAttribute.FRAGRANCE_FREE,
                ProductAttribute.NON_COMEDOGENIC,
                ProductAttribute.LIGHTWEIGHT,
            ),
            routineTimes = BOTH,
            rating = 4.4f,
            reviewCount = 870,
        ),

        Product(
            id = "prd-squalane-cloud-cream",
            name = "Squalane Cloud Cream",
            brand = HALCYON,
            category = ProductCategory.MOISTURIZER,
            form = ProductForm.JAR,
            tone = ProductTone.CLAY,
            size = "50 g",
            price = 2450,
            description = "A whipped, enveloping cream with a trace of neroli that softens skin overnight. " +
                "Squalane and peptides leave dry skin feeling cushioned and supple by morning.",
            howToUse = "Press a small amount over the face and neck as the last step, morning and evening. " +
                "Use a thinner layer in the morning under sunscreen.",
            keyIngredients = listOf(
                Ingredient(name = "Squalane", role = "Keeps skin soft and helps prevent moisture loss"),
                Ingredient(name = "Peptide complex", role = "Helps skin look smoother and more supple"),
                Ingredient(name = "Shea butter", role = "A rich emollient that helps dry skin feel comfortable"),
                Ingredient(name = "Oat kernel extract", role = "Helps skin feel soothed"),
            ),
            fullIngredients = "Aqua, Squalane, Glycerin, Butyrospermum Parkii Butter, Cetearyl Alcohol, Caprylic/Capric Triglyceride, " +
                "Palmitoyl Tripeptide-1, Palmitoyl Tetrapeptide-7, Avena Sativa Kernel Extract, Tocopherol, " +
                "Sodium Stearoyl Glutamate, Xanthan Gum, Parfum, Phenoxyethanol",
            supportsGoals = setOf(SkinGoal.HYDRATION, SkinGoal.FINE_LINES, SkinGoal.TEXTURE),
            skinTypes = setOf(SkinType.DRY, SkinType.NORMAL),
            attributes = setOf(
                ProductAttribute.RICH,
                ProductAttribute.VEGAN,
                ProductAttribute.CRUELTY_FREE,
            ),
            routineTimes = BOTH,
            rating = 4.6f,
            reviewCount = 410,
        ),

        // ---------------------------------------------------------------- Sunscreens

        Product(
            id = "prd-daily-veil-spf50-fluid",
            name = "Daily Veil SPF 50 Fluid",
            brand = HOUSE,
            category = ProductCategory.SUNSCREEN,
            form = ProductForm.TUBE,
            tone = ProductTone.MIST,
            size = "50 ml",
            price = 890,
            description = "An invisible, fast-drying fluid with SPF 50 and PA++++ that leaves no white cast. " +
                "Modern filters keep it light enough to wear every day, under makeup or on its own.",
            howToUse = "Apply two finger-lengths to the face and neck as the last morning step. " +
                "Reapply every two hours when outdoors.",
            keyIngredients = listOf(
                Ingredient(name = "Bis-ethylhexyloxyphenol methoxyphenyl triazine", role = "A modern, photostable filter covering UVA and UVB"),
                Ingredient(name = "Ethylhexyl triazone", role = "A UVB filter that keeps the texture light"),
                Ingredient(name = "Niacinamide 2%", role = "Helps skin look more even"),
                Ingredient(name = "Glycerin", role = "Keeps skin comfortable through the day"),
            ),
            fullIngredients = "Aqua, Dibutyl Adipate, Ethylhexyl Triazone, Bis-Ethylhexyloxyphenol Methoxyphenyl Triazine, " +
                "Diethylamino Hydroxybenzoyl Hexyl Benzoate, Glycerin, Niacinamide, Propanediol, Silica, " +
                "Polyglyceryl-3 Methylglucose Distearate, Tocopherol, Xanthan Gum, Phenoxyethanol, Ethylhexylglycerin",
            supportsGoals = setOf(SkinGoal.DARK_SPOTS, SkinGoal.UNEVEN_TONE, SkinGoal.OVERALL_HEALTH),
            skinTypes = setOf(SkinType.NORMAL, SkinType.DRY, SkinType.OILY, SkinType.COMBINATION, SkinType.SENSITIVE),
            attributes = setOf(
                ProductAttribute.BROAD_SPECTRUM,
                ProductAttribute.FRAGRANCE_FREE,
                ProductAttribute.LIGHTWEIGHT,
                ProductAttribute.NON_COMEDOGENIC,
            ),
            routineTimes = MORNING_ONLY,
            rating = 4.7f,
            reviewCount = 2380,
        ),

        Product(
            id = "prd-sheer-tint-mineral-spf45",
            name = "Sheer Tint Mineral SPF 45",
            brand = HALCYON,
            category = ProductCategory.SUNSCREEN,
            form = ProductForm.TUBE,
            tone = ProductTone.CLAY,
            size = "40 ml",
            price = 1850,
            description = "A creamy mineral sunscreen with a sheer, adaptable tint that softens the look of redness. " +
                "Iron oxides add a little extra help against visible light.",
            howToUse = "Shake well and smooth two finger-lengths over the face as the last morning step. " +
                "Reapply every two hours when outdoors.",
            keyIngredients = listOf(
                Ingredient(name = "Zinc oxide", role = "A mineral filter that offers broad UVA and UVB protection"),
                Ingredient(name = "Iron oxides", role = "Give a sheer tint and help against visible light"),
                Ingredient(name = "Squalane", role = "Keeps the finish soft and comfortable"),
            ),
            fullIngredients = "Zinc Oxide, Aqua, Caprylic/Capric Triglyceride, Squalane, Glycerin, Polyglyceryl-2 Dipolyhydroxystearate, " +
                "Isostearic Acid, Magnesium Sulfate, Tocopherol, CI 77491, CI 77492, CI 77499, " +
                "Polyhydroxystearic Acid, Phenoxyethanol",
            supportsGoals = setOf(SkinGoal.DARK_SPOTS, SkinGoal.UNEVEN_TONE, SkinGoal.OVERALL_HEALTH, SkinGoal.REDNESS),
            skinTypes = setOf(SkinType.DRY, SkinType.NORMAL, SkinType.SENSITIVE, SkinType.COMBINATION),
            attributes = setOf(
                ProductAttribute.BROAD_SPECTRUM,
                ProductAttribute.FRAGRANCE_FREE,
                ProductAttribute.GENTLE,
                ProductAttribute.CRUELTY_FREE,
            ),
            routineTimes = MORNING_ONLY,
            rating = 4.4f,
            reviewCount = 690,
        ),

        Product(
            id = "prd-aqua-shield-gel-spf50",
            name = "Aqua Shield Gel SPF 50",
            brand = MONSOON,
            category = ProductCategory.SUNSCREEN,
            form = ProductForm.TUBE,
            tone = ProductTone.SAGE,
            size = "50 ml",
            price = 1190,
            description = "A clear, watery gel sunscreen that dries down to a fresh, barely-there finish. " +
                "Made for humid weather and oilier skin.",
            howToUse = "Apply two finger-lengths to the face and neck as the last morning step. " +
                "Reapply every two hours when outdoors, and after sweating.",
            keyIngredients = listOf(
                Ingredient(name = "Bis-ethylhexyloxyphenol methoxyphenyl triazine", role = "A modern, photostable filter covering UVA and UVB"),
                Ingredient(name = "Green tea extract", role = "An antioxidant that helps skin feel refreshed"),
                Ingredient(name = "Sodium hyaluronate", role = "Adds light hydration without heaviness"),
            ),
            fullIngredients = "Aqua, Alcohol Denat., Ethylhexyl Triazone, Bis-Ethylhexyloxyphenol Methoxyphenyl Triazine, " +
                "Diethylamino Hydroxybenzoyl Hexyl Benzoate, Butylene Glycol, Camellia Sinensis Leaf Extract, " +
                "Sodium Hyaluronate, Silica, Acrylates/C10-30 Alkyl Acrylate Crosspolymer, Tromethamine, Phenoxyethanol",
            supportsGoals = setOf(SkinGoal.DARK_SPOTS, SkinGoal.UNEVEN_TONE, SkinGoal.OVERALL_HEALTH),
            skinTypes = setOf(SkinType.OILY, SkinType.COMBINATION, SkinType.NORMAL),
            attributes = setOf(
                ProductAttribute.BROAD_SPECTRUM,
                ProductAttribute.LIGHTWEIGHT,
                ProductAttribute.NON_COMEDOGENIC,
                ProductAttribute.VEGAN,
            ),
            routineTimes = MORNING_ONLY,
            rating = 4.5f,
            reviewCount = 1260,
        ),

        Product(
            id = "prd-matte-daily-lotion-spf50",
            name = "Matte Daily Lotion SPF 50",
            brand = KERNE,
            category = ProductCategory.SUNSCREEN,
            form = ProductForm.PUMP,
            tone = ProductTone.SAND,
            size = "60 ml",
            price = 1450,
            description = "A hybrid mineral and modern-filter lotion with a soft-matte finish that keeps shine in check. " +
                "It sets quickly and sits well under makeup.",
            howToUse = "Apply two finger-lengths to the face and neck as the last morning step. " +
                "Reapply every two hours when outdoors.",
            keyIngredients = listOf(
                Ingredient(name = "Zinc oxide", role = "A mineral filter that offers broad UVA and UVB protection"),
                Ingredient(name = "Bis-ethylhexyloxyphenol methoxyphenyl triazine", role = "A modern, photostable filter covering UVA and UVB"),
                Ingredient(name = "Silica", role = "Softly mattifies and blurs shine"),
                Ingredient(name = "Niacinamide 2%", role = "Helps skin look balanced and even"),
            ),
            fullIngredients = "Aqua, Zinc Oxide, C12-15 Alkyl Benzoate, Bis-Ethylhexyloxyphenol Methoxyphenyl Triazine, " +
                "Ethylhexyl Triazone, Silica, Niacinamide, Glycerin, Cetyl PEG/PPG-10/1 Dimethicone, " +
                "Sodium Chloride, Tocopherol, Phenoxyethanol, Ethylhexylglycerin",
            supportsGoals = setOf(SkinGoal.DARK_SPOTS, SkinGoal.UNEVEN_TONE, SkinGoal.OVERALL_HEALTH, SkinGoal.PORES),
            skinTypes = setOf(SkinType.OILY, SkinType.COMBINATION, SkinType.NORMAL),
            attributes = setOf(
                ProductAttribute.BROAD_SPECTRUM,
                ProductAttribute.FRAGRANCE_FREE,
                ProductAttribute.NON_COMEDOGENIC,
                ProductAttribute.LIGHTWEIGHT,
            ),
            routineTimes = MORNING_ONLY,
            rating = 4.5f,
            reviewCount = 1090,
        ),

        // ---------------------------------------------------------------- Treatments

        Product(
            id = "prd-bha-clarifying-liquid",
            name = "BHA 2% Clarifying Liquid",
            brand = KERNE,
            category = ProductCategory.TREATMENT,
            form = ProductForm.BOTTLE,
            tone = ProductTone.MIST,
            size = "100 ml",
            price = 740,
            description = "A clear, watery leave-on liquid with 2% salicylic acid that works inside the pore lining. " +
                "Used a few evenings a week, it helps pores look clearer and skin feel smoother.",
            howToUse = "Sweep over clean, dry skin with a cotton pad or your palms, two or three evenings a week, then moisturize. " +
                "Avoid using on the same night as a retinoid.",
            keyIngredients = listOf(
                Ingredient(name = "Salicylic acid 2%", role = "An oil-soluble exfoliant that helps clear the look of congested pores"),
                Ingredient(name = "Zinc PCA", role = "Helps skin look less shiny"),
                Ingredient(name = "Green tea extract", role = "An antioxidant that helps skin feel calm"),
            ),
            fullIngredients = "Aqua, Propanediol, Salicylic Acid, Butylene Glycol, Zinc PCA, Camellia Sinensis Leaf Extract, " +
                "Allantoin, Sodium Hydroxide, Polysorbate 20, Hydroxyethylcellulose, 1,2-Hexanediol, Phenoxyethanol",
            supportsGoals = setOf(SkinGoal.ACNE, SkinGoal.PORES, SkinGoal.TEXTURE),
            skinTypes = setOf(SkinType.OILY, SkinType.COMBINATION, SkinType.NORMAL),
            attributes = setOf(
                ProductAttribute.FRAGRANCE_FREE,
                ProductAttribute.NON_COMEDOGENIC,
                ProductAttribute.LIGHTWEIGHT,
                ProductAttribute.VEGAN,
            ),
            routineTimes = EVENING_ONLY,
            rating = 4.6f,
            reviewCount = 1720,
        ),

        Product(
            id = "prd-retinal-night-treatment",
            name = "Retinal 0.05% Night Treatment",
            brand = HOUSE,
            category = ProductCategory.TREATMENT,
            form = ProductForm.PUMP,
            tone = ProductTone.AMBER,
            size = "30 ml",
            price = 1690,
            description = "A soft, cushioned cream-serum with encapsulated retinal, buffered by squalane and ceramides. " +
                "Over time it helps skin look smoother and fine lines look softer.",
            howToUse = "Apply a pea-sized amount to dry skin two evenings a week, building up slowly as your skin allows. " +
                "Wear sunscreen every morning.",
            keyIngredients = listOf(
                Ingredient(name = "Encapsulated retinal 0.05%", role = "A vitamin A form that helps skin look smoother over time"),
                Ingredient(name = "Squalane", role = "Cushions the formula and keeps skin soft"),
                Ingredient(name = "Ceramide NP", role = "Helps support the barrier while skin adjusts"),
                Ingredient(name = "Bisabolol", role = "Helps skin feel calm"),
            ),
            fullIngredients = "Aqua, Squalane, Glycerin, Caprylic/Capric Triglyceride, Cetearyl Alcohol, Retinal, " +
                "Ceramide NP, Bisabolol, Tocopherol, Hydrogenated Lecithin, Sodium Hyaluronate, " +
                "Carbomer, Disodium EDTA, Phenoxyethanol",
            supportsGoals = setOf(SkinGoal.FINE_LINES, SkinGoal.TEXTURE),
            skinTypes = setOf(SkinType.NORMAL, SkinType.DRY, SkinType.OILY, SkinType.COMBINATION),
            attributes = setOf(
                ProductAttribute.FRAGRANCE_FREE,
                ProductAttribute.CRUELTY_FREE,
            ),
            routineTimes = EVENING_ONLY,
            rating = 4.5f,
            reviewCount = 880,
        ),

        Product(
            id = "prd-centella-calm-mask",
            name = "Centella Calm Mask",
            brand = MONSOON,
            category = ProductCategory.TREATMENT,
            form = ProductForm.JAR,
            tone = ProductTone.SAGE,
            size = "75 g",
            price = 980,
            description = "A cool, sage-green cream mask with centella and panthenol for days when skin feels warm or stressed. " +
                "It leaves skin feeling soothed, soft and comfortable.",
            howToUse = "Smooth a generous layer over clean skin for ten minutes, then rinse or tissue off. " +
                "Use two or three times a week, morning or evening.",
            keyIngredients = listOf(
                Ingredient(name = "Centella asiatica extract", role = "Helps skin feel soothed and comfortable"),
                Ingredient(name = "Madecassoside", role = "A centella compound that helps skin look calmer"),
                Ingredient(name = "Panthenol", role = "Helps skin feel soft and less tight"),
                Ingredient(name = "Allantoin", role = "Helps skin feel smooth"),
            ),
            fullIngredients = "Aqua, Glycerin, Kaolin, Centella Asiatica Extract, Panthenol, Butylene Glycol, Madecassoside, " +
                "Allantoin, Squalane, Cetearyl Alcohol, Chromium Oxide Greens, Xanthan Gum, 1,2-Hexanediol",
            supportsGoals = setOf(SkinGoal.REDNESS, SkinGoal.HYDRATION, SkinGoal.OVERALL_HEALTH),
            skinTypes = setOf(SkinType.NORMAL, SkinType.DRY, SkinType.OILY, SkinType.COMBINATION, SkinType.SENSITIVE),
            attributes = setOf(
                ProductAttribute.FRAGRANCE_FREE,
                ProductAttribute.GENTLE,
                ProductAttribute.VEGAN,
                ProductAttribute.CRUELTY_FREE,
            ),
            routineTimes = BOTH,
            rating = 4.7f,
            reviewCount = 1150,
        ),

        Product(
            id = "prd-lactic-renewal-tonic",
            name = "Lactic 8% Renewal Tonic",
            brand = HALCYON,
            category = ProductCategory.TREATMENT,
            form = ProductForm.BOTTLE,
            tone = ProductTone.IVORY,
            size = "120 ml",
            price = 2250,
            description = "A silky, toner-like treatment with 8% lactic acid and a hydrating base. " +
                "It helps skin look smoother and more radiant while staying kind to drier skin.",
            howToUse = "Press a few drops over clean, dry skin with your palms, two or three evenings a week, then moisturize. " +
                "Do not use on the same night as a retinoid, and wear sunscreen every morning.",
            keyIngredients = listOf(
                Ingredient(name = "Lactic acid 8%", role = "A gentle AHA that helps skin look smoother and brighter"),
                Ingredient(name = "Glycerin", role = "Keeps skin hydrated while it exfoliates"),
                Ingredient(name = "Oat kernel extract", role = "Helps skin feel soothed"),
            ),
            fullIngredients = "Aqua, Lactic Acid, Glycerin, Sodium Lactate, Propanediol, Avena Sativa Kernel Extract, " +
                "Sodium Hyaluronate, Allantoin, Panthenol, Hydroxyethylcellulose, Sodium Hydroxide, Phenoxyethanol",
            supportsGoals = setOf(SkinGoal.TEXTURE, SkinGoal.UNEVEN_TONE),
            skinTypes = setOf(SkinType.NORMAL, SkinType.DRY, SkinType.COMBINATION),
            attributes = setOf(
                ProductAttribute.FRAGRANCE_FREE,
                ProductAttribute.VEGAN,
                ProductAttribute.CRUELTY_FREE,
            ),
            routineTimes = EVENING_ONLY,
            rating = 4.3f,
            reviewCount = 350,
        ),
    )
}
