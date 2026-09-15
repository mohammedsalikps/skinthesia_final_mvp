package com.skinthesia.feature.marketplace

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.skinthesia.core.design.SkinthesiaTheme
import com.skinthesia.core.navigation.CartRoute
import com.skinthesia.core.navigation.LocalAppNavigator
import com.skinthesia.core.navigation.ProductDetailRoute
import com.skinthesia.core.navigation.containerViewModel
import com.skinthesia.core.ui.art.ProductArtwork
import com.skinthesia.core.ui.components.FadeInUp
import com.skinthesia.core.ui.components.IconAction
import com.skinthesia.core.ui.components.InfoNotice
import com.skinthesia.core.ui.components.LoadingState
import com.skinthesia.core.ui.components.MiniRing
import com.skinthesia.core.ui.components.QuantityStepper
import com.skinthesia.core.ui.components.RatingLine
import com.skinthesia.core.ui.components.ScreenHeader
import com.skinthesia.core.ui.components.SectionHeader
import com.skinthesia.core.ui.components.SectionOverline
import com.skinthesia.core.ui.components.SkinthesiaCard
import com.skinthesia.core.ui.components.SkinthesiaPrimaryButton
import com.skinthesia.core.ui.components.SkinthesiaScreen
import com.skinthesia.core.ui.components.SkinthesiaTopBar
import com.skinthesia.core.ui.components.Tag
import com.skinthesia.core.ui.components.TagTone
import com.skinthesia.core.ui.formatPrice
import com.skinthesia.core.ui.icons.SkinthesiaIcons
import com.skinthesia.domain.model.Product
import com.skinthesia.domain.model.ProductRecommendation
import com.skinthesia.domain.model.SkinType
import com.skinthesia.domain.repository.CartRepository
import com.skinthesia.domain.repository.PlanRepository
import com.skinthesia.domain.repository.ProductCatalogRepository
import com.skinthesia.domain.repository.UserProfileRepository
import com.skinthesia.feature.plan.ReasonLine
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class ProductDetailUiState(
    val loading: Boolean = true,
    val product: Product? = null,
    val recommendation: ProductRecommendation? = null,
    val inBag: Int = 0,
    val bagCount: Int = 0,
    val related: List<Product> = emptyList(),
    val skinType: SkinType? = null,
)

class ProductDetailViewModel(
    val productId: String,
    catalog: ProductCatalogRepository,
    plans: PlanRepository,
    private val cart: CartRepository,
    profiles: UserProfileRepository,
) : ViewModel() {

    private val skinType = MutableStateFlow<SkinType?>(null)

    init {
        viewModelScope.launch { skinType.value = profiles.current().skin.skinType }
    }

    val state: StateFlow<ProductDetailUiState> = combine(catalog.products, plans.currentPlan, cart.items, skinType) { products, plan, bag, type ->
        val product = products.firstOrNull { it.id == productId }
        ProductDetailUiState(
            loading = false,
            product = product,
            recommendation = plan?.recommendations?.firstOrNull { it.productId == productId },
            inBag = bag.firstOrNull { it.productId == productId }?.quantity ?: 0,
            bagCount = bag.sumOf { it.quantity },
            related = product?.let { p ->
                products.filter { it.id != p.id && it.category != p.category && it.supportsGoals.any { g -> g in p.supportsGoals } }
                    .sortedByDescending { it.rating }
                    .take(4)
            }.orEmpty(),
            skinType = type,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ProductDetailUiState())

    fun add(quantity: Int) {
        viewModelScope.launch { cart.add(productId, quantity) }
    }
}

/** Product details: what it does, key ingredients, fit for you, and adding it to the bag. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ProductDetailScreen() {
    val navigator = LocalAppNavigator.current
    val viewModel = containerViewModel { handle ->
        ProductDetailViewModel(handle.toRoute<ProductDetailRoute>().productId, catalog, plans, cart, profiles)
    }
    val state by viewModel.state.collectAsStateWithLifecycle()
    val colors = SkinthesiaTheme.colors
    val typography = SkinthesiaTheme.typography
    val spacing = SkinthesiaTheme.spacing
    var quantity by rememberSaveable { mutableIntStateOf(1) }
    var showFull by rememberSaveable { mutableStateOf(false) }
    val product = state.product

    SkinthesiaScreen(
        topBar = {
            SkinthesiaTopBar(
                title = product?.category?.label,
                onBack = navigator::back,
                actions = {
                    IconAction(icon = SkinthesiaIcons.Bag, contentDescription = "Your bag", onClick = { navigator.navigate(CartRoute) }, badgeCount = state.bagCount)
                },
            )
        },
        bottomBar = if (product != null) {
            {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    QuantityStepper(quantity = quantity, onChange = { quantity = it }, min = 1, max = 9)
                    Spacer(Modifier.width(12.dp))
                    SkinthesiaPrimaryButton(
                        text = (if (state.inBag > 0) "Add more · " else "Add to bag · ") + formatPrice(product.price * quantity),
                        onClick = {
                            viewModel.add(quantity)
                            quantity = 1
                        },
                        modifier = Modifier.weight(1f),
                        trailingIcon = null,
                    )
                }
                if (state.inBag > 0) {
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = "${state.inBag} in your bag",
                        style = typography.caption,
                        color = colors.success,
                        modifier = Modifier.align(Alignment.CenterHorizontally),
                    )
                }
            }
        } else {
            null
        },
    ) {
        when {
            state.loading -> LoadingState(message = "Loading product")
            product == null -> ScreenHeader(title = "Product not found", subtitle = "It may no longer be available.", centered = true)
            else -> {
                FadeInUp {
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .height(290.dp)
                            .clip(SkinthesiaTheme.shapes.card)
                            .background(colors.surfaceMuted),
                    ) {
                        ProductArtwork(
                            form = product.form,
                            tone = product.tone,
                            modifier = Modifier.fillMaxSize(),
                            mark = product.brand.take(1).uppercase(),
                            contentDescription = product.name,
                        )
                    }
                }
                Spacer(Modifier.height(spacing.md))
                SectionOverline(text = product.brand)
                Spacer(Modifier.height(6.dp))
                Text(text = product.name, style = typography.title, color = colors.textPrimary)
                Spacer(Modifier.height(8.dp))
                RatingLine(rating = product.rating, reviewCount = product.reviewCount)
                Spacer(Modifier.height(10.dp))
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(text = formatPrice(product.price), style = typography.metric, color = colors.textPrimary)
                    Spacer(Modifier.width(8.dp))
                    Text(text = product.size, style = typography.body, color = colors.textMuted)
                }
                if (product.attributes.isNotEmpty()) {
                    Spacer(Modifier.height(12.dp))
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        product.attributes.forEach { Tag(text = it.label, tone = TagTone.NEUTRAL) }
                    }
                }

                val recommendation = state.recommendation
                if (recommendation != null) {
                    Spacer(Modifier.height(spacing.md))
                    SkinthesiaCard(containerColor = colors.blushMist) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            MiniRing(value = recommendation.matchScore, size = 50.dp)
                            Spacer(Modifier.width(12.dp))
                            Column(Modifier.weight(1f)) {
                                Text(text = "Why it fits you", style = typography.labelLarge, color = colors.textPrimary)
                                Text(
                                    text = if (recommendation.isPrimary) "Part of your routine" else "An alternative for your plan",
                                    style = typography.caption,
                                    color = colors.textMuted,
                                )
                            }
                        }
                        Spacer(Modifier.height(10.dp))
                        recommendation.reasons.forEach { ReasonLine(it) }
                    }
                } else {
                    val type = state.skinType
                    if (type != null && type in product.skinTypes) {
                        Spacer(Modifier.height(spacing.md))
                        Tag(text = "Suits ${type.label.lowercase()} skin", tone = TagTone.SAGE, icon = SkinthesiaIcons.Check)
                    }
                }

                Spacer(Modifier.height(spacing.lg))
                SectionOverline(text = "About")
                Spacer(Modifier.height(8.dp))
                Text(text = product.description, style = typography.body, color = colors.textSecondary)

                if (product.keyIngredients.isNotEmpty()) {
                    Spacer(Modifier.height(spacing.lg))
                    SectionOverline(text = "Key ingredients")
                    Spacer(Modifier.height(4.dp))
                    product.keyIngredients.forEach { ingredient ->
                        Row(Modifier.padding(vertical = 7.dp).semantics(mergeDescendants = true) {}, verticalAlignment = Alignment.Top) {
                            Box(Modifier.padding(top = 7.dp).size(6.dp).clip(CircleShape).background(colors.primary))
                            Spacer(Modifier.width(12.dp))
                            Column {
                                Text(text = ingredient.name, style = typography.labelLarge, color = colors.textPrimary)
                                Text(text = ingredient.role, style = typography.bodySmall, color = colors.textSecondary)
                            }
                        }
                    }
                }

                Spacer(Modifier.height(spacing.lg))
                SectionOverline(text = "How to use")
                Spacer(Modifier.height(8.dp))
                Text(text = product.howToUse, style = typography.body, color = colors.textSecondary)

                Spacer(Modifier.height(spacing.lg))
                SkinthesiaCard {
                    SectionOverline(text = "Suits")
                    Spacer(Modifier.height(6.dp))
                    Text(text = product.skinTypes.joinToString(", ") { it.label }, style = typography.body, color = colors.textPrimary)
                    if (product.supportsGoals.isNotEmpty()) {
                        Spacer(Modifier.height(12.dp))
                        SectionOverline(text = "Helps with")
                        Spacer(Modifier.height(6.dp))
                        Text(text = product.supportsGoals.joinToString(", ") { it.label }, style = typography.body, color = colors.textPrimary)
                    }
                    Spacer(Modifier.height(12.dp))
                    SectionOverline(text = "Use")
                    Spacer(Modifier.height(6.dp))
                    Text(text = product.routineTimes.joinToString(" and ") { it.label.lowercase() }.replaceFirstChar { it.uppercase() }, style = typography.body, color = colors.textPrimary)
                }

                Spacer(Modifier.height(spacing.md))
                SkinthesiaCard(onClick = { showFull = !showFull }) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "Full ingredient list", style = typography.labelLarge, color = colors.textPrimary, modifier = Modifier.weight(1f))
                        Icon(
                            if (showFull) SkinthesiaIcons.ChevronUp else SkinthesiaIcons.ChevronDown,
                            contentDescription = if (showFull) "Collapse" else "Expand",
                            tint = colors.textMuted,
                            modifier = Modifier.size(16.dp),
                        )
                    }
                    AnimatedVisibility(visible = showFull) {
                        Text(text = product.fullIngredients, style = typography.bodySmall, color = colors.textSecondary, modifier = Modifier.padding(top = 10.dp))
                    }
                }

                if (state.related.isNotEmpty()) {
                    Spacer(Modifier.height(spacing.lg))
                    SectionHeader(title = "Pairs well with", overline = "Same goals")
                    Spacer(Modifier.height(12.dp))
                    Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        state.related.forEach { other ->
                            ProductTile(other, Modifier.width(160.dp)) { navigator.push(ProductDetailRoute(other.id)) }
                        }
                    }
                }
                Spacer(Modifier.height(spacing.lg))
                InfoNotice(text = "Patch-test new products before first use. Product information here is demonstration data.")
            }
        }
    }
}
