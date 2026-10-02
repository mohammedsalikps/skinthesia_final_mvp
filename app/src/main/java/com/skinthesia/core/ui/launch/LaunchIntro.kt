package com.skinthesia.core.ui.launch

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.skinthesia.R
import com.skinthesia.core.design.SkinthesiaTheme
import com.skinthesia.core.ui.components.BrandWordmark

private const val FULL_DURATION_MS = 8000
private const val REDUCED_DURATION_MS = 900
private const val EMBLEM_REVEAL_DP = 116f

/**
 * The Skinthesia opening: a calm, premium ~8 second brand moment shown once per
 * app launch, after the system splash hands off - "skincare innovation blink":
 *
 *   0.0-2.0s  A soft, glossy bead of light appears and settles - skincare, not tech.
 *   2.0-3.0s  It blinks away in one clean flash - the "skincare -> intelligence" beat.
 *   3.0-4.0s  A brief, calm, empty moment.
 *   4.0-6.0s  The official emblem is revealed by soft light, not constructed from parts.
 *   6.0-8.0s  The "Skinthesia" wordmark and tagline settle beneath it, and hold.
 *
 * No particles, no connecting lines, no generated imagery - only the real emblem
 * asset and Compose-native gradients, blur, scale and alpha. Progress is a single
 * deterministic, time-based value so every phase lands at the same moment on every
 * device and can't drift or desync; tapping anywhere skips straight to the app.
 * Reduced-motion gets a short, still-branded crossfade.
 */
@Composable
fun LaunchIntro(onFinished: () -> Unit, modifier: Modifier = Modifier) {
    val motion = SkinthesiaTheme.motion
    val colors = SkinthesiaTheme.colors
    val progress = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        val duration = if (motion.reducedMotion) REDUCED_DURATION_MS else FULL_DURATION_MS
        progress.animateTo(1f, animationSpec = tween(durationMillis = duration, easing = LinearEasing))
        onFinished()
    }

    val brandDescription = stringResource(R.string.brand_name)
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(colors.background)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onFinished,
            )
            .semantics { contentDescription = brandDescription },
        contentAlignment = Alignment.Center,
    ) {
        val t = progress.value
        if (motion.reducedMotion) {
            ReducedRevealContent(t)
        } else {
            AmbientGlow(t)
            DropletBloom(t)
            RevealContent(t)
            BlinkFlash(t)
        }
    }
}

/** A quiet, warm background wash that fades in once and holds - never a moving pattern. */
@Composable
private fun AmbientGlow(t: Float) {
    val colors = SkinthesiaTheme.colors
    val envIn = remap(t, 0f, 0.1f)
    Box(
        modifier = Modifier
            .fillMaxSize()
            .alpha(envIn)
            .background(
                Brush.radialGradient(
                    0f to colors.primaryMist.copy(alpha = 0.5f),
                    0.6f to colors.background,
                    1f to colors.background,
                ),
            ),
    )
}

/**
 * Beat one: a single soft, glossy bead of light - the impression of a drop of
 * serum catching light, never a literal illustration. It breathes gently while
 * present, then contracts and fades in the same beat the [BlinkFlash] covers the
 * screen, so the "skincare -> intelligence" transition reads as one clean blink
 * rather than a hard cut.
 */
@Composable
private fun DropletBloom(t: Float) {
    val colors = SkinthesiaTheme.colors
    val appear = easeOut(remap(t, 0.03f, 0.18f))
    val shrink = remap(t, 0.22f, 0.30f)
    val alpha = appear * (1f - remap(t, 0.24f, 0.32f))
    if (alpha <= 0.01f) return
    val breathe = 1f + 0.05f * bellCurve(t, 0.15f, 0.09f)
    val scale = (0.86f + 0.14f * appear) * breathe * (1f - 0.35f * shrink)

    Canvas(
        modifier = Modifier
            .size(108.dp)
            .alpha(alpha)
            .scale(scale),
    ) {
        val r = size.minDimension / 2f
        val c = center
        // Outer soft bloom.
        drawCircle(
            brush = Brush.radialGradient(listOf(colors.gold.copy(alpha = 0.35f), Color.Transparent), center = c, radius = r * 1.9f),
            radius = r * 1.9f,
            center = c,
        )
        // The bead itself - a warm, glassy sphere.
        drawCircle(
            brush = Brush.radialGradient(
                listOf(colors.goldSoft, colors.gold, colors.primary),
                center = Offset(c.x - r * 0.2f, c.y - r * 0.25f),
                radius = r * 1.5f,
            ),
            radius = r,
            center = c,
        )
        // A small specular highlight - the one detail that reads as "glossy droplet".
        drawOval(
            brush = Brush.radialGradient(listOf(Color.White.copy(alpha = 0.8f), Color.Transparent)),
            topLeft = Offset(c.x - r * 0.48f, c.y - r * 0.58f),
            size = Size(r * 0.5f, r * 0.34f),
        )
    }
}

/** A single brief, near-white flash covering the screen - the "blink" itself. */
@Composable
private fun BlinkFlash(t: Float) {
    val flash = bellCurve(t, 0.285f, 0.04f)
    if (flash <= 0.01f) return
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White.copy(alpha = flash * 0.92f)),
    )
}

@Composable
private fun RevealContent(t: Float) {
    val colors = SkinthesiaTheme.colors
    val emblemAlpha = remap(t, 0.40f, 0.56f)
    val glow = bellCurve(t, 0.50f, 0.11f)
    val sweepT = remap(t, 0.46f, 0.60f)
    val sweepAlpha = (1f - kotlin.math.abs(2f * sweepT - 1f)).coerceIn(0f, 1f)
    val wordmarkT = remap(t, 0.64f, 0.80f)
    val wordmarkOffset = (1f - easeOut(wordmarkT)) * 10f

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(contentAlignment = Alignment.Center) {
            if (glow > 0.01f) {
                Box(
                    modifier = Modifier
                        .size((EMBLEM_REVEAL_DP * 1.5f).dp)
                        .alpha(glow * 0.5f)
                        .blur((EMBLEM_REVEAL_DP * 0.22f).dp)
                        .background(colors.gold, CircleShape),
                )
            }
            if (emblemAlpha > 0.01f) {
                EmblemWithSweep(alpha = emblemAlpha, sweepProgress = sweepT, sweepAlpha = sweepAlpha, size = EMBLEM_REVEAL_DP.dp)
            }
        }
        if (wordmarkT > 0.01f) {
            Spacer(Modifier.height(14.dp))
            Box(modifier = Modifier.alpha(wordmarkT).offsetDp(0f, wordmarkOffset)) {
                BrandWordmark(height = 26.dp)
            }
            Spacer(Modifier.height(6.dp))
            Box(modifier = Modifier.alpha(wordmarkT).offsetDp(0f, wordmarkOffset)) {
                Text(
                    text = stringResource(R.string.brand_tagline),
                    style = SkinthesiaTheme.typography.brandTagline,
                    color = colors.textMuted,
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}

/**
 * The emblem with a single soft light sweep crossing it once as it solidifies -
 * masked to the artwork's own opaque pixels ([BlendMode.SrcAtop] inside an
 * offscreen-composited layer) so the highlight only ever touches the mark
 * itself, never the transparent air around it. Never redrawn or recreated -
 * this is the real [R.drawable.brand_emblem] asset, revealed by light.
 */
@Composable
private fun EmblemWithSweep(alpha: Float, sweepProgress: Float, sweepAlpha: Float, size: Dp) {
    val density = LocalDensity.current
    Box(
        modifier = Modifier
            .size(size)
            .graphicsLayer {
                this.alpha = alpha
                compositingStrategy = CompositingStrategy.Offscreen
            }
            .drawWithContent {
                drawContent()
                if (sweepAlpha > 0.01f) {
                    val span = this.size.width + this.size.height
                    val bandCenter = span * (sweepProgress * 1.5f - 0.25f)
                    val bandHalf = with(density) { 26.dp.toPx() }
                    drawRect(
                        brush = Brush.linearGradient(
                            0f to Color.Transparent,
                            0.5f to Color.White.copy(alpha = sweepAlpha * 0.85f),
                            1f to Color.Transparent,
                            start = Offset(bandCenter - bandHalf, 0f),
                            end = Offset(bandCenter + bandHalf, this.size.height),
                        ),
                        blendMode = BlendMode.SrcAtop,
                    )
                }
            },
    ) {
        Image(
            painter = painterResource(R.drawable.brand_emblem),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
        )
    }
}

/** Short, still-branded version shown when the system asks for reduced motion. */
@Composable
private fun ReducedRevealContent(t: Float) {
    val colors = SkinthesiaTheme.colors
    val alpha = remap(t, 0.15f, 0.55f)
    val wordmarkAlpha = remap(t, 0.45f, 0.75f)
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(modifier = Modifier.alpha(alpha)) {
            Image(
                painter = painterResource(R.drawable.brand_emblem),
                contentDescription = null,
                modifier = Modifier.size(EMBLEM_REVEAL_DP.dp),
            )
        }
        Spacer(Modifier.height(14.dp))
        Box(modifier = Modifier.alpha(wordmarkAlpha)) {
            BrandWordmark(height = 26.dp)
        }
        Spacer(Modifier.height(6.dp))
        Box(modifier = Modifier.alpha(wordmarkAlpha)) {
            Text(
                text = stringResource(R.string.brand_tagline),
                style = SkinthesiaTheme.typography.brandTagline,
                color = colors.textMuted,
                textAlign = TextAlign.Center,
            )
        }
    }
}

/** Offsets by a raw dp amount without triggering a state-read-driven recomposition of layout. */
private fun Modifier.offsetDp(x: Float, y: Float): Modifier = this.then(
    Modifier.layout { measurable, constraints ->
        val placeable = measurable.measure(constraints)
        val xPx = (x * density).toInt()
        val yPx = (y * density).toInt()
        layout(placeable.width, placeable.height) {
            placeable.place(xPx, yPx)
        }
    },
)

private fun remap(t: Float, from: Float, to: Float): Float =
    ((t - from) / (to - from)).coerceIn(0f, 1f)

private fun easeOut(t: Float): Float = 1f - (1f - t) * (1f - t)

/** Smooth 0..1..0 bump centered at [center] with the given [width]; used for gentle pulses and the blink flash. */
private fun bellCurve(t: Float, center: Float, width: Float): Float {
    val d = ((t - center) / width).coerceIn(-1f, 1f)
    return (1f - d * d).coerceIn(0f, 1f)
}
