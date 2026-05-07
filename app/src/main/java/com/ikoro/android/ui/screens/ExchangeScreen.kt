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
fun ExchangeScreen(
    wallet: MultiCurrencyWallet,
    exchangeRates: CachedExchangeRates,
    onExchange: (from: Currency, to: Currency, amount: Double) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedFromCurrency by remember { mutableStateOf(Currency.BITCOIN) }
    var selectedToCurrency by remember { mutableStateOf(Currency.USDT) }
    var fromAmount by remember { mutableStateOf("") }
    var toAmount by remember { mutableStateOf("0.0") }

    val fromBalance = wallet.getBalance(selectedFromCurrency)?.amount ?: 0.0
    val exchangeRate = exchangeRates.getRate(selectedFromCurrency, selectedToCurrency)

    // Calculate converted amount
    LaunchedEffect(fromAmount, exchangeRate) {
        if (fromAmount.isNotEmpty() && exchangeRate != null) {
            toAmount = try {
                val amount = fromAmount.toDouble()
                (amount * exchangeRate.rate).toString()
            } catch (e: NumberFormatException) {
                "0.0"
            }
        }
    }

    Column(
        modifier = modifier.fillMaxSize()
    ) {
        TopAppBar(
            title = { Text("Exchange") },
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
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp)
                ) {
                    Text(
                        text = "Exchange Rate",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "1 ${selectedFromCurrency.name} = ${exchangeRate?.rate ?: 0.0} ${selectedToCurrency.name}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )

                    if (exchangeRates.isExpired()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        AssistChip(
                            onClick = { },
                            label = { Text("Rate may be outdated") },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.Warning,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // From Currency Section
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
                        Text(
                            text = "From",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = "Balance: ${wallet.getBalance(selectedFromCurrency)?.formatAmount() ?: "0.0"}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = fromAmount,
                        onValueChange = { fromAmount = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("0.0") },
                        trailingIcon = {
                            var expanded by remember { mutableStateOf(false) }

                            Box {
                                Button(
                                    onClick = { expanded = true },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                                    )
                                ) {
                                    Text(selectedFromCurrency.name)
                                    Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                                }

                                DropdownMenu(
                                    expanded = expanded,
                                    onDismissRequest = { expanded = false }
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
                                                selectedFromCurrency = currency
                                                expanded = false
                                            }
                                        )
                                    }
                                }
                            }
                        },
                        singleLine = true
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Swap Button
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(40.dp),
                contentAlignment = Alignment.Center
            ) {
                FloatingActionButton(
                    onClick = {
                        val temp = selectedFromCurrency
                        selectedFromCurrency = selectedToCurrency
                        selectedToCurrency = temp
                    },
                    containerColor = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(Icons.Default.SwapVert, contentDescription = "Swap currencies")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // To Currency Section
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "To",
                        style = MaterialTheme.typography.titleMedium
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = toAmount,
                        onValueChange = { },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("0.0") },
                        trailingIcon = {
                            var expanded by remember { mutableStateOf(false) }

                            Box {
                                Button(
                                    onClick = { expanded = true },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                                    )
                                ) {
                                    Text(selectedToCurrency.name)
                                    Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                                }

                                DropdownMenu(
                                    expanded = expanded,
                                    onDismissRequest = { expanded = false }
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
                                                selectedToCurrency = currency
                                                expanded = false
                                            }
                                        )
                                    }
                                }
                            }
                        },
                        singleLine = true,
                        readOnly = true
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Exchange Button
            Button(
                onClick = {
                    if (fromAmount.isNotEmpty()) {
                        onExchange(selectedFromCurrency, selectedToCurrency, fromAmount.toDouble())
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = fromAmount.isNotEmpty() &&
                          fromAmount.toDoubleOrNull() != null &&
                          fromAmount.toDoubleOrNull() ?: 0.0 > 0 &&
                          fromAmount.toDoubleOrNull() ?: 0.0 <= fromBalance
            ) {
                Icon(Icons.Default.Exchange, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Exchange")
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Info Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.tertiaryContainer
                )
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Info,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onTertiaryContainer
                    )
                    Text(
                        text = "Exchange rates are cached for 1 hour for use during offline periods.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onTertiaryContainer
                    )
                }
            }
        }
    }
}