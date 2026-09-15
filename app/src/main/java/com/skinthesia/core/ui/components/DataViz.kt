package com.skinthesia.core.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.skinthesia.core.design.SkinthesiaTheme
import com.skinthesia.core.ui.icons.SkinthesiaIcons
import java.util.Locale
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

/** Animates from 0 to [target] once per [key]; instant when reduced motion is on. */
@Composable
fun rememberReveal(target: Float, key: Any? = target, durationMillis: Int = 1200, delayMillis: Int = 0): Animatable<Float, *> {
    val motion = SkinthesiaTheme.motion
    val anim = remember(key) { Animatable(if (motion.reducedMotion) target else 0f) }
    LaunchedEffect(key, target) {
        anim.animateTo(target, tween(motion.duration(durationMillis), motion.duration(delayMillis), motion.enterEasing))
    }
    return anim
}

/**
 * The SkinPrint ring: a clay-to-blush arc that sweeps to the score while the numeral
 * counts up. Tick marks every ten points keep it scientific without clutter.
 */
@Composable
fun ScoreRing(
    score: Int,
    modifier: Modifier = Modifier,
    size: Dp = 210.dp,
    strokeWidth: Dp = 10.dp,
    caption: String? = "/ 100",
    label: String? = null,
    animate: Boolean = true,
    delayMillis: Int = 200,
    /** Overrides the default numeral/caption/label column when supplied. */
    centerContent: (@Composable () -> Unit)? = null,
) {
    val colors = SkinthesiaTheme.colors
    val typography = SkinthesiaTheme.typography
    val progress = if (animate) rememberReveal(score / 100f, key = score, durationMillis = 1500, delayMillis = delayMillis).value else score / 100f
    val shown = (progress * 100).roundToInt()
    Box(
        modifier = modifier
            .size(size)
            .clearAndSetSemantics { contentDescription = "SkinPrint $score out of 100" + (label?.let { ", $it" } ?: "") },
        contentAlignment = Alignment.Center,
    ) {
        Canvas(Modifier.fillMaxSize()) {
            val stroke = strokeWidth.toPx()
            val inset = stroke / 2 + 10.dp.toPx()
            val arcSize = Size(this.size.width - inset * 2, this.size.height - inset * 2)
            val topLeft = Offset(inset, inset)
            val radius = arcSize.width / 2
            val center = this.center
            for (i in 0 until 50) {
                val angle = Math.toRadians(i * 7.2 - 90.0)
                val outer = radius + stroke / 2 + 7.dp.toPx()
                val len = if (i % 5 == 0) 5.dp.toPx() else 2.5.dp.toPx()
                drawLine(
                    color = colors.borderStrong.copy(alpha = if (i % 5 == 0) 0.9f else 0.55f),
                    start = Offset(center.x + (outer) * cos(angle).toFloat(), center.y + (outer) * sin(angle).toFloat()),
                    end = Offset(center.x + (outer + len) * cos(angle).toFloat(), center.y + (outer + len) * sin(angle).toFloat()),
                    strokeWidth = 1.dp.toPx(),
                    cap = StrokeCap.Round,
                )
            }
            drawArc(colors.track, 0f, 360f, false, topLeft, arcSize, style = Stroke(stroke))
            rotate(-90f, center) {
                drawArc(
                    brush = Brush.sweepGradient(
                        0f to colors.accentBlushDeep,
                        progress.coerceAtLeast(0.01f) to colors.primary,
                        1f to colors.primary,
                        center = center,
                    ),
                    startAngle = 0f,
                    sweepAngle = 360f * progress,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(stroke, cap = StrokeCap.Round),
                )
            }
            if (progress > 0.01f) {
                val end = Math.toRadians(360.0 * progress - 90.0)
                val dot = Offset(center.x + radius * cos(end).toFloat(), center.y + radius * sin(end).toFloat())
                drawCircle(colors.primary.copy(alpha = 0.18f), radius = stroke * 1.3f, center = dot)
                drawCircle(colors.surfaceElevated, radius = stroke * 0.32f, center = dot)
            }
        }
        if (centerContent != null) {
            centerContent()
        } else {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = shown.toString(), style = typography.scoreNumeral, color = colors.textPrimary)
                if (caption != null) Text(text = caption, style = typography.metricUnit, color = colors.textMuted)
                if (label != null) {
                    Spacer(Modifier.height(10.dp))
                    Tag(text = label, tone = TagTone.SAGE)
                }
            }
        }
    }
}

/** Small ring with the value in the middle, for dimension grids. */
@Composable
fun MiniRing(
    value: Int,
    modifier: Modifier = Modifier,
    size: Dp = 52.dp,
    color: Color = SkinthesiaTheme.colors.primary,
    delayMillis: Int = 0,
    /** Replaces the number in the centre, for example "2/4". */
    label: String? = null,
) {
    val colors = SkinthesiaTheme.colors
    val progress = rememberReveal(value / 100f, key = value, durationMillis = 1000, delayMillis = delayMillis).value
    Box(modifier = modifier.size(size), contentAlignment = Alignment.Center) {
        Canvas(Modifier.fillMaxSize()) {
            val stroke = 3.5.dp.toPx()
            val inset = stroke / 2
            val arcSize = Size(this.size.width - stroke, this.size.height - stroke)
            drawArc(colors.track, 0f, 360f, false, Offset(inset, inset), arcSize, style = Stroke(stroke))
            drawArc(color, -90f, 360f * progress, false, Offset(inset, inset), arcSize, style = Stroke(stroke, cap = StrokeCap.Round))
        }
        Text(text = label ?: (progress * 100).roundToInt().toString(), style = SkinthesiaTheme.typography.numeric, color = colors.textPrimary)
    }
}

/** Label, value and an animated hairline bar. */
@Composable
fun MetricBar(
    label: String,
    value: Float,
    modifier: Modifier = Modifier,
    max: Float = 100f,
    valueText: String = value.roundToInt().toString(),
    color: Color = SkinthesiaTheme.colors.primary,
    caption: String? = null,
    delayMillis: Int = 0,
    trailing: (@Composable () -> Unit)? = null,
) {
    val colors = SkinthesiaTheme.colors
    val typography = SkinthesiaTheme.typography
    val fraction = rememberReveal((value / max).coerceIn(0f, 1f), key = value, durationMillis = 900, delayMillis = delayMillis).value
    Column(modifier = modifier.fillMaxWidth().semantics(mergeDescendants = true) {}) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(text = label, style = typography.label, color = colors.textPrimary, modifier = Modifier.weight(1f))
            if (trailing != null) {
                trailing()
                Spacer(Modifier.width(8.dp))
            }
            Text(text = valueText, style = typography.numeric, color = colors.textPrimary)
        }
        Spacer(Modifier.height(8.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(SkinthesiaTheme.shapes.pill)
                .background(colors.track),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(fraction)
                    .height(6.dp)
                    .clip(SkinthesiaTheme.shapes.pill)
                    .background(Brush.horizontalGradient(listOf(color.copy(alpha = 0.55f), color))),
            )
        }
        if (caption != null) {
            Spacer(Modifier.height(6.dp))
            Text(text = caption, style = typography.caption, color = colors.textMuted)
        }
    }
}

/**
 * Current value, a potential range and an interactive projection marker on one track.
 * The range is drawn as a soft band to read as a goal, never as a promise.
 */
@Composable
fun PotentialRangeBar(
    current: Int,
    low: Int,
    high: Int,
    modifier: Modifier = Modifier,
    projected: Int = current,
) {
    val colors = SkinthesiaTheme.colors
    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(18.dp)
            .clearAndSetSemantics { contentDescription = "Current $current, potential range $low to $high" },
    ) {
        val trackH = 6.dp.toPx()
        val y = size.height / 2
        val w = size.width
        fun x(v: Int) = w * (v / 100f)
        drawRoundRect(colors.track, Offset(0f, y - trackH / 2), Size(w, trackH), CornerRadius(trackH))
        // The potential range rests softly on the track; today's value is the solid fill.
        drawRoundRect(colors.primarySoft.copy(alpha = 0.6f), Offset(x(low), y - trackH / 2), Size((x(high) - x(low)).coerceAtLeast(trackH), trackH), CornerRadius(trackH))
        drawRoundRect(colors.primary, Offset(0f, y - trackH / 2), Size(x(current), trackH), CornerRadius(trackH))
        // A slim tick marks the projected position, so the bar never reads as a draggable control.
        if (projected != current) {
            drawLine(colors.primary, Offset(x(projected), y - 6.dp.toPx()), Offset(x(projected), y + 6.dp.toPx()), strokeWidth = 2.dp.toPx(), cap = StrokeCap.Round)
        }
    }
}

data class ChartPoint(val label: String, val value: Float)

/**
 * Smooth trend line with a soft area fill, value labels and optional upcoming
 * (not yet measured) slots drawn as hollow dashed markers. Set [selectable] to
 * let the user tap a point and reveal its change since the previous one below
 * the chart - the value/label are already always visible on the line itself.
 */
@Composable
fun TrendChart(
    points: List<ChartPoint>,
    modifier: Modifier = Modifier,
    upcomingLabels: List<String> = emptyList(),
    minValue: Float? = null,
    maxValue: Float? = null,
    lineColor: Color = SkinthesiaTheme.colors.primary,
    height: Dp = 180.dp,
    valueDecimals: Int = 0,
    selectable: Boolean = false,
) {
    val colors = SkinthesiaTheme.colors
    val typography = SkinthesiaTheme.typography
    val measurer = rememberTextMeasurer()
    val reveal = rememberReveal(1f, key = points, durationMillis = 1200).value
    val description = points.joinToString { "${it.label} ${fmt(it.value, valueDecimals)}" }
    var selected by remember(points) { mutableStateOf<Int?>(null) }

    Column(modifier = modifier.fillMaxWidth()) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(height)
                .then(
                    if (selectable && points.isNotEmpty()) {
                        Modifier.pointerInput(points) {
                            detectTapGestures { offset ->
                                val slots = points.size + upcomingLabels.size
                                val sidePad = 18.dp.toPx()
                                fun x(i: Int) = if (slots == 1) size.width / 2f else sidePad + (size.width - sidePad * 2) * i / (slots - 1)
                                val nearest = points.indices.minByOrNull { abs(x(it) - offset.x) }
                                selected = if (selected == nearest) null else nearest
                            }
                        }
                    } else {
                        Modifier
                    },
                )
                .clearAndSetSemantics { contentDescription = "Trend: $description" },
        ) {
            if (points.isEmpty()) return@Canvas
            val slots = points.size + upcomingLabels.size
            val labelBand = 22.dp.toPx()
            val valueBand = 20.dp.toPx()
            val sidePad = 18.dp.toPx()
            val top = valueBand
            val bottom = size.height - labelBand
            val values = points.map { it.value }
            val lo = minValue ?: (values.min() - 8f).coerceAtLeast(0f)
            val hi = maxValue ?: (values.max() + 6f).coerceAtMost(100f).coerceAtLeast(lo + 1f)
            fun x(i: Int) = if (slots == 1) size.width / 2 else sidePad + (size.width - sidePad * 2) * i / (slots - 1)
            fun y(v: Float) = bottom - (bottom - top) * ((v - lo) / (hi - lo))

            for (g in 0..3) {
                val gy = top + (bottom - top) * g / 3f
                drawLine(colors.divider, Offset(0f, gy), Offset(size.width, gy), 1.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 8f)))
            }

            val coords = points.mapIndexed { i, p -> Offset(x(i), y(p.value)) }
            val line = Path().apply {
                moveTo(coords.first().x, coords.first().y)
                for (i in 1 until coords.size) {
                    val p0 = coords[i - 1]
                    val p1 = coords[i]
                    val cx = (p0.x + p1.x) / 2
                    cubicTo(cx, p0.y, cx, p1.y, p1.x, p1.y)
                }
            }
            val area = Path().apply {
                addPath(line)
                lineTo(coords.last().x, bottom)
                lineTo(coords.first().x, bottom)
                close()
            }
            clipRect(right = size.width * reveal) {
                drawPath(area, Brush.verticalGradient(listOf(lineColor.copy(alpha = 0.18f), lineColor.copy(alpha = 0f)), startY = top, endY = bottom))
                drawPath(line, lineColor, style = Stroke(2.dp.toPx(), cap = StrokeCap.Round))
            }

            if (selected != null) {
                val sc = coords[selected!!]
                drawLine(lineColor.copy(alpha = 0.3f), Offset(sc.x, top), Offset(sc.x, bottom), strokeWidth = 1.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(4f, 4f)))
            }

            coords.forEachIndexed { i, c ->
                val last = i == coords.lastIndex
                val isSelected = selected == i
                if (last) drawCircle(lineColor.copy(alpha = 0.16f), radius = 11.dp.toPx(), center = c)
                if (isSelected) drawCircle(lineColor.copy(alpha = 0.22f), radius = 13.dp.toPx(), center = c)
                drawCircle(colors.surfaceElevated, radius = if (last || isSelected) 5.5.dp.toPx() else 4.dp.toPx(), center = c)
                drawCircle(lineColor, radius = if (last || isSelected) 5.5.dp.toPx() else 4.dp.toPx(), center = c, style = Stroke(2.dp.toPx()))
                val valueLayout = measurer.measure(fmt(points[i].value, valueDecimals), typography.numeric.copy(fontSize = 11.sp, color = colors.textPrimary))
                drawText(valueLayout, topLeft = Offset(c.x - valueLayout.size.width / 2f, c.y - valueLayout.size.height - 8.dp.toPx()))
            }
            upcomingLabels.forEachIndexed { j, _ ->
                val c = Offset(x(points.size + j), bottom - 6.dp.toPx())
                drawCircle(colors.borderStrong, radius = 4.dp.toPx(), center = c, style = Stroke(1.2.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(3f, 3f))))
            }
            (points.map { it.label } + upcomingLabels).forEachIndexed { i, label ->
                val upcoming = i >= points.size
                val layout = measurer.measure(label, typography.caption.copy(color = if (upcoming) colors.textMuted.copy(alpha = 0.7f) else colors.textSecondary, textAlign = TextAlign.Center))
                drawText(layout, topLeft = Offset(x(i) - layout.size.width / 2f, bottom + 6.dp.toPx()))
            }
        }

        if (selectable) {
            AnimatedVisibility(
                visible = selected != null,
                enter = fadeIn(tween(200)) + expandVertically(tween(200, easing = FastOutSlowInEasing)),
                exit = fadeOut(tween(120)) + shrinkVertically(tween(120)),
            ) {
                val i = selected
                if (i != null) {
                    val point = points[i]
                    val prev = points.getOrNull(i - 1)
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(top = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column(Modifier.weight(1f)) {
                            Text(text = point.label, style = typography.labelSmall, color = colors.textMuted)
                            Text(text = fmt(point.value, valueDecimals), style = typography.metric, color = colors.textPrimary)
                        }
                        if (prev != null) {
                            val delta = (point.value - prev.value).toDouble()
                            Column(horizontalAlignment = Alignment.End) {
                                DeltaBadge(delta = delta, improved = if (abs(delta) < 0.05) null else delta > 0, decimals = valueDecimals)
                                Spacer(Modifier.height(3.dp))
                                Text(text = "since ${prev.label}", style = typography.caption, color = colors.textMuted)
                            }
                        } else {
                            Text(text = "First recorded value", style = typography.caption, color = colors.textMuted)
                        }
                    }
                }
            }
        }
    }
}

/** "+8" in sage, "−4" in rose, "±0" neutral; direction-aware and honest for neutral metrics. */
@Composable
fun DeltaBadge(
    delta: Double,
    improved: Boolean?,
    modifier: Modifier = Modifier,
    decimals: Int = 0,
    suffix: String = "",
) {
    val sign = when {
        delta > 0.0001 -> "+"
        delta < -0.0001 -> "−"
        else -> "±"
    }
    val tone = when (improved) {
        true -> TagTone.SAGE
        false -> TagTone.ROSE
        null -> TagTone.NEUTRAL
    }
    Tag(
        text = sign + fmt(abs(delta).toFloat(), decimals) + suffix,
        tone = tone,
        icon = when {
            delta > 0.0001 -> SkinthesiaIcons.Trend
            delta < -0.0001 -> SkinthesiaIcons.TrendDown
            else -> null
        },
        modifier = modifier,
    )
}

/** Four-segment meter for visibility levels (minimal → noticeable). */
@Composable
fun SegmentMeter(
    filled: Int,
    modifier: Modifier = Modifier,
    total: Int = 4,
    color: Color = SkinthesiaTheme.colors.primary,
) {
    val colors = SkinthesiaTheme.colors
    Row(modifier = modifier, horizontalArrangement = Arrangement.spacedBy(3.dp)) {
        repeat(total) { i ->
            Box(
                modifier = Modifier
                    .width(14.dp)
                    .height(5.dp)
                    .clip(SkinthesiaTheme.shapes.pill)
                    .background(if (i < filled) color else colors.track),
            )
        }
    }
}

/** Before and after bars on a shared scale. */
@Composable
fun ComparisonBars(
    from: Float,
    to: Float,
    fromLabel: String,
    toLabel: String,
    modifier: Modifier = Modifier,
    max: Float = 100f,
    decimals: Int = 0,
) {
    val colors = SkinthesiaTheme.colors
    val typography = SkinthesiaTheme.typography
    val a = rememberReveal((from / max).coerceIn(0f, 1f), key = from, durationMillis = 800).value
    val b = rememberReveal((to / max).coerceIn(0f, 1f), key = to, durationMillis = 1000, delayMillis = 200).value
    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(6.dp)) {
        listOf(Triple(fromLabel, a, colors.borderStrong) to from, Triple(toLabel, b, colors.primary) to to).forEach { (spec, raw) ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = spec.first, style = typography.caption, color = colors.textMuted, modifier = Modifier.width(56.dp))
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(8.dp)
                        .clip(SkinthesiaTheme.shapes.pill)
                        .background(colors.track),
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(spec.second)
                            .height(8.dp)
                            .clip(SkinthesiaTheme.shapes.pill)
                            .background(spec.third),
                    )
                }
                Spacer(Modifier.width(10.dp))
                Text(text = fmt(raw, decimals), style = typography.numeric, color = colors.textPrimary, modifier = Modifier.width(40.dp), textAlign = TextAlign.End)
            }
        }
    }
}

/** Small legend row: coloured dot and label. */
@Composable
fun LegendDot(label: String, color: Color, modifier: Modifier = Modifier) {
    Row(modifier = modifier.padding(vertical = 2.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(8.dp).clip(SkinthesiaTheme.shapes.pill).background(color))
        Spacer(Modifier.width(6.dp))
        Text(text = label, style = SkinthesiaTheme.typography.caption, color = SkinthesiaTheme.colors.textSecondary)
    }
}

fun fmt(value: Float, decimals: Int): String = String.format(Locale.US, "%.${decimals}f", value)
fun fmt(value: Double, decimals: Int): String = String.format(Locale.US, "%.${decimals}f", value)
