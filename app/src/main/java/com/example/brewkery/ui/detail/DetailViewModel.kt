package com.example.brewkery.ui.detail

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.brewkery.data.local.BrewkeryDatabase
import com.example.brewkery.data.model.CoffeeItemDto
import com.example.brewkery.data.model.MilkOptionDto
import com.example.brewkery.data.model.SizeOptionDto
import com.example.brewkery.data.repository.BrewkeryRepository
import com.example.brewkery.ui.home.ScreenState
import com.example.brewkery.util.NetworkMonitor
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class DetailUiState(
    val screenState: ScreenState = ScreenState.LOADING,
    val item: CoffeeItemDto? = null,
    val selectedSize: SizeOptionDto? = null,
    val selectedMilk: MilkOptionDto? = null,
    val selectedSugar: String = "",
    val quantity: Int = 1,
    val isAddedToCart: Boolean = false,
    val errorMessage: String? = null
) {
    val unitPrice: Double
        get() {
            val base = item?.basePrice ?: 0.0
            val sizeExtra = selectedSize?.extraPrice ?: 0.0
            val milkExtra = selectedMilk?.extraPrice ?: 0.0
            return base + sizeExtra + milkExtra
        }

    val subtotal: Double
        get() = unitPrice * quantity

    val deliveryFee: Double
        get() = 2.50

    val tax: Double
        get() = subtotal * 0.08

    val totalPrice: Double
        get() = subtotal + deliveryFee + tax
}

class DetailViewModel(
    private val itemId: Int,
    private val repository: BrewkeryRepository,
    private val networkMonitor: NetworkMonitor
) : ViewModel() {

    private val _uiState = MutableStateFlow(DetailUiState())
    val uiState: StateFlow<DetailUiState> = _uiState.asStateFlow()

    init {
        loadItemDetail()
    }

    fun loadItemDetail() {
        viewModelScope.launch {
            if (!networkMonitor.checkCurrentConnectivity()) {
                _uiState.value = _uiState.value.copy(screenState = ScreenState.NO_INTERNET)
                return@launch
            }

            _uiState.value = _uiState.value.copy(screenState = ScreenState.LOADING)

            val result = repository.fetchItemDetail(itemId)

            result.fold(
                onSuccess = { dto ->
                    val sizes = dto.customizations?.sizes ?: emptyList()
                    val milks = dto.customizations?.milkOptions ?: emptyList()
                    val sugars = dto.customizations?.sugarLevels ?: emptyList()

                    _uiState.value = _uiState.value.copy(
                        screenState = ScreenState.SUCCESS,
                        item = dto,
                        selectedSize = sizes.firstOrNull(),
                        selectedMilk = milks.firstOrNull(),
                        selectedSugar = sugars.firstOrNull() ?: "Standard"
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

    fun selectSize(size: SizeOptionDto) {
        _uiState.value = _uiState.value.copy(selectedSize = size)
    }

    fun selectMilk(milk: MilkOptionDto) {
        _uiState.value = _uiState.value.copy(selectedMilk = milk)
    }

    fun selectSugar(sugar: String) {
        _uiState.value = _uiState.value.copy(selectedSugar = sugar)
    }

    fun incrementQuantity() {
        _uiState.value = _uiState.value.copy(quantity = _uiState.value.quantity + 1)
    }

    fun decrementQuantity() {
        if (_uiState.value.quantity > 1) {
            _uiState.value = _uiState.value.copy(quantity = _uiState.value.quantity - 1)
        }
    }

    fun addToCart(onSuccess: () -> Unit) {
        val current = _uiState.value
        val item = current.item ?: return

        viewModelScope.launch {
            repository.addOrUpdateCartItem(
                itemId = item.id,
                name = item.name,
                imageUrl = item.imageUrl,
                size = current.selectedSize?.label ?: "Standard",
                sizePrice = current.selectedSize?.extraPrice ?: 0.0,
                milk = current.selectedMilk?.name ?: "Standard Milk",
                milkPrice = current.selectedMilk?.extraPrice ?: 0.0,
                sugar = current.selectedSugar,
                basePrice = item.basePrice,
                quantity = current.quantity
            )
            _uiState.value = _uiState.value.copy(isAddedToCart = true)
            onSuccess()
        }
    }

    class Factory(
        private val itemId: Int,
        private val context: Context
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            val db = BrewkeryDatabase.getInstance(context)
            val repo = BrewkeryRepository(db)
            val net = NetworkMonitor(context)
            return DetailViewModel(itemId, repo, net) as T
        }
    }
}
