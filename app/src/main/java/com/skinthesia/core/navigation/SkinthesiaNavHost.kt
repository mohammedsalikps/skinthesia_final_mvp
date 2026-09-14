package com.skinthesia.core.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.skinthesia.core.design.SkinthesiaTheme

/**
 * Root navigation: the onboarding graph and the main shell. Transitions are a
 * quiet fade with a short horizontal drift, and collapse to instant cuts when
 * reduced motion is requested.
 */
@Composable
fun SkinthesiaNavHost(
    startDestination: String,
    modifier: Modifier = Modifier,
) {
    val navController = rememberNavController()
    val motion = SkinthesiaTheme.motion
    val enterDuration = motion.duration(motion.slow)
    val exitDuration = motion.duration(motion.base)

    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier,
        enterTransition = {
            fadeIn(tween(enterDuration, easing = motion.enterEasing)) +
                slideInHorizontally(tween(enterDuration, easing = motion.enterEasing)) { it / 14 }
        },
        exitTransition = {
            fadeOut(tween(exitDuration, easing = motion.exitEasing)) +
                slideOutHorizontally(tween(exitDuration, easing = motion.exitEasing)) { -it / 20 }
        },
        popEnterTransition = {
            fadeIn(tween(enterDuration, easing = motion.enterEasing)) +
                slideInHorizontally(tween(enterDuration, easing = motion.enterEasing)) { -it / 14 }
        },
        popExitTransition = {
            fadeOut(tween(exitDuration, easing = motion.exitEasing)) +
                slideOutHorizontally(tween(exitDuration, easing = motion.exitEasing)) { it / 20 }
        },
    ) {
        onboardingGraph(
            navController = navController,
            onCompleted = {
                navController.navigate(MainRoutes.ROOT) {
                    popUpTo(OnboardingRoutes.GRAPH) { inclusive = true }
                    launchSingleTop = true
                }
            },
        )
        composable(
            route = MainRoutes.ROOT,
            enterTransition = { fadeIn(tween(motion.duration(motion.reveal), easing = motion.enterEasing)) },
            exitTransition = { fadeOut(tween(exitDuration)) },
            popEnterTransition = { fadeIn(tween(enterDuration)) },
            popExitTransition = { fadeOut(tween(exitDuration)) },
        ) {
            MainShell(
                onEditAnswers = { navController.navigate(OnboardingRoutes.PHOTO) },
            )
        }
    }
}
