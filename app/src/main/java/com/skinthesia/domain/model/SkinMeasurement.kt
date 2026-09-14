package com.skinthesia.domain.model

/** Every quantity Skinthesia tracks, from live probe readings to derived scores. */
enum class SkinMetric {
    HYDRATION,
    SEBUM,
    TEMPERATURE,
    TEXTURE,
    ELASTICITY,
    PIGMENTATION,
    PORES,
    REDNESS,
    FINE_LINES,
    BARRIER,
}

/** A single DermoScan reading. Percent values are 0..100, temperature in Celsius. */
data class SkinMeasurement(
    val hydration: Float,
    val sebum: Float,
    val temperature: Float,
    val texture: Float,
    val elasticity: Float,
    val measuredAt: Long,
)

/** A scored metric, optionally with the change since the previous SkinPrint. */
data class ProgressMetric(
    val metric: SkinMetric,
    /** 0..100 where higher is better. */
    val value: Int,
    /** Signed change versus the previous reading, in points. */
    val delta: Int? = null,
)
