package com.skinthesia.core.navigation

import androidx.compose.foundation.layout.statusBarsPadding
import com.skinthesia.feature.analyze.AnalyzeScreen
import com.skinthesia.feature.home.HomeScreen
import com.skinthesia.feature.learn.LearnScreen
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.skinthesia.core.design.SkinthesiaTheme
import com.skinthesia.core.ui.components.BottomBarItem
import com.skinthesia.core.ui.components.SkinthesiaBottomBar
import com.skinthesia.core.ui.icons.SkinthesiaIcons
import com.skinthesia.feature.shell.TabPlaceholder

private val TAB_ROUTES: List<Any> = listOf(HomeTab, JourneyTab, AnalyzeTab, LearnTab, ProfileTab)

/**
 * The daily experience: Home, Journey, Analyze (raised centre), Learn and Profile.
 * Detail screens are pushed on the root navigator so they cover the bar.
 */
@Composable
fun MainShell(modifier: Modifier = Modifier) {
    val colors = SkinthesiaTheme.colors
    val motion = SkinthesiaTheme.motion
    val tabs = rememberNavController()
    val entry by tabs.currentBackStackEntryAsState()
    val items = remember {
        listOf(
            BottomBarItem("Home", SkinthesiaIcons.Home),
            BottomBarItem("Journey", SkinthesiaIcons.Journey),
            BottomBarItem("Analyze", SkinthesiaIcons.Scan, emphasized = true),
            BottomBarItem("Learn", SkinthesiaIcons.Learn),
            BottomBarItem("Profile", SkinthesiaIcons.User),
        )
    }
    val selected = TAB_ROUTES.indexOfFirst { route -> entry?.destination?.hasRoute(route::class) == true }.coerceAtLeast(0)
    val selectTab: (Int) -> Unit = { index ->
        tabs.navigate(TAB_ROUTES[index]) {
            popUpTo(tabs.graph.findStartDestination().id) { saveState = true }
            launchSingleTop = true
            restoreState = true
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(colors.background)
            .statusBarsPadding(),
    ) {
        NavHost(
            navController = tabs,
            startDestination = HomeTab,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            enterTransition = { fadeIn(tween(motion.duration(motion.base))) },
            exitTransition = { fadeOut(tween(motion.duration(motion.fast))) },
            popEnterTransition = { fadeIn(tween(motion.duration(motion.base))) },
            popExitTransition = { fadeOut(tween(motion.duration(motion.fast))) },
        ) {
            composable<HomeTab> { HomeScreen(onSelectTab = selectTab) }
            composable<JourneyTab> { TabPlaceholder("Journey") }
            composable<AnalyzeTab> { AnalyzeScreen() }
            composable<LearnTab> { LearnScreen() }
            composable<ProfileTab> { TabPlaceholder("Profile") }
        }
        SkinthesiaBottomBar(
            items = items,
            selectedIndex = selected,
            onSelect = { index -> if (index != selected) selectTab(index) },
        )
    }
}
