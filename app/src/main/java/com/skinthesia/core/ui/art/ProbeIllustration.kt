package com.skinthesia.core.ui.art

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.skinthesia.R
import com.skinthesia.core.design.SkinthesiaPalette
import com.skinthesia.core.design.SkinthesiaTheme
import kotlin.math.sin

enum class ProbeVisualState { IDLE, SEARCHING, CONNECTED, MEASURING }

/** Cropped product photo is 441x1100px. */
private const val PROBE_ASPECT = 441f / 1100f

/**
 * The official Skinthesia Probe product photo. The artwork itself is never
 * redrawn or recolored - device state is communicated entirely by the UI around
 * it: a soft ground shadow always; expanding rings while [ProbeVisualState.SEARCHING];
 * a gentle steady glow near the sensor heads once [ProbeVisualState.CONNECTED]; a
 * pulsing glow in the same spot while [ProbeVisualState.MEASURING] (that's where
 * the sensors meet the skin); and a dimmed, still photo when [ProbeVisualState.IDLE].
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
    val breath by transition.animateFloat(0f, 1f, infiniteRepeatable(tween(1800, easing = LinearEasing), RepeatMode.Restart), label = "breath")
    val float by transition.animateFloat(0f, 1f, infiniteRepeatable(tween(3000, easing = LinearEasing), RepeatMode.Reverse), label = "float")
    val animate = !motion.reducedMotion

    val glowColor = when (state) {
        ProbeVisualState.IDLE -> colors.textMuted
        ProbeVisualState.SEARCHING -> colors.gold
        ProbeVisualState.CONNECTED -> colors.success
        ProbeVisualState.MEASURING -> colors.primary
    }
    val imageAlpha = if (state == ProbeVisualState.IDLE) 0.6f else 1f
    val floatOffset = if (animate && state != ProbeVisualState.IDLE) (float - 0.5f) * 5f else 0f

    Box(modifier = modifier.semantics { this.contentDescription = contentDescription }, contentAlignment = Alignment.Center) {
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .aspectRatio(PROBE_ASPECT, matchHeightConstraintsFirst = true)
                .offset(y = floatOffset.dp),
            contentAlignment = Alignment.TopCenter,
        ) {
            Canvas(Modifier.fillMaxSize()) {
                // Ground shadow: always present, grounds the device on any surface.
                drawOval(
                    brush = Brush.radialGradient(
                        listOf(SkinthesiaPalette.Cocoa.copy(alpha = 0.16f), Color.Transparent),
                        center = Offset(size.width * 0.5f, size.height * 0.99f),
                        radius = size.width * 0.62f,
                    ),
                    topLeft = Offset(size.width * 0.1f, size.height * 0.955f),
                    size = Size(size.width * 0.8f, size.height * 0.05f),
                )

                // The two sensor heads sit at the very top of the device: every glow
                // and ring effect is centered there, never over the body or logo.
                val headCenter = Offset(size.width * 0.5f, size.height * 0.09f)

                if (state == ProbeVisualState.SEARCHING) {
                    val phases = if (animate) listOf(wave, (wave + 0.33f) % 1f, (wave + 0.66f) % 1f) else listOf(0.2f, 0.55f, 0.9f)
                    phases.forEach { p ->
                        drawCircle(
                            color = glowColor.copy(alpha = (1f - p) * 0.4f),
                            radius = size.width * (0.32f + 0.5f * p),
                            center = headCenter,
                            style = Stroke(size.width * 0.012f),
                        )
                    }
                }

                if (state == ProbeVisualState.CONNECTED) {
                    val steady = if (animate) 0.14f + 0.08f * breath else 0.18f
                    drawCircle(
                        brush = Brush.radialGradient(listOf(glowColor.copy(alpha = steady), Color.Transparent), center = headCenter, radius = size.width * 0.55f),
                        radius = size.width * 0.55f,
                        center = headCenter,
                    )
                }

                if (state == ProbeVisualState.MEASURING) {
                    val pulse = if (animate) 0.55f + 0.45f * ((sin(breath * 2 * Math.PI.toFloat()) + 1f) / 2f) else 0.85f
                    drawCircle(
                        brush = Brush.radialGradient(listOf(glowColor.copy(alpha = 0.34f * pulse), Color.Transparent), center = headCenter, radius = size.width * 0.64f),
                        radius = size.width * 0.64f,
                        center = headCenter,
                    )
                }
            }
            Image(
                painter = painterResource(R.drawable.probe_device),
                contentDescription = null,
                contentScale = ContentScale.Fit,
                modifier = Modifier.fillMaxSize().alpha(imageAlpha),
            )
        }
    }
}
