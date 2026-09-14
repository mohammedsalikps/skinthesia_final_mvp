package com.skinthesia.feature.marketplace

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.skinthesia.core.design.SkinthesiaTheme
import com.skinthesia.core.navigation.CartRoute
import com.skinthesia.core.navigation.LocalAppNavigator
import com.skinthesia.core.navigation.MarketplaceRoute
import com.skinthesia.core.navigation.OrdersRoute
import com.skinthesia.core.navigation.ProductDetailRoute
import com.skinthesia.core.navigation.containerViewModel
import com.skinthesia.core.ui.art.ProductArtwork
import com.skinthesia.core.ui.components.EmptyState
import com.skinthesia.core.ui.components.FadeInUp
import com.skinthesia.core.ui.components.IconAction
import com.skinthesia.core.ui.components.InfoNotice
import com.skinthesia.core.ui.components.LoadingState
import com.skinthesia.core.ui.components.ScreenHeader
import com.skinthesia.core.ui.components.ScrollableFilterTabs
import com.skinthesia.core.ui.components.SearchField
import com.skinthesia.core.ui.components.SectionHeader
import com.skinthesia.core.ui.components.SkinthesiaCard
import com.skinthesia.core.ui.components.SkinthesiaScreen
import com.skinthesia.core.ui.components.SkinthesiaTopBar
import com.skinthesia.core.ui.components.Tag
import com.skinthesia.core.ui.components.TagTone
import com.skinthesia.core.ui.components.fmt
import com.skinthesia.core.ui.formatPrice
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

data class MarketplaceUiState(
    val loading: Boolean = true,
    val products: List<Product> = emptyList(),
    val recommended: List<Pair<ProductRecommendation, Product>> = emptyList(),
    val bagCount: Int = 0,
)

class MarketplaceViewModel(
    val initialCategory: ProductCategory?,
    catalog: ProductCatalogRepository,
    plans: PlanRepository,
    cart: CartRepository,
) : ViewModel() {
    val state: StateFlow<MarketplaceUiState> = combine(catalog.products, plans.currentPlan, cart.items) { products, plan, bag ->
        val byId = products.associateBy { it.id }
        MarketplaceUiState(
            loading = false,
            products = products.sortedWith(compareBy<Product> { it.category.ordinal }.thenByDescending { it.rating }),
            recommended = plan?.recommendations.orEmpty()
                .sortedByDescending { it.matchScore }
                .mapNotNull { recommendation -> byId[recommendation.productId]?.let { recommendation to it } }
                .take(6),
            bagCount = bag.sumOf { it.quantity },
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), MarketplaceUiState())
}

private fun Product.matches(query: String): Boolean {
    val q = query.trim()
    if (q.isEmpty()) return true
    return name.contains(q, ignoreCase = true) ||
        brand.contains(q, ignoreCase = true) ||
        category.label.contains(q, ignoreCase = true) ||
        keyIngredients.any { it.name.contains(q, ignoreCase = true) }
}

/** Marketplace home: search, categories, plan matches and the full catalogue. */
@Composable
fun MarketplaceScreen() {
    val navigator = LocalAppNavigator.current
    val viewModel = containerViewModel { handle ->
        val category = handle.toRoute<MarketplaceRoute>().category?.let { c -> runCatching { ProductCategory.valueOf(c) }.getOrNull() }
        MarketplaceViewModel(category, catalog, plans, cart)
    }
    val state by viewModel.state.collectAsStateWithLifecycle()
    val spacing = SkinthesiaTheme.spacing
    val categories: List<ProductCategory?> = listOf<ProductCategory?>(null) + ProductCategory.entries
    var query by rememberSaveable { mutableStateOf("") }
    var selected by rememberSaveable { mutableIntStateOf(viewModel.initialCategory?.let { categories.indexOf(it) } ?: 0) }
    val filter = categories.getOrNull(selected)
    val shown = state.products.filter { (filter == null || it.category == filter) && it.matches(query) }

    SkinthesiaScreen(
        topBar = {
            SkinthesiaTopBar(
                title = "Shop",
                onBack = navigator::back,
                actions = {
                    IconAction(icon = SkinthesiaIcons.Receipt, contentDescription = "Your orders", onClick = { navigator.navigate(OrdersRoute) })
                    IconAction(icon = SkinthesiaIcons.Bag, contentDescription = "Your bag", onClick = { navigator.navigate(CartRoute) }, badgeCount = state.bagCount)
                },
            )
        },
    ) {
        if (state.loading) {
            LoadingState(message = "Loading products")
            return@SkinthesiaScreen
        }
        ScreenHeader(title = "Skincare, matched to you", subtitle = "Every product explains what it does and who it suits.")
        Spacer(Modifier.height(spacing.md))
        SearchField(value = query, onValueChange = { query = it }, placeholder = "Search products or ingredients")
        Spacer(Modifier.height(spacing.md))
        ScrollableFilterTabs(
            options = categories.map { it?.plural ?: "All" },
            selectedIndex = selected,
            onSelect = { selected = it },
            contentPadding = PaddingValues(0.dp),
        )
        if (query.isBlank() && filter == null && state.recommended.isNotEmpty()) {
            Spacer(Modifier.height(spacing.lg))
            SectionHeader(title = "Matched to your plan", overline = "Recommended for you")
            Spacer(Modifier.height(12.dp))
            Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                state.recommended.forEach { (recommendation, product) ->
                    RecommendedTile(product = product, match = recommendation.matchScore) { navigator.navigate(ProductDetailRoute(product.id)) }
                }
            }
        }
        Spacer(Modifier.height(spacing.lg))
        SectionHeader(title = filter?.plural ?: "All products", overline = "${shown.size} products")
        Spacer(Modifier.height(12.dp))
        if (shown.isEmpty()) {
            EmptyState(icon = SkinthesiaIcons.Search, title = "Nothing matches", body = "Try another word, ingredient or category.")
        }
        shown.chunked(2).forEachIndexed { index, row ->
            FadeInUp(delayMillis = SkinthesiaTheme.motion.stagger(index.coerceAtMost(3))) {
                Row(Modifier.fillMaxWidth().height(IntrinsicSize.Min), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    row.forEach { product ->
                        ProductTile(product, Modifier.weight(1f).fillMaxHeight()) { navigator.navigate(ProductDetailRoute(product.id)) }
                    }
                    if (row.size == 1) Spacer(Modifier.weight(1f))
                }
            }
            Spacer(Modifier.height(12.dp))
        }
        Spacer(Modifier.height(spacing.sm))
        InfoNotice(text = "Products and prices are demonstration data. Checkout is simulated: no payment is taken and nothing ships.")
    }
}

@Composable
fun ProductTile(product: Product, modifier: Modifier = Modifier, onClick: () -> Unit) {
    val colors = SkinthesiaTheme.colors
    val typography = SkinthesiaTheme.typography
    SkinthesiaCard(modifier = modifier, onClick = onClick, contentPadding = PaddingValues(12.dp)) {
        ProductArtwork(
            form = product.form,
            tone = product.tone,
            modifier = Modifier.fillMaxWidth().height(128.dp).clip(SkinthesiaTheme.shapes.tile),
            contentDescription = null,
        )
        Spacer(Modifier.height(10.dp))
        Text(text = product.brand, style = typography.caption, color = colors.textMuted, maxLines = 1)
        Text(text = product.name, style = typography.label, color = colors.textPrimary, maxLines = 2, minLines = 2, overflow = TextOverflow.Ellipsis)
        Spacer(Modifier.height(6.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(text = formatPrice(product.price), style = typography.label, color = colors.textPrimary, modifier = Modifier.weight(1f))
            Icon(SkinthesiaIcons.StarFilled, contentDescription = null, tint = colors.gold, modifier = Modifier.size(12.dp))
            Spacer(Modifier.width(3.dp))
            Text(text = fmt(product.rating, 1), style = typography.caption, color = colors.textSecondary)
        }
    }
}

@Composable
private fun RecommendedTile(product: Product, match: Int, onClick: () -> Unit) {
    val colors = SkinthesiaTheme.colors
    val typography = SkinthesiaTheme.typography
    SkinthesiaCard(modifier = Modifier.width(156.dp), onClick = onClick, contentPadding = PaddingValues(12.dp)) {
        ProductArtwork(
            form = product.form,
            tone = product.tone,
            modifier = Modifier.fillMaxWidth().height(104.dp).clip(SkinthesiaTheme.shapes.tile),
            contentDescription = null,
        )
        Spacer(Modifier.height(10.dp))
        Text(text = product.name, style = typography.label, color = colors.textPrimary, maxLines = 2, minLines = 2, overflow = TextOverflow.Ellipsis)
        Spacer(Modifier.height(6.dp))
        Tag(text = "$match% match", tone = TagTone.SAGE)
    }
}
