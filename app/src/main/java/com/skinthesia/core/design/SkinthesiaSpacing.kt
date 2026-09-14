package com.skinthesia.core.design

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/** Spacing and sizing tokens. Keeps rhythm consistent across every screen. */
@Immutable
data class SkinthesiaSpacing(
    val xxs: Dp = 4.dp,
    val xs: Dp = 8.dp,
    val sm: Dp = 12.dp,
    val md: Dp = 16.dp,
    val lg: Dp = 24.dp,
    val xl: Dp = 32.dp,
    val xxl: Dp = 48.dp,
    val xxxl: Dp = 64.dp,
    /** Horizontal page margin used by every screen. */
    val screenHorizontal: Dp = 24.dp,
    /** Gap between major sections. */
    val section: Dp = 28.dp,
    /** Gap between grid tiles and list rows. */
    val gridGap: Dp = 10.dp,
    /** Minimum interactive target. */
    val touchTarget: Dp = 48.dp,
    val buttonHeight: Dp = 54.dp,
    val buttonHeightSmall: Dp = 44.dp,
    val iconSmall: Dp = 16.dp,
    val icon: Dp = 22.dp,
    val iconLarge: Dp = 28.dp,
    val borderThin: Dp = 1.dp,
    val borderSelected: Dp = 1.5.dp,
)

val LocalSkinthesiaSpacing = staticCompositionLocalOf { SkinthesiaSpacing() }
