package com.skinthesia.core.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.skinthesia.R
import com.skinthesia.core.design.SkinthesiaTheme
import com.skinthesia.core.ui.icons.SkinthesiaIcons

/** Circular check indicator that fills with clay when selected. */
@Composable
fun CheckCircle(
    selected: Boolean,
    modifier: Modifier = Modifier,
    size: Dp = 22.dp,
) {
    val colors = SkinthesiaTheme.colors
    val motion = SkinthesiaTheme.motion
    val fill by animateColorAsState(
        targetValue = if (selected) colors.primary else Color.Transparent,
        animationSpec = tween(motion.duration(motion.fast)),
        label = "checkFill",
    )
    val outline by animateColorAsState(
        targetValue = if (selected) colors.primary else colors.borderStrong,
        animationSpec = tween(motion.duration(motion.fast)),
        label = "checkOutline",
    )
    // A small, satisfying overshoot the moment a step is completed - settles
    // straight back to rest when unchecked, so it never bounces on the way out.
    val pop = remember { Animatable(1f) }
    LaunchedEffect(selected, motion.reducedMotion) {
        if (selected && !motion.reducedMotion) {
            pop.snapTo(0.7f)
            pop.animateTo(1f, spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow))
        } else {
            pop.snapTo(1f)
        }
    }
    Box(
        modifier = modifier
            .size(size)
            .scale(pop.value)
            .clip(SkinthesiaTheme.shapes.pill)
            .background(fill)
            .border(1.dp, outline, SkinthesiaTheme.shapes.pill),
        contentAlignment = Alignment.Center,
    ) {
        AnimatedVisibility(
            visible = selected,
            enter = fadeIn(tween(motion.duration(motion.fast))) + scaleIn(tween(motion.duration(motion.fast)), initialScale = 0.6f),
            exit = fadeOut(tween(motion.duration(motion.instant))) + scaleOut(tween(motion.duration(motion.instant)), targetScale = 0.6f),
        ) {
            Icon(
                imageVector = SkinthesiaIcons.Check,
                contentDescription = null,
                tint = colors.textOnPrimary,
                modifier = Modifier.size(size * 0.55f),
            )
        }
    }
}

/**
 * Square goal / factor tile with a line icon and caption. Selected tiles gain a
 * clay border, a blush fill and a small check badge, mirroring the reference.
 */
@Composable
fun SelectableTile(
    label: String,
    icon: ImageVector,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    dimmed: Boolean = false,
) {
    val colors = SkinthesiaTheme.colors
    val typography = SkinthesiaTheme.typography
    val spacing = SkinthesiaTheme.spacing
    val motion = SkinthesiaTheme.motion
    val shape = SkinthesiaTheme.shapes.tile
    val interaction = remember { MutableInteractionSource() }
    val container by animateColorAsState(
        targetValue = if (selected) colors.primaryMist else colors.surface,
        animationSpec = tween(motion.duration(motion.base)),
        label = "tileContainer",
    )
    val outline by animateColorAsState(
        targetValue = if (selected) colors.primary else colors.border,
        animationSpec = tween(motion.duration(motion.base)),
        label = "tileOutline",
    )
    val alpha by animateFloatAsState(
        targetValue = if (dimmed && !selected) 0.55f else 1f,
        animationSpec = tween(motion.duration(motion.base)),
        label = "tileAlpha",
    )
    val selectedState = stringResource(R.string.state_selected)
    val notSelectedState = stringResource(R.string.state_not_selected)

    Box(
        modifier = modifier
            .pressScale(interaction, pressedScale = 0.97f)
            .alpha(alpha)
            .aspectRatio(1f)
            .clip(shape)
            .background(container)
            .border(if (selected) spacing.borderSelected else spacing.borderThin, outline, shape)
            .toggleable(
                value = selected,
                enabled = enabled,
                interactionSource = interaction,
                indication = null,
                role = Role.Checkbox,
                onValueChange = { onClick() },
            )
            .semantics { stateDescription = if (selected) selectedState else notSelectedState },
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 8.dp, vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (selected) colors.primary else colors.textSecondary,
                modifier = Modifier.size(24.dp),
            )
            Spacer(Modifier.height(10.dp))
            Text(
                text = label,
                style = typography.labelSmall,
                color = if (selected) colors.textPrimary else colors.textSecondary,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
        AnimatedVisibility(
            visible = selected,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(8.dp),
            enter = fadeIn(tween(motion.duration(motion.fast))) + scaleIn(tween(motion.duration(motion.fast)), initialScale = 0.5f),
            exit = fadeOut(tween(motion.duration(motion.instant))) + scaleOut(tween(motion.duration(motion.instant)), targetScale = 0.5f),
        ) {
            Box(
                modifier = Modifier
                    .size(16.dp)
                    .clip(SkinthesiaTheme.shapes.pill)
                    .background(colors.primary),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = SkinthesiaIcons.Check,
                    contentDescription = null,
                    tint = colors.textOnPrimary,
                    modifier = Modifier.size(9.dp),
                )
            }
        }
    }
}

/** Full-width option row with a leading check circle, as on the target and concerns lists. */
@Composable
fun SelectableRow(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    supporting: String? = null,
) {
    val colors = SkinthesiaTheme.colors
    val typography = SkinthesiaTheme.typography
    val spacing = SkinthesiaTheme.spacing
    val motion = SkinthesiaTheme.motion
    val shape = SkinthesiaTheme.shapes.field
    val interaction = remember { MutableInteractionSource() }
    val container by animateColorAsState(
        targetValue = if (selected) colors.primaryMist else colors.surface,
        animationSpec = tween(motion.duration(motion.base)),
        label = "rowContainer",
    )
    val outline by animateColorAsState(
        targetValue = if (selected) colors.primary.copy(alpha = 0.55f) else colors.border,
        animationSpec = tween(motion.duration(motion.base)),
        label = "rowOutline",
    )
    val selectedState = stringResource(R.string.state_selected)
    val notSelectedState = stringResource(R.string.state_not_selected)

    Row(
        modifier = modifier
            .pressScale(interaction, pressedScale = 0.99f)
            .fillMaxWidth()
            .defaultMinSize(minHeight = 54.dp)
            .clip(shape)
            .background(container)
            .border(spacing.borderThin, outline, shape)
            .toggleable(
                value = selected,
                enabled = enabled,
                interactionSource = interaction,
                indication = null,
                role = Role.Checkbox,
                onValueChange = { onClick() },
            )
            .semantics { stateDescription = if (selected) selectedState else notSelectedState }
            .padding(horizontal = spacing.md, vertical = spacing.sm),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        CheckCircle(selected = selected)
        Spacer(Modifier.width(14.dp))
        Column {
            Text(
                text = label,
                style = typography.label,
                color = colors.textPrimary,
            )
            if (supporting != null) {
                Text(
                    text = supporting,
                    style = typography.caption,
                    color = colors.textMuted,
                )
            }
        }
    }
}
