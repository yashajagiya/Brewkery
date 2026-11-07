package com.example.brewkery.ui.cart

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.brewkery.data.local.BrewkeryDatabase
import com.example.brewkery.data.local.CartEntity
import com.example.brewkery.data.local.OrderEntity
import com.example.brewkery.data.repository.BrewkeryRepository
import com.google.gson.Gson
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlin.random.Random

data class CartUiState(
    val cartItems: List<CartEntity> = emptyList(),
    val isLoading: Boolean = false
) {
    val subtotal: Double
        get() = cartItems.sumOf { (it.basePrice + it.selectedSizePrice + it.selectedMilkPrice) * it.quantity }

    val deliveryFee: Double
        get() = if (cartItems.isEmpty()) 0.0 else 2.50

    val tax: Double
        get() = subtotal * 0.08

    val grandTotal: Double
        get() = subtotal + deliveryFee + tax

    val totalItemCount: Int
        get() = cartItems.sumOf { it.quantity }
}

class CartViewModel(
    private val repository: BrewkeryRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CartUiState())
    val uiState: StateFlow<CartUiState> = _uiState.asStateFlow()

    init {
        observeCart()
    }

    private fun observeCart() {
        viewModelScope.launch {
            repository.getCartItems().collectLatest { items ->
                _uiState.value = _uiState.value.copy(cartItems = items)
            }
        }
    }

    fun incrementQuantity(item: CartEntity) {
        viewModelScope.launch {
            repository.addOrUpdateCartItem(
                itemId = item.itemId,
                name = item.name,
                imageUrl = item.imageUrl,
                size = item.selectedSize,
                sizePrice = item.selectedSizePrice,
                milk = item.selectedMilk,
                milkPrice = item.selectedMilkPrice,
                sugar = item.selectedSugar,
                basePrice = item.basePrice,
                quantity = 1
            )
        }
    }

    fun decrementQuantity(item: CartEntity) {
        viewModelScope.launch {
            if (item.quantity > 1) {
                repository.addOrUpdateCartItem(
                    itemId = item.itemId,
                    name = item.name,
                    imageUrl = item.imageUrl,
                    size = item.selectedSize,
                    sizePrice = item.selectedSizePrice,
                    milk = item.selectedMilk,
                    milkPrice = item.selectedMilkPrice,
                    sugar = item.selectedSugar,
                    basePrice = item.basePrice,
                    quantity = -1
                )
            } else {
                repository.removeCartItem(item.id)
            }
        }
    }

    fun removeItem(id: Int) {
        viewModelScope.launch {
            repository.removeCartItem(id)
        }
    }

    fun clearCart() {
        viewModelScope.launch {
            repository.clearCart()
        }
    }

    fun placeOrder(onOrderPlaced: (String) -> Unit) {
        val current = _uiState.value
        if (current.cartItems.isEmpty()) return

        viewModelScope.launch {
            val orderCount = repository.getOrderCount()
            val totalItems = current.totalItemCount
            val ticketId = "BK-${totalItems}${1001 + orderCount}"

            val deliveryMins = Random.nextInt(5, 16)

            val itemsSummaryList = current.cartItems.map { item ->
                mapOf(
                    "name" to item.name,
                    "size" to item.selectedSize,
                    "milk" to item.selectedMilk,
                    "sugar" to item.selectedSugar,
                    "quantity" to item.quantity,
                    "price" to (item.basePrice + item.selectedSizePrice + item.selectedMilkPrice) * item.quantity
                )
            }
            val jsonSummary = Gson().toJson(itemsSummaryList)

            val newOrder = OrderEntity(
                ticketId = ticketId,
                itemsSummaryJson = jsonSummary,
                itemCount = totalItems,
                subtotal = current.subtotal,
                deliveryFee = current.deliveryFee,
                tax = current.tax,
                totalPrice = current.grandTotal,
                status = "PREPARING",
                deliveryTimeMinutes = deliveryMins,
                timestamp = System.currentTimeMillis()
            )

            repository.saveOrder(newOrder)
            repository.clearCart()
            onOrderPlaced(ticketId)
        }
    }

    class Factory(private val context: Context) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            val db = BrewkeryDatabase.getInstance(context)
            val repo = BrewkeryRepository(db)
            return CartViewModel(repo) as T
        }
    }
}
