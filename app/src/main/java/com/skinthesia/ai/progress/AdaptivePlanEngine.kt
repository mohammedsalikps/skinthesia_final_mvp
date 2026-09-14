package com.skinthesia.ai.progress

import com.skinthesia.ai.recommendation.MockRecommendationEngine
import com.skinthesia.ai.recommendation.PlanInput
import com.skinthesia.ai.recommendation.RecommendationEngine
import com.skinthesia.ai.skinprint.dimension
import com.skinthesia.domain.model.AdherenceSummary
import com.skinthesia.domain.model.Assessment
import com.skinthesia.domain.model.PersonalizedPlan
import com.skinthesia.domain.model.PlanChange
import com.skinthesia.domain.model.PlanChangeKind
import com.skinthesia.domain.model.Product
import com.skinthesia.domain.model.ProgressComparison
import com.skinthesia.domain.model.SkinGoal
import com.skinthesia.domain.model.SkinPrintDimension
import com.skinthesia.domain.model.UserProfile
import java.util.Locale
import kotlin.math.roundToInt

data class AdaptationInput(
    val profile: UserProfile,
    val latest: Assessment,
    val comparison: ProgressComparison?,
    val previous: PersonalizedPlan,
    val adherence: AdherenceSummary,
    val catalog: List<Product>,
    val now: Long,
)

/**
 * Evolves the plan after a check-in (screen 31) with predefined, explainable rules:
 * goals that clearly improved hand the focus to the next priority, dimensions that
 * slipped get attention, and low adherence lightens the routine. Every change comes
 * with its reason. Plan construction itself stays in the [RecommendationEngine].
 */
class AdaptivePlanEngine(private val engine: RecommendationEngine) {

    fun adapt(input: AdaptationInput): PersonalizedPlan {
        val metrics = input.comparison?.metrics.orEmpty().associateBy { it.key }
        fun metric(dimension: SkinPrintDimension) = metrics[ProgressAnalyzer.dimensionKey(dimension)]

        val previousFocus = input.previous.focus
        val graduated = previousFocus.filter { goal ->
            val m = goal.dimension?.let(::metric)
            m != null && m.delta >= GRADUATION_GAIN && m.to >= GRADUATION_LEVEL
        }
        val slipped = SkinPrintDimension.entries.filter { dimension ->
            val m = metric(dimension)
            m != null && m.delta <= -SLIP
        }

        val fresh = MockRecommendationEngine.focusGoals(input.profile, input.latest)
        val focus = previousFocus.filter { it !in graduated }.toMutableList()
        slipped.forEach { dimension ->
            input.profile.goals.goals.firstOrNull { it.dimension == dimension && it !in focus }?.let { focus.add(0, it) }
        }
        (fresh + input.profile.goals.ranked).filter { it !in focus && it !in graduated && it != SkinGoal.OVERALL_HEALTH }
            .forEach { if (focus.size < 2) focus += it }
        val nextFocus = focus.take(2).ifEmpty { fresh }

        // Only judge adherence once there are a few days of routine data to judge.
        val simplify = input.adherence.days >= MIN_ADHERENCE_DAYS &&
            input.adherence.scheduledSteps >= 6 &&
            input.adherence.rate < 0.5f
        val plan = engine.buildPlan(
            PlanInput(
                profile = input.profile,
                assessment = input.latest,
                catalog = input.catalog,
                previous = input.previous,
                now = input.now,
                focusOverride = nextFocus,
                simplify = simplify,
            ),
        )

        val changes = mutableListOf<PlanChange>()
        graduated.forEach { goal ->
            val m = goal.dimension?.let(::metric) ?: return@forEach
            changes += PlanChange(
                kind = PlanChangeKind.FOCUS_SHIFT,
                title = "${goal.label} is responding well",
                detail = "${m.label} rose from ${m.from.roundToInt()} to ${m.to.roundToInt()}, so your plan now leans toward ${plan.focusTitle.lowercase(Locale.ROOT)}.",
            )
        }
        if (graduated.isEmpty() && nextFocus != previousFocus) {
            changes += PlanChange(
                kind = PlanChangeKind.FOCUS_SHIFT,
                title = "New focus: ${plan.focusTitle}",
                detail = "Your latest check-in points here, so the plan leads with it for the next few weeks.",
            )
        }
        slipped.forEach { dimension ->
            val m = metric(dimension) ?: return@forEach
            changes += PlanChange(
                kind = PlanChangeKind.STEP_ADJUSTED,
                title = "${dimension.label} dipped slightly",
                detail = "It moved from ${m.from.roundToInt()} to ${m.to.roundToInt()}. Small dips are normal; we've given it a little more attention.",
            )
        }
        if (simplify) {
            changes += PlanChange(
                kind = PlanChangeKind.STEP_REMOVED,
                title = "A lighter routine",
                detail = "You completed ${(input.adherence.rate * 100).roundToInt()}% of steps recently, so your evening treatment is now easier to keep up.",
            )
        }
        val names = input.catalog.associate { it.id to it.name }
        val before = input.previous.allSteps.associateBy { it.id }
        plan.allSteps.forEach { step ->
            val old = before[step.id]
            when {
                old == null -> changes += PlanChange(PlanChangeKind.STEP_ADDED, "Added: ${step.type.label.lowercase(Locale.ROOT)}", "${step.title} joins your ${step.time.label.lowercase(Locale.ROOT)} routine.")
                old.productId != step.productId && !step.keepCurrentProduct -> changes += PlanChange(
                    kind = PlanChangeKind.PRODUCT_SWAPPED,
                    title = "New ${step.time.label.lowercase(Locale.ROOT)} ${step.type.label.lowercase(Locale.ROOT)}",
                    detail = "${names[step.productId] ?: step.title} replaces ${names[old.productId] ?: old.title} to support ${plan.focusTitle.lowercase(Locale.ROOT)}.",
                )
                old.frequency != step.frequency -> changes += PlanChange(
                    kind = PlanChangeKind.STEP_ADJUSTED,
                    title = "Adjusted: ${step.title}",
                    detail = "Now ${step.frequency.label.lowercase(Locale.ROOT)}.",
                )
            }
        }
        if (changes.isEmpty()) {
            changes += PlanChange(
                kind = PlanChangeKind.KEEP,
                title = "Your plan stays the same",
                detail = "Your latest check-in shows steady progress, so there's nothing to change. Keep going.",
            )
        }
        return plan.copy(changes = changes.distinctBy { it.title })
    }

    private companion object {
        const val GRADUATION_GAIN = 6.0
        const val GRADUATION_LEVEL = 70.0
        const val SLIP = 5.0
        const val MIN_ADHERENCE_DAYS = 3
    }
}
