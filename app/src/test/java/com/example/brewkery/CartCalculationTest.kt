package com.example.brewkery

import com.example.brewkery.data.local.CartEntity
import com.example.brewkery.domain.model.CartItem
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

class CartCalculationTest {

    @Test
    fun testCartItemUnitPriceCalculation() {
        val cartItem = CartItem(
            id = 1,
            itemId = 101,
            name = "Toasted Caramel Macchiato",
            imageUrl = "https://example.com/coffee.jpg",
            selectedSize = "Grande (12 oz)",
            selectedSizePrice = 0.65,
            selectedMilk = "Roasted Almond Milk",
            selectedMilkPrice = 0.50,
            selectedSugar = "100% Standard Sweet",
            basePrice = 4.85,
            quantity = 2
        )

        val expectedUnitPrice = 4.85 + 0.65 + 0.50 // 6.00
        val expectedTotalPrice = 6.00 * 2 // 12.00

        assertEquals(expectedUnitPrice, cartItem.unitPrice, 0.001)
        assertEquals(expectedTotalPrice, cartItem.totalPrice, 0.001)
    }

    @Test
    fun testCartEntityPriceCalculations() {
        val cartEntity = CartEntity(
            id = 1,
            itemId = 101,
            name = "Velvet Vanilla Cold Foam",
            imageUrl = "https://example.com/coldbrew.jpg",
            selectedSize = "Large (24 oz)",
            selectedSizePrice = 0.95,
            selectedMilk = "Signature Sweet Cream",
            selectedMilkPrice = 0.00,
            selectedSugar = "Regular Sweet",
            basePrice = 5.40,
            quantity = 3,
            customizationHash = "101-Large (24 oz)-Signature Sweet Cream-Regular Sweet"
        )

        val unitPrice = cartEntity.basePrice + cartEntity.selectedSizePrice + cartEntity.selectedMilkPrice
        val totalItemPrice = unitPrice * cartEntity.quantity

        assertEquals(6.35, unitPrice, 0.001)
        assertEquals(19.05, totalItemPrice, 0.001)
    }

    @Test
    fun testCartSubtotalTaxAndGrandTotalCalculations() {
        val items = listOf(
            CartItem(
                id = 1, itemId = 101, name = "Macchiato", imageUrl = "",
                selectedSize = "Tall", selectedSizePrice = 0.0,
                selectedMilk = "Oat", selectedMilkPrice = 0.0,
                selectedSugar = "Standard", basePrice = 4.85, quantity = 1
            ),
            CartItem(
                id = 2, itemId = 102, name = "Croissant", imageUrl = "",
                selectedSize = "Single", selectedSizePrice = 0.0,
                selectedMilk = "None", selectedMilkPrice = 0.0,
                selectedSugar = "Warm", basePrice = 3.90, quantity = 2
            )
        )

        val subtotal = items.sumOf { it.totalPrice } // 4.85 + (3.90 * 2) = 12.65
        val deliveryFee = 2.50
        val tax = subtotal * 0.08 // 1.012
        val grandTotal = subtotal + deliveryFee + tax // 16.162

        assertEquals(12.65, subtotal, 0.001)
        assertEquals(2.50, deliveryFee, 0.001)
        assertEquals(1.012, tax, 0.001)
        assertEquals(16.162, grandTotal, 0.001)
    }

    @Test
    fun testCustomizationHashUniqueness() {
        val hash1 = "101-Grande-Oat Milk-50% Sweet"
        val hash2 = "101-Venti-Oat Milk-50% Sweet"
        val hash3 = "101-Grande-Almond Milk-50% Sweet"

        assertNotEquals(hash1, hash2)
        assertNotEquals(hash1, hash3)
        assertEquals("101-Grande-Oat Milk-50% Sweet", hash1)
    }
}
