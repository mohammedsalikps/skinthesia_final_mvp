package com.skinthesia.domain.model

import kotlinx.serialization.Serializable

/** Device-local settings, including the demonstration controls. */
@Serializable
data class AppSettings(
    /**
     * Demonstration timeline: each weekly check-in advances the journey to the next
     * milestone week (4, 8, 12) instead of waiting for calendar weeks. Labelled in the UI.
     */
    val demoTimeline: Boolean = true,
    /** Makes the simulated probe fail its next connection, to exercise recovery flows. */
    val simulateProbeFailure: Boolean = false,
    val personalizedContent: Boolean = true,
    val routineReminders: Boolean = true,
    /** Off by default. The demonstration build sends no analytics anywhere. */
    val shareAnonymousUsage: Boolean = false,
)
