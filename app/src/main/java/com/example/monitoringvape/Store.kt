package com.example.monitoringvape

import android.content.Context
import androidx.compose.runtime.mutableStateListOf
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

/**
 * Simple data store. Saves everything to SharedPreferences as JSON,
 * so no extra Gradle dependencies are needed.
 */
class Store(context: Context) {
    private val prefs = context.getSharedPreferences("vapepos", Context.MODE_PRIVATE)

    val products = mutableStateListOf<Product>()
    val sales = mutableStateListOf<Sale>()
    private var receiptNo = 0

    init {
        load()
    }

    // ---------- Inventory ----------

    fun upsert(p: Product) {
        val i = products.indexOfFirst { it.id == p.id }
        if (i >= 0) products[i] = p else products.add(p)
        save()
    }

    fun newId(): String = UUID.randomUUID().toString()

    fun delete(id: String) {
        products.removeAll { it.id == id }
        save()
    }

    fun restock(id: String, qty: Int) {
        val i = products.indexOfFirst { it.id == id }
        if (i >= 0 && qty > 0) {
            products[i] = products[i].copy(stock = products[i].stock + qty)
            save()
        }
    }

    // ---------- Sales ----------

    fun checkout(cart: Map<String, Int>, method: String, paid: Double): Result<Sale> {
        val items = mutableListOf<SaleItem>()
        for ((id, qty) in cart) {
            val p = products.find { it.id == id }
                ?: return Result.failure(Exception("May produktong wala na sa inventory"))
            if (qty > p.stock) return Result.failure(Exception("Kulang ang stock ng ${p.name}"))
            items.add(SaleItem(p.id, p.name, qty, p.price, p.cost))
        }
        for (item in items) {
            val i = products.indexOfFirst { it.id == item.productId }
            products[i] = products[i].copy(stock = products[i].stock - item.qty)
        }
        receiptNo++
        val sale = Sale(
            id = newId(),
            no = receiptNo,
            time = System.currentTimeMillis(),
            items = items,
            total = items.sumOf { it.price * it.qty },
            method = method,
            paid = paid
        )
        sales.add(0, sale)
        save()
        return Result.success(sale)
    }

    /** Cancel a sale and return the stock. */
    fun void(saleId: String) {
        val sale = sales.find { it.id == saleId } ?: return
        for (item in sale.items) {
            val i = products.indexOfFirst { it.id == item.productId }
            if (i >= 0) products[i] = products[i].copy(stock = products[i].stock + item.qty)
        }
        sales.remove(sale)
        save()
    }

    fun resetAll() {
        products.clear()
        sales.clear()
        receiptNo = 0
        save()
    }

    // ---------- Persistence ----------

    private fun save() {
        val p = JSONArray()
        products.forEach {
            p.put(
                JSONObject()
                    .put("id", it.id).put("name", it.name).put("sku", it.sku)
                    .put("category", it.category).put("cost", it.cost).put("price", it.price)
                    .put("stock", it.stock).put("low", it.lowAlert)
            )
        }
        val s = JSONArray()
        sales.forEach { sale ->
            val items = JSONArray()
            sale.items.forEach {
                items.put(
                    JSONObject()
                        .put("pid", it.productId).put("name", it.name).put("qty", it.qty)
                        .put("price", it.price).put("cost", it.cost)
                )
            }
            s.put(
                JSONObject()
                    .put("id", sale.id).put("no", sale.no).put("time", sale.time)
                    .put("total", sale.total).put("method", sale.method).put("paid", sale.paid)
                    .put("items", items)
            )
        }
        prefs.edit()
            .putString("products", p.toString())
            .putString("sales", s.toString())
            .putInt("receiptNo", receiptNo)
            .apply()
    }

    private fun load() {
        try {
            val p = JSONArray(prefs.getString("products", "[]"))
            for (i in 0 until p.length()) {
                val o = p.getJSONObject(i)
                products.add(
                    Product(
                        o.getString("id"), o.getString("name"), o.optString("sku"),
                        o.optString("category"), o.getDouble("cost"), o.getDouble("price"),
                        o.getInt("stock"), o.optInt("low", 5)
                    )
                )
            }
            val s = JSONArray(prefs.getString("sales", "[]"))
            for (i in 0 until s.length()) {
                val o = s.getJSONObject(i)
                val arr = o.getJSONArray("items")
                val items = (0 until arr.length()).map {
                    val x = arr.getJSONObject(it)
                    SaleItem(
                        x.getString("pid"), x.getString("name"), x.getInt("qty"),
                        x.getDouble("price"), x.getDouble("cost")
                    )
                }
                sales.add(
                    Sale(
                        o.getString("id"), o.getInt("no"), o.getLong("time"), items,
                        o.getDouble("total"), o.getString("method"), o.getDouble("paid")
                    )
                )
            }
            receiptNo = prefs.getInt("receiptNo", sales.maxOfOrNull { it.no } ?: 0)
        } catch (e: Exception) {
            products.clear()
            sales.clear()
        }
    }
}
