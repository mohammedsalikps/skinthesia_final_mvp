package com.skinthesia.core.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.skinthesia.core.design.SkinthesiaTheme
import com.skinthesia.core.ui.icons.SkinthesiaIcons
import java.util.Locale
import kotlin.math.abs

enum class PillVariant { PRIMARY, SECONDARY, SOFT }

/** Compact pill button for inline actions ("Add to bag", "Book", "Retry"). */
@Composable
fun SkinthesiaPillButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    variant: PillVariant = PillVariant.PRIMARY,
    icon: ImageVector? = null,
    enabled: Boolean = true,
) {
    val colors = SkinthesiaTheme.colors
    val shape = SkinthesiaTheme.shapes.pill
    val interaction = remember { MutableInteractionSource() }
    val (container, content, outline) = when (variant) {
        PillVariant.PRIMARY -> Triple(if (enabled) colors.primary else colors.disabledContainer, colors.textOnPrimary, Color.Transparent)
        PillVariant.SECONDARY -> Triple(colors.surface, if (enabled) colors.textPrimary else colors.textMuted, colors.borderStrong)
        PillVariant.SOFT -> Triple(colors.primaryMist, colors.primary, Color.Transparent)
    }
    Row(
        modifier = modifier
            .pressScale(interaction, pressedScale = 0.97f)
            .heightIn(min = 44.dp)
            .clip(shape)
            .background(container)
            .border(1.dp, outline, shape)
            .clickable(enabled = enabled, interactionSource = interaction, indication = null, role = Role.Button, onClick = onClick)
            .padding(horizontal = 18.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
    ) {
        if (icon != null) {
            Icon(icon, contentDescription = null, tint = content, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(8.dp))
        }
        Text(text = text, style = SkinthesiaTheme.typography.buttonSmall, color = content)
    }
}

/** Initials in a soft tinted disc; experts get a small sage badge. */
@Composable
fun MonogramAvatar(
    name: String,
    modifier: Modifier = Modifier,
    size: Dp = 40.dp,
    isExpert: Boolean = false,
) {
    val colors = SkinthesiaTheme.colors
    val tones = listOf(colors.blushMist, colors.successSoft, colors.goldSoft, colors.infoSoft, colors.primaryMist)
    val tint = tones[abs(name.hashCode()) % tones.size]
    val initials = name.split(" ", ".", "·").filter { it.isNotBlank() }.take(2)
        .joinToString("") { it.first().uppercase(Locale.ROOT) }.ifBlank { "S" }
    Box(modifier = modifier.size(size)) {
        Box(
            modifier = Modifier
                .size(size)
                .clip(CircleShape)
                .background(tint)
                .border(1.dp, colors.border, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = initials,
                style = SkinthesiaTheme.typography.titleSmall.copy(fontSize = (size.value * 0.36f).let { androidx.compose.ui.unit.TextUnit(it, androidx.compose.ui.unit.TextUnitType.Sp) }),
                color = colors.textPrimary,
            )
        }
        if (isExpert) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .size(size * 0.36f)
                    .clip(CircleShape)
                    .background(colors.success)
                    .border(1.5.dp, colors.surface, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Icon(SkinthesiaIcons.Check, contentDescription = null, tint = colors.textOnPrimary, modifier = Modifier.size(size * 0.22f))
            }
        }
    }
}

/** Five thin stars with a numeric rating and optional review count. */
@Composable
fun RatingLine(
    rating: Float,
    modifier: Modifier = Modifier,
    reviewCount: Int? = null,
    starSize: Dp = 13.dp,
) {
    val colors = SkinthesiaTheme.colors
    val description = buildString {
        append(String.format(Locale.US, "Rated %.1f out of 5", rating))
        if (reviewCount != null) append(", $reviewCount reviews")
    }
    Row(
        modifier = modifier.clearAndSetSemantics { contentDescription = description },
        verticalAlignment = Alignment.CenterVertically,
    ) {
        repeat(5) { index ->
            Icon(
                imageVector = if (rating >= index + 0.75f) SkinthesiaIcons.StarFilled else SkinthesiaIcons.Star,
                contentDescription = null,
                tint = colors.gold,
                modifier = Modifier.size(starSize),
            )
        }
        Spacer(Modifier.width(6.dp))
        Text(
            text = String.format(Locale.US, "%.1f", rating) + (reviewCount?.let { " · " + formatCount(it) } ?: ""),
            style = SkinthesiaTheme.typography.caption,
            color = colors.textSecondary,
        )
    }
}

private fun formatCount(count: Int): String = if (count >= 1000) String.format(Locale.US, "%.1fk", count / 1000f) else count.toString()

/** Pill stepper for cart quantities. */
@Composable
fun QuantityStepper(
    quantity: Int,
    onChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
    min: Int = 0,
    max: Int = 9,
) {
    val colors = SkinthesiaTheme.colors
    val shape = SkinthesiaTheme.shapes.pill
    Row(
        modifier = modifier
            .height(40.dp)
            .clip(shape)
            .border(1.dp, colors.borderStrong, shape)
            .background(colors.surface),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        StepperButton(SkinthesiaIcons.Minus, "Decrease quantity", enabled = quantity > min) { onChange(quantity - 1) }
        Text(
            text = quantity.toString(),
            style = SkinthesiaTheme.typography.numeric,
            color = colors.textPrimary,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .widthIn(min = 22.dp)
                .semantics { contentDescription = "Quantity $quantity" },
        )
        StepperButton(SkinthesiaIcons.Plus, "Increase quantity", enabled = quantity < max) { onChange(quantity + 1) }
    }
}

@Composable
private fun StepperButton(icon: ImageVector, description: String, enabled: Boolean, onClick: () -> Unit) {
    val colors = SkinthesiaTheme.colors
    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(CircleShape)
            .clickable(enabled = enabled, role = Role.Button, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(icon, contentDescription = description, tint = if (enabled) colors.textPrimary else colors.border, modifier = Modifier.size(16.dp))
    }
}

/** Calm confirmation dialog; [destructive] uses the deep rose tone for the confirm action. */
@Composable
fun SkinthesiaConfirmDialog(
    title: String,
    body: String,
    confirmLabel: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    dismissLabel: String = "Cancel",
    destructive: Boolean = false,
) {
    val colors = SkinthesiaTheme.colors
    val typography = SkinthesiaTheme.typography
    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(SkinthesiaTheme.shapes.extraLarge)
                .background(colors.surfaceElevated)
                .border(1.dp, colors.border, SkinthesiaTheme.shapes.extraLarge)
                .padding(24.dp),
        ) {
            Text(text = title, style = typography.titleMedium, color = colors.textPrimary)
            Spacer(Modifier.height(10.dp))
            Text(text = body, style = typography.body, color = colors.textSecondary)
            Spacer(Modifier.height(24.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                SkinthesiaTextButton(text = dismissLabel, onClick = onDismiss)
                Spacer(Modifier.width(8.dp))
                Box(
                    modifier = Modifier
                        .heightIn(min = 44.dp)
                        .clip(SkinthesiaTheme.shapes.pill)
                        .background(if (destructive) colors.warningStrong else colors.primary)
                        .clickable(role = Role.Button, onClick = onConfirm)
                        .padding(horizontal = 20.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(text = confirmLabel, style = typography.buttonSmall, color = colors.textOnPrimary)
                }
            }
        }
    }
}
