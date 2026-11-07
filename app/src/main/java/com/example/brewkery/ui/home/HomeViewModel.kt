package com.example.brewkery.ui.home

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.brewkery.data.local.BrewkeryDatabase
import com.example.brewkery.data.local.OrderEntity
import com.example.brewkery.data.model.CategoryDto
import com.example.brewkery.data.model.CoffeeItemDto
import com.example.brewkery.data.model.MetaDto
import com.example.brewkery.data.repository.BrewkeryRepository
import com.example.brewkery.util.NetworkMonitor
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

enum class ScreenState {
    LOADING,
    SUCCESS,
    FAIL,
    NO_INTERNET
}

data class HomeUiState(
    val screenState: ScreenState = ScreenState.LOADING,
    val meta: MetaDto? = null,
    val categories: List<CategoryDto> = emptyList(),
    val allItems: List<CoffeeItemDto> = emptyList(),
    val filteredItems: List<CoffeeItemDto> = emptyList(),
    val selectedCategoryId: String = "ALL",
    val searchQuery: String = "",
    val activeOrders: List<OrderEntity> = emptyList(),
    val allOrders: List<OrderEntity> = emptyList(),
    val totalCartCount: Int = 0,
    val errorMessage: String? = null
)

class HomeViewModel(
    private val repository: BrewkeryRepository,
    private val networkMonitor: NetworkMonitor
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        observeNetwork()
        observeCart()
        observeOrders()
        loadCatalog()
    }

    private fun observeNetwork() {
        viewModelScope.launch {
            networkMonitor.isOnline.collectLatest { isOnline ->
                if (!isOnline && _uiState.value.allItems.isEmpty()) {
                    _uiState.value = _uiState.value.copy(screenState = ScreenState.NO_INTERNET)
                }
            }
        }
    }

    private fun observeCart() {
        viewModelScope.launch {
            repository.getCartItems().collectLatest { cartItems ->
                val totalCount = cartItems.sumOf { it.quantity }
                _uiState.value = _uiState.value.copy(totalCartCount = totalCount)
            }
        }
    }

    private fun observeOrders() {
        viewModelScope.launch {
            repository.getOrders().collectLatest { rawOrders ->
                processOrders(rawOrders)

                while (true) {
                    kotlinx.coroutines.delay(5000L)
                    processOrders(rawOrders)
                }
            }
        }
    }

    private suspend fun processOrders(rawOrders: List<OrderEntity>) {
        val now = System.currentTimeMillis()
        val processedOrders = rawOrders.map { order ->
            val deliveryTime = order.timestamp + (order.deliveryTimeMinutes * 60000L)
            if (now >= deliveryTime && order.status != "DELIVERED") {
                repository.updateOrderStatus(order.ticketId, "DELIVERED")
                order.copy(status = "DELIVERED")
            } else {
                order
            }
        }

        val sliderActiveOrders = processedOrders.filter { order ->
            val deliveryTime = order.timestamp + (order.deliveryTimeMinutes * 60000L)
            val removalTime = deliveryTime + (2 * 60000L) // Hide from slider 2 mins after delivery
            now < removalTime
        }

        _uiState.value = _uiState.value.copy(
            allOrders = processedOrders,
            activeOrders = sliderActiveOrders
        )
    }

    fun loadCatalog() {
        viewModelScope.launch {
            if (!networkMonitor.checkCurrentConnectivity() && _uiState.value.allItems.isEmpty()) {
                _uiState.value = _uiState.value.copy(screenState = ScreenState.NO_INTERNET)
                return@launch
            }

            _uiState.value = _uiState.value.copy(screenState = ScreenState.LOADING)

            val result = repository.fetchCatalog()

            result.fold(
                onSuccess = { response ->
                    val items = response.items
                    _uiState.value = _uiState.value.copy(
                        screenState = ScreenState.SUCCESS,
                        meta = response.meta,
                        categories = response.categories,
                        allItems = items,
                        filteredItems = applyFilters(items, _uiState.value.selectedCategoryId, _uiState.value.searchQuery)
                    )
                },
                onFailure = { error ->
                    _uiState.value = _uiState.value.copy(
                        screenState = ScreenState.FAIL,
                        errorMessage = error.message
                    )
                }
            )
        }
    }

    fun selectCategory(categoryId: String) {
        _uiState.value = _uiState.value.copy(
            selectedCategoryId = categoryId,
            filteredItems = applyFilters(_uiState.value.allItems, categoryId, _uiState.value.searchQuery)
        )
    }

    fun updateSearchQuery(query: String) {
        _uiState.value = _uiState.value.copy(
            searchQuery = query,
            filteredItems = applyFilters(_uiState.value.allItems, _uiState.value.selectedCategoryId, query)
        )
    }

    private fun applyFilters(
        items: List<CoffeeItemDto>,
        categoryId: String,
        query: String
    ): List<CoffeeItemDto> {
        return items.filter { item ->
            val matchesCategory = (categoryId == "ALL") || (item.categoryId == categoryId)
            val matchesQuery = query.isBlank() ||
                    item.name.contains(query, ignoreCase = true) ||
                    item.tagline.contains(query, ignoreCase = true) ||
                    item.description.contains(query, ignoreCase = true)
            matchesCategory && matchesQuery
        }
    }

    class Factory(private val context: Context) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            val db = BrewkeryDatabase.getInstance(context)
            val repo = BrewkeryRepository(db)
            val net = NetworkMonitor(context)
            return HomeViewModel(repo, net) as T
        }
    }
}
