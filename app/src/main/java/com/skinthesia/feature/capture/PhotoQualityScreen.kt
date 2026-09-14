package com.skinthesia.feature.capture

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
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
import com.skinthesia.core.navigation.PhotoQualityRoute
import com.skinthesia.core.navigation.containerViewModel
import com.skinthesia.core.navigation.toFlowKind
import com.skinthesia.core.ui.components.FaceGuideOverlay
import com.skinthesia.core.ui.components.FadeInUp
import com.skinthesia.core.ui.components.LoadingState
import com.skinthesia.core.ui.components.OnboardingScaffold
import com.skinthesia.core.ui.components.SkinthesiaCard
import com.skinthesia.core.ui.components.SkinthesiaDivider
import com.skinthesia.core.ui.components.SkinthesiaImage
import com.skinthesia.core.ui.components.SkinthesiaPrimaryButton
import com.skinthesia.core.ui.components.SkinthesiaTextButton
import com.skinthesia.core.ui.icons.SkinthesiaIcons
import com.skinthesia.domain.model.Assessment
import com.skinthesia.domain.model.CheckStatus
import com.skinthesia.domain.model.ImageSource
import com.skinthesia.domain.model.QualityVerdict
import com.skinthesia.domain.repository.AssessmentRepository
import com.skinthesia.feature.onboarding.OnboardingSteps
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

/** Observes one assessment for the capture-result screens, with the flow it belongs to. */
class AssessmentViewModel(
    val assessmentId: String,
    val flow: FlowKind,
    assessments: AssessmentRepository,
) : ViewModel() {
    val assessment: StateFlow<Assessment?> = assessments.observe(assessmentId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)
}

/**
 * Screen 04: an assistant-style check of the photo. The four friendly lines reveal one
 * by one; details are one tap away. Good photos continue, poor ones lead to guidance.
 */
@Composable
fun PhotoQualityScreen() {
    val navigator = LocalAppNavigator.current
    val viewModel = containerViewModel { handle ->
        val route = handle.toRoute<PhotoQualityRoute>()
        AssessmentViewModel(route.assessmentId, route.flow.toFlowKind(), assessments)
    }
    val assessment by viewModel.assessment.collectAsStateWithLifecycle()
    val flow = viewModel.flow
    val colors = SkinthesiaTheme.colors
    val typography = SkinthesiaTheme.typography
    val spacing = SkinthesiaTheme.spacing
    val motion = SkinthesiaTheme.motion
    val quality = assessment?.quality
    val photo = assessment?.photo

    var revealed by rememberSaveable { mutableIntStateOf(0) }
    LaunchedEffect(quality?.photoId) {
        if (quality == null) return@LaunchedEffect
        if (revealed >= 4) return@LaunchedEffect
        delay(motion.duration(500).toLong())
        repeat(4) {
            revealed = it + 1
            delay(motion.duration(380).toLong())
        }
    }
    val done = revealed >= 4
    var showDetails by rememberSaveable { mutableStateOf(false) }

    OnboardingScaffold(
        step = OnboardingSteps.SELFIE,
        totalSteps = OnboardingSteps.TOTAL,
        onBack = { navigator.retakePhoto(viewModel.assessmentId, flow) },
        bottomBar = {
            if (quality != null && done) {
                when (quality.verdict) {
                    QualityVerdict.RETAKE_RECOMMENDED -> {
                        SkinthesiaPrimaryButton(text = "See how to improve it", onClick = { navigator.retakeGuidance(viewModel.assessmentId, flow) })
                        SkinthesiaTextButton(text = "Use this photo anyway", onClick = { navigator.photoAccepted(viewModel.assessmentId, flow) })
                    }
                    else -> {
                        SkinthesiaPrimaryButton(text = "Continue", onClick = { navigator.photoAccepted(viewModel.assessmentId, flow) })
                        SkinthesiaTextButton(text = "Retake photo", leadingIcon = SkinthesiaIcons.Retake, onClick = { navigator.retakePhoto(viewModel.assessmentId, flow) })
                    }
                }
            }
        },
    ) {
        if (quality == null || photo == null) {
            LoadingState(message = "Checking your photo…")
            return@OnboardingScaffold
        }
        val (title, subtitle) = when {
            !done -> "Checking your photo" to "Looking at light, focus and position…"
            quality.verdict == QualityVerdict.GOOD -> "Your photo looks great" to "Everything we need is here."
            quality.verdict == QualityVerdict.ACCEPTABLE -> "Your photo will work" to "A small adjustment could make it even clearer."
            else -> "Let's try that again" to (quality.guidance?.title ?: "This photo isn't clear enough to analyse.")
        }
        Column(Modifier.fillMaxWidth().semantics { liveRegion = LiveRegionMode.Polite }, horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = title, style = typography.title, color = colors.textPrimary, textAlign = TextAlign.Center)
            Spacer(Modifier.height(8.dp))
            Text(text = subtitle, style = typography.subtitle, color = colors.textSecondary, textAlign = TextAlign.Center)
        }
        Spacer(Modifier.height(spacing.lg))
        FadeInUp {
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.58f)
                    .aspectRatio(0.8f)
                    .clip(SkinthesiaTheme.shapes.image)
                    .border(1.dp, colors.border, SkinthesiaTheme.shapes.image),
            ) {
                SkinthesiaImage(source = ImageSource.LocalFile(photo.filePath), contentDescription = "Your selfie", modifier = Modifier.fillMaxSize(), maxDimension = 900)
                FaceGuideOverlay(bracketColor = colors.surface, guideColor = Color.White.copy(alpha = if (done) 0f else 0.8f), scanning = !done)
            }
        }
        Spacer(Modifier.height(spacing.lg))
        SkinthesiaCard(modifier = Modifier.animateContentSize()) {
            quality.summary.forEachIndexed { index, summary ->
                AnimatedVisibility(
                    visible = index < revealed,
                    enter = fadeIn(tween(motion.duration(motion.base))) + expandVertically(tween(motion.duration(motion.base))),
                    exit = fadeOut() + shrinkVertically(),
                ) {
                    Column {
                        if (index > 0) SkinthesiaDivider()
                        QualityLine(
                            label = if (summary.status == CheckStatus.PASS || summary.status == CheckStatus.NOT_EVALUATED) summary.line.goodLabel else summary.line.issueLabel,
                            status = summary.status,
                        )
                    }
                }
            }
            if (!done) {
                Text(text = "Analysing…", style = typography.caption, color = colors.textMuted, modifier = Modifier.padding(vertical = 8.dp))
            }
        }
        if (done) {
            Spacer(Modifier.height(spacing.sm))
            SkinthesiaTextButton(
                text = if (showDetails) "Hide details" else "See all checks",
                leadingIcon = if (showDetails) SkinthesiaIcons.ChevronUp else SkinthesiaIcons.ChevronDown,
                onClick = { showDetails = !showDetails },
            )
            AnimatedVisibility(visible = showDetails, enter = fadeIn() + expandVertically(), exit = fadeOut() + shrinkVertically()) {
                SkinthesiaCard {
                    quality.checks.forEachIndexed { index, check ->
                        if (index > 0) SkinthesiaDivider()
                        Row(Modifier.fillMaxWidth().padding(vertical = 10.dp), verticalAlignment = Alignment.Top) {
                            StatusDot(check.status)
                            Spacer(Modifier.width(12.dp))
                            Column(Modifier.weight(1f)) {
                                Text(text = check.dimension.label + " · " + check.headline, style = typography.label, color = colors.textPrimary)
                                Text(text = check.detail, style = typography.bodySmall, color = colors.textMuted)
                            }
                        }
                    }
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "Checked on your phone: light, exposure and sharpness from the pixels, face position from Android's on-device face detector.",
                        style = typography.caption,
                        color = colors.textMuted,
                    )
                }
            }
        }
    }
}

@Composable
private fun QualityLine(label: String, status: CheckStatus) {
    val typography = SkinthesiaTheme.typography
    val colors = SkinthesiaTheme.colors
    Row(Modifier.fillMaxWidth().heightIn(min = 52.dp).padding(vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
        StatusDot(status, large = true)
        Spacer(Modifier.width(14.dp))
        Text(text = label, style = typography.labelLarge, color = colors.textPrimary, modifier = Modifier.weight(1f))
        Text(
            text = when (status) {
                CheckStatus.PASS -> "Good"
                CheckStatus.WARN -> "Could be better"
                CheckStatus.FAIL -> "Needs a retake"
                CheckStatus.NOT_EVALUATED -> "Skipped"
            },
            style = typography.caption,
            color = colors.textMuted,
        )
    }
}

@Composable
internal fun StatusDot(status: CheckStatus, large: Boolean = false) {
    val colors = SkinthesiaTheme.colors
    val (bg, fg, icon) = when (status) {
        CheckStatus.PASS -> Triple(colors.successSoft, colors.successStrong, SkinthesiaIcons.Check)
        CheckStatus.WARN -> Triple(colors.goldSoft, colors.goldStrong, SkinthesiaIcons.Info)
        CheckStatus.FAIL -> Triple(colors.warningSoft, colors.warningStrong, SkinthesiaIcons.Close)
        CheckStatus.NOT_EVALUATED -> Triple(colors.surfaceMuted, colors.textMuted, SkinthesiaIcons.Minus)
    }
    val size = if (large) 28.dp else 22.dp
    Box(Modifier.size(size).clip(CircleShape).background(bg), contentAlignment = Alignment.Center) {
        Icon(icon, contentDescription = null, tint = fg, modifier = Modifier.size(size * 0.55f))
    }
}
