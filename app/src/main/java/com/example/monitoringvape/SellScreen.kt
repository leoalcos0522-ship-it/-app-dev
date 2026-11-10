package com.example.monitoringvape

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

/** Quick sale recording: tap a product and its stock goes down automatically. */
@Composable
fun SellScreen(store: Store, notify: (String, String?, (() -> Unit)?) -> Unit) {
    var query by remember { mutableStateOf("") }
    var customQty by remember { mutableStateOf<Product?>(null) }

    fun sell(p: Product, qty: Int) {
        store.sell(p.id, qty)
            .onSuccess { sale ->
                notify(
                    "${p.name} ×$qty · ${peso(sale.total)} · Kita ${peso((p.price - p.cost) * qty)}",
                    "UNDO"
                ) { store.void(sale.id) }
            }
            .onFailure { notify(it.message ?: "May error", null, null) }
    }

    val shown = store.products.filter {
        query.isBlank() || it.name.contains(query, true) || it.sku.contains(query, true)
    }

    Column(Modifier.fillMaxSize().padding(12.dp)) {
        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            label = { Text("Hanapin produkto") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        LazyColumn(
            Modifier.weight(1f).padding(top = 8.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            if (shown.isEmpty()) item { Text("Walang produkto. Magdagdag sa Inventory tab.") }
            items(shown, key = { it.id }) { p ->
                Card(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(1f)) {
                                Text(p.name, fontWeight = FontWeight.Bold)
                                Text(
                                    "${peso(p.price)} · kita ${peso(p.price - p.cost)} bawat isa",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("Stock: ${p.stock}", fontWeight = FontWeight.Bold)
                                if (p.status != Status.OK) StatusBadge(p.status)
                            }
                        }
                        Row(
                            Modifier.padding(top = 6.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                enabled = p.stock > 0,
                                onClick = { sell(p, 1) },
                                modifier = Modifier.weight(1f)
                            ) { Text("Benta ×1") }
                            OutlinedButton(
                                enabled = p.stock > 0,
                                onClick = { customQty = p },
                                modifier = Modifier.weight(1f)
                            ) { Text("Ibang dami") }
                        }
                    }
                }
            }
        }
    }

    customQty?.let { p ->
        var qty by remember(p.id) { mutableStateOf("2") }
        AlertDialog(
            onDismissRequest = { customQty = null },
            title = { Text("Ilan ang nabenta? (${p.name})") },
            text = {
                OutlinedTextField(
                    value = qty,
                    onValueChange = { qty = it },
                    label = { Text("Dami") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    sell(p, qty.toIntOrNull() ?: 0)
                    customQty = null
                }) { Text("I-record") }
            },
            dismissButton = { TextButton(onClick = { customQty = null }) { Text("Cancel") } }
        )
    }
}
