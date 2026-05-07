//
// LiveShoppingScreen.kt
// Ikoro - ₿ỌFỌ Platform
//
// Live shopping with video broadcasts

package com.ikoro.android.ecommerce.ui.screens

import androidx.compose.foundation.background
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LiveShoppingScreen(modifier: Modifier = Modifier) {
    var showCreateStream by remember { mutableStateOf(false) }
    val liveStreams = remember { mutableStateListOf<LiveStream>() }

    // Sample live streams
    LaunchedEffect(Unit) {
        liveStreams.addAll(
            listOf(
                LiveStream(
                    "LS001",
                    "Fashion Week Special",
                    "₿ỌFỌ Fashion Store",
                    isLive = true,
                    viewerCount = 1250,
                    thumbnail = "https://via.placeholder.com/300",
                    startsAt = System.currentTimeMillis()
                ),
                LiveStream(
                    "LS002",
                    "Electronics Flash Sale",
                    "Tech Haven",
                    isLive = true,
                    viewerCount = 892,
                    thumbnail = "https://via.placeholder.com/300",
                    startsAt = System.currentTimeMillis() - 1800000
                ),
                LiveStream(
                    "LS003",
                    "Grocery Haul Alert",
                    "FreshMart",
                    isLive = false,
                    viewerCount = 0,
                    thumbnail = "https://via.placeholder.com/300",
                    startsAt = System.currentTimeMillis() + 3600000
                )
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
            Text(
                "Live Shopping",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )

            Button(
                onClick = { showCreateStream = true },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFE53935) // Red for live
                )
            ) {
                Icon(Icons.Default.VideoCall, contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Go Live")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Live streams
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(liveStreams) { stream ->
                LiveStreamCard(stream = stream)
            }
        }

        if (showCreateStream) {
            CreateStreamDialog(
                onDismiss = { showCreateStream = false },
                onCreateStream = { streamTitle, streamCategory ->
                    liveStreams.add(
                        LiveStream(
                            "LS${liveStreams.size + 1}",
                            streamTitle,
                            "You",
                            isLive = true,
                            viewerCount = 0,
                            thumbnail = "https://via.placeholder.com/300",
                            startsAt = System.currentTimeMillis()
                        )
                    )
                    showCreateStream = false
                }
            )
        }
    }
}

@Composable
fun LiveStreamCard(stream: LiveStream) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { /* Open stream */ }
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Stream header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (stream.isLive) {
                    Surface(
                        shape = MaterialTheme.shapes.small,
                        color = Color(0xFFE53935)
                    ) {
                        Text(
                            "● LIVE",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                } else {
                    Surface(
                        shape = MaterialTheme.shapes.small,
                        color = Color(0xFF757575)
                    ) {
                        Text(
                            "⏰ Scheduled",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White
                        )
                    }
                }

                if (stream.viewerCount > 0) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Visibility,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = Color.Gray
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            "${stream.viewerCount.toLocaleString()}",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Stream title and seller
            Text(
                stream.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(4.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.Storefront,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = Color(0xFF6200EE)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    stream.sellerName,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )
            }

            // Product spotlight (simplified)
            Spacer(modifier = Modifier.height(16.dp))

            Surface(
                shape = MaterialTheme.shapes.medium,
                color = Color(0xFFF5F5F5)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        "Product Spotlight",
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.Gray
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "₿0.05",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF4CAF50)
                            )
                            Text(
                                "Sample Product",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.Gray
                            )
                        }

                        Button(onClick = { /* Buy now */ }) {
                            Text("Buy Now")
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateStreamDialog(
    onDismiss: () -> Unit,
    onCreateStream: (String, String) -> Unit
) {
    var streamTitle by remember { mutableStateOf("") }
    var streamCategory by remember { mutableStateOf("General") }

    val categories = listOf("General", "Fashion", "Electronics", "Food", "Home", "Beauty", "Automotive")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Create Live Stream") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                OutlinedTextField(
                    value = streamTitle,
                    onValueChange = { streamTitle = it },
                    label = { Text("Stream Title") },
                    placeholder = { Text("e.g., Fashion Week Special") },
                    modifier = Modifier.fillMaxWidth()
                )

                var expanded by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = it }
                ) {
                    OutlinedTextField(
                        readOnly = true,
                        value = streamCategory,
                        onValueChange = {},
                        label = { Text("Category") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )

                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        categories.forEach { category ->
                            DropdownMenuItem(
                                text = { Text(category) },
                                onClick = {
                                    streamCategory = category
                                    expanded = false
                                }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (streamTitle.isNotBlank()) {
                        onCreateStream(streamTitle, streamCategory)
                    }
                },
                enabled = streamTitle.isNotBlank()
            ) {
                Text("Go Live")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

data class LiveStream(
    val id: String,
    val title: String,
    val sellerName: String,
    val isLive: Boolean,
    val viewerCount: Int,
    val thumbnail: String? = null,
    val startsAt: Long
)