package com.skinthesia.core.navigation

import kotlinx.serialization.Serializable

/**
 * Which journey a shared flow (capture, probe, analysis) is part of. Carried as a
 * string route argument so the same screens serve onboarding, weekly check-ins and
 * standalone measurements, while [AppNavigator] decides where each one goes next.
 */
enum class FlowKind { ONBOARDING, CHECK_IN, STANDALONE }

// Onboarding
@Serializable data object WelcomeRoute
@Serializable data object CreateProfileRoute
@Serializable data object GoalsRoute
@Serializable data object GoalStatementRoute
@Serializable data object QuestionnaireRoute
@Serializable data object LifestyleRoute

// Capture and photo quality
@Serializable data class SelfieGuideRoute(val assessmentId: String, val flow: String = FlowKind.ONBOARDING.name)
@Serializable data class CameraRoute(val assessmentId: String, val flow: String = FlowKind.ONBOARDING.name)
@Serializable data class PhotoQualityRoute(val assessmentId: String, val flow: String = FlowKind.ONBOARDING.name)
@Serializable data class RetakeGuidanceRoute(val assessmentId: String, val flow: String = FlowKind.ONBOARDING.name)

// Probe and measurement
@Serializable data class ProbeConnectRoute(val assessmentId: String? = null, val flow: String = FlowKind.ONBOARDING.name)
@Serializable data class ProbePairedRoute(val assessmentId: String? = null, val flow: String = FlowKind.ONBOARDING.name)
@Serializable data class CalibrationRoute(val assessmentId: String? = null, val flow: String = FlowKind.ONBOARDING.name)
@Serializable data class MeasureRoute(val sessionId: String, val region: String, val assessmentId: String? = null, val flow: String = FlowKind.ONBOARDING.name)
@Serializable data class MeasurementCompleteRoute(val sessionId: String, val assessmentId: String? = null, val flow: String = FlowKind.ONBOARDING.name)

// Analysis and SkinPrint
@Serializable data class CameraAnalysisRoute(val assessmentId: String, val flow: String = FlowKind.ONBOARDING.name)
@Serializable data class SensorAnalysisRoute(val assessmentId: String? = null, val sessionId: String? = null, val flow: String = FlowKind.ONBOARDING.name)
@Serializable data class CombinedAnalysisRoute(val assessmentId: String, val flow: String = FlowKind.ONBOARDING.name)
@Serializable data class PotentialRoute(val assessmentId: String, val onboarding: Boolean = false)
@Serializable data class SkinPrintRoute(val assessmentId: String, val onboarding: Boolean = false)
@Serializable data class ReportRoute(val assessmentId: String, val onboarding: Boolean = false)
@Serializable data class AreasRoute(val assessmentId: String)
@Serializable data class AreaDetailRoute(val assessmentId: String, val area: String)

// Plan
@Serializable data class PlanRoute(val tab: Int = 0, val onboarding: Boolean = false)
@Serializable data class RoutineRoute(val time: String)
@Serializable data object RecommendationsRoute

// Check-in and progress
@Serializable data object CheckInIntroRoute
@Serializable data class ProgressComparisonRoute(val assessmentId: String? = null, val flow: String = FlowKind.STANDALONE.name)
@Serializable data class AdaptivePlanRoute(val flow: String = FlowKind.STANDALONE.name)

// Main shell and its tabs
@Serializable data object MainRoute
@Serializable data object HomeTab
@Serializable data object JourneyTab
@Serializable data object AnalyzeTab
@Serializable data object LearnTab
@Serializable data object ProfileTab

// Marketplace
@Serializable data class MarketplaceRoute(val category: String? = null)
@Serializable data class ProductDetailRoute(val productId: String)
@Serializable data object CartRoute
@Serializable data object CheckoutRoute
@Serializable data class OrderConfirmationRoute(val orderId: String)
@Serializable data object OrdersRoute

// Consultation
@Serializable data object ExpertsRoute
@Serializable data class ExpertProfileRoute(val expertId: String)
@Serializable data class BookingRoute(val expertId: String)
@Serializable data class BookingConfirmationRoute(val bookingId: String)
@Serializable data object MyBookingsRoute

// Education and community
@Serializable data class ArticleRoute(val articleId: String)
@Serializable data class CommunityPostRoute(val postId: String)
@Serializable data class CreatePostRoute(val section: String? = null)

// Profile and settings
@Serializable data object EditProfileRoute
@Serializable data object EditGoalsRoute
@Serializable data object MyProductsRoute
@Serializable data object MeasurementHistoryRoute
@Serializable data class SessionDetailRoute(val sessionId: String)
@Serializable data object ConnectedDeviceRoute
@Serializable data object PrivacyRoute
@Serializable data object DataControlsRoute
@Serializable data object AccountRoute
