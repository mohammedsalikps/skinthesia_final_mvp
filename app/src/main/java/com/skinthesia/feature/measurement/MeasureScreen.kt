package com.skinthesia.feature.measurement

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.skinthesia.core.design.SkinthesiaTheme
import com.skinthesia.core.navigation.FlowKind
import com.skinthesia.core.navigation.LocalAppNavigator
import com.skinthesia.core.navigation.MeasureRoute
import com.skinthesia.core.navigation.containerViewModel
import com.skinthesia.core.navigation.toFlowKind
import com.skinthesia.core.ui.art.FaceDiagram
import com.skinthesia.core.ui.art.ProbeIllustration
import com.skinthesia.core.ui.art.ProbeVisualState
import com.skinthesia.core.ui.art.zone
import com.skinthesia.core.ui.components.FadeInUp
import com.skinthesia.core.ui.components.SectionOverline
import com.skinthesia.core.ui.components.SkinthesiaCard
import com.skinthesia.core.ui.components.SkinthesiaPrimaryButton
import com.skinthesia.core.ui.components.SkinthesiaTextButton
import com.skinthesia.core.ui.icons.SkinthesiaIcons
import com.skinthesia.domain.model.MeasurementRegion
import com.skinthesia.domain.model.ProbeError
import com.skinthesia.domain.model.RegionMeasurementEvent
import com.skinthesia.domain.model.SensorType
import com.skinthesia.domain.repository.AssessmentRepository
import com.skinthesia.domain.repository.SkinProbeRepository
import com.skinthesia.domain.repository.UserProfileRepository
import com.skinthesia.feature.common.FlowScaffold
import com.skinthesia.feature.onboarding.OnboardingSteps
import com.skinthesia.hardware.probe.SkinProbeManager
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.transformWhile
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

enum class MeasurePhase { READY, MEASURING, DONE, FAILED }

data class MeasureUiState(
    val phase: MeasurePhase = MeasurePhase.READY,
    val progress: Float = 0f,
    val values: Map<SensorType, Double> = emptyMap(),
    val error: ProbeError? = null,
    val completed: Set<MeasurementRegion> = emptySet(),
    val sensors: List<SensorType> = SensorType.entries,
    val simulated: Boolean = true,
)

/** Measures one facial region and saves its readings into the open session. */
class MeasureViewModel(
    val sessionId: String,
    val region: MeasurementRegion,
    val assessmentId: String?,
    val flow: FlowKind,
    private val probe: SkinProbeManager,
    private val sessions: SkinProbeRepository,
    private val assessments: AssessmentRepository,
    private val profiles: UserProfileRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(MeasureUiState())
    val state: StateFlow<MeasureUiState> = _state.asStateFlow()
    private var job: Job? = null

    val nextRegion: MeasurementRegion? get() = MeasurementRegion.entries.getOrNull(region.ordinal + 1)

    init {
        viewModelScope.launch {
            val session = sessions.session(sessionId) ?: return@launch
            val caps = probe.connectedDevice?.capabilities
            _state.update {
                it.copy(
                    completed = session.completedRegions,
                    sensors = caps?.let { c -> SensorType.entries.filter { s -> s in c } } ?: SensorType.entries,
                    simulated = session.isSimulated,
                )
            }
            val saved = session.readingsFor(region)
            if (saved.isNotEmpty() && _state.value.phase == MeasurePhase.READY) {
                _state.update { it.copy(phase = MeasurePhase.DONE, progress = 1f, values = saved.associate { r -> r.sensor to r.value }) }
            }
        }
    }

    fun start() {
        if (job?.isActive == true) return
        job = viewModelScope.launch {
            _state.update { it.copy(phase = MeasurePhase.MEASURING, progress = 0f, values = emptyMap(), error = null) }
            if (probe.connectedDevice == null) {
                val paired = profiles.current().pairedDeviceId
                if (paired == null || !probe.reconnect(paired)) {
                    fail(ProbeError.DISCONNECTED)
                    return@launch
                }
            }
            val session = sessions.session(sessionId) ?: run {
                fail(ProbeError.DISCONNECTED)
                return@launch
            }
            val week = assessmentId?.let { assessments.get(it)?.week } ?: 0
            probe.measure(region, session, week)
                .transformWhile { event ->
                    emit(event)
                    event is RegionMeasurementEvent.Progress
                }
                .collect { event ->
                    when (event) {
                        is RegionMeasurementEvent.Progress -> _state.update { it.copy(progress = event.fraction, values = event.live) }
                        is RegionMeasurementEvent.Completed -> {
                            sessions.saveReadings(sessionId, region, event.readings)
                            _state.update {
                                it.copy(
                                    phase = MeasurePhase.DONE,
                                    progress = 1f,
                                    values = event.readings.associate { r -> r.sensor to r.value },
                                    completed = it.completed + region,
                                )
                            }
                        }
                        is RegionMeasurementEvent.Failed -> fail(event.error)
                    }
                }
        }
    }

    private fun fail(error: ProbeError) = _state.update { it.copy(phase = MeasurePhase.FAILED, error = error) }
}

private fun String.toRegion(): MeasurementRegion =
    runCatching { MeasurementRegion.valueOf(this) }.getOrDefault(MeasurementRegion.FOREHEAD)

/** Screens 13 to 15: forehead, left cheek and right cheek, one calm step each. */
@Composable
fun MeasureScreen() {
    val navigator = LocalAppNavigator.current
    val viewModel = containerViewModel { handle ->
        val route = handle.toRoute<MeasureRoute>()
        MeasureViewModel(route.sessionId, route.region.toRegion(), route.assessmentId, route.flow.toFlowKind(), probeManager, probeRepository, assessments, profiles)
    }
    val state by viewModel.state.collectAsStateWithLifecycle()
    val colors = SkinthesiaTheme.colors
    val typography = SkinthesiaTheme.typography
    val spacing = SkinthesiaTheme.spacing
    val region = viewModel.region
    val next = viewModel.nextRegion
    val onNext = {
        if (next != null) {
            navigator.measureRegion(viewModel.sessionId, next, viewModel.assessmentId, viewModel.flow)
        } else {
            navigator.measurementComplete(viewModel.sessionId, viewModel.assessmentId, viewModel.flow)
        }
    }

    FlowScaffold(
        flow = viewModel.flow,
        step = OnboardingSteps.PROBE,
        title = "Measure",
        onBack = navigator::back,
        bottomBar = {
            when (state.phase) {
                MeasurePhase.READY -> SkinthesiaPrimaryButton(text = "Start Measurement", onClick = viewModel::start)
                MeasurePhase.MEASURING -> SkinthesiaPrimaryButton(text = "Measuring…", onClick = {}, enabled = false, loading = true)
                MeasurePhase.DONE -> {
                    SkinthesiaPrimaryButton(text = next?.let { "Next: " + it.label } ?: "Finish Measurement", onClick = onNext)
                    SkinthesiaTextButton(text = "Measure again", onClick = viewModel::start)
                }
                MeasurePhase.FAILED -> SkinthesiaPrimaryButton(text = "Try Again", onClick = viewModel::start)
            }
        },
    ) {
        FadeInUp {
            Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                SectionOverline(text = "Area ${region.ordinal + 1} of ${MeasurementRegion.entries.size}")
                Spacer(Modifier.height(8.dp))
                Text(text = region.label, style = typography.title, color = colors.textPrimary, textAlign = TextAlign.Center)
                Spacer(Modifier.height(6.dp))
                Text(text = region.instruction, style = typography.subtitle, color = colors.textSecondary, textAlign = TextAlign.Center)
            }
        }
        Spacer(Modifier.height(spacing.md))
        ProbeIllustration(
            state = when (state.phase) {
                MeasurePhase.READY -> ProbeVisualState.CONNECTED
                MeasurePhase.MEASURING -> ProbeVisualState.MEASURING
                MeasurePhase.DONE -> ProbeVisualState.CONNECTED
                MeasurePhase.FAILED -> ProbeVisualState.IDLE
            },
            contentDescription = "Skinthesia Probe, " + region.label.lowercase(),
            modifier = Modifier.fillMaxWidth().height(148.dp),
        )
        Spacer(Modifier.height(spacing.sm))
        FaceDiagram(
            contentDescription = "Face map. " + region.label + " is highlighted.",
            modifier = Modifier.fillMaxWidth().height(200.dp),
            activeZone = region.zone().takeIf { state.phase != MeasurePhase.DONE },
            completedZones = state.completed.map { it.zone() }.toSet(),
            measurementPoints = MeasurementRegion.entries.map { it.zone() }.toSet(),
            scanning = state.phase == MeasurePhase.MEASURING,
            showSideLabels = true,
        )
        Spacer(Modifier.height(spacing.md))
        RegionStepper(current = region, completed = state.completed)
        Spacer(Modifier.height(spacing.lg))
        MeasureStatus(state)
        Spacer(Modifier.height(spacing.md))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            state.sensors.forEach { sensor ->
                SensorTile(sensor = sensor, value = state.values[sensor], modifier = Modifier.weight(1f), settled = state.phase == MeasurePhase.DONE)
            }
        }
        val error = state.error
        if (state.phase == MeasurePhase.FAILED && error != null) {
            Spacer(Modifier.height(spacing.md))
            SkinthesiaCard {
                Row(verticalAlignment = Alignment.Top) {
                    Icon(SkinthesiaIcons.Alert, contentDescription = null, tint = colors.warning, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text(text = error.title, style = typography.labelLarge, color = colors.textPrimary)
                        Spacer(Modifier.height(2.dp))
                        Text(text = error.message, style = typography.bodySmall, color = colors.textSecondary)
                    }
                }
            }
        }
        if (state.simulated) {
            Spacer(Modifier.height(spacing.md))
            SimulatedNote(text = "Simulated readings for development. They are not real measurements.")
        }
    }
}

@Composable
private fun MeasureStatus(state: MeasureUiState) {
    val colors = SkinthesiaTheme.colors
    val typography = SkinthesiaTheme.typography
    val progress by animateFloatAsState(state.progress, tween(280), label = "measureProgress")
    val message = when (state.phase) {
        MeasurePhase.READY -> "Rest the probe flat on your skin, then tap Start."
        MeasurePhase.MEASURING -> "Hold still while the reading settles"
        MeasurePhase.DONE -> "Reading saved"
        MeasurePhase.FAILED -> state.error?.title ?: "Measurement stopped"
    }
    Column(Modifier.fillMaxWidth()) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            when (state.phase) {
                MeasurePhase.DONE -> Icon(SkinthesiaIcons.Check, contentDescription = null, tint = colors.success, modifier = Modifier.size(16.dp))
                MeasurePhase.FAILED -> Icon(SkinthesiaIcons.Alert, contentDescription = null, tint = colors.warning, modifier = Modifier.size(16.dp))
                else -> Unit
            }
            if (state.phase == MeasurePhase.DONE || state.phase == MeasurePhase.FAILED) Spacer(Modifier.width(8.dp))
            Text(
                text = message,
                style = typography.label,
                color = colors.textSecondary,
                modifier = Modifier.weight(1f).semantics { liveRegion = LiveRegionMode.Polite },
            )
            if (state.phase == MeasurePhase.MEASURING) {
                Text(text = "${(state.progress * 100).roundToInt()}%", style = typography.numeric, color = colors.textPrimary)
            }
        }
        Spacer(Modifier.height(10.dp))
        Box(
            Modifier
                .fillMaxWidth()
                .height(4.dp)
                .clip(CircleShape)
                .background(colors.border.copy(alpha = 0.6f))
                .clearAndSetSemantics { contentDescription = "Measurement progress ${(state.progress * 100).roundToInt()} percent" },
        ) {
            Box(
                Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(progress.coerceIn(0f, 1f))
                    .clip(CircleShape)
                    .background(if (state.phase == MeasurePhase.DONE) colors.success else colors.primary),
            )
        }
    }
}

@Composable
private fun RegionStepper(current: MeasurementRegion, completed: Set<MeasurementRegion>) {
    val colors = SkinthesiaTheme.colors
    val typography = SkinthesiaTheme.typography
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        MeasurementRegion.entries.forEach { r ->
            val done = r in completed
            val active = r == current
            Column(
                Modifier
                    .weight(1f)
                    .clearAndSetSemantics { contentDescription = r.label + if (done) ", done" else if (active) ", current" else "" },
            ) {
                Box(
                    Modifier
                        .fillMaxWidth()
                        .height(3.dp)
                        .clip(CircleShape)
                        .background(
                            when {
                                done -> colors.primary
                                active -> colors.primary.copy(alpha = 0.4f)
                                else -> colors.border
                            },
                        ),
                )
                Spacer(Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (done) {
                        Icon(SkinthesiaIcons.Check, contentDescription = null, tint = colors.success, modifier = Modifier.size(12.dp))
                        Spacer(Modifier.width(4.dp))
                    }
                    Text(text = r.label, style = typography.caption, color = if (active) colors.textPrimary else colors.textMuted)
                }
            }
        }
    }
}
