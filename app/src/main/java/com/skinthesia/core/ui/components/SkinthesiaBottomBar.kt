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
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.skinthesia.core.design.SkinthesiaTheme
import com.skinthesia.core.navigation.MainTab

/** Persistent five-tab bar: Home, SkinPrint, Act (raised), Learn, Community. */
@Composable
fun SkinthesiaBottomBar(
    selected: MainTab,
    onSelect: (MainTab) -> Unit,
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
                .height(66.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            MainTab.entries.forEach { tab ->
                BottomBarItem(
                    tab = tab,
                    selected = tab == selected,
                    onClick = { onSelect(tab) },
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                )
            }
        }
    }
}

@Composable
private fun BottomBarItem(
    tab: MainTab,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = SkinthesiaTheme.colors
    val typography = SkinthesiaTheme.typography
    val motion = SkinthesiaTheme.motion
    val interaction = remember { MutableInteractionSource() }
    val label = stringResource(tab.labelRes)
    val tint by animateColorAsState(
        targetValue = if (selected) colors.primary else colors.textMuted,
        animationSpec = tween(motion.duration(motion.base)),
        label = "tabTint",
    )
    val indicator by animateColorAsState(
        targetValue = if (selected) colors.primary else Color.Transparent,
        animationSpec = tween(motion.duration(motion.base)),
        label = "tabIndicator",
    )

    Column(
        modifier = modifier.selectable(
            selected = selected,
            interactionSource = interaction,
            indication = null,
            role = Role.Tab,
            onClick = onClick,
        ),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        if (tab.isEmphasized) {
            val container by animateColorAsState(
                targetValue = if (selected) colors.primary else colors.primaryMist,
                animationSpec = tween(motion.duration(motion.base)),
                label = "emphasizedTab",
            )
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(SkinthesiaTheme.shapes.pill)
                    .background(container)
                    .border(1.dp, if (selected) colors.primary else colors.accentBlushDeep, SkinthesiaTheme.shapes.pill),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = tab.icon,
                    contentDescription = null,
                    tint = if (selected) colors.textOnPrimary else colors.primary,
                    modifier = Modifier.size(20.dp),
                )
            }
        } else {
            Icon(
                imageVector = tab.icon,
                contentDescription = null,
                tint = tint,
                modifier = Modifier.size(22.dp),
            )
        }
        Spacer(Modifier.height(4.dp))
        Text(
            text = label,
            style = typography.navigation,
            color = tint,
            maxLines = 1,
        )
        Spacer(Modifier.height(3.dp))
        Box(
            modifier = Modifier
                .size(4.dp)
                .clip(SkinthesiaTheme.shapes.pill)
                .background(indicator),
        )
    }
}
