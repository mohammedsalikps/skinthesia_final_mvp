package com.skinthesia.domain.model

import kotlinx.serialization.Serializable

@Serializable
enum class RoutineTime(val label: String) {
    MORNING("Morning"),
    EVENING("Evening"),
}

@Serializable
enum class RoutineStepType(val label: String) {
    CLEANSE("Cleanse"),
    TREAT("Treatment"),
    MOISTURIZE("Moisturize"),
    PROTECT("Protect"),
}

@Serializable
enum class StepFrequency(val label: String) {
    DAILY("Daily"),
    ALTERNATE_NIGHTS("Every other night"),
    TWICE_WEEKLY("2–3 times a week"),
}

@Serializable
data class RoutineStep(
    val id: String,
    val order: Int,
    val type: RoutineStepType,
    val time: RoutineTime,
    val title: String,
    val instruction: String,
    val purpose: String,
    val frequency: StepFrequency,
    val productId: String?,
    /** The user already owns a product for this step; we suggest keeping it. */
    val keepCurrentProduct: Boolean = false,
)

@Serializable
data class Routine(
    val time: RoutineTime,
    val steps: List<RoutineStep>,
)

@Serializable
enum class LifestyleTopic(val label: String) {
    SLEEP("Sleep"),
    STRESS("Stress"),
    SUN("Sun"),
    WATER("Water"),
    NOURISHMENT("Nourishment"),
    MOVEMENT("Movement"),
    ENVIRONMENT("Environment"),
    SCREENS("Screens"),
}

/** Contextual, non-causal suggestion (screen 24, Lifestyle tab). */
@Serializable
data class LifestyleSuggestion(
    val id: String,
    val topic: LifestyleTopic,
    val title: String,
    val body: String,
    /** What prompted the suggestion, in the user's own terms. */
    val basedOn: String,
)

@Serializable
enum class ReasonKind { GOAL, SKIN_TYPE, MEASUREMENT, VISUAL, PREFERENCE, BUDGET, ROUTINE }

@Serializable
data class RecommendationReason(val text: String, val kind: ReasonKind)

/** An explainable product suggestion. Every recommendation carries its reasons. */
@Serializable
data class ProductRecommendation(
    val productId: String,
    val step: RoutineStepType?,
    val times: Set<RoutineTime>,
    /** 0..100 fit against the user's data. */
    val matchScore: Int,
    val reasons: List<RecommendationReason>,
    val isPrimary: Boolean,
)

@Serializable
enum class PlanChangeKind(val label: String) {
    FOCUS_SHIFT("Focus"),
    STEP_ADDED("Added"),
    STEP_REMOVED("Simplified"),
    STEP_ADJUSTED("Adjusted"),
    PRODUCT_SWAPPED("Swapped"),
    KEEP("Keep going"),
}

@Serializable
data class PlanChange(
    val kind: PlanChangeKind,
    val title: String,
    val detail: String,
)

@Serializable
data class PersonalizedPlan(
    val id: String,
    val version: Int,
    val createdAt: Long,
    val basedOnAssessmentId: String,
    val focus: List<SkinGoal>,
    val focusTitle: String,
    val summary: String,
    val morning: Routine,
    val evening: Routine,
    val recommendations: List<ProductRecommendation>,
    val lifestyle: List<LifestyleSuggestion>,
    /** Why this version differs from the previous one (adaptive plan, screen 31). */
    val changes: List<PlanChange> = emptyList(),
    val engineVersion: String,
) {
    fun routine(time: RoutineTime): Routine = if (time == RoutineTime.MORNING) morning else evening
    val allSteps: List<RoutineStep> get() = morning.steps + evening.steps
}

/** A routine step ticked off on a given day (yyyy-MM-dd). */
@Serializable
data class RoutineLog(
    val date: String,
    val stepId: String,
    val completedAt: Long,
)

data class AdherenceSummary(
    val days: Int,
    val completedSteps: Int,
    val scheduledSteps: Int,
    val streakDays: Int,
) {
    val rate: Float get() = if (scheduledSteps == 0) 0f else completedSteps.toFloat() / scheduledSteps
}
