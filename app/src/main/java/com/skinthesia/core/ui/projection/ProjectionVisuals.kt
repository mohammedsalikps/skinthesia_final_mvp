package com.skinthesia.core.ui.projection

import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import com.skinthesia.ai.projection.ProjectionFrame

/**
 * Turns a [ProjectionFrame]'s plain visual signal into an actual Compose colour
 * transform - a presentation-only pass over the user's own real photo, never a
 * generated image. Kept out of the platform-agnostic `ai.projection` package
 * since [ColorFilter] is a Compose UI type.
 */
fun ProjectionFrame.toColorFilter(): ColorFilter {
    val matrix = ColorMatrix().apply { setToSaturation(1f + saturationLift) }
    matrix.values[4] += 8f * warmthLift
    matrix.values[9] += 6f * warmthLift
    matrix.values[14] += 3f * warmthLift
    return ColorFilter.colorMatrix(matrix)
}
