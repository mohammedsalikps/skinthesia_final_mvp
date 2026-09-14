package com.skinthesia.feature.plan

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.skinthesia.core.design.SkinthesiaTheme
import com.skinthesia.core.navigation.LocalAppNavigator
import com.skinthesia.core.navigation.PlanRoute
import com.skinthesia.core.navigation.ProductDetailRoute
import com.skinthesia.core.navigation.RecommendationsRoute
import com.skinthesia.core.navigation.RoutineRoute
import com.skinthesia.core.navigation.containerViewModel
import com.skinthesia.core.ui.components.EmptyState
import com.skinthesia.core.ui.components.FadeInUp
import com.skinthesia.core.ui.components.InfoNotice
import com.skinthesia.core.ui.components.LoadingState
import com.skinthesia.core.ui.components.SectionOverline
import com.skinthesia.core.ui.components.SegmentedTabs
import com.skinthesia.core.ui.components.SkinthesiaCard
import com.skinthesia.core.ui.components.SkinthesiaPrimaryButton
import com.skinthesia.core.ui.components.SkinthesiaScreen
import com.skinthesia.core.ui.components.SkinthesiaSecondaryButton
import com.skinthesia.core.ui.components.SkinthesiaTopBar
import com.skinthesia.core.ui.components.Tag
import com.skinthesia.core.ui.components.TagTone
import com.skinthesia.core.ui.icons.SkinthesiaIcons
import com.skinthesia.domain.model.PersonalizedPlan
import com.skinthesia.domain.model.Product
import com.skinthesia.domain.model.Routine
import com.skinthesia.domain.model.RoutineTime
import com.skinthesia.domain.model.StepFrequency
import com.skinthesia.domain.repository.PlanRepository
import com.skinthesia.domain.repository.ProductCatalogRepository
import com.skinthesia.feature.analysis.IconBadge
import com.skinthesia.feature.onboarding.icon
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

data class PlanUiState(
    val loading: Boolean = true,
    val plan: PersonalizedPlan? = null,
    val products: Map<String, Product> = emptyMap(),
)

class PlanViewModel(
    val initialTab: Int,
    val onboarding: Boolean,
    plans: PlanRepository,
    catalog: ProductCatalogRepository,
) : ViewModel() {
    val state: StateFlow<PlanUiState> = combine(plans.currentPlan, catalog.products) { plan, products ->
        PlanUiState(loading = false, plan = plan, products = products.associateBy { it.id })
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), PlanUiState())
}

private val PLAN_TABS = listOf("Routine", "Products", "Lifestyle")

/** Screen 24: the personalized plan, with routine, products and lifestyle tabs. */
@Composable
fun PlanScreen() {
    val navigator = LocalAppNavigator.current
    val viewModel = containerViewModel { handle ->
        val route = handle.toRoute<PlanRoute>()
        PlanViewModel(route.tab, route.onboarding, plans, catalog)
    }
    val state by viewModel.state.collectAsStateWithLifecycle()
    var tab by rememberSaveable { mutableIntStateOf(viewModel.initialTab.coerceIn(0, PLAN_TABS.lastIndex)) }
    val plan = state.plan

    SkinthesiaScreen(
        topBar = { SkinthesiaTopBar(title = "Your plan", onBack = navigator::back) },
        bottomBar = if (viewModel.onboarding && plan != null) {
            { SkinthesiaPrimaryButton(text = "Start My Journey", onClick = navigator::goHome) }
        } else {
            null
        },
    ) {
        when {
            state.loading -> LoadingState(message = "Loading your plan")
            plan == null -> EmptyState(
                icon = SkinthesiaIcons.Layers,
                title = "Your plan appears after your first analysis",
                body = "Take a selfie analysis and we'll build a routine around your skin.",
            )
            else -> PlanContent(
                plan = plan,
                products = state.products,
                tab = tab,
                onTab = { tab = it },
                onOpenRoutine = { time -> navigator.navigate(RoutineRoute(time.name)) },
                onOpenProduct = { id -> navigator.navigate(ProductDetailRoute(id)) },
                onAllRecommendations = { navigator.navigate(RecommendationsRoute) },
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun PlanContent(
    plan: PersonalizedPlan,
    products: Map<String, Product>,
    tab: Int,
    onTab: (Int) -> Unit,
    onOpenRoutine: (RoutineTime) -> Unit,
    onOpenProduct: (String) -> Unit,
    onAllRecommendations: () -> Unit,
) {
    val colors = SkinthesiaTheme.colors
    val typography = SkinthesiaTheme.typography
    val spacing = SkinthesiaTheme.spacing

    FadeInUp {
        Column {
            SectionOverline(text = "Personalized plan · Version ${plan.version}")
            Spacer(Modifier.height(8.dp))
            Text(text = plan.focusTitle, style = typography.display, color = colors.textPrimary)
            Spacer(Modifier.height(8.dp))
            Text(text = plan.summary, style = typography.body, color = colors.textSecondary)
            if (plan.focus.isNotEmpty()) {
                Spacer(Modifier.height(12.dp))
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    plan.focus.forEach { goal -> Tag(text = goal.label, tone = TagTone.CLAY, icon = goal.icon) }
                }
            }
        }
    }
    if (plan.changes.isNotEmpty()) {
        Spacer(Modifier.height(spacing.md))
        SkinthesiaCard(containerColor = colors.blushMist) {
            SectionOverline(text = "What changed this version")
            Spacer(Modifier.height(8.dp))
            plan.changes.forEach { change ->
                Row(Modifier.padding(vertical = 5.dp), verticalAlignment = Alignment.Top) {
                    Tag(text = change.kind.label, tone = TagTone.NEUTRAL)
                    Spacer(Modifier.width(10.dp))
                    Column(Modifier.weight(1f)) {
                        Text(text = change.title, style = typography.label, color = colors.textPrimary)
                        Text(text = change.detail, style = typography.bodySmall, color = colors.textSecondary)
                    }
                }
            }
        }
    }
    Spacer(Modifier.height(spacing.lg))
    SegmentedTabs(options = PLAN_TABS, selectedIndex = tab, onSelect = onTab)
    Spacer(Modifier.height(spacing.md))
    when (tab) {
        0 -> {
            RoutineTime.entries.forEach { time ->
                RoutineSummaryCard(time = time, routine = plan.routine(time), products = products, onOpen = { onOpenRoutine(time) })
                Spacer(Modifier.height(12.dp))
            }
            InfoNotice(text = "Introduce one new product at a time, and patch-test anything new on a small area first.")
        }
        1 -> {
            Text(
                text = "Chosen for your skin type, goals and readings. Every product shows why it fits.",
                style = typography.bodySmall,
                color = colors.textSecondary,
            )
            Spacer(Modifier.height(12.dp))
            plan.recommendations.filter { it.isPrimary }.forEach { recommendation ->
                products[recommendation.productId]?.let { product ->
                    RecommendationCard(recommendation = recommendation, product = product, onOpen = { onOpenProduct(product.id) }, maxReasons = 2)
                    Spacer(Modifier.height(12.dp))
                }
            }
            SkinthesiaSecondaryButton(text = "See All Recommendations", onClick = onAllRecommendations)
        }
        else -> {
            InfoNotice(text = "Context, not causes: small habits that support your routine.")
            Spacer(Modifier.height(12.dp))
            plan.lifestyle.forEach { suggestion ->
                LifestyleCard(suggestion)
                Spacer(Modifier.height(12.dp))
            }
        }
    }
}

@Composable
private fun RoutineSummaryCard(time: RoutineTime, routine: Routine, products: Map<String, Product>, onOpen: () -> Unit) {
    val colors = SkinthesiaTheme.colors
    val typography = SkinthesiaTheme.typography
    SkinthesiaCard(onClick = onOpen) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconBadge(time.icon)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(text = "${time.label} routine", style = typography.titleSmall, color = colors.textPrimary)
                Text(text = "${routine.steps.size} steps", style = typography.caption, color = colors.textMuted)
            }
            Icon(SkinthesiaIcons.ChevronRight, contentDescription = null, tint = colors.textMuted, modifier = Modifier.size(18.dp))
        }
        Spacer(Modifier.height(10.dp))
        routine.steps.sortedBy { it.order }.forEachIndexed { index, step ->
            Row(Modifier.fillMaxWidth().padding(vertical = 7.dp), verticalAlignment = Alignment.CenterVertically) {
                Text(text = "${index + 1}", style = typography.numeric, color = colors.primary, modifier = Modifier.width(22.dp))
                Column(Modifier.weight(1f)) {
                    Text(text = step.title, style = typography.label, color = colors.textPrimary)
                    val product = step.productId?.let(products::get)
                    Text(
                        text = product?.let { if (it.name == step.title) it.brand + " · " + step.type.label else it.brand + " · " + it.name }
                            ?: if (step.keepCurrentProduct) "Your current product" else step.type.label,
                        style = typography.caption,
                        color = colors.textMuted,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                if (step.frequency != StepFrequency.DAILY) {
                    Spacer(Modifier.width(8.dp))
                    Tag(text = step.frequency.label, tone = TagTone.MIST)
                }
            }
        }
    }
}
