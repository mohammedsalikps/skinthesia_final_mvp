package com.skinthesia.core.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.error
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.skinthesia.core.design.SkinthesiaTheme
import com.skinthesia.core.ui.icons.SkinthesiaIcons

/** Soft bordered text field with a label above, helper or error text and an optional counter. */
@Composable
fun SkinthesiaTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String? = null,
    placeholder: String? = null,
    supportingText: String? = null,
    errorText: String? = null,
    singleLine: Boolean = true,
    minLines: Int = 1,
    maxLength: Int? = null,
    leadingIcon: ImageVector? = null,
    trailing: (@Composable () -> Unit)? = null,
    enabled: Boolean = true,
    keyboardOptions: KeyboardOptions = KeyboardOptions(imeAction = if (singleLine) ImeAction.Done else ImeAction.Default),
    keyboardActions: KeyboardActions = KeyboardActions.Default,
) {
    val colors = SkinthesiaTheme.colors
    val typography = SkinthesiaTheme.typography
    val motion = SkinthesiaTheme.motion
    val shape = SkinthesiaTheme.shapes.field
    val interaction = remember { MutableInteractionSource() }
    val focused by interaction.collectIsFocusedAsState()
    val isError = errorText != null
    val outline by animateColorAsState(
        targetValue = when {
            isError -> colors.warningStrong
            focused -> colors.primary
            else -> colors.borderStrong
        },
        animationSpec = tween(motion.duration(motion.fast)),
        label = "fieldOutline",
    )

    Column(modifier = modifier) {
        if (label != null) {
            Text(text = label, style = typography.labelSmall, color = colors.textSecondary)
            Spacer(Modifier.height(8.dp))
        }
        BasicTextField(
            value = value,
            onValueChange = { next -> if (maxLength == null || next.length <= maxLength) onValueChange(next) },
            enabled = enabled,
            singleLine = singleLine,
            minLines = minLines,
            textStyle = typography.body.copy(color = colors.textPrimary),
            cursorBrush = SolidColor(colors.primary),
            keyboardOptions = keyboardOptions,
            keyboardActions = keyboardActions,
            interactionSource = interaction,
            modifier = Modifier
                .fillMaxWidth()
                .semantics {
                    if (label != null) contentDescription = label
                    if (errorText != null) error(errorText)
                },
            decorationBox = { inner ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 52.dp)
                        .clip(shape)
                        .background(colors.surface)
                        .border(if (focused || isError) 1.4.dp else 1.dp, outline, shape)
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = if (singleLine) Alignment.CenterVertically else Alignment.Top,
                ) {
                    if (leadingIcon != null) {
                        Icon(leadingIcon, contentDescription = null, tint = colors.textMuted, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(10.dp))
                    }
                    Box(modifier = Modifier.weight(1f)) {
                        if (value.isEmpty() && placeholder != null) {
                            Text(text = placeholder, style = typography.body, color = colors.textMuted)
                        }
                        inner()
                    }
                    if (trailing != null) {
                        Spacer(Modifier.width(8.dp))
                        trailing()
                    }
                }
            },
        )
        val helper = errorText ?: supportingText
        if (helper != null || maxLength != null) {
            Spacer(Modifier.height(6.dp))
            Row(Modifier.fillMaxWidth()) {
                Text(
                    text = helper.orEmpty(),
                    style = typography.caption,
                    color = if (isError) colors.warningStrong else colors.textMuted,
                    modifier = Modifier.weight(1f),
                )
                if (maxLength != null && !singleLine) {
                    Text(text = "${value.length}/$maxLength", style = typography.caption, color = colors.textMuted)
                }
            }
        }
    }
}

/** Search field with a leading magnifier and a clear control. */
@Composable
fun SearchField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "Search",
) {
    SkinthesiaTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        placeholder = placeholder,
        leadingIcon = SkinthesiaIcons.Search,
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        trailing = if (value.isNotEmpty()) {
            {
                IconAction(
                    icon = SkinthesiaIcons.Close,
                    contentDescription = "Clear search",
                    onClick = { onValueChange("") },
                    modifier = Modifier.size(28.dp),
                    tint = SkinthesiaTheme.colors.textMuted,
                )
            }
        } else {
            null
        },
    )
}
