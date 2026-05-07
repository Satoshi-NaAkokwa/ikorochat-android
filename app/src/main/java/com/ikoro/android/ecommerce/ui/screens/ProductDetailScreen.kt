//
// ProductDetailScreen.kt
// Ikoro - ₿ỌFỌ Platform
//
// Detailed product view with images, videos, reviews, and seller info
//

package com.ikoro.android.ecommerce.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.ikoro.android.ecommerce.data.model.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductDetailScreen(
    product: Product,
    modifier: Modifier = Modifier
) {
    var selectedTabIndex by remember { mutableStateOf(0) }
    val tabs = listOf("Details", "Reviews", "Seller")

    // Sample media data
    val images = remember {
        listOf(
            ProductImage("img1", "https://via.placeholder.com/300", isPrimary = true),
            ProductImage("img2", "https://via.placeholder.com/300"),
            ProductImage("img3", "https://via.placeholder.com/300"),
            ProductImage("img4", "https://via.placeholder.com/300")
        )
    }

    val videos = remember {
        listOf(
            ProductVideo("vid1", "https://via.placeholder.com/video", duration = 120, caption = "Product demo")
        )
    }

    val seller = remember {
        SellerProfile(
            id = "S001",
            name = "₿ỌFỌ Official Store",
            isVerified = true,
            rating = 4.8f,
            reviewCount = 1234,
            description = "Your trusted source for quality products with ₿ỌFỌ",
            location = "Lagos, Nigeria",
            joinedDate = System.currentTimeMillis() - 31536000000,
            totalSales = 5678
        )
    }

    val reviews = remember {
        listOf(
            ProductReview(
                "R001",
                product.id,
                "U001",
                "John Doe",
                rating = 5.0f,
                comment = "Great product! Exactly as described.",
                createdAt = System.currentTimeMillis() - 86400000,
                helpfulCount = 12,
                isVerifiedPurchase = true
            ),
            ProductReview(
                "R002",
                product.id,
                "U002",
                "Jane Smith",
                rating = 4.5f,
                comment = "Good quality, fast delivery. Would recommend!",
                createdAt = System.currentTimeMillis() - 172800000,
                helpfulCount = 8,
                isVerifiedPurchase = true
            )
        )
    }

    Column(modifier = modifier.fillMaxSize()) {
        // Image gallery
        ImageGallery(images = images, videos = videos)

        // Product info
        ProductInfo(product = product)

        // Tabs
        TabRow(selectedTabIndex = selectedTabIndex) {
            tabs.forEachIndexed { index, tab ->
                Tab(
                    selected = selectedTabIndex == index,
                    onClick = { selectedTabIndex = index },
                    text = { Text(tab) }
                )
            }
        }

        // Tab content
        when (selectedTabIndex) {
            0 -> ProductDetailsTab(product = product)
            1 -> ProductReviewsTab(reviews = reviews)
            2 -> SellerProfileTab(seller = seller)
        }

        Spacer(modifier = Modifier.height(80.dp)) // Bottom padding
    }
}

@Composable
fun ImageGallery(images: List<ProductImage>, videos: List<ProductVideo>) {
    val pagerState = rememberPagerState(pageCount = images.size + videos.size)

    Column {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
        ) { page ->
            if (page < images.size) {
                AsyncImage(
                    model = images[page].url,
                    contentDescription = "Product image",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                VideoThumbnail(
                    video = videos[page - images.size],
                    modifier = Modifier.fillMaxSize()
                )
            }
        }

        // Page indicators
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            repeat(pagerState.pageCount) { page ->
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .background(
                            if (pagerState.currentPage == page) Color(0xFF6200EE) else Color.Gray,
                            CircleShape
                        )
                )
            }
        }
    }
}

@Composable
fun VideoThumbnail(video: ProductVideo, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black),
        contentAlignment = Alignment.Center
    ) {
        AsyncImage(
            model = video.thumbnail ?: "https://via.placeholder.com/300",
            contentDescription = "Video thumbnail",
            contentScale = ContentScale.Fit,
            modifier = Modifier.fillMaxSize()
        )

        // Play button overlay
        Surface(
            shape = CircleShape,
            color = Color.White.copy(alpha = 0.9f),
            modifier = Modifier.padding(8.dp)
        ) {
            Icon(Icons.Default.PlayArrow, contentDescription = "Play", tint = Color.Black)
        }

        // Duration
        Text(
            formatDuration(video.duration),
            style = MaterialTheme.typography.bodySmall,
            color = Color.White,
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(8.dp)
        )
    }
}

@Composable
fun ProductInfo(product: Product) {
    Column(modifier = Modifier.padding(16.dp)) {
        Text(
            product.name,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            product.priceFormatted,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF4CAF50)
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            product.description,
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            ProductDetailChip("Category: ${product.category}")
            ProductDetailChip("In Stock")
        }
    }
}

@Composable
fun ProductDetailChip(text: String) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = Color(0xFFE8EAF6)
    ) {
        Text(
            text,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            style = MaterialTheme.typography.bodySmall,
            color = Color(0xFF6200EE)
        )
    }
}

@Composable
fun ProductDetailsTab(product: Product) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            "Product Description",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(product.description)

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            "Features",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(12.dp))

        val features = listOf(
            "High quality materials",
            "₿ỌFỌ accepted",
            "Fast delivery",
            "100% offline verification",
            "Seller verified"
        )

        features.forEach { feature ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF4CAF50), modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(feature)
            }
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
fun ProductReviewsTab(reviews: List<ProductReview>) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            "Customer Reviews",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (reviews.isEmpty()) {
            Text("No reviews yet. Be the first to review!", color = Color.Gray)
        } else {
            reviews.forEach { review ->
                ReviewCard(review = review)
                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }
}

@Composable
fun ReviewCard(review: ProductReview) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = CircleShape,
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(Icons.Default.Person, contentDescription = null)
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            review.userName,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        if (review.isVerifiedPurchase) {
                            Text(
                                "✓ Verified Purchase",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF4CAF50)
                            )
                        }
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFFFA000), modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("${review.rating}")
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(review.comment, style = MaterialTheme.typography.bodyMedium)

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    formatTime(review.createdAt),
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.ThumbUp, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("${review.helpfulCount}", style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}

@Composable
fun SellerProfileTab(seller: SellerProfile) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Seller header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = CircleShape,
                modifier = Modifier.size(80.dp)
            ) {
                Icon(Icons.Default.Storefront, contentDescription = null, modifier = Modifier.padding(20.dp))
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        seller.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    if (seller.isVerified) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Surface(
                            shape = CircleShape,
                            color = Color(0xFF03DAC6)
                        ) {
                            Icon(Icons.Default.Verified, contentDescription = "Verified", tint = Color.White, modifier = Modifier.padding(4.dp))
                        }
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFFFA000), modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("${seller.rating} (${seller.reviewCount} reviews)", style = MaterialTheme.typography.bodySmall)
                }
            }

            Button(onClick = { /* Follow seller */ }) {
                Text("Follow")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Seller info
        seller.location?.let { location ->
            InfoRow(Icons.Default.LocationOn, location)
        }

        Spacer(modifier = Modifier.height(8.dp))

        seller.responseTime?.let { responseTime ->
            InfoRow(Icons.Default.Schedule, responseTime)
        }

        Spacer(modifier = Modifier.height(8.dp))

        InfoRow(Icons.Default.ShoppingCart, "${seller.totalSales} sales")

        Spacer(modifier = Modifier.height(8.dp))

        InfoRow(Icons.Default.Calendar, "Joined ${formatTime(seller.joinedDate)}")

        Spacer(modifier = Modifier.height(16.dp))

        // Stats
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            SellerStat("Response", "Fast")
            SellerStat("On-time", "98%")
            SellerStat("Rating", "4.8/5")
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Description
        if (!seller.description.isNullOrBlank()) {
            Text(
                "About Seller",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(seller.description, color = Color.Gray)
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Action buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                onClick = { /* Message seller */ },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6200EE))
            ) {
                Text("Message")
            }

            OutlinedButton(
                onClick = { /* View other products */ },
                modifier = Modifier.weight(1f)
            ) {
                Text("View Products")
            }
        }
    }
}

@Composable
fun InfoRow(icon: androidx.compose.ui.graphics.vector.ImageVector, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = null, modifier = Modifier.size(20.dp), tint = Color.Gray)
        Spacer(modifier = Modifier.width(12.dp))
        Text(text, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
fun SellerStat(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
        Text(label, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
    }
}

fun formatDuration(seconds: Long): String {
    val minutes = seconds / 60
    val remainingSeconds = seconds % 60
    return "${minutes}:${remainingSeconds.toString().padStart(2, '0')}"
}

fun formatTime(timestamp: Long): String {
    val diff = System.currentTimeMillis() - timestamp
    return when {
        diff < 60000 -> "Just now"
        diff < 3600000 -> "${diff / 60000}m ago"
        diff < 86400000 -> "${diff / 3600000}h ago"
        diff < 2592000000 -> "${diff / 86400000}d ago"
        else -> "${diff / 2592000000}M ago"
    }
}