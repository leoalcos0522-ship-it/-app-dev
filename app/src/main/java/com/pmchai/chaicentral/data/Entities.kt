package com.pmchai.chaicentral.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "products")
data class Product(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val price: Double,
    val stock: Int,
)

/** One line item sold; a single row keeps sales analytics simple. */
@Entity(tableName = "sales")
data class Sale(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val productId: Long,
    val productName: String,
    val quantity: Int,
    val total: Double,
    val timestamp: Long = System.currentTimeMillis(),
)

data class ProductSales(val productName: String, val qty: Int, val revenue: Double)
