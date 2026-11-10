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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

private class TopRow(var qty: Int = 0, var revenue: Double = 0.0, var profit: Double = 0.0)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportScreen(store: Store) {
    val ranges = listOf("Ngayon" to 0, "7 araw" to 6, "30 araw" to 29, "Lahat" to -1)
    var selected by remember { mutableIntStateOf(0) }
    var confirmReset by remember { mutableStateOf(false) }

    val since = ranges[selected].second.let { if (it < 0) 0L else startOfDay(it) }
    val sales = store.sales.filter { it.time >= since }

    val revenue = sales.sumOf { it.total }
    val cost = sales.sumOf { s -> s.items.sumOf { it.cost * it.qty } }
    val units = sales.sumOf { s -> s.items.sumOf { it.qty } }
    val stockValue = store.products.sumOf { it.cost * it.stock }

    val top = HashMap<String, TopRow>()
    sales.forEach { s ->
        s.items.forEach { i ->
            val r = top.getOrPut(i.name) { TopRow() }
            r.qty += i.qty
            r.revenue += i.price * i.qty
            r.profit += (i.price - i.cost) * i.qty
        }
    }
    val topList = top.entries.sortedByDescending { it.value.qty }.take(10)
    val lowList = store.products.filter { it.status != Status.OK }.sortedBy { it.stock }

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
        item { StatCard("Kabuuang benta", peso(revenue)) }
        item { StatCard("Kita (profit)", peso(revenue - cost)) }
        item { StatCard("Transaksyon / Units nabenta", "${sales.size} / $units") }
        item { StatCard("Halaga ng stock (puhunan)", peso(stockValue)) }

        item { Text("Pinakamabentang produkto", style = MaterialTheme.typography.titleMedium) }
        if (topList.isEmpty()) item { Text("Walang benta sa panahong ito.") }
        topList.forEach { (name, r) ->
            item {
                Card(Modifier.fillMaxWidth()) {
                    Row(Modifier.padding(12.dp)) {
                        Column(Modifier.weight(1f)) {
                            Text(name, fontWeight = FontWeight.Bold)
                            Text("${r.qty} pcs · benta ${peso(r.revenue)}", style = MaterialTheme.typography.bodySmall)
                        }
                        Text("Kita ${peso(r.profit)}")
                    }
                }
            }
        }

        item { Text("Low / Ubos na stock", style = MaterialTheme.typography.titleMedium) }
        if (lowList.isEmpty()) item { Text("Okay lahat ng stock 👍") }
        lowList.forEach { p ->
            item {
                Card(Modifier.fillMaxWidth()) {
                    Row(Modifier.padding(12.dp)) {
                        Text(p.name, Modifier.weight(1f))
                        Text("${p.stock} pcs  ")
                        StatusBadge(p.status)
                    }
                }
            }
        }

        item { OutlinedButton(onClick = { confirmReset = true }) { Text("Burahin lahat ng data") } }
    }

    if (confirmReset) {
        AlertDialog(
            onDismissRequest = { confirmReset = false },
            title = { Text("Burahin LAHAT?") },
            text = { Text("Mabubura ang lahat ng produkto at benta. Hindi na ito mababawi.") },
            confirmButton = { TextButton(onClick = { store.resetAll(); confirmReset = false }) { Text("Oo, burahin") } },
            dismissButton = { TextButton(onClick = { confirmReset = false }) { Text("Cancel") } }
        )
    }
}

@Composable
private fun StatCard(label: String, value: String) {
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(12.dp)) {
            Text(label, style = MaterialTheme.typography.bodySmall)
            Text(value, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        }
    }
}
