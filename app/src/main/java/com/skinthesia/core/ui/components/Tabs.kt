package com.skinthesia.core.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.skinthesia.core.design.SkinthesiaTheme

/**
 * Pill segmented control with a sliding cream indicator, as used for
 * "AM Routine / PM Routine" and "Routine / Products / Lifestyle".
 */
@Composable
fun SegmentedTabs(
    options: List<String>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = SkinthesiaTheme.colors
    val typography = SkinthesiaTheme.typography
    val motion = SkinthesiaTheme.motion
    val shape = SkinthesiaTheme.shapes.pill
    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .height(46.dp)
            .clip(shape)
            .background(colors.surfaceMuted)
            .border(1.dp, colors.border, shape)
            .padding(4.dp),
    ) {
        val segment = maxWidth / options.size.coerceAtLeast(1)
        val offset by animateDpAsState(
            targetValue = segment * selectedIndex,
            animationSpec = tween(motion.duration(motion.base), easing = motion.standardEasing),
            label = "segmentOffset",
        )
        Box(
            modifier = Modifier
                .offset(x = offset)
                .width(segment)
                .fillMaxHeight()
                .clip(shape)
                .background(colors.surfaceElevated)
                .border(1.dp, colors.border, shape),
        )
        Row(Modifier.fillMaxSize()) {
            options.forEachIndexed { index, label ->
                val selected = index == selectedIndex
                val tint by animateColorAsState(
                    targetValue = if (selected) colors.textPrimary else colors.textMuted,
                    animationSpec = tween(motion.duration(motion.base)),
                    label = "segmentTint",
                )
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clip(shape)
                        .selectable(selected = selected, role = Role.Tab, onClick = { onSelect(index) }),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(text = label, style = typography.label, color = tint, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
            }
        }
    }
}

/** A single filter pill; clay when selected, cream with a hairline otherwise. */
@Composable
fun FilterPill(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = SkinthesiaTheme.colors
    val motion = SkinthesiaTheme.motion
    val shape = SkinthesiaTheme.shapes.pill
    val container by animateColorAsState(
        targetValue = if (selected) colors.primary else colors.surface,
        animationSpec = tween(motion.duration(motion.base)),
        label = "pillContainer",
    )
    val content by animateColorAsState(
        targetValue = if (selected) colors.textOnPrimary else colors.textSecondary,
        animationSpec = tween(motion.duration(motion.base)),
        label = "pillContent",
    )
    Box(
        modifier = modifier
            .minimumInteractiveComponentSize()
            .height(38.dp)
            .clip(shape)
            .background(container)
            .border(1.dp, if (selected) colors.primary else colors.border, shape)
            .selectable(selected = selected, role = Role.Tab, onClick = onClick)
            .padding(horizontal = 16.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(text = label, style = SkinthesiaTheme.typography.label, color = content, maxLines = 1)
    }
}

/** Horizontally scrolling row of [FilterPill]s that keeps the selected one in view. */
@Composable
fun ScrollableFilterTabs(
    options: List<String>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(horizontal = SkinthesiaTheme.spacing.screenHorizontal),
) {
    val state = rememberLazyListState()
    LaunchedEffect(selectedIndex) {
        if (selectedIndex >= 0) state.animateScrollToItem(selectedIndex.coerceAtLeast(0))
    }
    LazyRow(
        state = state,
        modifier = modifier.fillMaxWidth(),
        contentPadding = contentPadding,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        itemsIndexed(options) { index, label ->
            FilterPill(label = label, selected = index == selectedIndex, onClick = { onSelect(index) })
        }
    }
}
