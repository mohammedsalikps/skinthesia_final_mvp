package com.skinthesia.feature.plan

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.skinthesia.core.design.SkinthesiaTheme
import com.skinthesia.core.navigation.CartRoute
import com.skinthesia.core.navigation.LocalAppNavigator
import com.skinthesia.core.navigation.ProductDetailRoute
import com.skinthesia.core.navigation.containerViewModel
import com.skinthesia.core.ui.components.EmptyState
import com.skinthesia.core.ui.components.FadeInUp
import com.skinthesia.core.ui.components.IconAction
import com.skinthesia.core.ui.components.InfoNotice
import com.skinthesia.core.ui.components.LoadingState
import com.skinthesia.core.ui.components.ScreenHeader
import com.skinthesia.core.ui.components.ScrollableFilterTabs
import com.skinthesia.core.ui.components.SkinthesiaScreen
import com.skinthesia.core.ui.components.SkinthesiaTopBar
import com.skinthesia.core.ui.icons.SkinthesiaIcons
import com.skinthesia.domain.model.Product
import com.skinthesia.domain.model.ProductCategory
import com.skinthesia.domain.model.ProductRecommendation
import com.skinthesia.domain.repository.CartRepository
import com.skinthesia.domain.repository.PlanRepository
import com.skinthesia.domain.repository.ProductCatalogRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class RecommendationsUiState(
    val loading: Boolean = true,
    val items: List<Pair<ProductRecommendation, Product>> = emptyList(),
    val inBag: Set<String> = emptySet(),
    val bagCount: Int = 0,
)

class RecommendationsViewModel(
    plans: PlanRepository,
    catalog: ProductCatalogRepository,
    private val cart: CartRepository,
) : ViewModel() {

    val state: StateFlow<RecommendationsUiState> = combine(plans.currentPlan, catalog.products, cart.items) { plan, products, bag ->
        val byId = products.associateBy { it.id }
        RecommendationsUiState(
            loading = false,
            items = plan?.recommendations.orEmpty()
                .sortedWith(compareByDescending<ProductRecommendation> { it.isPrimary }.thenByDescending { it.matchScore })
                .mapNotNull { recommendation -> byId[recommendation.productId]?.let { recommendation to it } },
            inBag = bag.map { it.productId }.toSet(),
            bagCount = bag.sumOf { it.quantity },
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), RecommendationsUiState())

    fun add(productId: String) {
        viewModelScope.launch { cart.add(productId) }
    }
}

/** Screen 27: every recommendation with its fit and the reasons behind it. */
@Composable
fun RecommendationsScreen() {
    val navigator = LocalAppNavigator.current
    val viewModel = containerViewModel { RecommendationsViewModel(plans, catalog, cart) }
    val state by viewModel.state.collectAsStateWithLifecycle()
    val spacing = SkinthesiaTheme.spacing
    val motion = SkinthesiaTheme.motion
    var selected by rememberSaveable { mutableIntStateOf(0) }
    val categories: List<ProductCategory?> = listOf<ProductCategory?>(null) +
        state.items.map { it.second.category }.distinct().sortedBy { it.ordinal }
    val filter = categories.getOrNull(selected)
    val shown = state.items.filter { filter == null || it.second.category == filter }

    SkinthesiaScreen(
        topBar = {
            SkinthesiaTopBar(
                title = "Recommendations",
                onBack = navigator::back,
                actions = {
                    IconAction(
                        icon = SkinthesiaIcons.Bag,
                        contentDescription = "Your bag",
                        onClick = { navigator.navigate(CartRoute) },
                        badgeCount = state.bagCount,
                    )
                },
            )
        },
    ) {
        if (state.loading) {
            LoadingState(message = "Loading recommendations")
            return@SkinthesiaScreen
        }
        ScreenHeader(
            title = "Chosen for you",
            subtitle = "Matched to your skin type, goals, readings and budget. Every suggestion shows why.",
        )
        Spacer(Modifier.height(spacing.md))
        if (categories.size > 2) {
            ScrollableFilterTabs(
                options = categories.map { it?.plural ?: "All" },
                selectedIndex = selected,
                onSelect = { selected = it },
                contentPadding = PaddingValues(0.dp),
            )
            Spacer(Modifier.height(spacing.md))
        }
        if (shown.isEmpty()) {
            EmptyState(
                icon = SkinthesiaIcons.Products,
                title = "No recommendations yet",
                body = "Complete an analysis and we'll suggest products that fit your skin.",
            )
        }
        shown.forEachIndexed { index, (recommendation, product) ->
            FadeInUp(delayMillis = motion.stagger(index.coerceAtMost(4))) {
                RecommendationCard(
                    recommendation = recommendation,
                    product = product,
                    onOpen = { navigator.navigate(ProductDetailRoute(product.id)) },
                    inBag = product.id in state.inBag,
                    onAdd = { viewModel.add(product.id) },
                )
            }
            Spacer(Modifier.height(12.dp))
        }
        InfoNotice(
            text = "Patch-test new products and introduce them one at a time. Suggestions use product information and your own data; they aren't medical advice.",
        )
    }
}
