package com.skinthesia.ai.recommendation

import com.skinthesia.domain.model.Assessment
import com.skinthesia.domain.model.AttentionLevel
import com.skinthesia.domain.model.Climate
import com.skinthesia.domain.model.DietPattern
import com.skinthesia.domain.model.Environment
import com.skinthesia.domain.model.ExerciseFrequency
import com.skinthesia.domain.model.Habit
import com.skinthesia.domain.model.Ids
import com.skinthesia.domain.model.Level
import com.skinthesia.domain.model.LifestyleProfile
import com.skinthesia.domain.model.LifestyleSuggestion
import com.skinthesia.domain.model.LifestyleTopic
import com.skinthesia.domain.model.PersonalizedPlan
import com.skinthesia.domain.model.Product
import com.skinthesia.domain.model.ProductAttribute
import com.skinthesia.domain.model.ProductCategory
import com.skinthesia.domain.model.ProductRecommendation
import com.skinthesia.domain.model.ReasonKind
import com.skinthesia.domain.model.RecommendationReason
import com.skinthesia.domain.model.Routine
import com.skinthesia.domain.model.RoutineLevel
import com.skinthesia.domain.model.RoutineStep
import com.skinthesia.domain.model.RoutineStepType
import com.skinthesia.domain.model.RoutineTime
import com.skinthesia.domain.model.Sensitivity
import com.skinthesia.domain.model.SkinGoal
import com.skinthesia.domain.model.SkinPrintDimension
import com.skinthesia.domain.model.SkinType
import com.skinthesia.domain.model.SleepPattern
import com.skinthesia.domain.model.StepFrequency
import com.skinthesia.domain.model.UserProfile
import com.skinthesia.domain.model.VisibilityLevel
import com.skinthesia.domain.model.VisualIndicator
import java.util.Locale
import kotlin.math.roundToInt

/**
 * Deterministic, rule-based recommendation engine for the demonstration. Every
 * product pick carries the reasons it was chosen; routines stay achievable (four
 * morning steps, three evening steps) and respect sensitivities, existing products
 * and budget. A trained ranking model can replace it behind [RecommendationEngine].
 */
class MockRecommendationEngine : RecommendationEngine {

    override val engineName: String = "Skinthesia recommendations (rule-based)"

    override fun buildPlan(input: PlanInput): PersonalizedPlan {
        val focus = input.focusOverride?.takeIf { it.isNotEmpty() } ?: focusGoals(input.profile, input.assessment)
        val ctx = Ctx(input.profile, input.assessment, focus)
        val lighter = input.simplify ||
            Sensitivity.FEWER_STEPS in ctx.profile.skin.preferences ||
            ctx.profile.skin.routineLevel == RoutineLevel.NONE
        val products = input.catalog

        fun best(categories: Set<ProductCategory>, step: RoutineStepType, time: RoutineTime, exclude: Set<String> = emptySet()) =
            products.asSequence()
                .filter { it.category in categories && it.id !in exclude }
                .mapNotNull { p -> score(p, ctx, step, time)?.let { p to it } }
                .maxByOrNull { it.second.matchScore }

        val amCleanser = best(setOf(ProductCategory.CLEANSER), RoutineStepType.CLEANSE, RoutineTime.MORNING)
        val amSerum = best(setOf(ProductCategory.SERUM), RoutineStepType.TREAT, RoutineTime.MORNING)
        val amMoisturizer = best(setOf(ProductCategory.MOISTURIZER), RoutineStepType.MOISTURIZE, RoutineTime.MORNING)
        val sunscreen = best(setOf(ProductCategory.SUNSCREEN), RoutineStepType.PROTECT, RoutineTime.MORNING)
        val pmCleanser = best(setOf(ProductCategory.CLEANSER), RoutineStepType.CLEANSE, RoutineTime.EVENING)
        val pmTreatment = best(
            setOf(ProductCategory.TREATMENT, ProductCategory.SERUM),
            RoutineStepType.TREAT,
            RoutineTime.EVENING,
            exclude = setOfNotNull(amSerum?.first?.id),
        )
        val pmMoisturizer = best(setOf(ProductCategory.MOISTURIZER), RoutineStepType.MOISTURIZE, RoutineTime.EVENING)

        val morning = Routine(
            time = RoutineTime.MORNING,
            steps = listOfNotNull(
                amCleanser?.let { step(RoutineStepType.CLEANSE, RoutineTime.MORNING, 1, it.first, ctx, StepFrequency.DAILY) },
                amSerum?.let { step(RoutineStepType.TREAT, RoutineTime.MORNING, 2, it.first, ctx, StepFrequency.DAILY) },
                amMoisturizer?.let { step(RoutineStepType.MOISTURIZE, RoutineTime.MORNING, 3, it.first, ctx, StepFrequency.DAILY) },
                sunscreen?.let { step(RoutineStepType.PROTECT, RoutineTime.MORNING, 4, it.first, ctx, StepFrequency.DAILY) },
            ),
        )
        val evening = Routine(
            time = RoutineTime.EVENING,
            steps = listOfNotNull(
                pmCleanser?.let { step(RoutineStepType.CLEANSE, RoutineTime.EVENING, 1, it.first, ctx, StepFrequency.DAILY) },
                pmTreatment?.let {
                    val frequency = when {
                        !it.first.isStrongActive() -> if (lighter) StepFrequency.ALTERNATE_NIGHTS else StepFrequency.DAILY
                        ctx.profile.skin.isSensitive || lighter -> StepFrequency.TWICE_WEEKLY
                        else -> StepFrequency.ALTERNATE_NIGHTS
                    }
                    step(RoutineStepType.TREAT, RoutineTime.EVENING, 2, it.first, ctx, frequency)
                },
                pmMoisturizer?.let { step(RoutineStepType.MOISTURIZE, RoutineTime.EVENING, 3, it.first, ctx, StepFrequency.DAILY) },
            ),
        )

        val primaryPicks = listOfNotNull(amCleanser, amSerum, amMoisturizer, sunscreen, pmCleanser, pmTreatment, pmMoisturizer)
        val primaries = primaryPicks
            .groupBy { it.first.id }
            .map { (_, picks) ->
                val rec = picks.maxBy { it.second.matchScore }.second
                rec.copy(times = picks.flatMap { it.second.times }.toSet(), isPrimary = true)
            }
        val alternates = products.asSequence()
            .filter { p -> primaries.none { it.productId == p.id } }
            .mapNotNull { p -> score(p, ctx, step = null, time = null) }
            .filter { it.matchScore >= 55 }
            .sortedByDescending { it.matchScore }
            .take(MAX_ALTERNATES)
            .toList()

        val assessment = input.assessment
        val sources = buildList {
            if (assessment.cameraAnalysis != null) add("photo analysis")
            if (assessment.combined?.sensorSummaries?.isNotEmpty() == true) add("probe readings")
            add("goals")
        }
        val focusTitle = focus.mapIndexed { i, goal -> if (i == 0) goal.label else goal.label.lowercase(Locale.ROOT) }.joinToString(" & ")
        return PersonalizedPlan(
            id = Ids.new("plan"),
            version = (input.previous?.version ?: 0) + 1,
            createdAt = input.now,
            basedOnAssessmentId = assessment.id,
            focus = focus,
            focusTitle = focusTitle,
            summary = "Built around ${focusTitle.lowercase(Locale.ROOT)}, using your SkinPrint, " + sources.joinToString(", ") + ".",
            morning = morning,
            evening = evening,
            recommendations = primaries.sortedByDescending { it.matchScore } + alternates,
            lifestyle = lifestyleSuggestions(ctx.profile.lifestyle, focus),
            changes = emptyList(),
            engineVersion = ENGINE_VERSION,
        )
    }

    override fun rank(profile: UserProfile, assessment: Assessment?, catalog: List<Product>): List<ProductRecommendation> {
        val focus = assessment?.let { focusGoals(profile, it) } ?: profile.goals.ranked.take(2)
        val ctx = Ctx(profile, assessment, focus)
        return catalog.mapNotNull { score(it, ctx, step = null, time = null) }.sortedByDescending { it.matchScore }
    }

    private class Ctx(val profile: UserProfile, val assessment: Assessment?, val focus: List<SkinGoal>) {
        val hydrationScore: Int? = assessment?.skinPrint?.value(SkinPrintDimension.HYDRATION)
        val pigmentation: VisibilityLevel? = assessment?.cameraAnalysis?.estimates
            ?.firstOrNull { it.indicator == VisualIndicator.PIGMENTATION }?.visibility
    }

    private fun score(product: Product, ctx: Ctx, step: RoutineStepType?, time: RoutineTime?): ProductRecommendation? {
        if (time != null && time !in product.routineTimes) return null
        val skin = ctx.profile.skin
        val prefs = skin.preferences
        if (Sensitivity.FRAGRANCE_FREE in prefs && ProductAttribute.FRAGRANCE_FREE !in product.attributes) return null
        if (Sensitivity.VEGAN in prefs && ProductAttribute.VEGAN !in product.attributes) return null
        if (skin.isSensitive && product.isStrongActive() && ProductAttribute.GENTLE !in product.attributes && product.category == ProductCategory.TREATMENT) {
            // Strong actives are still allowed for sensitive skin, but only gentle formulas.
            return null
        }
        if (Sensitivity.NON_COMEDOGENIC in prefs &&
            product.category in setOf(ProductCategory.MOISTURIZER, ProductCategory.SUNSCREEN) &&
            ProductAttribute.NON_COMEDOGENIC !in product.attributes
        ) return null

        var score = 38.0
        val reasons = mutableListOf<RecommendationReason>()

        ctx.focus.forEachIndexed { index, goal ->
            if (goal in product.supportsGoals) {
                score += if (index == 0) 20 else 13
                reasons += RecommendationReason("Supports your ${goal.label.lowercase(Locale.ROOT)} focus", ReasonKind.GOAL)
            }
        }
        val otherGoals = ctx.profile.goals.goals.filter { it !in ctx.focus && it in product.supportsGoals }
        if (otherGoals.isNotEmpty()) {
            score += 5.0 * otherGoals.size
            reasons += RecommendationReason("Also helps with ${otherGoals.first().label.lowercase(Locale.ROOT)}", ReasonKind.GOAL)
        }

        skin.skinType?.let { type ->
            if (type in product.skinTypes) {
                score += 10
                reasons += RecommendationReason("Suits ${type.label.lowercase(Locale.ROOT)} skin", ReasonKind.SKIN_TYPE)
            } else {
                score -= 12
            }
        }

        val hydration = ctx.hydrationScore
        if (hydration != null && hydration < 62 && SkinGoal.HYDRATION in product.supportsGoals) {
            score += 6
            val measured = ctx.assessment?.combined?.sensorSummaries?.isNotEmpty() == true
            reasons += RecommendationReason(
                if (measured) "Your hydration readings are on the lower side" else "Your hydration looks like it could use support",
                ReasonKind.MEASUREMENT,
            )
        }
        val pigmentation = ctx.pigmentation
        if (pigmentation != null && pigmentation >= VisibilityLevel.MODERATE &&
            (SkinGoal.DARK_SPOTS in product.supportsGoals || SkinGoal.UNEVEN_TONE in product.supportsGoals)
        ) {
            score += 5
            reasons += RecommendationReason("Your photo suggests some uneven tone", ReasonKind.VISUAL)
        }

        if (step == RoutineStepType.MOISTURIZE) {
            val wantsRich = time == RoutineTime.EVENING && (skin.skinType == SkinType.DRY || (hydration != null && hydration < 62))
            if (wantsRich && ProductAttribute.RICH in product.attributes) {
                score += 7
                reasons += RecommendationReason("A richer texture for overnight hydration", ReasonKind.ROUTINE)
            }
            if (time == RoutineTime.MORNING && ProductAttribute.LIGHTWEIGHT in product.attributes) score += 5
        }

        when {
            Sensitivity.FRAGRANCE_FREE in prefs -> reasons += RecommendationReason("Fragrance-free, as you prefer", ReasonKind.PREFERENCE)
            skin.isSensitive && ProductAttribute.GENTLE in product.attributes -> {
                score += 6
                reasons += RecommendationReason("A gentle formula for sensitive skin", ReasonKind.PREFERENCE)
            }
        }
        if (Sensitivity.VEGAN in prefs) reasons += RecommendationReason("Vegan formula", ReasonKind.PREFERENCE)

        if (product.price <= skin.budget.maxPerProduct) {
            score += 8
            reasons += RecommendationReason("Within your ${skin.budget.label.lowercase(Locale.ROOT)} budget", ReasonKind.BUDGET)
        } else {
            score -= 14
        }
        if (product.category in skin.currentProducts && step != RoutineStepType.TREAT) {
            reasons += RecommendationReason("An option if you want to replace yours", ReasonKind.ROUTINE)
        }
        score += (product.rating - 4.0) * 6

        return ProductRecommendation(
            productId = product.id,
            step = step,
            times = time?.let { setOf(it) } ?: product.routineTimes,
            matchScore = score.roundToInt().coerceIn(0, 100),
            reasons = reasons.distinctBy { it.text }.take(4),
            isPrimary = false,
        )
    }

    private fun step(
        type: RoutineStepType,
        time: RoutineTime,
        order: Int,
        product: Product,
        ctx: Ctx,
        frequency: StepFrequency,
    ): RoutineStep {
        val keep = product.category in ctx.profile.skin.currentProducts && type != RoutineStepType.TREAT
        return RoutineStep(
            id = (if (time == RoutineTime.MORNING) "am-" else "pm-") + type.name.lowercase(Locale.ROOT),
            order = order,
            type = type,
            time = time,
            title = if (keep) "Your current ${product.category.label.lowercase(Locale.ROOT)}" else product.name,
            instruction = instruction(type, time, product),
            purpose = purpose(type, product, ctx.focus),
            frequency = frequency,
            productId = product.id,
            keepCurrentProduct = keep,
        )
    }

    private fun instruction(type: RoutineStepType, time: RoutineTime, product: Product): String = when (type) {
        RoutineStepType.CLEANSE -> if (time == RoutineTime.MORNING) {
            "Massage onto damp skin for 30 seconds, rinse with lukewarm water and pat dry."
        } else {
            "Massage onto damp skin for a full minute to lift sunscreen and the day, then rinse."
        }
        RoutineStepType.TREAT -> if (product.isStrongActive()) {
            "Apply a pea-sized amount to dry skin. Start slowly and skip a night if skin feels tight."
        } else {
            "Press two or three drops into skin before your moisturizer."
        }
        RoutineStepType.MOISTURIZE -> "Smooth over face and neck while skin is still slightly damp."
        RoutineStepType.PROTECT -> "Apply two finger-lengths as your last step. Reapply every two hours outdoors."
    }

    private fun purpose(type: RoutineStepType, product: Product, focus: List<SkinGoal>): String = when (type) {
        RoutineStepType.CLEANSE -> "Clears skin without stripping moisture."
        RoutineStepType.TREAT -> {
            val goal = focus.firstOrNull { it in product.supportsGoals } ?: product.supportsGoals.firstOrNull()
            val ingredient = product.keyIngredients.firstOrNull()?.name?.substringBefore(" ")?.lowercase(Locale.ROOT)
            buildString {
                append("Supports ")
                append(goal?.label?.lowercase(Locale.ROOT) ?: "your goals")
                if (ingredient != null) append(" with ").append(ingredient)
                append('.')
            }
        }
        RoutineStepType.MOISTURIZE -> "Seals in hydration and supports your skin barrier."
        RoutineStepType.PROTECT -> "Shields from UV, protecting your tone and every other step."
    }

    private fun lifestyleSuggestions(lifestyle: LifestyleProfile, focus: List<SkinGoal>): List<LifestyleSuggestion> = buildList {
        when (lifestyle.sleep) {
            SleepPattern.UNDER_6, SleepPattern.SIX_TO_SEVEN -> add(
                suggestion(
                    LifestyleTopic.SLEEP,
                    "Protect your evening routine",
                    "A short, consistent evening routine is easier to keep on late nights. Two minutes still counts.",
                    "You told us you sleep ${lifestyle.sleep.label}",
                ),
            )
            else -> Unit
        }
        if (lifestyle.stress == Level.HIGH || lifestyle.stress == Level.MODERATE) add(
            suggestion(
                LifestyleTopic.STRESS,
                "Keep it simple on demanding days",
                "On busy weeks, cleanse, moisturize and protect are enough. Consistency matters more than extra steps.",
                "You described your stress as ${lifestyle.stress.label.lowercase(Locale.ROOT)}",
            ),
        )
        if (lifestyle.sunExposure == Level.HIGH || lifestyle.sunExposure == Level.MODERATE || SkinGoal.DARK_SPOTS in focus) add(
            suggestion(
                LifestyleTopic.SUN,
                "Reapply sunscreen outdoors",
                "Every two hours in direct sun, and after sweating or swimming. A hat and shade help too.",
                lifestyle.sunExposure?.let { "You described your sun exposure as ${it.label.lowercase(Locale.ROOT)}" } ?: "Your focus includes tone",
            ),
        )
        if (lifestyle.environment == Environment.INDOOR_AC || lifestyle.climate == Climate.DRY || lifestyle.climate == Climate.COLD) add(
            suggestion(
                LifestyleTopic.ENVIRONMENT,
                "Counter dry air",
                "Air-conditioning and dry weather can leave skin feeling tight. A richer layer at night can feel more comfortable.",
                "You spend time in dry or air-conditioned air",
            ),
        )
        if (Habit.LOW_WATER_INTAKE in lifestyle.habits) add(
            suggestion(
                LifestyleTopic.WATER,
                "Keep water close",
                "Drinking enough water supports your general wellbeing. Skin hydration is also shaped by your routine and environment.",
                "You mentioned low water intake",
            ),
        )
        if (lifestyle.diet == DietPattern.ON_THE_GO) add(
            suggestion(
                LifestyleTopic.NOURISHMENT,
                "Colourful plates, when you can",
                "Fruit, vegetables and healthy fats support overall health. No single food changes skin overnight.",
                "You said meals are often on the go",
            ),
        )
        if (lifestyle.exercise == ExerciseFrequency.OFTEN || lifestyle.exercise == ExerciseFrequency.WEEKLY) add(
            suggestion(
                LifestyleTopic.MOVEMENT,
                "Rinse after workouts",
                "A quick cleanse after exercise clears sweat and sunscreen so skin feels comfortable.",
                "You exercise ${lifestyle.exercise.label.lowercase(Locale.ROOT)}",
            ),
        )
        if (Habit.LONG_SCREEN_HOURS in lifestyle.habits) add(
            suggestion(
                LifestyleTopic.SCREENS,
                "Pause between screens",
                "Short breaks rest tired eyes during long screen days; they're a comfort habit rather than a skin fix.",
                "You mentioned long screen hours",
            ),
        )
        if (isEmpty()) add(
            suggestion(
                LifestyleTopic.SUN,
                "Make sunscreen automatic",
                "Keep it by your toothbrush so it becomes part of every morning.",
                "Every plan includes daily protection",
            ),
        )
    }.take(5)

    private fun suggestion(topic: LifestyleTopic, title: String, body: String, basedOn: String) =
        LifestyleSuggestion(id = "ls-" + topic.name.lowercase(Locale.ROOT), topic = topic, title = title, body = body, basedOn = basedOn)

    companion object {
        const val ENGINE_VERSION = "rules-1.0"
        private const val MAX_ALTERNATES = 5
        private val ACTIVE_MARKERS = listOf("retin", "salicylic", "glycolic", "lactic", "mandelic")

        /** Picks the two goals the plan should lead with, from priorities, goals and analysis. */
        fun focusGoals(profile: UserProfile, assessment: Assessment): List<SkinGoal> {
            val weights = mutableMapOf<SkinGoal, Double>()
            profile.goals.priorities.forEachIndexed { index, goal -> weights.merge(goal, 30.0 - index * 6, Double::plus) }
            profile.goals.goals.forEach { weights.merge(it, 10.0, Double::plus) }
            assessment.combined?.focusAreas?.forEach { area ->
                val weight = when (area.level) {
                    AttentionLevel.NEEDS_ATTENTION -> 24.0
                    AttentionLevel.MODERATE -> 16.0
                    AttentionLevel.MILD -> 8.0
                    AttentionLevel.GOOD -> 0.0
                }
                weights.merge(area.area.goal, weight, Double::plus)
            }
            return weights.filterKeys { it != SkinGoal.OVERALL_HEALTH }
                .entries.sortedByDescending { it.value }
                .map { it.key }
                .take(2)
                .ifEmpty { listOf(SkinGoal.HYDRATION) }
        }

        fun Product.isStrongActive(): Boolean =
            keyIngredients.any { ingredient -> ACTIVE_MARKERS.any { ingredient.name.contains(it, ignoreCase = true) } }
    }
}
