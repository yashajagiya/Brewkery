package com.example.brewkery

import com.example.brewkery.data.local.OrderEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class OrderStatusTest {

    @Test
    fun testOrderStatusPreparing() {
        val now = System.currentTimeMillis()
        val order = OrderEntity(
            ticketId = "BK-1001",
            itemsSummaryJson = "[]",
            itemCount = 2,
            subtotal = 10.0,
            deliveryFee = 2.50,
            tax = 0.80,
            totalPrice = 13.30,
            status = "PREPARING",
            deliveryTimeMinutes = 20,
            timestamp = now - (5 * 60000L) // 5 mins ago
        )

        val deliveryTime = order.timestamp + (order.deliveryTimeMinutes * 60000L)
        val remainingMillis = deliveryTime - now
        val remainingMins = (remainingMillis / 60000L).toInt().coerceAtLeast(1)
        val status = if (remainingMins > 5) "PREPARING" else "OUT FOR DELIVERY"

        assertEquals(15, remainingMins)
        assertEquals("PREPARING", status)
    }

    @Test
    fun testOrderStatusOutForDelivery() {
        val now = System.currentTimeMillis()
        val order = OrderEntity(
            ticketId = "BK-1002",
            itemsSummaryJson = "[]",
            itemCount = 1,
            subtotal = 5.0,
            deliveryFee = 2.50,
            tax = 0.40,
            totalPrice = 7.90,
            status = "PREPARING",
            deliveryTimeMinutes = 20,
            timestamp = now - (17 * 60000L) // 17 mins ago (3 mins remaining)
        )

        val deliveryTime = order.timestamp + (order.deliveryTimeMinutes * 60000L)
        val remainingMillis = deliveryTime - now
        val remainingMins = (remainingMillis / 60000L).toInt().coerceAtLeast(1)
        val status = if (remainingMins > 5) "PREPARING" else "OUT FOR DELIVERY"

        assertEquals(3, remainingMins)
        assertEquals("OUT FOR DELIVERY", status)
    }

    @Test
    fun testOrderStatusDelivered() {
        val now = System.currentTimeMillis()
        val order = OrderEntity(
            ticketId = "BK-1003",
            itemsSummaryJson = "[]",
            itemCount = 1,
            subtotal = 5.0,
            deliveryFee = 2.50,
            tax = 0.40,
            totalPrice = 7.90,
            status = "PREPARING",
            deliveryTimeMinutes = 20,
            timestamp = now - (22 * 60000L) // 22 mins ago (delivered 2 mins ago)
        )

        val deliveryTime = order.timestamp + (order.deliveryTimeMinutes * 60000L)
        val isDelivered = now >= deliveryTime
        val status = if (isDelivered) "DELIVERED" else "PREPARING"

        assertTrue(isDelivered)
        assertEquals("DELIVERED", status)
    }

    @Test
    fun testActiveSliderRetentionWindow() {
        val now = System.currentTimeMillis()
        val deliveryMins = 20

        // Order A: Delivered 1 minute ago (within 2-minute post-delivery window) -> Should remain in slider
        val orderA = OrderEntity(
            ticketId = "BK-2001",
            itemsSummaryJson = "[]",
            itemCount = 1, subtotal = 5.0, deliveryFee = 2.5, tax = 0.4, totalPrice = 7.9,
            status = "DELIVERED", deliveryTimeMinutes = deliveryMins,
            timestamp = now - (21 * 60000L) // 21 mins elapsed -> delivered 1 min ago
        )

        // Order B: Delivered 3 minutes ago (exceeds 2-minute window) -> Should be removed from slider
        val orderB = OrderEntity(
            ticketId = "BK-2002",
            itemsSummaryJson = "[]",
            itemCount = 1, subtotal = 5.0, deliveryFee = 2.5, tax = 0.4, totalPrice = 7.9,
            status = "DELIVERED", deliveryTimeMinutes = deliveryMins,
            timestamp = now - (23 * 60000L) // 23 mins elapsed -> delivered 3 mins ago
        )

        fun isSliderActive(order: OrderEntity): Boolean {
            val deliveryTime = order.timestamp + (order.deliveryTimeMinutes * 60000L)
            val removalTime = deliveryTime + (2 * 60000L) // 2 mins post-delivery
            return now < removalTime
        }

        assertTrue(isSliderActive(orderA))
        assertFalse(isSliderActive(orderB))
    }
}
