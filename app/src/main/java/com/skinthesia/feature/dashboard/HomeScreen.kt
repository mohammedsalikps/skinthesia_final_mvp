package com.skinthesia.feature.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.skinthesia.R
import com.skinthesia.core.design.SkinthesiaTheme
import com.skinthesia.core.ui.ContentMaxWidth
import com.skinthesia.core.ui.components.BrandMonogram
import com.skinthesia.core.ui.components.FadeInUp
import com.skinthesia.core.ui.components.SectionOverline
import com.skinthesia.core.ui.components.SkinthesiaCard
import com.skinthesia.core.ui.components.SkinthesiaDivider
import com.skinthesia.core.ui.components.SkinthesiaImage
import com.skinthesia.core.ui.components.SkinthesiaSecondaryButton
import com.skinthesia.core.ui.icons.SkinthesiaIcons
import com.skinthesia.core.ui.labelRes
import com.skinthesia.domain.model.ImageSource
import com.skinthesia.domain.model.SkinProfile
import com.skinthesia.domain.model.UserProfile
import java.time.LocalTime

/**
 * Screen 14 (Phase 1 state): a personalised summary of everything gathered
 * during onboarding, and a clear view of what comes next.
 */
@Composable
fun HomeScreen(
    profile: UserProfile?,
    onEditAnswers: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = SkinthesiaTheme.colors
    val typography = SkinthesiaTheme.typography
    val spacing = SkinthesiaTheme.spacing
    val motion = SkinthesiaTheme.motion
    val skin = profile?.skinProfile ?: SkinProfile.Empty
    val greeting = rememberGreeting()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(colors.background)
            .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal))
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = spacing.screenHorizontal),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = ContentMaxWidth)
                .fillMaxWidth(),
        ) {
            Spacer(Modifier.height(spacing.xs))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                contentAlignment = Alignment.Center,
            ) {
                BrandMonogram(size = 28.dp)
            }
            Spacer(Modifier.height(spacing.lg))

            FadeInUp {
                Column {
                    Text(text = greeting, style = typography.title, color = colors.textPrimary)
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = stringResource(R.string.home_journey_started),
                        style = typography.subtitle,
                        color = colors.textSecondary,
                    )
                }
            }
            Spacer(Modifier.height(spacing.lg))

            FadeInUp(delayMillis = motion.stagger(1)) { ProfileCard(skin) }
            Spacer(Modifier.height(spacing.md))
            FadeInUp(delayMillis = motion.stagger(2)) { GoalsCard(skin) }
            Spacer(Modifier.height(spacing.md))
            FadeInUp(delayMillis = motion.stagger(3)) { FactorsCard(skin) }
            Spacer(Modifier.height(spacing.md))
            FadeInUp(delayMillis = motion.stagger(4)) { NextStepsCard() }
            Spacer(Modifier.height(spacing.lg))
            FadeInUp(delayMillis = motion.stagger(5)) {
                SkinthesiaSecondaryButton(
                    text = stringResource(R.string.home_edit_answers),
                    onClick = onEditAnswers,
                    leadingIcon = SkinthesiaIcons.Edit,
                )
            }
            Spacer(Modifier.height(spacing.lg))
        }
    }
}

@Composable
private fun rememberGreeting(): String {
    val hour = remember { LocalTime.now().hour }
    return stringResource(
        when {
            hour < 12 -> R.string.home_greeting_morning
            hour < 18 -> R.string.home_greeting_afternoon
            else -> R.string.home_greeting_evening
        },
    )
}

@Composable
private fun ProfileCard(skin: SkinProfile) {
    val colors = SkinthesiaTheme.colors
    val spacing = SkinthesiaTheme.spacing
    val notProvided = stringResource(R.string.home_not_provided)
    SkinthesiaCard {
        Row(verticalAlignment = Alignment.Top) {
            Box(
                modifier = Modifier
                    .size(76.dp)
                    .clip(SkinthesiaTheme.shapes.medium)
                    .background(colors.surfaceMuted)
                    .border(spacing.borderThin, colors.border, SkinthesiaTheme.shapes.medium),
                contentAlignment = Alignment.Center,
            ) {
                val photo = skin.baselinePhoto
                if (photo != null) {
                    SkinthesiaImage(
                        source = ImageSource.LocalFile(photo.filePath),
                        contentDescription = stringResource(R.string.home_baseline_photo_cd),
                        modifier = Modifier.fillMaxSize(),
                        maxDimension = 400,
                    )
                } else {
                    BrandMonogram(size = 30.dp, tint = colors.textMuted)
                }
            }
            Spacer(Modifier.width(spacing.md))
            Column(modifier = Modifier.weight(1f)) {
                SectionOverline(text = stringResource(R.string.home_profile_overline))
                Spacer(Modifier.height(spacing.xs))
                DetailLine(
                    label = stringResource(R.string.home_age_label),
                    value = skin.ageRange?.let { stringResource(it.labelRes) } ?: notProvided,
                )
                DetailLine(
                    label = stringResource(R.string.home_skin_type_label),
                    value = skin.skinType?.let { stringResource(it.labelRes) } ?: notProvided,
                )
                DetailLine(
                    label = stringResource(R.string.home_concerns_label),
                    value = if (skin.concerns.isEmpty()) {
                        stringResource(R.string.home_none_selected)
                    } else {
                        skin.concerns.map { stringResource(it.labelRes) }.joinToString(", ")
                    },
                )
                DetailLine(
                    label = stringResource(R.string.home_baseline_label),
                    value = if (skin.baselinePhoto != null) {
                        stringResource(R.string.home_baseline_captured)
                    } else {
                        notProvided
                    },
                )
            }
        }
    }
}

@Composable
private fun DetailLine(label: String, value: String) {
    val colors = SkinthesiaTheme.colors
    val typography = SkinthesiaTheme.typography
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Text(
            text = label,
            style = typography.caption,
            color = colors.textMuted,
            modifier = Modifier.width(88.dp),
        )
        Text(
            text = value,
            style = typography.bodySmall,
            color = colors.textPrimary,
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun GoalsCard(skin: SkinProfile) {
    val colors = SkinthesiaTheme.colors
    val typography = SkinthesiaTheme.typography
    val spacing = SkinthesiaTheme.spacing
    SkinthesiaCard {
        SectionOverline(text = stringResource(R.string.home_goals_overline))
        Spacer(Modifier.height(spacing.sm))
        if (skin.goals.isEmpty()) {
            Text(
                text = stringResource(R.string.home_none_selected),
                style = typography.bodySmall,
                color = colors.textMuted,
            )
        } else {
            ChipRow(labels = skin.goals.map { stringResource(it.labelRes) })
        }
        val target = skin.target
        if (target != null && target.phrase.isNotBlank()) {
            Spacer(Modifier.height(spacing.md))
            SkinthesiaDivider()
            Spacer(Modifier.height(spacing.md))
            SectionOverline(text = stringResource(R.string.home_target_overline))
            Spacer(Modifier.height(spacing.xs))
            Text(
                text = "“${target.phrase}”",
                style = typography.displayItalic.copy(fontSize = 20.sp, lineHeight = 28.sp),
                color = colors.primary,
            )
            if (target.goals.isNotEmpty()) {
                Spacer(Modifier.height(spacing.sm))
                ChipRow(labels = target.goals.map { stringResource(it.labelRes) })
            }
        }
    }
}

@Composable
private fun FactorsCard(skin: SkinProfile) {
    val colors = SkinthesiaTheme.colors
    val typography = SkinthesiaTheme.typography
    val spacing = SkinthesiaTheme.spacing
    SkinthesiaCard {
        SectionOverline(text = stringResource(R.string.home_factors_overline))
        Spacer(Modifier.height(spacing.sm))
        if (skin.lifestyleFactors.isEmpty()) {
            Text(
                text = stringResource(R.string.home_none_selected),
                style = typography.bodySmall,
                color = colors.textMuted,
            )
        } else {
            ChipRow(labels = skin.lifestyleFactors.map { stringResource(it.labelRes) })
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ChipRow(labels: List<String>) {
    val colors = SkinthesiaTheme.colors
    val typography = SkinthesiaTheme.typography
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        labels.forEach { label ->
            Box(
                modifier = Modifier
                    .clip(SkinthesiaTheme.shapes.pill)
                    .background(colors.surfaceMuted)
                    .padding(horizontal = 12.dp, vertical = 7.dp),
            ) {
                Text(text = label, style = typography.labelSmall, color = colors.textSecondary)
            }
        }
    }
}

@Composable
private fun NextStepsCard() {
    val spacing = SkinthesiaTheme.spacing
    SkinthesiaCard {
        SectionOverline(text = stringResource(R.string.home_next_overline))
        Spacer(Modifier.height(spacing.md))
        NextStep(
            icon = SkinthesiaIcons.Scan,
            title = stringResource(R.string.home_next_measure_title),
            body = stringResource(R.string.home_next_measure_body),
            current = true,
            last = false,
        )
        NextStep(
            icon = SkinthesiaIcons.SkinPrint,
            title = stringResource(R.string.home_next_skinprint_title),
            body = stringResource(R.string.home_next_skinprint_body),
            current = false,
            last = false,
        )
        NextStep(
            icon = SkinthesiaIcons.Products,
            title = stringResource(R.string.home_next_protocol_title),
            body = stringResource(R.string.home_next_protocol_body),
            current = false,
            last = true,
        )
    }
}

@Composable
private fun NextStep(
    icon: ImageVector,
    title: String,
    body: String,
    current: Boolean,
    last: Boolean,
) {
    val colors = SkinthesiaTheme.colors
    val typography = SkinthesiaTheme.typography
    val spacing = SkinthesiaTheme.spacing
    Row(verticalAlignment = Alignment.Top) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(SkinthesiaTheme.shapes.pill)
                    .background(if (current) colors.primary else colors.surfaceMuted)
                    .border(
                        spacing.borderThin,
                        if (current) colors.primary else colors.border,
                        SkinthesiaTheme.shapes.pill,
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (current) colors.textOnPrimary else colors.textMuted,
                    modifier = Modifier.size(18.dp),
                )
            }
            if (!last) {
                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .height(34.dp)
                        .background(colors.border),
                )
            }
        }
        Spacer(Modifier.width(spacing.md))
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(bottom = if (last) 0.dp else spacing.sm),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = title, style = typography.label, color = colors.textPrimary)
                if (current) {
                    Spacer(Modifier.width(spacing.xs))
                    Box(
                        modifier = Modifier
                            .clip(SkinthesiaTheme.shapes.pill)
                            .background(colors.primaryMist)
                            .padding(horizontal = 8.dp, vertical = 3.dp),
                    ) {
                        Text(
                            text = stringResource(R.string.home_you_are_here).uppercase(),
                            style = typography.overline,
                            color = colors.primary,
                        )
                    }
                }
            }
            Spacer(Modifier.height(2.dp))
            Text(text = body, style = typography.bodySmall, color = colors.textSecondary)
        }
    }
}
