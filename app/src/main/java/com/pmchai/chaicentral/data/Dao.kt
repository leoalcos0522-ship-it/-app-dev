package com.pmchai.chaicentral.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ChaiDao {
    @Query("SELECT * FROM products ORDER BY name") fun products(): Flow<List<Product>>
    @Insert suspend fun insert(p: Product): Long
    @Update suspend fun update(p: Product)
    @Delete suspend fun delete(p: Product)

    @Insert suspend fun insertSale(s: Sale)
    @Query("UPDATE products SET stock = stock - :qty WHERE id = :id AND stock >= :qty")
    suspend fun deductStock(id: Long, qty: Int): Int

    @Transaction
    suspend fun sell(p: Product, qty: Int): Boolean {
        if (deductStock(p.id, qty) == 0) return false
        insertSale(Sale(productId = p.id, productName = p.name, quantity = qty, total = p.price * qty))
        return true
    }

    @Query("SELECT * FROM sales ORDER BY timestamp DESC") fun sales(): Flow<List<Sale>>
    @Query("SELECT productName, SUM(quantity) AS qty, SUM(total) AS revenue FROM sales GROUP BY productId ORDER BY revenue DESC LIMIT 5")
    fun topProducts(): Flow<List<ProductSales>>
    @Query("SELECT COALESCE(SUM(total),0) FROM sales WHERE timestamp >= :since")
    fun revenueSince(since: Long): Flow<Double>
}
