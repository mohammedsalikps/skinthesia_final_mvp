package com.skinthesia.core.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import com.skinthesia.LocalAppContainer
import com.skinthesia.domain.repository.ImageKey
import com.skinthesia.feature.onboarding.LifestyleScreen
import com.skinthesia.feature.onboarding.OnboardingEvent
import com.skinthesia.feature.onboarding.OnboardingViewModel
import com.skinthesia.feature.onboarding.QuestionnaireScreen
import com.skinthesia.feature.onboarding.SetGoalsScreen
import com.skinthesia.feature.onboarding.TakePhotoScreen
import com.skinthesia.feature.onboarding.WelcomeScreen
import com.skinthesia.feature.onboarding.YourTargetScreen

/**
 * Screens 01–06 as a nested graph. All steps share one [OnboardingViewModel]
 * scoped to the graph entry, so answers survive back navigation.
 */
fun NavGraphBuilder.onboardingGraph(
    navController: NavHostController,
    onCompleted: () -> Unit,
) {
    navigation(startDestination = OnboardingRoutes.WELCOME, route = OnboardingRoutes.GRAPH) {
        composable(OnboardingRoutes.WELCOME) {
            val container = LocalAppContainer.current
            val heroImage = remember(container) { container.imageRepository.image(ImageKey.WELCOME_HERO) }
            WelcomeScreen(
                heroImage = heroImage,
                onStart = { navController.navigate(OnboardingRoutes.PHOTO) },
            )
        }

        composable(OnboardingRoutes.PHOTO) { entry ->
            val viewModel = entry.onboardingViewModel(navController)
            val state by viewModel.uiState.collectAsStateWithLifecycle()
            TakePhotoScreen(
                state = state,
                onBack = { navController.navigateUp() },
                onContinue = { navController.navigate(OnboardingRoutes.GOALS) },
                onCapture = viewModel::capturePhoto,
                onImport = viewModel::importPhoto,
                onRetake = viewModel::retakePhoto,
                onDismissNotice = viewModel::dismissNotice,
            )
        }

        composable(OnboardingRoutes.GOALS) { entry ->
            val viewModel = entry.onboardingViewModel(navController)
            val state by viewModel.uiState.collectAsStateWithLifecycle()
            SetGoalsScreen(
                state = state,
                onToggleGoal = viewModel::toggleGoal,
                onBack = { navController.navigateUp() },
                onContinue = { navController.navigate(OnboardingRoutes.TARGET) },
                onDismissNotice = viewModel::dismissNotice,
            )
        }

        composable(OnboardingRoutes.TARGET) { entry ->
            val viewModel = entry.onboardingViewModel(navController)
            val state by viewModel.uiState.collectAsStateWithLifecycle()
            YourTargetScreen(
                state = state,
                onToggleTarget = viewModel::toggleTargetGoal,
                onBack = { navController.navigateUp() },
                onContinue = { navController.navigate(OnboardingRoutes.QUESTIONNAIRE) },
            )
        }

        composable(OnboardingRoutes.QUESTIONNAIRE) { entry ->
            val viewModel = entry.onboardingViewModel(navController)
            val state by viewModel.uiState.collectAsStateWithLifecycle()
            QuestionnaireScreen(
                state = state,
                onAgeRange = viewModel::setAgeRange,
                onSkinType = viewModel::setSkinType,
                onToggleConcern = viewModel::toggleConcern,
                onBack = { navController.navigateUp() },
                onContinue = { navController.navigate(OnboardingRoutes.LIFESTYLE) },
            )
        }

        composable(OnboardingRoutes.LIFESTYLE) { entry ->
            val viewModel = entry.onboardingViewModel(navController)
            val state by viewModel.uiState.collectAsStateWithLifecycle()
            LaunchedEffect(viewModel) {
                viewModel.events.collect { event ->
                    if (event is OnboardingEvent.Completed) onCompleted()
                }
            }
            LifestyleScreen(
                state = state,
                onToggleFactor = viewModel::toggleLifestyleFactor,
                onBack = { navController.navigateUp() },
                onNext = viewModel::finishOnboarding,
            )
        }
    }
}

/** Resolves the graph-scoped [OnboardingViewModel] for any onboarding destination. */
@Composable
private fun NavBackStackEntry.onboardingViewModel(navController: NavHostController): OnboardingViewModel {
    val parentEntry = remember(this) { navController.getBackStackEntry(OnboardingRoutes.GRAPH) }
    val container = LocalAppContainer.current
    return viewModel(viewModelStoreOwner = parentEntry, factory = container.viewModelFactory)
}
