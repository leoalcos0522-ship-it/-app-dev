package com.pmchai.chaicentral.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.pmchai.chaicentral.data.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Calendar

class ChaiViewModel(app: Application) : AndroidViewModel(app) {
    private val dao = ChaiDb.get(app).dao()

    val products = dao.products().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val sales = dao.sales().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val topProducts = dao.topProducts().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val todayRevenue = dao.revenueSince(startOfToday()).stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message
    fun clearMessage() { _message.value = null }

    fun addProduct(name: String, price: Double, stock: Int) = viewModelScope.launch {
        dao.insert(Product(name = name, price = price, stock = stock))
    }
    fun restock(p: Product, amount: Int) = viewModelScope.launch { dao.update(p.copy(stock = p.stock + amount)) }
    fun remove(p: Product) = viewModelScope.launch { dao.delete(p) }
    fun sell(p: Product, qty: Int) = viewModelScope.launch {
        _message.value = if (dao.sell(p, qty)) "Sold $qty × ${p.name}" else "Not enough stock for ${p.name}"
    }

    private fun startOfToday() = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
    }.timeInMillis
}
