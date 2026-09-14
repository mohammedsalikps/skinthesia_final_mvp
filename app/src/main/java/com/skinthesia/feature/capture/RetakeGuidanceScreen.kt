package com.skinthesia.feature.capture

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.toRoute
import com.skinthesia.core.design.SkinthesiaTheme
import com.skinthesia.core.navigation.LocalAppNavigator
import com.skinthesia.core.navigation.RetakeGuidanceRoute
import com.skinthesia.core.navigation.containerViewModel
import com.skinthesia.core.navigation.toFlowKind
import com.skinthesia.core.ui.components.FadeInUp
import com.skinthesia.core.ui.components.LoadingState
import com.skinthesia.core.ui.components.OnboardingScaffold
import com.skinthesia.core.ui.components.SkinthesiaCard
import com.skinthesia.core.ui.components.SkinthesiaImage
import com.skinthesia.core.ui.components.SkinthesiaPrimaryButton
import com.skinthesia.core.ui.components.SkinthesiaTextButton
import com.skinthesia.core.ui.components.Tag
import com.skinthesia.core.ui.components.TagTone
import com.skinthesia.core.ui.icons.SkinthesiaIcons
import com.skinthesia.domain.model.CheckStatus
import com.skinthesia.domain.model.ImageSource
import com.skinthesia.domain.model.QualityDimension
import com.skinthesia.feature.onboarding.OnboardingSteps

private val QualityDimension.illustration: ImageVector
    get() = when (this) {
        QualityDimension.LIGHTING, QualityDimension.EXPOSURE -> SkinthesiaIcons.Sun
        QualityDimension.SHARPNESS -> SkinthesiaIcons.Camera
        QualityDimension.FACE_VISIBILITY, QualityDimension.FACE_ANGLE -> SkinthesiaIcons.Face
        QualityDimension.FRAMING -> SkinthesiaIcons.Target
        QualityDimension.RESOLUTION -> SkinthesiaIcons.Gallery
    }

/**
 * Screen 05: specific, kind guidance for the one thing to fix, with practical tips and
 * a clear Retake Photo action. The user can also proceed knowingly.
 */
@Composable
fun RetakeGuidanceScreen() {
    val navigator = LocalAppNavigator.current
    val viewModel = containerViewModel { handle ->
        val route = handle.toRoute<RetakeGuidanceRoute>()
        AssessmentViewModel(route.assessmentId, route.flow.toFlowKind(), assessments)
    }
    val assessment by viewModel.assessment.collectAsStateWithLifecycle()
    val colors = SkinthesiaTheme.colors
    val typography = SkinthesiaTheme.typography
    val spacing = SkinthesiaTheme.spacing
    val motion = SkinthesiaTheme.motion
    val quality = assessment?.quality
    val guidance = quality?.guidance
    val issue = quality?.primaryIssue ?: QualityDimension.LIGHTING

    OnboardingScaffold(
        step = OnboardingSteps.SELFIE,
        totalSteps = OnboardingSteps.TOTAL,
        onBack = navigator::back,
        bottomBar = {
            SkinthesiaPrimaryButton(text = "Retake Photo", onClick = { navigator.retakePhoto(viewModel.assessmentId, viewModel.flow) })
            SkinthesiaTextButton(text = "Use this photo anyway", onClick = { navigator.photoAccepted(viewModel.assessmentId, viewModel.flow) })
        },
    ) {
        if (quality == null || guidance == null) {
            LoadingState(message = "Preparing guidance…")
            return@OnboardingScaffold
        }
        Spacer(Modifier.height(spacing.sm))
        FadeInUp {
            Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                Box(
                    modifier = Modifier
                        .size(148.dp)
                        .clip(CircleShape)
                        .background(colors.blushMist)
                        .border(1.dp, colors.border, CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(issue.illustration, contentDescription = null, tint = colors.primary, modifier = Modifier.size(56.dp))
                }
                assessment?.photo?.let { photo ->
                    Box(
                        modifier = Modifier
                            .offset(x = 62.dp, y = 48.dp)
                            .size(64.dp)
                            .clip(SkinthesiaTheme.shapes.medium)
                            .border(2.dp, colors.surfaceElevated, SkinthesiaTheme.shapes.medium),
                    ) {
                        SkinthesiaImage(source = ImageSource.LocalFile(photo.filePath), contentDescription = "Your last photo", modifier = Modifier.fillMaxSize(), maxDimension = 300)
                    }
                }
            }
        }
        Spacer(Modifier.height(spacing.lg))
        FadeInUp(delayMillis = motion.stagger(1)) {
            Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = guidance.title, style = typography.title, color = colors.textPrimary, textAlign = TextAlign.Center)
                Spacer(Modifier.height(8.dp))
                Text(text = guidance.message, style = typography.subtitle, color = colors.textSecondary, textAlign = TextAlign.Center)
            }
        }
        Spacer(Modifier.height(spacing.lg))
        FadeInUp(delayMillis = motion.stagger(2)) {
            SkinthesiaCard {
                Text(text = "Try this", style = typography.overline.copy(), color = colors.textMuted)
                Spacer(Modifier.height(12.dp))
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    guidance.tips.forEach { tip -> TipRow(SkinthesiaIcons.Check, tip, "") }
                }
            }
        }
        Spacer(Modifier.height(spacing.md))
        val flagged = quality.checks.filter { it.status == CheckStatus.FAIL || it.status == CheckStatus.WARN }
        if (flagged.isNotEmpty()) {
            FadeInUp(delayMillis = motion.stagger(3)) {
                Column {
                    Text(text = "What we noticed", style = typography.label, color = colors.textSecondary)
                    Spacer(Modifier.height(8.dp))
                    androidx.compose.foundation.layout.FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        flagged.forEach { check ->
                            Tag(text = check.headline, tone = if (check.status == CheckStatus.FAIL) TagTone.ROSE else TagTone.GOLD)
                        }
                    }
                }
            }
        }
        Spacer(Modifier.height(spacing.md))
        Text(
            text = "Using this photo anyway may make visual estimates less reliable.",
            style = typography.caption,
            color = colors.textMuted,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}
