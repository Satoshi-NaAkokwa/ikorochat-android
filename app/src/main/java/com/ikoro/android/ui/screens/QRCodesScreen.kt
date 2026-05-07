package com.ikoro.android.ui.screens

import androidx.compose.foundation.layout.*
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
fun QRCodesScreen(
    wallet: MultiCurrencyWallet,
    onGenerateQR: (currency: Currency, amount: Double?, description: String?) -> Unit,
    onScannedQR: (paymentData: PaymentQRCode) -> Unit,
    modifier: Modifier = Modifier
) {
    var showGenerateDialog by remember { mutableStateOf(false) }
    var selectedCurrency by remember { mutableStateOf(Currency.BITCOIN) }
    var amount by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }

    // Sample QR codes
    val recentQRCodes = remember {
        listOf(
            PaymentQRCode(
                id = "QR001",
                address = "bc1qxy2kgdygjrsqtzq2n0yrf2493p83kkfjhx0wlh",
                currency = Currency.BITCOIN,
                amount = 0.05,
                description = "Coffee payment",
                createdAt = System.currentTimeMillis() - 3600000
            ),
            PaymentQRCode(
                id = "QR002",
                address = "ofo1xyzabc123def456",
                currency = Currency.OFO,
                amount = null,
                description = "Payment for service",
                createdAt = System.currentTimeMillis() - 7200000
            )
        )
    }

    Column(
        modifier = modifier.fillMaxSize()
    ) {
        TopAppBar(
            title = { Text("QR Payments") },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
            ),
            actions = {
                IconButton(onClick = { showGenerateDialog = true }) {
                    Icon(Icons.Default.Add, contentDescription = "Generate QR Code")
                }
            }
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Quick Actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    modifier = Modifier.weight(1f),
                    onClick = { showGenerateDialog = true }
                ) {
                    Icon(Icons.Default.QrCode2, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Generate")
                }

                Button(
                    modifier = Modifier.weight(1f),
                    onClick = { /* TODO: Open scanner */ }
                ) {
                    Icon(Icons.Default.CenterFocusStrong, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Scan")
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Recent QR Codes
            Text(
                text = "Recent QR Codes",
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(modifier = Modifier.height(12.dp))

            recentQRCodes.forEach { qrCode ->
                QRCodeCard(qrCode) { onScannedQR(qrCode) }
                Spacer(modifier = Modifier.height(12.dp))
            }

            // All Currencies Balance Overview
            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Your Balances",
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(modifier = Modifier.height(12.dp))

            Currency.values().forEach { currency ->
                val balance = wallet.getBalance(currency)
                if (balance != null) {
                    CurrencyBalanceCard(balance) {
                        selectedCurrency = currency
                        showGenerateDialog = true
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }

    // Generate QR Dialog
    if (showGenerateDialog) {
        GenerateQRDialog(
            selectedCurrency = selectedCurrency,
            amount = amount,
            description = description,
            onCurrencyChange = { selectedCurrency = it },
            onAmountChange = { amount = it },
            onDescriptionChange = { description = it },
            onGenerate = {
                onGenerateQR(
                    selectedCurrency,
                    amount.toDoubleOrNull(),
                    description.ifBlank { null }
                )
                showGenerateDialog = false
                amount = ""
                description = ""
            },
            onDismiss = { showGenerateDialog = false }
        )
    }
}

@Composable
fun QRCodeCard(
    qrCode: PaymentQRCode,
    onCopy: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.QrCode2,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = qrCode.description ?: "Payment Request",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "${qrCode.currency.name} - ${formatTimestamp(qrCode.createdAt)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                IconButton(onClick = onCopy) {
                    Icon(Icons.Default.ContentCopy, contentDescription = "Copy")
                }
            }

            if (qrCode.amount != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Amount: ${qrCode.amount} ${qrCode.currency.name}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = qrCode.address,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1
            )
        }
    }
}

@Composable
fun CurrencyBalanceCard(
    balance: CurrencyBalance,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        onClick = onClick
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
                        modifier = Modifier.padding(8.dp),
                        style = MaterialTheme.typography.titleLarge
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
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
                Icon(
                    Icons.Default.QrCode2,
                    contentDescription = "Generate QR",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GenerateQRDialog(
    selectedCurrency: Currency,
    amount: String,
    description: String,
    onCurrencyChange: (Currency) -> Unit,
    onAmountChange: (String) -> Unit,
    onDescriptionChange: (String) -> Unit,
    onGenerate: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Generate Payment QR") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Select Currency",
                    style = MaterialTheme.typography.titleSmall
                )

                var currencyDropdownExpanded by remember { mutableStateOf(false) }

                Box {
                    OutlinedButton(
                        onClick = { currencyDropdownExpanded = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(selectedCurrency.name)
                        Spacer(modifier = Modifier.weight(1f))
                        Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                    }

                    DropdownMenu(
                        expanded = currencyDropdownExpanded,
                        onDismissRequest = { currencyDropdownExpanded = false }
                    ) {
                        Currency.values().forEach { currency ->
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        when (currency) {
                                            Currency.BITCOIN -> "₿ Bitcoin"
                                            Currency.OFO -> "₿ỌFỌ"
                                            Currency.NAIRA -> "₦ Naira"
                                            Currency.USDT -> "USDT"
                                            Currency.USDC -> "USDC"
                                        }
                                    )
                                },
                                onClick = {
                                    onCurrencyChange(currency)
                                    currencyDropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = amount,
                    onValueChange = onAmountChange,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Amount (optional)") },
                    placeholder = { Text("0.0") },
                    singleLine = true
                )

                OutlinedTextField(
                    value = description,
                    onValueChange = onDescriptionChange,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Description (optional)") },
                    placeholder = { Text("Payment for...") },
                    maxLines = 2
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onGenerate,
                enabled = amount.isEmpty() || amount.toDoubleOrNull() != null
            ) {
                Text("Generate QR")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}