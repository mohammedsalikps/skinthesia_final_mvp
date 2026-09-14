package com.skinthesia.core.design

import androidx.compose.foundation.shape.CornerBasedShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.dp

/** Soft, generous radii that echo the reference artwork. */
@Immutable
data class SkinthesiaShapes(
    val extraSmall: CornerBasedShape = RoundedCornerShape(8.dp),
    val small: CornerBasedShape = RoundedCornerShape(12.dp),
    val medium: CornerBasedShape = RoundedCornerShape(16.dp),
    val large: CornerBasedShape = RoundedCornerShape(22.dp),
    val extraLarge: CornerBasedShape = RoundedCornerShape(28.dp),
    /** Fully rounded buttons and chips. */
    val pill: CornerBasedShape = RoundedCornerShape(50),
    /** Standard content cards. */
    val card: CornerBasedShape = RoundedCornerShape(20.dp),
    /** Selectable tiles in grids. */
    val tile: CornerBasedShape = RoundedCornerShape(18.dp),
    /** Form fields and dropdowns. */
    val field: CornerBasedShape = RoundedCornerShape(14.dp),
    /** Photography containers. */
    val image: CornerBasedShape = RoundedCornerShape(26.dp),
)

val LocalSkinthesiaShapes = staticCompositionLocalOf { SkinthesiaShapes() }

fun SkinthesiaShapes.toMaterialShapes(): Shapes = Shapes(
    extraSmall = extraSmall,
    small = small,
    medium = medium,
    large = large,
    extraLarge = extraLarge,
)
