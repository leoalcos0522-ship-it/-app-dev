package com.pmchai.chaicentral.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.pmchai.chaicentral.data.Product

private enum class Tab(val label: String) { Dashboard("Dashboard"), Inventory("Inventory"), Sales("Sales") }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChaiApp(vm: ChaiViewModel = viewModel()) {
    var tab by remember { mutableStateOf(Tab.Dashboard) }
    val snack = remember { SnackbarHostState() }
    val msg by vm.message.collectAsStateWithLifecycle()
    LaunchedEffect(msg) { msg?.let { snack.showSnackbar(it); vm.clearMessage() } }

    Scaffold(
        topBar = { TopAppBar(title = { Text("ChaiCentral · PM Chai Café") }) },
        snackbarHost = { SnackbarHost(snack) },
        bottomBar = {
            NavigationBar {
                Tab.values().forEach { t ->
                    NavigationBarItem(
                        selected = tab == t, onClick = { tab = t }, label = { Text(t.label) },
                        icon = { Icon(when (t) {
                            Tab.Dashboard -> Icons.Default.BarChart
                            Tab.Inventory -> Icons.Default.Inventory2
                            Tab.Sales -> Icons.Default.PointOfSale
                        }, null) },
                    )
                }
            }
        },
    ) { pad ->
        Box(Modifier.padding(pad).padding(16.dp)) {
            when (tab) {
                Tab.Dashboard -> Dashboard(vm)
                Tab.Inventory -> Inventory(vm)
                Tab.Sales -> Sales(vm)
            }
        }
    }
}

@Composable
private fun Dashboard(vm: ChaiViewModel) {
    val today by vm.todayRevenue.collectAsStateWithLifecycle()
    val top by vm.topProducts.collectAsStateWithLifecycle()
    val products by vm.products.collectAsStateWithLifecycle()
    val low = products.filter { it.stock <= 5 }
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Card(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp)) {
                Text("Today's revenue", style = MaterialTheme.typography.labelLarge)
                Text("₱%.2f".format(today), style = MaterialTheme.typography.headlineMedium)
            }
        }
        Text("Top sellers", style = MaterialTheme.typography.titleMedium)
        if (top.isEmpty()) Text("No sales yet")
        top.forEach { Text("${it.productName} — ${it.qty} sold, ₱%.2f".format(it.revenue)) }
        Text("Low stock (≤5)", style = MaterialTheme.typography.titleMedium)
        if (low.isEmpty()) Text("All good")
        low.forEach { Text("${it.name}: ${it.stock} left") }
    }
}

@Composable
private fun Inventory(vm: ChaiViewModel) {
    val products by vm.products.collectAsStateWithLifecycle()
    var name by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    var stock by remember { mutableStateOf("") }
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedTextField(name, { name = it }, label = { Text("Item name") }, modifier = Modifier.fillMaxWidth())
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(price, { price = it }, label = { Text("Price") }, modifier = Modifier.weight(1f))
            OutlinedTextField(stock, { stock = it }, label = { Text("Stock") }, modifier = Modifier.weight(1f))
        }
        Button(onClick = {
            val p = price.toDoubleOrNull(); val s = stock.toIntOrNull()
            if (name.isNotBlank() && p != null && s != null) {
                vm.addProduct(name.trim(), p, s); name = ""; price = ""; stock = ""
            }
        }) { Text("Add item") }
        LazyColumn {
            items(products, key = { it.id }) { p ->
                ListItem(
                    headlineContent = { Text(p.name) },
                    supportingContent = { Text("₱%.2f · stock ${p.stock}".format(p.price)) },
                    trailingContent = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            TextButton({ vm.restock(p, 10) }) { Text("+10") }
                            IconButton({ vm.remove(p) }) { Icon(Icons.Default.Delete, "Delete") }
                        }
                    },
                )
                HorizontalDivider()
            }
        }
    }
}

@Composable
private fun Sales(vm: ChaiViewModel) {
    val products by vm.products.collectAsStateWithLifecycle()
    val sales by vm.sales.collectAsStateWithLifecycle()
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Tap an item to sell one", style = MaterialTheme.typography.titleMedium)
        products.chunked(2).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                row.forEach { p: Product ->
                    FilledTonalButton({ vm.sell(p, 1) }, Modifier.weight(1f), enabled = p.stock > 0) {
                        Text("${p.name}\n₱%.0f".format(p.price))
                    }
                }
                if (row.size == 1) Spacer(Modifier.weight(1f))
            }
        }
        Text("Recent sales", style = MaterialTheme.typography.titleMedium)
        LazyColumn {
            items(sales.take(30), key = { it.id }) {
                Text("${it.quantity} × ${it.productName} — ₱%.2f".format(it.total))
            }
        }
    }
}
