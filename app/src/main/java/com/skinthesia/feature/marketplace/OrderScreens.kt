package com.skinthesia.feature.marketplace

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.skinthesia.core.design.SkinthesiaTheme
import com.skinthesia.core.navigation.LocalAppNavigator
import com.skinthesia.core.navigation.MarketplaceRoute
import com.skinthesia.core.navigation.OrderConfirmationRoute
import com.skinthesia.core.navigation.OrdersRoute
import com.skinthesia.core.navigation.containerViewModel
import com.skinthesia.core.ui.components.EmptyState
import com.skinthesia.core.ui.components.FadeInUp
import com.skinthesia.core.ui.components.KeyValueRow
import com.skinthesia.core.ui.components.LoadingState
import com.skinthesia.core.ui.components.ScreenHeader
import com.skinthesia.core.ui.components.SectionOverline
import com.skinthesia.core.ui.components.SkinthesiaCard
import com.skinthesia.core.ui.components.SkinthesiaDivider
import com.skinthesia.core.ui.components.SkinthesiaPrimaryButton
import com.skinthesia.core.ui.components.SkinthesiaScreen
import com.skinthesia.core.ui.components.SkinthesiaTextButton
import com.skinthesia.core.ui.components.SkinthesiaTopBar
import com.skinthesia.core.ui.components.Tag
import com.skinthesia.core.ui.components.TagTone
import com.skinthesia.core.ui.formatDay
import com.skinthesia.core.ui.formatPrice
import com.skinthesia.core.ui.icons.SkinthesiaIcons
import com.skinthesia.domain.model.Order
import com.skinthesia.domain.repository.OrderRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class OrderConfirmationViewModel(val orderId: String, private val orders: OrderRepository) : ViewModel() {
    private val _order = MutableStateFlow<Order?>(null)
    val order: StateFlow<Order?> = _order.asStateFlow()
    private val _loading = MutableStateFlow(true)
    val loading: StateFlow<Boolean> = _loading.asStateFlow()

    init {
        viewModelScope.launch {
            _order.value = orders.order(orderId)
            _loading.value = false
        }
    }
}

/** Order confirmation, clearly marked as a simulated order. */
@Composable
fun OrderConfirmationScreen() {
    val navigator = LocalAppNavigator.current
    val viewModel = containerViewModel { handle -> OrderConfirmationViewModel(handle.toRoute<OrderConfirmationRoute>().orderId, orders) }
    val order by viewModel.order.collectAsStateWithLifecycle()
    val loading by viewModel.loading.collectAsStateWithLifecycle()
    val colors = SkinthesiaTheme.colors
    val typography = SkinthesiaTheme.typography
    val spacing = SkinthesiaTheme.spacing
    BackHandler { navigator.goHome() }

    SkinthesiaScreen(
        topBar = { SkinthesiaTopBar(title = "Order placed", onBack = navigator::goHome) },
        bottomBar = {
            SkinthesiaPrimaryButton(text = "Back to Home", onClick = navigator::goHome)
            SkinthesiaTextButton(text = "View my orders", onClick = { navigator.navigate(OrdersRoute) })
        },
    ) {
        val placed = order
        when {
            loading -> LoadingState(message = "Confirming your order")
            placed == null -> ScreenHeader(title = "Order not found", centered = true)
            else -> {
                FadeInUp {
                    Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            Modifier.size(68.dp).clip(CircleShape).background(colors.successSoft),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(SkinthesiaIcons.Check, contentDescription = null, tint = colors.success, modifier = Modifier.size(30.dp))
                        }
                        Spacer(Modifier.height(spacing.md))
                        Text(text = "Thank you", style = typography.title, color = colors.textPrimary)
                        Spacer(Modifier.height(6.dp))
                        Text(text = "Order ${placed.number}", style = typography.subtitle, color = colors.textSecondary)
                        Spacer(Modifier.height(10.dp))
                        if (placed.isSimulated) Tag(text = "Simulated order", tone = TagTone.GOLD, dot = true)
                    }
                }
                Spacer(Modifier.height(spacing.lg))
                OrderSummaryCard(placed)
                Spacer(Modifier.height(spacing.md))
                SkinthesiaCard {
                    SectionOverline(text = "Delivering to")
                    Spacer(Modifier.height(6.dp))
                    Text(text = placed.deliveryName, style = typography.labelLarge, color = colors.textPrimary)
                    Text(text = placed.deliveryAddress, style = typography.bodySmall, color = colors.textSecondary)
                }
                Spacer(Modifier.height(spacing.md))
                Text(
                    text = "This is a demonstration: no payment was taken and nothing will be shipped.",
                    style = typography.caption,
                    color = colors.textMuted,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}

@Composable
private fun OrderSummaryCard(order: Order) {
    val colors = SkinthesiaTheme.colors
    val typography = SkinthesiaTheme.typography
    SkinthesiaCard {
        order.lines.forEach { line ->
            Row(Modifier.fillMaxWidth().padding(vertical = 5.dp).semantics(mergeDescendants = true) {}) {
                Text(text = "${line.quantity} × ${line.name}", style = typography.body, color = colors.textPrimary, modifier = Modifier.weight(1f))
                Spacer(Modifier.width(8.dp))
                Text(text = formatPrice(line.unitPrice * line.quantity), style = typography.numeric, color = colors.textPrimary)
            }
        }
        SkinthesiaDivider(Modifier.padding(vertical = 8.dp))
        KeyValueRow(label = "Subtotal", value = formatPrice(order.subtotal))
        KeyValueRow(label = "Delivery", value = if (order.shipping == 0) "Free" else formatPrice(order.shipping))
        KeyValueRow(label = "Total", value = formatPrice(order.total), emphasize = true)
    }
}

class OrdersViewModel(orders: OrderRepository) : ViewModel() {
    val orders: StateFlow<List<Order>?> = orders.orders
        .map { list -> list.sortedByDescending { it.placedAt } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)
}

/** Order history. Every order in this build is simulated and labelled as such. */
@Composable
fun OrdersScreen() {
    val navigator = LocalAppNavigator.current
    val viewModel = containerViewModel { OrdersViewModel(orders) }
    val list by viewModel.orders.collectAsStateWithLifecycle()
    val colors = SkinthesiaTheme.colors
    val typography = SkinthesiaTheme.typography

    SkinthesiaScreen(topBar = { SkinthesiaTopBar(title = "Your orders", onBack = navigator::back) }) {
        val orders = list
        when {
            orders == null -> LoadingState(message = "Loading orders")
            orders.isEmpty() -> EmptyState(
                icon = SkinthesiaIcons.Receipt,
                title = "No orders yet",
                body = "Orders you place in the shop appear here.",
                actionLabel = "Browse products",
                onAction = { navigator.navigate(MarketplaceRoute()) },
            )
            else -> orders.forEach { order ->
                SkinthesiaCard(onClick = { navigator.navigate(OrderConfirmationRoute(order.id)) }) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text(text = "Order ${order.number}", style = typography.labelLarge, color = colors.textPrimary)
                            Text(
                                text = formatDay(order.placedAt) + " · " + order.lines.sumOf { it.quantity } + " items",
                                style = typography.caption,
                                color = colors.textMuted,
                            )
                        }
                        Text(text = formatPrice(order.total), style = typography.numeric, color = colors.textPrimary)
                    }
                    Spacer(Modifier.height(10.dp))
                    Row {
                        Tag(text = order.status.label, tone = TagTone.SAGE)
                        if (order.isSimulated) {
                            Spacer(Modifier.width(8.dp))
                            Tag(text = "Simulated", tone = TagTone.GOLD, dot = true)
                        }
                    }
                }
                Spacer(Modifier.height(12.dp))
            }
        }
    }
}
