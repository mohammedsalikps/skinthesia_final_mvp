package com.skinthesia.core.navigation

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.lifecycle.Lifecycle
import androidx.navigation.NavHostController
import com.skinthesia.domain.model.MeasurementRegion

/**
 * One place that knows how the product flows connect. Screens report what happened
 * ("photo accepted", "measurement complete"); the navigator decides where that leads
 * for onboarding, a weekly check-in or a standalone measurement.
 */
class AppNavigator(private val nav: NavHostController) {

    fun navigate(route: Any) = nav.navigate(route) { launchSingleTop = true }

    /**
     * Pushes a destination even when the same screen is already on top, such as the next
     * measurement region or a related article. Ignored mid-transition to avoid double pushes.
     */
    fun push(route: Any) {
        val resumed = nav.currentBackStackEntry?.lifecycle?.currentState?.isAtLeast(Lifecycle.State.RESUMED) ?: true
        if (resumed) nav.navigate(route)
    }

    fun back() {
        if (!nav.navigateUp()) nav.popBackStack()
    }

    /** Clears the whole stack and lands on the tab shell. */
    fun goHome() {
        nav.navigate(MainRoute) {
            popUpTo(nav.graph.id) { inclusive = true }
            launchSingleTop = true
        }
    }

    /** Replaces the current destination, so Back skips the step being left. */
    fun replace(route: Any) {
        val current = nav.currentBackStackEntry?.destination?.id
        nav.navigate(route) {
            if (current != null) popUpTo(current) { inclusive = true }
            launchSingleTop = true
        }
    }

    // Capture flow
    fun startCapture(assessmentId: String, flow: FlowKind) =
        navigate(if (flow == FlowKind.ONBOARDING) SelfieGuideRoute(assessmentId, flow.name) else CameraRoute(assessmentId, flow.name))

    fun photoCaptured(assessmentId: String, flow: FlowKind) = replace(PhotoQualityRoute(assessmentId, flow.name))

    fun retakeGuidance(assessmentId: String, flow: FlowKind) = replace(RetakeGuidanceRoute(assessmentId, flow.name))

    fun retakePhoto(assessmentId: String, flow: FlowKind) = replace(CameraRoute(assessmentId, flow.name))

    fun photoAccepted(assessmentId: String, flow: FlowKind) = when (flow) {
        FlowKind.ONBOARDING -> navigate(GoalsRoute)
        FlowKind.CHECK_IN -> navigate(ProbeConnectRoute(assessmentId, flow.name))
        FlowKind.STANDALONE -> navigate(CombinedAnalysisRoute(assessmentId, flow.name))
    }

    // Probe flow
    fun probeConnected(assessmentId: String?, flow: FlowKind) = replace(ProbePairedRoute(assessmentId, flow.name))

    fun calibrate(assessmentId: String?, flow: FlowKind) = navigate(CalibrationRoute(assessmentId, flow.name))

    fun measureRegion(sessionId: String, region: MeasurementRegion, assessmentId: String?, flow: FlowKind) =
        push(MeasureRoute(sessionId, region.name, assessmentId, flow.name))

    fun measurementComplete(sessionId: String, assessmentId: String?, flow: FlowKind) =
        navigate(MeasurementCompleteRoute(sessionId, assessmentId, flow.name))

    /** After the probe step, or when the user skips it. */
    fun afterProbe(assessmentId: String?, sessionId: String?, flow: FlowKind) = when {
        flow == FlowKind.STANDALONE || assessmentId == null -> navigate(SensorAnalysisRoute(null, sessionId, FlowKind.STANDALONE.name))
        flow == FlowKind.ONBOARDING -> navigate(CameraAnalysisRoute(assessmentId, flow.name))
        else -> navigate(CombinedAnalysisRoute(assessmentId, flow.name))
    }

    // Analysis flow
    fun cameraAnalysisDone(assessmentId: String, hasSession: Boolean, flow: FlowKind) =
        if (hasSession) navigate(SensorAnalysisRoute(assessmentId, null, flow.name)) else navigate(CombinedAnalysisRoute(assessmentId, flow.name))

    fun sensorAnalysisDone(assessmentId: String?, flow: FlowKind) =
        if (assessmentId != null && flow != FlowKind.STANDALONE) navigate(CombinedAnalysisRoute(assessmentId, flow.name)) else goHome()

    fun combinedDone(assessmentId: String, flow: FlowKind) = when (flow) {
        FlowKind.ONBOARDING -> replace(PotentialRoute(assessmentId, onboarding = true))
        FlowKind.CHECK_IN -> replace(ProgressComparisonRoute(assessmentId, flow.name))
        FlowKind.STANDALONE -> replace(SkinPrintRoute(assessmentId))
    }
}

val LocalAppNavigator = staticCompositionLocalOf<AppNavigator> { error("AppNavigator has not been provided.") }

fun String?.toFlowKind(): FlowKind = runCatching { FlowKind.valueOf(this ?: "") }.getOrDefault(FlowKind.STANDALONE)
