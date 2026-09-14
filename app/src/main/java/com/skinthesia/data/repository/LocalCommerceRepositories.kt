package com.skinthesia.data.repository

import com.skinthesia.data.local.SkinthesiaJson
import com.skinthesia.data.local.db.CartItemEntity
import com.skinthesia.data.local.db.CommerceDao
import com.skinthesia.data.local.db.OrderEntity
import com.skinthesia.data.seed.ProductCatalogSeed
import com.skinthesia.domain.model.CartItem
import com.skinthesia.domain.model.CartLine
import com.skinthesia.domain.model.CartSummary
import com.skinthesia.domain.model.DeliveryDetails
import com.skinthesia.domain.model.Ids
import com.skinthesia.domain.model.Order
import com.skinthesia.domain.model.OrderLine
import com.skinthesia.domain.model.OrderStatus
import com.skinthesia.domain.model.Product
import com.skinthesia.domain.repository.CartRepository
import com.skinthesia.domain.repository.OrderRepository
import com.skinthesia.domain.repository.ProductCatalogRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map

/** Bundled catalogue. A marketplace API client implements the same interface later. */
class SeedProductCatalogRepository(
    seed: List<Product> = ProductCatalogSeed.products,
) : ProductCatalogRepository {
    private val state = MutableStateFlow(seed)
    override val products: Flow<List<Product>> = state.asStateFlow()
    override suspend fun all(): List<Product> = state.value
    override suspend fun byId(id: String): Product? = state.value.firstOrNull { it.id == id }
}

class RoomCartRepository(
    private val dao: CommerceDao,
    private val clock: () -> Long = System::currentTimeMillis,
) : CartRepository {

    override val items: Flow<List<CartItem>> = dao.cart().map { list -> list.map { CartItem(it.productId, it.quantity, it.addedAt) } }

    override suspend fun add(productId: String, quantity: Int) {
        val existing = dao.cartItem(productId)
        val next = ((existing?.quantity ?: 0) + quantity).coerceIn(1, MAX_QUANTITY)
        dao.upsertCartItem(CartItemEntity(productId, next, existing?.addedAt ?: clock()))
    }

    override suspend fun setQuantity(productId: String, quantity: Int) {
        if (quantity <= 0) {
            dao.removeCartItem(productId)
            return
        }
        val existing = dao.cartItem(productId)
        dao.upsertCartItem(CartItemEntity(productId, quantity.coerceAtMost(MAX_QUANTITY), existing?.addedAt ?: clock()))
    }

    override suspend fun remove(productId: String) = dao.removeCartItem(productId)

    override suspend fun clear() = dao.clearCart()

    private companion object {
        const val MAX_QUANTITY = 9
    }
}

/**
 * Simulated checkout. Orders are recorded locally and clearly marked simulated; no
 * payment provider is contacted and no card details are ever collected.
 */
class RoomOrderRepository(
    private val dao: CommerceDao,
    private val clock: () -> Long = System::currentTimeMillis,
) : OrderRepository {

    override val orders: Flow<List<Order>> = dao.orders().map { list -> list.mapNotNull { it.toDomain() } }

    override suspend fun placeOrder(lines: List<CartLine>, delivery: DeliveryDetails): Order {
        val summary = CartSummary(lines)
        val count = dao.orderCount() + 1
        val order = Order(
            id = Ids.new("ord"),
            number = "SK-" + (240_000 + count * 37).toString(),
            lines = lines.map { OrderLine(it.product.id, it.product.name, it.quantity, it.product.price) },
            subtotal = summary.subtotal,
            shipping = summary.shipping,
            total = summary.total,
            placedAt = clock(),
            deliveryName = delivery.fullName.trim(),
            deliveryAddress = listOf(delivery.addressLine, delivery.city, delivery.postalCode).joinToString(", ") { it.trim() },
            status = OrderStatus.CONFIRMED,
            isSimulated = true,
        )
        dao.insertOrder(OrderEntity(order.id, order.placedAt, SkinthesiaJson.encodeToString(Order.serializer(), order)))
        return order
    }

    override suspend fun order(id: String): Order? = dao.order(id)?.toDomain()

    override suspend fun deleteAll() = dao.deleteOrders()

    private fun OrderEntity.toDomain(): Order? =
        runCatching { SkinthesiaJson.decodeFromString(Order.serializer(), json) }.getOrNull()
}
