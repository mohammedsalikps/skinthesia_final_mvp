package com.skinthesia.feature.analysis

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.skinthesia.core.design.SkinthesiaTheme
import com.skinthesia.core.navigation.CameraAnalysisRoute
import com.skinthesia.core.navigation.FlowKind
import com.skinthesia.core.navigation.LocalAppNavigator
import com.skinthesia.core.navigation.containerViewModel
import com.skinthesia.core.navigation.toFlowKind
import com.skinthesia.core.ui.components.ErrorState
import com.skinthesia.core.ui.components.FadeInUp
import com.skinthesia.core.ui.components.InfoNotice
import com.skinthesia.core.ui.components.ScreenHeader
import com.skinthesia.core.ui.components.SkinthesiaCard
import com.skinthesia.core.ui.components.SkinthesiaDivider
import com.skinthesia.core.ui.components.SkinthesiaPrimaryButton
import com.skinthesia.core.ui.components.Tag
import com.skinthesia.core.ui.components.TagTone
import com.skinthesia.domain.model.CameraAnalysisResult
import com.skinthesia.domain.model.IndicatorEstimate
import com.skinthesia.domain.repository.AssessmentRepository
import com.skinthesia.domain.usecase.RunAnalysisUseCase
import com.skinthesia.feature.common.FlowScaffold
import com.skinthesia.feature.onboarding.OnboardingSteps
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

data class CameraAnalysisUiState(
    val photoPath: String? = null,
    val result: CameraAnalysisResult? = null,
    val stage: Int = 0,
    val hasSession: Boolean = false,
    val failed: Boolean = false,
)

/** Runs the (development) vision model on the assessment photo, pacing the reveal. */
class CameraAnalysisViewModel(
    val assessmentId: String,
    val flow: FlowKind,
    private val assessments: AssessmentRepository,
    private val runAnalysis: RunAnalysisUseCase,
    private val clock: () -> Long,
) : ViewModel() {

    private val _state = MutableStateFlow(CameraAnalysisUiState())
    val state: StateFlow<CameraAnalysisUiState> = _state.asStateFlow()
    private var job: Job? = null

    init {
        analyze()
    }

    fun analyze() {
        if (job?.isActive == true) return
        job = viewModelScope.launch {
            val assessment = assessments.get(assessmentId)
            _state.value = CameraAnalysisUiState(photoPath = assessment?.photo?.filePath, hasSession = assessment?.measurementSessionId != null)
            val ticker = launch {
                for (stage in 1 until STAGES) {
                    delay(STAGE_MILLIS)
                    _state.update { it.copy(stage = stage) }
                }
            }
            val started = clock()
            val result = runAnalysis.camera(assessmentId)
            val remaining = MIN_MILLIS - (clock() - started)
            if (remaining > 0) delay(remaining)
            ticker.cancel()
            _state.update { it.copy(result = result, stage = STAGES, failed = result == null) }
        }
    }

    companion object {
        const val STAGES = 3
        private const val STAGE_MILLIS = 950L
        private const val MIN_MILLIS = 2_900L
    }
}

private val STAGE_LABELS = listOf("Mapping facial zones", "Reading tone and texture", "Estimating visible features")

/** Screen 17: the camera analysis, shown as visual estimates from a development model. */
@Composable
fun CameraAnalysisScreen() {
    val navigator = LocalAppNavigator.current
    val viewModel = containerViewModel { handle ->
        val route = handle.toRoute<CameraAnalysisRoute>()
        CameraAnalysisViewModel(route.assessmentId, route.flow.toFlowKind(), assessments, runAnalysis, clock)
    }
    val state by viewModel.state.collectAsStateWithLifecycle()
    val colors = SkinthesiaTheme.colors
    val typography = SkinthesiaTheme.typography
    val spacing = SkinthesiaTheme.spacing
    val result = state.result
    val done = result != null

    FlowScaffold(
        flow = viewModel.flow,
        step = OnboardingSteps.ANALYSIS,
        title = "Photo analysis",
        onBack = navigator::back,
        bottomBar = {
            if (done) {
                SkinthesiaPrimaryButton(
                    text = if (state.hasSession) "See Probe Readings" else "Combine My Results",
                    onClick = { navigator.cameraAnalysisDone(viewModel.assessmentId, state.hasSession, viewModel.flow) },
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
        ScreenHeader(
            title = if (done) "What your photo shows" else "Reading your photo",
            subtitle = if (done) {
                "Visual estimates from a development model, not a diagnosis."
            } else {
                "We look only at what's visible: tone, texture and surface features."
            },
            centered = true,
            modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite },
        )
        Spacer(Modifier.height(spacing.md))
        PhotoScanFrame(
            photoPath = state.photoPath,
            scanning = !done && !state.failed,
            modifier = Modifier.fillMaxWidth().animateContentSize().height(if (done) 200.dp else 320.dp),
        )
        Spacer(Modifier.height(spacing.lg))
        when {
            state.failed -> ErrorState(
                title = "We couldn't analyse this photo",
                body = "Try again, or retake your selfie in soft, even light.",
                onRetry = viewModel::analyze,
            )
            result == null -> SkinthesiaCard {
                STAGE_LABELS.forEachIndexed { index, label ->
                    ProcessStepRow(title = label, status = processStatus(index, state.stage, done = false))
                }
            }
            else -> CameraResults(result)
        }
    }
}

@Composable
private fun CameraResults(result: CameraAnalysisResult) {
    val colors = SkinthesiaTheme.colors
    val typography = SkinthesiaTheme.typography
    FadeInUp {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = "${result.estimates.size} visible features", style = typography.labelLarge, color = colors.textPrimary, modifier = Modifier.weight(1f))
                if (result.isSimulated) Tag(text = "Development model", tone = TagTone.GOLD, dot = true)
            }
            Spacer(Modifier.height(12.dp))
            SkinthesiaCard {
                val sorted = result.estimates.sortedByDescending { it.level }
                sorted.forEachIndexed { index, estimate ->
                    IndicatorRow(estimate, delayMillis = 120 + index * 80)
                    if (index < sorted.lastIndex) SkinthesiaDivider()
                }
            }
            Spacer(Modifier.height(12.dp))
            InfoNotice(text = "Bars show how visible each feature looks in this photo, in this light. Lighting, angle and makeup all affect them.")
        }
    }
}

@Composable
private fun IndicatorRow(estimate: IndicatorEstimate, delayMillis: Int) {
    val colors = SkinthesiaTheme.colors
    val typography = SkinthesiaTheme.typography
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp).semantics(mergeDescendants = true) {},
        verticalAlignment = Alignment.Top,
    ) {
        IconBadge(estimate.indicator.icon)
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = estimate.indicator.label, style = typography.labelLarge, color = colors.textPrimary, modifier = Modifier.weight(1f))
                Tag(text = estimate.visibility.label, tone = estimate.visibility.tagTone)
            }
            Spacer(Modifier.height(8.dp))
            LevelBar(fraction = estimate.level / 100f, color = visibilityColor(estimate.visibility), delayMillis = delayMillis)
            Spacer(Modifier.height(6.dp))
            val regions = estimate.regions.joinToString(", ") { it.label }
            Text(
                text = (if (regions.isNotEmpty()) "$regions · " else "") + "${(estimate.confidence * 100).roundToInt()}% confidence",
                style = typography.caption,
                color = colors.textMuted,
            )
        }
    }
}
