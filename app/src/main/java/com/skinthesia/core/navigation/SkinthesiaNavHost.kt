package com.skinthesia.core.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.skinthesia.core.design.SkinthesiaTheme
import com.skinthesia.feature.capture.CameraScreen
import com.skinthesia.feature.capture.PhotoQualityScreen
import com.skinthesia.feature.capture.RetakeGuidanceScreen
import com.skinthesia.feature.capture.SelfieGuideScreen
import com.skinthesia.feature.measurement.CalibrationScreen
import com.skinthesia.feature.measurement.MeasureScreen
import com.skinthesia.feature.measurement.MeasurementCompleteScreen
import com.skinthesia.feature.measurement.ProbeConnectScreen
import com.skinthesia.feature.measurement.ProbePairedScreen
import com.skinthesia.feature.onboarding.CreateProfileScreen
import com.skinthesia.feature.onboarding.GoalStatementScreen
import com.skinthesia.feature.onboarding.GoalsScreen
import com.skinthesia.feature.onboarding.LifestyleScreen
import com.skinthesia.feature.onboarding.QuestionnaireScreen
import com.skinthesia.feature.onboarding.WelcomeScreen

/**
 * Root navigation. Transitions are a quiet fade with a short horizontal drift, and
 * collapse to instant cuts when the system asks for reduced motion.
 */
@Composable
fun SkinthesiaNavHost(
    startDestination: Any,
    modifier: Modifier = Modifier,
) {
    val nav = rememberNavController()
    val navigator = remember(nav) { AppNavigator(nav) }
    val motion = SkinthesiaTheme.motion
    val enter = motion.duration(motion.slow)
    val exit = motion.duration(motion.base)

    CompositionLocalProvider(LocalAppNavigator provides navigator) {
        NavHost(
            navController = nav,
            startDestination = startDestination,
            modifier = modifier,
            enterTransition = { fadeIn(tween(enter, easing = motion.enterEasing)) + slideInHorizontally(tween(enter, easing = motion.enterEasing)) { it / 16 } },
            exitTransition = { fadeOut(tween(exit, easing = motion.exitEasing)) + slideOutHorizontally(tween(exit, easing = motion.exitEasing)) { -it / 24 } },
            popEnterTransition = { fadeIn(tween(enter, easing = motion.enterEasing)) + slideInHorizontally(tween(enter, easing = motion.enterEasing)) { -it / 16 } },
            popExitTransition = { fadeOut(tween(exit, easing = motion.exitEasing)) + slideOutHorizontally(tween(exit, easing = motion.exitEasing)) { it / 24 } },
        ) {
            composable<WelcomeRoute> { WelcomeScreen(onStart = { navigator.navigate(CreateProfileRoute) }) }
            composable<CreateProfileRoute> { CreateProfileScreen() }
            composable<SelfieGuideRoute> { SelfieGuideScreen() }
            composable<CameraRoute>(
                enterTransition = { fadeIn(tween(enter)) },
                exitTransition = { fadeOut(tween(exit)) },
            ) { CameraScreen() }
            composable<PhotoQualityRoute> { PhotoQualityScreen() }
            composable<RetakeGuidanceRoute> { RetakeGuidanceScreen() }
            composable<GoalsRoute> { GoalsScreen() }
            composable<GoalStatementRoute> { GoalStatementScreen() }
            composable<QuestionnaireRoute> { QuestionnaireScreen() }
            composable<LifestyleRoute> { LifestyleScreen() }

            // Probe and measurement
            composable<ProbeConnectRoute> { ProbeConnectScreen() }
            composable<ProbePairedRoute> { ProbePairedScreen() }
            composable<CalibrationRoute> { CalibrationScreen() }
            composable<MeasureRoute> { MeasureScreen() }
            composable<MeasurementCompleteRoute> { MeasurementCompleteScreen() }

            composable<MainRoute>(
                enterTransition = { fadeIn(tween(motion.duration(motion.reveal), easing = motion.enterEasing)) },
                popEnterTransition = { fadeIn(tween(enter)) },
            ) { MainShell() }
        }
    }
}
