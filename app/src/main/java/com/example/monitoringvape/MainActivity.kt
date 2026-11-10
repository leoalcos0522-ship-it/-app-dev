package com.example.monitoringvape

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.example.monitoringvape.ui.theme.MonitoringVapeTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val store = Store(applicationContext)
        setContent {
            MonitoringVapeTheme {
                VapeApp(store)
            }
        }
    }
}

private val tabs = listOf(
    "Dashboard" to "📊",
    "Benta" to "🛒",
    "Inventory" to "📦",
    "Kasaysayan" to "🧾"
)

@Composable
fun VapeApp(store: Store) {
    var tab by rememberSaveable { mutableIntStateOf(0) }
    val snackbar = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    // notify(message, actionLabel, onAction) - the action (e.g. UNDO) is optional.
    val notify: (String, String?, (() -> Unit)?) -> Unit = { msg, actionLabel, onAction ->
        scope.launch {
            snackbar.currentSnackbarData?.dismiss()
            val result = snackbar.showSnackbar(msg, actionLabel, duration = SnackbarDuration.Long)
            if (result == SnackbarResult.ActionPerformed) onAction?.invoke()
        }
    }
    val notifySimple: (String) -> Unit = { notify(it, null, null) }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbar) },
        bottomBar = {
            NavigationBar {
                tabs.forEachIndexed { i, (label, emoji) ->
                    NavigationBarItem(
                        selected = tab == i,
                        onClick = { tab = i },
                        icon = { Text(emoji) },
                        label = { Text(label) }
                    )
                }
            }
        }
    ) { padding ->
        Box(Modifier.padding(padding).statusBarsPadding()) {
            when (tab) {
                0 -> DashboardScreen(store)
                1 -> SellScreen(store, notify)
                2 -> InventoryScreen(store, notifySimple)
                else -> HistoryScreen(store)
            }
        }
    }
}
