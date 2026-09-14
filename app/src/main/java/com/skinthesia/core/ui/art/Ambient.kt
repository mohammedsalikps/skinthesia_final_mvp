package com.skinthesia.core.ui.art

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.skinthesia.core.design.SkinthesiaTheme

/**
 * Soft blush and gold light pooled behind hero moments (SkinPrint reveal, combined
 * analysis). Purely decorative; carries no semantics.
 */
@Composable
fun AmbientGlow(modifier: Modifier = Modifier, intensity: Float = 1f) {
    val colors = SkinthesiaTheme.colors
    Canvas(modifier = modifier.fillMaxSize()) {
        fun pool(center: Offset, radius: Float, color: Color, alpha: Float) {
            drawCircle(
                brush = Brush.radialGradient(listOf(color.copy(alpha = alpha * intensity), Color.Transparent), center = center, radius = radius),
                radius = radius,
                center = center,
            )
        }
        pool(Offset(size.width * 0.2f, size.height * 0.18f), size.width * 0.8f, colors.accentBlush, 0.55f)
        pool(Offset(size.width * 0.9f, size.height * 0.42f), size.width * 0.7f, colors.goldSoft, 0.7f)
        pool(Offset(size.width * 0.4f, size.height * 0.95f), size.width * 0.9f, colors.blushMist, 0.8f)
    }
}
