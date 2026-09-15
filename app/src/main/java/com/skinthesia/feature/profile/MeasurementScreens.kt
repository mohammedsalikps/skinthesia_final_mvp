package com.skinthesia.feature.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import com.skinthesia.core.navigation.SessionDetailRoute
import com.skinthesia.core.navigation.containerViewModel
import com.skinthesia.core.ui.art.FaceDiagram
import com.skinthesia.core.ui.art.ProbeIllustration
import com.skinthesia.core.ui.art.ProbeVisualState
import com.skinthesia.core.ui.art.zone
import com.skinthesia.core.ui.components.EmptyState
import com.skinthesia.core.ui.components.InfoNotice
import com.skinthesia.core.ui.components.KeyValueRow
import com.skinthesia.core.ui.components.LoadingState
import com.skinthesia.core.ui.components.ScreenHeader
import com.skinthesia.core.ui.components.SectionOverline
import com.skinthesia.core.ui.components.SkinthesiaCard
import com.skinthesia.core.ui.components.SkinthesiaConfirmDialog
import com.skinthesia.core.ui.components.SkinthesiaDivider
import com.skinthesia.core.ui.components.SkinthesiaPrimaryButton
import com.skinthesia.core.ui.components.SkinthesiaScreen
import com.skinthesia.core.ui.components.SkinthesiaSecondaryButton
import com.skinthesia.core.ui.components.SkinthesiaTextButton
import com.skinthesia.core.ui.components.SkinthesiaTopBar
import com.skinthesia.core.ui.components.Tag
import com.skinthesia.core.ui.components.TagTone
import com.skinthesia.core.ui.formatDateTime
import com.skinthesia.core.ui.icons.SkinthesiaIcons
import com.skinthesia.domain.model.MeasurementRegion
import com.skinthesia.domain.model.MeasurementSession
import com.skinthesia.domain.model.ProbeState
import com.skinthesia.domain.model.SensorType
import com.skinthesia.domain.repository.AssessmentRepository
import com.skinthesia.domain.repository.SkinProbeRepository
import com.skinthesia.domain.repository.UserProfileRepository
import com.skinthesia.feature.analysis.IconBadge
import com.skinthesia.feature.measurement.BatteryMeter
import com.skinthesia.feature.measurement.ReadingsTable
import com.skinthesia.feature.measurement.SignalBars
import com.skinthesia.feature.measurement.SimulatedNote
import com.skinthesia.feature.measurement.SimulatedTag
import com.skinthesia.feature.measurement.format
import com.skinthesia.feature.measurement.icon
import com.skinthesia.feature.measurement.shortLabel
import com.skinthesia.hardware.probe.SkinProbeManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MeasurementHistoryViewModel(sessions: SkinProbeRepository) : ViewModel() {
    val sessions: StateFlow<List<MeasurementSession>?> = sessions.sessions()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)
}

/** Every probe session, newest first, clearly labelled when simulated. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MeasurementHistoryScreen() {
    val navigator = LocalAppNavigator.current
    val viewModel = containerViewModel { MeasurementHistoryViewModel(probeRepository) }
    val list by viewModel.sessions.collectAsStateWithLifecycle()
    val colors = SkinthesiaTheme.colors
    val typography = SkinthesiaTheme.typography

    SkinthesiaScreen(topBar = { SkinthesiaTopBar(title = "Measurement history", onBack = navigator::back) }) {
        val sessions = list
        when {
            sessions == null -> LoadingState(message = "Loading sessions")
            sessions.isEmpty() -> EmptyState(
                icon = SkinthesiaIcons.Probe,
                title = "No probe sessions yet",
                body = "Readings from your probe appear here, session by session.",
                actionLabel = "Take a probe reading",
                onAction = { navigator.navigate(ProbeConnectRoute(null, FlowKind.STANDALONE.name)) },
            )
            else -> sessions.forEach { session ->
                SkinthesiaCard(onClick = { navigator.navigate(SessionDetailRoute(session.id)) }) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconBadge(SkinthesiaIcons.Probe)
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text(text = session.label, style = typography.labelLarge, color = colors.textPrimary)
                            Text(text = formatDateTime(session.startedAt) + " · " + session.deviceName, style = typography.caption, color = colors.textMuted)
                        }
                        if (session.isSimulated) SimulatedTag()
                    }
                    Spacer(Modifier.height(12.dp))
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        SensorType.entries.forEach { sensor ->
                            session.average(sensor)?.let { value -> Tag(text = sensor.shortLabel + " " + sensor.format(value), tone = TagTone.NEUTRAL) }
                        }
                        if (!session.isComplete) Tag(text = "Incomplete", tone = TagTone.ROSE)
                    }
                }
                Spacer(Modifier.height(12.dp))
            }
        }
    }
}

data class SessionDetailUiState(val loading: Boolean = true, val session: MeasurementSession? = null, val linkedLabel: String? = null)

class SessionDetailViewModel(
    val sessionId: String,
    private val sessions: SkinProbeRepository,
    private val assessments: AssessmentRepository,
) : ViewModel() {
    private val _state = MutableStateFlow(SessionDetailUiState())
    val state: StateFlow<SessionDetailUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            val session = sessions.session(sessionId)
            val linked = session?.assessmentId?.let { assessments.get(it)?.weekLabel }
            _state.value = SessionDetailUiState(loading = false, session = session, linkedLabel = linked)
        }
    }
}

/** One probe session: where it was taken, what each area read, and its provenance. */
@Composable
fun SessionDetailScreen() {
    val navigator = LocalAppNavigator.current
    val viewModel = containerViewModel { handle -> SessionDetailViewModel(handle.toRoute<SessionDetailRoute>().sessionId, probeRepository, assessments) }
    val state by viewModel.state.collectAsStateWithLifecycle()
    val colors = SkinthesiaTheme.colors
    val typography = SkinthesiaTheme.typography
    val spacing = SkinthesiaTheme.spacing
    val session = state.session

    SkinthesiaScreen(topBar = { SkinthesiaTopBar(title = session?.label ?: "Session", onBack = navigator::back) }) {
        when {
            state.loading -> LoadingState(message = "Loading session")
            session == null -> ScreenHeader(title = "Session not found", centered = true)
            else -> {
                Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = formatDateTime(session.startedAt), style = typography.titleSmall, color = colors.textPrimary)
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = session.deviceName + (state.linkedLabel?.let { " · Part of your ${if (it == "Day 1") "Day 1 analysis" else "$it check-in"}" } ?: ""),
                        style = typography.caption,
                        color = colors.textMuted,
                        textAlign = TextAlign.Center,
                    )
                    Spacer(Modifier.height(10.dp))
                    Row {
                        if (session.isSimulated) SimulatedTag()
                        if (!session.isComplete) {
                            Spacer(Modifier.width(8.dp))
                            Tag(text = "Incomplete", tone = TagTone.ROSE)
                        }
                    }
                }
                Spacer(Modifier.height(spacing.md))
                FaceDiagram(
                    contentDescription = "Face map of the areas measured in this session.",
                    modifier = Modifier.fillMaxWidth().height(180.dp),
                    completedZones = session.completedRegions.map { it.zone() }.toSet(),
                    measurementPoints = MeasurementRegion.entries.map { it.zone() }.toSet(),
                )
                Spacer(Modifier.height(spacing.md))
                if (session.readings.isEmpty()) {
                    Text(text = "No readings were saved in this session.", style = typography.body, color = colors.textSecondary)
                } else {
                    ReadingsTable(session)
                }
                Spacer(Modifier.height(10.dp))
                Text(text = "Hydration is a relative index from the development simulator, not a clinical unit.", style = typography.caption, color = colors.textMuted)
                if (session.isSimulated) {
                    Spacer(Modifier.height(spacing.md))
                    SimulatedNote()
                }
            }
        }
    }
}

class ConnectedDeviceViewModel(
    private val probe: SkinProbeManager,
    private val profiles: UserProfileRepository,
    sessions: SkinProbeRepository,
) : ViewModel() {
    val probeState: StateFlow<ProbeState> = probe.state
    private val _paired = MutableStateFlow<String?>(null)
    val paired: StateFlow<String?> = _paired.asStateFlow()
    private val _busy = MutableStateFlow(false)
    val busy: StateFlow<Boolean> = _busy.asStateFlow()
    val lastSession: StateFlow<MeasurementSession?> = sessions.sessions().map { it.firstOrNull() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    init {
        viewModelScope.launch { _paired.value = profiles.current().pairedDeviceId }
    }

    fun reconnect() {
        val id = _paired.value ?: return
        viewModelScope.launch {
            _busy.value = true
            probe.acknowledgeError()
            probe.reconnect(id)
            _busy.value = false
        }
    }

    fun disconnect() {
        viewModelScope.launch { probe.disconnect() }
    }

    fun forget() {
        viewModelScope.launch {
            probe.disconnect()
            profiles.update { it.copy(pairedDeviceId = null) }
            _paired.value = null
        }
    }
}

/** The paired probe: status, details, and connection controls. Honest about the simulator. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ConnectedDeviceScreen() {
    val navigator = LocalAppNavigator.current
    val viewModel = containerViewModel { ConnectedDeviceViewModel(probeManager, profiles, probeRepository) }
    val state by viewModel.probeState.collectAsStateWithLifecycle()
    val paired by viewModel.paired.collectAsStateWithLifecycle()
    val busy by viewModel.busy.collectAsStateWithLifecycle()
    val last by viewModel.lastSession.collectAsStateWithLifecycle()
    val colors = SkinthesiaTheme.colors
    val typography = SkinthesiaTheme.typography
    val spacing = SkinthesiaTheme.spacing
    var confirmForget by remember { mutableStateOf(false) }
    val device = when (val s = state) {
        is ProbeState.Connected -> s.device
        is ProbeState.Ready -> s.device
        is ProbeState.Calibrating -> s.device
        else -> null
    }
    val failed = state as? ProbeState.Failed

    SkinthesiaScreen(
        topBar = { SkinthesiaTopBar(title = "Connected device", onBack = navigator::back) },
        bottomBar = {
            when {
                paired == null -> SkinthesiaPrimaryButton(text = "Pair a Probe", onClick = { navigator.navigate(ProbeConnectRoute(null, FlowKind.STANDALONE.name)) })
                device != null -> SkinthesiaSecondaryButton(text = "Disconnect", onClick = viewModel::disconnect)
                else -> SkinthesiaPrimaryButton(text = "Reconnect", loading = busy, enabled = !busy, onClick = viewModel::reconnect)
            }
            if (paired != null) SkinthesiaTextButton(text = "Forget this probe", onClick = { confirmForget = true })
        },
    ) {
        ProbeIllustration(
            state = if (device != null) ProbeVisualState.CONNECTED else ProbeVisualState.IDLE,
            modifier = Modifier.fillMaxWidth().height(190.dp),
        )
        Spacer(Modifier.height(spacing.sm))
        Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = "Skinthesia Probe", style = typography.title, color = colors.textPrimary)
            Spacer(Modifier.height(8.dp))
            Tag(
                text = when {
                    device != null -> "Connected"
                    paired != null -> "Paired · not connected"
                    else -> "Not paired"
                },
                tone = if (device != null) TagTone.SAGE else TagTone.NEUTRAL,
            )
        }
        failed?.let {
            Spacer(Modifier.height(spacing.md))
            InfoNotice(text = it.error.title + ". " + it.error.message, icon = SkinthesiaIcons.Alert)
        }
        Spacer(Modifier.height(spacing.lg))
        if (device != null) {
            SkinthesiaCard {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "Device", style = typography.label, color = colors.textSecondary, modifier = Modifier.weight(1f))
                    if (device.isSimulated) SimulatedTag()
                }
                Spacer(Modifier.height(6.dp))
                KeyValueRow(label = "Identifier", value = device.id)
                KeyValueRow(label = "Model", value = device.model)
                KeyValueRow(label = "Firmware", value = device.firmwareVersion ?: "Unknown")
                Row(Modifier.fillMaxWidth().height(40.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "Battery", style = typography.body, color = colors.textSecondary, modifier = Modifier.weight(1f))
                    BatteryMeter(device.batteryPercent)
                }
                Row(Modifier.fillMaxWidth().height(40.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "Signal", style = typography.body, color = colors.textSecondary, modifier = Modifier.weight(1f))
                    SignalBars(device.signalStrength)
                }
                Spacer(Modifier.height(8.dp))
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    device.capabilities.forEach { Tag(text = it.label, tone = TagTone.CLAY, icon = it.icon) }
                }
            }
        } else if (paired != null) {
            SkinthesiaCard {
                KeyValueRow(label = "Paired probe", value = paired.orEmpty())
                Text(text = "It connects automatically when you start a measurement.", style = typography.bodySmall, color = colors.textSecondary)
            }
        }
        last?.let { session ->
            Spacer(Modifier.height(spacing.md))
            SkinthesiaCard(onClick = { navigator.navigate(SessionDetailRoute(session.id)) }) {
                SectionOverline(text = "Last reading")
                Spacer(Modifier.height(4.dp))
                Text(text = session.label + " · " + formatDateTime(session.startedAt), style = typography.labelLarge, color = colors.textPrimary)
            }
        }
        Spacer(Modifier.height(spacing.md))
        SkinthesiaCard {
            Row(verticalAlignment = Alignment.Top) {
                IconBadge(SkinthesiaIcons.Info)
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(text = "Hardware readiness", style = typography.labelLarge, color = colors.textPrimary)
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "This build connects to a development simulator. A real probe will plug into the same connection layer once the Skinthesia hardware protocol is supplied. No Bluetooth identifiers, packet formats or sensor specifications are assumed.",
                        style = typography.bodySmall,
                        color = colors.textSecondary,
                    )
                }
            }
        }
        Spacer(Modifier.height(SkinthesiaTheme.spacing.md))
        SkinthesiaDivider()
    }

    if (confirmForget) {
        SkinthesiaConfirmDialog(
            title = "Forget this probe?",
            body = "You'll need to search for it again before your next measurement. Your readings are kept.",
            confirmLabel = "Forget",
            destructive = true,
            onConfirm = {
                viewModel.forget()
                confirmForget = false
            },
            onDismiss = { confirmForget = false },
        )
    }
}
