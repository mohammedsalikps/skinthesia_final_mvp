package com.skinthesia.core.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.skinthesia.core.design.SkinthesiaTheme

/** Single-choice option with a title, a quiet description and a radio indicator. */
@Composable
fun OptionRow(
    title: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    description: String? = null,
    icon: ImageVector? = null,
) {
    val colors = SkinthesiaTheme.colors
    val typography = SkinthesiaTheme.typography
    val motion = SkinthesiaTheme.motion
    val shape = SkinthesiaTheme.shapes.field
    val interaction = remember { MutableInteractionSource() }
    val container by animateColorAsState(if (selected) colors.primaryMist else colors.surface, tween(motion.duration(motion.base)), label = "optionBg")
    val outline by animateColorAsState(if (selected) colors.primary else colors.border, tween(motion.duration(motion.base)), label = "optionOutline")
    val dot by animateDpAsState(if (selected) 8.dp else 0.dp, tween(motion.duration(motion.fast)), label = "optionDot")
    Row(
        modifier = modifier
            .pressScale(interaction, pressedScale = 0.99f)
            .fillMaxWidth()
            .heightIn(min = 60.dp)
            .clip(shape)
            .background(container)
            .border(if (selected) 1.3.dp else 1.dp, outline, shape)
            .selectable(selected = selected, interactionSource = interaction, indication = null, role = Role.RadioButton, onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (icon != null) {
            Icon(icon, contentDescription = null, tint = if (selected) colors.primary else colors.textSecondary, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(14.dp))
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, style = typography.labelLarge, color = colors.textPrimary)
            if (description != null) {
                Spacer(Modifier.size(2.dp))
                Text(text = description, style = typography.bodySmall, color = colors.textMuted)
            }
        }
        Spacer(Modifier.width(12.dp))
        Box(
            modifier = Modifier
                .size(20.dp)
                .clip(CircleShape)
                .border(1.3.dp, if (selected) colors.primary else colors.borderStrong, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Box(Modifier.size(dot).clip(CircleShape).background(colors.primary))
        }
    }
}

/** Fixed-column grid of equally sized cells that works inside a scrolling column. */
@Composable
fun <T> TileGrid(
    items: List<T>,
    modifier: Modifier = Modifier,
    columns: Int = 3,
    gap: androidx.compose.ui.unit.Dp = SkinthesiaTheme.spacing.gridGap,
    staggered: Boolean = true,
    content: @Composable (T) -> Unit,
) {
    val motion = SkinthesiaTheme.motion
    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(gap)) {
        items.chunked(columns).forEachIndexed { rowIndex, row ->
            val rowContent: @Composable () -> Unit = {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(gap)) {
                    row.forEach { item -> Box(modifier = Modifier.weight(1f)) { content(item) } }
                    repeat(columns - row.size) { Spacer(Modifier.weight(1f)) }
                }
            }
            if (staggered) FadeInUp(delayMillis = motion.stagger(rowIndex + 1)) { rowContent() } else rowContent()
        }
    }
}

/** Compact question block: small icon, label, then its answer control. */
@Composable
fun QuestionBlock(
    title: String,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    hint: String? = null,
    content: @Composable () -> Unit,
) {
    val colors = SkinthesiaTheme.colors
    val typography = SkinthesiaTheme.typography
    Column(modifier = modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (icon != null) {
                Icon(icon, contentDescription = null, tint = colors.primary, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(10.dp))
            }
            Text(text = title, style = typography.labelLarge, color = colors.textPrimary)
        }
        if (hint != null) {
            Spacer(Modifier.size(4.dp))
            Text(text = hint, style = typography.bodySmall, color = colors.textMuted)
        }
        Spacer(Modifier.size(12.dp))
        content()
    }
}
