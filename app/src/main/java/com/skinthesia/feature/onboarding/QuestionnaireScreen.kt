package com.skinthesia.feature.onboarding

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.skinthesia.core.design.SkinthesiaTheme
import com.skinthesia.core.navigation.LifestyleRoute
import com.skinthesia.core.navigation.LocalAppNavigator
import com.skinthesia.core.navigation.containerViewModel
import com.skinthesia.core.ui.components.ChoiceChips
import com.skinthesia.core.ui.components.FadeInUp
import com.skinthesia.core.ui.components.OnboardingHeader
import com.skinthesia.core.ui.components.OnboardingScaffold
import com.skinthesia.core.ui.components.OptionRow
import com.skinthesia.core.ui.components.QuestionBlock
import com.skinthesia.core.ui.components.SegmentedTabs
import com.skinthesia.core.ui.components.SkinthesiaDivider
import com.skinthesia.core.ui.components.SkinthesiaPrimaryButton
import com.skinthesia.core.ui.components.SkinthesiaTextField
import com.skinthesia.core.ui.icons.SkinthesiaIcons
import com.skinthesia.domain.model.Budget
import com.skinthesia.domain.model.OnboardingStage
import com.skinthesia.domain.model.ProductCategory
import com.skinthesia.domain.model.RoutineLevel
import com.skinthesia.domain.model.Sensitivity
import com.skinthesia.domain.model.SkinConcern
import com.skinthesia.domain.model.SkinProfile
import com.skinthesia.domain.model.SkinType
import com.skinthesia.domain.repository.UserProfileRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class QuestionnaireViewModel(private val profiles: UserProfileRepository) : ViewModel() {

    private val _state = MutableStateFlow(SkinProfile())
    val state: StateFlow<SkinProfile> = _state.asStateFlow()

    init {
        viewModelScope.launch { _state.value = profiles.current().skin }
    }

    fun setSkinType(type: SkinType) = _state.update { it.copy(skinType = type) }
    fun toggleConcern(concern: SkinConcern) = _state.update { it.copy(concerns = it.concerns.toggle(concern)) }
    fun setRoutine(level: RoutineLevel) = _state.update { it.copy(routineLevel = level) }
    fun toggleProduct(category: ProductCategory) = _state.update { it.copy(currentProducts = it.currentProducts.toggle(category)) }
    fun setNote(note: String) = _state.update { it.copy(existingProductsNote = note.take(140)) }
    fun togglePreference(preference: Sensitivity) = _state.update { it.copy(preferences = it.preferences.toggle(preference)) }
    fun setBudget(budget: Budget) = _state.update { it.copy(budget = budget) }

    fun submit(onDone: () -> Unit) {
        val skin = _state.value
        if (skin.skinType == null) return
        viewModelScope.launch {
            profiles.update { it.copy(skin = skin, onboardingStage = it.onboardingStage.atLeast(OnboardingStage.LIFESTYLE)) }
            onDone()
        }
    }
}

internal fun <T> Set<T>.toggle(item: T): Set<T> = if (item in this) this - item else this + item

/** Screen 08: skin type, concerns, routine, products, preferences and budget in one calm page. */
@Composable
fun QuestionnaireScreen() {
    val navigator = LocalAppNavigator.current
    val viewModel = containerViewModel { QuestionnaireViewModel(profiles) }
    val skin by viewModel.state.collectAsStateWithLifecycle()
    val spacing = SkinthesiaTheme.spacing
    val motion = SkinthesiaTheme.motion

    OnboardingScaffold(
        step = OnboardingSteps.QUESTIONNAIRE,
        totalSteps = OnboardingSteps.TOTAL,
        onBack = navigator::back,
        bottomBar = {
            SkinthesiaPrimaryButton(
                text = "Next",
                onClick = { viewModel.submit { navigator.navigate(LifestyleRoute) } },
                enabled = skin.skinType != null,
            )
        },
    ) {
        OnboardingHeader(title = "A little about your skin", subtitle = "Six quick questions. Only skin type is required.")
        Spacer(Modifier.height(spacing.lg))

        FadeInUp {
            QuestionBlock(title = "How would you describe your skin?", icon = SkinthesiaIcons.Face) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    SkinType.entries.forEach { type ->
                        OptionRow(
                            title = type.label,
                            description = type.description,
                            selected = skin.skinType == type,
                            onClick = { viewModel.setSkinType(type) },
                        )
                    }
                }
            }
        }
        Divider()
        FadeInUp(delayMillis = motion.stagger(1)) {
            QuestionBlock(title = "Main concerns", icon = SkinthesiaIcons.Target, hint = "Choose any that apply.") {
                ChoiceChips(options = SkinConcern.entries, selected = skin.concerns, onToggle = viewModel::toggleConcern, label = { it.label })
            }
        }
        Divider()
        FadeInUp(delayMillis = motion.stagger(2)) {
            QuestionBlock(title = "Your current routine", icon = SkinthesiaIcons.Sunrise) {
                ChoiceChips(
                    options = RoutineLevel.entries,
                    selected = setOfNotNull(skin.routineLevel),
                    onToggle = viewModel::setRoutine,
                    label = { it.label + " · " + it.description.lowercase() },
                    single = true,
                )
            }
        }
        Divider()
        FadeInUp(delayMillis = motion.stagger(3)) {
            QuestionBlock(title = "Products you already use", icon = SkinthesiaIcons.Products, hint = "We'll build around what you have.") {
                Column {
                    ChoiceChips(options = ProductCategory.entries, selected = skin.currentProducts, onToggle = viewModel::toggleProduct, label = { it.plural })
                    Spacer(Modifier.height(12.dp))
                    SkinthesiaTextField(
                        value = skin.existingProductsNote,
                        onValueChange = viewModel::setNote,
                        placeholder = "Anything we should know? Favourites, brands (optional)",
                        singleLine = false,
                        minLines = 2,
                        maxLength = 140,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
        }
        Divider()
        FadeInUp(delayMillis = motion.stagger(4)) {
            QuestionBlock(title = "Preferences and sensitivities", icon = SkinthesiaIcons.Shield) {
                ChoiceChips(options = Sensitivity.entries, selected = skin.preferences, onToggle = viewModel::togglePreference, label = { it.label })
            }
        }
        Divider()
        FadeInUp(delayMillis = motion.stagger(5)) {
            QuestionBlock(title = "Budget per product", icon = SkinthesiaIcons.Bag, hint = skin.budget.range) {
                SegmentedTabs(
                    options = Budget.entries.map { it.label },
                    selectedIndex = skin.budget.ordinal,
                    onSelect = { viewModel.setBudget(Budget.entries[it]) },
                )
            }
        }
    }
}

@Composable
private fun Divider() {
    Spacer(Modifier.height(SkinthesiaTheme.spacing.lg))
    SkinthesiaDivider()
    Spacer(Modifier.height(SkinthesiaTheme.spacing.lg))
}
