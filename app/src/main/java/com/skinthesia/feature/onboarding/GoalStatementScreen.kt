package com.skinthesia.feature.onboarding

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.skinthesia.core.design.SkinthesiaTheme
import com.skinthesia.core.navigation.LocalAppNavigator
import com.skinthesia.core.navigation.QuestionnaireRoute
import com.skinthesia.core.navigation.containerViewModel
import com.skinthesia.core.ui.components.FadeInUp
import com.skinthesia.core.ui.components.OnboardingHeader
import com.skinthesia.core.ui.components.OnboardingScaffold
import com.skinthesia.core.ui.components.SectionHeader
import com.skinthesia.core.ui.components.SkinthesiaCard
import com.skinthesia.core.ui.components.SkinthesiaPrimaryButton
import com.skinthesia.core.ui.components.SkinthesiaTextButton
import com.skinthesia.core.ui.components.SkinthesiaTextField
import com.skinthesia.core.ui.components.Tag
import com.skinthesia.core.ui.components.TagTone
import com.skinthesia.core.ui.icons.SkinthesiaIcons
import com.skinthesia.domain.model.GoalPlan
import com.skinthesia.domain.model.OnboardingStage
import com.skinthesia.domain.model.SkinGoal
import com.skinthesia.domain.repository.UserProfileRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class GoalStatementViewModel(
    private val profiles: UserProfileRepository,
    private val clock: () -> Long,
) : ViewModel() {

    data class UiState(
        val goals: List<SkinGoal> = emptyList(),
        val priorities: List<SkinGoal> = emptyList(),
        val statement: String = GoalStatements.DEFAULT,
        val suggestions: List<String> = emptyList(),
        val editing: Boolean = false,
    )

    private val _state = MutableStateFlow(UiState())
    val state: StateFlow<UiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            val plan = profiles.current().goals
            _state.value = UiState(
                goals = plan.goals,
                priorities = plan.priorities,
                statement = plan.statement.ifBlank { GoalStatements.build(plan.ranked) },
                suggestions = GoalStatements.suggestions(plan.ranked),
            )
        }
    }

    fun setStatement(value: String) = _state.update { it.copy(statement = value.take(MAX_STATEMENT)) }
    fun useSuggestion(value: String) = _state.update { it.copy(statement = value, editing = false) }
    fun setEditing(editing: Boolean) = _state.update { it.copy(editing = editing) }

    fun togglePriority(goal: SkinGoal) = _state.update { state ->
        val next = when {
            goal in state.priorities -> state.priorities - goal
            state.priorities.size >= GoalPlan.MAX_PRIORITIES -> state.priorities
            else -> state.priorities + goal
        }
        state.copy(priorities = next)
    }

    fun submit(onDone: () -> Unit) {
        val current = _state.value
        viewModelScope.launch {
            profiles.update { p ->
                p.copy(
                    goals = p.goals.copy(
                        statement = current.statement.trim().ifBlank { GoalStatements.build(current.goals) },
                        priorities = current.priorities,
                        startedAt = p.goals.startedAt ?: clock(),
                    ),
                    onboardingStage = p.onboardingStage.atLeast(OnboardingStage.QUESTIONNAIRE),
                )
            }
            onDone()
        }
    }

    private companion object {
        const val MAX_STATEMENT = 60
    }
}

/** Screen 07: the 12-week intention in the user's own words, plus up to three priorities. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun GoalStatementScreen() {
    val navigator = LocalAppNavigator.current
    val viewModel = containerViewModel { GoalStatementViewModel(profiles, clock) }
    val state by viewModel.state.collectAsStateWithLifecycle()
    val colors = SkinthesiaTheme.colors
    val typography = SkinthesiaTheme.typography
    val spacing = SkinthesiaTheme.spacing
    val motion = SkinthesiaTheme.motion

    OnboardingScaffold(
        step = OnboardingSteps.TARGET,
        totalSteps = OnboardingSteps.TOTAL,
        onBack = navigator::back,
        bottomBar = {
            SkinthesiaPrimaryButton(text = "Set My Goal", onClick = { viewModel.submit { navigator.navigate(QuestionnaireRoute) } })
        },
    ) {
        OnboardingHeader(title = "Your 12-week goal", subtitle = "Describe the change you'd like to see.")
        Spacer(Modifier.height(spacing.lg))

        FadeInUp {
            AnimatedContent(
                targetState = state.editing,
                transitionSpec = { fadeIn(tween(motion.duration(motion.base))) togetherWith fadeOut(tween(motion.duration(motion.fast))) },
                label = "statementEdit",
            ) { editing ->
                if (editing) {
                    Column(Modifier.fillMaxWidth()) {
                        SkinthesiaTextField(
                            value = state.statement,
                            onValueChange = viewModel::setStatement,
                            label = "In your words",
                            maxLength = 60,
                            singleLine = false,
                            minLines = 2,
                            modifier = Modifier.fillMaxWidth(),
                        )
                        SkinthesiaTextButton(text = "Done", onClick = { viewModel.setEditing(false) })
                    }
                } else {
                    Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "“${state.statement}”",
                            style = typography.displayItalic,
                            color = colors.primary,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth().padding(horizontal = spacing.sm),
                        )
                        SkinthesiaTextButton(text = "Edit in my words", leadingIcon = SkinthesiaIcons.Edit, onClick = { viewModel.setEditing(true) })
                    }
                }
            }
        }

        Spacer(Modifier.height(spacing.sm))
        FadeInUp(delayMillis = motion.stagger(1)) {
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                state.suggestions.filter { it != state.statement }.take(4).forEach { suggestion ->
                    Box(
                        modifier = Modifier
                            .heightIn(min = 40.dp)
                            .clip(SkinthesiaTheme.shapes.pill)
                            .border(1.dp, colors.border, SkinthesiaTheme.shapes.pill)
                            .clickable(role = Role.Button) { viewModel.useSuggestion(suggestion) }
                            .padding(horizontal = 14.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(text = suggestion, style = typography.bodySmall, color = colors.textSecondary)
                    }
                }
            }
        }

        Spacer(Modifier.height(spacing.section))
        FadeInUp(delayMillis = motion.stagger(2)) {
            Column {
                SectionHeader(title = "Your priorities", overline = "Up to three")
                Spacer(Modifier.height(4.dp))
                Text(text = "Tap the goals your plan should lead with.", style = typography.bodySmall, color = colors.textMuted)
                Spacer(Modifier.height(spacing.sm))
                state.goals.forEach { goal ->
                    PriorityRow(
                        goal = goal,
                        rank = state.priorities.indexOf(goal).takeIf { it >= 0 }?.plus(1),
                        enabled = goal in state.priorities || state.priorities.size < GoalPlan.MAX_PRIORITIES,
                        onClick = { viewModel.togglePriority(goal) },
                    )
                    Spacer(Modifier.height(8.dp))
                }
            }
        }

        Spacer(Modifier.height(spacing.md))
        FadeInUp(delayMillis = motion.stagger(3)) { ProgrammeTimeline() }
    }
}

@Composable
private fun PriorityRow(goal: SkinGoal, rank: Int?, enabled: Boolean, onClick: () -> Unit) {
    val colors = SkinthesiaTheme.colors
    val typography = SkinthesiaTheme.typography
    val selected = rank != null
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 56.dp)
            .clip(SkinthesiaTheme.shapes.field)
            .background(if (selected) colors.primaryMist else colors.surface)
            .border(1.dp, if (selected) colors.primary.copy(alpha = 0.6f) else colors.border, SkinthesiaTheme.shapes.field)
            .toggleable(value = selected, enabled = enabled, role = Role.Checkbox, onValueChange = { onClick() })
            .semantics { stateDescription = if (selected) "Priority $rank" else "Not a priority" }
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(goal.icon, contentDescription = null, tint = if (selected) colors.primary else colors.textMuted, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(14.dp))
        Text(text = goal.label, style = typography.labelLarge, color = if (enabled) colors.textPrimary else colors.textMuted, modifier = Modifier.weight(1f))
        if (rank != null) {
            Box(Modifier.size(26.dp).clip(CircleShape).background(colors.primary), contentAlignment = Alignment.Center) {
                Text(text = rank.toString(), style = typography.labelSmall, color = colors.textOnPrimary)
            }
        } else {
            Icon(SkinthesiaIcons.Star, contentDescription = null, tint = colors.borderStrong, modifier = Modifier.size(20.dp))
        }
    }
}

@Composable
private fun ProgrammeTimeline() {
    val colors = SkinthesiaTheme.colors
    val typography = SkinthesiaTheme.typography
    SkinthesiaCard {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(text = "12 weeks, measured as you go", style = typography.label, color = colors.textPrimary, modifier = Modifier.weight(1f))
            Tag(text = "3 check-ins", tone = TagTone.CLAY)
        }
        Spacer(Modifier.height(14.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            listOf("Day 1" to "Baseline", "Week 4" to "Check-in", "Week 8" to "Check-in", "Week 12" to "Review").forEachIndexed { index, (week, label) ->
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        Modifier
                            .size(12.dp)
                            .clip(CircleShape)
                            .background(if (index == 0) colors.primary else colors.surfaceElevated)
                            .border(1.3.dp, if (index == 0) colors.primary else colors.borderStrong, CircleShape),
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(text = week, style = typography.labelSmall, color = colors.textPrimary)
                    Text(text = label, style = typography.caption, color = colors.textMuted)
                }
            }
        }
    }
}
