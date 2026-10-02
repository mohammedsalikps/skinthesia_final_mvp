package com.skinthesia.ai.projection

/**
 * One point on the illustrative skin-projection timeline. Never a medical or
 * generative prediction - a presentation-only signal describing how strongly a
 * visual pass should be applied over the user's own real photo at that week.
 */
data class ProjectionFrame(
    val week: Int,
    /** True only for the baseline week - the real captured photo, shown unfiltered. */
    val isBaseline: Boolean,
    /** 0f..1f - how much brighter/warmer the illustrative pass should read. */
    val warmthLift: Float,
    /** 0f..1f - a gentle saturation lift alongside the warmth. */
    val saturationLift: Float,
    /** What the UI should call this frame, e.g. "Today" or "Illustrative projection". */
    val label: String,
)

/**
 * The shared visual-lift curve behind every illustrative projection in the app,
 * as a fraction 0f..1f of the horizon - exposed standalone so a continuously
 * animated UI (e.g. a slider being dragged) can drive it directly, not just a
 * discrete week lookup. [DemoProjectionProvider.frame] is built on top of it.
 */
fun projectionLift(fraction: Float): ProjectionFrame {
    val t = fraction.coerceIn(0f, 1f)
    return ProjectionFrame(week = -1, isBaseline = t <= 0f, warmthLift = t, saturationLift = t * 0.16f, label = "")
}

/**
 * Produces the illustrative skin-projection timeline shown from the user's own
 * baseline photo. [DemoProjectionProvider] is a front-end visual simulation only,
 * built from the same weekly-potential horizon already used elsewhere in the app.
 * A future `AIProjectionProvider` backed by a real generative model can implement
 * this same interface without any UI change.
 */
interface ProjectionProvider {
    val horizonWeeks: Int

    /** The weeks selectable on the timeline, always starting with the baseline (0). */
    val availableWeeks: List<Int>

    fun frame(week: Int): ProjectionFrame
}

class DemoProjectionProvider(override val horizonWeeks: Int = 12) : ProjectionProvider {

    override val availableWeeks: List<Int> =
        listOf(0, 2, 4, 8, 12).filter { it <= horizonWeeks }.ifEmpty { listOf(0) }

    override fun frame(week: Int): ProjectionFrame {
        val clamped = week.coerceIn(0, horizonWeeks)
        val t = clamped.toFloat() / horizonWeeks.coerceAtLeast(1)
        // Week 0 is the baseline sentinel (Assessment.week convention) but still reads as "Week 1" - matches the display rule already used on Home/Journey.
        val displayWeek = if (clamped == 0) 1 else clamped
        return projectionLift(t).copy(
            week = clamped,
            label = if (clamped <= 0) "Today" else "Week $displayWeek · Illustrative projection",
        )
    }
}
