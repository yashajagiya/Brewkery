package com.example.brewkery.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface OrderDao {
    @Query("SELECT * FROM orders_table ORDER BY timestamp DESC")
    fun getAllOrders(): Flow<List<OrderEntity>>

    @Query("SELECT * FROM orders_table WHERE ticketId = :ticketId LIMIT 1")
    suspend fun getOrderById(ticketId: String): OrderEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrder(order: OrderEntity)

    @Query("UPDATE orders_table SET status = :status WHERE ticketId = :ticketId")
    suspend fun updateOrderStatus(ticketId: String, status: String): Int

    @Query("SELECT COUNT(*) FROM orders_table")
    suspend fun getOrderCount(): Int
}
