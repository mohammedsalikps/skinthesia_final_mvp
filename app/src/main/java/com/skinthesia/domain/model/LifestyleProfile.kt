package com.skinthesia.domain.model

import kotlinx.serialization.Serializable

@Serializable
enum class SleepPattern(val label: String) {
    UNDER_6("Under 6 h"),
    SIX_TO_SEVEN("6–7 h"),
    SEVEN_TO_EIGHT("7–8 h"),
    OVER_8("8 h +"),
}

@Serializable
enum class DietPattern(val label: String) {
    BALANCED("Mostly balanced"),
    MIXED("A bit of everything"),
    ON_THE_GO("Often on the go"),
}

@Serializable
enum class ExerciseFrequency(val label: String) {
    RARELY("Rarely"),
    WEEKLY("1–2× a week"),
    OFTEN("3+ times a week"),
}

@Serializable
enum class Environment(val label: String) {
    INDOOR_AC("Air-conditioned indoors"),
    CITY_COMMUTE("City commute"),
    OUTDOORS("Mostly outdoors"),
    MIXED("A mix"),
}

@Serializable
enum class Climate(val label: String) {
    HUMID("Humid"),
    DRY("Dry"),
    TEMPERATE("Temperate"),
    COLD("Cold"),
}

@Serializable
enum class Habit(val label: String) {
    LONG_SCREEN_HOURS("Long screen hours"),
    LOW_WATER_INTAKE("Low water intake"),
    DAILY_MAKEUP("Daily makeup"),
    FREQUENT_TRAVEL("Frequent travel"),
    TOUCHING_FACE("Touching my face often"),
    SMOKING("Smoking"),
}

/**
 * Lifestyle context (screen 09). This is context for personalisation only; the
 * product never claims any of these causes a skin condition.
 */
@Serializable
data class LifestyleProfile(
    val sleep: SleepPattern? = null,
    val stress: Level? = null,
    val sunExposure: Level? = null,
    val diet: DietPattern? = null,
    val exercise: ExerciseFrequency? = null,
    val environment: Environment? = null,
    val climate: Climate? = null,
    val habits: Set<Habit> = emptySet(),
) {
    val answeredCount: Int
        get() = listOf(sleep, stress, sunExposure, diet, exercise, environment, climate).count { it != null } +
            if (habits.isNotEmpty()) 1 else 0
}
