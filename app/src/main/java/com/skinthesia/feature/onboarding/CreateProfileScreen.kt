package com.skinthesia.feature.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.skinthesia.core.design.SkinthesiaTheme
import com.skinthesia.core.navigation.FlowKind
import com.skinthesia.core.navigation.LocalAppNavigator
import com.skinthesia.core.navigation.containerViewModel
import com.skinthesia.core.ui.components.ChoiceChips
import com.skinthesia.core.ui.components.FadeInUp
import com.skinthesia.core.ui.components.OnboardingHeader
import com.skinthesia.core.ui.components.OnboardingScaffold
import com.skinthesia.core.ui.components.QuestionBlock
import com.skinthesia.core.ui.components.SkinthesiaPrimaryButton
import com.skinthesia.core.ui.components.SkinthesiaTextField
import com.skinthesia.core.ui.icons.SkinthesiaIcons
import com.skinthesia.domain.model.AgeRange
import com.skinthesia.domain.model.AssessmentKind
import com.skinthesia.domain.model.OnboardingStage
import com.skinthesia.domain.repository.UserProfileRepository
import com.skinthesia.domain.usecase.StartAssessmentUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class CreateProfileViewModel(
    private val profiles: UserProfileRepository,
    private val startAssessment: StartAssessmentUseCase,
) : ViewModel() {

    data class UiState(
        val firstName: String = "",
        val ageRange: AgeRange? = null,
        val saving: Boolean = false,
    ) {
        val canContinue: Boolean get() = firstName.isNotBlank() && ageRange != null && !saving
    }

    private val _state = MutableStateFlow(UiState())
    val state: StateFlow<UiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            val profile = profiles.current()
            _state.update { it.copy(firstName = profile.firstName, ageRange = profile.ageRange) }
        }
    }

    fun setName(value: String) = _state.update { it.copy(firstName = value.take(MAX_NAME)) }
    fun setAge(value: AgeRange) = _state.update { it.copy(ageRange = value) }

    fun submit(onDone: (assessmentId: String) -> Unit) {
        val current = _state.value
        if (!current.canContinue) return
        _state.update { it.copy(saving = true) }
        viewModelScope.launch {
            profiles.update {
                it.copy(
                    firstName = current.firstName.trim(),
                    ageRange = current.ageRange,
                    onboardingStage = it.onboardingStage.atLeast(OnboardingStage.SELFIE),
                )
            }
            val assessment = startAssessment(AssessmentKind.BASELINE)
            _state.update { it.copy(saving = false) }
            onDone(assessment.id)
        }
    }

    private companion object {
        const val MAX_NAME = 40
    }
}

/** Screen 02: only the essentials, a first name and an age range. */
@Composable
fun CreateProfileScreen() {
    val navigator = LocalAppNavigator.current
    val viewModel = containerViewModel { CreateProfileViewModel(profiles, startAssessment) }
    val state by viewModel.state.collectAsStateWithLifecycle()
    val colors = SkinthesiaTheme.colors
    val typography = SkinthesiaTheme.typography
    val spacing = SkinthesiaTheme.spacing
    val motion = SkinthesiaTheme.motion

    OnboardingScaffold(
        step = OnboardingSteps.PROFILE,
        totalSteps = OnboardingSteps.TOTAL,
        onBack = navigator::back,
        bottomBar = {
            SkinthesiaPrimaryButton(
                text = "Continue",
                onClick = { viewModel.submit { id -> navigator.startCapture(id, FlowKind.ONBOARDING) } },
                enabled = state.canContinue,
                loading = state.saving,
            )
        },
    ) {
        OnboardingHeader(
            title = "Let's get to know you",
            subtitle = "Just the basics. You can change these any time.",
        )
        Spacer(Modifier.height(spacing.section))
        FadeInUp {
            SkinthesiaTextField(
                value = state.firstName,
                onValueChange = viewModel::setName,
                label = "What should we call you?",
                placeholder = "First name",
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words, imeAction = ImeAction.Done),
            )
        }
        Spacer(Modifier.height(spacing.lg))
        FadeInUp(delayMillis = motion.stagger(1)) {
            QuestionBlock(title = "Age range", hint = "Skin changes with time; this helps us set sensible expectations.") {
                ChoiceChips(
                    options = AgeRange.entries,
                    selected = setOfNotNull(state.ageRange),
                    onToggle = viewModel::setAge,
                    label = { it.label },
                    single = true,
                )
            }
        }
        Spacer(Modifier.height(spacing.section))
        FadeInUp(delayMillis = motion.stagger(2)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(SkinthesiaTheme.shapes.card)
                    .background(colors.surface)
                    .padding(16.dp),
                verticalAlignment = Alignment.Top,
            ) {
                Icon(SkinthesiaIcons.Lock, contentDescription = null, tint = colors.primary, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(12.dp))
                androidx.compose.foundation.layout.Column {
                    Text(text = "Your data stays on this device", style = typography.label, color = colors.textPrimary)
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "Photos and measurements are stored privately on your phone. Nothing is shared without your permission.",
                        style = typography.bodySmall,
                        color = colors.textSecondary,
                    )
                }
            }
        }
    }
}
