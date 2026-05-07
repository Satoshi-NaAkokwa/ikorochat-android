//
// ErrandScreen.kt
// Ikoro - ₿ỌFỌ Platform
//
// Errand logistics system
//

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ErrandScreen(modifier: Modifier = Modifier) {
    var showRequestDialog by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    val errands = remember { mutableStateListOf<Errand>() }

    // Sample data
    LaunchedEffect(Unit) {
        errands.addAll(
            listOf(
                Errand("E001", "Deliver grocery to Ikoyi", "Lagos Main Market", "Ikoyi", 0.005, ErrandStatus.IN_PROGRESS, System.currentTimeMillis() - 3600000),
                Errand("E002", "Pick up document from Lekki", "Lekki Phase 1", "Victoria Island", 0.003, ErrandStatus.PENDING, System.currentTimeMillis() - 7200000),
                Errand("E003", "Buy medicine from pharmacy", "Ikeja", "Surulere", 0.008, ErrandStatus.COMPLETED, System.currentTimeMillis() - 10800000)
            )
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    "Errand & Delivery",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "Request a runner for your errands",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )
            }

            Button(
                onClick = { showRequestDialog = true },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF6200EE)
                )
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Request Errand")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Stats cards
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StatCard("Active", errands.count { it.status == ErrandStatus.IN_PROGRESS }.toString(), Color(0xFF2196F3))
            StatCard("Pending", errands.count { it.status == ErrandStatus.PENDING }.toString(), Color(0xFFFF9800))
            StatCard("Completed", errands.count { it.status == ErrandStatus.COMPLETED }.toString(), Color(0xFF4CAF50))
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Errands list
        Text(
            "Your Errands",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(12.dp))

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(errands) { errand ->
                ErrandCard(errand = errand)
            }
        }
    }

    if (showRequestDialog) {
        RequestErrandDialog(
            onDismiss = { showRequestDialog = false },
            onRequest = { pickup, dropoff, items ->
                coroutineScope.launch {
                    errands.add(
                        Errand(
                            "E${(errands.size + 1).toString().padStart(3, '0')}",
                            items,
                            pickup,
                            dropoff,
                            estimatePrice(items.length),
                            ErrandStatus.PENDING,
                            System.currentTimeMillis()
                        )
                    )
                }
                showRequestDialog = false
            }
        )
    }
}

@Composable
fun StatCard(title: String, value: String, color: Color) {
    Card(
        modifier = Modifier
            .weight(1f),
        colors = CardDefaults.cardColors(containerColor = color)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(value, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = Color.White)
            Text(title, style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.9f))
        }
    }
}

@Composable
fun ErrandCard(errand: Errand) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    errand.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                StatusBadge(errand.status)
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.LocationOn, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.Gray)
                Spacer(modifier = Modifier.width(8.dp))
                Text("${errand.pickup} → ${errand.dropoff}", style = MaterialTheme.typography.bodyMedium)
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    "Price: ${errand.priceFormatted}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF4CAF50)
                )
                Text(
                    formatTime(errand.createdAt),
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }
        }
    }
}

@Composable
fun StatusBadge(status: ErrandStatus) {
    val (color, text) = when (status) {
        ErrandStatus.PENDING -> Color(0xFFFF9800) to "Pending"
        ErrandStatus.IN_PROGRESS -> Color(0xFF2196F3) to "In Progress"
        ErrandStatus.COMPLETED -> Color(0xFF4CAF50) to "Completed"
        ErrandStatus.CANCELLED -> Color(0xFFF44336) to "Cancelled"
    }

    Surface(
        shape = MaterialTheme.shapes.small,
        color = color
    ) {
        Text(
            text,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall,
            color = Color.White
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RequestErrandDialog(
    onDismiss: () -> Unit,
    onRequest: (String, String, String) -> Unit
) {
    var pickupLocation by remember { mutableStateOf("") }
    var dropoffLocation by remember { mutableStateOf("") }
    var items by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Request Errand") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                OutlinedTextField(
                    value = pickupLocation,
                    onValueChange = { pickupLocation = it },
                    label = { Text("Pickup Location") },
                    placeholder = { Text("Enter pickup address") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = dropoffLocation,
                    onValueChange = { dropoffLocation = it },
                    label = { Text("Dropoff Location") },
                    placeholder = { Text("Enter delivery address") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = items,
                    onValueChange = { items = it },
                    label = { Text("Items Description") },
                    placeholder = { Text("Describe items to pick up/deliver") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (pickupLocation.isNotBlank() && dropoffLocation.isNotBlank() && items.isNotBlank()) {
                        onRequest(pickupLocation, dropoffLocation, items)
                    }
                },
                enabled = pickupLocation.isNotBlank() && dropoffLocation.isNotBlank() && items.isNotBlank()
            ) {
                Text("Request")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

data class Errand(
    val id: String,
    val title: String,
    val pickup: String,
    val dropoff: String,
    val price: Double,
    val status: ErrandStatus,
    val createdAt: Long
) {
    val priceFormatted: String
        get() = "₿${String.format("%.8f", price)}"
}

enum class ErrandStatus {
    PENDING,
    IN_PROGRESS,
    COMPLETED,
    CANCELLED
}

fun estimatePrice(descriptionLength: Int): Double {
    // Simple price estimation based on description length (simulated)
    return 0.003 + (descriptionLength * 0.0001)
}

fun formatTime(timestamp: Long): String {
    val now = System.currentTimeMillis()
    val diff = now - timestamp

    return when {
        diff < 60000 -> "Just now"
        diff < 3600000 -> "${diff / 60000}m ago"
        diff < 86400000 -> "${diff / 3600000}h ago"
        else -> "${diff / 86400000}d ago"
    }
}