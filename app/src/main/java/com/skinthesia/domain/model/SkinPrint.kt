package com.skinthesia.domain.model

/** Overall skin health band shown with the SkinPrint score. */
enum class SkinHealthStatus {
    NEEDS_ATTENTION,
    FAIR,
    GOOD,
    EXCELLENT;

    companion object {
        fun fromScore(score: Int): SkinHealthStatus = when {
            score >= 85 -> EXCELLENT
            score >= 65 -> GOOD
            score >= 45 -> FAIR
            else -> NEEDS_ATTENTION
        }
    }
}

enum class Severity { LOW, MODERATE, HIGH }

/** Facial zones used by the face map and region findings. */
enum class SkinRegion {
    FOREHEAD,
    UNDER_EYES,
    LEFT_CHEEK,
    RIGHT_CHEEK,
    NOSE,
    AROUND_MOUTH,
    JAWLINE,
    CHIN,
}

/** A concern detected in a specific facial region. */
data class RegionFinding(
    val region: SkinRegion,
    val concern: SkinConcern,
    val severity: Severity,
    val note: String,
)

/** An influence on the user's skin, with how relevant analysis believes it is. */
data class ContributingFactor(
    val factor: LifestyleFactor,
    val relevance: Severity,
    val description: String,
)

/** The signature report produced by an analysis run. */
data class SkinPrint(
    val id: String,
    /** 0..100 */
    val score: Int,
    val status: SkinHealthStatus,
    val summary: String,
    val metrics: List<ProgressMetric>,
    val regionFindings: List<RegionFinding>,
    val contributingFactors: List<ContributingFactor>,
    val generatedAt: Long,
)
