package com.skinthesia.core.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.skinthesia.R
import com.skinthesia.core.design.SkinthesiaTheme
import com.skinthesia.core.ui.ContentMaxWidth
import com.skinthesia.core.ui.icons.SkinthesiaIcons

/**
 * Shared frame for onboarding steps: back arrow, centred monogram, a thin
 * step indicator, a scrolling content column and a pinned call to action.
 */
@Composable
fun OnboardingScaffold(
    step: Int,
    totalSteps: Int,
    onBack: (() -> Unit)?,
    modifier: Modifier = Modifier,
    /**
     * When false the content column fills the space between the top bar and the
     * pinned action without scrolling, so children can claim height with `weight`.
     */
    scrollable: Boolean = true,
    bottomBar: (@Composable ColumnScope.() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    val colors = SkinthesiaTheme.colors
    val spacing = SkinthesiaTheme.spacing

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(colors.background)
            .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal)),
    ) {
        OnboardingTopBar(step = step, totalSteps = totalSteps, onBack = onBack)
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .then(if (scrollable) Modifier.verticalScroll(rememberScrollState()) else Modifier)
                .padding(horizontal = spacing.screenHorizontal),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Column(
                modifier = Modifier
                    .widthIn(max = ContentMaxWidth)
                    .fillMaxWidth()
                    .then(if (scrollable) Modifier else Modifier.weight(1f)),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                content()
                Spacer(Modifier.height(if (scrollable) spacing.lg else spacing.xs))
            }
        }
        if (bottomBar != null) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = spacing.screenHorizontal)
                    .padding(top = spacing.sm)
                    .navigationBarsPadding()
                    .imePadding()
                    .padding(bottom = spacing.md),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Column(
                    modifier = Modifier
                        .widthIn(max = ContentMaxWidth)
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    bottomBar()
                }
            }
        }
    }
}

@Composable
private fun OnboardingTopBar(
    step: Int,
    totalSteps: Int,
    onBack: (() -> Unit)?,
) {
    val spacing = SkinthesiaTheme.spacing
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
        ) {
            if (onBack != null) {
                BackButton(
                    onClick = onBack,
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .padding(start = 8.dp),
                )
            }
            BrandMonogram(
                size = 28.dp,
                modifier = Modifier.align(Alignment.Center),
            )
        }
        StepProgress(step = step, totalSteps = totalSteps)
        Spacer(Modifier.height(spacing.md))
    }
}

/** Segmented hairline that shows where the user is in onboarding. */
@Composable
fun StepProgress(
    step: Int,
    totalSteps: Int,
    modifier: Modifier = Modifier,
) {
    val colors = SkinthesiaTheme.colors
    val motion = SkinthesiaTheme.motion
    val description = stringResource(R.string.step_progress, step, totalSteps)
    Row(
        modifier = modifier.semantics { contentDescription = description },
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        repeat(totalSteps) { index ->
            val active = index < step
            val color by animateColorAsState(
                targetValue = if (active) colors.primary else colors.border,
                animationSpec = tween(motion.duration(motion.slow)),
                label = "stepSegment",
            )
            Box(
                modifier = Modifier
                    .width(18.dp)
                    .height(2.dp)
                    .clip(SkinthesiaTheme.shapes.pill)
                    .background(color),
            )
        }
    }
}

/** 48dp back control with the thin arrow icon. */
@Composable
fun BackButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = SkinthesiaTheme.colors
    val interaction = remember { MutableInteractionSource() }
    Box(
        modifier = modifier
            .size(SkinthesiaTheme.spacing.touchTarget)
            .clip(SkinthesiaTheme.shapes.pill)
            .clickable(
                interactionSource = interaction,
                indication = null,
                role = Role.Button,
                onClick = onClick,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = SkinthesiaIcons.ArrowLeft,
            contentDescription = stringResource(R.string.action_back),
            tint = colors.textPrimary,
            modifier = Modifier.size(22.dp),
        )
    }
}

/** Centred serif title with an optional muted subtitle. */
@Composable
fun OnboardingHeader(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
) {
    val colors = SkinthesiaTheme.colors
    val typography = SkinthesiaTheme.typography
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = title,
            style = typography.title,
            color = colors.textPrimary,
            textAlign = TextAlign.Center,
        )
        if (subtitle != null) {
            Spacer(Modifier.height(10.dp))
            Text(
                text = subtitle,
                style = typography.subtitle,
                color = colors.textSecondary,
                textAlign = TextAlign.Center,
            )
        }
    }
}
