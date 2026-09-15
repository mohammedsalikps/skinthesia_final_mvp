package com.skinthesia.core.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.skinthesia.core.design.SkinthesiaTheme
import com.skinthesia.domain.model.ScoreBand
import com.skinthesia.domain.model.SkinPrintDimension
import com.skinthesia.domain.model.SkinScore

/**
 * Skinthesia's Home hero: the overall SkinPrint as a single premium progress
 * ring (score, unit and status at its centre), with each of the five real
 * dimensions available as a tappable chip beneath it. Chosen over the radar
 * silhouette used on the SkinPrint detail screen because a dashboard's job is
 * to answer "what's my status?" at a glance - a ring reads that instantly,
 * while the shape comparison the radar gives is more useful once someone has
 * already decided to dig in.
 */
@Composable
fun SkinPrintScoreCircle(
    scores: List<SkinScore>,
    overall: Int,
    band: ScoreBand,
    modifier: Modifier = Modifier,
    size: Dp = 208.dp,
    previous: Map<SkinPrintDimension, Int>? = null,
) {
    val colors = SkinthesiaTheme.colors
    val typography = SkinthesiaTheme.typography
    var selected by remember { mutableStateOf<SkinPrintDimension?>(null) }

    val dimensions = SkinPrintDimension.entries
    val ordered = remember(scores) { dimensions.mapNotNull { d -> scores.firstOrNull { it.dimension == d } } }

    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        ScoreRing(
            score = overall,
            size = size,
            strokeWidth = 12.dp,
            centerContent = {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = overall.toString(), style = typography.scoreNumeral, color = colors.textPrimary)
                    Text(text = "SKINPRINT", style = typography.overline, color = colors.textMuted)
                    Spacer(Modifier.height(8.dp))
                    Tag(text = band.label, tone = TagTone.SAGE)
                }
            },
        )
        Spacer(Modifier.height(16.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .semantics {
                    contentDescription = "SkinPrint by dimension: " +
                        ordered.joinToString { "${it.dimension.label} ${it.value}" }
                },
            horizontalArrangement = Arrangement.SpaceEvenly,
        ) {
            dimensions.forEachIndexed { i, dimension ->
                val score = ordered.getOrNull(i)
                DimensionChip(
                    dimension = dimension,
                    value = score?.value,
                    isSelected = selected == dimension,
                    onClick = { selected = if (selected == dimension) null else dimension },
                )
            }
        }

        AnimatedVisibility(
            visible = selected != null,
            enter = fadeIn(tween(220)) + expandVertically(tween(220, easing = FastOutSlowInEasing)),
            exit = fadeOut(tween(140)) + shrinkVertically(tween(140)),
        ) {
            val dimension = selected
            val score = dimension?.let { d -> ordered.firstOrNull { it.dimension == d } }
            if (dimension != null && score != null) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(top = 14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = dimension.label, style = typography.titleSmall, color = colors.textPrimary, textAlign = TextAlign.Center)
                        Spacer(Modifier.width(8.dp))
                        Text(text = score.value.toString(), style = typography.metric, color = colors.primary)
                        val prev = previous?.get(dimension)
                        if (prev != null && prev != score.value) {
                            Spacer(Modifier.width(6.dp))
                            val delta = (score.value - prev).toDouble()
                            DeltaBadge(delta = delta, improved = delta > 0)
                        }
                    }
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = score.explanation,
                        style = typography.bodySmall,
                        color = colors.textSecondary,
                        textAlign = TextAlign.Center,
                    )
                }
            }
        }
    }
}

@Composable
private fun DimensionChip(
    dimension: SkinPrintDimension,
    value: Int?,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    val colors = SkinthesiaTheme.colors
    val typography = SkinthesiaTheme.typography
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(if (isSelected) 36.dp else 32.dp)
                .clip(CircleShape)
                .background(if (isSelected) colors.primary else colors.surfaceMuted)
                .border(if (isSelected) 0.dp else SkinthesiaTheme.spacing.borderThin, colors.border, CircleShape)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    role = Role.Button,
                    onClick = onClick,
                ),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                dimension.icon(),
                contentDescription = dimension.label,
                tint = if (isSelected) colors.textOnPrimary else colors.textSecondary,
                modifier = Modifier.size(if (isSelected) 17.dp else 15.dp),
            )
        }
        if (value != null) {
            Spacer(Modifier.height(4.dp))
            Text(text = value.toString(), style = typography.labelSmall, color = if (isSelected) colors.textPrimary else colors.textMuted)
        }
    }
}
