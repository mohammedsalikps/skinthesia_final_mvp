package com.skinthesia.feature.onboarding

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.skinthesia.R
import com.skinthesia.core.design.SkinthesiaTheme
import com.skinthesia.core.navigation.OnboardingRoutes
import com.skinthesia.core.ui.components.OnboardingHeader
import com.skinthesia.core.ui.components.OnboardingScaffold
import com.skinthesia.core.ui.components.SelectableTile
import com.skinthesia.core.ui.components.SkinthesiaPrimaryButton
import com.skinthesia.core.ui.icon
import com.skinthesia.core.ui.labelRes
import com.skinthesia.domain.model.LifestyleFactor

/** Screen 06: multi-select grid of influences on the user's skin. */
@Composable
fun LifestyleScreen(
    state: OnboardingUiState,
    onToggleFactor: (LifestyleFactor) -> Unit,
    onBack: () -> Unit,
    onNext: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val spacing = SkinthesiaTheme.spacing

    OnboardingScaffold(
        step = 6,
        totalSteps = OnboardingRoutes.TOTAL_STEPS,
        onBack = onBack,
        modifier = modifier,
        bottomBar = {
            SkinthesiaPrimaryButton(
                text = stringResource(R.string.action_next),
                onClick = onNext,
                enabled = !state.isSaving,
                loading = state.isSaving,
            )
        },
    ) {
        OnboardingHeader(
            title = stringResource(R.string.lifestyle_title),
            subtitle = stringResource(R.string.lifestyle_instruction),
        )
        Spacer(Modifier.height(spacing.lg))

        SelectionGrid(items = LifestyleFactor.entries) { factor ->
            SelectableTile(
                label = stringResource(factor.labelRes),
                icon = factor.icon,
                selected = factor in state.lifestyleFactors,
                onClick = { onToggleFactor(factor) },
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}
