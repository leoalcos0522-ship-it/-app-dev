package com.example.monitoringvape

enum class Status { OK, LOW, OUT }

data class Product(
    val id: String,
    val name: String,
    val sku: String,
    val category: String,
    val cost: Double,
    val price: Double,
    val stock: Int,
    val lowAlert: Int
) {
    val status: Status
        get() = when {
            stock <= 0 -> Status.OUT
            stock <= lowAlert -> Status.LOW
            else -> Status.OK
        }
}

data class SaleItem(
    val productId: String,
    val name: String,
    val qty: Int,
    val price: Double,
    val cost: Double
)

data class Sale(
    val id: String,
    val no: Int,
    val time: Long,
    val items: List<SaleItem>,
    val total: Double,
    val method: String,
    val paid: Double
)
