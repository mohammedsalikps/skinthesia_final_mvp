package com.skinthesia.domain.model

enum class RoutineTime { AM, PM }

enum class RoutineStepType { CLEANSE, TREAT, MOISTURIZE, PROTECT }

/** One step of the personalized AM or PM protocol. */
data class RoutineStep(
    val order: Int,
    val type: RoutineStepType,
    val productName: String,
    val purpose: String,
    val time: RoutineTime,
    val productImage: ImageSource? = null,
)
