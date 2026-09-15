package com.skinthesia.feature.analysis

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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import com.skinthesia.core.navigation.SensorAnalysisRoute
import com.skinthesia.core.navigation.containerViewModel
import com.skinthesia.core.navigation.toFlowKind
import com.skinthesia.core.ui.components.FadeInUp
import com.skinthesia.core.ui.components.LoadingState
import com.skinthesia.core.ui.components.ScreenHeader
import com.skinthesia.core.ui.components.SkinthesiaCard
import com.skinthesia.core.ui.components.SkinthesiaPrimaryButton
import com.skinthesia.core.ui.components.Tag
import com.skinthesia.core.ui.components.TagTone
import com.skinthesia.domain.model.MeasurementRegion
import com.skinthesia.domain.model.MeasurementSession
import com.skinthesia.domain.model.SensorType
import com.skinthesia.domain.repository.AssessmentRepository
import com.skinthesia.domain.repository.SkinProbeRepository
import com.skinthesia.feature.common.FlowScaffold
import com.skinthesia.feature.measurement.SimulatedNote
import com.skinthesia.feature.measurement.SimulatedTag
import com.skinthesia.feature.measurement.caption
import com.skinthesia.feature.measurement.format
import com.skinthesia.feature.measurement.icon
import com.skinthesia.feature.onboarding.OnboardingSteps
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class SensorReadout(val sensor: SensorType, val average: Double, val byRegion: List<Pair<MeasurementRegion, Double>>)

data class SensorAnalysisUiState(
    val loading: Boolean = true,
    val session: MeasurementSession? = null,
    val readouts: List<SensorReadout> = emptyList(),
)

class SensorAnalysisViewModel(
    val assessmentId: String?,
    val sessionId: String?,
    val flow: FlowKind,
    private val assessments: AssessmentRepository,
    private val sessions: SkinProbeRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(SensorAnalysisUiState())
    val state: StateFlow<SensorAnalysisUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            val id = sessionId ?: assessmentId?.let { assessments.get(it)?.measurementSessionId }
            val session = id?.let { sessions.session(it) }
            val readouts = session?.let { s ->
                SensorType.entries.mapNotNull { sensor ->
                    val average = s.average(sensor) ?: return@mapNotNull null
                    SensorReadout(sensor, average, MeasurementRegion.entries.mapNotNull { region -> s.value(region, sensor)?.let { region to it } })
                }
            }.orEmpty()
            _state.value = SensorAnalysisUiState(loading = false, session = session, readouts = readouts)
        }
    }
}

/** Plain, non-diagnostic notes derived only from the readings themselves. */
private fun noteFor(readout: SensorReadout): String = when (readout.sensor) {
    SensorType.PH -> "Skin's surface is naturally mildly acidic. We'll watch how your average moves over time."
    SensorType.HYDRATION -> {
        val forehead = readout.byRegion.firstOrNull { it.first == MeasurementRegion.FOREHEAD }?.second
        val cheeks = readout.byRegion.filter { it.first != MeasurementRegion.FOREHEAD }.map { it.second }.takeIf { it.isNotEmpty() }?.average()
        val comparison = when {
            forehead == null || cheeks == null -> ""
            forehead - cheeks >= 6 -> " Your cheeks read drier than your forehead."
            cheeks - forehead >= 6 -> " Your forehead read drier than your cheeks."
            else -> " It read fairly evenly across your face."
        }
        "A relative index: higher suggests better-hydrated skin.$comparison"
    }
    SensorType.TEMPERATURE -> "Recorded alongside the other readings as context."
    SensorType.SEBUM -> "A relative index: higher suggests more oil at the surface. The T-zone often reads higher than the cheeks."
    SensorType.SKIN_BARRIER -> "A relative index: higher suggests a stronger, more resilient skin barrier."
}

/** Screen 18: probe readings per sensor and region, labelled as simulated when they are. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SensorAnalysisScreen() {
    val navigator = LocalAppNavigator.current
    val viewModel = containerViewModel { handle ->
        val route = handle.toRoute<SensorAnalysisRoute>()
        SensorAnalysisViewModel(route.assessmentId, route.sessionId, route.flow.toFlowKind(), assessments, probeRepository)
    }
    val state by viewModel.state.collectAsStateWithLifecycle()
    val colors = SkinthesiaTheme.colors
    val typography = SkinthesiaTheme.typography
    val spacing = SkinthesiaTheme.spacing
    val motion = SkinthesiaTheme.motion
    val session = state.session
    val withAssessment = viewModel.assessmentId != null && viewModel.flow != FlowKind.STANDALONE

    FlowScaffold(
        flow = viewModel.flow,
        step = OnboardingSteps.ANALYSIS,
        title = "Probe readings",
        onBack = navigator::back,
        bottomBar = {
            SkinthesiaPrimaryButton(
                text = if (withAssessment) "Combine With My Photo" else "Done",
                onClick = { navigator.sensorAnalysisDone(viewModel.assessmentId, viewModel.flow) },
                enabled = !state.loading,
            )
        },
    ) {
        when {
            state.loading -> LoadingState(message = "Loading your readings")
            session == null || state.readouts.isEmpty() -> ScreenHeader(
                title = "No probe readings yet",
                subtitle = "Connect your probe from the Analyze tab to add hydration, pH and temperature readings.",
                centered = true,
            )
            else -> {
                ScreenHeader(
                    title = "Your probe readings",
                    subtitle = "Averages across your forehead and both cheeks.",
                    centered = true,
                )
                Spacer(Modifier.height(12.dp))
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Tag(text = session.label, tone = TagTone.NEUTRAL)
                    Tag(text = "${session.completedRegions.size} areas", tone = TagTone.CLAY)
                    if (session.isSimulated) SimulatedTag()
                }
                Spacer(Modifier.height(spacing.lg))
                state.readouts.forEachIndexed { index, readout ->
                    FadeInUp(delayMillis = motion.stagger(index)) {
                        SensorReadoutCard(readout, simulated = session.isSimulated, delayMillis = 200 + index * 150)
                    }
                    Spacer(Modifier.height(spacing.md))
                }
                Text(
                    text = "Readings vary with the time of day and your recent routine, so we focus on trends rather than single values.",
                    style = typography.bodySmall,
                    color = colors.textSecondary,
                )
                if (session.isSimulated) {
                    Spacer(Modifier.height(spacing.md))
                    SimulatedNote()
                }
            }
        }
    }
}

@Composable
private fun SensorReadoutCard(readout: SensorReadout, simulated: Boolean, delayMillis: Int) {
    val colors = SkinthesiaTheme.colors
    val typography = SkinthesiaTheme.typography
    SkinthesiaCard {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconBadge(readout.sensor.icon)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(text = readout.sensor.caption, style = typography.labelLarge, color = colors.textPrimary)
                Text(text = "Average of ${readout.byRegion.size} areas", style = typography.caption, color = colors.textMuted)
            }
            if (simulated) SimulatedTag()
        }
        Spacer(Modifier.height(12.dp))
        Text(
            text = readout.sensor.format(readout.average),
            style = typography.metricLarge,
            color = colors.textPrimary,
            modifier = Modifier.semantics { },
        )
        Spacer(Modifier.height(10.dp))
        val max = readout.byRegion.maxOfOrNull { it.second }?.takeIf { it > 0 } ?: 1.0
        readout.byRegion.forEachIndexed { index, (region, value) ->
            Row(Modifier.fillMaxWidth().height(30.dp).semantics(mergeDescendants = true) {}, verticalAlignment = Alignment.CenterVertically) {
                Text(text = region.label, style = typography.bodySmall, color = colors.textSecondary, modifier = Modifier.width(92.dp))
                LevelBar(
                    fraction = (value / max).toFloat(),
                    color = colors.primary.copy(alpha = 0.75f),
                    modifier = Modifier.weight(1f),
                    delayMillis = delayMillis + index * 90,
                )
                Spacer(Modifier.width(12.dp))
                Text(
                    text = readout.sensor.format(value),
                    style = typography.numeric,
                    color = colors.textPrimary,
                    textAlign = TextAlign.End,
                    modifier = Modifier.width(60.dp),
                )
            }
        }
        Spacer(Modifier.height(10.dp))
        Text(text = noteFor(readout), style = typography.bodySmall, color = colors.textSecondary)
    }
}
