package com.skinthesia.feature.onboarding

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.skinthesia.core.design.SkinthesiaTheme
import com.skinthesia.core.navigation.LocalAppNavigator
import com.skinthesia.core.navigation.MainRoute
import com.skinthesia.core.navigation.containerViewModel
import com.skinthesia.core.ui.components.ChoiceChips
import com.skinthesia.core.ui.components.FadeInUp
import com.skinthesia.core.ui.components.InfoNotice
import com.skinthesia.core.ui.components.OnboardingHeader
import com.skinthesia.core.ui.components.OnboardingScaffold
import com.skinthesia.core.ui.components.QuestionBlock
import com.skinthesia.core.ui.components.SkinthesiaPrimaryButton
import com.skinthesia.core.ui.icons.SkinthesiaIcons
import com.skinthesia.domain.model.Climate
import com.skinthesia.domain.model.DietPattern
import com.skinthesia.domain.model.Environment
import com.skinthesia.domain.model.ExerciseFrequency
import com.skinthesia.domain.model.Habit
import com.skinthesia.domain.model.Level
import com.skinthesia.domain.model.LifestyleProfile
import com.skinthesia.domain.model.OnboardingStage
import com.skinthesia.domain.model.SleepPattern
import com.skinthesia.domain.repository.UserProfileRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class LifestyleViewModel(private val profiles: UserProfileRepository) : ViewModel() {

    private val _state = MutableStateFlow(LifestyleProfile())
    val state: StateFlow<LifestyleProfile> = _state.asStateFlow()

    init {
        viewModelScope.launch { _state.value = profiles.current().lifestyle }
    }

    fun update(transform: (LifestyleProfile) -> LifestyleProfile) = _state.update(transform)

    fun submit(onDone: () -> Unit) {
        val lifestyle = _state.value
        viewModelScope.launch {
            profiles.update { it.copy(lifestyle = lifestyle, onboardingStage = it.onboardingStage.atLeast(OnboardingStage.PROBE)) }
            onDone()
        }
    }
}

private fun <T> T?.pick(value: T): T? = if (this == value) null else value

/** Screen 09: daily context, presented as context and never as a cause. */
@Composable
fun LifestyleScreen() {
    val navigator = LocalAppNavigator.current
    val viewModel = containerViewModel { LifestyleViewModel(profiles) }
    val life by viewModel.state.collectAsStateWithLifecycle()
    val spacing = SkinthesiaTheme.spacing
    val motion = SkinthesiaTheme.motion

    OnboardingScaffold(
        step = OnboardingSteps.LIFESTYLE,
        totalSteps = OnboardingSteps.TOTAL,
        onBack = navigator::back,
        bottomBar = {
            SkinthesiaPrimaryButton(text = "Next", onClick = { viewModel.submit { navigator.navigate(MainRoute) } })
        },
    ) {
        OnboardingHeader(title = "Your daily context", subtitle = "All optional. These shape suggestions in your plan.")
        Spacer(Modifier.height(spacing.md))
        InfoNotice(text = "Lifestyle can influence how skin feels, but it's rarely the whole story. We use it as context, never to judge.")
        Spacer(Modifier.height(spacing.lg))

        val blocks: List<@Composable () -> Unit> = listOf(
            {
                QuestionBlock(title = "Sleep on a typical night", icon = SkinthesiaIcons.Moon) {
                    ChoiceChips(SleepPattern.entries, setOfNotNull(life.sleep), { v -> viewModel.update { it.copy(sleep = it.sleep.pick(v)) } }, { it.label }, single = true)
                }
            },
            {
                QuestionBlock(title = "Stress lately", icon = SkinthesiaIcons.Stress) {
                    ChoiceChips(Level.entries, setOfNotNull(life.stress), { v -> viewModel.update { it.copy(stress = it.stress.pick(v)) } }, { it.label }, single = true)
                }
            },
            {
                QuestionBlock(title = "Time in the sun", icon = SkinthesiaIcons.Sun) {
                    ChoiceChips(Level.entries, setOfNotNull(life.sunExposure), { v -> viewModel.update { it.copy(sunExposure = it.sunExposure.pick(v)) } }, { it.label }, single = true)
                }
            },
            {
                QuestionBlock(title = "Meals", icon = SkinthesiaIcons.Diet) {
                    ChoiceChips(DietPattern.entries, setOfNotNull(life.diet), { v -> viewModel.update { it.copy(diet = it.diet.pick(v)) } }, { it.label }, single = true)
                }
            },
            {
                QuestionBlock(title = "Exercise", icon = SkinthesiaIcons.Movement) {
                    ChoiceChips(ExerciseFrequency.entries, setOfNotNull(life.exercise), { v -> viewModel.update { it.copy(exercise = it.exercise.pick(v)) } }, { it.label }, single = true)
                }
            },
            {
                QuestionBlock(title = "Where you spend your days", icon = SkinthesiaIcons.Work) {
                    ChoiceChips(Environment.entries, setOfNotNull(life.environment), { v -> viewModel.update { it.copy(environment = it.environment.pick(v)) } }, { it.label }, single = true)
                }
            },
            {
                QuestionBlock(title = "Climate", icon = SkinthesiaIcons.Climate) {
                    ChoiceChips(Climate.entries, setOfNotNull(life.climate), { v -> viewModel.update { it.copy(climate = it.climate.pick(v)) } }, { it.label }, single = true)
                }
            },
            {
                QuestionBlock(title = "Habits", icon = SkinthesiaIcons.Screen, hint = "Choose any that apply.") {
                    ChoiceChips(Habit.entries, life.habits, { v -> viewModel.update { it.copy(habits = it.habits.toggle(v)) } }, { it.label })
                }
            },
        )
        blocks.forEachIndexed { index, block ->
            FadeInUp(delayMillis = motion.stagger(index)) { block() }
            if (index < blocks.lastIndex) Spacer(Modifier.height(spacing.lg))
        }
    }
}
