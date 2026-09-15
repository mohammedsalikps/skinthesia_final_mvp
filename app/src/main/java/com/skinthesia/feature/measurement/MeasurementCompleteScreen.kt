package com.skinthesia.feature.measurement

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import com.skinthesia.core.navigation.MeasurementCompleteRoute
import com.skinthesia.core.navigation.containerViewModel
import com.skinthesia.core.navigation.toFlowKind
import com.skinthesia.core.ui.art.FaceDiagram
import com.skinthesia.core.ui.art.zone
import com.skinthesia.core.ui.components.FadeInUp
import com.skinthesia.core.ui.components.SkinthesiaCard
import com.skinthesia.core.ui.components.SkinthesiaDivider
import com.skinthesia.core.ui.components.SkinthesiaPrimaryButton
import com.skinthesia.core.ui.components.Tag
import com.skinthesia.core.ui.components.TagTone
import com.skinthesia.core.ui.icons.SkinthesiaIcons
import com.skinthesia.domain.model.MeasurementRegion
import com.skinthesia.domain.model.MeasurementSession
import com.skinthesia.domain.model.OnboardingStage
import com.skinthesia.domain.model.SensorType
import com.skinthesia.domain.repository.SkinProbeRepository
import com.skinthesia.domain.repository.UserProfileRepository
import com.skinthesia.feature.common.FlowScaffold
import com.skinthesia.feature.onboarding.OnboardingSteps
import com.skinthesia.feature.onboarding.atLeast
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MeasurementCompleteViewModel(
    val sessionId: String,
    val assessmentId: String?,
    val flow: FlowKind,
    private val sessions: SkinProbeRepository,
    private val profiles: UserProfileRepository,
) : ViewModel() {

    val session: StateFlow<MeasurementSession?> = sessions.observeSession(sessionId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    init {
        viewModelScope.launch {
            val current = sessions.session(sessionId)
            if (current != null && !current.isComplete) sessions.completeSession(sessionId)
            if (flow == FlowKind.ONBOARDING) {
                profiles.update { it.copy(onboardingStage = it.onboardingStage.atLeast(OnboardingStage.ANALYSIS)) }
            }
        }
    }
}

/** Screen 16: the session summary, clearly labelled when the readings are simulated. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MeasurementCompleteScreen() {
    val navigator = LocalAppNavigator.current
    val viewModel = containerViewModel { handle ->
        val route = handle.toRoute<MeasurementCompleteRoute>()
        MeasurementCompleteViewModel(route.sessionId, route.assessmentId, route.flow.toFlowKind(), probeRepository, profiles)
    }
    val session by viewModel.session.collectAsStateWithLifecycle()
    val colors = SkinthesiaTheme.colors
    val typography = SkinthesiaTheme.typography
    val spacing = SkinthesiaTheme.spacing
    val motion = SkinthesiaTheme.motion
    val standalone = viewModel.flow == FlowKind.STANDALONE

    FlowScaffold(
        flow = viewModel.flow,
        step = OnboardingSteps.PROBE,
        title = "Measurement complete",
        onBack = navigator::back,
        bottomBar = {
            SkinthesiaPrimaryButton(
                text = if (standalone) "View Readings" else "Analyze My Skin",
                onClick = { navigator.afterProbe(viewModel.assessmentId, viewModel.sessionId, viewModel.flow) },
                enabled = session != null,
            )
        },
    ) {
        FadeInUp {
            Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    Modifier.size(64.dp).clip(CircleShape).background(colors.success.copy(alpha = 0.14f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(SkinthesiaIcons.Check, contentDescription = null, tint = colors.success, modifier = Modifier.size(28.dp))
                }
                Spacer(Modifier.height(spacing.md))
                Text(text = "Measurement complete", style = typography.title, color = colors.textPrimary, textAlign = TextAlign.Center)
                Spacer(Modifier.height(8.dp))
                Text(
                    text = if (standalone) {
                        "Three areas recorded and saved to your measurement history."
                    } else {
                        "Three areas recorded. Next, we'll combine these readings with your photo."
                    },
                    style = typography.subtitle,
                    color = colors.textSecondary,
                    textAlign = TextAlign.Center,
                )
                Spacer(Modifier.height(14.dp))
                session?.let { s ->
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Tag(text = s.label, tone = TagTone.NEUTRAL)
                        Tag(text = s.deviceName, tone = TagTone.CLAY)
                        if (s.isSimulated) SimulatedTag()
                    }
                }
            }
        }
        Spacer(Modifier.height(spacing.md))
        FadeInUp(delayMillis = motion.stagger(1)) {
            FaceDiagram(
                contentDescription = "Face map with the forehead, left cheek and right cheek measured.",
                modifier = Modifier.fillMaxWidth().height(190.dp),
                completedZones = MeasurementRegion.entries.map { it.zone() }.toSet(),
                measurementPoints = MeasurementRegion.entries.map { it.zone() }.toSet(),
            )
        }
        Spacer(Modifier.height(spacing.md))
        session?.let { s ->
            FadeInUp(delayMillis = motion.stagger(2)) { ReadingsTable(s) }
            Spacer(Modifier.height(10.dp))
            Text(
                text = "Hydration is a relative index from the development simulator, not a clinical unit.",
                style = typography.caption,
                color = colors.textMuted,
            )
            if (s.isSimulated) {
                Spacer(Modifier.height(spacing.md))
                SimulatedNote()
            }
        }
    }
}

@Composable
internal fun ReadingsTable(session: MeasurementSession) {
    val colors = SkinthesiaTheme.colors
    val typography = SkinthesiaTheme.typography
    val sensors = SensorType.entries.filter { s -> session.readings.any { it.sensor == s } }
    SkinthesiaCard {
        Row(Modifier.fillMaxWidth().padding(bottom = 10.dp)) {
            Text(text = "Area", style = typography.caption, color = colors.textMuted, modifier = Modifier.weight(1.3f))
            sensors.forEach { s ->
                Text(text = s.shortLabel, style = typography.caption, color = colors.textMuted, textAlign = TextAlign.End, modifier = Modifier.weight(1f))
            }
        }
        SkinthesiaDivider()
        MeasurementRegion.entries.forEach { region ->
            Row(
                Modifier.fillMaxWidth().height(46.dp).semantics(mergeDescendants = true) {},
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(text = region.label, style = typography.body, color = colors.textPrimary, modifier = Modifier.weight(1.3f))
                sensors.forEach { s ->
                    Text(
                        text = session.value(region, s)?.let { s.format(it) } ?: "—",
                        style = typography.numeric,
                        color = colors.textPrimary,
                        textAlign = TextAlign.End,
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }
        SkinthesiaDivider()
        Row(
            Modifier.fillMaxWidth().padding(top = 12.dp).semantics(mergeDescendants = true) {},
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(text = "Average", style = typography.labelLarge, color = colors.textPrimary, modifier = Modifier.weight(1.3f))
            sensors.forEach { s ->
                Text(
                    text = session.average(s)?.let { s.format(it) } ?: "—",
                    style = typography.numeric,
                    color = colors.primary,
                    textAlign = TextAlign.End,
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}
