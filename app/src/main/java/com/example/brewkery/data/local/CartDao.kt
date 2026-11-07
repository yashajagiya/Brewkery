package com.example.brewkery.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface CartDao {
    @Query("SELECT * FROM cart_table ORDER BY id ASC")
    fun getAllCartItems(): Flow<List<CartEntity>>

    @Query("SELECT * FROM cart_table WHERE customizationHash = :hash LIMIT 1")
    suspend fun getCartItemByHash(hash: String): CartEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCartItem(item: CartEntity)

    @Update
    suspend fun updateCartItem(item: CartEntity)

    @Query("DELETE FROM cart_table WHERE id = :id")
    suspend fun deleteCartItem(id: Int): Int

    @Query("DELETE FROM cart_table")
    suspend fun clearCart(): Int
}
