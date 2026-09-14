package com.skinthesia.feature.marketplace

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.skinthesia.core.design.SkinthesiaTheme
import com.skinthesia.core.navigation.CheckoutRoute
import com.skinthesia.core.navigation.LocalAppNavigator
import com.skinthesia.core.navigation.MarketplaceRoute
import com.skinthesia.core.navigation.OrderConfirmationRoute
import com.skinthesia.core.navigation.ProductDetailRoute
import com.skinthesia.core.navigation.containerViewModel
import com.skinthesia.core.ui.components.EmptyState
import com.skinthesia.core.ui.components.KeyValueRow
import com.skinthesia.core.ui.components.LoadingState
import com.skinthesia.core.ui.components.QuantityStepper
import com.skinthesia.core.ui.components.SectionOverline
import com.skinthesia.core.ui.components.SkinthesiaCard
import com.skinthesia.core.ui.components.SkinthesiaDivider
import com.skinthesia.core.ui.components.SkinthesiaPrimaryButton
import com.skinthesia.core.ui.components.SkinthesiaScreen
import com.skinthesia.core.ui.components.SkinthesiaTextField
import com.skinthesia.core.ui.components.SkinthesiaTopBar
import com.skinthesia.core.ui.formatPrice
import com.skinthesia.core.ui.icons.SkinthesiaIcons
import com.skinthesia.domain.model.CartLine
import com.skinthesia.domain.model.CartSummary
import com.skinthesia.domain.model.DeliveryDetails
import com.skinthesia.domain.repository.CartRepository
import com.skinthesia.domain.repository.OrderRepository
import com.skinthesia.domain.repository.ProductCatalogRepository
import com.skinthesia.domain.repository.UserProfileRepository
import com.skinthesia.feature.analysis.LevelBar
import com.skinthesia.feature.plan.ProductThumb
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** The bag, joined with the catalogue. Null while loading. */
private fun bagSummary(cart: CartRepository, catalog: ProductCatalogRepository) =
    combine(cart.items, catalog.products) { items, products ->
        val byId = products.associateBy { it.id }
        CartSummary(items.sortedBy { it.addedAt }.mapNotNull { item -> byId[item.productId]?.let { CartLine(it, item.quantity) } })
    }

class CartViewModel(private val cart: CartRepository, catalog: ProductCatalogRepository) : ViewModel() {
    val summary: StateFlow<CartSummary?> = bagSummary(cart, catalog)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    fun setQuantity(productId: String, quantity: Int) {
        viewModelScope.launch { if (quantity <= 0) cart.remove(productId) else cart.setQuantity(productId, quantity) }
    }
}

/** The bag: quantities, delivery threshold and totals. */
@Composable
fun CartScreen() {
    val navigator = LocalAppNavigator.current
    val viewModel = containerViewModel { CartViewModel(cart, catalog) }
    val summary by viewModel.summary.collectAsStateWithLifecycle()
    val colors = SkinthesiaTheme.colors
    val typography = SkinthesiaTheme.typography
    val spacing = SkinthesiaTheme.spacing
    val bag = summary

    SkinthesiaScreen(
        topBar = { SkinthesiaTopBar(title = "Your bag", onBack = navigator::back) },
        bottomBar = if (bag != null && bag.lines.isNotEmpty()) {
            { SkinthesiaPrimaryButton(text = "Checkout · " + formatPrice(bag.total), onClick = { navigator.navigate(CheckoutRoute) }) }
        } else {
            null
        },
    ) {
        when {
            bag == null -> LoadingState(message = "Loading your bag")
            bag.lines.isEmpty() -> EmptyState(
                icon = SkinthesiaIcons.Bag,
                title = "Your bag is empty",
                body = "Products you add from your plan or the shop appear here.",
                actionLabel = "Browse products",
                onAction = { navigator.navigate(MarketplaceRoute()) },
            )
            else -> {
                bag.lines.forEach { line ->
                    CartLineCard(
                        line = line,
                        onQuantity = { viewModel.setQuantity(line.product.id, it) },
                        onOpen = { navigator.navigate(ProductDetailRoute(line.product.id)) },
                    )
                    Spacer(Modifier.height(12.dp))
                }
                Spacer(Modifier.height(spacing.sm))
                SkinthesiaCard {
                    if (bag.subtotal < CartSummary.FREE_SHIPPING_THRESHOLD) {
                        Text(
                            text = "Add ${formatPrice(CartSummary.FREE_SHIPPING_THRESHOLD - bag.subtotal)} more for free delivery",
                            style = typography.label,
                            color = colors.textPrimary,
                        )
                        Spacer(Modifier.height(8.dp))
                        LevelBar(fraction = bag.subtotal.toFloat() / CartSummary.FREE_SHIPPING_THRESHOLD, color = colors.success, height = 4.dp)
                        Spacer(Modifier.height(14.dp))
                    }
                    KeyValueRow(label = "Subtotal", value = formatPrice(bag.subtotal))
                    KeyValueRow(label = "Delivery", value = if (bag.shipping == 0) "Free" else formatPrice(bag.shipping))
                    SkinthesiaDivider(Modifier.padding(vertical = 6.dp))
                    KeyValueRow(label = "Total", value = formatPrice(bag.total), emphasize = true)
                }
            }
        }
    }
}

@Composable
private fun CartLineCard(line: CartLine, onQuantity: (Int) -> Unit, onOpen: () -> Unit) {
    val colors = SkinthesiaTheme.colors
    val typography = SkinthesiaTheme.typography
    SkinthesiaCard(onClick = onOpen) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            ProductThumb(line.product, size = 64.dp)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(text = line.product.brand, style = typography.caption, color = colors.textMuted)
                Text(text = line.product.name, style = typography.label, color = colors.textPrimary, maxLines = 2, overflow = TextOverflow.Ellipsis)
                Spacer(Modifier.height(4.dp))
                Text(text = formatPrice(line.total), style = typography.numeric, color = colors.textPrimary)
            }
            Spacer(Modifier.width(8.dp))
            QuantityStepper(quantity = line.quantity, onChange = onQuantity, min = 0, max = 9)
        }
    }
}

data class CheckoutUiState(
    val summary: CartSummary? = null,
    val delivery: DeliveryDetails = DeliveryDetails("", "", "", "", ""),
    val placing: Boolean = false,
    val attempted: Boolean = false,
)

class CheckoutViewModel(
    private val cart: CartRepository,
    catalog: ProductCatalogRepository,
    private val orders: OrderRepository,
    profiles: UserProfileRepository,
) : ViewModel() {

    private val form = MutableStateFlow(CheckoutUiState())

    val state: StateFlow<CheckoutUiState> = combine(form, bagSummary(cart, catalog)) { f, bag -> f.copy(summary = bag) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), CheckoutUiState())

    init {
        viewModelScope.launch {
            val name = profiles.current().firstName
            form.update { if (it.delivery.fullName.isBlank()) it.copy(delivery = it.delivery.copy(fullName = name)) else it }
        }
    }

    fun edit(transform: (DeliveryDetails) -> DeliveryDetails) = form.update { it.copy(delivery = transform(it.delivery)) }

    /** Records a simulated order on this device. No payment provider is involved. */
    fun place(onPlaced: (String) -> Unit) {
        val current = state.value
        val bag = current.summary ?: return
        if (current.placing) return
        if (!current.delivery.isComplete || bag.lines.isEmpty()) {
            form.update { it.copy(attempted = true) }
            return
        }
        form.update { it.copy(placing = true) }
        viewModelScope.launch {
            val order = orders.placeOrder(bag.lines, current.delivery)
            cart.clear()
            form.update { it.copy(placing = false) }
            onPlaced(order.id)
        }
    }
}

/** Simulated checkout: delivery details and a clearly labelled demonstration payment step. */
@Composable
fun CheckoutScreen() {
    val navigator = LocalAppNavigator.current
    val viewModel = containerViewModel { CheckoutViewModel(cart, catalog, orders, profiles) }
    val state by viewModel.state.collectAsStateWithLifecycle()
    val colors = SkinthesiaTheme.colors
    val typography = SkinthesiaTheme.typography
    val spacing = SkinthesiaTheme.spacing
    val delivery = state.delivery
    val bag = state.summary
    val showErrors = state.attempted

    SkinthesiaScreen(
        topBar = { SkinthesiaTopBar(title = "Checkout", onBack = navigator::back) },
        bottomBar = {
            SkinthesiaPrimaryButton(
                text = "Place Simulated Order" + (bag?.let { " · " + formatPrice(it.total) } ?: ""),
                loading = state.placing,
                enabled = bag != null && bag.lines.isNotEmpty() && !state.placing,
                onClick = { viewModel.place { id -> navigator.replace(OrderConfirmationRoute(id)) } },
            )
        },
    ) {
        SectionOverline(text = "Delivery details")
        Spacer(Modifier.height(12.dp))
        SkinthesiaTextField(
            value = delivery.fullName,
            onValueChange = { v -> viewModel.edit { it.copy(fullName = v) } },
            label = "Full name",
            errorText = if (showErrors && delivery.fullName.isBlank()) "Enter a name for delivery" else null,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
        )
        Spacer(Modifier.height(12.dp))
        SkinthesiaTextField(
            value = delivery.phone,
            onValueChange = { v -> viewModel.edit { it.copy(phone = v.filter { c -> c.isDigit() || c == ' ' || c == '+' }.take(16)) } },
            label = "Phone",
            errorText = if (showErrors && delivery.phone.count { it.isDigit() } < 10) "Enter a 10-digit phone number" else null,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone, imeAction = ImeAction.Next),
        )
        Spacer(Modifier.height(12.dp))
        SkinthesiaTextField(
            value = delivery.addressLine,
            onValueChange = { v -> viewModel.edit { it.copy(addressLine = v) } },
            label = "Address",
            errorText = if (showErrors && delivery.addressLine.isBlank()) "Enter a delivery address" else null,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
        )
        Spacer(Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            SkinthesiaTextField(
                value = delivery.city,
                onValueChange = { v -> viewModel.edit { it.copy(city = v) } },
                label = "City",
                errorText = if (showErrors && delivery.city.isBlank()) "Required" else null,
                modifier = Modifier.weight(1f),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
            )
            SkinthesiaTextField(
                value = delivery.postalCode,
                onValueChange = { v -> viewModel.edit { it.copy(postalCode = v.filter { c -> c.isDigit() }.take(6)) } },
                label = "PIN code",
                errorText = if (showErrors && delivery.postalCode.length != 6) "6 digits" else null,
                modifier = Modifier.weight(1f),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
            )
        }

        Spacer(Modifier.height(spacing.lg))
        SectionOverline(text = "Payment")
        Spacer(Modifier.height(12.dp))
        SkinthesiaCard(containerColor = colors.goldSoft, borderColor = Color.Transparent) {
            Row(verticalAlignment = Alignment.Top) {
                Icon(SkinthesiaIcons.Lock, contentDescription = null, tint = colors.goldStrong, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(12.dp))
                Column {
                    Text(text = "Simulated payment", style = typography.labelLarge, color = colors.goldStrong)
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "This demonstration build isn't connected to a payment provider. Placing an order records it on this device only. No money is taken and nothing is shipped.",
                        style = typography.bodySmall,
                        color = colors.goldStrong,
                    )
                }
            }
        }

        if (bag != null && bag.lines.isNotEmpty()) {
            Spacer(Modifier.height(spacing.lg))
            SectionOverline(text = "Order summary")
            Spacer(Modifier.height(12.dp))
            SkinthesiaCard {
                bag.lines.forEach { line ->
                    Row(Modifier.fillMaxWidth().padding(vertical = 5.dp).semantics(mergeDescendants = true) {}) {
                        Text(text = "${line.quantity} × ${line.product.name}", style = typography.body, color = colors.textPrimary, modifier = Modifier.weight(1f))
                        Spacer(Modifier.width(8.dp))
                        Text(text = formatPrice(line.total), style = typography.numeric, color = colors.textPrimary)
                    }
                }
                SkinthesiaDivider(Modifier.padding(vertical = 8.dp))
                KeyValueRow(label = "Subtotal", value = formatPrice(bag.subtotal))
                KeyValueRow(label = "Delivery", value = if (bag.shipping == 0) "Free" else formatPrice(bag.shipping))
                KeyValueRow(label = "Total", value = formatPrice(bag.total), emphasize = true)
            }
        }
        Spacer(Modifier.height(spacing.md))
        Text(
            text = "Your details stay on this device and are used only for this simulated order.",
            style = typography.caption,
            color = colors.textMuted,
        )
    }
}
