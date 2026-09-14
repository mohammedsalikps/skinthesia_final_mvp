package com.skinthesia.core.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.skinthesia.LocalAppContainer
import com.skinthesia.core.design.SkinthesiaTheme
import com.skinthesia.core.ui.components.SkinthesiaBottomBar
import com.skinthesia.feature.dashboard.HomeScreen
import com.skinthesia.feature.dashboard.HomeViewModel
import com.skinthesia.feature.dashboard.PendingTabScreen

/** Post-onboarding shell: tab content above the persistent bottom navigation. */
@Composable
fun MainShell(
    onEditAnswers: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = SkinthesiaTheme.colors
    val motion = SkinthesiaTheme.motion
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentTab = MainTab.fromRoute(backStackEntry?.destination?.route) ?: MainTab.HOME

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(colors.background),
    ) {
        NavHost(
            navController = navController,
            startDestination = MainTab.HOME.route,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            enterTransition = { fadeIn(tween(motion.duration(motion.base))) },
            exitTransition = { fadeOut(tween(motion.duration(motion.fast))) },
            popEnterTransition = { fadeIn(tween(motion.duration(motion.base))) },
            popExitTransition = { fadeOut(tween(motion.duration(motion.fast))) },
        ) {
            composable(MainTab.HOME.route) {
                HomeRoute(onEditAnswers = onEditAnswers)
            }
            MainTab.entries.filter { it != MainTab.HOME }.forEach { tab ->
                composable(tab.route) {
                    PendingTabScreen(tab = tab)
                }
            }
        }
        SkinthesiaBottomBar(
            selected = currentTab,
            onSelect = { tab ->
                if (tab != currentTab) {
                    navController.navigate(tab.route) {
                        popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            },
        )
    }
}

@Composable
private fun HomeRoute(onEditAnswers: () -> Unit) {
    val container = LocalAppContainer.current
    val viewModel: HomeViewModel = viewModel(factory = container.viewModelFactory)
    val profile by viewModel.profile.collectAsStateWithLifecycle()
    HomeScreen(profile = profile, onEditAnswers = onEditAnswers)
}
