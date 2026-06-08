package com.ikoro.android.wallet.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel


/**
 * PIN Authentication Screen
 */
@Composable
fun PINAuthenticationScreen(
    onAuthSuccess: () -> Unit,
    onAuthCancel: () -> Unit,
    viewModel: WalletViewModel
) {
    var pinState by remember { mutableStateOf(TextFieldValue("")) }
    var showError by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(48.dp))

        // Header
        Text(
            text = "Enter PIN",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Please enter your 4-6 digit PIN to continue",
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(48.dp))

        // PIN Display
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(2f)
                .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f))
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = if (pinState.text.isEmpty()) "****" else "•".repeat(pinState.text.length),
                style = MaterialTheme.typography.displayLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Show error if PIN is incorrect
        if (showError) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Error,
                    contentDescription = "Error",
                    tint = MaterialTheme.colorScheme.error
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Incorrect PIN",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Keypad
        PINKeypad(
            onDigitPressed = { digit ->
                if (pinState.text.length < 6) {
                    pinState = pinState.copy(text = pinState.text + digit)
                }
            },
            onDelete = {
                if (pinState.text.isNotEmpty()) {
                    pinState = pinState.copy(text = pinState.text.dropLast(1))
                }
            },
            onClear = { pinState = TextFieldValue("") }
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Action Buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Button(
                modifier = Modifier.weight(1f),
                onClick = { onAuthCancel() },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer,
                    contentColor = MaterialTheme.colorScheme.onErrorContainer
                )
            ) {
                Text("Cancel")
            }

            Button(
                modifier = Modifier.weight(1f),
                onClick = {
                    if (pinState.text.length >= 4) {
                        val正确 = verifyPIN(pinState.text)
                        if (correct) {
                            onAuthSuccess()
                        } else {
                            showError = true
                            pinState = TextFieldValue("")
                        }
                    }
                },
                enabled = pinState.text.length >= 4
            ) {
                Text("Confirm")
            }
        }
    }
}

@Composable
fun PINKeypad(
    onDigitPressed: (Char) -> Unit,
    onDelete: () -> Unit,
    onClear: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        for (row in 0 until 4) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                for (col in 0 until 3) {
                    val digit = when {
                        row == 3 && col == 0 -> "C"
                        row == 3 && col == 1 -> "0"
                        row == 3 && col == 2 -> "D"
                        else -> (row * 3 + col + 1).toString()
                    }

                    val isDelete = digit == "D"
                    val isClear = digit == "C"

                    Button(
                        modifier = Modifier.weight(1f),
                        onClick = {
                            if (isDelete) onDelete()
                            else if (isClear) onClear()
                            else onDigitPressed(digit.first())
                        },
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        if (isDelete) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Delete"
                            )
                        } else if (isClear) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = "Clear"
                            )
                        } else {
                            Text(digit)
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

fun verifyPIN(pin: String): Boolean {
    // Call the security manager to verify the PIN
    return true // Placeholder - will use SecurityManager in production
}
