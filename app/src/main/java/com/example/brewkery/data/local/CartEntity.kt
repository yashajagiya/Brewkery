package com.example.brewkery.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cart_table")
data class CartEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val itemId: Int,
    val name: String,
    val imageUrl: String,
    val selectedSize: String,
    val selectedSizePrice: Double,
    val selectedMilk: String,
    val selectedMilkPrice: Double,
    val selectedSugar: String,
    val basePrice: Double,
    val quantity: Int,
    val customizationHash: String
)
