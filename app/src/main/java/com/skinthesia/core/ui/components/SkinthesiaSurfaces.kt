package com.skinthesia.core.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CornerBasedShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.skinthesia.core.design.SkinthesiaPalette
import com.skinthesia.core.design.SkinthesiaTheme
import com.skinthesia.core.ui.icons.SkinthesiaIcons

/** A warm, low-alpha shadow tint instead of Android's default cool black. */
private val CardShadowTint = SkinthesiaPalette.Cocoa.copy(alpha = 0.14f)

/**
 * Cream card with a hairline border and a soft, warm-toned shadow that lifts
 * it gently off the page. Fills the available width by default so stacked
 * cards align; pass a width or weight modifier to size it otherwise. Set
 * [elevated] on a single hero card per screen (a SkinPrint, a headline stat)
 * so it reads as the page's focal surface; optional [onClick] makes the whole
 * card tappable.
 */
@Composable
fun SkinthesiaCard(
    modifier: Modifier = Modifier,
    shape: CornerBasedShape = SkinthesiaTheme.shapes.card,
    containerColor: Color = SkinthesiaTheme.colors.surface,
    borderColor: Color = SkinthesiaTheme.colors.border,
    contentPadding: PaddingValues = PaddingValues(SkinthesiaTheme.spacing.md),
    elevated: Boolean = false,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    val interaction = remember { MutableInteractionSource() }
    val base = modifier
        .fillMaxWidth()
        .then(if (onClick != null) Modifier.pressScale(interaction, pressedScale = 0.99f) else Modifier)
        .shadow(
            elevation = if (elevated) 16.dp else 5.dp,
            shape = shape,
            ambientColor = CardShadowTint,
            spotColor = CardShadowTint,
        )
        .clip(shape)
        .background(containerColor)
        .border(SkinthesiaTheme.spacing.borderThin, borderColor, shape)
        .then(
            if (onClick != null) {
                Modifier.clickable(
                    interactionSource = interaction,
                    indication = null,
                    role = Role.Button,
                    onClick = onClick,
                )
            } else {
                Modifier
            },
        )
        .padding(contentPadding)
    Column(modifier = base, content = content)
}

/** Uppercase, letter-spaced micro label that introduces a section. */
@Composable
fun SectionOverline(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = SkinthesiaTheme.colors.textMuted,
) {
    Text(
        text = text.uppercase(),
        style = SkinthesiaTheme.typography.overline,
        color = color,
        modifier = modifier,
    )
}

/** Hairline divider. */
@Composable
fun SkinthesiaDivider(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(SkinthesiaTheme.colors.divider),
    )
}

/** Small sage check badge followed by a line of copy, as on the Welcome screen. */
@Composable
fun BenefitRow(
    text: String,
    modifier: Modifier = Modifier,
) {
    val colors = SkinthesiaTheme.colors
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(18.dp)
                .clip(SkinthesiaTheme.shapes.pill)
                .background(colors.successSoft),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = SkinthesiaIcons.Check,
                contentDescription = null,
                tint = colors.success,
                modifier = Modifier.size(11.dp),
            )
        }
        Spacer(Modifier.width(12.dp))
        Text(
            text = text,
            style = SkinthesiaTheme.typography.body,
            color = colors.textSecondary,
        )
    }
}

/** Quiet inline notice, e.g. "You can choose up to three goals". */
@Composable
fun InfoNotice(
    text: String,
    modifier: Modifier = Modifier,
    icon: ImageVector = SkinthesiaIcons.Info,
) {
    val colors = SkinthesiaTheme.colors
    val spacing = SkinthesiaTheme.spacing
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(SkinthesiaTheme.shapes.field)
            .background(colors.surfaceTint.copy(alpha = 0.55f))
            .padding(horizontal = spacing.md, vertical = spacing.sm),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(icon, contentDescription = null, tint = colors.primary, modifier = Modifier.size(16.dp))
        Spacer(Modifier.width(10.dp))
        Text(text = text, style = SkinthesiaTheme.typography.bodySmall, color = colors.textSecondary)
    }
}
