package com.skinthesia.feature.plan

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.skinthesia.core.design.SkinthesiaTheme
import com.skinthesia.core.ui.art.ProductArtwork
import com.skinthesia.core.ui.components.AttentionPill
import com.skinthesia.core.ui.components.MiniRing
import com.skinthesia.core.ui.components.SectionOverline
import com.skinthesia.core.ui.components.SkinthesiaCard
import com.skinthesia.core.ui.components.SkinthesiaPillButton
import com.skinthesia.core.ui.components.SourceBadge
import com.skinthesia.core.ui.components.Tag
import com.skinthesia.core.ui.components.TagTone
import com.skinthesia.core.ui.formatPrice
import com.skinthesia.core.ui.icons.SkinthesiaIcons
import com.skinthesia.domain.model.AttentionLevel
import com.skinthesia.domain.model.FocusArea
import com.skinthesia.domain.model.ImprovementArea
import com.skinthesia.domain.model.Insight
import com.skinthesia.domain.model.InsightKind
import com.skinthesia.domain.model.LifestyleSuggestion
import com.skinthesia.domain.model.LifestyleTopic
import com.skinthesia.domain.model.Product
import com.skinthesia.domain.model.ProductRecommendation
import com.skinthesia.domain.model.ReasonKind
import com.skinthesia.domain.model.RecommendationReason
import com.skinthesia.domain.model.RoutineStepType
import com.skinthesia.domain.model.RoutineTime
import com.skinthesia.feature.analysis.IconBadge
import com.skinthesia.feature.analysis.LevelBar

val ImprovementArea.icon: ImageVector
    get() = when (this) {
        ImprovementArea.HYDRATION -> SkinthesiaIcons.Droplet
        ImprovementArea.UNEVEN_TONE -> SkinthesiaIcons.EvenTone
        ImprovementArea.DARK_SPOTS -> SkinthesiaIcons.DarkSpots
        ImprovementArea.TEXTURE -> SkinthesiaIcons.Texture
        ImprovementArea.DARK_CIRCLES -> SkinthesiaIcons.UnderEye
        ImprovementArea.BLEMISHES -> SkinthesiaIcons.Breakouts
        ImprovementArea.REDNESS -> SkinthesiaIcons.Calm
        ImprovementArea.PORES -> SkinthesiaIcons.Pores
        ImprovementArea.FINE_LINES -> SkinthesiaIcons.FineLines
    }

val RoutineStepType.icon: ImageVector
    get() = when (this) {
        RoutineStepType.CLEANSE -> SkinthesiaIcons.Droplet
        RoutineStepType.TREAT -> SkinthesiaIcons.Flask
        RoutineStepType.MOISTURIZE -> SkinthesiaIcons.Layers
        RoutineStepType.PROTECT -> SkinthesiaIcons.Shield
    }

val RoutineTime.icon: ImageVector
    get() = if (this == RoutineTime.MORNING) SkinthesiaIcons.Sunrise else SkinthesiaIcons.Moon

val ReasonKind.icon: ImageVector
    get() = when (this) {
        ReasonKind.GOAL -> SkinthesiaIcons.Target
        ReasonKind.SKIN_TYPE -> SkinthesiaIcons.Face
        ReasonKind.MEASUREMENT -> SkinthesiaIcons.Probe
        ReasonKind.VISUAL -> SkinthesiaIcons.Camera
        ReasonKind.PREFERENCE -> SkinthesiaIcons.Heart
        ReasonKind.BUDGET -> SkinthesiaIcons.Bag
        ReasonKind.ROUTINE -> SkinthesiaIcons.Clock
    }

val LifestyleTopic.icon: ImageVector
    get() = when (this) {
        LifestyleTopic.SLEEP -> SkinthesiaIcons.Moon
        LifestyleTopic.STRESS -> SkinthesiaIcons.Stress
        LifestyleTopic.SUN -> SkinthesiaIcons.Sun
        LifestyleTopic.WATER -> SkinthesiaIcons.Droplet
        LifestyleTopic.NOURISHMENT -> SkinthesiaIcons.Diet
        LifestyleTopic.MOVEMENT -> SkinthesiaIcons.Movement
        LifestyleTopic.ENVIRONMENT -> SkinthesiaIcons.Climate
        LifestyleTopic.SCREENS -> SkinthesiaIcons.Screen
    }

val InsightKind.icon: ImageVector
    get() = when (this) {
        InsightKind.SENSOR -> SkinthesiaIcons.Probe
        InsightKind.VISUAL -> SkinthesiaIcons.Camera
        InsightKind.COMBINED -> SkinthesiaIcons.Sparkle
        InsightKind.CONTEXT -> SkinthesiaIcons.Leaf
        InsightKind.PROGRESS -> SkinthesiaIcons.Trend
    }

/** Calm colour for an attention level, used on bars and the face map. */
@Composable
fun attentionColor(level: AttentionLevel): Color {
    val colors = SkinthesiaTheme.colors
    return when (level) {
        AttentionLevel.GOOD -> colors.success
        AttentionLevel.MILD -> colors.info
        AttentionLevel.MODERATE -> colors.gold
        AttentionLevel.NEEDS_ATTENTION -> colors.accentBlushDeep
    }
}

@Composable
fun ProductThumb(product: Product, modifier: Modifier = Modifier, size: Dp = 64.dp) {
    ProductArtwork(
        form = product.form,
        tone = product.tone,
        modifier = modifier.size(size).clip(SkinthesiaTheme.shapes.tile),
        mark = product.brand.take(1).uppercase(),
        contentDescription = null,
    )
}

/** One focus area: what it is, why it was flagged, and how it scores. */
@Composable
fun FocusAreaRow(
    focus: FocusArea,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    showBar: Boolean = false,
    delayMillis: Int = 0,
) {
    val colors = SkinthesiaTheme.colors
    val typography = SkinthesiaTheme.typography
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(SkinthesiaTheme.shapes.field)
            .clickable(onClickLabel = "Learn about " + focus.area.label.lowercase(), onClick = onClick)
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.Top,
    ) {
        IconBadge(focus.area.icon)
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = focus.area.label, style = typography.labelLarge, color = colors.textPrimary)
                if (focus.isUserGoal) {
                    Spacer(Modifier.width(8.dp))
                    Tag(text = "Your goal", tone = TagTone.CLAY)
                }
            }
            Spacer(Modifier.height(4.dp))
            Text(text = focus.summary, style = typography.bodySmall, color = colors.textSecondary, maxLines = 3, overflow = TextOverflow.Ellipsis)
            if (showBar) {
                Spacer(Modifier.height(10.dp))
                LevelBar(fraction = focus.score / 100f, color = attentionColor(focus.level), delayMillis = delayMillis)
            }
            Spacer(Modifier.height(8.dp))
            AttentionPill(focus.level)
        }
        Spacer(Modifier.width(12.dp))
        Column(horizontalAlignment = Alignment.End) {
            Text(text = focus.score.toString(), style = typography.metric, color = colors.textPrimary)
            Spacer(Modifier.height(4.dp))
            Icon(SkinthesiaIcons.ChevronRight, contentDescription = null, tint = colors.textMuted, modifier = Modifier.size(16.dp))
        }
    }
}

@Composable
fun ReasonLine(reason: RecommendationReason, modifier: Modifier = Modifier) {
    val colors = SkinthesiaTheme.colors
    Row(modifier.padding(vertical = 3.dp), verticalAlignment = Alignment.Top) {
        Icon(reason.kind.icon, contentDescription = null, tint = colors.primary, modifier = Modifier.padding(top = 2.dp).size(14.dp))
        Spacer(Modifier.width(8.dp))
        Text(text = reason.text, style = SkinthesiaTheme.typography.bodySmall, color = colors.textSecondary)
    }
}

/** An explainable product suggestion: fit, reasons, and an optional add-to-bag action. */
@Composable
fun RecommendationCard(
    recommendation: ProductRecommendation,
    product: Product,
    onOpen: () -> Unit,
    modifier: Modifier = Modifier,
    inBag: Boolean = false,
    onAdd: (() -> Unit)? = null,
    maxReasons: Int = 3,
) {
    val colors = SkinthesiaTheme.colors
    val typography = SkinthesiaTheme.typography
    SkinthesiaCard(modifier = modifier, onClick = onOpen) {
        Row(verticalAlignment = Alignment.Top) {
            ProductThumb(product, size = 84.dp)
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text(text = product.brand, style = typography.caption, color = colors.textMuted)
                Spacer(Modifier.height(2.dp))
                Text(text = product.name, style = typography.labelLarge, color = colors.textPrimary, maxLines = 2, overflow = TextOverflow.Ellipsis)
                Spacer(Modifier.height(4.dp))
                Text(text = product.category.label + " · " + product.size, style = typography.caption, color = colors.textMuted)
                Spacer(Modifier.height(6.dp))
                Text(text = formatPrice(product.price), style = typography.label, color = colors.textPrimary)
            }
            Spacer(Modifier.width(8.dp))
            Column(Modifier.semantics(mergeDescendants = true) {}, horizontalAlignment = Alignment.CenterHorizontally) {
                MiniRing(value = recommendation.matchScore, size = 46.dp)
                Spacer(Modifier.height(4.dp))
                Text(text = "match", style = typography.caption, color = colors.textMuted)
            }
        }
        if (recommendation.reasons.isNotEmpty()) {
            Spacer(Modifier.height(12.dp))
            recommendation.reasons.take(maxReasons).forEach { ReasonLine(it) }
        }
        if (recommendation.isPrimary || onAdd != null) {
            Spacer(Modifier.height(10.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (recommendation.isPrimary) Tag(text = "In your routine", tone = TagTone.SAGE, icon = SkinthesiaIcons.Check)
                Spacer(Modifier.weight(1f))
                if (onAdd != null) {
                    SkinthesiaPillButton(text = if (inBag) "In your bag" else "Add to bag", onClick = onAdd, enabled = !inBag)
                }
            }
        }
    }
}

@Composable
fun LifestyleCard(suggestion: LifestyleSuggestion, modifier: Modifier = Modifier) {
    val colors = SkinthesiaTheme.colors
    val typography = SkinthesiaTheme.typography
    SkinthesiaCard(modifier = modifier) {
        Row(verticalAlignment = Alignment.Top) {
            IconBadge(suggestion.topic.icon, container = colors.successSoft, tint = colors.successStrong)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                SectionOverline(text = suggestion.topic.label)
                Spacer(Modifier.height(4.dp))
                Text(text = suggestion.title, style = typography.labelLarge, color = colors.textPrimary)
                Spacer(Modifier.height(4.dp))
                Text(text = suggestion.body, style = typography.bodySmall, color = colors.textSecondary)
                Spacer(Modifier.height(8.dp))
                Text(text = "Based on: " + suggestion.basedOn, style = typography.caption, color = colors.textMuted)
            }
        }
    }
}

/**
 * The Skinthesia AI treatment: a tinted editorial surface rather than a plain
 * bordered card, so an AI-derived observation reads as intelligence being
 * offered, not just another information block. Uses only the insight's own
 * real title/body/sources - the quote styling is presentation, not new data.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun InsightCard(insight: Insight, modifier: Modifier = Modifier) {
    val colors = SkinthesiaTheme.colors
    val typography = SkinthesiaTheme.typography
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(SkinthesiaTheme.shapes.large)
            .background(Brush.linearGradient(listOf(colors.primaryMist, colors.blushMist)))
            .padding(SkinthesiaTheme.spacing.lg),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(SkinthesiaIcons.Sparkle, contentDescription = null, tint = colors.primary, modifier = Modifier.size(13.dp))
            Spacer(Modifier.width(6.dp))
            Text(text = "SKINTHESIA AI", style = typography.overline, color = colors.primary)
        }
        Spacer(Modifier.height(12.dp))
        Text(
            text = "“${insight.title}”",
            style = typography.articleLead,
            color = colors.textPrimary,
        )
        Spacer(Modifier.height(10.dp))
        Text(text = insight.body, style = typography.bodySmall, color = colors.textSecondary)
        if (insight.sources.isNotEmpty()) {
            Spacer(Modifier.height(12.dp))
            FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                insight.sources.forEach { SourceBadge(it) }
            }
        }
    }
}
