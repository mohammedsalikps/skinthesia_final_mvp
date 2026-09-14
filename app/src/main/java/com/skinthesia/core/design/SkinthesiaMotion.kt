package com.skinthesia.core.design

import android.provider.Settings
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.Easing
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode

/**
 * Motion tokens. Skinthesia animations are calm and unhurried: eased-out
 * entrances, gentle progress reveals, and no bounce.
 */
@Immutable
data class SkinthesiaMotion(
    val instant: Int = 90,
    val fast: Int = 160,
    val base: Int = 260,
    val slow: Int = 420,
    val reveal: Int = 700,
    val stagger: Int = 70,
    val enterEasing: Easing = CubicBezierEasing(0.2f, 0.0f, 0.0f, 1.0f),
    val exitEasing: Easing = CubicBezierEasing(0.4f, 0.0f, 1.0f, 1.0f),
    val standardEasing: Easing = CubicBezierEasing(0.4f, 0.0f, 0.2f, 1.0f),
    /** Whether the user has asked the system to minimise animation. */
    val reducedMotion: Boolean = false,
) {
    /** Returns [millis] or zero when reduced motion is requested. */
    fun duration(millis: Int): Int = if (reducedMotion) 0 else millis

    /** Entrance delay for the [index]-th item in a staggered list. */
    fun stagger(index: Int): Int = if (reducedMotion) 0 else index * stagger
}

val LocalSkinthesiaMotion = staticCompositionLocalOf { SkinthesiaMotion() }

/**
 * Reads the system animator scale. A scale of zero means the user has turned
 * animations off in accessibility or developer settings, which we honour as
 * a reduced motion preference.
 */
@Composable
fun rememberReducedMotionPreference(): Boolean {
    if (LocalInspectionMode.current) return false
    val context = LocalContext.current
    return remember(context) {
        runCatching {
            val scale = Settings.Global.getFloat(
                context.contentResolver,
                Settings.Global.ANIMATOR_DURATION_SCALE,
                1f,
            )
            scale == 0f
        }.getOrDefault(false)
    }
}
