package com.skinthesia.core.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.skinthesia.core.design.SkinthesiaTheme
import com.skinthesia.core.ui.icons.SkinthesiaIcons

/** Standard list row: soft icon disc, title, optional subtitle, trailing value and chevron. */
@Composable
fun ListRow(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    leadingIcon: ImageVector? = null,
    leading: (@Composable () -> Unit)? = null,
    trailingText: String? = null,
    trailing: (@Composable () -> Unit)? = null,
    onClick: (() -> Unit)? = null,
    showChevron: Boolean = onClick != null,
    iconTint: Color = SkinthesiaTheme.colors.primary,
    iconContainer: Color = SkinthesiaTheme.colors.primaryMist,
    titleColor: Color = SkinthesiaTheme.colors.textPrimary,
) {
    val colors = SkinthesiaTheme.colors
    val typography = SkinthesiaTheme.typography
    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 60.dp)
            .then(if (onClick != null) Modifier.clickable(role = Role.Button, onClick = onClick) else Modifier)
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        when {
            leading != null -> {
                leading()
                Spacer(Modifier.width(14.dp))
            }
            leadingIcon != null -> {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(iconContainer),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(leadingIcon, contentDescription = null, tint = iconTint, modifier = Modifier.size(19.dp))
                }
                Spacer(Modifier.width(14.dp))
            }
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, style = typography.labelLarge, color = titleColor, maxLines = 2, overflow = TextOverflow.Ellipsis)
            if (subtitle != null) {
                Spacer(Modifier.size(2.dp))
                Text(text = subtitle, style = typography.bodySmall, color = colors.textMuted, maxLines = 3, overflow = TextOverflow.Ellipsis)
            }
        }
        if (trailingText != null) {
            Spacer(Modifier.width(12.dp))
            Text(text = trailingText, style = typography.numeric, color = colors.textSecondary)
        }
        if (trailing != null) {
            Spacer(Modifier.width(12.dp))
            trailing()
        }
        if (showChevron) {
            Spacer(Modifier.width(6.dp))
            Icon(SkinthesiaIcons.ChevronRight, contentDescription = null, tint = colors.textMuted, modifier = Modifier.size(18.dp))
        }
    }
}

/**
 * Section title with an optional overline and a quiet trailing action link.
 * Set [curated] on a Skinthesia-picked selection (a recommendation shelf, not
 * general browsing) to add a small trailing mark showing it's a Skinthesia pick.
 */
@Composable
fun SectionHeader(
    title: String,
    modifier: Modifier = Modifier,
    overline: String? = null,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
    curated: Boolean = false,
) {
    val colors = SkinthesiaTheme.colors
    val typography = SkinthesiaTheme.typography
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Bottom,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            if (overline != null) {
                SectionOverline(text = overline)
                Spacer(Modifier.size(4.dp))
            }
            Text(
                text = title,
                style = typography.titleSmall,
                color = colors.textPrimary,
                modifier = Modifier.semantics { heading() },
            )
        }
        if (curated) {
            Spacer(Modifier.width(8.dp))
            BrandMonogram(size = 18.dp, modifier = Modifier.padding(bottom = 3.dp))
        }
        if (actionLabel != null && onAction != null) {
            Row(
                modifier = Modifier
                    .heightIn(min = 40.dp)
                    .clip(SkinthesiaTheme.shapes.pill)
                    .clickable(role = Role.Button, onClick = onAction)
                    .padding(start = 10.dp, end = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(text = actionLabel, style = typography.buttonSmall, color = colors.primary)
                Spacer(Modifier.width(4.dp))
                Icon(SkinthesiaIcons.ArrowRight, contentDescription = null, tint = colors.primary, modifier = Modifier.size(15.dp))
            }
        }
    }
}

/** A quiet label and value on one line. */
@Composable
fun KeyValueRow(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    valueColor: Color = SkinthesiaTheme.colors.textPrimary,
    emphasize: Boolean = false,
) {
    val typography = SkinthesiaTheme.typography
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(text = label, style = typography.body, color = SkinthesiaTheme.colors.textSecondary, modifier = Modifier.weight(1f))
        Text(text = value, style = if (emphasize) typography.labelLarge else typography.numeric, color = valueColor)
    }
}
