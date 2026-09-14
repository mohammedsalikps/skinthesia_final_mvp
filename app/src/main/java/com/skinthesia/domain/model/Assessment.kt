package com.skinthesia.domain.model

import kotlinx.serialization.Serializable

@Serializable
enum class AssessmentKind(val label: String) {
    BASELINE("Baseline"),
    CHECK_IN("Weekly check-in"),
}

@Serializable
enum class AssessmentStatus { IN_PROGRESS, COMPLETE, ABANDONED }

/**
 * One pass through the analysis loop: selfie, optional probe session, camera
 * analysis and the combined result. The baseline and every check-in are assessments.
 */
@Serializable
data class Assessment(
    val id: String,
    val kind: AssessmentKind,
    /** Programme week this assessment represents; the baseline is week 0. */
    val week: Int,
    val startedAt: Long,
    val completedAt: Long? = null,
    val status: AssessmentStatus = AssessmentStatus.IN_PROGRESS,
    val photo: SkinPhoto? = null,
    val quality: PhotoQualityResult? = null,
    val measurementSessionId: String? = null,
    val probeSkipped: Boolean = false,
    val cameraAnalysis: CameraAnalysisResult? = null,
    val combined: CombinedAnalysis? = null,
) {
    val skinPrint: SkinPrint? get() = combined?.skinPrint
    val isComplete: Boolean get() = status == AssessmentStatus.COMPLETE
    val weekLabel: String get() = if (week == 0) "Day 1" else "Week $week"
}
