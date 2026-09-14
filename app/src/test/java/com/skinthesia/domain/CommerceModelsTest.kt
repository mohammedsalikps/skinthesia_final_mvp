package com.skinthesia.domain

import com.skinthesia.domain.model.CartLine
import com.skinthesia.domain.model.CartSummary
import com.skinthesia.domain.model.DeliveryDetails
import com.skinthesia.domain.model.Product
import com.skinthesia.domain.model.ProductCategory
import com.skinthesia.domain.model.ProductForm
import com.skinthesia.domain.model.ProductTone
import com.skinthesia.domain.model.RoutineTime
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CommerceModelsTest {

    private fun product(price: Int) = Product(
        id = "p$price",
        name = "Test serum",
        brand = "Test brand",
        category = ProductCategory.SERUM,
        form = ProductForm.DROPPER,
        tone = ProductTone.IVORY,
        size = "30 ml",
        price = price,
        description = "",
        howToUse = "",
        keyIngredients = emptyList(),
        fullIngredients = "",
        supportsGoals = emptySet(),
        skinTypes = emptySet(),
        attributes = emptySet(),
        routineTimes = setOf(RoutineTime.MORNING),
        rating = 4.5f,
        reviewCount = 10,
    )

    @Test
    fun `delivery is charged below the free threshold`() {
        val summary = CartSummary(listOf(CartLine(product(850), 1)))
        assertEquals(850, summary.subtotal)
        assertEquals(CartSummary.SHIPPING_FEE, summary.shipping)
        assertEquals(850 + CartSummary.SHIPPING_FEE, summary.total)
    }

    @Test
    fun `delivery is free from the threshold upwards`() {
        val summary = CartSummary(listOf(CartLine(product(CartSummary.FREE_SHIPPING_THRESHOLD / 2), 2)))
        assertEquals(CartSummary.FREE_SHIPPING_THRESHOLD, summary.subtotal)
        assertEquals(0, summary.shipping)
        assertEquals(2, summary.itemCount)
    }

    @Test
    fun `an empty bag has no delivery charge`() {
        val summary = CartSummary(emptyList())
        assertEquals(0, summary.shipping)
        assertEquals(0, summary.total)
    }

    @Test
    fun `delivery details need a name, address, city, 10-digit phone and 6-digit PIN`() {
        val complete = DeliveryDetails("Asha", "98765 43210", "12 Lake View Road", "Kochi", "682001")
        assertTrue(complete.isComplete)
        assertFalse(complete.copy(phone = "12345").isComplete)
        assertFalse(complete.copy(postalCode = "6820").isComplete)
        assertFalse(complete.copy(addressLine = " ").isComplete)
        assertFalse(complete.copy(fullName = "").isComplete)
    }
}
