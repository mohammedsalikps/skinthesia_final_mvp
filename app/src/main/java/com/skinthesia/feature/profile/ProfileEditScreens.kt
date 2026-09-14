package com.skinthesia.feature.profile

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.skinthesia.core.design.SkinthesiaTheme
import com.skinthesia.core.navigation.LocalAppNavigator
import com.skinthesia.core.navigation.containerViewModel
import com.skinthesia.core.ui.components.ChoiceChips
import com.skinthesia.core.ui.components.InfoNotice
import com.skinthesia.core.ui.components.LoadingState
import com.skinthesia.core.ui.components.QuestionBlock
import com.skinthesia.core.ui.components.SectionOverline
import com.skinthesia.core.ui.components.SelectableTile
import com.skinthesia.core.ui.components.SkinthesiaPrimaryButton
import com.skinthesia.core.ui.components.SkinthesiaScreen
import com.skinthesia.core.ui.components.SkinthesiaTextField
import com.skinthesia.core.ui.components.SkinthesiaTopBar
import com.skinthesia.core.ui.components.TileGrid
import com.skinthesia.core.ui.icons.SkinthesiaIcons
import com.skinthesia.domain.model.AgeRange
import com.skinthesia.domain.model.Budget
import com.skinthesia.domain.model.GoalPlan
import com.skinthesia.domain.model.Sensitivity
import com.skinthesia.domain.model.SkinConcern
import com.skinthesia.domain.model.SkinGoal
import com.skinthesia.domain.model.SkinType
import com.skinthesia.domain.model.UserProfile
import com.skinthesia.domain.repository.UserProfileRepository
import com.skinthesia.feature.onboarding.GoalStatements
import com.skinthesia.feature.onboarding.icon
import com.skinthesia.feature.onboarding.toggle
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class EditProfileViewModel(private val profiles: UserProfileRepository) : ViewModel() {
    private val _draft = MutableStateFlow<UserProfile?>(null)
    val draft: StateFlow<UserProfile?> = _draft.asStateFlow()

    init {
        viewModelScope.launch { _draft.value = profiles.current() }
    }

    fun edit(transform: (UserProfile) -> UserProfile) = _draft.update { it?.let(transform) }

    fun save(onDone: () -> Unit) {
        val d = _draft.value ?: return
        viewModelScope.launch {
            profiles.update { current ->
                current.copy(firstName = d.firstName.trim().ifBlank { current.firstName }, ageRange = d.ageRange, skin = d.skin)
            }
            onDone()
        }
    }
}

/** Edit the details that shape the plan: name, age range, skin type, concerns and preferences. */
@Composable
fun EditProfileScreen() {
    val navigator = LocalAppNavigator.current
    val viewModel = containerViewModel { EditProfileViewModel(profiles) }
    val draft by viewModel.draft.collectAsStateWithLifecycle()
    val spacing = SkinthesiaTheme.spacing

    SkinthesiaScreen(
        topBar = { SkinthesiaTopBar(title = "Skin profile", onBack = navigator::back) },
        bottomBar = { SkinthesiaPrimaryButton(text = "Save Changes", enabled = draft != null, onClick = { viewModel.save(navigator::back) }) },
    ) {
        val d = draft
        if (d == null) {
            LoadingState(message = "Loading")
            return@SkinthesiaScreen
        }
        SkinthesiaTextField(
            value = d.firstName,
            onValueChange = { v -> viewModel.edit { it.copy(firstName = v.take(40)) } },
            label = "First name",
            maxLength = 40,
        )
        Spacer(Modifier.height(spacing.lg))
        QuestionBlock(title = "Age range", icon = SkinthesiaIcons.User) {
            ChoiceChips(AgeRange.entries, setOfNotNull(d.ageRange), { v -> viewModel.edit { it.copy(ageRange = if (it.ageRange == v) null else v) } }, { it.label }, single = true)
        }
        Spacer(Modifier.height(spacing.lg))
        QuestionBlock(title = "Skin type", icon = SkinthesiaIcons.Face) {
            ChoiceChips(SkinType.entries, setOfNotNull(d.skin.skinType), { v -> viewModel.edit { it.copy(skin = it.skin.copy(skinType = v)) } }, { it.label }, single = true)
        }
        Spacer(Modifier.height(spacing.lg))
        QuestionBlock(title = "Main concerns", icon = SkinthesiaIcons.Target, hint = "Choose any that apply.") {
            ChoiceChips(SkinConcern.entries, d.skin.concerns, { v -> viewModel.edit { it.copy(skin = it.skin.copy(concerns = it.skin.concerns.toggle(v))) } }, { it.label })
        }
        Spacer(Modifier.height(spacing.lg))
        QuestionBlock(title = "Preferences", icon = SkinthesiaIcons.Leaf, hint = "We avoid what you'd rather not use.") {
            ChoiceChips(Sensitivity.entries, d.skin.preferences, { v -> viewModel.edit { it.copy(skin = it.skin.copy(preferences = it.skin.preferences.toggle(v))) } }, { it.label })
        }
        Spacer(Modifier.height(spacing.lg))
        QuestionBlock(title = "Budget per product", icon = SkinthesiaIcons.Bag) {
            ChoiceChips(Budget.entries, setOf(d.skin.budget), { v -> viewModel.edit { it.copy(skin = it.skin.copy(budget = v)) } }, { it.label }, single = true)
        }
        Spacer(Modifier.height(spacing.lg))
        InfoNotice(text = "Changes shape your plan at your next check-in.")
    }
}

class EditGoalsViewModel(private val profiles: UserProfileRepository) : ViewModel() {
    private val _draft = MutableStateFlow<GoalPlan?>(null)
    val draft: StateFlow<GoalPlan?> = _draft.asStateFlow()

    init {
        viewModelScope.launch { _draft.value = profiles.current().goals }
    }

    fun toggleGoal(goal: SkinGoal) = _draft.update { plan ->
        plan?.let {
            val goals = if (goal in it.goals) it.goals - goal else it.goals + goal
            it.copy(goals = goals, priorities = it.priorities.filter { p -> p in goals })
        }
    }

    fun togglePriority(goal: SkinGoal) = _draft.update { plan ->
        plan?.let {
            when {
                goal in it.priorities -> it.copy(priorities = it.priorities - goal)
                it.priorities.size < GoalPlan.MAX_PRIORITIES -> it.copy(priorities = it.priorities + goal)
                else -> it
            }
        }
    }

    fun setStatement(value: String) = _draft.update { it?.copy(statement = value.take(120)) }

    fun save(onDone: () -> Unit) {
        val d = _draft.value ?: return
        if (d.goals.isEmpty()) return
        viewModelScope.launch {
            profiles.update { it.copy(goals = d) }
            onDone()
        }
    }
}

/** Edit goals, priorities and the 12-week statement. */
@Composable
fun EditGoalsScreen() {
    val navigator = LocalAppNavigator.current
    val viewModel = containerViewModel { EditGoalsViewModel(profiles) }
    val draft by viewModel.draft.collectAsStateWithLifecycle()
    val colors = SkinthesiaTheme.colors
    val typography = SkinthesiaTheme.typography
    val spacing = SkinthesiaTheme.spacing

    SkinthesiaScreen(
        topBar = { SkinthesiaTopBar(title = "Your goals", onBack = navigator::back) },
        bottomBar = {
            SkinthesiaPrimaryButton(text = "Save Goals", enabled = draft?.goals?.isNotEmpty() == true, onClick = { viewModel.save(navigator::back) })
        },
    ) {
        val d = draft
        if (d == null) {
            LoadingState(message = "Loading")
            return@SkinthesiaScreen
        }
        SectionOverline(text = "What you're working on")
        Spacer(Modifier.height(12.dp))
        TileGrid(items = SkinGoal.entries, columns = 3) { goal ->
            SelectableTile(label = goal.label, icon = goal.icon, selected = goal in d.goals, onClick = { viewModel.toggleGoal(goal) })
        }
        Spacer(Modifier.height(spacing.lg))
        SectionOverline(text = "Priorities · up to three")
        Spacer(Modifier.height(6.dp))
        Text(text = "Your plan leads with these, in the order you choose them.", style = typography.bodySmall, color = colors.textSecondary)
        Spacer(Modifier.height(10.dp))
        ChoiceChips(d.goals, d.priorities.toSet(), viewModel::togglePriority, { goal -> d.priorities.indexOf(goal).let { i -> if (i >= 0) "${i + 1}. ${goal.label}" else goal.label } })
        Spacer(Modifier.height(spacing.lg))
        SectionOverline(text = "Your ${d.durationWeeks}-week goal")
        Spacer(Modifier.height(10.dp))
        SkinthesiaTextField(value = d.statement, onValueChange = viewModel::setStatement, placeholder = GoalStatements.DEFAULT, maxLength = 120)
        Spacer(Modifier.height(spacing.lg))
        InfoNotice(text = "Your plan adapts to new goals at your next check-in.")
    }
}
