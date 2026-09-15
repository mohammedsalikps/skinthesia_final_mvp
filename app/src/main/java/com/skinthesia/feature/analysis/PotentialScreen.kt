package com.skinthesia.feature.analysis

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Slider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.skinthesia.ai.skinprint.PotentialEstimator
import com.skinthesia.core.design.SkinthesiaTheme
import com.skinthesia.core.navigation.LocalAppNavigator
import com.skinthesia.core.navigation.PotentialRoute
import com.skinthesia.core.navigation.SkinPrintRoute
import com.skinthesia.core.navigation.containerViewModel
import com.skinthesia.core.ui.components.FadeInUp
import com.skinthesia.core.ui.components.LoadingState
import com.skinthesia.core.ui.components.PotentialRangeBar
import com.skinthesia.core.ui.components.ScreenHeader
import com.skinthesia.core.ui.components.SectionHeader
import com.skinthesia.core.ui.components.SectionOverline
import com.skinthesia.core.ui.components.SkinthesiaCard
import com.skinthesia.core.ui.components.SkinthesiaDivider
import com.skinthesia.core.ui.components.SkinthesiaPrimaryButton
import com.skinthesia.core.ui.components.SkinthesiaScreen
import com.skinthesia.core.ui.components.SkinthesiaTopBar
import com.skinthesia.core.ui.components.SkinthesiaImage
import com.skinthesia.core.ui.components.Tag
import com.skinthesia.core.ui.components.TagTone
import com.skinthesia.core.ui.icons.SkinthesiaIcons
import com.skinthesia.domain.model.ImageSource
import com.skinthesia.domain.model.PotentialDimension
import com.skinthesia.domain.model.PotentialState
import com.skinthesia.domain.repository.AssessmentRepository
import com.skinthesia.domain.repository.UserProfileRepository
import com.skinthesia.feature.onboarding.GoalStatements
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

data class PotentialUiState(
    val loading: Boolean = true,
    val potential: PotentialState? = null,
    val statement: String = "",
    val focus: List<String> = emptyList(),
    /** The user's own Week 1 photo - the only real image in the projection; every later week is a filter over it. */
    val photoPath: String? = null,
)

class PotentialViewModel(
    val assessmentId: String,
    val onboarding: Boolean,
    private val assessments: AssessmentRepository,
    private val profiles: UserProfileRepository,
    private val estimator: PotentialEstimator,
) : ViewModel() {

    private val _state = MutableStateFlow(PotentialUiState())
    val state: StateFlow<PotentialUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            val assessment = assessments.get(assessmentId)
            val skinPrint = assessment?.skinPrint
            val profile = profiles.current()
            _state.value = PotentialUiState(
                loading = false,
                potential = skinPrint?.let { estimator.estimate(it, profile.goals) },
                statement = profile.goals.statement.takeIf { it.isNotBlank() } ?: GoalStatements.DEFAULT,
                focus = profile.goals.ranked.take(3).map { it.label },
                photoPath = assessment?.photo?.filePath,
            )
        }
    }
}

/** The potential range at [week], interpolated linearly from today to the horizon. */
fun PotentialState.rangeAt(week: Int): Pair<Int, Int> {
    val t = week.coerceIn(0, horizonWeeks).toFloat() / horizonWeeks
    val low = currentOverall + ((potentialLow - currentOverall) * t).roundToInt()
    val high = currentOverall + ((potentialHigh - currentOverall) * t).roundToInt()
    return low to high
}

/** Screen 20: today against an interactive, clearly non-guaranteed potential range. */
@Composable
fun PotentialScreen() {
    val navigator = LocalAppNavigator.current
    val viewModel = containerViewModel { handle ->
        val route = handle.toRoute<PotentialRoute>()
        PotentialViewModel(route.assessmentId, route.onboarding, assessments, profiles, potentialEstimator)
    }
    val state by viewModel.state.collectAsStateWithLifecycle()
    val reduced = SkinthesiaTheme.motion.reducedMotion
    val potential = state.potential
    var week by rememberSaveable { mutableIntStateOf(0) }
    var introPlayed by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(potential) {
        val p = potential ?: return@LaunchedEffect
        if (!introPlayed) {
            introPlayed = true
            if (reduced) {
                week = p.horizonWeeks
            } else {
                delay(600)
                for (w in 1..p.horizonWeeks) {
                    week = w
                    delay(80)
                }
            }
        }
    }

    SkinthesiaScreen(
        topBar = { SkinthesiaTopBar(title = "Your potential", onBack = navigator::back) },
        bottomBar = {
            SkinthesiaPrimaryButton(
                text = "See My SkinPrint",
                onClick = { navigator.navigate(SkinPrintRoute(viewModel.assessmentId, viewModel.onboarding)) },
                enabled = potential != null,
            )
        },
    ) {
        when {
            state.loading -> LoadingState(message = "Estimating your potential")
            potential == null -> ScreenHeader(
                title = "Your potential appears after your first analysis",
                subtitle = "Complete a selfie analysis to see where consistent care could take you.",
                centered = true,
            )
            else -> PotentialContent(potential = potential, statement = state.statement, photoPath = state.photoPath, week = week, onWeek = { week = it })
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PotentialContent(potential: PotentialState, statement: String, photoPath: String?, week: Int, onWeek: (Int) -> Unit) {
    val colors = SkinthesiaTheme.colors
    val typography = SkinthesiaTheme.typography
    val spacing = SkinthesiaTheme.spacing
    val motion = SkinthesiaTheme.motion
    val (low, high) = potential.rangeAt(week)

    FadeInUp {
        Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            SectionOverline(text = "Your ${potential.horizonWeeks}-week goal")
            Spacer(Modifier.height(10.dp))
            Text(text = "“$statement”", style = typography.displayItalic, color = colors.primary, textAlign = TextAlign.Center)
        }
    }
    Spacer(Modifier.height(spacing.lg))
    FadeInUp(delayMillis = motion.stagger(1)) { CompareHeader(current = potential.currentOverall, low = low, high = high, week = week) }
    if (photoPath != null) {
        Spacer(Modifier.height(spacing.md))
        FadeInUp(delayMillis = motion.stagger(2)) {
            VisualProjectionCard(photoPath = photoPath, horizon = potential.horizonWeeks, week = week, onWeek = onWeek)
        }
    }
    Spacer(Modifier.height(spacing.md))
    FadeInUp(delayMillis = motion.stagger(3)) {
        SkinthesiaCard {
            PotentialChart(potential = potential, week = week, onWeek = onWeek)
            Spacer(Modifier.height(4.dp))
            Slider(
                value = week.toFloat(),
                onValueChange = { onWeek(it.roundToInt()) },
                valueRange = 0f..potential.horizonWeeks.toFloat(),
                steps = (potential.horizonWeeks - 1).coerceAtLeast(0),
                thumb = { WeekThumb() },
                track = { sliderState -> WeekTrack(sliderState.value / potential.horizonWeeks.coerceAtLeast(1)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .semantics { stateDescription = if (week == 0) "Today, SkinPrint ${potential.currentOverall}" else "Week $week, potential $low to $high" },
            )
            Text(
                text = "Drag through the weeks to explore your range.",
                style = typography.caption,
                color = colors.textMuted,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
    Spacer(Modifier.height(spacing.md))
    AssumptionsCard(potential.assumptions)
    Spacer(Modifier.height(spacing.lg))
    SectionHeader(title = "By dimension", overline = "Where change could come from")
    Spacer(Modifier.height(12.dp))
    SkinthesiaCard {
        potential.dimensions.forEachIndexed { index, dimension ->
            DimensionPotentialRow(dimension, week = week, horizon = potential.horizonWeeks)
            if (index < potential.dimensions.lastIndex) SkinthesiaDivider()
        }
    }
}

@Composable
private fun CompareHeader(current: Int, low: Int, high: Int, week: Int) {
    val colors = SkinthesiaTheme.colors
    val typography = SkinthesiaTheme.typography
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f).semantics(mergeDescendants = true) {}, horizontalAlignment = Alignment.CenterHorizontally) {
            SectionOverline(text = "Today")
            Spacer(Modifier.height(6.dp))
            Text(text = "$current", style = typography.metricLarge, color = colors.textPrimary)
            Text(text = "SkinPrint", style = typography.caption, color = colors.textMuted)
        }
        Icon(SkinthesiaIcons.ArrowRight, contentDescription = null, tint = colors.textMuted, modifier = Modifier.size(20.dp))
        Column(Modifier.weight(1f).semantics(mergeDescendants = true) {}, horizontalAlignment = Alignment.CenterHorizontally) {
            SectionOverline(text = if (week == 0) "Today" else "Week $week", color = colors.primary)
            Spacer(Modifier.height(6.dp))
            Text(text = if (low == high) "$low" else "$low–$high", style = typography.metricLarge, color = colors.primary)
            Text(text = "potential range", style = typography.caption, color = colors.textMuted)
        }
    }
}

@Composable
private fun PotentialChart(potential: PotentialState, week: Int, onWeek: (Int) -> Unit) {
    val colors = SkinthesiaTheme.colors
    val measurer = rememberTextMeasurer()
    val labelStyle = SkinthesiaTheme.typography.caption.copy(color = colors.textMuted)
    val horizon = potential.horizonWeeks.coerceAtLeast(1)
    val animatedWeek by animateFloatAsState(week.toFloat(), tween(220), label = "chartWeek")
    val latestOnWeek by rememberUpdatedState(onWeek)
    val leftDp = 30.dp
    val rightDp = 14.dp

    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(196.dp)
            .pointerInput(horizon) {
                detectTapGestures { offset ->
                    val left = leftDp.toPx()
                    val span = size.width - left - rightDp.toPx()
                    latestOnWeek(((offset.x - left) / span * horizon).roundToInt().coerceIn(0, horizon))
                }
            }
            .pointerInput(horizon) {
                detectHorizontalDragGestures { change, _ ->
                    change.consume()
                    val left = leftDp.toPx()
                    val span = size.width - left - rightDp.toPx()
                    latestOnWeek(((change.position.x - left) / span * horizon).roundToInt().coerceIn(0, horizon))
                }
            }
            .clearAndSetSemantics { contentDescription = "Chart of your SkinPrint potential range over $horizon weeks" },
    ) {
        val left = leftDp.toPx()
        val right = rightDp.toPx()
        val top = 12.dp.toPx()
        val bottom = 26.dp.toPx()
        val cw = size.width - left - right
        val ch = size.height - top - bottom
        val c = potential.currentOverall.toFloat()
        val lo = potential.potentialLow.toFloat()
        val hi = potential.potentialHigh.toFloat()
        val yMin = (c - 8f).coerceAtLeast(0f)
        val yMax = (hi + 6f).coerceAtMost(100f).coerceAtLeast(yMin + 1f)
        fun x(w: Float) = left + cw * w / horizon
        fun y(v: Float) = top + ch * (1f - (v - yMin) / (yMax - yMin))
        val dash = PathEffect.dashPathEffect(floatArrayOf(4.dp.toPx(), 5.dp.toPx()))

        listOf(yMin, (yMin + yMax) / 2f, yMax).forEach { v ->
            drawLine(colors.divider, Offset(left, y(v)), Offset(left + cw, y(v)), strokeWidth = 1.dp.toPx(), pathEffect = dash)
            val layout = measurer.measure(v.roundToInt().toString(), labelStyle)
            drawText(layout, topLeft = Offset(0f, y(v) - layout.size.height / 2f))
        }

        val end = horizon.toFloat()
        val band = Path().apply {
            moveTo(x(0f), y(c))
            lineTo(x(end), y(hi))
            lineTo(x(end), y(lo))
            close()
        }
        drawPath(
            band,
            Brush.horizontalGradient(
                listOf(colors.primarySoft.copy(alpha = 0.12f), colors.primarySoft.copy(alpha = 0.6f)),
                startX = left,
                endX = left + cw,
            ),
        )
        drawLine(colors.primary.copy(alpha = 0.35f), Offset(x(0f), y(c)), Offset(x(end), y(hi)), strokeWidth = 1.dp.toPx())
        drawLine(colors.primary.copy(alpha = 0.35f), Offset(x(0f), y(c)), Offset(x(end), y(lo)), strokeWidth = 1.dp.toPx())
        drawLine(colors.primary, Offset(x(0f), y(c)), Offset(x(end), y((lo + hi) / 2f)), strokeWidth = 1.5.dp.toPx(), pathEffect = dash)
        drawCircle(colors.textPrimary, radius = 4.dp.toPx(), center = Offset(x(0f), y(c)))

        val wk = animatedWeek
        val t = wk / horizon
        val loW = c + (lo - c) * t
        val hiW = c + (hi - c) * t
        drawLine(colors.borderStrong, Offset(x(wk), top), Offset(x(wk), top + ch), strokeWidth = 1.dp.toPx())
        drawLine(colors.primary, Offset(x(wk), y(hiW)), Offset(x(wk), y(loW)), strokeWidth = 4.dp.toPx(), cap = StrokeCap.Round)
        val marker = Offset(x(wk), y((loW + hiW) / 2f))
        drawCircle(colors.surfaceElevated, radius = 6.dp.toPx(), center = marker)
        drawCircle(colors.primary, radius = 6.dp.toPx(), center = marker, style = Stroke(2.dp.toPx()))

        listOf(0, horizon / 3, horizon * 2 / 3, horizon).distinct().forEach { w ->
            val text = if (w == 0) "Today" else "Week $w"
            val layout = measurer.measure(text, labelStyle)
            val lx = (x(w.toFloat()) - layout.size.width / 2f).coerceIn(0f, size.width - layout.size.width)
            drawText(layout, topLeft = Offset(lx, top + ch + 8.dp.toPx()))
        }
    }
}

/**
 * The 12-week illustrative projection: the user's own Week 1 photo, with a
 * purely visual brighten/saturation lift that grows with the selected week.
 * This is presentation over the SAME real image, never a generated photo, and
 * is labelled as such - it must never read as a scientific prediction.
 */
@Composable
private fun VisualProjectionCard(photoPath: String, horizon: Int, week: Int, onWeek: (Int) -> Unit) {
    val colors = SkinthesiaTheme.colors
    val typography = SkinthesiaTheme.typography
    val t = week.coerceIn(0, horizon).toFloat() / horizon.coerceAtLeast(1)
    val animatedT by animateFloatAsState(t, tween(260), label = "projectionT")
    SkinthesiaCard {
        SectionOverline(text = "Your skin journey")
        Spacer(Modifier.height(10.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(260.dp)
                .clip(SkinthesiaTheme.shapes.image)
                .background(colors.surfaceMuted),
        ) {
            SkinthesiaImage(
                source = ImageSource.LocalFile(photoPath),
                contentDescription = if (week == 0) "Your Day 1 photo" else "Illustrative projection at week $week",
                modifier = Modifier.fillMaxSize(),
                maxDimension = 900,
                colorFilter = ColorFilter.colorMatrix(projectionMatrix(animatedT)),
            )
            if (week > 0) {
                Tag(
                    text = "Illustrative projection",
                    tone = TagTone.GOLD,
                    dot = true,
                    modifier = Modifier.align(Alignment.TopStart).padding(12.dp),
                )
            }
            Box(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .fillMaxWidth()
                    .background(Brush.verticalGradient(listOf(Color.Transparent, colors.scrim)))
                    .padding(12.dp),
            ) {
                Text(
                    text = if (week == 0) "Day 1 · your actual photo" else "Week $week · a visual simulation, not a guaranteed outcome",
                    style = typography.caption,
                    color = colors.textOnPhoto,
                )
            }
        }
        Spacer(Modifier.height(12.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            PROJECTION_WEEKS.filter { it <= horizon }.distinct().forEach { w ->
                ProjectionWeekChip(week = w, selected = week == w, onClick = { onWeek(w) })
            }
        }
    }
}

private val PROJECTION_WEEKS = listOf(0, 2, 4, 8, 12)

@Composable
private fun ProjectionWeekChip(week: Int, selected: Boolean, onClick: () -> Unit) {
    val colors = SkinthesiaTheme.colors
    val typography = SkinthesiaTheme.typography
    Box(
        modifier = Modifier
            .clip(SkinthesiaTheme.shapes.pill)
            .background(if (selected) colors.primary else colors.surfaceMuted)
            .clickable(role = Role.Button, onClickLabel = if (week == 0) "Week 1" else "Week $week", onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 8.dp),
    ) {
        Text(
            text = if (week == 0) "W1" else "W$week",
            style = typography.labelSmall,
            color = if (selected) colors.textOnPrimary else colors.textSecondary,
        )
    }
}

/** A subtle brighten + saturation lift as [t] (0..1) grows - a visual simulation over the SAME real photo, never a generated one. */
private fun projectionMatrix(t: Float): ColorMatrix {
    val matrix = ColorMatrix().apply { setToSaturation(1f + 0.16f * t) }
    matrix.values[4] += 8f * t
    matrix.values[9] += 6f * t
    matrix.values[14] += 3f * t
    return matrix
}

@Composable
private fun AssumptionsCard(assumptions: List<String>) {
    val colors = SkinthesiaTheme.colors
    val typography = SkinthesiaTheme.typography
    SkinthesiaCard(containerColor = colors.goldSoft, borderColor = Color.Transparent) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(SkinthesiaIcons.Info, contentDescription = null, tint = colors.goldStrong, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(10.dp))
            Text(text = "A potential range, not a promise", style = typography.labelLarge, color = colors.goldStrong)
        }
        Spacer(Modifier.height(8.dp))
        assumptions.forEach { line ->
            Row(Modifier.padding(vertical = 4.dp), verticalAlignment = Alignment.Top) {
                Box(Modifier.padding(top = 7.dp).size(4.dp).clip(CircleShape).background(colors.goldStrong.copy(alpha = 0.7f)))
                Spacer(Modifier.width(10.dp))
                Text(text = line, style = typography.bodySmall, color = colors.goldStrong)
            }
        }
    }
}

@Composable
private fun DimensionPotentialRow(dimension: PotentialDimension, week: Int, horizon: Int) {
    val colors = SkinthesiaTheme.colors
    val typography = SkinthesiaTheme.typography
    val t = week.coerceIn(0, horizon).toFloat() / horizon.coerceAtLeast(1)
    val mid = (dimension.potentialLow + dimension.potentialHigh) / 2f
    val projected = (dimension.current + (mid - dimension.current) * t).roundToInt()
    Column(Modifier.fillMaxWidth().padding(vertical = 12.dp).semantics(mergeDescendants = true) {}) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(dimension.dimension.icon, contentDescription = null, tint = colors.primary, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(8.dp))
            Text(text = dimension.dimension.label, style = typography.labelLarge, color = colors.textPrimary, modifier = Modifier.weight(1f))
            if (dimension.isFocus) {
                Tag(text = "Focus", tone = TagTone.CLAY)
                Spacer(Modifier.width(8.dp))
            }
            Text(
                text = "${dimension.current} → ${dimension.potentialLow}–${dimension.potentialHigh}",
                style = typography.numeric,
                color = colors.textSecondary,
            )
        }
        Spacer(Modifier.height(10.dp))
        PotentialRangeBar(current = dimension.current, low = dimension.potentialLow, high = dimension.potentialHigh, projected = projected)
    }
}

@Composable
private fun WeekThumb() {
    val colors = SkinthesiaTheme.colors
    Box(
        Modifier
            .size(22.dp)
            .clip(CircleShape)
            .background(colors.surfaceElevated)
            .border(2.dp, colors.primary, CircleShape),
    )
}

@Composable
private fun WeekTrack(fraction: Float) {
    val colors = SkinthesiaTheme.colors
    Box(Modifier.fillMaxWidth().height(4.dp).clip(CircleShape).background(colors.track)) {
        Box(Modifier.fillMaxHeight().fillMaxWidth(fraction.coerceIn(0f, 1f)).background(colors.primary))
    }
}
