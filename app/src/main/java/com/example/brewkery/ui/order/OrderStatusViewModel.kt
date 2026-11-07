package com.example.brewkery.ui.order

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.brewkery.data.local.BrewkeryDatabase
import com.example.brewkery.data.local.OrderEntity
import com.example.brewkery.data.repository.BrewkeryRepository
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

data class OrderSummaryItem(
    val name: String = "",
    val size: String = "",
    val milk: String = "",
    val sugar: String = "",
    val quantity: Int = 1,
    val price: Double = 0.0
)

data class OrderStatusUiState(
    val isLoading: Boolean = true,
    val order: OrderEntity? = null,
    val itemsSummary: List<OrderSummaryItem> = emptyList(),
    val remainingMinutes: Int = 0,
    val statusText: String = "PREPARING"
)

class OrderStatusViewModel(
    private val ticketId: String,
    private val repository: BrewkeryRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(OrderStatusUiState())
    val uiState: StateFlow<OrderStatusUiState> = _uiState.asStateFlow()

    init {
        loadOrder()
    }

    private fun loadOrder() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val startTime = System.currentTimeMillis()

            repository.getOrders().collectLatest { orders ->
                val targetOrder = orders.find { it.ticketId == ticketId } ?: orders.firstOrNull()

                val elapsedTime = System.currentTimeMillis() - startTime
                if (elapsedTime < 500L) {
                    delay(500L - elapsedTime)
                }

                if (targetOrder != null) {
                    val summaryList: List<OrderSummaryItem> = try {
                        val type = object : TypeToken<List<Map<String, Any>>>() {}.type
                        val rawList: List<Map<String, Any>> = Gson().fromJson(targetOrder.itemsSummaryJson, type)
                        rawList.map { map ->
                            OrderSummaryItem(
                                name = map["name"] as? String ?: "",
                                size = map["size"] as? String ?: "",
                                milk = map["milk"] as? String ?: "",
                                sugar = map["sugar"] as? String ?: "",
                                quantity = (map["quantity"] as? Double)?.toInt() ?: 1,
                                price = map["price"] as? Double ?: 0.0
                            )
                        }
                    } catch (e: Exception) {
                        emptyList()
                    }

                    val deliveryTimeMillis = targetOrder.timestamp + (targetOrder.deliveryTimeMinutes * 60000L)
                    val now = System.currentTimeMillis()

                    val (remaining, status) = if (now >= deliveryTimeMillis || targetOrder.status == "DELIVERED") {
                        if (targetOrder.status != "DELIVERED") {
                            repository.updateOrderStatus(targetOrder.ticketId, "DELIVERED")
                        }
                        Pair(0, "DELIVERED")
                    } else {
                        val remainingMillis = deliveryTimeMillis - now
                        val remMins = ((remainingMillis / 60000L) + 1).toInt().coerceAtLeast(1)
                        val st = if (remMins > 5) "PREPARING" else "OUT FOR DELIVERY"
                        Pair(remMins, st)
                    }

                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        order = targetOrder.copy(status = status),
                        itemsSummary = summaryList,
                        remainingMinutes = remaining,
                        statusText = status
                    )
                } else {
                    _uiState.value = _uiState.value.copy(isLoading = false)
                }
            }
        }
    }

    class Factory(
        private val ticketId: String,
        private val context: Context
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            val db = BrewkeryDatabase.getInstance(context)
            val repo = BrewkeryRepository(db)
            return OrderStatusViewModel(ticketId, repo) as T
        }
    }
}
