package com.skinthesia.core.ui.art

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import com.skinthesia.core.design.SkinthesiaPalette
import com.skinthesia.core.design.SkinthesiaTheme
import kotlin.math.min

enum class ProbeVisualState { IDLE, SEARCHING, CONNECTED, MEASURING }

/**
 * Illustration of the Skinthesia Probe: a porcelain handheld wand with a clay
 * accent band and a sensor head. Searching and measuring states radiate calm rings.
 */
@Composable
fun ProbeIllustration(
    state: ProbeVisualState,
    modifier: Modifier = Modifier,
    contentDescription: String = "Skinthesia Probe",
) {
    val colors = SkinthesiaTheme.colors
    val motion = SkinthesiaTheme.motion
    val transition = rememberInfiniteTransition(label = "probe")
    val wave by transition.animateFloat(0f, 1f, infiniteRepeatable(tween(2200, easing = LinearEasing), RepeatMode.Restart), label = "wave")
    val blink by transition.animateFloat(0.35f, 1f, infiniteRepeatable(tween(900), RepeatMode.Reverse), label = "blink")
    val animate = !motion.reducedMotion
    val led = when (state) {
        ProbeVisualState.IDLE -> colors.textMuted
        ProbeVisualState.SEARCHING -> colors.gold
        ProbeVisualState.CONNECTED -> colors.success
        ProbeVisualState.MEASURING -> colors.primary
    }
    val ledAlpha = if (animate && (state == ProbeVisualState.SEARCHING || state == ProbeVisualState.MEASURING)) blink else 1f

    Canvas(modifier = modifier.semantics { this.contentDescription = contentDescription }) {
        val vw = 200f
        val vh = 260f
        val scale = min(size.width / vw, size.height / vh)
        withTransform({
            translate((size.width - vw * scale) / 2, (size.height - vh * scale) / 2)
            scale(scale, scale, pivot = Offset.Zero)
        }) {
            drawOval(
                brush = Brush.radialGradient(listOf(SkinthesiaPalette.Cocoa.copy(alpha = 0.16f), Color.Transparent), center = Offset(104f, 244f), radius = 60f),
                topLeft = Offset(44f, 236f),
                size = Size(120f, 16f),
            )
            withTransform({ rotate(-14f, pivot = Offset(100f, 140f)) }) {
                val head = Offset(100f, 70f)
                if (state == ProbeVisualState.SEARCHING || state == ProbeVisualState.MEASURING) {
                    val phases = if (animate) listOf(wave, (wave + 0.33f) % 1f, (wave + 0.66f) % 1f) else listOf(0.2f, 0.55f, 0.9f)
                    phases.forEach { p ->
                        drawCircle(
                            color = (if (state == ProbeVisualState.MEASURING) colors.primary else colors.gold).copy(alpha = (1f - p) * 0.35f),
                            radius = 38f + 70f * p,
                            center = head,
                            style = Stroke(1.2f),
                        )
                    }
                }
                drawRoundRect(
                    brush = Brush.horizontalGradient(
                        listOf(SkinthesiaPalette.SandDeep, SkinthesiaPalette.Cream, SkinthesiaPalette.White, SkinthesiaPalette.Linen, SkinthesiaPalette.SandDeep),
                        startX = 76f,
                        endX = 124f,
                    ),
                    topLeft = Offset(77f, 92f),
                    size = Size(46f, 146f),
                    cornerRadius = CornerRadius(23f),
                )
                drawRect(
                    brush = Brush.horizontalGradient(listOf(SkinthesiaPalette.ClayDeep, SkinthesiaPalette.Clay, SkinthesiaPalette.ClaySoft, SkinthesiaPalette.ClayDeep), startX = 77f, endX = 123f),
                    topLeft = Offset(77f, 118f),
                    size = Size(46f, 7f),
                )
                drawCircle(
                    brush = Brush.radialGradient(listOf(SkinthesiaPalette.White, SkinthesiaPalette.Cream, SkinthesiaPalette.SandDeep), center = Offset(90f, 58f), radius = 44f),
                    radius = 34f,
                    center = head,
                )
                drawCircle(SkinthesiaPalette.SandDeep.copy(alpha = 0.8f), radius = 34f, center = head, style = Stroke(1f))
                drawCircle(
                    brush = Brush.radialGradient(listOf(SkinthesiaPalette.ClaySoft, SkinthesiaPalette.ClayDeep), center = Offset(95f, 66f), radius = 20f),
                    radius = 17f,
                    center = head,
                )
                drawCircle(SkinthesiaPalette.Gold.copy(alpha = 0.7f), radius = 17f, center = head, style = Stroke(1.4f))
                drawCircle(SkinthesiaPalette.White.copy(alpha = 0.35f), radius = 5f, center = Offset(94f, 64f))
                drawCircle(led.copy(alpha = 0.25f * ledAlpha), radius = 7f, center = Offset(100f, 152f))
                drawCircle(led.copy(alpha = ledAlpha), radius = 3.2f, center = Offset(100f, 152f))
                drawLine(SkinthesiaPalette.White.copy(alpha = 0.7f), Offset(86f, 132f), Offset(86f, 222f), strokeWidth = 2f)
            }
        }
    }
}
