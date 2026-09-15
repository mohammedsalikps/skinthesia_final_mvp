package com.skinthesia.core.ui.launch

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.skinthesia.R
import com.skinthesia.core.design.SkinthesiaTheme
import com.skinthesia.core.ui.art.ProductArtwork
import com.skinthesia.core.ui.components.BrandMonogram
import com.skinthesia.core.ui.components.BrandWordmark
import com.skinthesia.domain.model.ProductForm
import com.skinthesia.domain.model.ProductTone
import kotlin.math.cos
import kotlin.math.sin

private const val FULL_DURATION_MS = 5000
private const val REDUCED_DURATION_MS = 900

/**
 * The ~5 second brand opening: floating skincare silhouettes dissolve into light and
 * converge to reveal the Skinthesia mark, then the wordmark and tagline settle in.
 * Shown once per app launch, after the system splash hands off. Progress is a single
 * deterministic, time-based value so it behaves the same on every device and can't
 * drift or desync; tapping anywhere skips straight to the app.
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
        EnvironmentGlow(t, motion.reducedMotion)
        if (motion.reducedMotion) {
            ReducedRevealContent(t)
        } else {
            FloatingProducts(t)
            ParticleField(t)
            RevealContent(t)
        }
    }
}

@Composable
private fun EnvironmentGlow(t: Float, reducedMotion: Boolean) {
    val colors = SkinthesiaTheme.colors
    val envIn = remap(t, 0f, 0.12f)
    val glowPulse = 1f + 0.06f * bellCurve(t, 0.74f, 0.14f)
    Box(
        modifier = Modifier
            .fillMaxSize()
            .alpha(if (reducedMotion) 1f else envIn)
            .scale(if (reducedMotion) 1f else glowPulse)
            .background(
                Brush.radialGradient(
                    0f to colors.primaryMist.copy(alpha = 0.55f),
                    0.55f to colors.background,
                    1f to colors.background,
                ),
            ),
    )
}

private data class IntroProduct(
    val form: ProductForm,
    val tone: ProductTone,
    val startX: Float,
    val startY: Float,
    val wobbleFreq: Float,
    val wobblePhase: Float,
)

private val INTRO_PRODUCTS = listOf(
    IntroProduct(ProductForm.DROPPER, ProductTone.CLAY, -100f, -68f, 1.4f, 0.0f),
    IntroProduct(ProductForm.PUMP, ProductTone.SAGE, 104f, -60f, 1.1f, 1.4f),
    IntroProduct(ProductForm.JAR, ProductTone.BLUSH, -92f, 82f, 1.3f, 2.6f),
    IntroProduct(ProductForm.TUBE, ProductTone.AMBER, 96f, 88f, 1.0f, 4.0f),
)

@Composable
private fun FloatingProducts(t: Float) {
    INTRO_PRODUCTS.forEachIndexed { index, product ->
        val delay = index * 0.025f
        val fadeIn = remap(t, 0.02f + delay, 0.16f + delay)
        val fadeOut = 1f - remap(t, 0.40f + delay, 0.56f + delay)
        val alpha = (fadeIn * fadeOut).coerceIn(0f, 1f)
        if (alpha <= 0f) return@forEachIndexed

        val orbit = easeInOut(remap(t, 0.10f, 0.50f))
        val settle = 1f - 0.5f * orbit
        val wobble = sin(t * product.wobbleFreq * 6.2832f + product.wobblePhase)
        val x = product.startX * settle + wobble * 5f
        val y = product.startY * settle + cos(t * product.wobbleFreq * 5.1f + product.wobblePhase) * 4f
        val productScale = 0.82f + 0.18f * fadeIn

        Box(
            modifier = Modifier
                .size(52.dp)
                .offsetDp(x, y)
                .scale(productScale)
                .alpha(alpha),
        ) {
            ProductArtwork(
                form = product.form,
                tone = product.tone,
                modifier = Modifier.fillMaxSize(),
                mark = "",
                backdrop = false,
            )
        }
    }
}

/**
 * Fixed, deterministic UV points sampled from the emblem's own silhouette (via
 * farthest-point sampling of its alpha mask) so the particles converge into the
 * real mark's shape rather than an invented one.
 */
private val EMBLEM_TARGETS = listOf(
    0.7515f to 0.4028f, 0.0517f to 0.6766f, 0.5785f to 0.9821f, 0.2326f to 0.0972f,
    0.6143f to 0.0278f, 0.3658f to 0.4563f, 0.8907f to 0.7560f, 0.0417f to 0.3552f,
    0.2724f to 0.9008f, 0.5845f to 0.6726f, 0.8449f to 0.1548f, 0.9742f to 0.5198f,
    0.4533f to 0.2262f, 0.3519f to 0.6845f, 0.9483f to 0.3234f, 0.5626f to 0.4722f,
    0.4175f to 0.0298f, 0.7137f to 0.8413f, 0.6382f to 0.2321f, 0.4414f to 0.8929f,
    0.0159f to 0.5179f, 0.1074f to 0.2063f, 0.3141f to 0.3056f, 0.1431f to 0.8075f,
    0.6779f to 0.5615f, 0.5249f to 0.3413f, 0.7157f to 0.1190f, 0.5268f to 0.7917f,
    0.2962f to 0.5675f, 0.8986f to 0.6270f, 0.6481f to 0.3492f, 0.4652f to 0.5298f,
    0.4155f to 0.3571f, 0.8569f to 0.2659f, 0.3400f to 0.1052f, 0.9145f to 0.4325f,
    0.6163f to 0.8810f, 0.8171f to 0.8452f, 0.5229f to 0.0734f, 0.6282f to 0.7798f,
    0.4254f to 0.7540f, 0.5547f to 0.5754f, 0.1451f to 0.7044f, 0.1272f to 0.3016f,
    0.0895f to 0.5873f, 0.6799f to 0.6607f, 0.0815f to 0.4425f, 0.4751f to 0.4325f,
    0.2386f to 0.8115f, 0.2068f to 0.1885f, 0.3579f to 0.9385f, 0.5447f to 0.2163f,
    0.2684f to 0.6548f, 0.5328f to 0.9028f, 0.6899f to 0.9226f, 0.3101f to 0.3929f,
)

private const val EMBLEM_REVEAL_DP = 116f

@Composable
private fun ParticleField(t: Float) {
    val colors = SkinthesiaTheme.colors
    val particleColor = colors.primary
    val glowColor = colors.gold
    val density = LocalDensity.current
    Canvas(modifier = Modifier.fillMaxSize()) {
        val cx = size.width / 2f
        val cy = size.height / 2f
        val pxPerDp = density.density

        EMBLEM_TARGETS.forEachIndexed { i, (u, v) ->
            val product = INTRO_PRODUCTS[i % INTRO_PRODUCTS.size]
            val jitterAngle = i * 2.399963f
            val jitterRadius = (14f + (i % 7) * 4f) * pxPerDp
            val startX = cx + (product.startX * pxPerDp) + cos(jitterAngle) * jitterRadius
            val startY = cy + (product.startY * pxPerDp) + sin(jitterAngle) * jitterRadius
            val targetX = cx + (u - 0.5f) * EMBLEM_REVEAL_DP * pxPerDp
            val targetY = cy + (v - 0.5f) * EMBLEM_REVEAL_DP * pxPerDp

            val fadeIn = remap(t, 0.18f + (i % 5) * 0.01f, 0.32f)
            val fadeOut = 1f - remap(t, 0.74f, 0.86f)
            val particleAlpha = (fadeIn * fadeOut).coerceIn(0f, 1f)
            if (particleAlpha <= 0.01f) return@forEachIndexed

            val converge = easeInOut(remap(t, 0.46f, 0.80f))
            val px = lerp(startX, targetX, converge)
            val py = lerp(startY, targetY, converge)
            val radius = (1.1f + (i % 4) * 0.5f) * pxPerDp

            drawCircle(color = glowColor.copy(alpha = particleAlpha * 0.25f), radius = radius * 2.4f, center = Offset(px, py))
            drawCircle(color = particleColor.copy(alpha = particleAlpha * 0.9f), radius = radius, center = Offset(px, py))
        }
    }
}

@Composable
private fun RevealContent(t: Float) {
    val colors = SkinthesiaTheme.colors
    val emblemAlpha = remap(t, 0.62f, 0.84f)
    val glow = bellCurve(t, 0.76f, 0.10f)
    val wordmarkT = remap(t, 0.84f, 0.97f)
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
                Box(modifier = Modifier.alpha(emblemAlpha)) {
                    BrandMonogram(size = EMBLEM_REVEAL_DP.dp)
                }
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

/** Short, still-branded version shown when the system asks for reduced motion. */
@Composable
private fun ReducedRevealContent(t: Float) {
    val colors = SkinthesiaTheme.colors
    val alpha = remap(t, 0.15f, 0.55f)
    val wordmarkAlpha = remap(t, 0.45f, 0.75f)
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(modifier = Modifier.alpha(alpha)) {
            BrandMonogram(size = EMBLEM_REVEAL_DP.dp)
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

private fun lerp(a: Float, b: Float, t: Float): Float = a + (b - a) * t

private fun easeInOut(t: Float): Float = FastOutSlowInEasing.transform(t)

private fun easeOut(t: Float): Float = 1f - (1f - t) * (1f - t)

/** Smooth 0..1..0 bump centered at [center] with the given [width]; used for the glow pulse. */
private fun bellCurve(t: Float, center: Float, width: Float): Float {
    val d = ((t - center) / width).coerceIn(-1f, 1f)
    return (1f - d * d).coerceIn(0f, 1f)
}
