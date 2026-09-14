package com.skinthesia.feature.measurement

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.skinthesia.core.design.SkinthesiaTheme
import com.skinthesia.core.navigation.CalibrationRoute
import com.skinthesia.core.navigation.FlowKind
import com.skinthesia.core.navigation.LocalAppNavigator
import com.skinthesia.core.navigation.containerViewModel
import com.skinthesia.core.navigation.toFlowKind
import com.skinthesia.core.ui.art.ProbeIllustration
import com.skinthesia.core.ui.art.ProbeVisualState
import com.skinthesia.core.ui.components.FadeInUp
import com.skinthesia.core.ui.components.SectionOverline
import com.skinthesia.core.ui.components.SkinthesiaCard
import com.skinthesia.core.ui.components.SkinthesiaDivider
import com.skinthesia.core.ui.components.SkinthesiaPrimaryButton
import com.skinthesia.core.ui.components.SkinthesiaTextButton
import com.skinthesia.core.ui.icons.SkinthesiaIcons
import com.skinthesia.domain.model.MeasurementRegion
import com.skinthesia.domain.model.MeasurementSession
import com.skinthesia.domain.model.ProbeDevice
import com.skinthesia.domain.model.ProbeState
import com.skinthesia.domain.model.SensorType
import com.skinthesia.domain.repository.AssessmentRepository
import com.skinthesia.domain.repository.SkinProbeRepository
import com.skinthesia.domain.repository.UserProfileRepository
import com.skinthesia.feature.common.FlowScaffold
import com.skinthesia.feature.onboarding.OnboardingSteps
import com.skinthesia.hardware.probe.SkinProbeManager
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/**
 * Runs the probe's pre-measurement checks, then opens (or resumes) the measurement
 * session that the three region readings are saved into.
 */
class CalibrationViewModel(
    val assessmentId: String?,
    val flow: FlowKind,
    private val handle: SavedStateHandle,
    private val probe: SkinProbeManager,
    private val sessions: SkinProbeRepository,
    private val profiles: UserProfileRepository,
    private val assessments: AssessmentRepository,
) : ViewModel() {

    val state: StateFlow<ProbeState> = probe.state
    val sessionId: StateFlow<String?> = handle.getStateFlow<String?>(KEY_SESSION, null)
    private var job: Job? = null

    init {
        start()
    }

    fun start() {
        if (job?.isActive == true) return
        job = viewModelScope.launch {
            probe.acknowledgeError()
            if (probe.connectedDevice == null) {
                val paired = profiles.current().pairedDeviceId ?: return@launch
                if (!probe.reconnect(paired)) return@launch
            }
            if (!probe.calibrate()) return@launch
            val device = probe.connectedDevice ?: return@launch
            handle[KEY_SESSION] = openSession(device).id
        }
    }

    /** Reuses an unfinished session for this assessment so an interrupted journey resumes cleanly. */
    private suspend fun openSession(device: ProbeDevice): MeasurementSession {
        handle.get<String>(KEY_SESSION)?.let { id -> sessions.session(id)?.takeIf { !it.isComplete }?.let { return it } }
        val linked = assessmentId?.let { assessments.get(it)?.measurementSessionId }?.let { sessions.session(it) }
        if (linked != null && !linked.isComplete && linked.deviceId == device.id) return linked
        val session = sessions.startSession(profiles.current().id, assessmentId, device)
        assessmentId?.let { id -> assessments.update(id) { it.copy(measurementSessionId = session.id, probeSkipped = false) } }
        return session
    }

    private companion object {
        const val KEY_SESSION = "calibration.sessionId"
    }
}

private enum class StepStatus { PENDING, ACTIVE, DONE }

/** Screen 12: three calm checks, then "ready to measure". */
@Composable
fun CalibrationScreen() {
    val navigator = LocalAppNavigator.current
    val viewModel = containerViewModel { handle ->
        val route = handle.toRoute<CalibrationRoute>()
        CalibrationViewModel(route.assessmentId, route.flow.toFlowKind(), handle, probeManager, probeRepository, profiles, assessments)
    }
    val state by viewModel.state.collectAsStateWithLifecycle()
    val sessionId by viewModel.sessionId.collectAsStateWithLifecycle()
    val colors = SkinthesiaTheme.colors
    val typography = SkinthesiaTheme.typography
    val spacing = SkinthesiaTheme.spacing
    val motion = SkinthesiaTheme.motion

    val device = when (val s = state) {
        is ProbeState.Connected -> s.device
        is ProbeState.Calibrating -> s.device
        is ProbeState.Ready -> s.device
        else -> null
    }
    val failed = state as? ProbeState.Failed
    val lost = failed != null || (device == null && state !is ProbeState.Connecting)
    val ready = state is ProbeState.Ready && sessionId != null
    val progress = when (val s = state) {
        is ProbeState.Calibrating -> s.progress
        is ProbeState.Ready -> 1f
        else -> 0f
    }
    val sensors = device?.capabilities?.let { caps -> SensorType.entries.filter { it in caps } }.orEmpty()
    val steps = listOf(
        "Connection" to "Stable link to your phone",
        "Sensors" to (if (sensors.isEmpty()) "Checking the sensors" else sensors.joinToString(", ") { it.label } + " responding"),
        "Measurement readiness" to "Ready for gentle skin contact",
    )
    fun statusOf(index: Int): StepStatus = when {
        lost -> StepStatus.PENDING
        state is ProbeState.Ready -> StepStatus.DONE
        progress >= (index + 1) / 3f - 0.01f -> StepStatus.DONE
        progress >= index / 3f - 0.01f -> StepStatus.ACTIVE
        else -> StepStatus.PENDING
    }

    FlowScaffold(
        flow = viewModel.flow,
        step = OnboardingSteps.PROBE,
        title = "Calibration",
        onBack = navigator::back,
        bottomBar = {
            if (lost) {
                SkinthesiaPrimaryButton(text = "Try Again", onClick = viewModel::start)
                SkinthesiaTextButton(text = "Back to connection", onClick = navigator::back)
            } else {
                SkinthesiaPrimaryButton(
                    text = if (ready) "Start Measuring" else "Calibrating…",
                    enabled = ready,
                    trailingIcon = if (ready) SkinthesiaIcons.ArrowRight else null,
                    onClick = { sessionId?.let { navigator.measureRegion(it, MeasurementRegion.FOREHEAD, viewModel.assessmentId, viewModel.flow) } },
                )
            }
        },
    ) {
        FadeInUp {
            ProbeIllustration(
                state = if (ready || lost) ProbeVisualState.CONNECTED else ProbeVisualState.MEASURING,
                modifier = Modifier.fillMaxWidth().height(200.dp),
            )
        }
        Spacer(Modifier.height(spacing.sm))
        FadeInUp(delayMillis = motion.stagger(1)) {
            Column(Modifier.fillMaxWidth().semantics { liveRegion = LiveRegionMode.Polite }, horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = when {
                        failed != null -> failed.error.title
                        lost -> "Your probe isn't connected"
                        ready -> "Ready to measure"
                        else -> "Preparing your probe"
                    },
                    style = typography.title,
                    color = colors.textPrimary,
                    textAlign = TextAlign.Center,
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = when {
                        failed != null -> failed.error.message
                        lost -> "Reconnect your probe to continue this session."
                        ready -> "We'll take readings from your forehead, left cheek and right cheek."
                        else -> "Keep the probe still and away from your skin while it runs a quick check."
                    },
                    style = typography.subtitle,
                    color = colors.textSecondary,
                    textAlign = TextAlign.Center,
                )
            }
        }
        Spacer(Modifier.height(spacing.lg))
        FadeInUp(delayMillis = motion.stagger(2)) {
            SkinthesiaCard {
                steps.forEachIndexed { index, (title, detail) ->
                    CalibrationStepRow(title = title, detail = detail, status = statusOf(index))
                    if (index < steps.lastIndex) SkinthesiaDivider()
                }
            }
        }
        if (ready) {
            Spacer(Modifier.height(spacing.md))
            FadeInUp {
                SkinthesiaCard {
                    SectionOverline(text = "Before you start")
                    Spacer(Modifier.height(10.dp))
                    listOf(
                        "Clean, dry skin gives steadier readings.",
                        "Rest the probe flat. There's no need to press.",
                        "Hold still until each reading settles.",
                    ).forEach { tip ->
                        Row(Modifier.padding(vertical = 5.dp), verticalAlignment = Alignment.Top) {
                            Box(Modifier.padding(top = 8.dp).size(5.dp).clip(CircleShape).background(colors.primary))
                            Spacer(Modifier.width(12.dp))
                            Text(text = tip, style = typography.body, color = colors.textSecondary)
                        }
                    }
                }
            }
        }
        Spacer(Modifier.height(spacing.md))
        SimulatedNote()
    }
}

@Composable
private fun CalibrationStepRow(title: String, detail: String, status: StepStatus) {
    val colors = SkinthesiaTheme.colors
    val typography = SkinthesiaTheme.typography
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp).semantics(mergeDescendants = true) {},
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(30.dp)
                .clip(CircleShape)
                .background(
                    when (status) {
                        StepStatus.DONE -> colors.success.copy(alpha = 0.14f)
                        StepStatus.ACTIVE -> colors.primaryMist
                        StepStatus.PENDING -> colors.border.copy(alpha = 0.45f)
                    },
                ),
            contentAlignment = Alignment.Center,
        ) {
            when (status) {
                StepStatus.DONE -> Icon(SkinthesiaIcons.Check, contentDescription = null, tint = colors.success, modifier = Modifier.size(15.dp))
                StepStatus.ACTIVE -> PulseDot()
                StepStatus.PENDING -> Box(Modifier.size(6.dp).clip(CircleShape).background(colors.textMuted.copy(alpha = 0.5f)))
            }
        }
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f)) {
            Text(text = title, style = typography.labelLarge, color = colors.textPrimary)
            Spacer(Modifier.height(2.dp))
            Text(text = detail, style = typography.bodySmall, color = colors.textSecondary)
        }
        Spacer(Modifier.width(8.dp))
        Text(
            text = when (status) {
                StepStatus.DONE -> "Done"
                StepStatus.ACTIVE -> "Checking"
                StepStatus.PENDING -> "Waiting"
            },
            style = typography.caption,
            color = if (status == StepStatus.DONE) colors.success else colors.textMuted,
        )
    }
}
