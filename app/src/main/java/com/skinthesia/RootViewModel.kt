package com.skinthesia

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.skinthesia.core.navigation.CombinedAnalysisRoute
import com.skinthesia.core.navigation.CreateProfileRoute
import com.skinthesia.core.navigation.FlowKind
import com.skinthesia.core.navigation.GoalStatementRoute
import com.skinthesia.core.navigation.GoalsRoute
import com.skinthesia.core.navigation.LifestyleRoute
import com.skinthesia.core.navigation.MainRoute
import com.skinthesia.core.navigation.ProbeConnectRoute
import com.skinthesia.core.navigation.QuestionnaireRoute
import com.skinthesia.core.navigation.SelfieGuideRoute
import com.skinthesia.core.navigation.WelcomeRoute
import com.skinthesia.domain.model.AssessmentKind
import com.skinthesia.domain.model.OnboardingStage
import com.skinthesia.domain.repository.UserProfileRepository
import com.skinthesia.domain.usecase.StartAssessmentUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Decides where the app opens: the tab shell for returning users, or the exact
 * onboarding step a new user left off at. The splash screen waits for this.
 */
class RootViewModel(
    private val profiles: UserProfileRepository,
    private val startAssessment: StartAssessmentUseCase,
) : ViewModel() {

    private val _startDestination = MutableStateFlow<Any?>(null)
    val startDestination: StateFlow<Any?> = _startDestination.asStateFlow()

    init {
        viewModelScope.launch {
            _startDestination.value = runCatching { resolve() }.getOrDefault(WelcomeRoute)
        }
    }

    private suspend fun resolve(): Any {
        val profile = profiles.current()
        return when (profile.onboardingStage) {
            OnboardingStage.WELCOME -> WelcomeRoute
            OnboardingStage.PROFILE -> CreateProfileRoute
            OnboardingStage.SELFIE -> SelfieGuideRoute(baselineId(), FlowKind.ONBOARDING.name)
            OnboardingStage.GOALS -> GoalsRoute
            OnboardingStage.GOAL_STATEMENT -> GoalStatementRoute
            OnboardingStage.QUESTIONNAIRE -> QuestionnaireRoute
            OnboardingStage.LIFESTYLE -> LifestyleRoute
            OnboardingStage.PROBE -> ProbeConnectRoute(baselineId(), FlowKind.ONBOARDING.name)
            OnboardingStage.ANALYSIS -> CombinedAnalysisRoute(baselineId(), FlowKind.ONBOARDING.name)
            OnboardingStage.COMPLETE -> MainRoute
        }
    }

    private suspend fun baselineId(): String = startAssessment(AssessmentKind.BASELINE).id
}
