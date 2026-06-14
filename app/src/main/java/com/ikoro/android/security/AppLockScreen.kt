package com.ikoro.android.security

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp

@Composable
fun AppLockScreen(
    onUnlocked: () -> Unit
) {
    val context = LocalContext.current
    val hasPin = remember { SecureVault.hasPin(context) }
    val canBio = remember { SecureVault.canUseBiometric(context) }
    val useBio = remember { SecureVault.useBiometric(context) }

    var pin by remember { mutableStateOf("") }
    var error by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        if (!hasPin && !useBio) {
            onUnlocked()
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("🔒 Ikoro Locked", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(24.dp))

        if (useBio && canBio) {
            Button(onClick = {
                // Prompt must be triggered from FragmentActivity; this is a fallback.
                error = "Please use device biometric prompt or enter PIN"
            }) {
                Text("Use Biometric")
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        OutlinedTextField(
            value = pin,
            onValueChange = { pin = it.take(6) },
            label = { Text("Enter PIN") },
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = {
            if (SecureVault.checkPin(context, pin)) {
                onUnlocked()
            } else {
                error = "Wrong PIN"
                pin = ""
            }
        }) {
            Text("Unlock")
        }

        if (error.isNotBlank()) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(error, color = MaterialTheme.colorScheme.error)
        }
    }
}

@Composable
fun FirstTimeLockSetup(
    onDone: () -> Unit
) {
    val context = LocalContext.current
    var pin by remember { mutableStateOf("") }
    var confirm by remember { mutableStateOf("") }
    var error by remember { mutableStateOf("") }
    var enableBio by remember { mutableStateOf(false) }
    val canBio = remember { SecureVault.canUseBiometric(context) }

    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Secure your wallet", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(
            value = pin,
            onValueChange = { pin = it.take(6) },
            label = { Text("Set 6-digit PIN") },
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = confirm,
            onValueChange = { confirm = it.take(6) },
            label = { Text("Confirm PIN") },
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        if (canBio) {
            Spacer(modifier = Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(checked = enableBio, onCheckedChange = { enableBio = it })
                Text("Use biometric when available")
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = {
            if (pin.length < 4) {
                error = "PIN too short"
            } else if (pin != confirm) {
                error = "PINs do not match"
            } else {
                SecureVault.setPin(context, pin)
                SecureVault.setUseBiometric(context, enableBio)
                onDone()
            }
        }) {
            Text("Secure Wallet")
        }

        if (error.isNotBlank()) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(error, color = MaterialTheme.colorScheme.error)
        }
    }
}
