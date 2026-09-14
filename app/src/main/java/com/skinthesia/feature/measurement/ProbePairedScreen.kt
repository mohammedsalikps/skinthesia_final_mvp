package com.skinthesia.feature.measurement

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.toRoute
import com.skinthesia.core.design.SkinthesiaTheme
import com.skinthesia.core.navigation.LocalAppNavigator
import com.skinthesia.core.navigation.ProbePairedRoute
import com.skinthesia.core.navigation.containerViewModel
import com.skinthesia.core.navigation.toFlowKind
import com.skinthesia.core.ui.art.ProbeIllustration
import com.skinthesia.core.ui.art.ProbeVisualState
import com.skinthesia.core.ui.components.FadeInUp
import com.skinthesia.core.ui.components.KeyValueRow
import com.skinthesia.core.ui.components.SkinthesiaCard
import com.skinthesia.core.ui.components.SkinthesiaDivider
import com.skinthesia.core.ui.components.SkinthesiaPrimaryButton
import com.skinthesia.core.ui.components.SkinthesiaTextButton
import com.skinthesia.core.ui.components.Tag
import com.skinthesia.core.ui.components.TagTone
import com.skinthesia.core.ui.icons.SkinthesiaIcons
import com.skinthesia.domain.model.ProbeState
import com.skinthesia.feature.common.FlowScaffold
import com.skinthesia.feature.onboarding.OnboardingSteps

/** Screen 11: the paired device, its battery, signal, firmware and sensors. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ProbePairedScreen() {
    val navigator = LocalAppNavigator.current
    val viewModel = containerViewModel { handle ->
        val route = handle.toRoute<ProbePairedRoute>()
        ProbeConnectViewModel(route.assessmentId, route.flow.toFlowKind(), probeManager, profiles, assessments)
    }
    val state by viewModel.state.collectAsStateWithLifecycle()
    val colors = SkinthesiaTheme.colors
    val typography = SkinthesiaTheme.typography
    val spacing = SkinthesiaTheme.spacing
    val motion = SkinthesiaTheme.motion
    val device = when (val s = state) {
        is ProbeState.Connected -> s.device
        is ProbeState.Ready -> s.device
        is ProbeState.Calibrating -> s.device
        else -> null
    }

    FlowScaffold(
        flow = viewModel.flow,
        step = OnboardingSteps.PROBE,
        title = "Your probe",
        onBack = navigator::back,
        bottomBar = {
            if (device != null) {
                SkinthesiaPrimaryButton(text = "Continue", onClick = { navigator.calibrate(viewModel.assessmentId, viewModel.flow) })
            } else {
                SkinthesiaPrimaryButton(text = "Reconnect", onClick = navigator::back)
            }
            SkinthesiaTextButton(text = "Not your probe? Search again", onClick = navigator::back)
        },
    ) {
        FadeInUp {
            ProbeIllustration(state = if (device != null) ProbeVisualState.CONNECTED else ProbeVisualState.IDLE, modifier = Modifier.fillMaxWidth().height(210.dp))
        }
        Spacer(Modifier.height(spacing.sm))
        FadeInUp(delayMillis = motion.stagger(1)) {
            Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = device?.name ?: "Skinthesia Probe", style = typography.title, color = colors.textPrimary, textAlign = TextAlign.Center)
                Spacer(Modifier.height(10.dp))
                Tag(
                    text = if (device != null) "Connected" else "Disconnected",
                    tone = if (device != null) TagTone.SAGE else TagTone.ROSE,
                    icon = if (device != null) SkinthesiaIcons.Check else SkinthesiaIcons.Alert,
                )
            }
        }
        Spacer(Modifier.height(spacing.lg))
        if (device != null) {
            FadeInUp(delayMillis = motion.stagger(2)) {
                SkinthesiaCard {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "Device", style = typography.label, color = colors.textSecondary, modifier = Modifier.weight(1f))
                        if (device.isSimulated) SimulatedTag()
                    }
                    Spacer(Modifier.height(6.dp))
                    KeyValueRow(label = "Identifier", value = device.id)
                    KeyValueRow(label = "Model", value = device.model)
                    KeyValueRow(label = "Firmware", value = device.firmwareVersion ?: "Unknown")
                    SkinthesiaDivider(Modifier.padding(vertical = 6.dp))
                    Row(Modifier.fillMaxWidth().height(40.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "Battery", style = typography.body, color = colors.textSecondary, modifier = Modifier.weight(1f))
                        BatteryMeter(device.batteryPercent)
                    }
                    Row(Modifier.fillMaxWidth().height(40.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "Signal", style = typography.body, color = colors.textSecondary, modifier = Modifier.weight(1f))
                        SignalBars(device.signalStrength)
                        Spacer(Modifier.width(8.dp))
                        Text(text = device.signalStrength?.let { "$it dBm" } ?: "", style = typography.numeric, color = colors.textSecondary)
                    }
                    SkinthesiaDivider(Modifier.padding(vertical = 6.dp))
                    Text(text = "Sensors reported by this device", style = typography.label, color = colors.textSecondary)
                    Spacer(Modifier.height(10.dp))
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        device.capabilities.forEach { sensor -> Tag(text = sensor.label, tone = TagTone.CLAY, icon = sensor.icon) }
                    }
                }
            }
            Spacer(Modifier.height(spacing.md))
            SimulatedNote()
        }
    }
}
