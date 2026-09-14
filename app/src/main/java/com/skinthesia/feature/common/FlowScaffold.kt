package com.skinthesia.feature.common

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.runtime.Composable
import com.skinthesia.core.navigation.FlowKind
import com.skinthesia.core.ui.components.OnboardingScaffold
import com.skinthesia.core.ui.components.SkinthesiaScreen
import com.skinthesia.core.ui.components.SkinthesiaTopBar
import com.skinthesia.feature.onboarding.OnboardingSteps

/**
 * Shared frame for flows used in several journeys: during onboarding it shows the
 * step indicator; in a check-in or standalone measurement it shows a titled top bar.
 */
@Composable
fun FlowScaffold(
    flow: FlowKind,
    step: Int,
    title: String,
    onBack: () -> Unit,
    scrollable: Boolean = true,
    bottomBar: (@Composable ColumnScope.() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    if (flow == FlowKind.ONBOARDING) {
        OnboardingScaffold(
            step = step,
            totalSteps = OnboardingSteps.TOTAL,
            onBack = onBack,
            scrollable = scrollable,
            bottomBar = bottomBar,
            content = content,
        )
    } else {
        SkinthesiaScreen(
            topBar = { SkinthesiaTopBar(title = title, onBack = onBack) },
            bottomBar = bottomBar,
            scrollable = scrollable,
            content = content,
        )
    }
}
