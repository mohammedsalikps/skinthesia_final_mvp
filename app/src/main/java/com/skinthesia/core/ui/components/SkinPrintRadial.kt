package com.skinthesia.core.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.vector.ImageVector
import com.skinthesia.core.design.SkinthesiaTheme
import com.skinthesia.core.ui.icons.SkinthesiaIcons
import com.skinthesia.domain.model.ScoreBand
import com.skinthesia.domain.model.SkinPrintDimension
import com.skinthesia.domain.model.SkinScore
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

private fun SkinPrintDimension.icon(): ImageVector = when (this) {
    SkinPrintDimension.CLARITY -> SkinthesiaIcons.Glow
    SkinPrintDimension.EVEN_TONE -> SkinthesiaIcons.EvenTone
    SkinPrintDimension.TEXTURE -> SkinthesiaIcons.Texture
    SkinPrintDimension.HYDRATION -> SkinthesiaIcons.Droplet
    SkinPrintDimension.PORE_APPEARANCE -> SkinthesiaIcons.Pores
}

/**
 * Skinthesia's signature visualization: the five SkinPrint dimensions as one
 * interactive shape, with the overall score held at its centre. Tap a dimension
 * to highlight it and reveal its value, trend and explanation below - reading
 * the shape's silhouette should give an instant sense of where skin is strong
 * versus where it needs attention, without a wall of numbers.
 */
@Composable
fun SkinPrintRadial(
    scores: List<SkinScore>,
    overall: Int,
    band: ScoreBand,
    modifier: Modifier = Modifier,
    size: Dp = 264.dp,
    previous: Map<SkinPrintDimension, Int>? = null,
) {
    val colors = SkinthesiaTheme.colors
    val typography = SkinthesiaTheme.typography
    val density = LocalDensity.current
    var selected by remember { mutableStateOf<SkinPrintDimension?>(null) }

    val dimensions = SkinPrintDimension.entries
    val reveal = rememberReveal(1f, key = scores, durationMillis = 1100, delayMillis = 150).value

    val ordered = remember(scores) { dimensions.mapNotNull { d -> scores.firstOrNull { it.dimension == d } } }

    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Box(modifier = Modifier.size(size), contentAlignment = Alignment.Center) {
            val labelRadiusDp = size / 2 - 20.dp
            val gridRadiusDp = labelRadiusDp - 34.dp

            Canvas(
                modifier = Modifier
                    .size(size)
                    .semantics {
                        contentDescription = "SkinPrint by dimension: " +
                            ordered.joinToString { "${it.dimension.label} ${it.value}" }
                    },
            ) {
                val center = this.center
                val gridR = gridRadiusDp.toPx()
                val n = dimensions.size

                fun vertex(index: Int, fraction: Float): Offset {
                    val angle = Math.toRadians((-90.0 + index * (360.0 / n)))
                    val r = gridR * fraction.coerceIn(0f, 1f)
                    return Offset(center.x + (r * cos(angle)).toFloat(), center.y + (r * sin(angle)).toFloat())
                }

                // Concentric grid rings (25/50/75/100%) as soft pentagons.
                listOf(0.25f, 0.5f, 0.75f, 1f).forEach { frac ->
                    val path = Path()
                    for (i in 0 until n) {
                        val v = vertex(i, frac)
                        if (i == 0) path.moveTo(v.x, v.y) else path.lineTo(v.x, v.y)
                    }
                    path.close()
                    drawPath(path, color = colors.border.copy(alpha = if (frac == 1f) 0.9f else 0.45f), style = Stroke(1.dp.toPx()))
                }
                // Axis spokes.
                for (i in 0 until n) {
                    drawLine(colors.border.copy(alpha = 0.5f), center, vertex(i, 1f), strokeWidth = 1.dp.toPx())
                }

                // The data shape itself, unfurling from the centre.
                val fractions = ordered.map { (it.value / 100f) * reveal }
                if (fractions.size == n) {
                    val dataPath = Path()
                    fractions.forEachIndexed { i, f ->
                        val v = vertex(i, f)
                        if (i == 0) dataPath.moveTo(v.x, v.y) else dataPath.lineTo(v.x, v.y)
                    }
                    dataPath.close()
                    drawPath(
                        dataPath,
                        brush = Brush.radialGradient(
                            listOf(colors.primary.copy(alpha = 0.32f), colors.accentBlushDeep.copy(alpha = 0.14f)),
                            center = center,
                            radius = gridR * 1.05f,
                        ),
                    )
                    drawPath(dataPath, color = colors.primary.copy(alpha = 0.85f), style = Stroke(2.2.dp.toPx(), join = StrokeJoin.Round, cap = StrokeCap.Round))
                    fractions.forEachIndexed { i, f ->
                        val v = vertex(i, f)
                        val isSelected = ordered[i].dimension == selected
                        drawCircle(colors.surfaceElevated, radius = (if (isSelected) 6f else 4.2f).dp.toPx(), center = v)
                        drawCircle(
                            colors.primary,
                            radius = (if (isSelected) 6f else 4.2f).dp.toPx(),
                            center = v,
                            style = Stroke((if (isSelected) 2.4f else 1.8f).dp.toPx()),
                        )
                        if (isSelected) drawCircle(colors.primary.copy(alpha = 0.16f), radius = 13.dp.toPx(), center = v)
                    }
                }
            }

            // Interactive dimension nodes sit at the outer edge, a fixed and always
            // reachable touch target independent of how small a value's own point is.
            dimensions.forEachIndexed { i, dimension ->
                val angle = Math.toRadians((-90.0 + i * (360.0 / dimensions.size)))
                val rPx = with(density) { labelRadiusDp.toPx() }
                val dx = (rPx * cos(angle)).roundToInt()
                val dy = (rPx * sin(angle)).roundToInt()
                val score = ordered.getOrNull(i)
                val isSelected = selected == dimension
                Box(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .offset { IntOffset(dx, dy) },
                    contentAlignment = Alignment.Center,
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            modifier = Modifier
                                .size(if (isSelected) 34.dp else 30.dp)
                                .clip(CircleShape)
                                .background(if (isSelected) colors.primary else colors.surfaceElevated)
                                .border(
                                    if (isSelected) 0.dp else SkinthesiaTheme.spacing.borderThin,
                                    colors.border,
                                    CircleShape,
                                )
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null,
                                    role = Role.Button,
                                    onClick = { selected = if (selected == dimension) null else dimension },
                                ),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                dimension.icon(),
                                contentDescription = dimension.label,
                                tint = if (isSelected) colors.textOnPrimary else colors.textSecondary,
                                modifier = Modifier.size(if (isSelected) 16.dp else 14.dp),
                            )
                        }
                        if (score != null) {
                            Spacer(Modifier.height(3.dp))
                            Text(text = score.value.toString(), style = typography.labelSmall, color = if (isSelected) colors.textPrimary else colors.textMuted)
                        }
                    }
                }
            }

            if (selected == null) {
                // A value near 100 on a dimension pulls that vertex out, but a low
                // value elsewhere can pull an adjacent vertex in close enough to
                // cross behind the centre numeral - a soft backing disc keeps the
                // reading clear regardless of the data's shape. Fixed, conservative
                // sizes: legible and fully contained at every chart size this is
                // actually used at (small hero cards through the full detail screen).
                Box(
                    modifier = Modifier.size(70.dp).clip(CircleShape).background(colors.surface.copy(alpha = 0.94f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = overall.toString(),
                            style = typography.scoreNumeral.copy(fontSize = 32.sp, lineHeight = 34.sp),
                            color = colors.textPrimary,
                        )
                        Text(text = band.label, style = typography.metricUnit.copy(fontSize = 9.sp), color = colors.textMuted)
                    }
                }
            }
        }

        AnimatedVisibility(
            visible = selected != null,
            enter = fadeIn(tween(220)) + expandVertically(tween(220, easing = FastOutSlowInEasing)),
            exit = fadeOut(tween(140)) + shrinkVertically(tween(140)),
        ) {
            val dimension = selected
            val score = dimension?.let { d -> ordered.firstOrNull { it.dimension == d } }
            if (dimension != null && score != null) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(top = 14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = dimension.label, style = typography.titleSmall, color = colors.textPrimary, textAlign = TextAlign.Center)
                        Spacer(Modifier.width(8.dp))
                        Text(text = score.value.toString(), style = typography.metric, color = colors.primary)
                        val prev = previous?.get(dimension)
                        if (prev != null && prev != score.value) {
                            Spacer(Modifier.width(6.dp))
                            val delta = (score.value - prev).toDouble()
                            DeltaBadge(delta = delta, improved = delta > 0)
                        }
                    }
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = score.explanation,
                        style = typography.bodySmall,
                        color = colors.textSecondary,
                        textAlign = TextAlign.Center,
                    )
                }
            }
        }
    }
}
