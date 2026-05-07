package com.ikoro.android.ui.screens

import android.net.Uri
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
import com.ikoro.android.media.CompactImageCarousel
import com.ikoro.android.media.MediaPicker

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MarketplaceScreen(modifier: Modifier = Modifier) {
    var searchQuery by remember { mutableStateOf("") }
    var showAddProductDialog by remember { mutableStateOf(false) }

    val products = remember {
        listOf(
            ProductItem(
                "P001",
                "Organic Coffee Beans",
                "Premium Arabica coffee beans from Ethiopia",
                0.05,
                "Food",
                "₿0.05",
                listOf() // Empty list for products without images
            ),
            ProductItem(
                "P002",
                "Handmade Leather Wallet",
                "Handcrafted leather wallet with RFID blocking",
                0.02,
                "Accessories",
                "₿0.02",
                listOf()
            ),
            ProductItem(
                "P003",
                "Smart Watch",
                "Fitness tracker with heart rate monitor",
                0.15,
                "Electronics",
                "₿0.15",
                listOf()
            ),
            ProductItem(
                "P004",
                "Wireless Earbuds",
                "Noise-canceling Bluetooth earbuds",
                0.08,
                "Electronics",
                "₿0.08",
                listOf()
            ),
            ProductItem(
                "P005",
                "Eco-Friendly Tote Bag",
                "Reusable cotton tote bag",
                0.005,
                "Accessories",
                "₿0.005",
                listOf()
            ),
        )
    }

    Column(
        modifier = modifier.fillMaxSize()
    ) {
        TopAppBar(
            title = { Text("Marketplace") },
            actions = {
                IconButton(onClick = { showAddProductDialog = true }) {
                    Icon(Icons.Default.Add, contentDescription = "Add Product")
                }
            },
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
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Search products...") },
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = null)
                },
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(products) { product ->
                    ProductCard(product)
                }
            }
        }
    }

    if (showAddProductDialog) {
        AddProductDialog(
            onDismiss = { showAddProductDialog = false },
            onProductAdded = { /* Handle product addition */ }
        )
    }
}

@Composable
fun ProductCard(product: ProductItem) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Product image carousel (if images exist)
            if (product.images.isNotEmpty()) {
                CompactImageCarousel(
                    images = product.images,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    onImageClick = { /* Open full screen carousel */ }
                )
                Spacer(modifier = Modifier.height(12.dp))
            }

            Text(
                text = product.title,
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = product.description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                SuggestionChip(
                    onClick = { },
                    label = { Text(product.category) }
                )
                Text(
                    text = product.priceDisplay,
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
fun AddProductDialog(
    onDismiss: () -> Unit,
    onProductAdded: (ProductItem) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("") }
    var productImages by remember { mutableStateOf<List<Uri>>(emptyList()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add New Product") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Product Title") },
                    singleLine = true
                )

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") },
                    minLines = 3,
                    maxLines = 5
                )

                OutlinedTextField(
                    value = price,
                    onValueChange = { price = it },
                    label = { Text("Price (BTC)") },
                    singleLine = true
                )

                OutlinedTextField(
                    value = category,
                    onValueChange = { category = it },
                    label = { Text("Category") },
                    singleLine = true
                )

                // Media picker for product images
                if (productImages.isNotEmpty()) {
                    Text(
                        text = "Product Images (${productImages.size})",
                        style = MaterialTheme.typography.labelMedium
                    )
                    CompactImageCarousel(
                        images = productImages,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(150.dp)
                    )
                }

                MediaPicker(
                    onMediaSelected = { mediaItems ->
                        // Extract URIs from media items
                        val newImages = mediaItems.mapNotNull { it.uri }
                        productImages = productImages + newImages
                    },
                    maxSelection = 5
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank() && price.isNotBlank()) {
                        val priceBtc = price.toDoubleOrNull() ?: 0.0
                        val product = ProductItem(
                            id = "P${System.currentTimeMillis()}",
                            title = title,
                            description = description,
                            priceBtc = priceBtc,
                            category = category.ifBlank { "Other" },
                            priceDisplay = "₿$price",
                            images = productImages
                        )
                        onProductAdded(product)
                        onDismiss()
                    }
                }
            ) {
                Text("Add Product")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

data class ProductItem(
    val id: String,
    val title: String,
    val description: String,
    val priceBtc: Double,
    val category: String,
    val priceDisplay: String,
    val images: List<Uri> = emptyList()
)
