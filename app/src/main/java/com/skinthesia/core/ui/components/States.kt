package com.skinthesia.core.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.skinthesia.core.design.SkinthesiaTheme
import com.skinthesia.core.ui.icons.SkinthesiaIcons

/** The monogram inside a slowly breathing ring; the app's calm loading signature. */
@Composable
fun BreathingMark(modifier: Modifier = Modifier, size: Dp = 64.dp) {
    val colors = SkinthesiaTheme.colors
    val motion = SkinthesiaTheme.motion
    val transition = rememberInfiniteTransition(label = "breathing")
    val phase by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(1800, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "breath",
    )
    val t = if (motion.reducedMotion) 0.5f else phase
    Box(modifier = modifier.size(size), contentAlignment = Alignment.Center) {
        Canvas(Modifier.fillMaxSize()) {
            val base = this.size.minDimension / 2
            drawCircle(color = colors.accentBlush.copy(alpha = 0.35f + 0.25f * t), radius = base * (0.78f + 0.22f * t))
            drawCircle(color = colors.primary.copy(alpha = 0.25f), radius = base * (0.78f + 0.22f * t), style = Stroke(1.dp.toPx()))
        }
        BrandMonogram(size = size * 0.52f)
    }
}

@Composable
fun LoadingState(
    message: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 48.dp)
            .semantics { liveRegion = LiveRegionMode.Polite },
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        BreathingMark()
        Spacer(Modifier.height(16.dp))
        Text(
            text = message,
            style = SkinthesiaTheme.typography.body,
            color = SkinthesiaTheme.colors.textSecondary,
            textAlign = TextAlign.Center,
        )
    }
}

/** Friendly empty state with an optional call to action. */
@Composable
fun EmptyState(
    icon: ImageVector,
    title: String,
    body: String,
    modifier: Modifier = Modifier,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
) {
    val colors = SkinthesiaTheme.colors
    val typography = SkinthesiaTheme.typography
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 40.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(CircleShape)
                .background(colors.surface)
                .border(1.dp, colors.border, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Icon(icon, contentDescription = null, tint = colors.primary, modifier = Modifier.size(26.dp))
        }
        Spacer(Modifier.height(4.dp))
        Text(text = title, style = typography.titleSmall, color = colors.textPrimary, textAlign = TextAlign.Center)
        Text(
            text = body,
            style = typography.body,
            color = colors.textSecondary,
            textAlign = TextAlign.Center,
            modifier = Modifier.widthIn(max = 320.dp),
        )
        if (actionLabel != null && onAction != null) {
            Spacer(Modifier.height(6.dp))
            SkinthesiaPillButton(text = actionLabel, onClick = onAction)
        }
    }
}

/** Error state that always offers a way forward. */
@Composable
fun ErrorState(
    title: String,
    body: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
    retryLabel: String = "Try again",
    secondaryLabel: String? = null,
    onSecondary: (() -> Unit)? = null,
) {
    val colors = SkinthesiaTheme.colors
    val typography = SkinthesiaTheme.typography
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 32.dp)
            .semantics { liveRegion = LiveRegionMode.Assertive },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Box(
            modifier = Modifier
                .size(60.dp)
                .clip(CircleShape)
                .background(colors.warningSoft),
            contentAlignment = Alignment.Center,
        ) {
            Icon(SkinthesiaIcons.Alert, contentDescription = null, tint = colors.warningStrong, modifier = Modifier.size(24.dp))
        }
        Text(text = title, style = typography.titleSmall, color = colors.textPrimary, textAlign = TextAlign.Center)
        Text(
            text = body,
            style = typography.body,
            color = colors.textSecondary,
            textAlign = TextAlign.Center,
            modifier = Modifier.widthIn(max = 320.dp),
        )
        Spacer(Modifier.height(6.dp))
        SkinthesiaPillButton(text = retryLabel, onClick = onRetry, icon = SkinthesiaIcons.Refresh)
        if (secondaryLabel != null && onSecondary != null) {
            SkinthesiaTextButton(text = secondaryLabel, onClick = onSecondary)
        }
    }
}
