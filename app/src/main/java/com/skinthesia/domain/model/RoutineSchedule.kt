package com.skinthesia.domain.model

import java.time.DayOfWeek
import java.time.LocalDate

/** Whether a step is scheduled on [date], given its frequency. */
fun RoutineStep.isDueOn(date: LocalDate): Boolean = when (frequency) {
    StepFrequency.DAILY -> true
    StepFrequency.ALTERNATE_NIGHTS -> date.toEpochDay() % 2 == 0L
    StepFrequency.TWICE_WEEKLY -> date.dayOfWeek in TREATMENT_DAYS
}

fun PersonalizedPlan.stepsDueOn(date: LocalDate, time: RoutineTime): List<RoutineStep> =
    routine(time).steps.filter { it.isDueOn(date) }

private val TREATMENT_DAYS = setOf(DayOfWeek.MONDAY, DayOfWeek.THURSDAY, DayOfWeek.SATURDAY)

/**
 * Adherence over [from]..[to] (inclusive), counting only days since the plan began.
 * The streak counts back from [to], or from the day before when today has no steps
 * ticked yet, so an unfinished morning never breaks a streak.
 */
fun adherence(plan: PersonalizedPlan, logs: List<RoutineLog>, from: LocalDate, to: LocalDate, planStart: LocalDate): AdherenceSummary {
    val start = maxOf(from, planStart)
    if (start.isAfter(to)) return AdherenceSummary(days = 0, completedSteps = 0, scheduledSteps = 0, streakDays = 0)
    val done = logs.groupBy { it.date }.mapValues { (_, list) -> list.map { it.stepId }.toSet() }
    var scheduled = 0
    var completed = 0
    var day = start
    while (!day.isAfter(to)) {
        val due = plan.allSteps.filter { it.isDueOn(day) }.map { it.id }
        scheduled += due.size
        completed += due.count { it in done[day.toString()].orEmpty() }
        day = day.plusDays(1)
    }
    var streak = 0
    var cursor = if (done[to.toString()].isNullOrEmpty()) to.minusDays(1) else to
    while (!cursor.isBefore(start)) {
        val due = plan.allSteps.filter { it.isDueOn(cursor) }.map { it.id }
        if (due.isEmpty() || !done[cursor.toString()].orEmpty().containsAll(due)) break
        streak++
        cursor = cursor.minusDays(1)
    }
    val days = (to.toEpochDay() - start.toEpochDay() + 1).toInt()
    return AdherenceSummary(days = days, completedSteps = completed, scheduledSteps = scheduled, streakDays = streak)
}
