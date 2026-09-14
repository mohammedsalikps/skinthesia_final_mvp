package com.skinthesia.core.design

import androidx.compose.foundation.LocalIndication
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.remember

/**
 * Root theme. Wraps Material 3 so any Material component inherits the palette,
 * while exposing the richer Skinthesia tokens through [SkinthesiaTheme].
 */
@Composable
fun SkinthesiaTheme(
    content: @Composable () -> Unit,
) {
    val colors = SkinthesiaLightColors
    val typography = remember { SkinthesiaTypography() }
    val shapes = remember { SkinthesiaShapes() }
    val spacing = remember { SkinthesiaSpacing() }
    val reducedMotion = rememberReducedMotionPreference()
    val motion = remember(reducedMotion) { SkinthesiaMotion(reducedMotion = reducedMotion) }
    val materialColors = remember(colors) { colors.toMaterialColorScheme() }
    val materialTypography = remember(typography) { typography.toMaterialTypography() }
    val materialShapes = remember(shapes) { shapes.toMaterialShapes() }

    CompositionLocalProvider(
        LocalSkinthesiaColors provides colors,
        LocalSkinthesiaTypography provides typography,
        LocalSkinthesiaShapes provides shapes,
        LocalSkinthesiaSpacing provides spacing,
        LocalSkinthesiaMotion provides motion,
    ) {
        MaterialTheme(
            colorScheme = materialColors,
            typography = materialTypography,
            shapes = materialShapes,
        ) {
            CompositionLocalProvider(
                LocalContentColor provides colors.textPrimary,
                LocalTextStyle provides typography.body,
                LocalIndication provides ripple(color = colors.primary),
                content = content,
            )
        }
    }
}

/** Convenient accessors: `SkinthesiaTheme.colors.primary`, `SkinthesiaTheme.spacing.lg`, ... */
object SkinthesiaTheme {
    val colors: SkinthesiaColorScheme
        @Composable @ReadOnlyComposable get() = LocalSkinthesiaColors.current

    val typography: SkinthesiaTypography
        @Composable @ReadOnlyComposable get() = LocalSkinthesiaTypography.current

    val shapes: SkinthesiaShapes
        @Composable @ReadOnlyComposable get() = LocalSkinthesiaShapes.current

    val spacing: SkinthesiaSpacing
        @Composable @ReadOnlyComposable get() = LocalSkinthesiaSpacing.current

    val motion: SkinthesiaMotion
        @Composable @ReadOnlyComposable get() = LocalSkinthesiaMotion.current
}
