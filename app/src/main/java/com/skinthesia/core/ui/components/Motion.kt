package com.skinthesia.core.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.interaction.InteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.skinthesia.core.design.SkinthesiaTheme

/** Gentle scale feedback while a control is pressed. */
@Composable
fun Modifier.pressScale(
    interactionSource: InteractionSource,
    pressedScale: Float = 0.98f,
): Modifier {
    val motion = SkinthesiaTheme.motion
    val pressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) pressedScale else 1f,
        animationSpec = tween(motion.duration(motion.fast), easing = motion.standardEasing),
        label = "pressScale",
    )
    return this.graphicsLayer {
        scaleX = scale
        scaleY = scale
    }
}

/**
 * Calm entrance: content fades in while drifting up a few dp. Use [delayMillis]
 * with [com.skinthesia.core.design.SkinthesiaMotion.stagger] to cascade lists.
 * Honours the reduced motion preference by appearing instantly.
 */
@Composable
fun FadeInUp(
    modifier: Modifier = Modifier,
    delayMillis: Int = 0,
    initialOffsetY: Dp = 14.dp,
    content: @Composable () -> Unit,
) {
    val motion = SkinthesiaTheme.motion
    val density = LocalDensity.current
    val visibleState = remember { MutableTransitionState(false).apply { targetState = true } }
    val duration = motion.duration(motion.slow)
    val delay = motion.duration(delayMillis)
    AnimatedVisibility(
        visibleState = visibleState,
        modifier = modifier,
        enter = fadeIn(tween(duration, delay, motion.enterEasing)) +
            slideInVertically(tween(duration, delay, motion.enterEasing)) {
                with(density) { initialOffsetY.roundToPx() }
            },
        exit = ExitTransition.None,
    ) {
        content()
    }
}
