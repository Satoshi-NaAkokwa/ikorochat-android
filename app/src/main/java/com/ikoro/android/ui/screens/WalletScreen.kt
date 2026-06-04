// WalletScreen.kt - Updated with backend integration
// See WalletScreen.kt in wallet module for full backend integration
// This file is a placeholder - actual implementation in wallet module

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

// Re-export wallet service types for UI access
import com.ikoro.android.wallet.service.TransactionResult
import com.ikoro.android.wallet.service.BalanceSyncResult
import com.ikoro.android.wallet.service.ExchangeResult
import com.ikoro.android.wallet.viewmodel.WalletViewModel
import com.ikoro.android.wallet.manager.TransactionQueueManager.QueueStats

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WalletScreen(
    walletViewModel: WalletViewModel = com.ikoro.android.wallet.viewmodel.WalletViewModel::class.java.let {
        androidx.lifecycle.viewmodel.compose.viewModel()
    },
    modifier: Modifier = Modifier
) {
    val wallets by walletViewModel.wallets.collectAsState()
    val selectedCurrency by walletViewModel.selectedCurrency.collectAsState()
    
    // Show wallet screen with backend integration
    WalletScreenContent(
        wallets = wallets,
        selectedCurrency = selectedCurrency,
        modifier = modifier,
        onSend = { currency ->
            // TODO: Implement send flow
        },
        onReceive = { currency ->
            // TODO: Implement receive flow  
        },
        onExchange = {
            // TODO: Implement exchange flow
        }
    )
}

@Composable
private fun WalletScreenContent(
    wallets: List<com.ikoro.android.wallet.service.WalletInfo>,
    selectedCurrency: com.ikoro.android.data.model.Currency?,
    modifier: Modifier = Modifier,
    onSend: (com.ikoro.android.data.model.Currency) -> Unit = {},
    onReceive: (com.ikoro.android.data.model.Currency) -> Unit = {},
    onExchange: () -> Unit = {}
) {
    Column(modifier = modifier.fillMaxSize()) {
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
                val btcWallet = wallets.find { it.currency == com.ikoro.android.data.model.Currency.BITCOIN }
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
                        text = btcWallet?.let { formatAmount(it.balance, it.currency) } ?: "₿0.00000000",
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
                    onClick = { onSend(com.ikoro.android.data.model.Currency.BITCOIN) }
                ) {
                    Icon(Icons.Default.Send, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Send")
                }
                Button(
                    modifier = Modifier.weight(1f),
                    onClick = { onReceive(com.ikoro.android.data.model.Currency.BITCOIN) }
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
                    modifier = Modifier.weight(1f)
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
            wallets.forEach { wallet ->
                CurrencyBalanceItem(
                    balance = com.ikoro.android.data.model.CurrencyBalance(
                        currency = wallet.currency,
                        amount = wallet.balance
                    ),
                    onSend = onSend,
                    onReceive = onReceive
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Text(
                text = "Recent Transactions",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
fun CurrencyBalanceItem(
    balance: com.ikoro.android.data.model.CurrencyBalance,
    onSend: (com.ikoro.android.data.model.Currency) -> Unit,
    onReceive: (com.ikoro.android.data.model.Currency) -> Unit
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

fun formatAmount(amount: Double, currency: com.ikoro.android.data.model.Currency): String {
    return when (currency) {
        com.ikoro.android.data.model.Currency.BITCOIN -> "₿%.8f".format(amount)
        com.ikoro.android.data.model.Currency.OFO -> "₿ỌFỌ%.8f".format(amount)
        com.ikoro.android.data.model.Currency.NAIRA -> "₦%.2f".format(amount)
        com.ikoro.android.data.model.Currency.USDT -> "USDT%.6f".format(amount)
        com.ikoro.android.data.model.Currency.USDC -> "USDC%.6f".format(amount)
    }
}
