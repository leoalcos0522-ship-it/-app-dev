package com.example.monitoringvape

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.unit.dp

@Composable
fun HistoryScreen(store: Store) {
    var voiding by remember { mutableStateOf<Sale?>(null) }

    LazyColumn(
        Modifier.fillMaxSize().padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        if (store.sales.isEmpty()) item { Text("Wala pang benta.") }
        items(store.sales, key = { it.id }) { s ->
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text("Resibo ${receiptLabel(s.no)}", fontWeight = FontWeight.Bold)
                            Text("${formatTime(s.time)} · ${s.method}", style = MaterialTheme.typography.bodySmall)
                        }
                        Text(peso(s.total), fontWeight = FontWeight.Bold)
                    }
                    s.items.forEach { Text("• ${it.name} × ${it.qty}", style = MaterialTheme.typography.bodyMedium) }
                    TextButton(onClick = { voiding = s }) { Text("Void") }
                }
            }
        }
    }

    voiding?.let { s ->
        AlertDialog(
            onDismissRequest = { voiding = null },
            title = { Text("I-void ang resibo ${receiptLabel(s.no)}?") },
            text = { Text("Mawawala ito sa benta at ibabalik ang stock.") },
            confirmButton = { TextButton(onClick = { store.void(s.id); voiding = null }) { Text("I-void") } },
            dismissButton = { TextButton(onClick = { voiding = null }) { Text("Cancel") } }
        )
    }
}
