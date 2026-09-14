package com.skinthesia.feature.onboarding

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.unit.dp
import com.skinthesia.R
import com.skinthesia.core.design.SkinthesiaTheme
import com.skinthesia.core.navigation.OnboardingRoutes
import com.skinthesia.core.ui.components.CheckCircle
import com.skinthesia.core.ui.components.DropdownOption
import com.skinthesia.core.ui.components.FadeInUp
import com.skinthesia.core.ui.components.OnboardingHeader
import com.skinthesia.core.ui.components.OnboardingScaffold
import com.skinthesia.core.ui.components.SkinthesiaDivider
import com.skinthesia.core.ui.components.SkinthesiaDropdownField
import com.skinthesia.core.ui.components.SkinthesiaPrimaryButton
import com.skinthesia.core.ui.labelRes
import com.skinthesia.domain.model.AgeRange
import com.skinthesia.domain.model.SkinConcern
import com.skinthesia.domain.model.SkinType

/** Screen 05: age range, self-identified skin type and main concerns. */
@Composable
fun QuestionnaireScreen(
    state: OnboardingUiState,
    onAgeRange: (AgeRange) -> Unit,
    onSkinType: (SkinType) -> Unit,
    onToggleConcern: (SkinConcern) -> Unit,
    onBack: () -> Unit,
    onContinue: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = SkinthesiaTheme.colors
    val typography = SkinthesiaTheme.typography
    val spacing = SkinthesiaTheme.spacing
    val motion = SkinthesiaTheme.motion

    val ageOptions = AgeRange.entries.map { DropdownOption(it, stringResource(it.labelRes)) }
    val skinTypeOptions = SkinType.entries.map { DropdownOption(it, stringResource(it.labelRes)) }

    OnboardingScaffold(
        step = 5,
        totalSteps = OnboardingRoutes.TOTAL_STEPS,
        onBack = onBack,
        modifier = modifier,
        bottomBar = {
            SkinthesiaPrimaryButton(
                text = stringResource(R.string.action_next),
                onClick = onContinue,
                enabled = state.canContinueFromQuestionnaire,
            )
        },
    ) {
        OnboardingHeader(
            title = stringResource(R.string.questionnaire_title),
            subtitle = stringResource(R.string.questionnaire_subtitle),
        )
        Spacer(Modifier.height(spacing.section))

        FadeInUp {
            SkinthesiaDropdownField(
                label = stringResource(R.string.field_age_range),
                selected = state.ageRange,
                options = ageOptions,
                onSelect = onAgeRange,
                modifier = Modifier.fillMaxWidth(),
            )
        }
        Spacer(Modifier.height(spacing.md + spacing.xxs))

        FadeInUp(delayMillis = motion.stagger(1)) {
            SkinthesiaDropdownField(
                label = stringResource(R.string.field_skin_type),
                selected = state.skinType,
                options = skinTypeOptions,
                onSelect = onSkinType,
                modifier = Modifier.fillMaxWidth(),
            )
        }
        Spacer(Modifier.height(spacing.lg))

        FadeInUp(delayMillis = motion.stagger(2)) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = stringResource(R.string.field_main_concerns),
                    style = typography.labelSmall,
                    color = colors.textSecondary,
                )
                Spacer(Modifier.height(spacing.xxs))
                SkinConcern.entries.forEachIndexed { index, concern ->
                    ConcernRow(
                        label = stringResource(concern.labelRes),
                        selected = concern in state.concerns,
                        onClick = { onToggleConcern(concern) },
                    )
                    if (index < SkinConcern.entries.lastIndex) {
                        SkinthesiaDivider()
                    }
                }
            }
        }
    }
}

/** Light list row: check circle and label separated by hairlines, as in the reference. */
@Composable
private fun ConcernRow(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val colors = SkinthesiaTheme.colors
    val typography = SkinthesiaTheme.typography
    val interaction = remember { MutableInteractionSource() }
    val selectedState = stringResource(R.string.state_selected)
    val notSelectedState = stringResource(R.string.state_not_selected)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = 48.dp)
            .toggleable(
                value = selected,
                interactionSource = interaction,
                indication = null,
                role = Role.Checkbox,
                onValueChange = { onClick() },
            )
            .semantics { stateDescription = if (selected) selectedState else notSelectedState }
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        CheckCircle(selected = selected, size = 20.dp)
        Spacer(Modifier.width(14.dp))
        Text(
            text = label,
            style = typography.body,
            color = if (selected) colors.textPrimary else colors.textSecondary,
        )
    }
}
