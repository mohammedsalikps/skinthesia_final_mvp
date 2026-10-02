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
import com.skinthesia.feature.analysis.CameraAnalysisScreen
import com.skinthesia.feature.analysis.CombinedAnalysisScreen
import com.skinthesia.feature.analysis.PotentialScreen
import com.skinthesia.feature.analysis.SensorAnalysisScreen
import com.skinthesia.feature.analysis.SkinPrintScreen
import com.skinthesia.feature.capture.CameraScreen
import com.skinthesia.feature.checkin.CheckInIntroScreen
import com.skinthesia.feature.progress.AdaptivePlanScreen
import com.skinthesia.feature.progress.ProgressComparisonScreen
import com.skinthesia.feature.projection.SkinProjectionScreen
import com.skinthesia.feature.capture.PhotoQualityScreen
import com.skinthesia.feature.capture.RetakeGuidanceScreen
import com.skinthesia.feature.capture.SelfieGuideScreen
import com.skinthesia.feature.community.CommunityPostScreen
import com.skinthesia.feature.community.CreatePostScreen
import com.skinthesia.feature.consultation.BookingConfirmationScreen
import com.skinthesia.feature.consultation.BookingScreen
import com.skinthesia.feature.consultation.ExpertProfileScreen
import com.skinthesia.feature.consultation.ExpertsScreen
import com.skinthesia.feature.consultation.MyBookingsScreen
import com.skinthesia.feature.learn.ArticleScreen
import com.skinthesia.feature.marketplace.CartScreen
import com.skinthesia.feature.marketplace.CheckoutScreen
import com.skinthesia.feature.marketplace.MarketplaceScreen
import com.skinthesia.feature.marketplace.OrderConfirmationScreen
import com.skinthesia.feature.marketplace.OrdersScreen
import com.skinthesia.feature.marketplace.ProductDetailScreen
import com.skinthesia.feature.measurement.CalibrationScreen
import com.skinthesia.feature.measurement.MeasureScreen
import com.skinthesia.feature.measurement.MeasurementCompleteScreen
import com.skinthesia.feature.measurement.ProbeConnectScreen
import com.skinthesia.feature.measurement.ProbePairedScreen
import com.skinthesia.feature.onboarding.CreateProfileScreen
import com.skinthesia.feature.plan.PlanScreen
import com.skinthesia.feature.profile.AccountScreen
import com.skinthesia.feature.profile.ConnectedDeviceScreen
import com.skinthesia.feature.profile.DataControlsScreen
import com.skinthesia.feature.profile.EditGoalsScreen
import com.skinthesia.feature.profile.EditProfileScreen
import com.skinthesia.feature.profile.MeasurementHistoryScreen
import com.skinthesia.feature.profile.MyProductsScreen
import com.skinthesia.feature.profile.PrivacyScreen
import com.skinthesia.feature.profile.SessionDetailScreen
import com.skinthesia.feature.plan.RecommendationsScreen
import com.skinthesia.feature.plan.RoutineScreen
import com.skinthesia.feature.report.AreaDetailScreen
import com.skinthesia.feature.report.AreasScreen
import com.skinthesia.feature.report.ReportScreen
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

            // Analysis and SkinPrint
            composable<CameraAnalysisRoute> { CameraAnalysisScreen() }
            composable<SensorAnalysisRoute> { SensorAnalysisScreen() }
            composable<CombinedAnalysisRoute> { CombinedAnalysisScreen() }
            composable<PotentialRoute> { PotentialScreen() }
            composable<SkinPrintRoute> { SkinPrintScreen() }

            // Report and plan
            composable<ReportRoute> { ReportScreen() }
            composable<AreasRoute> { AreasScreen() }
            composable<AreaDetailRoute> { AreaDetailScreen() }
            composable<PlanRoute> { PlanScreen() }
            composable<RoutineRoute> { RoutineScreen() }
            composable<RecommendationsRoute> { RecommendationsScreen() }

            // Check-in and progress
            composable<CheckInIntroRoute> { CheckInIntroScreen() }
            composable<ProgressComparisonRoute> { ProgressComparisonScreen() }
            composable<AdaptivePlanRoute> { AdaptivePlanScreen() }
            composable<SkinProjectionRoute> { SkinProjectionScreen() }

            // Marketplace (checkout is simulated)
            composable<MarketplaceRoute> { MarketplaceScreen() }
            composable<ProductDetailRoute> { ProductDetailScreen() }
            composable<CartRoute> { CartScreen() }
            composable<CheckoutRoute> { CheckoutScreen() }
            composable<OrderConfirmationRoute> { OrderConfirmationScreen() }
            composable<OrdersRoute> { OrdersScreen() }

            // Consultation (booking is simulated)
            composable<ExpertsRoute> { ExpertsScreen() }
            composable<ExpertProfileRoute> { ExpertProfileScreen() }
            composable<BookingRoute> { BookingScreen() }
            composable<BookingConfirmationRoute> { BookingConfirmationScreen() }
            composable<MyBookingsRoute> { MyBookingsScreen() }

            // Education and community
            composable<ArticleRoute> { ArticleScreen() }
            composable<CommunityPostRoute> { CommunityPostScreen() }
            composable<CreatePostRoute> { CreatePostScreen() }

            // Profile and settings
            composable<EditProfileRoute> { EditProfileScreen() }
            composable<EditGoalsRoute> { EditGoalsScreen() }
            composable<MyProductsRoute> { MyProductsScreen() }
            composable<MeasurementHistoryRoute> { MeasurementHistoryScreen() }
            composable<SessionDetailRoute> { SessionDetailScreen() }
            composable<ConnectedDeviceRoute> { ConnectedDeviceScreen() }
            composable<PrivacyRoute> { PrivacyScreen() }
            composable<DataControlsRoute> { DataControlsScreen() }
            composable<AccountRoute> { AccountScreen() }

            composable<MainRoute>(
                enterTransition = { fadeIn(tween(motion.duration(motion.reveal), easing = motion.enterEasing)) },
                popEnterTransition = { fadeIn(tween(enter)) },
            ) { MainShell() }
        }
    }
}
