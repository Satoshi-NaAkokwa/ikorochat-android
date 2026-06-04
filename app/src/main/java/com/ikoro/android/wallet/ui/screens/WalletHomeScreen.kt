package com.ikoro.android.wallet.ui.screens

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavController
import com.ikoro.android.wallet.ui.viewmodel.WalletViewModel


/**
 * Wallet Home Screen - Main wallet UI
 */
@Composable
fun WalletHomeScreen(
    navController: NavController
) {
    val viewModel: WalletViewModel = hiltViewModel()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // Wallet Balance Card
        WalletBalanceCard(viewModel)

        Spacer(modifier = Modifier.height(24.dp))

        // Quick Actions
        QuickActions(viewModel)

        Spacer(modifier = Modifier.height(24.dp))

        // Currency Selection
        CurrencySelector(viewModel)

        Spacer(modifier = Modifier.height(24.dp))

        // Transaction History
        TransactionHistory(viewModel)
    }
}

@Composable
fun WalletBalanceCard(viewModel: WalletViewModel) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primary
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Total Balance",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onPrimary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "${viewModel.getBalance()} ${viewModel.selectedCurrency.value}",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimary
            )
        }
    }
}

@Composable
fun QuickActions(viewModel: WalletViewModel) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Button(
            modifier = Modifier.weight(1f),
            onClick = { /* TODO: Open Send Screen */ }
        ) {
            Icon(Icons.Default.Send, contentDescription = "Send")
            Spacer(modifier = Modifier.width(8.dp))
            Text("Send")
        }
        Button(
            modifier = Modifier.weight(1f),
            onClick = { /* TODO: Open Receive Screen */ }
        ) {
            Icon(Icons.Default.CallReceived, contentDescription = "Receive")
            Spacer(modifier = Modifier.width(8.dp))
            Text("Receive")
        }
    }

    Spacer(modifier = Modifier.height(12.dp))

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        OutlinedButton(
            modifier = Modifier.weight(1f),
            onClick = { /* TODO: Open Exchange Screen */ }
        ) {
            Icon(Icons.Default.Exchange, contentDescription = "Exchange")
            Spacer(modifier = Modifier.width(8.dp))
            Text("Exchange")
        }
        OutlinedButton(
            modifier = Modifier.weight(1f),
            onClick = { /* TODO: Generate QR */ }
        ) {
            Icon(Icons.Default.QrCode2, contentDescription = "QR Code")
            Spacer(modifier = Modifier.width(8.dp))
            Text("QR")
        }
    }
}

@Composable
fun CurrencySelector(viewModel: WalletViewModel) {
    Text(
        text = "Select Currency",
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.SemiBold
    )
    Spacer(modifier = Modifier.height(12.dp))

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        CurrencyChip("₿", "BITCOIN", viewModel)
        CurrencyChip("₿ỌFỌ", "OFO", viewModel)
        CurrencyChip("₦", "NAIRA", viewModel)
        CurrencyChip("USDT", "USDT", viewModel)
        CurrencyChip("USDC", "USDC", viewModel)
    }
}

@Composable
fun CurrencyChip(icon: String, currency: String, viewModel: WalletViewModel) {
    val selected = viewModel.selectedCurrency.value == currency
    Button(
        modifier = Modifier.weight(1f),
        onClick = { viewModel.setSelectedCurrency(currency) },
        colors = ButtonDefaults.buttonColors(
            containerColor = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Text(icon)
    }
}

@Composable
fun TransactionHistory(viewModel: WalletViewModel) {
    Text(
        text = "Recent Transactions",
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.SemiBold
    )
    Spacer(modifier = Modifier.height(12.dp))

    if (viewModel.transactions.value.isEmpty()) {
        Text(
            text = "No transactions yet",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    } else {
        viewModel.transactions.value.forEach { txn ->
            TransactionItem(txn)
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
fun TransactionItem(txn: com.ikoro.android.wallet.data.model.Transaction) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = if (txn.type == "SEND") Icons.Default.Send else Icons.Default.CallReceived,
                    contentDescription = null
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = txn.description ?: "Transaction",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = txn.currency,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Text(
                text = when (txn.type) {
                    "SEND" -> "-"
                    "RECEIVE" -> "+"
                    else -> ""
                } + txn.amount.toString(),
                style = MaterialTheme.typography.titleMedium,
                color = if (txn.type == "SEND") MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
            )
        }
    }
}

// Sample transaction for preview
private val sampleTxn = com.ikoro.android.wallet.data.model.Transaction(
    id = "T001",
    walletId = "default",
    amount = 0.05,
    currency = "BITCOIN",
    type = "RECEIVE",
    fromAddress = "addr1",
    toAddress = "addr2",
    description = "Received from Alice",
    createdAt = System.currentTimeMillis() - 3600000
)
