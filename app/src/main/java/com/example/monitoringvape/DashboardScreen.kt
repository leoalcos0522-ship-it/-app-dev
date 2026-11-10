package com.example.monitoringvape

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp

/**
 * Home screen. Reads straight from the store's observable lists, so it updates
 * by itself every time a sale is recorded, undone, or stock changes.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(store: Store, auth: Auth, onLock: () -> Unit) {
    val ranges = listOf("Ngayon" to 0, "7 araw" to 6, "30 araw" to 29, "Lahat" to -1)
    var selected by remember { mutableIntStateOf(0) }
    var confirmReset by remember { mutableStateOf(false) }
    var changePw by remember { mutableStateOf(false) }

    val since = ranges[selected].second.let { if (it < 0) 0L else startOfDay(it) }
    val sales = store.sales.filter { it.time >= since }

    val revenue = sales.sumOf { it.total }
    val cost = sales.sumOf { s -> s.items.sumOf { it.cost * it.qty } }
    val profit = revenue - cost
    val units = sales.sumOf { s -> s.items.sumOf { it.qty } }
    val stockLeft = store.products.sumOf { it.stock }
    val stockValue = store.products.sumOf { it.cost * it.stock }

    // Per-product: how many sold in this range, profit, and stock remaining now.
    val soldQty = HashMap<String, Int>()
    val soldProfit = HashMap<String, Double>()
    sales.forEach { s ->
        s.items.forEach { i ->
            soldQty[i.productId] = (soldQty[i.productId] ?: 0) + i.qty
            soldProfit[i.productId] = (soldProfit[i.productId] ?: 0.0) + (i.price - i.cost) * i.qty
        }
    }
    val rows = store.products.sortedByDescending { soldQty[it.id] ?: 0 }
    val lowCount = store.products.count { it.status != Status.OK }

    LazyColumn(
        Modifier.fillMaxSize().padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ranges.forEachIndexed { i, (label, _) ->
                    FilterChip(selected = selected == i, onClick = { selected = i }, label = { Text(label) })
                }
            }
        }

        item {
            Card(
                Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF059669))
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text("KITA (PROFIT)", color = Color.White, style = MaterialTheme.typography.labelLarge)
                    Text(
                        peso(profit),
                        color = Color.White,
                        style = MaterialTheme.typography.displaySmall,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                StatCard("Benta", peso(revenue), Modifier.weight(1f))
                StatCard("Nabawas na stock", "$units pcs", Modifier.weight(1f))
            }
        }
        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                StatCard("Natitirang stock", "$stockLeft pcs", Modifier.weight(1f))
                StatCard("Halaga ng stock (puhunan)", peso(stockValue), Modifier.weight(1f))
            }
        }

        if (lowCount > 0) {
            item {
                Text(
                    "⚠️ $lowCount produkto ang low / ubos na ang stock",
                    color = MaterialTheme.colorScheme.error,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        item { Text("Bawat produkto", style = MaterialTheme.typography.titleMedium) }
        if (rows.isEmpty()) item { Text("Wala pang produkto. Magdagdag sa Inventory tab.") }
        rows.forEach { p ->
            item(key = p.id) {
                Card(Modifier.fillMaxWidth()) {
                    Row(Modifier.padding(12.dp), verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text(p.name, fontWeight = FontWeight.Bold)
                            Text(
                                "Nabenta: ${soldQty[p.id] ?: 0} · Kita: ${peso(soldProfit[p.id] ?: 0.0)}",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                        Column(horizontalAlignment = androidx.compose.ui.Alignment.End) {
                            Text("Natitira: ${p.stock}", fontWeight = FontWeight.Bold)
                            if (p.status != Status.OK) StatusBadge(p.status)
                        }
                    }
                }
            }
        }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = onLock) { Text("🔒 I-lock") }
                OutlinedButton(onClick = { changePw = true }) { Text("Palitan ang password") }
            }
        }
        item { OutlinedButton(onClick = { confirmReset = true }) { Text("Burahin lahat ng data") } }
    }

    if (changePw) {
        ChangePasswordDialog(auth, onDismiss = { changePw = false }, onDone = { changePw = false })
    }

    if (confirmReset) {
        var pw by remember { mutableStateOf("") }
        var wrong by remember { mutableStateOf(false) }
        AlertDialog(
            onDismissRequest = { confirmReset = false },
            title = { Text("Burahin LAHAT?") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Mabubura ang lahat ng produkto at benta. Hindi na ito mababawi. Ilagay ang password para magpatuloy.")
                    OutlinedTextField(
                        value = pw,
                        onValueChange = { pw = it; wrong = false },
                        label = { Text("Password") },
                        singleLine = true,
                        isError = wrong,
                        visualTransformation = PasswordVisualTransformation()
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    if (auth.check(pw)) { store.resetAll(); confirmReset = false } else wrong = true
                }) { Text("Oo, burahin") }
            },
            dismissButton = { TextButton(onClick = { confirmReset = false }) { Text("Cancel") } }
        )
    }
}

@Composable
private fun StatCard(label: String, value: String, modifier: Modifier = Modifier) {
    Card(modifier) {
        Column(Modifier.padding(12.dp)) {
            Text(label, style = MaterialTheme.typography.bodySmall)
            Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        }
    }
}
