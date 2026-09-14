package com.skinthesia.core.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.skinthesia.core.design.SkinthesiaTheme
import com.skinthesia.core.ui.icons.SkinthesiaIcons

/** Warm clay pill with cream text and a trailing arrow: the app's main call to action. */
@Composable
fun SkinthesiaPrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    loading: Boolean = false,
    trailingIcon: ImageVector? = SkinthesiaIcons.ArrowRight,
) {
    val colors = SkinthesiaTheme.colors
    val typography = SkinthesiaTheme.typography
    val spacing = SkinthesiaTheme.spacing
    val motion = SkinthesiaTheme.motion
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val container by animateColorAsState(
        targetValue = when {
            !enabled -> colors.disabledContainer
            pressed -> colors.primaryPressed
            else -> colors.primary
        },
        animationSpec = tween(motion.duration(motion.fast)),
        label = "primaryButtonColor",
    )
    val contentColor = if (enabled) colors.textOnPrimary else colors.disabledContent

    Box(
        modifier = modifier
            .pressScale(interaction)
            .fillMaxWidth()
            .height(spacing.buttonHeight)
            .clip(SkinthesiaTheme.shapes.pill)
            .background(container)
            .clickable(
                enabled = enabled && !loading,
                interactionSource = interaction,
                indication = null,
                role = Role.Button,
                onClick = onClick,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.alpha(if (loading) 0f else 1f),
        ) {
            Text(text = text, style = typography.button, color = contentColor)
            if (trailingIcon != null) {
                Spacer(Modifier.width(10.dp))
                Icon(
                    imageVector = trailingIcon,
                    contentDescription = null,
                    tint = contentColor,
                    modifier = Modifier.size(18.dp),
                )
            }
        }
        if (loading) {
            CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                color = contentColor,
                strokeWidth = 1.5.dp,
            )
        }
    }
}

/** Cream pill with a thin border and dark brown text for secondary actions. */
@Composable
fun SkinthesiaSecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    leadingIcon: ImageVector? = null,
    trailingIcon: ImageVector? = null,
) {
    val colors = SkinthesiaTheme.colors
    val typography = SkinthesiaTheme.typography
    val spacing = SkinthesiaTheme.spacing
    val interaction = remember { MutableInteractionSource() }
    val contentColor = if (enabled) colors.textPrimary else colors.textMuted

    Box(
        modifier = modifier
            .pressScale(interaction)
            .fillMaxWidth()
            .height(spacing.buttonHeight)
            .clip(SkinthesiaTheme.shapes.pill)
            .background(colors.surface)
            .border(spacing.borderThin, colors.borderStrong, SkinthesiaTheme.shapes.pill)
            .clickable(
                enabled = enabled,
                interactionSource = interaction,
                indication = null,
                role = Role.Button,
                onClick = onClick,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (leadingIcon != null) {
                Icon(leadingIcon, contentDescription = null, tint = contentColor, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(10.dp))
            }
            Text(text = text, style = typography.button, color = contentColor)
            if (trailingIcon != null) {
                Spacer(Modifier.width(10.dp))
                Icon(trailingIcon, contentDescription = null, tint = contentColor, modifier = Modifier.size(18.dp))
            }
        }
    }
}

/** Quiet inline action such as "Skip for now" or "Choose from gallery". */
@Composable
fun SkinthesiaTextButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    leadingIcon: ImageVector? = null,
) {
    val colors = SkinthesiaTheme.colors
    val typography = SkinthesiaTheme.typography
    val spacing = SkinthesiaTheme.spacing
    val interaction = remember { MutableInteractionSource() }
    val contentColor = if (enabled) colors.textSecondary else colors.textMuted

    Row(
        modifier = modifier
            .defaultMinSize(minHeight = spacing.touchTarget)
            .clip(SkinthesiaTheme.shapes.pill)
            .clickable(
                enabled = enabled,
                interactionSource = interaction,
                indication = null,
                role = Role.Button,
                onClick = onClick,
            )
            .padding(horizontal = spacing.md, vertical = spacing.xs),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
    ) {
        if (leadingIcon != null) {
            Icon(leadingIcon, contentDescription = null, tint = contentColor, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(8.dp))
        }
        Text(text = text, style = typography.buttonSmall, color = contentColor)
    }
}

/** Circular outlined icon control with an optional caption, as used for Retake and Flip. */
@Composable
fun SkinthesiaIconButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    label: String? = null,
    size: Dp = 48.dp,
) {
    val colors = SkinthesiaTheme.colors
    val typography = SkinthesiaTheme.typography
    val interaction = remember { MutableInteractionSource() }
    val tint = if (enabled) colors.textPrimary else colors.textMuted

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier
                .pressScale(interaction, pressedScale = 0.94f)
                .size(size)
                .clip(SkinthesiaTheme.shapes.pill)
                .background(colors.surface)
                .border(SkinthesiaTheme.spacing.borderThin, colors.borderStrong, SkinthesiaTheme.shapes.pill)
                .clickable(
                    enabled = enabled,
                    interactionSource = interaction,
                    indication = null,
                    role = Role.Button,
                    onClick = onClick,
                ),
            contentAlignment = Alignment.Center,
        ) {
            Icon(icon, contentDescription = contentDescription, tint = tint, modifier = Modifier.size(20.dp))
        }
        if (label != null) {
            Spacer(Modifier.height(6.dp))
            Text(text = label, style = typography.caption, color = colors.textSecondary)
        }
    }
}
