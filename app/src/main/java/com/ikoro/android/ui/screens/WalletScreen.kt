package com.ikoro.android.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ikoro.android.data.model.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WalletScreen(
    wallet: MultiCurrencyWallet? = null,
    onSend: (currency: Currency) -> Unit = {},
    onReceive: (currency: Currency) -> Unit = {},
    onExchange: () -> Unit = {},
    onQRCode: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    // Default sample wallet if none provided
    val sampleWallet = remember {
        MultiCurrencyWallet(
            balances = mapOf(
                Currency.BITCOIN to CurrencyBalance(Currency.BITCOIN, 0.12567890),
                Currency.OFO to CurrencyBalance(Currency.OFO, 0.05),
                Currency.NAIRA to CurrencyBalance(Currency.NAIRA, 250000.00),
                Currency.USDT to CurrencyBalance(Currency.USDT, 500.00),
                Currency.USDC to CurrencyBalance(Currency.USDC, 300.00)
            ),
            lastUpdated = System.currentTimeMillis()
        )
    }

    val currentWallet = wallet ?: sampleWallet

    val transactions = remember {
        listOf(
            CurrencyTransaction("T001", "Received from @alice", 0.05, Currency.BITCOIN, TransactionType.RECEIVE, 1715097600000),
            CurrencyTransaction("T002", "Sent to @bob", 0.02, Currency.BITCOIN, TransactionType.SEND, 1715184000000),
            CurrencyTransaction("T003", "Received from @charlie", 0.1, Currency.BITCOIN, TransactionType.RECEIVE, 1715270400000),
            CurrencyTransaction("T004", "Exchanged to USDT", 0.08, Currency.USDT, TransactionType.EXCHANGE, 1715356800000),
            CurrencyTransaction("T005", "Receive OFO", 0.03, Currency.OFO, TransactionType.RECEIVE, 1715443200000),
        )
    }

    Column(
        modifier = modifier.fillMaxSize()
    ) {
        TopAppBar(
            title = { Text("Wallet") },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
            )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Primary Balance Card (Bitcoin)
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                val btcBalance = currentWallet.getBalance(Currency.BITCOIN)
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Primary Balance",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = btcBalance?.formatAmount() ?: "₿0.00000000",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    modifier = Modifier.weight(1f),
                    onClick = { onSend(Currency.BITCOIN) }
                ) {
                    Icon(Icons.Default.Send, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Send")
                }
                Button(
                    modifier = Modifier.weight(1f),
                    onClick = { onReceive(Currency.BITCOIN) }
                ) {
                    Icon(Icons.Default.CallReceived, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Receive")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    modifier = Modifier.weight(1f),
                    onClick = onExchange
                ) {
                    Icon(Icons.Default.Exchange, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Exchange")
                }
                OutlinedButton(
                    modifier = Modifier.weight(1f),
                    onClick = onQRCode
                ) {
                    Icon(Icons.Default.QrCode2, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("QR Code")
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "All Balances",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Multi-Currency Balance Cards
            Currency.values().forEach { currency ->
                val balance = currentWallet.getBalance(currency)
                if (balance != null) {
                    CurrencyBalanceItem(balance, onSend, onReceive)
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Recent Transactions",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(12.dp))

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(transactions) { transaction ->
                    TransactionCard(transaction)
                }
            }
        }
    }
}

@Composable
fun CurrencyBalanceItem(
    balance: CurrencyBalance,
    onSend: (Currency) -> Unit,
    onReceive: (Currency) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
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
                Card(
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Text(
                        text = balance.icon,
                        modifier = Modifier.padding(10.dp),
                        style = MaterialTheme.typography.titleLarge
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(
                        text = balance.currency.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = balance.symbol,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Column(
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = balance.formatAmount(),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    IconButton(
                        onClick = { onSend(balance.currency) },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            Icons.Default.Send,
                            contentDescription = "Send ${balance.currency.name}",
                            modifier = Modifier.size(20.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    IconButton(
                        onClick = { onReceive(balance.currency) },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            Icons.Default.CallReceived,
                            contentDescription = "Receive ${balance.currency.name}",
                            modifier = Modifier.size(20.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TransactionCard(transaction: CurrencyTransaction) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
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
                    imageVector = when (transaction.type) {
                        TransactionType.RECEIVE -> Icons.Default.CallReceived
                        TransactionType.SEND -> Icons.Default.Send
                        TransactionType.EXCHANGE -> Icons.Default.Exchange
                    },
                    contentDescription = null,
                    tint = when (transaction.type) {
                        TransactionType.RECEIVE -> MaterialTheme.colorScheme.primary
                        TransactionType.SEND -> MaterialTheme.colorScheme.error
                        TransactionType.EXCHANGE -> MaterialTheme.colorScheme.tertiary
                    }
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = transaction.description,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = formatTimestamp(transaction.timestamp),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = transaction.currency.name,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            Text(
                text = when (transaction.type) {
                    TransactionType.RECEIVE -> "+"
                    TransactionType.SEND -> "-"
                    TransactionType.EXCHANGE -> "↔"
                } + formatAmount(transaction.amount, transaction.currency),
                style = MaterialTheme.typography.titleMedium,
                color = when (transaction.type) {
                    TransactionType.RECEIVE -> MaterialTheme.colorScheme.primary
                    TransactionType.SEND -> MaterialTheme.colorScheme.error
                    TransactionType.EXCHANGE -> MaterialTheme.colorScheme.tertiary
                }
            )
        }
    }
}

fun formatAmount(amount: Double, currency: Currency): String {
    return when (currency) {
        Currency.BITCOIN -> "₿%.8f".format(amount)
        Currency.OFO -> "₿ỌFỌ%.8f".format(amount)
        Currency.NAIRA -> "₦%.2f".format(amount)
        Currency.USDT -> "USDT%.6f".format(amount)
        Currency.USDC -> "USDC%.6f".format(amount)
    }
}

fun formatTimestamp(timestamp: Long): String {
    val date = java.util.Date(timestamp)
    val format = java.text.SimpleDateFormat("MMM dd, HH:mm", java.util.Locale.getDefault())
    return format.format(date)
}