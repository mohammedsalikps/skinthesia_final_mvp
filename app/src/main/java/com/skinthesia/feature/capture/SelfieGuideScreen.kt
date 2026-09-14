package com.skinthesia.feature.capture

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.toRoute
import com.skinthesia.core.design.SkinthesiaTheme
import com.skinthesia.core.navigation.CameraRoute
import com.skinthesia.core.navigation.LocalAppNavigator
import com.skinthesia.core.navigation.SelfieGuideRoute
import com.skinthesia.core.navigation.containerViewModel
import com.skinthesia.core.navigation.toFlowKind
import com.skinthesia.core.ui.art.FaceDiagram
import com.skinthesia.core.ui.components.FaceGuideOverlay
import com.skinthesia.core.ui.components.FadeInUp
import com.skinthesia.core.ui.components.OnboardingHeader
import com.skinthesia.core.ui.components.OnboardingScaffold
import com.skinthesia.core.ui.components.SkinthesiaPrimaryButton
import com.skinthesia.core.ui.components.SkinthesiaTextButton
import com.skinthesia.core.ui.icons.SkinthesiaIcons
import com.skinthesia.feature.onboarding.OnboardingSteps

/** Screen 03 introduction: what makes a good skin photo, before the camera opens. */
@Composable
fun SelfieGuideScreen() {
    val navigator = LocalAppNavigator.current
    val viewModel = containerViewModel { handle ->
        val route = handle.toRoute<SelfieGuideRoute>()
        CaptureViewModel(route.assessmentId, photoStore, attachPhoto, clock, route.flow)
    }
    val state by viewModel.state.collectAsStateWithLifecycle()
    val route = viewModel.assessmentId
    val flow = viewModel.flow.toFlowKind()
    val colors = SkinthesiaTheme.colors
    val typography = SkinthesiaTheme.typography
    val spacing = SkinthesiaTheme.spacing
    val motion = SkinthesiaTheme.motion
    val gallery = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) viewModel.importPhoto(uri.toString()) { navigator.photoCaptured(route, flow) }
    }

    OnboardingScaffold(
        step = OnboardingSteps.SELFIE,
        totalSteps = OnboardingSteps.TOTAL,
        onBack = navigator::back,
        bottomBar = {
            SkinthesiaPrimaryButton(text = "Open Camera", onClick = { navigator.navigate(CameraRoute(route, flow.name)) }, loading = state.processing)
            SkinthesiaTextButton(
                text = "Choose from gallery",
                leadingIcon = SkinthesiaIcons.Gallery,
                onClick = { gallery.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
            )
        },
    ) {
        OnboardingHeader(title = "Let's take your selfie", subtitle = "A clear, natural photo lets us see\nyour skin as it really is.")
        Spacer(Modifier.height(spacing.lg))
        FadeInUp {
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.62f)
                    .aspectRatio(0.8f)
                    .clip(SkinthesiaTheme.shapes.image)
                    .background(colors.surface)
                    .border(1.dp, colors.border, SkinthesiaTheme.shapes.image),
                contentAlignment = Alignment.Center,
            ) {
                FaceDiagram(contentDescription = "Face guide", modifier = Modifier.fillMaxSize().padding(22.dp), scanning = true)
                FaceGuideOverlay(guideColor = androidx.compose.ui.graphics.Color.Transparent)
            }
        }
        Spacer(Modifier.height(spacing.lg))
        val tips = listOf(
            Triple(SkinthesiaIcons.Sun, "Good lighting", "Face a window or soft daylight."),
            Triple(SkinthesiaIcons.Face, "Face centred", "Keep your whole face inside the guide."),
            Triple(SkinthesiaIcons.Eye, "No filters", "Turn off beauty filters and portrait mode."),
            Triple(SkinthesiaIcons.Droplet, "Avoid heavy makeup", "Bare or light makeup gives truer results."),
            Triple(SkinthesiaIcons.Scan, "Follow the guidance", "We'll check light and position after you capture."),
        )
        Column(verticalArrangement = Arrangement.spacedBy(14.dp), modifier = Modifier.fillMaxWidth()) {
            tips.forEachIndexed { index, (icon, title, body) ->
                FadeInUp(delayMillis = motion.stagger(index + 1)) { TipRow(icon, title, body) }
            }
        }
        Spacer(Modifier.height(spacing.md))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(SkinthesiaIcons.Lock, contentDescription = null, tint = colors.textMuted, modifier = Modifier.size(14.dp))
            Spacer(Modifier.width(6.dp))
            Text(text = "Your photo stays privately on this device.", style = typography.caption, color = colors.textMuted)
        }
        state.error?.let {
            Spacer(Modifier.height(spacing.sm))
            Text(text = it, style = typography.bodySmall, color = colors.warningStrong)
        }
    }
}

@Composable
internal fun TipRow(icon: ImageVector, title: String, body: String) {
    val colors = SkinthesiaTheme.colors
    val typography = SkinthesiaTheme.typography
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
        Box(Modifier.size(40.dp).clip(CircleShape).background(colors.primaryMist), contentAlignment = Alignment.Center) {
            Icon(icon, contentDescription = null, tint = colors.primary, modifier = Modifier.size(19.dp))
        }
        Spacer(Modifier.width(14.dp))
        Column {
            Text(text = title, style = typography.labelLarge, color = colors.textPrimary)
            Text(text = body, style = typography.bodySmall, color = colors.textSecondary)
        }
    }
}
