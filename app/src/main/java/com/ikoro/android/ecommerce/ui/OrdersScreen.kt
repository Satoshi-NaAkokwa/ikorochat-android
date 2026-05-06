package com.ikoro.android.ecommerce.ui

import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.ikoro.android.ecommerce.model.Order
import com.ikoro.android.ecommerce.viewmodel.OrdersViewModel

/**
 * Orders Screen for ₿ Ọ F Ọ
 * Displays user orders with status tracking
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrdersScreen(
    onOrderClick: (String) -> Unit,
    viewModel: OrdersViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {
    val orders by viewModel.orders.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Orders") }
            )
        }
    ) { paddingValues ->
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (orders.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        Icons.Default.ShoppingCart,
                        contentDescription = "No Orders",
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "No orders yet",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(orders) { order ->
                    OrderCard(
                        order = order,
                        onClick = { onOrderClick(order.id) }
                    )
                }
            }
        }
    }
}

@Composable
fun OrderCard(
    order: Order,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
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
                    text = "Order #${order.id.take(8)}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                StatusChip(status = order.status)
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Total:",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Text(
                    text = formatSatoshi(order.totalSatoshi),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Calendar,
                    contentDescription = "Date",
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = formatDate(order.createdAt),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun StatusChip(status: com.ikoro.android.ecommerce.model.OrderStatus) {
    val (color, label) = when (status) {
        com.ikoro.android.ecommerce.model.OrderStatus.PENDING -> Color(0xFFFF9800) to "Pending"
        com.ikoro.android.ecommerce.model.OrderStatus.PAID -> Color(0xFF2196F3) to "Paid"
        com.ikoro.android.ecommerce.model.OrderStatus.PROCESSING -> Color(0xFF9C27B0) to "Processing"
        com.ikoro.android.ecommerce.model.OrderStatus.SHIPPED -> Color(0xFF4CAF50) to "Shipped"
        com.ikoro.android.ecommerce.model.OrderStatus.DELIVERED -> Color(0xFF4CAF50) to "Delivered"
        com.ikoro.android.ecommerce.model.OrderStatus.COMPLETED -> Color(0xFF4CAF50) to "Completed"
        com.ikoro.android.ecommerce.model.OrderStatus.CANCELLED -> Color(0xFFF44336) to "Cancelled"
        com.ikoro.android.ecommerce.model.OrderStatus.DISPUTED -> Color(0xFFF44336) to "Disputed"
    }

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = color.copy(alpha = 0.2f)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = color,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun OrderDetailScreen(
    orderId: String,
    onBackClick: () -> Unit,
    viewModel: OrdersViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {
    val order by viewModel.selectedOrder.collectAsState()

    LaunchedEffect(orderId) {
        viewModel.loadOrder(orderId)
    }

    if (order == null) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
        return
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Order Details") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Order Status Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    StatusChip(status = order.status)

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = formatSatoshi(order.totalSatoshi),
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Text(
                        text = order.currency,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            // Order Timeline
            Text(
                text = "Order Timeline",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            Timeline(
                items = listOf(
                    mapOf(
                        "status" to "pending",
                        "timestamp" to System.currentTimeMillis() - 3600000,
                        "description" to "Order created"
                    ),
                    mapOf(
                        "status" to order.status,
                        "timestamp" to order.createdAt,
                        "description" to "Current status"
                    )
                )
            )

            // Order Actions
            when (order.status) {
                com.ikoro.android.ecommerce.model.OrderStatus.DELIVERED -> {
                    Button(
                        onClick = { viewModel.completeOrder(order.id) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Mark as Received")
                    }
                }
                com.ikoro.android.ecommerce.model.OrderStatus.PAID,
                com.ikoro.android.ecommerce.model.OrderStatus.PROCESSING -> {
                    OutlinedButton(
                        onClick = { /* Dispute */ },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Warning, null, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Report Issue")
                    }
                }
                else -> {}
            }
        }
    }
}

@Composable
fun Timeline(
    items: List<Map<String, Any>>
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            items.forEachIndexed { index, item ->
            Row(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .background(
                                if (index == 0) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.secondaryContainer
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Circle,
                            contentDescription = "Status",
                            modifier = Modifier.size(8.dp),
                            tint = if (index == 0) MaterialTheme.colorScheme.onPrimary
                            else MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                    if (index < items.size - 1) {
                        Divider(
                            modifier = Modifier
                                .width(2.dp)
                                .height(40.dp),
                            color = MaterialTheme.colorScheme.secondaryContainer
                        )
                    }
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = (item["status"] as? String) ?: "",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = (item["description"] as? String) ?: "",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = formatDate((item["timestamp"] as? Long ?: 0)),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (index < items.size - 1) {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
fun formatSatoshi(satoshi: Long): String {
    val btc = satoshi / 100000000.0
    return "₿%.8f".format(btc)
}

@Composable
fun formatDate(timestamp: Long): String {
    val date = java.util.Date(timestamp)
    val format = java.text.SimpleDateFormat("MMM dd, yyyy HH:mm", java.util.Locale.getDefault())
    return format.format(date)
}