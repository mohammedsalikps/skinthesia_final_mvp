package com.skinthesia.feature.analysis

import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.PathMeasure
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.skinthesia.core.design.SkinthesiaTheme
import com.skinthesia.core.navigation.CombinedAnalysisRoute
import com.skinthesia.core.navigation.FlowKind
import com.skinthesia.core.navigation.LocalAppNavigator
import com.skinthesia.core.navigation.containerViewModel
import com.skinthesia.core.navigation.toFlowKind
import com.skinthesia.core.ui.components.BrandMonogram
import com.skinthesia.core.ui.components.ErrorState
import com.skinthesia.core.ui.components.FadeInUp
import com.skinthesia.core.ui.components.ListRow
import com.skinthesia.core.ui.components.ScoreRing
import com.skinthesia.core.ui.components.SectionOverline
import com.skinthesia.core.ui.components.SkinPrintRadial
import com.skinthesia.core.ui.components.SkinthesiaCard
import com.skinthesia.core.ui.components.SkinthesiaPrimaryButton
import com.skinthesia.core.ui.components.Tag
import com.skinthesia.core.ui.components.TagTone
import com.skinthesia.core.ui.icons.SkinthesiaIcons
import com.skinthesia.domain.model.Assessment
import com.skinthesia.domain.model.CombinedAnalysis
import com.skinthesia.domain.model.MeasurementSession
import com.skinthesia.domain.model.SkinPrint
import com.skinthesia.domain.model.UserProfile
import com.skinthesia.domain.repository.AssessmentRepository
import com.skinthesia.domain.repository.SkinProbeRepository
import com.skinthesia.domain.repository.UserProfileRepository
import com.skinthesia.domain.usecase.RunAnalysisUseCase
import com.skinthesia.feature.common.FlowScaffold
import com.skinthesia.feature.measurement.SimulatedTag
import com.skinthesia.feature.onboarding.OnboardingSteps
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.sin
import kotlin.math.sqrt

/** What went into the analysis, phrased for the diagram and the transparency card. */
data class AnalysisInputSummary(
    val cameraDetail: String,
    val cameraUsed: Boolean,
    val cameraSimulated: Boolean,
    val probeDetail: String,
    val probeUsed: Boolean,
    val probeSimulated: Boolean,
    val probeAreas: Int,
    val lifestyleDetail: String,
    val goalsDetail: String,
    val goalsLabels: String,
)

data class CombinedUiState(
    val inputs: AnalysisInputSummary? = null,
    val stage: Int = 0,
    val result: CombinedAnalysis? = null,
    val failed: Boolean = false,
)

class CombinedAnalysisViewModel(
    val assessmentId: String,
    val flow: FlowKind,
    private val assessments: AssessmentRepository,
    private val profiles: UserProfileRepository,
    private val sessions: SkinProbeRepository,
    private val runAnalysis: RunAnalysisUseCase,
    private val clock: () -> Long,
) : ViewModel() {

    private val _state = MutableStateFlow(CombinedUiState())
    val state: StateFlow<CombinedUiState> = _state.asStateFlow()
    private var job: Job? = null

    init {
        run()
    }

    fun run() {
        if (job?.isActive == true) return
        job = viewModelScope.launch {
            _state.update { it.copy(failed = false, result = null, stage = 0) }
            val assessment = assessments.get(assessmentId)
            if (assessment == null) {
                _state.update { it.copy(failed = true) }
                return@launch
            }
            val profile = profiles.current()
            val session = assessment.measurementSessionId?.let { sessions.session(it) }?.takeIf { it.readings.isNotEmpty() }
            _state.update { it.copy(inputs = summarize(assessment, profile, session)) }
            val cached = assessment.combined
            val stageMillis = if (cached != null) 350L else STAGE_MILLIS
            val ticker = launch {
                for (stage in 1 until STAGES) {
                    delay(stageMillis)
                    _state.update { it.copy(stage = stage) }
                }
            }
            val started = clock()
            val result = cached ?: runAnalysis.combine(assessmentId)
            val minimum = if (cached != null) 1_200L else MIN_MILLIS
            val remaining = minimum - (clock() - started)
            if (remaining > 0) delay(remaining)
            ticker.cancel()
            val fresh = assessments.get(assessmentId)
            if (fresh?.cameraAnalysis != null) _state.update { s -> s.copy(inputs = summarize(fresh, profile, session)) }
            _state.update { it.copy(result = result, stage = STAGES, failed = result == null) }
        }
    }

    private fun summarize(a: Assessment, p: UserProfile, s: MeasurementSession?): AnalysisInputSummary {
        val camera = a.cameraAnalysis
        val life = p.lifestyle
        val answered = listOfNotNull(life.sleep, life.stress, life.sunExposure, life.diet, life.exercise, life.environment, life.climate).size +
            (if (life.habits.isNotEmpty()) 1 else 0)
        val goals = p.goals.ranked.take(3)
        return AnalysisInputSummary(
            cameraDetail = when {
                camera != null -> "${camera.estimates.size} estimates"
                a.photo != null -> "Selfie ready"
                else -> "No photo"
            },
            cameraUsed = a.photo != null,
            cameraSimulated = camera?.isSimulated ?: true,
            probeDetail = s?.let { "${it.readings.size} readings" } ?: "Skipped",
            probeUsed = s != null,
            probeSimulated = s?.isSimulated ?: false,
            probeAreas = s?.completedRegions?.size ?: 0,
            lifestyleDetail = when (answered) {
                0 -> "Not shared"
                1 -> "1 answer"
                else -> "$answered answers"
            },
            goalsDetail = if (goals.isEmpty()) "Overall health" else "${goals.size} " + if (goals.size == 1) "priority" else "priorities",
            goalsLabels = goals.joinToString(", ") { it.label }.ifEmpty { "Overall skin health" },
        )
    }

    companion object {
        const val STAGES = 3
        private const val STAGE_MILLIS = 1_150L
        private const val MIN_MILLIS = 3_600L
    }
}

/** Screen 19: camera, probe, lifestyle and goals flow into Skinthesia AI and become a SkinPrint. */
@Composable
fun CombinedAnalysisScreen() {
    val navigator = LocalAppNavigator.current
    val viewModel = containerViewModel { handle ->
        val route = handle.toRoute<CombinedAnalysisRoute>()
        CombinedAnalysisViewModel(route.assessmentId, route.flow.toFlowKind(), assessments, profiles, probeRepository, runAnalysis, clock)
    }
    val state by viewModel.state.collectAsStateWithLifecycle()
    val colors = SkinthesiaTheme.colors
    val typography = SkinthesiaTheme.typography
    val spacing = SkinthesiaTheme.spacing
    val motion = SkinthesiaTheme.motion
    val result = state.result
    val inputs = state.inputs
    val done = result != null
    val probeUsed = inputs?.probeUsed == true

    val status = when {
        state.failed -> "Something interrupted the analysis."
        done && probeUsed -> "Built from your photo, probe readings, goals and daily context."
        done -> "Built from your photo, your goals and your daily context."
        state.stage == 0 && probeUsed -> "Aligning photo estimates with probe readings…"
        state.stage == 0 -> "Reading your photo estimates…"
        state.stage == 1 -> "Weighing your goals and daily context…"
        else -> "Calculating your SkinPrint…"
    }

    FlowScaffold(
        flow = viewModel.flow,
        step = OnboardingSteps.ANALYSIS,
        title = "Skinthesia AI",
        onBack = navigator::back,
        bottomBar = {
            if (done) {
                SkinthesiaPrimaryButton(
                    text = when (viewModel.flow) {
                        FlowKind.ONBOARDING -> "See My Potential"
                        FlowKind.CHECK_IN -> "See My Progress"
                        FlowKind.STANDALONE -> "View My SkinPrint"
                    },
                    onClick = { navigator.combinedDone(viewModel.assessmentId, viewModel.flow) },
                )
            } else if (!state.failed) {
                Text(
                    text = "This takes a few seconds.",
                    style = typography.caption,
                    color = colors.textMuted,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
                )
            }
        },
    ) {
        Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            SectionOverline(text = "Skinthesia AI")
            Spacer(Modifier.height(8.dp))
            Text(
                text = if (done) "Your SkinPrint is ready" else "Bringing it all together",
                style = typography.title,
                color = colors.textPrimary,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = status,
                style = typography.subtitle,
                color = colors.textSecondary,
                textAlign = TextAlign.Center,
                modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite },
            )
        }
        Spacer(Modifier.height(spacing.lg))
        when {
            state.failed -> ErrorState(
                title = "We couldn't finish your analysis",
                body = "Your photo and readings are saved. Try again in a moment.",
                onRetry = viewModel::run,
            )
            inputs != null -> ConvergenceDiagram(inputs = inputs, stage = state.stage, skinPrint = result?.skinPrint)
        }
        if (result != null && inputs != null) {
            Spacer(Modifier.height(spacing.lg))
            FadeInUp(delayMillis = motion.stagger(1)) { SkinPrintSignature(result.skinPrint) }
            Spacer(Modifier.height(spacing.md))
            FadeInUp(delayMillis = motion.stagger(2)) { Highlights(result) }
            Spacer(Modifier.height(spacing.md))
            FadeInUp(delayMillis = motion.stagger(3)) { WhatWentIn(inputs, result) }
        }
    }
}

private data class DiagramNode(
    val icon: ImageVector,
    val label: String,
    val detail: String,
    val used: Boolean,
    val stage: Int,
)

private val NodeBlock = 112.dp
private val CoreCenterY = 300.dp
private val OrbSize = 116.dp
private val RingSize = 200.dp

@Composable
private fun ConvergenceDiagram(inputs: AnalysisInputSummary, stage: Int, skinPrint: SkinPrint?) {
    val colors = SkinthesiaTheme.colors
    val reduced = SkinthesiaTheme.motion.reducedMotion
    val done = skinPrint != null
    val transition = rememberInfiniteTransition(label = "convergence")
    val flowPhase by transition.animateFloat(0f, 1f, infiniteRepeatable(tween(1800, easing = LinearEasing)), label = "flow")
    val spin by transition.animateFloat(0f, 360f, infiniteRepeatable(tween(2600, easing = LinearEasing)), label = "spin")
    val breath by transition.animateFloat(0f, 1f, infiniteRepeatable(tween(1400, easing = FastOutSlowInEasing), RepeatMode.Reverse), label = "breath")
    val settle by animateFloatAsState(if (done) 1f else 0f, tween(if (reduced) 0 else 700), label = "settle")

    val nodes = listOf(
        DiagramNode(SkinthesiaIcons.Camera, "Photo", inputs.cameraDetail, inputs.cameraUsed, stage = 0),
        DiagramNode(SkinthesiaIcons.Probe, "Probe", inputs.probeDetail, inputs.probeUsed, stage = 0),
        DiagramNode(SkinthesiaIcons.Leaf, "Lifestyle", inputs.lifestyleDetail, used = true, stage = 1),
        DiagramNode(SkinthesiaIcons.Target, "Goals", inputs.goalsDetail, used = true, stage = 1),
    )
    val description = "Diagram: photo, probe, lifestyle and goals flow into Skinthesia AI" +
        (skinPrint?.let { ", which produced a SkinPrint of ${it.overall} out of 100." } ?: ".")

    BoxWithConstraints(
        Modifier
            .fillMaxWidth()
            .height(CoreCenterY + RingSize / 2 + 8.dp)
            .semantics(mergeDescendants = false) { contentDescription = description },
    ) {
        val width = maxWidth
        Canvas(Modifier.fillMaxSize().clearAndSetSemantics { }) {
            val cx = size.width / 2
            val cy = CoreCenterY.toPx()
            val endRadius = 60.dp.toPx() + 30.dp.toPx() * settle
            val dash = PathEffect.dashPathEffect(floatArrayOf(6.dp.toPx(), 7.dp.toPx()))
            nodes.forEachIndexed { i, node ->
                val x = size.width * (2 * i + 1) / 8f
                val startY = NodeBlock.toPx()
                val dx = (x - cx) * 0.18f
                val endX = cx + dx
                val endY = cy - sqrt((endRadius * endRadius - dx * dx).coerceAtLeast(0f))
                val path = Path().apply {
                    moveTo(x, startY)
                    cubicTo(x, startY + 56.dp.toPx(), endX, endY - 64.dp.toPx(), endX, endY)
                }
                drawPath(
                    path = path,
                    color = if (node.used) colors.borderStrong else colors.border,
                    style = Stroke(width = 1.2.dp.toPx(), cap = StrokeCap.Round, pathEffect = if (node.used) null else dash),
                )
                if (node.used && !done && !reduced) {
                    val measure = PathMeasure().apply { setPath(path, false) }
                    val length = measure.length
                    repeat(2) { k ->
                        val t = (flowPhase + i * 0.21f + k * 0.5f) % 1f
                        val position = measure.getPosition(length * t)
                        val fade = sin(t * PI).toFloat()
                        drawCircle(colors.primary.copy(alpha = 0.18f * fade), radius = 6.dp.toPx(), center = position)
                        drawCircle(colors.primary.copy(alpha = 0.85f * fade), radius = 2.6.dp.toPx(), center = position)
                    }
                }
                if (node.used && settle > 0f) {
                    drawCircle(colors.primary.copy(alpha = 0.6f * settle), radius = 2.2.dp.toPx(), center = Offset(endX, endY))
                }
            }
            if (!done) {
                val center = Offset(cx, cy)
                val orbRadius = (OrbSize / 2).toPx()
                val halo = orbRadius + 10.dp.toPx() + 8.dp.toPx() * (if (reduced) 0.5f else breath)
                drawCircle(colors.primarySoft.copy(alpha = 0.22f), radius = halo, center = center)
                val arcRadius = orbRadius + 7.dp.toPx()
                rotate(if (reduced) 0f else spin, pivot = center) {
                    drawArc(
                        brush = Brush.sweepGradient(0f to Color.Transparent, 0.3f to colors.primary, 1f to colors.primary, center = center),
                        startAngle = 0f,
                        sweepAngle = 108f,
                        useCenter = false,
                        topLeft = Offset(cx - arcRadius, cy - arcRadius),
                        size = Size(arcRadius * 2, arcRadius * 2),
                        style = Stroke(2.dp.toPx(), cap = StrokeCap.Round),
                    )
                }
            }
        }
        nodes.forEachIndexed { i, node ->
            val highlighted = !done && node.used && (stage >= 2 || stage == node.stage)
            DiagramNodeView(
                node = node,
                highlighted = highlighted,
                done = done,
                modifier = Modifier.width(width / 4).offset(x = width * i / 4),
            )
        }
        Box(
            modifier = Modifier.align(Alignment.TopCenter).offset(y = CoreCenterY - RingSize / 2).size(RingSize),
            contentAlignment = Alignment.Center,
        ) {
            Crossfade(targetState = skinPrint, animationSpec = tween(if (reduced) 0 else 600), label = "core") { print ->
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    if (print != null) {
                        ScoreRing(score = print.overall, size = RingSize, strokeWidth = 8.dp, caption = "SkinPrint", label = print.band.label)
                    } else {
                        CoreOrb()
                    }
                }
            }
        }
    }
}

@Composable
private fun CoreOrb() {
    val colors = SkinthesiaTheme.colors
    val typography = SkinthesiaTheme.typography
    Box(
        modifier = Modifier
            .size(OrbSize)
            .clip(CircleShape)
            .background(Brush.radialGradient(listOf(colors.surfaceElevated, colors.primaryMist)))
            .border(1.dp, colors.borderStrong.copy(alpha = 0.6f), CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            BrandMonogram(size = 44.dp)
            Spacer(Modifier.height(2.dp))
            Text(text = "AI", style = typography.displayItalic.copy(fontSize = 16.sp, lineHeight = 20.sp), color = colors.primary)
        }
    }
}

@Composable
private fun DiagramNodeView(node: DiagramNode, highlighted: Boolean, done: Boolean, modifier: Modifier) {
    val colors = SkinthesiaTheme.colors
    val typography = SkinthesiaTheme.typography
    val ring by animateColorAsState(if (highlighted) colors.primary else colors.border, label = "nodeRing")
    Column(modifier.padding(horizontal = 2.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Box(Modifier.size(54.dp)) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .align(Alignment.Center)
                    .clip(CircleShape)
                    .background(if (node.used) colors.surface else colors.surfaceMuted)
                    .border(1.dp, ring, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Icon(node.icon, contentDescription = null, tint = if (node.used) colors.primary else colors.textMuted, modifier = Modifier.size(22.dp))
            }
            if (done && node.used) {
                Box(
                    modifier = Modifier.align(Alignment.TopEnd).size(18.dp).clip(CircleShape).background(colors.success),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(SkinthesiaIcons.Check, contentDescription = null, tint = colors.textOnPrimary, modifier = Modifier.size(11.dp))
                }
            }
        }
        Spacer(Modifier.height(8.dp))
        Text(text = node.label, style = typography.label, color = if (node.used) colors.textPrimary else colors.textMuted, maxLines = 1)
        Text(
            text = node.detail,
            style = typography.caption,
            color = colors.textMuted,
            textAlign = TextAlign.Center,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

/** The freshly-built SkinPrint signature, shown once as its own reveal beat right after the core settles. */
@Composable
private fun SkinPrintSignature(skinPrint: SkinPrint) {
    val colors = SkinthesiaTheme.colors
    val typography = SkinthesiaTheme.typography
    SkinthesiaCard(elevated = true) {
        Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            SectionOverline(text = "Your SkinPrint signature")
            Spacer(Modifier.height(4.dp))
            Text(text = skinPrint.band.label, style = typography.titleSmall, color = colors.textPrimary)
            Spacer(Modifier.height(6.dp))
            SkinPrintRadial(scores = skinPrint.scores, overall = skinPrint.overall, band = skinPrint.band, size = 224.dp)
        }
    }
}

@Composable
private fun Highlights(result: CombinedAnalysis) {
    val best = result.skinPrint.scores.maxByOrNull { it.value }
    val focus = result.focusAreas.firstOrNull()
    Row(Modifier.fillMaxWidth().height(IntrinsicSize.Min), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        HighlightCard(
            overline = "Strongest",
            title = best?.dimension?.label ?: "Balanced",
            detail = best?.let { "${it.value} of 100" } ?: "Across all areas",
            modifier = Modifier.weight(1f).fillMaxHeight(),
        )
        HighlightCard(
            overline = "Focus first",
            title = focus?.area?.label ?: "Maintain",
            detail = focus?.level?.label ?: "Keep up your routine",
            modifier = Modifier.weight(1f).fillMaxHeight(),
        )
    }
}

@Composable
private fun HighlightCard(overline: String, title: String, detail: String, modifier: Modifier) {
    val colors = SkinthesiaTheme.colors
    val typography = SkinthesiaTheme.typography
    SkinthesiaCard(modifier = modifier) {
        SectionOverline(text = overline)
        Spacer(Modifier.height(8.dp))
        Text(text = title, style = typography.titleSmall, color = colors.textPrimary, maxLines = 2)
        Spacer(Modifier.height(4.dp))
        Text(text = detail, style = typography.caption, color = colors.textMuted)
    }
}

@Composable
private fun WhatWentIn(inputs: AnalysisInputSummary, result: CombinedAnalysis) {
    val colors = SkinthesiaTheme.colors
    val typography = SkinthesiaTheme.typography
    SkinthesiaCard {
        SectionOverline(text = "What went in")
        Spacer(Modifier.height(4.dp))
        ListRow(
            title = "Photo",
            subtitle = if (inputs.cameraUsed) "Visual estimates of ${inputs.cameraDetail.substringBefore(' ')} features" else "No photo this time",
            leadingIcon = SkinthesiaIcons.Camera,
            trailing = { if (inputs.cameraUsed && inputs.cameraSimulated) Tag(text = "Development model", tone = TagTone.GOLD, dot = true) },
        )
        ListRow(
            title = "Probe",
            subtitle = if (inputs.probeUsed) "${inputs.probeAreas} areas · ${inputs.probeDetail}" else "Skipped this time",
            leadingIcon = SkinthesiaIcons.Probe,
            trailing = { if (inputs.probeUsed && inputs.probeSimulated) SimulatedTag() },
        )
        ListRow(
            title = "Goals",
            subtitle = inputs.goalsLabels,
            leadingIcon = SkinthesiaIcons.Target,
            trailing = { Tag(text = "You told us", tone = TagTone.NEUTRAL) },
        )
        ListRow(
            title = "Daily context",
            subtitle = "Used as context, never as a cause",
            leadingIcon = SkinthesiaIcons.Leaf,
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = "Engine ${result.engineVersion} · ${result.skinPrint.algorithmVersion}",
            style = typography.caption,
            color = colors.textMuted,
        )
    }
}
