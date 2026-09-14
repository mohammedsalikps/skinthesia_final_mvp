package com.skinthesia.feature.onboarding

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import com.skinthesia.R
import com.skinthesia.core.design.SkinthesiaTheme
import com.skinthesia.core.navigation.OnboardingRoutes
import com.skinthesia.core.ui.components.InfoNotice
import com.skinthesia.core.ui.components.OnboardingHeader
import com.skinthesia.core.ui.components.OnboardingScaffold
import com.skinthesia.core.ui.components.SelectableTile
import com.skinthesia.core.ui.components.SkinthesiaPrimaryButton
import com.skinthesia.core.ui.icon
import com.skinthesia.core.ui.labelRes
import com.skinthesia.domain.model.SkinGoal
import kotlinx.coroutines.delay

private const val NOTICE_DURATION_MS = 2800L

/** Screen 03: choose up to three improvement goals from a 3x3 grid of tiles. */
@Composable
fun SetGoalsScreen(
    state: OnboardingUiState,
    onToggleGoal: (SkinGoal) -> Unit,
    onBack: () -> Unit,
    onContinue: () -> Unit,
    onDismissNotice: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = SkinthesiaTheme.colors
    val typography = SkinthesiaTheme.typography
    val spacing = SkinthesiaTheme.spacing
    val motion = SkinthesiaTheme.motion

    LaunchedEffect(state.notice) {
        if (state.notice == OnboardingNotice.GOAL_LIMIT) {
            delay(NOTICE_DURATION_MS)
            onDismissNotice()
        }
    }

    OnboardingScaffold(
        step = 3,
        totalSteps = OnboardingRoutes.TOTAL_STEPS,
        onBack = onBack,
        modifier = modifier,
        bottomBar = {
            SkinthesiaPrimaryButton(
                text = stringResource(R.string.action_continue),
                onClick = onContinue,
                enabled = state.canContinueFromGoals,
            )
        },
    ) {
        OnboardingHeader(
            title = stringResource(R.string.goals_title),
            subtitle = stringResource(R.string.goals_instruction),
        )
        Spacer(Modifier.height(spacing.lg))

        SelectionGrid(items = SkinGoal.entries) { goal ->
            val selected = goal in state.selectedGoals
            SelectableTile(
                label = stringResource(goal.labelRes),
                icon = goal.icon,
                selected = selected,
                dimmed = state.goalLimitReached && !selected,
                onClick = { onToggleGoal(goal) },
                modifier = Modifier.fillMaxWidth(),
            )
        }

        Spacer(Modifier.height(spacing.md))
        Text(
            text = "${state.selectedGoals.size} / ${SkinGoal.MAX_SELECTION}",
            style = typography.overline,
            color = colors.textMuted,
            textAlign = TextAlign.Center,
        )

        // Shown below the grid so tiles never shift under the user's finger.
        AnimatedVisibility(
            visible = state.notice == OnboardingNotice.GOAL_LIMIT,
            enter = fadeIn(tween(motion.duration(motion.base))) + expandVertically(tween(motion.duration(motion.base))),
            exit = fadeOut(tween(motion.duration(motion.fast))) + shrinkVertically(tween(motion.duration(motion.fast))),
        ) {
            Column {
                Spacer(Modifier.height(spacing.sm))
                InfoNotice(text = stringResource(R.string.goals_limit_reached))
            }
        }
    }
}
