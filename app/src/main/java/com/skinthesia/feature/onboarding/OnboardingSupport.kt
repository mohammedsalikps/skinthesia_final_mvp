package com.skinthesia.feature.onboarding

import androidx.compose.ui.graphics.vector.ImageVector
import com.skinthesia.core.ui.icons.SkinthesiaIcons
import com.skinthesia.domain.model.Climate
import com.skinthesia.domain.model.DietPattern
import com.skinthesia.domain.model.Environment
import com.skinthesia.domain.model.ExerciseFrequency
import com.skinthesia.domain.model.OnboardingStage
import com.skinthesia.domain.model.SkinGoal
import com.skinthesia.domain.model.SleepPattern

/** Progress indicator positions for the first-run journey. */
object OnboardingSteps {
    const val TOTAL = 8
    const val PROFILE = 1
    const val SELFIE = 2
    const val GOALS = 3
    const val TARGET = 4
    const val QUESTIONNAIRE = 5
    const val LIFESTYLE = 6
    const val PROBE = 7
    const val ANALYSIS = 8
}

/** Moves the saved stage forward only; revisiting an earlier step never rewinds it. */
fun OnboardingStage.atLeast(next: OnboardingStage): OnboardingStage = if (ordinal >= next.ordinal) this else next

val SkinGoal.icon: ImageVector
    get() = when (this) {
        SkinGoal.ACNE -> SkinthesiaIcons.Breakouts
        SkinGoal.DARK_SPOTS -> SkinthesiaIcons.DarkSpots
        SkinGoal.UNEVEN_TONE -> SkinthesiaIcons.EvenTone
        SkinGoal.TEXTURE -> SkinthesiaIcons.Texture
        SkinGoal.PORES -> SkinthesiaIcons.Pores
        SkinGoal.HYDRATION -> SkinthesiaIcons.Droplet
        SkinGoal.REDNESS -> SkinthesiaIcons.Calm
        SkinGoal.DARK_CIRCLES -> SkinthesiaIcons.UnderEye
        SkinGoal.FINE_LINES -> SkinthesiaIcons.FineLines
        SkinGoal.OVERALL_HEALTH -> SkinthesiaIcons.Sparkle
    }

/** Builds calm, editable 12-week statements from the goals a user picked. */
object GoalStatements {
    private const val TAIL = "healthier-looking skin"
    const val DEFAULT = "Clearer, $TAIL"

    private fun SkinGoal.adjective(): String = when (this) {
        SkinGoal.ACNE -> "clearer"
        SkinGoal.DARK_SPOTS, SkinGoal.UNEVEN_TONE -> "more even"
        SkinGoal.TEXTURE, SkinGoal.FINE_LINES -> "smoother"
        SkinGoal.PORES -> "more refined"
        SkinGoal.HYDRATION -> "more hydrated"
        SkinGoal.REDNESS -> "calmer"
        SkinGoal.DARK_CIRCLES -> "brighter"
        SkinGoal.OVERALL_HEALTH -> "healthier"
    }

    fun build(goals: List<SkinGoal>): String {
        val adjectives = goals.filter { it != SkinGoal.OVERALL_HEALTH }.map { it.adjective() }.distinct().take(2)
        return when (adjectives.size) {
            0 -> DEFAULT
            1 -> adjectives[0].replaceFirstChar { it.uppercase() } + ", " + TAIL
            else -> adjectives[0].replaceFirstChar { it.uppercase() } + ", " + adjectives[1] + " and " + TAIL
        }
    }

    fun suggestions(goals: List<SkinGoal>): List<String> =
        (listOf(build(goals)) + listOf(
            DEFAULT,
            "Calm, comfortable, balanced skin",
            "Smoother, more hydrated skin",
            "A brighter, more even tone",
            "Skin I feel good in, every day",
        )).distinct().take(5)
}

val SleepPattern.icon: ImageVector get() = SkinthesiaIcons.Moon
val DietPattern.icon: ImageVector get() = SkinthesiaIcons.Diet
val ExerciseFrequency.icon: ImageVector get() = SkinthesiaIcons.Movement
val Environment.icon: ImageVector get() = SkinthesiaIcons.Work
val Climate.icon: ImageVector get() = SkinthesiaIcons.Climate
