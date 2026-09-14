package com.skinthesia.domain.model

import kotlinx.serialization.Serializable
import java.util.UUID

/**
 * Where a value came from. Every number shown to the user carries one of these,
 * so the UI can label it honestly ("Simulated", "Visual estimate", "You told us").
 */
@Serializable
enum class DataSource(val label: String) {
    /** Estimated from a photo by a vision model. Never a clinical measurement. */
    VISUAL_ESTIMATE("Visual estimate"),

    /** Read from a connected Skinthesia probe. */
    SENSOR_MEASURED("Probe measurement"),

    /** Produced by the development probe simulator, not by real hardware. */
    SENSOR_SIMULATED("Simulated reading"),

    /** Told to us by the user. */
    USER_REPORTED("You told us"),

    /** Computed from other values, such as the SkinPrint composite. */
    DERIVED("Tracking indicator"),
}

val DataSource.isSimulated: Boolean get() = this == DataSource.SENSOR_SIMULATED

/** Generic three-step scale used for self-reported context. */
@Serializable
enum class Level(val label: String) { LOW("Low"), MODERATE("Moderate"), HIGH("High") }

/** How much attention an area needs, phrased calmly and without clinical claims. */
@Serializable
enum class AttentionLevel(val label: String) {
    GOOD("Looking good"),
    MILD("Mild"),
    MODERATE("Moderate"),
    NEEDS_ATTENTION("Needs attention"),
}

object Ids {
    fun new(prefix: String): String = "$prefix-${UUID.randomUUID()}"
}
