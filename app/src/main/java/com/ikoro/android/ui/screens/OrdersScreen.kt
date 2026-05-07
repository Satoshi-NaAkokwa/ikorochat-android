package com.ikoro.android.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrdersScreen() {
    val orders = remember {
        listOf(
            Order("O001", "Organic Coffee Beans", "Processing", 1715097600000, 0.05),
            Order("O002", "Handmade Leather Wallet", "Shipped", 1715184000000, 0.02),
            Order("O003", "Smart Watch", "Completed", 1715270400000, 0.15),
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Orders") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(orders) { order ->
                OrderCard(order)
            }
        }
    }
}

@Composable
fun OrderCard(order: Order) {
    val (status, statusColor) = when (order.status) {
        "Processing" -> "Processing" to MaterialTheme.colorScheme.tertiary
        "Shipped" -> "Shipped" to MaterialTheme.colorScheme.primary
        "Completed" -> "Completed" to MaterialTheme.colorScheme.secondary
        else -> "Unknown" to MaterialTheme.colorScheme.onSurfaceVariant
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = order.productName,
                    style = MaterialTheme.typography.titleMedium
                )
                SuggestionChip(
                    onClick = { },
                    label = { Text(status) },
                    colors = SuggestionChipDefaults.suggestionChipColors(
                        containerColor = statusColor.copy(alpha = 0.2f)
                    )
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "₿%.8f".format(order.amount),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = formatTimestamp(order.timestamp),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

data class Order(
    val id: String,
    val productName: String,
    val status: String,
    val timestamp: Long,
    val amount: Double
)