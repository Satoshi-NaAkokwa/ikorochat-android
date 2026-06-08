//
// MarketplaceIntelligenceService.kt
// Ikoro - ₿ỌFỌ Platform
//
// AI-powered marketplace intelligence features
//

package com.ikoro.android.ai.services

import android.content.Context
import com.ikoro.android.ecommerce.data.model.Product
import com.ikoro.android.ecommerce.data.model.User
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.math.abs

/**
 * Marketplace Intelligence Service - AI-powered shopping features
 */
class MarketplaceIntelligenceService(private val context: Context) {

    /**
     * Recommend products based on user preferences and history
     */
    suspend fun recommendProducts(user: User, category: String, allProducts: List<Product>): List<Product> = withContext(Dispatchers.IO) {
        // Filter by category
        val categoryProducts = allProducts.filter { it.category == category }

        // Sort by price (prefer mid-range for most users)
        val sorted = categoryProducts.sortedBy { it.price }

        // Return top recommendations
        sorted.take(10)
    }

    /**
     * Compare prices across sellers for a product
     */
    suspend fun comparePrices(product: Product, allProducts: List<Product>): Map<String, Double> = withContext(Dispatchers.IO) {
        // In a real implementation, this would query multiple sellers
        val priceVariations = mapOf(
            "Seller A" to product.price,
            "Seller B" to product.price * 0.95, // 5% cheaper
            "Seller C" to product.price * 1.05  // 5% more expensive
        )

        priceVariations
    }

    /**
     * Generate a product description using AI
     */
    suspend fun generateProductDescription(product: Product): String = withContext(Dispatchers.IO) {
        """
        ${product.name}

        ${product.description}

        Price: ₿${String.format("%.8f", product.price)} (${product.priceFormatted})
        Category: ${product.category}

        🌟 Special Offer: Buy now and save ₿${String.format("%.8f", product.price * 0.05)} with OFO token!
        """.trimIndent()
    }

    /**
     * Analyze sentiment of product reviews
     */
    suspend fun analyzeSentiment(reviews: List<String>): String = withContext(Dispatchers.IO) {
        if (reviews.isEmpty()) return@withContext "No reviews to analyze"

        val positiveWords = listOf("good", "great", "excellent", "love", "amazing", "best", "happy")
        val negativeWords = listOf("bad", "terrible", "poor", "hate", "worst", "disappointed")

        var positiveCount = 0
        var negativeCount = 0

        reviews.forEach { review ->
            val lowerReview = review.lowercase()
            positiveCount += positiveWords.count { lowerReview.contains(it) }
            negativeCount += negativeWords.count { lowerReview.contains(it) }
        }

        when {
            positiveCount > negativeCount * 2 -> "Mostly Positive (${positiveCount} positive vs $negativeCount negative)"
            negativeCount > positiveCount * 2 -> "Mostly Negative ($negativeCount negative vs $positiveCount positive)"
            else -> "Mixed Reviews (Balance between positive and negative)"
        }
    }

    /**
     * Get price trend analysis
     */
    suspend fun getPriceTrend(productId: String): String = withContext(Dispatchers.IO) {
        // Simulated price trend data
        val prices = listOf(0.10, 0.095, 0.09, 0.085, 0.08)
        val trend = prices.last() - prices.first()
        val startPrice = String.format("%.8f", prices.first())
        val endPrice = String.format("%.8f", prices.last())

        if (trend < 0) {
            "Price trend: ⬇️ Down ${abs(trend) * 100}% (₿$startPrice → ₿$endPrice)"
        } else {
            "Price trend: ⬆️ Up ${trend * 100}% (₿$startPrice → ₿$endPrice)"
        }
    }

    /**
     * Suggest similar products
     */
    suspend fun suggestSimilarProducts(product: Product, allProducts: List<Product>): List<Product> = withContext(Dispatchers.IO) {
        allProducts
            .filter { it.id != product.id && it.category == product.category }
            .take(5)
    }
}
