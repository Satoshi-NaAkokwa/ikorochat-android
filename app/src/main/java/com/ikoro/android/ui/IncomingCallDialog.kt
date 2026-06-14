package com.ikoro.android.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun IncomingCallDialog(
    peer: String?,
    onAccept: () -> Unit,
    onReject: () -> Unit
) {
    AlertDialog(
        onDismissRequest = { },
        confirmButton = {
            Button(onClick = onAccept) {
                Text("Accept")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onReject) {
                Text("Reject")
            }
        },
        title = {
            Text("Incoming call", fontWeight = FontWeight.Bold)
        },
        text = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("${peer?.take(20) ?: "Unknown"} is calling")
                Spacer(modifier = Modifier.height(8.dp))
                CircularProgressIndicator()
            }
        }
    )
}
