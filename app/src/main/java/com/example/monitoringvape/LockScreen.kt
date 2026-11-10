package com.example.monitoringvape

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp

private const val MIN_LENGTH = 4

/** First run: create a password. After that: ask for it every time the app opens. */
@Composable
fun LockScreen(auth: Auth, onUnlocked: () -> Unit) {
    val creating = remember { !auth.hasPassword() }
    var pw by remember { mutableStateOf("") }
    var pw2 by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    var fails by remember { mutableStateOf(0) }
    var lockedUntil by remember { mutableStateOf(0L) }

    fun submit() {
        if (creating) {
            if (pw.length < MIN_LENGTH) {
                error = "Hindi bababa sa $MIN_LENGTH na character ang password"
            } else if (pw != pw2) {
                error = "Hindi magkapareho ang dalawang password"
            } else {
                auth.setPassword(pw)
                onUnlocked()
            }
            return
        }
        val now = System.currentTimeMillis()
        if (now < lockedUntil) {
            error = "Maghintay ng ${(lockedUntil - now) / 1000 + 1} segundo"
        } else if (auth.check(pw)) {
            onUnlocked()
        } else {
            fails++
            pw = ""
            if (fails >= 5) {
                fails = 0
                lockedUntil = now + 30_000
                error = "Limang beses nang mali. Maghintay ng 30 segundo."
            } else {
                error = "Maling password"
            }
        }
    }

    Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(
            Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .imePadding()
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("💨 Vape POS", style = MaterialTheme.typography.headlineMedium)
            Spacer(Modifier.height(8.dp))
            Text(
                if (creating) "Gumawa ng password para sa app" else "Ilagay ang password",
                style = MaterialTheme.typography.bodyLarge
            )
            Spacer(Modifier.height(16.dp))

            OutlinedTextField(
                value = pw,
                onValueChange = { pw = it; error = null },
                label = { Text(if (creating) "Bagong password" else "Password") },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = if (creating) ImeAction.Next else ImeAction.Done
                ),
                keyboardActions = KeyboardActions(onDone = { submit() }),
                modifier = Modifier.fillMaxWidth()
            )
            if (creating) {
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = pw2,
                    onValueChange = { pw2 = it; error = null },
                    label = { Text("Ulitin ang password") },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(onDone = { submit() }),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    "Tandaan ang password. Kapag nakalimutan, kailangang i-clear ang data ng app at mawawala ang lahat ng benta at produkto.",
                    style = MaterialTheme.typography.bodySmall
                )
            }
            error?.let {
                Spacer(Modifier.height(8.dp))
                Text(it, color = MaterialTheme.colorScheme.error)
            }
            Spacer(Modifier.height(16.dp))
            Button(onClick = { submit() }, modifier = Modifier.fillMaxWidth()) {
                Text(if (creating) "I-save at buksan" else "Buksan")
            }
        }
    }
}

/** Change the password from inside the app (asks for the current one first). */
@Composable
fun ChangePasswordDialog(auth: Auth, onDismiss: () -> Unit, onDone: () -> Unit) {
    var current by remember { mutableStateOf("") }
    var new1 by remember { mutableStateOf("") }
    var new2 by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Palitan ang password") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                OutlinedTextField(
                    current, { current = it; error = null },
                    label = { Text("Kasalukuyang password") }, singleLine = true,
                    visualTransformation = PasswordVisualTransformation()
                )
                OutlinedTextField(
                    new1, { new1 = it; error = null },
                    label = { Text("Bagong password") }, singleLine = true,
                    visualTransformation = PasswordVisualTransformation()
                )
                OutlinedTextField(
                    new2, { new2 = it; error = null },
                    label = { Text("Ulitin ang bagong password") }, singleLine = true,
                    visualTransformation = PasswordVisualTransformation()
                )
                error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                if (!auth.check(current)) error = "Mali ang kasalukuyang password"
                else if (new1.length < MIN_LENGTH) error = "Hindi bababa sa $MIN_LENGTH na character"
                else if (new1 != new2) error = "Hindi magkapareho ang bagong password"
                else {
                    auth.setPassword(new1)
                    onDone()
                }
            }) { Text("I-save") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}
