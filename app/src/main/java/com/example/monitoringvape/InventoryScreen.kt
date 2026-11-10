package com.example.monitoringvape

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
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

@Composable
fun InventoryScreen(store: Store, notify: (String) -> Unit) {
    var query by remember { mutableStateOf("") }
    var editing by remember { mutableStateOf<Product?>(null) }
    var adding by remember { mutableStateOf(false) }
    var restocking by remember { mutableStateOf<Product?>(null) }
    var deleting by remember { mutableStateOf<Product?>(null) }

    val shown = store.products.filter {
        query.isBlank() || it.name.contains(query, true) ||
            it.sku.contains(query, true) || it.category.contains(query, true)
    }

    Column(Modifier.fillMaxSize().padding(12.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                label = { Text("Hanapin") },
                singleLine = true,
                modifier = Modifier.weight(1f)
            )
            Button(onClick = { adding = true }) { Text("＋ Produkto") }
        }

        val low = store.products.count { it.status != Status.OK }
        if (low > 0) {
            Text(
                "⚠️ $low produkto ang low / ubos na ang stock",
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(vertical = 6.dp)
            )
        }

        LazyColumn(Modifier.weight(1f).padding(top = 6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            if (shown.isEmpty()) item { Text("Walang produkto pa. Pindutin ang ＋ Produkto.") }
            items(shown, key = { it.id }) { p ->
                Card(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(1f)) {
                                Text(p.name, fontWeight = FontWeight.Bold)
                                Text("${p.sku} · ${p.category}", style = MaterialTheme.typography.bodySmall)
                            }
                            StatusBadge(p.status)
                        }
                        Text("Puhunan ${peso(p.cost)} · Presyo ${peso(p.price)} · Stock ${p.stock}")
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            OutlinedButton(onClick = { restocking = p }) { Text("+Stock") }
                            OutlinedButton(onClick = { editing = p }) { Text("Edit") }
                            TextButton(onClick = { deleting = p }) { Text("Burahin") }
                        }
                    }
                }
            }
        }
    }

    if (adding) {
        ProductDialog(null, onDismiss = { adding = false }) {
            store.upsert(it.copy(id = store.newId()))
            adding = false
        }
    }
    editing?.let { p ->
        ProductDialog(p, onDismiss = { editing = null }) {
            store.upsert(it)
            editing = null
        }
    }
    restocking?.let { p ->
        var qty by remember(p.id) { mutableStateOf("10") }
        AlertDialog(
            onDismissRequest = { restocking = null },
            title = { Text("Dagdag stock: ${p.name}") },
            text = {
                OutlinedTextField(
                    value = qty,
                    onValueChange = { qty = it },
                    label = { Text("Ilan?") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    val n = qty.toIntOrNull() ?: 0
                    if (n > 0) store.restock(p.id, n) else notify("Maglagay ng tamang bilang")
                    restocking = null
                }) { Text("I-save") }
            },
            dismissButton = { TextButton(onClick = { restocking = null }) { Text("Cancel") } }
        )
    }
    deleting?.let { p ->
        AlertDialog(
            onDismissRequest = { deleting = null },
            title = { Text("Burahin ang ${p.name}?") },
            text = { Text("Mananatili sa kasaysayan ang mga naunang benta.") },
            confirmButton = { TextButton(onClick = { store.delete(p.id); deleting = null }) { Text("Burahin") } },
            dismissButton = { TextButton(onClick = { deleting = null }) { Text("Cancel") } }
        )
    }
}

@Composable
private fun ProductDialog(initial: Product?, onDismiss: () -> Unit, onSave: (Product) -> Unit) {
    var name by remember { mutableStateOf(initial?.name ?: "") }
    var sku by remember { mutableStateOf(initial?.sku ?: "") }
    var cat by remember { mutableStateOf(initial?.category ?: "") }
    var cost by remember { mutableStateOf(initial?.cost?.toString() ?: "") }
    var price by remember { mutableStateOf(initial?.price?.toString() ?: "") }
    var stock by remember { mutableStateOf(initial?.stock?.toString() ?: "") }
    var low by remember { mutableStateOf(initial?.lowAlert?.toString() ?: "5") }

    val valid = name.isNotBlank() && cost.toDoubleOrNull() != null &&
        price.toDoubleOrNull() != null && stock.toIntOrNull() != null

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (initial == null) "Bagong produkto" else "I-edit ang produkto") },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                OutlinedTextField(name, { name = it }, label = { Text("Pangalan") }, singleLine = true)
                OutlinedTextField(sku, { sku = it }, label = { Text("SKU") }, singleLine = true)
                OutlinedTextField(cat, { cat = it }, label = { Text("Kategorya (Device, E-Juice, Pod…)") }, singleLine = true)
                OutlinedTextField(
                    cost, { cost = it }, label = { Text("Puhunan (₱)") }, singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                )
                OutlinedTextField(
                    price, { price = it }, label = { Text("Presyo (₱)") }, singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                )
                OutlinedTextField(
                    stock, { stock = it }, label = { Text("Stock") }, singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                OutlinedTextField(
                    low, { low = it }, label = { Text("Low-stock alert") }, singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
            }
        },
        confirmButton = {
            TextButton(
                enabled = valid,
                onClick = {
                    onSave(
                        Product(
                            id = initial?.id ?: "",
                            name = name.trim(),
                            sku = sku.trim(),
                            category = cat.trim(),
                            cost = cost.toDouble(),
                            price = price.toDouble(),
                            stock = stock.toInt(),
                            lowAlert = low.toIntOrNull() ?: 0
                        )
                    )
                }
            ) { Text("I-save") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}
