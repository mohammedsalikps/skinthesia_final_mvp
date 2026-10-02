package com.skinthesia.feature.measurement

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.CircularProgressIndicator
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
import com.skinthesia.core.navigation.FlowKind
import com.skinthesia.core.navigation.LocalAppNavigator
import com.skinthesia.core.navigation.ProbeConnectRoute
import com.skinthesia.core.navigation.containerViewModel
import com.skinthesia.core.navigation.toFlowKind
import com.skinthesia.core.ui.art.ProbeIllustration
import com.skinthesia.core.ui.art.ProbeVisualState
import com.skinthesia.core.ui.components.ErrorState
import com.skinthesia.core.ui.components.FadeInUp
import com.skinthesia.core.ui.components.SkinthesiaCard
import com.skinthesia.core.ui.components.SkinthesiaPillButton
import com.skinthesia.core.ui.components.SkinthesiaPrimaryButton
import com.skinthesia.core.ui.components.SkinthesiaTextButton
import com.skinthesia.domain.model.OnboardingStage
import com.skinthesia.domain.model.ProbeDevice
import com.skinthesia.domain.model.ProbeState
import com.skinthesia.domain.repository.AssessmentRepository
import com.skinthesia.domain.repository.UserProfileRepository
import com.skinthesia.feature.common.FlowScaffold
import com.skinthesia.feature.onboarding.OnboardingSteps
import com.skinthesia.feature.onboarding.atLeast
import com.skinthesia.hardware.probe.SkinProbeManager
import com.skinthesia.hardware.probe.SwitchableSkinProbeManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ProbeConnectViewModel(
    val assessmentId: String?,
    val flow: FlowKind,
    private val probe: SkinProbeManager,
    private val profiles: UserProfileRepository,
    private val assessments: AssessmentRepository,
) : ViewModel() {

    val state: StateFlow<ProbeState> = probe.state
    private val _pairedId = MutableStateFlow<String?>(null)
    val pairedId: StateFlow<String?> = _pairedId.asStateFlow()

    /** True only when [probe] is backed by the real ESP32 transport and can fall back to the existing simulated-probe experience if it can't be found. False in ordinary mock mode, where there is nothing to switch to. */
    val canSimulate: Boolean = probe is SwitchableSkinProbeManager

    init {
        probe.acknowledgeError()
        viewModelScope.launch { _pairedId.value = profiles.current().pairedDeviceId }
    }

    fun search() {
        probe.acknowledgeError()
        probe.startDiscovery()
    }

    /** Switches this session to the app's existing mock probe (MockBleDeviceProvider + SimulatedSensorDataProvider, unmodified) and starts the same discovery search already used above, so everything from here on behaves exactly like the existing mock-mode experience. No-op if [canSimulate] is false. */
    fun simulate() {
        (probe as? SwitchableSkinProbeManager)?.useSimulated()
        search()
    }

    fun connect(device: ProbeDevice, onConnected: () -> Unit) {
        viewModelScope.launch {
            if (probe.connect(device)) {
                profiles.update { it.copy(pairedDeviceId = device.id) }
                onConnected()
            }
        }
    }

    fun reconnect(onConnected: () -> Unit) {
        val id = _pairedId.value ?: return search()
        viewModelScope.launch { if (probe.reconnect(id)) onConnected() }
    }

    fun skip(onSkipped: () -> Unit) {
        probe.stopDiscovery()
        viewModelScope.launch {
            assessmentId?.let { id -> assessments.update(id) { it.copy(probeSkipped = true, measurementSessionId = null) } }
            if (flow == FlowKind.ONBOARDING) profiles.update { it.copy(onboardingStage = it.onboardingStage.atLeast(OnboardingStage.ANALYSIS)) }
            onSkipped()
        }
    }

    override fun onCleared() {
        probe.stopDiscovery()
    }
}

/** Screen 10: discover and connect the Skinthesia Probe, or continue without one. */
@Composable
fun ProbeConnectScreen() {
    val navigator = LocalAppNavigator.current
    val viewModel = containerViewModel { handle ->
        val route = handle.toRoute<ProbeConnectRoute>()
        ProbeConnectViewModel(route.assessmentId, route.flow.toFlowKind(), probeManager, profiles, assessments)
    }
    val state by viewModel.state.collectAsStateWithLifecycle()
    val pairedId by viewModel.pairedId.collectAsStateWithLifecycle()
    val colors = SkinthesiaTheme.colors
    val typography = SkinthesiaTheme.typography
    val spacing = SkinthesiaTheme.spacing
    val onConnected = { navigator.probeConnected(viewModel.assessmentId, viewModel.flow) }
    val onSkip = { viewModel.skip { navigator.afterProbe(viewModel.assessmentId, null, viewModel.flow) } }

    FlowScaffold(
        flow = viewModel.flow,
        step = OnboardingSteps.PROBE,
        title = "Connect probe",
        onBack = navigator::back,
        bottomBar = {
            when (state) {
                is ProbeState.Idle -> {
                    SkinthesiaPrimaryButton(text = if (pairedId != null) "Reconnect my probe" else "Search for my probe", onClick = { if (pairedId != null) viewModel.reconnect(onConnected) else viewModel.search() })
                    SkinthesiaTextButton(text = if (viewModel.flow == FlowKind.STANDALONE) "Not now" else "I don't have a probe yet", onClick = onSkip)
                }
                is ProbeState.Connected, is ProbeState.Ready, is ProbeState.Calibrating -> SkinthesiaPrimaryButton(text = "Continue", onClick = onConnected)
                is ProbeState.Scanning, is ProbeState.Connecting -> SkinthesiaTextButton(text = "Skip for now", onClick = onSkip)
                is ProbeState.Failed -> Unit
            }
        },
    ) {
        val visual = when (state) {
            is ProbeState.Scanning, is ProbeState.Connecting -> ProbeVisualState.SEARCHING
            is ProbeState.Connected, is ProbeState.Ready, is ProbeState.Calibrating -> ProbeVisualState.CONNECTED
            else -> ProbeVisualState.IDLE
        }
        FadeInUp {
            ProbeIllustration(state = visual, modifier = Modifier.fillMaxWidth().height(230.dp))
        }
        Spacer(Modifier.height(spacing.md))
        AnimatedContent(
            targetState = state,
            transitionSpec = { fadeIn(tween(250)) togetherWith fadeOut(tween(200)) },
            contentKey = { it::class },
            label = "probeState",
        ) { target ->
            when (val s = target) {
                is ProbeState.Idle -> IdleContent()
                is ProbeState.Scanning -> ScanningContent(found = s.found, connecting = null, onConnect = { viewModel.connect(it, onConnected) })
                is ProbeState.Connecting -> ScanningContent(found = listOf(s.device), connecting = s.device, onConnect = {})
                is ProbeState.Connected -> ConnectedHint(s.device)
                is ProbeState.Ready -> ConnectedHint(s.device)
                is ProbeState.Calibrating -> ConnectedHint(s.device)
                is ProbeState.Failed -> ErrorState(
                    title = s.error.title,
                    body = s.error.message,
                    onRetry = if (viewModel.canSimulate) viewModel::simulate else viewModel::search,
                    retryLabel = if (viewModel.canSimulate) "Simulate" else "Try again",
                    secondaryLabel = "Skip for now",
                    onSecondary = onSkip,
                )
            }
        }
        Spacer(Modifier.height(spacing.lg))
        SimulatedNote()
    }
}

@Composable
private fun IdleContent() {
    val colors = SkinthesiaTheme.colors
    val typography = SkinthesiaTheme.typography
    Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = "Connect your Skinthesia Probe", style = typography.title, color = colors.textPrimary, textAlign = TextAlign.Center)
        Spacer(Modifier.height(8.dp))
        Text(
            text = "Switch your probe on and keep it close to your phone. It adds hydration, pH and temperature readings to your SkinPrint.",
            style = typography.subtitle,
            color = colors.textSecondary,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(20.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            listOf("1" to "Power on", "2" to "Keep nearby", "3" to "We'll find it").forEach { (n, label) ->
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = n, style = typography.metric, color = colors.primary)
                    Text(text = label, style = typography.caption, color = colors.textSecondary)
                }
            }
        }
    }
}

@Composable
private fun ScanningContent(found: List<ProbeDevice>, connecting: ProbeDevice?, onConnect: (ProbeDevice) -> Unit) {
    val colors = SkinthesiaTheme.colors
    val typography = SkinthesiaTheme.typography
    Column(Modifier.fillMaxWidth()) {
        Row(Modifier.fillMaxWidth().semantics { liveRegion = LiveRegionMode.Polite }, verticalAlignment = Alignment.CenterVertically) {
            PulseDot()
            Spacer(Modifier.width(12.dp))
            Text(
                text = when {
                    connecting != null -> "Connecting to ${connecting.name}…"
                    found.isEmpty() -> "Looking for nearby probes…"
                    else -> "${found.size} probe" + (if (found.size == 1) "" else "s") + " nearby"
                },
                style = typography.label,
                color = colors.textSecondary,
            )
        }
        Spacer(Modifier.height(14.dp))
        found.forEachIndexed { index, device ->
            FadeInUp(delayMillis = SkinthesiaTheme.motion.stagger(index)) {
                SkinthesiaCard(modifier = Modifier.padding(bottom = 10.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(text = device.name, style = typography.labelLarge, color = colors.textPrimary)
                                Spacer(Modifier.width(8.dp))
                                if (device.isSimulated) SimulatedTag()
                            }
                            Spacer(Modifier.height(4.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(text = device.id, style = typography.caption, color = colors.textMuted)
                                Spacer(Modifier.width(10.dp))
                                SignalBars(device.signalStrength)
                                Spacer(Modifier.width(10.dp))
                                BatteryMeter(device.batteryPercent)
                            }
                        }
                        if (connecting?.id == device.id) {
                            CircularProgressIndicator(color = colors.primary, strokeWidth = 2.dp, modifier = Modifier.size(22.dp))
                        } else {
                            SkinthesiaPillButton(text = "Connect", onClick = { onConnect(device) }, enabled = connecting == null)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ConnectedHint(device: ProbeDevice) {
    val colors = SkinthesiaTheme.colors
    val typography = SkinthesiaTheme.typography
    Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = "${device.name} connected", style = typography.titleMedium, color = colors.textPrimary, textAlign = TextAlign.Center)
        Spacer(Modifier.height(6.dp))
        Text(text = "Ready when you are.", style = typography.subtitle, color = colors.textSecondary)
    }
}
