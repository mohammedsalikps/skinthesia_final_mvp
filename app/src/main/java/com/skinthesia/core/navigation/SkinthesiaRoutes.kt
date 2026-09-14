package com.skinthesia.core.navigation

import androidx.annotation.StringRes
import androidx.compose.ui.graphics.vector.ImageVector
import com.skinthesia.R
import com.skinthesia.core.ui.icons.SkinthesiaIcons

/** Forward/back onboarding journey: Discover and Understand. */
object OnboardingRoutes {
    const val GRAPH = "onboarding"
    const val WELCOME = "onboarding/welcome"
    const val PHOTO = "onboarding/photo"
    const val GOALS = "onboarding/goals"
    const val TARGET = "onboarding/target"
    const val QUESTIONNAIRE = "onboarding/questionnaire"
    const val LIFESTYLE = "onboarding/lifestyle"

    /** Steps shown by the progress indicator (Welcome is step 1). */
    const val TOTAL_STEPS = 6
}

/** The post-onboarding shell with persistent bottom navigation. */
object MainRoutes {
    const val ROOT = "main"
}

/** Bottom navigation tabs, in display order. */
enum class MainTab(
    val route: String,
    @param:StringRes val labelRes: Int,
) {
    HOME("main/home", R.string.nav_home),
    SKINPRINT("main/skinprint", R.string.nav_skinprint),
    ACT("main/act", R.string.nav_act),
    LEARN("main/learn", R.string.nav_learn),
    COMMUNITY("main/community", R.string.nav_community);

    val icon: ImageVector
        get() = when (this) {
            HOME -> SkinthesiaIcons.Home
            SKINPRINT -> SkinthesiaIcons.SkinPrint
            ACT -> SkinthesiaIcons.Scan
            LEARN -> SkinthesiaIcons.Learn
            COMMUNITY -> SkinthesiaIcons.Community
        }

    /** The centre tab is drawn as a raised circle, as in the reference. */
    val isEmphasized: Boolean
        get() = this == ACT

    companion object {
        fun fromRoute(route: String?): MainTab? = entries.firstOrNull { it.route == route }
    }
}
