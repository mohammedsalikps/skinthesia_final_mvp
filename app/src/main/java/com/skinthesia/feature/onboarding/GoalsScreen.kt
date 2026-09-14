package com.skinthesia.feature.onboarding

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.skinthesia.core.design.SkinthesiaTheme
import com.skinthesia.core.navigation.GoalStatementRoute
import com.skinthesia.core.navigation.LocalAppNavigator
import com.skinthesia.core.navigation.containerViewModel
import com.skinthesia.core.ui.components.FadeInUp
import com.skinthesia.core.ui.components.OnboardingHeader
import com.skinthesia.core.ui.components.OnboardingScaffold
import com.skinthesia.core.ui.components.SelectableRow
import com.skinthesia.core.ui.components.SelectableTile
import com.skinthesia.core.ui.components.SkinthesiaPrimaryButton
import com.skinthesia.core.ui.components.TileGrid
import com.skinthesia.domain.model.GoalPlan
import com.skinthesia.domain.model.OnboardingStage
import com.skinthesia.domain.model.SkinGoal
import com.skinthesia.domain.repository.UserProfileRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class GoalsViewModel(private val profiles: UserProfileRepository) : ViewModel() {

    private val _selected = MutableStateFlow<List<SkinGoal>>(emptyList())
    val selected: StateFlow<List<SkinGoal>> = _selected.asStateFlow()

    init {
        viewModelScope.launch { _selected.value = profiles.current().goals.goals }
    }

    fun toggle(goal: SkinGoal) = _selected.update { if (goal in it) it - goal else it + goal }

    fun submit(onDone: () -> Unit) {
        val goals = _selected.value
        if (goals.isEmpty()) return
        viewModelScope.launch {
            profiles.update { p ->
                val priorities = p.goals.priorities.filter { it in goals }
                    .ifEmpty { goals.filter { it != SkinGoal.OVERALL_HEALTH }.take(GoalPlan.MAX_PRIORITIES) }
                p.copy(
                    goals = p.goals.copy(goals = goals, priorities = priorities),
                    onboardingStage = p.onboardingStage.atLeast(OnboardingStage.GOAL_STATEMENT),
                )
            }
            onDone()
        }
    }
}

/** Screen 06: choose every goal that matters; priorities come next. */
@Composable
fun GoalsScreen() {
    val navigator = LocalAppNavigator.current
    val viewModel = containerViewModel { GoalsViewModel(profiles) }
    val selected by viewModel.selected.collectAsStateWithLifecycle()
    val colors = SkinthesiaTheme.colors
    val typography = SkinthesiaTheme.typography
    val spacing = SkinthesiaTheme.spacing
    val grid = SkinGoal.entries.filter { it != SkinGoal.OVERALL_HEALTH }

    OnboardingScaffold(
        step = OnboardingSteps.GOALS,
        totalSteps = OnboardingSteps.TOTAL,
        onBack = navigator::back,
        bottomBar = {
            Text(
                text = when (selected.size) {
                    0 -> "Choose at least one goal"
                    1 -> "1 goal selected"
                    else -> "${selected.size} goals selected"
                },
                style = typography.caption,
                color = colors.textMuted,
            )
            Spacer(Modifier.height(10.dp()))
            SkinthesiaPrimaryButton(
                text = "Continue",
                onClick = { viewModel.submit { navigator.navigate(GoalStatementRoute) } },
                enabled = selected.isNotEmpty(),
            )
        },
    ) {
        OnboardingHeader(
            title = "What would you like\nto focus on?",
            subtitle = "Choose everything that matters to you.\nYou'll pick your priorities next.",
        )
        Spacer(Modifier.height(spacing.lg))
        TileGrid(items = grid) { goal ->
            SelectableTile(
                label = goal.label,
                icon = goal.icon,
                selected = goal in selected,
                onClick = { viewModel.toggle(goal) },
                modifier = Modifier.fillMaxWidth(),
            )
        }
        Spacer(Modifier.height(spacing.gridGap))
        FadeInUp(delayMillis = SkinthesiaTheme.motion.stagger(4)) {
            SelectableRow(
                label = SkinGoal.OVERALL_HEALTH.label,
                supporting = "Balanced care across every area",
                selected = SkinGoal.OVERALL_HEALTH in selected,
                onClick = { viewModel.toggle(SkinGoal.OVERALL_HEALTH) },
            )
        }
        Spacer(Modifier.height(spacing.md))
        Text(
            text = "Goals shape your plan, never a diagnosis.",
            style = typography.caption,
            color = colors.textMuted,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

private fun Int.dp() = androidx.compose.ui.unit.Dp(this.toFloat())
