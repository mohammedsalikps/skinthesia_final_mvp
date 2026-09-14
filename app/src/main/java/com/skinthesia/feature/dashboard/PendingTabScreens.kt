package com.skinthesia.feature.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.skinthesia.R
import com.skinthesia.core.design.SkinthesiaTheme
import com.skinthesia.core.navigation.MainTab
import com.skinthesia.core.ui.components.BrandMonogram
import com.skinthesia.core.ui.components.FadeInUp
import com.skinthesia.core.ui.components.SectionOverline
import com.skinthesia.core.ui.components.SkinthesiaCard
import com.skinthesia.core.ui.icons.SkinthesiaIcons

/**
 * Pre-measurement state for the SkinPrint, Act, Learn and Community tabs.
 * Each explains what the tab will hold once the first DermoScan is complete.
 */
@Composable
fun PendingTabScreen(
    tab: MainTab,
    modifier: Modifier = Modifier,
) {
    val colors = SkinthesiaTheme.colors
    val typography = SkinthesiaTheme.typography
    val spacing = SkinthesiaTheme.spacing
    val motion = SkinthesiaTheme.motion

    val titleRes = when (tab) {
        MainTab.ACT -> R.string.tab_act_title
        MainTab.LEARN -> R.string.tab_learn_title
        MainTab.COMMUNITY -> R.string.tab_community_title
        MainTab.SKINPRINT, MainTab.HOME -> R.string.tab_skinprint_title
    }
    val bodyRes = when (tab) {
        MainTab.ACT -> R.string.tab_act_body
        MainTab.LEARN -> R.string.tab_learn_body
        MainTab.COMMUNITY -> R.string.tab_community_body
        MainTab.SKINPRINT, MainTab.HOME -> R.string.tab_skinprint_body
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(colors.background)
            .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal))
            .statusBarsPadding()
            .padding(horizontal = spacing.screenHorizontal),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 420.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            FadeInUp { BrandMonogram(size = 44.dp) }
            Spacer(Modifier.height(spacing.lg))
            FadeInUp(delayMillis = motion.stagger(1)) {
                Text(
                    text = stringResource(titleRes),
                    style = typography.title,
                    color = colors.textPrimary,
                    textAlign = TextAlign.Center,
                )
            }
            Spacer(Modifier.height(spacing.sm))
            FadeInUp(delayMillis = motion.stagger(2)) {
                Text(
                    text = stringResource(bodyRes),
                    style = typography.body,
                    color = colors.textSecondary,
                    textAlign = TextAlign.Center,
                )
            }
            Spacer(Modifier.height(spacing.xl))
            FadeInUp(delayMillis = motion.stagger(3)) {
                SkinthesiaCard {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(SkinthesiaTheme.shapes.pill)
                                .background(colors.primaryMist),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                imageVector = SkinthesiaIcons.Scan,
                                contentDescription = null,
                                tint = colors.primary,
                                modifier = Modifier.size(20.dp),
                            )
                        }
                        Spacer(Modifier.width(spacing.md))
                        Column {
                            SectionOverline(text = stringResource(R.string.tab_next_step_overline))
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = stringResource(R.string.tab_next_step_measure),
                                style = typography.label,
                                color = colors.textPrimary,
                            )
                        }
                    }
                }
            }
        }
    }
}
