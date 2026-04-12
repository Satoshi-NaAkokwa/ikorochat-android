package com.bitchat.android.ui.settings

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun OpenClawSettingsSheet() {
    var isConnected by remember { mutableStateOf(false) }
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "OpenClaw Settings",
            style = MaterialTheme.typography.titleLarge
        )
        
        if (isConnected) {
            Text("Status: Connected")
            Button(
                onClick = { isConnected = false }
            ) {
                Text("Disconnect")
            }
        } else {
            Text("Status: Not Connected")
            Button(
                onClick = { isConnected = true }
            ) {
                Text("Pair Device")
            }
        }
        
        Text(
            text = "OpenClaw provides AI-assisted feature development with zero-risk sandbox.",
            style = MaterialTheme.typography.bodySmall
        )
    }
}