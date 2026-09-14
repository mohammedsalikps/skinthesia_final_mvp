package com.skinthesia.feature.onboarding

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import com.skinthesia.R
import com.skinthesia.core.design.SkinthesiaTheme
import com.skinthesia.core.navigation.OnboardingRoutes
import com.skinthesia.core.ui.components.FadeInUp
import com.skinthesia.core.ui.components.OnboardingHeader
import com.skinthesia.core.ui.components.OnboardingScaffold
import com.skinthesia.core.ui.components.SelectableRow
import com.skinthesia.core.ui.components.SkinthesiaPrimaryButton
import com.skinthesia.core.ui.labelRes
import com.skinthesia.domain.model.TargetGoal

/** Screen 04: the 12-week target as an editorial quote, plus the measurable outcomes behind it. */
@Composable
fun YourTargetScreen(
    state: OnboardingUiState,
    onToggleTarget: (TargetGoal) -> Unit,
    onBack: () -> Unit,
    onContinue: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = SkinthesiaTheme.colors
    val typography = SkinthesiaTheme.typography
    val spacing = SkinthesiaTheme.spacing
    val motion = SkinthesiaTheme.motion
    val quoteDescription = stringResource(R.string.target_quote_cd, state.targetPhrase)

    OnboardingScaffold(
        step = 4,
        totalSteps = OnboardingRoutes.TOTAL_STEPS,
        onBack = onBack,
        modifier = modifier,
        bottomBar = {
            SkinthesiaPrimaryButton(
                text = stringResource(R.string.action_set_goal),
                onClick = onContinue,
                enabled = state.canContinueFromTarget,
            )
        },
    ) {
        OnboardingHeader(title = stringResource(R.string.target_title))
        Spacer(Modifier.height(spacing.lg))

        FadeInUp {
            AnimatedContent(
                targetState = state.targetPhrase,
                transitionSpec = {
                    fadeIn(tween(motion.duration(motion.slow))) togetherWith fadeOut(tween(motion.duration(motion.fast)))
                },
                label = "targetPhrase",
            ) { phrase ->
                Text(
                    text = "“$phrase”",
                    style = typography.displayItalic,
                    color = colors.primary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = spacing.sm)
                        .semantics { contentDescription = quoteDescription },
                )
            }
        }

        Spacer(Modifier.height(spacing.section))

        Column(modifier = Modifier.fillMaxWidth()) {
            TargetGoal.entries.forEachIndexed { index, target ->
                FadeInUp(delayMillis = motion.stagger(index + 1)) {
                    SelectableRow(
                        label = stringResource(target.labelRes),
                        selected = target in state.targetGoals,
                        onClick = { onToggleTarget(target) },
                    )
                }
                if (index < TargetGoal.entries.lastIndex) {
                    Spacer(Modifier.height(spacing.gridGap))
                }
            }
        }

        Spacer(Modifier.height(spacing.lg))
        FadeInUp(delayMillis = motion.stagger(TargetGoal.entries.size + 1)) {
            Text(
                text = stringResource(R.string.target_support),
                style = typography.bodySmall,
                color = colors.textMuted,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}
