package com.skinthesia.core.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.unit.dp
import com.skinthesia.core.design.SkinthesiaColorScheme
import com.skinthesia.core.design.SkinthesiaTheme
import com.skinthesia.core.ui.icons.SkinthesiaIcons
import com.skinthesia.domain.model.AttentionLevel
import com.skinthesia.domain.model.DataSource

/** Selectable pill for multi-choice answers (questionnaire, lifestyle, preferences). */
@Composable
fun SkinthesiaChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    leadingIcon: ImageVector? = null,
    enabled: Boolean = true,
    role: Role = Role.Checkbox,
) {
    val colors = SkinthesiaTheme.colors
    val motion = SkinthesiaTheme.motion
    val shape = SkinthesiaTheme.shapes.pill
    val interaction = remember { MutableInteractionSource() }
    val container by animateColorAsState(
        targetValue = if (selected) colors.primaryMist else colors.surface,
        animationSpec = tween(motion.duration(motion.base)),
        label = "chipContainer",
    )
    val outline by animateColorAsState(
        targetValue = if (selected) colors.primary else colors.border,
        animationSpec = tween(motion.duration(motion.base)),
        label = "chipOutline",
    )
    Row(
        modifier = modifier
            .pressScale(interaction, pressedScale = 0.97f)
            .heightIn(min = 44.dp)
            .clip(shape)
            .background(container)
            .border(if (selected) 1.3.dp else 1.dp, outline, shape)
            .toggleable(
                value = selected,
                enabled = enabled,
                interactionSource = interaction,
                indication = null,
                role = role,
                onValueChange = { onClick() },
            )
            .semantics { stateDescription = if (selected) "Selected" else "Not selected" }
            .padding(start = if (leadingIcon != null || selected) 12.dp else 16.dp, end = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        when {
            selected -> Icon(SkinthesiaIcons.Check, contentDescription = null, tint = colors.primary, modifier = Modifier.size(15.dp))
            leadingIcon != null -> Icon(leadingIcon, contentDescription = null, tint = colors.textSecondary, modifier = Modifier.size(17.dp))
        }
        if (selected || leadingIcon != null) Spacer(Modifier.width(7.dp))
        Text(
            text = label,
            style = SkinthesiaTheme.typography.label,
            color = if (selected) colors.textPrimary else colors.textSecondary,
        )
    }
}

/** Wrapping group of [SkinthesiaChip]s. Use [single] for one-of-many questions. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun <T> ChoiceChips(
    options: List<T>,
    selected: Set<T>,
    onToggle: (T) -> Unit,
    label: (T) -> String,
    modifier: Modifier = Modifier,
    icon: ((T) -> ImageVector?)? = null,
    single: Boolean = false,
) {
    FlowRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        options.forEach { option ->
            SkinthesiaChip(
                label = label(option),
                selected = option in selected,
                onClick = { onToggle(option) },
                leadingIcon = icon?.invoke(option),
                role = if (single) Role.RadioButton else Role.Checkbox,
            )
        }
    }
}

enum class TagTone { NEUTRAL, BLUSH, SAGE, CLAY, GOLD, MIST, ROSE }

private fun TagTone.container(c: SkinthesiaColorScheme): Color = when (this) {
    TagTone.NEUTRAL -> c.surfaceMuted
    TagTone.BLUSH -> c.blushMist
    TagTone.SAGE -> c.successSoft
    TagTone.CLAY -> c.primaryMist
    TagTone.GOLD -> c.goldSoft
    TagTone.MIST -> c.infoSoft
    TagTone.ROSE -> c.warningSoft
}

private fun TagTone.content(c: SkinthesiaColorScheme): Color = when (this) {
    TagTone.NEUTRAL -> c.textSecondary
    TagTone.BLUSH -> c.textPrimary
    TagTone.SAGE -> c.successStrong
    TagTone.CLAY -> c.primary
    TagTone.GOLD -> c.goldStrong
    TagTone.MIST -> c.infoStrong
    TagTone.ROSE -> c.warningStrong
}

/** Small static label pill. */
@Composable
fun Tag(
    text: String,
    modifier: Modifier = Modifier,
    tone: TagTone = TagTone.NEUTRAL,
    icon: ImageVector? = null,
    dot: Boolean = false,
) {
    val colors = SkinthesiaTheme.colors
    val content = tone.content(colors)
    Row(
        modifier = modifier
            .clip(SkinthesiaTheme.shapes.pill)
            .background(tone.container(colors))
            .padding(horizontal = 10.dp, vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (dot) {
            Box(Modifier.size(6.dp).clip(CircleShape).background(content))
            Spacer(Modifier.width(6.dp))
        }
        if (icon != null) {
            Icon(icon, contentDescription = null, tint = content, modifier = Modifier.size(12.dp))
            Spacer(Modifier.width(5.dp))
        }
        Text(text = text, style = SkinthesiaTheme.typography.labelSmall, color = content, maxLines = 1)
    }
}

/**
 * Honest provenance label for any value: simulated readings, visual estimates,
 * probe measurements, self-reported answers and derived indicators.
 */
@Composable
fun SourceBadge(source: DataSource, modifier: Modifier = Modifier) {
    val tone = when (source) {
        DataSource.SENSOR_SIMULATED -> TagTone.GOLD
        DataSource.VISUAL_ESTIMATE -> TagTone.MIST
        DataSource.SENSOR_MEASURED -> TagTone.SAGE
        DataSource.USER_REPORTED -> TagTone.NEUTRAL
        DataSource.DERIVED -> TagTone.BLUSH
    }
    Tag(text = source.label, tone = tone, dot = true, modifier = modifier)
}

/** Calm attention level label for focus areas. */
@Composable
fun AttentionPill(level: AttentionLevel, modifier: Modifier = Modifier) {
    val tone = when (level) {
        AttentionLevel.NEEDS_ATTENTION -> TagTone.ROSE
        AttentionLevel.MODERATE -> TagTone.GOLD
        AttentionLevel.MILD -> TagTone.MIST
        AttentionLevel.GOOD -> TagTone.SAGE
    }
    Tag(text = level.label, tone = tone, modifier = modifier)
}
