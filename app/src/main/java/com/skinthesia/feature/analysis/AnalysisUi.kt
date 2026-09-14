package com.skinthesia.feature.analysis

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.skinthesia.core.design.SkinthesiaTheme
import com.skinthesia.core.ui.art.FaceDiagram
import com.skinthesia.core.ui.components.SkinthesiaImage
import com.skinthesia.core.ui.components.TagTone
import com.skinthesia.core.ui.components.rememberReveal
import com.skinthesia.core.ui.icons.SkinthesiaIcons
import com.skinthesia.domain.model.ImageSource
import com.skinthesia.domain.model.SkinPrintDimension
import com.skinthesia.domain.model.VisibilityLevel
import com.skinthesia.domain.model.VisualIndicator
import com.skinthesia.feature.measurement.PulseDot
import kotlin.math.abs

val VisualIndicator.icon: ImageVector
    get() = when (this) {
        VisualIndicator.ACNE_APPEARANCE -> SkinthesiaIcons.Breakouts
        VisualIndicator.PIGMENTATION -> SkinthesiaIcons.DarkSpots
        VisualIndicator.TEXTURE -> SkinthesiaIcons.Texture
        VisualIndicator.PORES -> SkinthesiaIcons.Pores
        VisualIndicator.REDNESS -> SkinthesiaIcons.Calm
        VisualIndicator.DARK_CIRCLES -> SkinthesiaIcons.UnderEye
        VisualIndicator.FINE_LINES -> SkinthesiaIcons.FineLines
    }

val SkinPrintDimension.icon: ImageVector
    get() = when (this) {
        SkinPrintDimension.CLARITY -> SkinthesiaIcons.Sparkle
        SkinPrintDimension.EVEN_TONE -> SkinthesiaIcons.EvenTone
        SkinPrintDimension.TEXTURE -> SkinthesiaIcons.Texture
        SkinPrintDimension.HYDRATION -> SkinthesiaIcons.Droplet
        SkinPrintDimension.PORE_APPEARANCE -> SkinthesiaIcons.Pores
    }

val VisibilityLevel.tagTone: TagTone
    get() = when (this) {
        VisibilityLevel.MINIMAL -> TagTone.SAGE
        VisibilityLevel.MILD -> TagTone.MIST
        VisibilityLevel.MODERATE -> TagTone.GOLD
        VisibilityLevel.NOTICEABLE -> TagTone.ROSE
    }

/** Calm colour for how visible a feature looks. Never alarm-red. */
@Composable
fun visibilityColor(level: VisibilityLevel): Color {
    val colors = SkinthesiaTheme.colors
    return when (level) {
        VisibilityLevel.MINIMAL -> colors.success
        VisibilityLevel.MILD -> colors.info
        VisibilityLevel.MODERATE -> colors.gold
        VisibilityLevel.NOTICEABLE -> colors.accentBlushDeep
    }
}

enum class ProcessStatus { PENDING, ACTIVE, DONE }

fun processStatus(index: Int, stage: Int, done: Boolean): ProcessStatus = when {
    done || index < stage -> ProcessStatus.DONE
    index == stage -> ProcessStatus.ACTIVE
    else -> ProcessStatus.PENDING
}

/** One line of a staged "working on it" checklist. */
@Composable
fun ProcessStepRow(title: String, status: ProcessStatus, modifier: Modifier = Modifier) {
    val colors = SkinthesiaTheme.colors
    Row(
        modifier = modifier.fillMaxWidth().padding(vertical = 10.dp).semantics(mergeDescendants = true) {},
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(24.dp)
                .clip(CircleShape)
                .background(
                    when (status) {
                        ProcessStatus.DONE -> colors.successSoft
                        ProcessStatus.ACTIVE -> colors.primaryMist
                        ProcessStatus.PENDING -> colors.surfaceMuted
                    },
                ),
            contentAlignment = Alignment.Center,
        ) {
            when (status) {
                ProcessStatus.DONE -> Icon(SkinthesiaIcons.Check, contentDescription = null, tint = colors.success, modifier = Modifier.size(13.dp))
                ProcessStatus.ACTIVE -> PulseDot(size = 7.dp)
                ProcessStatus.PENDING -> Unit
            }
        }
        Spacer(Modifier.width(12.dp))
        Text(
            text = title,
            style = SkinthesiaTheme.typography.body,
            color = if (status == ProcessStatus.PENDING) colors.textMuted else colors.textPrimary,
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
fun IconBadge(
    icon: ImageVector,
    modifier: Modifier = Modifier,
    size: Dp = 36.dp,
    container: Color = SkinthesiaTheme.colors.primaryMist,
    tint: Color = SkinthesiaTheme.colors.primary,
) {
    Box(modifier.size(size).clip(CircleShape).background(container), contentAlignment = Alignment.Center) {
        Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(size * 0.5f))
    }
}

/** A slim bar that grows in once, used for levels and scores. */
@Composable
fun LevelBar(fraction: Float, color: Color, modifier: Modifier = Modifier, delayMillis: Int = 0, height: Dp = 5.dp) {
    val reveal = rememberReveal(fraction.coerceIn(0f, 1f), durationMillis = 900, delayMillis = delayMillis)
    Box(modifier.fillMaxWidth().height(height).clip(CircleShape).background(SkinthesiaTheme.colors.track)) {
        Box(Modifier.fillMaxHeight().fillMaxWidth(reveal.value.coerceIn(0f, 1f)).clip(CircleShape).background(color))
    }
}

private val LANDMARKS = listOf(0.5f to 0.30f, 0.36f to 0.50f, 0.64f to 0.50f, 0.5f to 0.47f, 0.5f to 0.68f)

/**
 * The user's selfie in a quiet frame. While [scanning], a soft light band sweeps
 * across and a few facial landmarks glow as it passes.
 */
@Composable
fun PhotoScanFrame(photoPath: String?, scanning: Boolean, modifier: Modifier = Modifier) {
    val colors = SkinthesiaTheme.colors
    val reduced = SkinthesiaTheme.motion.reducedMotion
    val transition = rememberInfiniteTransition(label = "scan")
    val sweep by transition.animateFloat(0.08f, 0.92f, infiniteRepeatable(tween(2000, easing = LinearEasing), RepeatMode.Reverse), label = "sweep")
    val veil by animateFloatAsState(if (scanning) 1f else 0f, tween(500), label = "veil")
    Box(modifier.clip(SkinthesiaTheme.shapes.card).background(colors.surfaceMuted)) {
        if (photoPath != null) {
            SkinthesiaImage(source = ImageSource.LocalFile(photoPath), contentDescription = "Your selfie", modifier = Modifier.fillMaxSize(), maxDimension = 900)
        } else {
            FaceDiagram(contentDescription = "Face outline", modifier = Modifier.fillMaxSize().padding(28.dp))
        }
        Canvas(Modifier.fillMaxSize()) {
            val light = colors.textOnPhoto
            if (veil > 0f) {
                drawRect(colors.photoOverlay.copy(alpha = 0.18f * veil))
                val y = size.height * (if (reduced) 0.5f else sweep)
                val band = 70.dp.toPx()
                drawRect(
                    brush = Brush.verticalGradient(
                        listOf(Color.Transparent, colors.primarySoft.copy(alpha = 0.45f * veil), Color.Transparent),
                        startY = y - band,
                        endY = y + band,
                    ),
                    topLeft = Offset(0f, y - band),
                    size = Size(size.width, band * 2),
                )
                drawLine(light.copy(alpha = 0.85f * veil), Offset(0f, y), Offset(size.width, y), strokeWidth = 1.5.dp.toPx())
                LANDMARKS.forEach { (fx, fy) ->
                    val p = Offset(size.width * fx, size.height * fy)
                    val near = (1f - abs(p.y - y) / (size.height * 0.35f)).coerceIn(0.25f, 1f)
                    drawCircle(light.copy(alpha = 0.9f * veil * near), radius = 3.dp.toPx(), center = p)
                    drawCircle(light.copy(alpha = 0.35f * veil * near), radius = 9.dp.toPx(), center = p, style = Stroke(1.dp.toPx()))
                }
            }
            val inset = 14.dp.toPx()
            val len = 22.dp.toPx()
            val sw = 2.dp.toPx()
            val c = light.copy(alpha = 0.9f)
            fun corner(x: Float, y: Float, dx: Float, dy: Float) {
                drawLine(c, Offset(x, y), Offset(x + dx * len, y), sw, StrokeCap.Round)
                drawLine(c, Offset(x, y), Offset(x, y + dy * len), sw, StrokeCap.Round)
            }
            corner(inset, inset, 1f, 1f)
            corner(size.width - inset, inset, -1f, 1f)
            corner(inset, size.height - inset, 1f, -1f)
            corner(size.width - inset, size.height - inset, -1f, -1f)
        }
    }
}
