package com.skinthesia.ai.progress

import com.skinthesia.domain.model.Assessment
import com.skinthesia.domain.model.AssessmentKind
import com.skinthesia.domain.model.DataSource
import com.skinthesia.domain.model.JourneyMilestone
import com.skinthesia.domain.model.MeasurementSession
import com.skinthesia.domain.model.MetricKind
import com.skinthesia.domain.model.MilestoneKind
import com.skinthesia.domain.model.ProgressComparison
import com.skinthesia.domain.model.ProgressMetric
import com.skinthesia.domain.model.ProgressSnapshot
import com.skinthesia.domain.model.SensorType
import com.skinthesia.domain.model.SkinPrintDimension
import java.util.Locale
import kotlin.math.roundToInt

/** Turns assessments into snapshots, comparisons and journey milestones. Pure logic. */
class ProgressAnalyzer {

    fun snapshot(assessment: Assessment, session: MeasurementSession?): ProgressSnapshot? {
        val skinPrint = assessment.skinPrint ?: return null
        val sensorAverages = SensorType.entries.mapNotNull { sensor ->
            session?.average(sensor)?.let { sensor to it }
        }.toMap()
        return ProgressSnapshot(
            assessmentId = assessment.id,
            week = assessment.week,
            date = assessment.completedAt ?: assessment.startedAt,
            overall = skinPrint.overall,
            scores = skinPrint.scores.associate { it.dimension to it.value },
            sensorAverages = sensorAverages,
            photoPath = assessment.photo?.filePath,
            sensorsSimulated = session?.isSimulated ?: false,
        )
    }

    fun compare(baseline: ProgressSnapshot, current: ProgressSnapshot): ProgressComparison {
        val sensorSource = if (current.sensorsSimulated || baseline.sensorsSimulated) DataSource.SENSOR_SIMULATED else DataSource.SENSOR_MEASURED
        val metrics = buildList {
            add(
                ProgressMetric(
                    key = KEY_SKINPRINT,
                    label = "SkinPrint",
                    kind = MetricKind.SKINPRINT,
                    from = baseline.overall.toDouble(),
                    to = current.overall.toDouble(),
                    unit = "",
                    decimals = 0,
                    higherIsBetter = true,
                    source = DataSource.DERIVED,
                ),
            )
            SkinPrintDimension.entries.forEach { dimension ->
                val from = baseline.scores[dimension] ?: return@forEach
                val to = current.scores[dimension] ?: return@forEach
                val measured = dimension == SkinPrintDimension.HYDRATION && SensorType.HYDRATION in current.sensorAverages
                add(
                    ProgressMetric(
                        key = dimensionKey(dimension),
                        label = dimension.label,
                        kind = MetricKind.DIMENSION,
                        from = from.toDouble(),
                        to = to.toDouble(),
                        unit = "",
                        decimals = 0,
                        higherIsBetter = true,
                        source = if (measured) sensorSource else if (dimension == SkinPrintDimension.HYDRATION) DataSource.USER_REPORTED else DataSource.VISUAL_ESTIMATE,
                    ),
                )
            }
            SensorType.entries.forEach { sensor ->
                val from = baseline.sensorAverages[sensor] ?: return@forEach
                val to = current.sensorAverages[sensor] ?: return@forEach
                add(
                    ProgressMetric(
                        key = sensorKey(sensor),
                        label = sensor.label,
                        kind = MetricKind.SENSOR,
                        from = from,
                        to = to,
                        unit = sensor.unit,
                        decimals = sensor.decimals,
                        higherIsBetter = if (sensor == SensorType.HYDRATION) true else null,
                        source = sensorSource,
                    ),
                )
            }
        }
        val overallDelta = current.overall - baseline.overall
        val headline = when {
            overallDelta > 0 -> "Your SkinPrint rose $overallDelta point" + (if (overallDelta == 1) "" else "s")
            overallDelta == 0 -> "Your SkinPrint is holding steady"
            else -> "A small dip this time"
        }
        val best = metrics.filter { it.kind == MetricKind.DIMENSION && (it.improved == true) }.maxByOrNull { it.delta }
        val summary = when {
            best != null && overallDelta >= 0 ->
                "Biggest change: ${best.label.lowercase(Locale.ROOT)}, up ${best.delta.roundToInt()} since ${baseline.weekLabel.lowercase(Locale.ROOT)}. Consistency is doing its work."
            overallDelta < 0 ->
                "Small changes are normal from week to week. Light, sleep and season all play a part; your trend matters more than any one check-in."
            else -> "Things are stable. Steady skin is a good base for the next phase of your plan."
        }
        return ProgressComparison(baseline, current, metrics, headline, summary)
    }

    /** Achieved and upcoming milestones across the programme. */
    fun milestones(
        completed: List<Assessment>,
        planVersions: Int,
        bestStreakDays: Int,
        programmeWeeks: Int,
    ): List<JourneyMilestone> {
        val ordered = completed.sortedBy { it.week }
        val baseline = ordered.firstOrNull { it.kind == AssessmentKind.BASELINE } ?: return emptyList()
        val baseScore = baseline.skinPrint?.overall ?: 0
        val baseHydration = baseline.skinPrint?.value(SkinPrintDimension.HYDRATION) ?: 0
        val result = mutableListOf(
            JourneyMilestone("ms-start", 0, MilestoneKind.STARTED, "Your journey began", "Baseline SkinPrint of $baseScore.", baseline.completedAt ?: baseline.startedAt),
        )
        ordered.firstOrNull { it.kind == AssessmentKind.CHECK_IN }?.let { first ->
            result += JourneyMilestone("ms-first-check-in", first.week, MilestoneKind.FIRST_CHECK_IN, "First check-in", "You measured again and compared with day 1.", first.completedAt)
        }
        ordered.firstOrNull { (it.skinPrint?.overall ?: 0) >= baseScore + 5 && it.kind == AssessmentKind.CHECK_IN }?.let { gain ->
            result += JourneyMilestone("ms-gain", gain.week, MilestoneKind.SCORE_GAIN, "+5 SkinPrint", "Your indicator climbed to ${gain.skinPrint?.overall}.", gain.completedAt)
        }
        if (baseHydration < HYDRATION_TARGET) {
            ordered.firstOrNull { (it.skinPrint?.value(SkinPrintDimension.HYDRATION) ?: 0) >= HYDRATION_TARGET }?.let { reached ->
                result += JourneyMilestone("ms-hydration", reached.week, MilestoneKind.TARGET_REACHED, "Hydration target reached", "Hydration reached ${reached.skinPrint?.value(SkinPrintDimension.HYDRATION)}.", reached.completedAt)
            }
        }
        if (bestStreakDays >= 7) {
            result += JourneyMilestone("ms-streak", ordered.last().week, MilestoneKind.STREAK, "7-day routine streak", "A full week of routines, step by step.", ordered.last().completedAt)
        }
        if (planVersions >= 2) {
            val check = ordered.lastOrNull { it.kind == AssessmentKind.CHECK_IN } ?: ordered.last()
            result += JourneyMilestone("ms-plan", check.week, MilestoneKind.PLAN_EVOLVED, "Your plan evolved", "Your routine adapted to your progress.", check.completedAt)
        }
        val done = ordered.map { it.week }.toSet()
        val lastWeek = ordered.last().week
        if (lastWeek >= programmeWeeks) {
            result += JourneyMilestone("ms-complete", programmeWeeks, MilestoneKind.PROGRAMME_COMPLETE, "12 weeks complete", "A full programme of care. Your journey continues from here.", ordered.last().completedAt)
        }
        JourneyClock.MILESTONE_WEEKS.filter { it !in done && it > lastWeek && it <= programmeWeeks }.forEach { week ->
            result += JourneyMilestone("ms-upcoming-$week", week, MilestoneKind.UPCOMING, "Week $week check-in", "New selfie and probe readings, compared with day 1.", null)
        }
        return result.sortedWith(compareBy<JourneyMilestone> { it.week }.thenBy { it.isUpcoming })
    }

    companion object {
        const val KEY_SKINPRINT = "skinprint"
        const val HYDRATION_TARGET = 65
        fun dimensionKey(dimension: SkinPrintDimension) = "dim:" + dimension.name
        fun sensorKey(sensor: SensorType) = "sensor:" + sensor.name
    }
}
