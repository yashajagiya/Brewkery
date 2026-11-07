package com.example.brewkery.data.repository

import android.util.Log
import com.example.brewkery.data.local.BrewkeryDatabase
import com.example.brewkery.data.local.CartEntity
import com.example.brewkery.data.local.OrderEntity
import com.example.brewkery.data.model.CatalogResponse
import com.example.brewkery.data.model.CoffeeItemDto
import com.example.brewkery.data.remote.BrewkeryApiService
import kotlinx.coroutines.flow.Flow
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

class BrewkeryRepository(private val database: BrewkeryDatabase) {

    private val apiService: BrewkeryApiService by lazy {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        }
        val client = OkHttpClient.Builder()
            .addInterceptor(logging)
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .build()

        Retrofit.Builder()
            .baseUrl(BrewkeryApiService.BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(BrewkeryApiService::class.java)
    }

    suspend fun fetchCatalog(): Result<CatalogResponse> {
        return try {
            val response = apiService.getCatalog()
            if (response.isSuccessful && response.body() != null) {
                Log.d("Brewkery", "Successfully fetched catalog")
                Result.success(response.body()!!)
            } else {
                Log.e("Brewkery", "Catalog fetch failed with code ${response.code()}")
                Result.failure(Exception("Failed to fetch catalog: ${response.message()}"))
            }
        } catch (e: Exception) {
            Log.e("Brewkery", "Exception fetching catalog", e)
            Result.failure(e)
        }
    }

    suspend fun fetchItemDetail(id: Int): Result<CoffeeItemDto> {
        return try {
            val response = apiService.getItemDetail(id)
            if (response.isSuccessful && response.body() != null) {
                Log.d("Brewkery", "Successfully fetched item detail for id $id")
                Result.success(response.body()!!)
            } else {
                Log.e("Brewkery", "Item detail fetch failed for id $id with code ${response.code()}")
                Result.failure(Exception("Failed to fetch item detail: ${response.message()}"))
            }
        } catch (e: Exception) {
            Log.e("Brewkery", "Exception fetching item detail for id $id", e)
            Result.failure(e)
        }
    }

    fun getCartItems(): Flow<List<CartEntity>> = database.cartDao().getAllCartItems()

    suspend fun addOrUpdateCartItem(
        itemId: Int,
        name: String,
        imageUrl: String,
        size: String,
        sizePrice: Double,
        milk: String,
        milkPrice: Double,
        sugar: String,
        basePrice: Double,
        quantity: Int
    ) {
        try {
            val hash = "$itemId-$size-$milk-$sugar"
            val existing = database.cartDao().getCartItemByHash(hash)
            if (existing != null) {
                val updated = existing.copy(quantity = existing.quantity + quantity)
                database.cartDao().updateCartItem(updated)
                Log.d("Brewkery", "Updated cart item quantity for hash $hash to ${updated.quantity}")
            } else {
                val newEntity = CartEntity(
                    itemId = itemId,
                    name = name,
                    imageUrl = imageUrl,
                    selectedSize = size,
                    selectedSizePrice = sizePrice,
                    selectedMilk = milk,
                    selectedMilkPrice = milkPrice,
                    selectedSugar = sugar,
                    basePrice = basePrice,
                    quantity = quantity,
                    customizationHash = hash
                )
                database.cartDao().insertCartItem(newEntity)
                Log.d("Brewkery", "Inserted new cart item for hash $hash")
            }
        } catch (e: Exception) {
            Log.e("Brewkery", "Error adding item to cart", e)
        }
    }

    suspend fun removeCartItem(id: Int) {
        try {
            database.cartDao().deleteCartItem(id)
            Log.d("Brewkery", "Removed cart item $id")
        } catch (e: Exception) {
            Log.e("Brewkery", "Error removing cart item", e)
        }
    }

    suspend fun clearCart() {
        try {
            database.cartDao().clearCart()
            Log.d("Brewkery", "Cleared cart")
        } catch (e: Exception) {
            Log.e("Brewkery", "Error clearing cart", e)
        }
    }

    fun getOrders(): Flow<List<OrderEntity>> = database.orderDao().getAllOrders()

    suspend fun saveOrder(order: OrderEntity) {
        try {
            database.orderDao().insertOrder(order)
            Log.d("Brewkery", "Saved order ticket ${order.ticketId}")
        } catch (e: Exception) {
            Log.e("Brewkery", "Error saving order", e)
        }
    }

    suspend fun updateOrderStatus(ticketId: String, status: String) {
        try {
            database.orderDao().updateOrderStatus(ticketId, status)
            Log.d("Brewkery", "Updated order $ticketId status to $status")
        } catch (e: Exception) {
            Log.e("Brewkery", "Error updating order status", e)
        }
    }

    suspend fun getOrderCount(): Int {
        return try {
            database.orderDao().getOrderCount()
        } catch (e: Exception) {
            Log.e("Brewkery", "Error getting order count", e)
            0
        }
    }
}
