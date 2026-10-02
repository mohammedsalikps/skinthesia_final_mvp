package com.skinthesia.domain.model

import kotlinx.serialization.Serializable

@Serializable
enum class ProductCategory(val label: String, val plural: String) {
    CLEANSER("Cleanser", "Cleansers"),
    SERUM("Serum", "Serums"),
    MOISTURIZER("Moisturizer", "Moisturizers"),
    SUNSCREEN("Sunscreen", "Sunscreens"),
    TREATMENT("Treatment", "Treatments"),
    /** The Skinthesia hardware itself - not a topical product; see [ProductVisual]. */
    PRO("Probe", "Probe"),
    FACE_MASK("Face Mask", "Face Masks"),
    HAND_GLOVES("Gloves", "Gloves"),
}

/** Packaging shape, used to draw the product artwork. */
@Serializable
enum class ProductForm { DROPPER, PUMP, TUBE, JAR, BOTTLE }

/** Palette used by the product artwork. */
@Serializable
enum class ProductTone { IVORY, BLUSH, SAGE, CLAY, AMBER, MIST, SAND }

@Serializable
enum class ProductAttribute(val label: String) {
    FRAGRANCE_FREE("Fragrance-free"),
    VEGAN("Vegan"),
    CRUELTY_FREE("Cruelty-free"),
    NON_COMEDOGENIC("Non-comedogenic"),
    GENTLE("Gentle formula"),
    LIGHTWEIGHT("Lightweight"),
    RICH("Rich texture"),
    BROAD_SPECTRUM("Broad spectrum"),
}

@Serializable
data class Ingredient(
    val name: String,
    /** What it does, in plain words. */
    val role: String,
)

/** A catalogue product. The local catalogue can be replaced by a marketplace API later. */
@Serializable
data class Product(
    val id: String,
    val name: String,
    val brand: String,
    val category: ProductCategory,
    val form: ProductForm,
    val tone: ProductTone,
    val size: String,
    /** Price in whole rupees. */
    val price: Int,
    val description: String,
    val howToUse: String,
    val keyIngredients: List<Ingredient>,
    val fullIngredients: String,
    val supportsGoals: Set<SkinGoal>,
    val skinTypes: Set<SkinType>,
    val attributes: Set<ProductAttribute>,
    val routineTimes: Set<RoutineTime>,
    val rating: Float,
    val reviewCount: Int,
) {
    val budget: Budget
        get() = when {
            price < Budget.ACCESSIBLE.maxPerProduct -> Budget.ACCESSIBLE
            price <= Budget.MID_RANGE.maxPerProduct -> Budget.MID_RANGE
            else -> Budget.PREMIUM
        }
}

@Serializable
data class CartItem(
    val productId: String,
    val quantity: Int,
    val addedAt: Long,
)

data class CartLine(val product: Product, val quantity: Int) {
    val total: Int get() = product.price * quantity
}

data class CartSummary(val lines: List<CartLine>) {
    val itemCount: Int get() = lines.sumOf { it.quantity }
    val subtotal: Int get() = lines.sumOf { it.total }
    val shipping: Int get() = if (subtotal == 0 || subtotal >= FREE_SHIPPING_THRESHOLD) 0 else SHIPPING_FEE
    val total: Int get() = subtotal + shipping

    companion object {
        const val FREE_SHIPPING_THRESHOLD = 1500
        const val SHIPPING_FEE = 99
    }
}

@Serializable
data class OrderLine(
    val productId: String,
    val name: String,
    val quantity: Int,
    val unitPrice: Int,
)

@Serializable
enum class OrderStatus(val label: String) {
    CONFIRMED("Confirmed"),
    CANCELLED("Cancelled"),
}

/** A simulated order. No payment is taken in the demonstration build. */
@Serializable
data class Order(
    val id: String,
    val number: String,
    val lines: List<OrderLine>,
    val subtotal: Int,
    val shipping: Int,
    val total: Int,
    val placedAt: Long,
    val deliveryName: String,
    val deliveryAddress: String,
    val status: OrderStatus,
    val isSimulated: Boolean = true,
)

data class DeliveryDetails(
    val fullName: String,
    val phone: String,
    val addressLine: String,
    val city: String,
    val postalCode: String,
) {
    val isComplete: Boolean
        get() = fullName.isNotBlank() && phone.count { it.isDigit() } >= 10 &&
            addressLine.isNotBlank() && city.isNotBlank() && postalCode.count { it.isDigit() } == 6
}
