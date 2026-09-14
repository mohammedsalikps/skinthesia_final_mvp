package com.skinthesia.testing

import com.skinthesia.domain.model.Assessment
import com.skinthesia.domain.model.AssessmentKind
import com.skinthesia.domain.model.AssessmentStatus
import com.skinthesia.domain.model.Budget
import com.skinthesia.domain.model.CameraAnalysisResult
import com.skinthesia.domain.model.CombinedAnalysis
import com.skinthesia.domain.model.DataSource
import com.skinthesia.domain.model.GoalPlan
import com.skinthesia.domain.model.IndicatorEstimate
import com.skinthesia.domain.model.Ingredient
import com.skinthesia.domain.model.Level
import com.skinthesia.domain.model.LifestyleProfile
import com.skinthesia.domain.model.MeasurementRegion
import com.skinthesia.domain.model.MeasurementSession
import com.skinthesia.domain.model.OnboardingStage
import com.skinthesia.domain.model.PhotoSource
import com.skinthesia.domain.model.Product
import com.skinthesia.domain.model.ProductAttribute
import com.skinthesia.domain.model.ProductCategory
import com.skinthesia.domain.model.ProductForm
import com.skinthesia.domain.model.ProductTone
import com.skinthesia.domain.model.RoutineTime
import com.skinthesia.domain.model.ScoreBand
import com.skinthesia.domain.model.SensorMeasurement
import com.skinthesia.domain.model.SensorType
import com.skinthesia.domain.model.Sensitivity
import com.skinthesia.domain.model.SkinConcern
import com.skinthesia.domain.model.SkinGoal
import com.skinthesia.domain.model.SkinPhoto
import com.skinthesia.domain.model.SkinPrint
import com.skinthesia.domain.model.SkinPrintDimension
import com.skinthesia.domain.model.SkinPrintInputs
import com.skinthesia.domain.model.SkinProfile
import com.skinthesia.domain.model.SkinRegion
import com.skinthesia.domain.model.SkinScore
import com.skinthesia.domain.model.SkinType
import com.skinthesia.domain.model.SleepPattern
import com.skinthesia.domain.model.UserProfile
import com.skinthesia.domain.model.VisualIndicator

object Fixtures {
    const val NOW = 1_780_000_000_000L

    val DEFAULT_LEVELS = mapOf(
        VisualIndicator.ACNE_APPEARANCE to 40,
        VisualIndicator.REDNESS to 30,
        VisualIndicator.PIGMENTATION to 50,
        VisualIndicator.DARK_CIRCLES to 40,
        VisualIndicator.TEXTURE to 45,
        VisualIndicator.FINE_LINES to 20,
        VisualIndicator.PORES to 40,
    )

    fun profile(
        goals: List<SkinGoal> = listOf(SkinGoal.HYDRATION, SkinGoal.DARK_SPOTS),
        priorities: List<SkinGoal> = listOf(SkinGoal.HYDRATION),
        concerns: Set<SkinConcern> = setOf(SkinConcern.DRYNESS, SkinConcern.DARK_SPOTS),
        skinType: SkinType = SkinType.COMBINATION,
        preferences: Set<Sensitivity> = emptySet(),
        budget: Budget = Budget.MID_RANGE,
        currentProducts: Set<ProductCategory> = emptySet(),
        lifestyle: LifestyleProfile = LifestyleProfile(
            sleep = SleepPattern.SIX_TO_SEVEN,
            stress = Level.MODERATE,
            sunExposure = Level.HIGH,
        ),
    ) = UserProfile(
        id = "user-test",
        firstName = "Asha",
        createdAt = NOW,
        onboardingStage = OnboardingStage.COMPLETE,
        skin = SkinProfile(
            skinType = skinType,
            concerns = concerns,
            currentProducts = currentProducts,
            preferences = preferences,
            budget = budget,
        ),
        goals = GoalPlan(goals = goals, priorities = priorities, statement = "Clearer, healthier-looking skin"),
        lifestyle = lifestyle,
    )

    fun photo(id: String = "photo-1") = SkinPhoto(id = id, filePath = "/private/$id.jpg", capturedAt = NOW, source = PhotoSource.CAMERA)

    fun camera(levels: Map<VisualIndicator, Int> = DEFAULT_LEVELS, simulated: Boolean = true) = CameraAnalysisResult(
        id = "cam-1",
        photoId = "photo-1",
        estimates = levels.map { (indicator, level) ->
            IndicatorEstimate(indicator, level, 0.75f, listOf(SkinRegion.LEFT_CHEEK, SkinRegion.RIGHT_CHEEK))
        },
        analyzedAt = NOW,
        modelVersion = "test",
        isSimulated = simulated,
    )

    fun session(
        forehead: Double = 62.0,
        leftCheek: Double = 51.0,
        rightCheek: Double = 52.0,
        ph: Double = 5.4,
        simulated: Boolean = true,
    ): MeasurementSession {
        val source = if (simulated) DataSource.SENSOR_SIMULATED else DataSource.SENSOR_MEASURED
        val hydration = mapOf(
            MeasurementRegion.FOREHEAD to forehead,
            MeasurementRegion.LEFT_CHEEK to leftCheek,
            MeasurementRegion.RIGHT_CHEEK to rightCheek,
        )
        val readings = hydration.flatMap { (region, value) ->
            listOf(
                SensorMeasurement("r-${region.name}-h", "user-test", "ses-1", region, SensorType.HYDRATION, value, NOW, source),
                SensorMeasurement("r-${region.name}-p", "user-test", "ses-1", region, SensorType.PH, ph, NOW, source),
                SensorMeasurement("r-${region.name}-t", "user-test", "ses-1", region, SensorType.TEMPERATURE, 32.4, NOW, source),
            )
        }
        return MeasurementSession(
            id = "ses-1",
            number = 1,
            userId = "user-test",
            assessmentId = "as-1",
            deviceId = "SIM-01A3",
            deviceName = "Skinthesia Probe",
            isSimulated = simulated,
            startedAt = NOW,
            completedAt = NOW,
            readings = readings,
        )
    }

    fun assessment(
        id: String = "as-1",
        week: Int = 0,
        kind: AssessmentKind = AssessmentKind.BASELINE,
        camera: CameraAnalysisResult? = camera(),
        sessionId: String? = "ses-1",
        combined: CombinedAnalysis? = null,
    ) = Assessment(
        id = id,
        kind = kind,
        week = week,
        startedAt = NOW + week * WEEK,
        completedAt = NOW + week * WEEK,
        status = AssessmentStatus.COMPLETE,
        photo = photo("photo-$id"),
        measurementSessionId = sessionId,
        cameraAnalysis = camera,
        combined = combined,
    )

    /** A completed assessment carrying a hand-built SkinPrint, for progress tests. */
    fun scoredAssessment(id: String, week: Int, overall: Int, hydration: Int, kind: AssessmentKind = AssessmentKind.CHECK_IN): Assessment {
        val scores = SkinPrintDimension.entries.map { dimension ->
            SkinScore(dimension, if (dimension == SkinPrintDimension.HYDRATION) hydration else overall, setOf(DataSource.DERIVED), 0.8f, "")
        }
        val skinPrint = SkinPrint(
            id = "sp-$id",
            assessmentId = id,
            overall = overall,
            scores = scores,
            band = ScoreBand.fromScore(overall),
            week = week,
            createdAt = NOW,
            inputs = SkinPrintInputs(true, true, true, true, true),
            algorithmVersion = "test",
        )
        val combined = CombinedAnalysis(
            assessmentId = id,
            skinPrint = skinPrint,
            focusAreas = emptyList(),
            strengths = emptyList(),
            insights = emptyList(),
            sensorSummaries = emptyList(),
            contextNotes = emptyList(),
            createdAt = NOW,
            engineVersion = "test",
            usedSimulatedData = true,
        )
        return assessment(id = id, week = week, kind = kind, combined = combined)
    }

    fun product(
        id: String,
        category: ProductCategory,
        goals: Set<SkinGoal>,
        price: Int = 1200,
        attributes: Set<ProductAttribute> = setOf(ProductAttribute.FRAGRANCE_FREE),
        times: Set<RoutineTime> = setOf(RoutineTime.MORNING, RoutineTime.EVENING),
        ingredients: List<String> = listOf("Glycerin"),
        skinTypes: Set<SkinType> = SkinType.entries.toSet(),
    ) = Product(
        id = id,
        name = id.replace('-', ' '),
        brand = "Test",
        category = category,
        form = ProductForm.PUMP,
        tone = ProductTone.IVORY,
        size = "50 ml",
        price = price,
        description = "",
        howToUse = "",
        keyIngredients = ingredients.map { Ingredient(it, "role") },
        fullIngredients = ingredients.joinToString(),
        supportsGoals = goals,
        skinTypes = skinTypes,
        attributes = attributes,
        routineTimes = times,
        rating = 4.5f,
        reviewCount = 100,
    )

    val catalog: List<Product> = listOf(
        product("cleanser-gentle", ProductCategory.CLEANSER, setOf(SkinGoal.HYDRATION, SkinGoal.OVERALL_HEALTH), 650, setOf(ProductAttribute.FRAGRANCE_FREE, ProductAttribute.GENTLE)),
        product("cleanser-foam", ProductCategory.CLEANSER, setOf(SkinGoal.ACNE, SkinGoal.PORES), 700, emptySet()),
        product("serum-hydra", ProductCategory.SERUM, setOf(SkinGoal.HYDRATION, SkinGoal.TEXTURE), 1100),
        product("serum-vitc", ProductCategory.SERUM, setOf(SkinGoal.DARK_SPOTS, SkinGoal.UNEVEN_TONE), 1500, times = setOf(RoutineTime.MORNING)),
        product("serum-niacin", ProductCategory.SERUM, setOf(SkinGoal.PORES, SkinGoal.ACNE, SkinGoal.UNEVEN_TONE), 900),
        product("treatment-retinal", ProductCategory.TREATMENT, setOf(SkinGoal.FINE_LINES, SkinGoal.TEXTURE), 1900, times = setOf(RoutineTime.EVENING), ingredients = listOf("Retinal 0.05%")),
        product("treatment-tone", ProductCategory.TREATMENT, setOf(SkinGoal.DARK_SPOTS, SkinGoal.UNEVEN_TONE, SkinGoal.REDNESS), 1300, setOf(ProductAttribute.FRAGRANCE_FREE, ProductAttribute.GENTLE), setOf(RoutineTime.EVENING), listOf("Azelaic acid 10%")),
        product("moist-gel", ProductCategory.MOISTURIZER, setOf(SkinGoal.HYDRATION), 950, setOf(ProductAttribute.FRAGRANCE_FREE, ProductAttribute.LIGHTWEIGHT)),
        product("moist-rich", ProductCategory.MOISTURIZER, setOf(SkinGoal.HYDRATION, SkinGoal.TEXTURE), 1400, setOf(ProductAttribute.FRAGRANCE_FREE, ProductAttribute.RICH)),
        product("spf-fluid", ProductCategory.SUNSCREEN, setOf(SkinGoal.DARK_SPOTS, SkinGoal.UNEVEN_TONE, SkinGoal.OVERALL_HEALTH), 850, setOf(ProductAttribute.FRAGRANCE_FREE, ProductAttribute.BROAD_SPECTRUM), setOf(RoutineTime.MORNING)),
        product("spf-scented", ProductCategory.SUNSCREEN, setOf(SkinGoal.DARK_SPOTS), 600, setOf(ProductAttribute.BROAD_SPECTRUM), setOf(RoutineTime.MORNING)),
    )

    private const val WEEK = 7L * 24 * 60 * 60 * 1000
}
