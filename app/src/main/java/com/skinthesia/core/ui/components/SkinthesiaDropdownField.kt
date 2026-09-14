package com.skinthesia.core.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.skinthesia.R
import com.skinthesia.core.design.SkinthesiaTheme
import com.skinthesia.core.ui.icons.SkinthesiaIcons

/** One choice in a [SkinthesiaDropdownField]. */
data class DropdownOption<T>(
    val value: T,
    val label: String,
)

/** Labelled single-select field styled as a soft bordered box with a chevron. */
@Composable
fun <T> SkinthesiaDropdownField(
    label: String,
    selected: T?,
    options: List<DropdownOption<T>>,
    onSelect: (T) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = stringResource(R.string.field_select),
) {
    val colors = SkinthesiaTheme.colors
    val typography = SkinthesiaTheme.typography
    val spacing = SkinthesiaTheme.spacing
    val motion = SkinthesiaTheme.motion
    val shape = SkinthesiaTheme.shapes.field
    val density = LocalDensity.current
    val interaction = remember { MutableInteractionSource() }

    var expanded by remember { mutableStateOf(false) }
    var fieldWidthPx by remember { mutableIntStateOf(0) }
    val selectedLabel = options.firstOrNull { it.value == selected }?.label
    val chevronRotation by animateFloatAsState(
        targetValue = if (expanded) 180f else 0f,
        animationSpec = tween(motion.duration(motion.fast)),
        label = "chevron",
    )

    Column(modifier = modifier) {
        Text(text = label, style = typography.labelSmall, color = colors.textSecondary)
        Spacer(Modifier.height(8.dp))
        Box {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .onSizeChanged { fieldWidthPx = it.width }
                    .height(52.dp)
                    .clip(shape)
                    .background(colors.surface)
                    .border(
                        spacing.borderThin,
                        if (expanded) colors.primary else colors.borderStrong,
                        shape,
                    )
                    .clickable(
                        interactionSource = interaction,
                        indication = null,
                        role = Role.DropdownList,
                    ) { expanded = true }
                    .padding(horizontal = spacing.md),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = selectedLabel ?: placeholder,
                    style = typography.body,
                    color = if (selectedLabel != null) colors.textPrimary else colors.textMuted,
                    modifier = Modifier.weight(1f),
                )
                Icon(
                    imageVector = SkinthesiaIcons.ChevronDown,
                    contentDescription = null,
                    tint = colors.textSecondary,
                    modifier = Modifier
                        .size(18.dp)
                        .graphicsLayer { rotationZ = chevronRotation },
                )
            }
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier.width(with(density) { fieldWidthPx.toDp() }),
                shape = SkinthesiaTheme.shapes.medium,
                containerColor = colors.surfaceElevated,
                border = BorderStroke(spacing.borderThin, colors.border),
                shadowElevation = 2.dp,
                tonalElevation = 0.dp,
            ) {
                options.forEach { option ->
                    val isSelected = option.value == selected
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = option.label,
                                style = typography.body,
                                color = if (isSelected) colors.primary else colors.textPrimary,
                            )
                        },
                        onClick = {
                            onSelect(option.value)
                            expanded = false
                        },
                        trailingIcon = if (isSelected) {
                            {
                                Icon(
                                    imageVector = SkinthesiaIcons.Check,
                                    contentDescription = null,
                                    tint = colors.primary,
                                    modifier = Modifier.size(16.dp),
                                )
                            }
                        } else {
                            null
                        },
                        contentPadding = PaddingValues(horizontal = spacing.md),
                    )
                }
            }
        }
    }
}
