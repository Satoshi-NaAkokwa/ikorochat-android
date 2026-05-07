//
// ProductModels.kt
// Ikoro - ₿ỌFỌ Platform
//
// Extended data models for visual e-commerce
//

package com.ikoro.android.ecommerce.data.model

/**
 * Product Image Model
 */
data class ProductImage(
    val id: String,
    val url: String,
    val caption: String? = null,
    val isPrimary: Boolean = false
)

/**
 * Product Video Model
 */
data class ProductVideo(
    val id: String,
    val url: String,
    val thumbnail: String? = null,
    val duration: Long, // in seconds
    val caption: String? = null
)

/**
 * Seller Profile Model
 */
data class SellerProfile(
    val id: String,
    val name: String,
    val avatar: String? = null,
    val isVerified: Boolean = false,
    val rating: Float = 0.0f,
    val reviewCount: Int = 0,
    val description: String? = null,
    val location: String? = null,
    val joinedDate: Long,
    val responseTime: String? = "Usually responds within 1 hour",
    val totalSales: Int = 0
)

/**
 * Product Review Model
 */
data class ProductReview(
    val id: String,
    val productId: String,
    val userId: String,
    val userName: String,
    val userAvatar: String? = null,
    val rating: Float,
    val comment: String,
    val images: List<String> = emptyList(),
    val videos: List<String> = emptyList(),
    val createdAt: Long,
    val helpfulCount: Int = 0,
    val isVerifiedPurchase: Boolean = false
)

/**
 * Extended Product Model with Media
 */
data class ProductWithMedia(
    val product: Product,
    val images: List<ProductImage>,
    val videos: List<ProductVideo>,
    val seller: SellerProfile,
    val reviews: List<ProductReview>,
    val inStock: Boolean = true,
    val stockQuantity: Int = 0
)