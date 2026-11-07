package com.example.brewkery.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "orders_table")
data class OrderEntity(
    @PrimaryKey val ticketId: String,
    val itemsSummaryJson: String,
    val itemCount: Int,
    val subtotal: Double,
    val deliveryFee: Double,
    val tax: Double,
    val totalPrice: Double,
    val status: String,
    val deliveryTimeMinutes: Int,
    val timestamp: Long
)
