package com.skinthesia.core.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.skinthesia.core.design.SkinthesiaPalette
import com.skinthesia.core.design.SkinthesiaTheme

/** One destination in the bottom bar. */
data class BottomBarItem(
    val label: String,
    val icon: ImageVector,
    /** Drawn as the raised clay circle in the centre, like the reference's scan button. */
    val emphasized: Boolean = false,
)

/** Persistent five-slot navigation bar with a quiet dot for the active tab. */
@Composable
fun SkinthesiaBottomBar(
    items: List<BottomBarItem>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = SkinthesiaTheme.colors
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(colors.surface),
    ) {
        SkinthesiaDivider()
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .height(68.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            items.forEachIndexed { index, item ->
                BottomBarSlot(
                    item = item,
                    selected = index == selectedIndex,
                    onClick = { onSelect(index) },
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                )
            }
        }
    }
}

@Composable
private fun BottomBarSlot(
    item: BottomBarItem,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = SkinthesiaTheme.colors
    val motion = SkinthesiaTheme.motion
    val interaction = remember { MutableInteractionSource() }
    val tint by animateColorAsState(
        targetValue = if (selected) colors.primary else colors.textMuted,
        animationSpec = tween(motion.duration(motion.base)),
        label = "tabTint",
    )
    val dot by animateColorAsState(
        targetValue = if (selected) colors.primary else Color.Transparent,
        animationSpec = tween(motion.duration(motion.base)),
        label = "tabDot",
    )
    Column(
        modifier = modifier
            .pressScale(interaction, pressedScale = 0.94f)
            .selectable(
                selected = selected,
                interactionSource = interaction,
                indication = null,
                role = Role.Tab,
                onClick = onClick,
            ),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        if (item.emphasized) {
            val container by animateColorAsState(
                targetValue = if (selected) colors.primary else colors.primaryMist,
                animationSpec = tween(motion.duration(motion.base)),
                label = "centerTab",
            )
            Box(
                modifier = Modifier
                    .shadow(
                        elevation = 6.dp,
                        shape = CircleShape,
                        ambientColor = SkinthesiaPalette.Cocoa.copy(alpha = 0.22f),
                        spotColor = SkinthesiaPalette.Cocoa.copy(alpha = 0.22f),
                    )
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(container)
                    .border(1.dp, if (selected) colors.primary else colors.accentBlushDeep, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Icon(item.icon, contentDescription = null, tint = if (selected) colors.textOnPrimary else colors.primary, modifier = Modifier.size(20.dp))
            }
        } else {
            Icon(item.icon, contentDescription = null, tint = tint, modifier = Modifier.size(22.dp))
        }
        Spacer(Modifier.height(4.dp))
        Text(text = item.label, style = SkinthesiaTheme.typography.navigation, color = tint, maxLines = 1)
        Spacer(Modifier.height(3.dp))
        Box(
            modifier = Modifier
                .size(4.dp)
                .clip(CircleShape)
                .background(dot),
        )
    }
}
