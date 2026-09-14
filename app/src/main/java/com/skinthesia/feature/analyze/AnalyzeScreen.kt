package com.skinthesia.feature.analyze

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.skinthesia.ai.progress.JourneyClock
import com.skinthesia.core.design.SkinthesiaTheme
import com.skinthesia.core.navigation.CheckInIntroRoute
import com.skinthesia.core.navigation.FlowKind
import com.skinthesia.core.navigation.LocalAppNavigator
import com.skinthesia.core.navigation.MeasurementHistoryRoute
import com.skinthesia.core.navigation.ProbeConnectRoute
import com.skinthesia.core.navigation.ProgressComparisonRoute
import com.skinthesia.core.navigation.SkinPrintRoute
import com.skinthesia.core.navigation.containerViewModel
import com.skinthesia.core.ui.art.FaceDiagram
import com.skinthesia.core.ui.components.FadeInUp
import com.skinthesia.core.ui.components.InfoNotice
import com.skinthesia.core.ui.components.ListRow
import com.skinthesia.core.ui.components.LoadingState
import com.skinthesia.core.ui.components.ScreenHeader
import com.skinthesia.core.ui.components.SectionOverline
import com.skinthesia.core.ui.components.SkinthesiaCard
import com.skinthesia.core.ui.components.SkinthesiaDivider
import com.skinthesia.core.ui.components.SkinthesiaScreen
import com.skinthesia.core.ui.components.Tag
import com.skinthesia.core.ui.components.TagTone
import com.skinthesia.core.ui.icons.SkinthesiaIcons
import com.skinthesia.domain.model.Assessment
import com.skinthesia.domain.model.AssessmentKind
import com.skinthesia.domain.model.ProbeState
import com.skinthesia.domain.model.UserProfile
import com.skinthesia.domain.repository.AssessmentRepository
import com.skinthesia.domain.repository.SettingsRepository
import com.skinthesia.domain.repository.SkinProbeRepository
import com.skinthesia.domain.repository.UserProfileRepository
import com.skinthesia.feature.analysis.IconBadge
import com.skinthesia.feature.measurement.SimulatedTag
import com.skinthesia.hardware.probe.SkinProbeManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class AnalyzeUiState(
    val loading: Boolean = true,
    val latest: Assessment? = null,
    val nextWeek: Int = 4,
    val programmeComplete: Boolean = false,
    val hasCheckIns: Boolean = false,
    val pairedDeviceId: String? = null,
    val probe: ProbeState = ProbeState.Idle,
    val sessionCount: Int = 0,
    val probeSimulated: Boolean = true,
)

class AnalyzeViewModel(
    private val profiles: UserProfileRepository,
    assessments: AssessmentRepository,
    settings: SettingsRepository,
    probe: SkinProbeManager,
    sessions: SkinProbeRepository,
    private val journeyClock: JourneyClock,
) : ViewModel() {

    private val profile = MutableStateFlow(UserProfile())

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch { profile.value = profiles.current() }
    }

    val state: StateFlow<AnalyzeUiState> = combine(
        assessments.completed(),
        settings.settings,
        profile,
        probe.state,
        sessions.sessions(),
    ) { completed, s, p, probeState, history ->
        val baseline = completed.firstOrNull { it.kind == AssessmentKind.BASELINE }
        val weeks = completed.map { it.week }
        AnalyzeUiState(
            loading = false,
            latest = completed.lastOrNull { it.skinPrint != null },
            nextWeek = journeyClock.nextCheckInWeek(baseline?.startedAt, weeks, s.demoTimeline),
            programmeComplete = (weeks.maxOrNull() ?: 0) >= p.goals.durationWeeks,
            hasCheckIns = completed.any { it.kind == AssessmentKind.CHECK_IN },
            pairedDeviceId = p.pairedDeviceId,
            probe = probeState,
            sessionCount = history.size,
            probeSimulated = history.firstOrNull()?.isSimulated ?: true,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), AnalyzeUiState())
}

/** The Analyze tab: weekly check-in first, then probe readings, comparisons and history. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AnalyzeScreen() {
    val navigator = LocalAppNavigator.current
    val viewModel = containerViewModel { AnalyzeViewModel(profiles, assessments, settings, probeManager, probeRepository, journeyClock) }
    val state by viewModel.state.collectAsStateWithLifecycle()
    val colors = SkinthesiaTheme.colors
    val typography = SkinthesiaTheme.typography
    val spacing = SkinthesiaTheme.spacing
    LaunchedEffect(Unit) { viewModel.refresh() }

    SkinthesiaScreen(insetBottom = false) {
        if (state.loading) {
            LoadingState(message = "Loading")
            return@SkinthesiaScreen
        }
        Spacer(Modifier.height(spacing.md))
        ScreenHeader(
            overline = "Analyze",
            title = "Measure what's changing",
            subtitle = "Weekly check-ins pair a selfie with your probe. Take a probe reading any time.",
        )
        Spacer(Modifier.height(spacing.lg))
        FadeInUp {
            SkinthesiaCard(onClick = { navigator.navigate(CheckInIntroRoute) }) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        SectionOverline(text = if (state.programmeComplete) "Programme complete" else "Recommended")
                        Spacer(Modifier.height(6.dp))
                        Text(
                            text = if (state.programmeComplete) "Bonus check-in" else "Week ${state.nextWeek} check-in",
                            style = typography.titleMedium,
                            color = colors.textPrimary,
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(text = "Selfie, probe reading and progress comparison. About three minutes.", style = typography.bodySmall, color = colors.textSecondary)
                    }
                    Spacer(Modifier.width(12.dp))
                    FaceDiagram(contentDescription = "", modifier = Modifier.size(width = 84.dp, height = 104.dp), scanning = true)
                }
                Spacer(Modifier.height(12.dp))
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Tag(text = "Selfie", tone = TagTone.MIST, icon = SkinthesiaIcons.Camera)
                    Tag(text = "Probe", tone = TagTone.CLAY, icon = SkinthesiaIcons.Probe)
                    Tag(text = "Compare", tone = TagTone.SAGE, icon = SkinthesiaIcons.Trend)
                }
            }
        }
        Spacer(Modifier.height(spacing.md))
        SkinthesiaCard {
            ListRow(
                title = "Probe reading",
                subtitle = "Hydration, pH and temperature from three areas",
                leadingIcon = SkinthesiaIcons.Probe,
                onClick = { navigator.navigate(ProbeConnectRoute(null, FlowKind.STANDALONE.name)) },
            )
            SkinthesiaDivider()
            ListRow(
                title = "Progress comparison",
                subtitle = if (state.hasCheckIns) "Your latest check-in against Day 1" else "Available after your first check-in",
                leadingIcon = SkinthesiaIcons.Trend,
                onClick = if (state.hasCheckIns) ({ navigator.navigate(ProgressComparisonRoute()) }) else null,
                titleColor = if (state.hasCheckIns) colors.textPrimary else colors.textMuted,
            )
            state.latest?.let { latest ->
                SkinthesiaDivider()
                ListRow(
                    title = "Latest SkinPrint",
                    subtitle = latest.weekLabel + (latest.skinPrint?.let { " · ${it.overall} of 100" } ?: ""),
                    leadingIcon = SkinthesiaIcons.SkinPrint,
                    onClick = { navigator.navigate(SkinPrintRoute(latest.id)) },
                )
            }
            SkinthesiaDivider()
            ListRow(
                title = "Measurement history",
                subtitle = when (state.sessionCount) {
                    0 -> "No probe sessions yet"
                    1 -> "1 probe session"
                    else -> "${state.sessionCount} probe sessions"
                },
                leadingIcon = SkinthesiaIcons.Clock,
                onClick = { navigator.navigate(MeasurementHistoryRoute) },
            )
        }
        Spacer(Modifier.height(spacing.md))
        SkinthesiaCard {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconBadge(SkinthesiaIcons.Bluetooth)
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(text = "Skinthesia Probe", style = typography.labelLarge, color = colors.textPrimary)
                    val device = when (val s = state.probe) {
                        is ProbeState.Connected -> s.device
                        is ProbeState.Ready -> s.device
                        is ProbeState.Calibrating -> s.device
                        else -> null
                    }
                    Text(
                        text = when {
                            device != null -> "Connected · ${device.id}"
                            state.pairedDeviceId != null -> "Paired · connects when you measure"
                            else -> "Not paired yet"
                        },
                        style = typography.caption,
                        color = colors.textMuted,
                    )
                }
                if (state.pairedDeviceId != null && state.probeSimulated) SimulatedTag()
            }
        }
        Spacer(Modifier.height(spacing.md))
        InfoNotice(text = "In this build, photo analysis uses a development model and probe readings are simulated. Both are labelled wherever they appear.")
        Spacer(Modifier.height(spacing.xl))
    }
}
