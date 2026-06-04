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
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.ikoro.android.wallet.ui.viewmodel.WalletViewModel


/**
 * Send Transaction Screen
 */
@Composable
fun SendTransactionScreen(
    onSent: () -> Unit,
    onBack: () -> Unit,
    viewModel: WalletViewModel
) {
    var amountState by remember { mutableStateOf(TextFieldValue("")) }
    var addressState by remember { mutableStateOf(TextFieldValue("")) }
    var descriptionState by remember { mutableStateOf(TextFieldValue("")) }
    var requiresAuth by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Send ${viewModel.selectedCurrency.value}",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back")
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Amount Input
        OutlinedTextField(
            value = amountState,
            onValueChange = { amountState = it },
            label = { Text("Amount") },
            prefix = { Text("${getCurrencySymbol(viewModel.selectedCurrency.value)} ") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Address Input
        OutlinedTextField(
            value = addressState,
            onValueChange = { addressState = it },
            label = { Text("Recipient Address") },
            leadingIcon = { Icon(Icons.Default.QrCode2, contentDescription = null) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Description Input
        OutlinedTextField(
            value = descriptionState,
            onValueChange = { descriptionState = it },
            label = { Text("Description (Optional)") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Current Balance Display
        Text(
            text = "Available Balance: ${viewModel.getBalance()} ${viewModel.selectedCurrency.value}",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Action Buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                modifier = Modifier.weight(1f),
                onClick = onBack
            ) {
                Text("Cancel")
            }

            OutlinedButton(
                modifier = Modifier.weight(1f),
                onClick = {
                    if (amountState.text.toDoubleOrNull() != null && addressState.text.isNotEmpty()) {
                        requiresAuth = true
                    }
                },
                enabled = amountState.text.toDoubleOrNull() != null && addressState.text.isNotEmpty()
            ) {
                Text("Next")
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }

    // Authentication Sheet if needed
    if (requiresAuth) {
        PINAuthenticationScreen(
            onAuthSuccess = {
                // Create transaction
                val amount = amountState.text.toDoubleOrNull() ?: 0.0
                viewModel.createTransaction(
                    amount = amount,
                    toAddress = addressState.text,
                    description = descriptionState.text.ifEmpty { null }
                )
                requiresAuth = false
                onSent()
            },
            onAuthCancel = { requiresAuth = false },
            viewModel = viewModel
        )
    }
}

fun getCurrencySymbol(currency: String): String {
    return when (currency) {
        "BITCOIN" -> "₿"
        "OFO" -> "₿ỌFỌ"
        "NAIRA" -> "₦"
        "USDT" -> "USDT"
        "USDC" -> "USDC"
        else -> "$"
    }
}
