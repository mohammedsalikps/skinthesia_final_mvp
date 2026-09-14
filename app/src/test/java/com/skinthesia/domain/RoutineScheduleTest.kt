package com.skinthesia.domain

import com.skinthesia.domain.model.PersonalizedPlan
import com.skinthesia.domain.model.Routine
import com.skinthesia.domain.model.RoutineLog
import com.skinthesia.domain.model.RoutineStep
import com.skinthesia.domain.model.RoutineStepType
import com.skinthesia.domain.model.RoutineTime
import com.skinthesia.domain.model.StepFrequency
import com.skinthesia.domain.model.adherence
import com.skinthesia.domain.model.isDueOn
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class RoutineScheduleTest {

    private fun step(id: String, frequency: StepFrequency, time: RoutineTime = RoutineTime.MORNING) =
        RoutineStep(id, 1, RoutineStepType.CLEANSE, time, id, "", "", frequency, "p")

    private val plan = PersonalizedPlan(
        id = "plan",
        version = 1,
        createdAt = 0L,
        basedOnAssessmentId = "as",
        focus = emptyList(),
        focusTitle = "",
        summary = "",
        morning = Routine(RoutineTime.MORNING, listOf(step("am-cleanse", StepFrequency.DAILY))),
        evening = Routine(RoutineTime.EVENING, listOf(step("pm-cleanse", StepFrequency.DAILY, RoutineTime.EVENING))),
        recommendations = emptyList(),
        lifestyle = emptyList(),
        engineVersion = "test",
    )

    @Test
    fun `frequencies schedule steps on the right days`() {
        val monday = LocalDate.of(2026, 9, 14)
        assertTrue(step("a", StepFrequency.DAILY).isDueOn(monday))
        assertTrue(step("b", StepFrequency.TWICE_WEEKLY).isDueOn(monday))
        assertFalse(step("b", StepFrequency.TWICE_WEEKLY).isDueOn(monday.plusDays(1)))
        val alternate = step("c", StepFrequency.ALTERNATE_NIGHTS)
        assertTrue(alternate.isDueOn(monday) != alternate.isDueOn(monday.plusDays(1)))
    }

    @Test
    fun `adherence counts completed steps and the current streak`() {
        val start = LocalDate.of(2026, 9, 10)
        val today = start.plusDays(3)
        val logs = (0..2).flatMap { offset ->
            val date = start.plusDays(offset.toLong()).toString()
            listOf(RoutineLog(date, "am-cleanse", 0L), RoutineLog(date, "pm-cleanse", 0L))
        }
        val summary = adherence(plan, logs, from = start, to = today, planStart = start)
        assertEquals(4, summary.days)
        assertEquals(8, summary.scheduledSteps)
        assertEquals(6, summary.completedSteps)
        // Nothing ticked yet today, so the streak counts the three full days before it.
        assertEquals(3, summary.streakDays)
    }
}
