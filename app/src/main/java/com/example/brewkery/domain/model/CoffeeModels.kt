package com.example.brewkery.domain.model

data class Category(
    val id: String,
    val name: String,
    val icon: String,
    val itemCount: Int
)

data class CoffeeItem(
    val id: Int,
    val categoryId: String,
    val name: String,
    val tagline: String,
    val description: String,
    val basePrice: Double,
    val rating: Double,
    val reviewCount: Int,
    val prepTime: String,
    val calories: Int,
    val imageUrl: String,
    val badge: String?,
    val ingredients: List<String>,
    val sizes: List<SizeOption>,
    val sugarLevels: List<String>,
    val milkOptions: List<MilkOption>
)

data class SizeOption(
    val id: String,
    val label: String,
    val extraPrice: Double
)

data class MilkOption(
    val id: String,
    val name: String,
    val extraPrice: Double
)

data class CartItem(
    val id: Int,
    val itemId: Int,
    val name: String,
    val imageUrl: String,
    val selectedSize: String,
    val selectedSizePrice: Double,
    val selectedMilk: String,
    val selectedMilkPrice: Double,
    val selectedSugar: String,
    val basePrice: Double,
    val quantity: Int
) {
    val unitPrice: Double
        get() = basePrice + selectedSizePrice + selectedMilkPrice

    val totalPrice: Double
        get() = unitPrice * quantity
}

enum class OrderStatus {
    PREPARING,
    OUT_FOR_DELIVERY,
    DELIVERED
}

data class Order(
    val ticketId: String,
    val itemsSummary: String,
    val itemCount: Int,
    val subtotal: Double,
    val deliveryFee: Double,
    val tax: Double,
    val totalPrice: Double,
    val status: OrderStatus,
    val deliveryTimeMinutes: Int,
    val timestamp: Long
)
