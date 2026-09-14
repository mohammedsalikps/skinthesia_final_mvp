package com.skinthesia.domain.model

import kotlinx.serialization.Serializable

/** Where the first-run journey currently stands; used to resume after the app is closed. */
@Serializable
enum class OnboardingStage {
    WELCOME,
    PROFILE,
    SELFIE,
    GOALS,
    GOAL_STATEMENT,
    QUESTIONNAIRE,
    LIFESTYLE,
    PROBE,
    ANALYSIS,
    COMPLETE,
}

@Serializable
enum class AgeRange(val label: String) {
    UNDER_18("Under 18"),
    AGE_18_24("18–24"),
    AGE_25_34("25–34"),
    AGE_35_44("35–44"),
    AGE_45_54("45–54"),
    AGE_55_PLUS("55+"),
}

/**
 * The single local user. Identity fields are ready for a future cloud account;
 * today everything stays on the device.
 */
@Serializable
data class UserProfile(
    val id: String = "",
    val firstName: String = "",
    val ageRange: AgeRange? = null,
    val createdAt: Long = 0L,
    val onboardingStage: OnboardingStage = OnboardingStage.WELCOME,
    val skin: SkinProfile = SkinProfile(),
    val goals: GoalPlan = GoalPlan(),
    val lifestyle: LifestyleProfile = LifestyleProfile(),
    /** The first complete assessment, the baseline every comparison starts from. */
    val baselineAssessmentId: String? = null,
    /** An assessment flow (baseline or check-in) that has been started but not finished. */
    val activeAssessmentId: String? = null,
    /** The probe paired most recently, reconnected automatically during check-ins. */
    val pairedDeviceId: String? = null,
) {
    val isOnboarded: Boolean get() = onboardingStage == OnboardingStage.COMPLETE
    val greetingName: String get() = firstName.trim().ifBlank { "there" }
}
