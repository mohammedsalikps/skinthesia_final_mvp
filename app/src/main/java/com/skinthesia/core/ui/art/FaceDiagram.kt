package com.skinthesia.core.ui.art

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.sp
import com.skinthesia.core.design.SkinthesiaTheme
import com.skinthesia.domain.model.MeasurementRegion
import com.skinthesia.domain.model.SkinRegion
import kotlin.math.min

/** Zones the face diagram can highlight. Left and right are the user's own sides (mirror view). */
enum class FaceZone { FOREHEAD, LEFT_CHEEK, RIGHT_CHEEK, NOSE, UNDER_EYES, AROUND_MOUTH, CHIN, JAWLINE }

data class ZoneHighlight(val zone: FaceZone, val color: Color, val intensity: Float = 1f)

fun MeasurementRegion.zone(): FaceZone = when (this) {
    MeasurementRegion.FOREHEAD -> FaceZone.FOREHEAD
    MeasurementRegion.LEFT_CHEEK -> FaceZone.LEFT_CHEEK
    MeasurementRegion.RIGHT_CHEEK -> FaceZone.RIGHT_CHEEK
}

fun SkinRegion.zone(): FaceZone = when (this) {
    SkinRegion.FOREHEAD -> FaceZone.FOREHEAD
    SkinRegion.UNDER_EYES -> FaceZone.UNDER_EYES
    SkinRegion.LEFT_CHEEK -> FaceZone.LEFT_CHEEK
    SkinRegion.RIGHT_CHEEK -> FaceZone.RIGHT_CHEEK
    SkinRegion.NOSE -> FaceZone.NOSE
    SkinRegion.AROUND_MOUTH -> FaceZone.AROUND_MOUTH
    SkinRegion.CHIN -> FaceZone.CHIN
    SkinRegion.JAWLINE -> FaceZone.JAWLINE
}

private const val VIEW_W = 200f
private const val VIEW_H = 252f

private const val OUTLINE =
    "M100 18C58 18 34 52 34 100C34 140 44 176 66 204C78 220 90 230 100 232C110 230 122 220 134 204C156 176 166 140 166 100C166 52 142 18 100 18Z"
private val FEATURES = listOf(
    "M60 92C70 86 82 86 90 90",
    "M110 90C118 86 130 86 140 92",
    "M63 108C71 113 81 113 89 108",
    "M111 108C119 113 129 113 137 108",
    "M100 112C99 128 95 140 92 148C96 152 104 152 108 148",
    "M84 177C92 173 98 175 100 177C102 175 108 173 116 177",
    "M86 178C94 184 106 184 114 178",
    "M34 100C26 98 24 118 34 124",
    "M166 100C174 98 176 118 166 124",
    "M82 226L79 250",
    "M118 226L121 250",
)

/** Zone centres and radii in the 200 × 252 viewport; the user's left cheek sits on screen-left. */
private fun zoneSpots(zone: FaceZone): List<Triple<Float, Float, Float>> = when (zone) {
    FaceZone.FOREHEAD -> listOf(Triple(100f, 60f, 30f))
    FaceZone.LEFT_CHEEK -> listOf(Triple(64f, 148f, 21f))
    FaceZone.RIGHT_CHEEK -> listOf(Triple(136f, 148f, 21f))
    FaceZone.NOSE -> listOf(Triple(100f, 136f, 13f))
    FaceZone.UNDER_EYES -> listOf(Triple(76f, 124f, 11f), Triple(124f, 124f, 11f))
    FaceZone.AROUND_MOUTH -> listOf(Triple(100f, 180f, 19f))
    FaceZone.CHIN -> listOf(Triple(100f, 214f, 14f))
    FaceZone.JAWLINE -> listOf(Triple(60f, 190f, 14f), Triple(140f, 190f, 14f))
}

/**
 * Minimal single-line face used for measurement guidance, the face map and
 * analysis overlays. Highlights glow softly; the active zone breathes; completed
 * zones carry a small sage check.
 */
@Composable
fun FaceDiagram(
    contentDescription: String,
    modifier: Modifier = Modifier,
    highlights: List<ZoneHighlight> = emptyList(),
    activeZone: FaceZone? = null,
    completedZones: Set<FaceZone> = emptySet(),
    measurementPoints: Set<FaceZone> = emptySet(),
    scanning: Boolean = false,
    showSideLabels: Boolean = false,
    lineColor: Color = SkinthesiaTheme.colors.textSecondary.copy(alpha = 0.6f),
) {
    val colors = SkinthesiaTheme.colors
    val motion = SkinthesiaTheme.motion
    val measurer = rememberTextMeasurer()
    val outline = remember { PathParser().parsePathString(OUTLINE).toPath() }
    val features = remember { FEATURES.map { PathParser().parsePathString(it).toPath() } }
    val check = remember { PathParser().parsePathString("M-4 0L-1 3L4.5-3").toPath() }
    val transition = rememberInfiniteTransition(label = "face")
    val pulse by transition.animateFloat(0f, 1f, infiniteRepeatable(tween(1600, easing = FastOutSlowInEasing), RepeatMode.Restart), label = "pulse")
    val sweep by transition.animateFloat(0f, 1f, infiniteRepeatable(tween(2600, easing = LinearEasing), RepeatMode.Reverse), label = "sweep")
    val animate = !motion.reducedMotion

    Canvas(modifier = modifier.semantics { this.contentDescription = contentDescription }) {
        val scale = min(size.width / VIEW_W, size.height / VIEW_H)
        val dx = (size.width - VIEW_W * scale) / 2
        val dy = (size.height - VIEW_H * scale) / 2
        withTransform({
            translate(dx, dy)
            scale(scale, scale, pivot = Offset.Zero)
        }) {
            drawPath(outline, colors.surfaceElevated.copy(alpha = 0.55f))

            highlights.forEach { h -> zoneSpots(h.zone).forEach { (x, y, r) -> glow(Offset(x, y), r * 1.25f, h.color, 0.5f * h.intensity) } }
            completedZones.forEach { zone -> zoneSpots(zone).forEach { (x, y, r) -> glow(Offset(x, y), r * 1.2f, colors.success, 0.45f) } }
            activeZone?.let { zone ->
                zoneSpots(zone).forEach { (x, y, r) ->
                    glow(Offset(x, y), r * 1.3f, colors.primary, 0.42f)
                    val p = if (animate) pulse else 0.4f
                    drawCircle(colors.primary.copy(alpha = (1f - p) * 0.8f), radius = r * (0.9f + 0.9f * p), center = Offset(x, y), style = Stroke(1.2f))
                }
            }

            if (scanning) {
                clipPath(outline) {
                    val y = 18f + (232f - 18f) * (if (animate) sweep else 0.5f)
                    drawRect(
                        brush = Brush.verticalGradient(
                            0f to colors.primary.copy(alpha = 0f),
                            0.85f to colors.primary.copy(alpha = 0.16f),
                            1f to colors.primary.copy(alpha = 0f),
                            startY = y - 36f,
                            endY = y + 2f,
                        ),
                        topLeft = Offset(0f, y - 36f),
                        size = androidx.compose.ui.geometry.Size(VIEW_W, 38f),
                    )
                    drawLine(colors.primary.copy(alpha = 0.7f), Offset(20f, y), Offset(180f, y), strokeWidth = 1.2f)
                }
            }

            val line = Stroke(width = 1.5f, cap = StrokeCap.Round, join = StrokeJoin.Round)
            drawPath(outline, lineColor, style = line)
            features.forEach { drawPath(it, lineColor, style = Stroke(width = 1.2f, cap = StrokeCap.Round, join = StrokeJoin.Round)) }

            measurementPoints.forEach { zone ->
                zoneSpots(zone).forEach { (x, y, _) ->
                    drawCircle(colors.surfaceElevated, radius = 5f, center = Offset(x, y))
                    drawCircle(if (zone in completedZones) colors.success else colors.primary, radius = 5f, center = Offset(x, y), style = Stroke(1.4f))
                }
            }
            completedZones.forEach { zone ->
                val (x, y, r) = zoneSpots(zone).first()
                val c = Offset(x + r * 0.7f, y - r * 0.7f)
                drawCircle(colors.success, radius = 7f, center = c)
                withTransform({ translate(c.x, c.y) }) {
                    drawPath(check, colors.textOnPrimary, style = Stroke(1.6f, cap = StrokeCap.Round, join = StrokeJoin.Round))
                }
            }

            if (showSideLabels) {
                val style = TextStyle(fontSize = 9.sp, color = colors.textMuted)
                val left = measurer.measure("L", style)
                val right = measurer.measure("R", style)
                drawText(left, topLeft = Offset(16f - left.size.width / (2f * scale), 144f))
                drawText(right, topLeft = Offset(184f - right.size.width / (2f * scale), 144f))
            }
        }
    }
}

private fun DrawScope.glow(center: Offset, radius: Float, color: Color, alpha: Float) {
    drawCircle(
        brush = Brush.radialGradient(
            0f to color.copy(alpha = alpha),
            0.6f to color.copy(alpha = alpha * 0.45f),
            1f to color.copy(alpha = 0f),
            center = center,
            radius = radius,
        ),
        radius = radius,
        center = center,
    )
}
