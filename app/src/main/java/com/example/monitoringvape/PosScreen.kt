package com.example.monitoringvape

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PosScreen(store: Store, notify: (String) -> Unit) {
    val cart = remember { mutableStateMapOf<String, Int>() }
    var query by remember { mutableStateOf("") }
    var paid by remember { mutableStateOf("") }
    var method by remember { mutableStateOf("Cash") }
    var idOk by remember { mutableStateOf(false) }

    val total: Double = cart.entries.sumOf { (id, q) ->
        (store.products.find { it.id == id }?.price ?: 0.0) * q
    }
    val paidValue = paid.toDoubleOrNull() ?: 0.0
    val change = if (method == "Cash" && paidValue > total) paidValue - total else 0.0
    val shown = store.products.filter {
        query.isBlank() || it.name.contains(query, true) || it.sku.contains(query, true)
    }

    Column(Modifier.fillMaxSize().padding(12.dp)) {
        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            label = { Text("Hanapin produkto / SKU") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.padding(4.dp))

        // Product list
        LazyColumn(
            Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            if (shown.isEmpty()) {
                item { Text("Walang produkto. Magdagdag sa Inventory tab.") }
            }
            items(shown, key = { it.id }) { p ->
                Card(
                    Modifier.fillMaxWidth().clickable(enabled = p.stock > 0) {
                        val now = cart[p.id] ?: 0
                        if (now + 1 > p.stock) notify("Kulang ang stock") else cart[p.id] = now + 1
                    }
                ) {
                    Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text(p.name, fontWeight = FontWeight.Bold)
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text("${p.sku} · stock ${p.stock}", style = MaterialTheme.typography.bodySmall)
                                if (p.status != Status.OK) StatusBadge(p.status)
                            }
                        }
                        Text(peso(p.price), fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Cart
        Card(Modifier.fillMaxWidth().padding(top = 8.dp)) {
            Column(Modifier.padding(12.dp)) {
                Text("Cart", style = MaterialTheme.typography.titleMedium)
                if (cart.isEmpty()) {
                    Text("Walang laman. Pindutin ang produkto para idagdag.", style = MaterialTheme.typography.bodySmall)
                } else {
                    LazyColumn(Modifier.heightIn(max = 140.dp)) {
                        items(cart.keys.toList(), key = { it }) { id ->
                            val p = store.products.find { it.id == id }
                            if (p != null) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(p.name, Modifier.weight(1f))
                                    OutlinedButton(onClick = {
                                        val q = (cart[id] ?: 1) - 1
                                        if (q <= 0) cart.remove(id) else cart[id] = q
                                    }) { Text("−") }
                                    Text("${cart[id] ?: 0}", Modifier.padding(horizontal = 8.dp))
                                    OutlinedButton(onClick = {
                                        val q = (cart[id] ?: 0) + 1
                                        if (q > p.stock) notify("Kulang ang stock") else cart[id] = q
                                    }) { Text("+") }
                                    Text(peso(p.price * (cart[id] ?: 0)), Modifier.padding(start = 8.dp))
                                }
                            }
                        }
                    }
                }

                Row(Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("TOTAL", style = MaterialTheme.typography.titleLarge)
                    Text(peso(total), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("Cash", "GCash", "Card").forEach { m ->
                        FilterChip(selected = method == m, onClick = { method = m }, label = { Text(m) })
                    }
                }

                if (method == "Cash") {
                    Row(
                        Modifier.fillMaxWidth().padding(top = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = paid,
                            onValueChange = { paid = it },
                            label = { Text("Bayad (₱)") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.weight(1f)
                        )
                        Column {
                            Text("Sukli", style = MaterialTheme.typography.bodySmall)
                            Text(peso(change), fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = idOk, onCheckedChange = { idOk = it })
                    Text("ID verified — 18 pataas lang")
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = {
                            if (cart.isEmpty()) {
                                notify("Walang laman ang cart")
                            } else if (!idOk) {
                                notify("I-check muna ang ID verified (18+)")
                            } else if (method == "Cash" && paidValue < total) {
                                notify("Kulang ang bayad")
                            } else {
                                val result = store.checkout(
                                    cart.toMap(), method, if (method == "Cash") paidValue else total
                                )
                                result.onSuccess { sale ->
                                    notify("Resibo ${receiptLabel(sale.no)} · ${peso(sale.total)} · Sukli ${peso(sale.paid - sale.total)}")
                                    cart.clear()
                                    paid = ""
                                    idOk = false
                                }.onFailure { notify(it.message ?: "May error") }
                            }
                        },
                        modifier = Modifier.weight(1f)
                    ) { Text("Bayaran") }
                    OutlinedButton(onClick = { cart.clear(); paid = "" }) { Text("Clear") }
                }
            }
        }
    }
}
