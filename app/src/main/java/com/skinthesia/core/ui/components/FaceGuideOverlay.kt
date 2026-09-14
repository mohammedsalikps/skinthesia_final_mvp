package com.skinthesia.core.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.skinthesia.core.design.SkinthesiaTheme

/**
 * Framing guide drawn over the camera preview: clay corner brackets, a dashed
 * face oval and, while [scanning], a soft light sweep. Purely decorative, so it
 * carries no semantics.
 */
@Composable
fun FaceGuideOverlay(
    modifier: Modifier = Modifier,
    bracketColor: Color = SkinthesiaTheme.colors.primary,
    guideColor: Color = Color.White.copy(alpha = 0.85f),
    scanning: Boolean = false,
) {
    val motion = SkinthesiaTheme.motion
    val transition = rememberInfiniteTransition(label = "faceGuide")
    val sweep by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2400, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "sweep",
    )
    val showSweep = scanning && !motion.reducedMotion

    Canvas(modifier = modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height
        val inset = 14.dp.toPx()
        val armLength = 22.dp.toPx()
        val bracketStroke = 2.dp.toPx()

        fun corner(x: Float, y: Float, dirX: Float, dirY: Float) {
            drawLine(
                color = bracketColor,
                start = Offset(x, y),
                end = Offset(x + dirX * armLength, y),
                strokeWidth = bracketStroke,
                cap = StrokeCap.Round,
            )
            drawLine(
                color = bracketColor,
                start = Offset(x, y),
                end = Offset(x, y + dirY * armLength),
                strokeWidth = bracketStroke,
                cap = StrokeCap.Round,
            )
        }
        corner(inset, inset, 1f, 1f)
        corner(width - inset, inset, -1f, 1f)
        corner(inset, height - inset, 1f, -1f)
        corner(width - inset, height - inset, -1f, -1f)

        val ovalWidth = width * 0.58f
        val ovalHeight = height * 0.70f
        val ovalTopLeft = Offset((width - ovalWidth) / 2f, height * 0.5f - ovalHeight / 2f)
        drawOval(
            color = guideColor,
            topLeft = ovalTopLeft,
            size = Size(ovalWidth, ovalHeight),
            style = Stroke(
                width = 1.5.dp.toPx(),
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 10f)),
            ),
        )

        if (showSweep) {
            val y = ovalTopLeft.y + ovalHeight * sweep
            drawLine(
                brush = Brush.horizontalGradient(
                    colors = listOf(Color.Transparent, guideColor, Color.Transparent),
                    startX = ovalTopLeft.x,
                    endX = ovalTopLeft.x + ovalWidth,
                ),
                start = Offset(ovalTopLeft.x, y),
                end = Offset(ovalTopLeft.x + ovalWidth, y),
                strokeWidth = 1.5.dp.toPx(),
            )
        }
    }
}
