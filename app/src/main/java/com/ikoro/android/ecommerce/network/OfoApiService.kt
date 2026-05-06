package com.ikoro.android.ecommerce.network

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import javax.inject.Inject
import javax.inject.Singleton

/**
 * ₿ Ọ F Ọ API Service
 * Handles communication with the ₿ Ọ F Ọ backend API
 */
@Singleton
class OfoApiService @Inject constructor(
    private val httpClient: OkHttpClient,
    private val json: Json
) {

    private val BASE_URL = "http://192.168.1.100:3000/api/v1" // Local development server
    private const val AUTH_HEADER = "Authorization"

    private suspend fun <T> apiCall(
        endpoint: String,
        method: String = "GET",
        body: String? = null,
        token: String? = null,
        clazz: Class<T>
    ): T {
        return withContext(Dispatchers.IO) {
            val requestBuilder = Request.Builder()
                .url("$BASE_URL$endpoint")
                .method(method, null)

            token?.let { requestBuilder.addHeader(AUTH_HEADER, "Bearer $it") }

            body?.let {
                val mediaType = "application/json; charset=utf-8".toMediaType()
                val requestBody = body.toRequestBody(mediaType)
                requestBuilder.post(requestBody)
            }

            val request = requestBuilder.build()
            val response = httpClient.newCall(request).execute()

            if (!response.isSuccessful) {
                throw Exception("API call failed: ${response.code}")
            }

            try {
                json.decodeFromString(clazz, response.body?.string() ?: "{}")
            } catch (e: Exception) {
                throw Exception("Failed to parse response: ${e.message}")
            }
        }
    }

    // Authentication
    suspend fun register(publicKey: String): AuthResponse {
        return apiCall(
            endpoint = "/auth/register",
            method = "POST",
            body = """{"public_key": "$publicKey", "device_info": {"platform": "android", "version": "1.0.0"}}""",
            clazz = AuthResponse::class.java
        )
    }

    suspend fun login(challenge: String, signature: String): AuthResponse {
        return apiCall(
            endpoint = "/auth/login",
            method = "POST",
            body = """{"challenge": "$challenge", "signature": "$signature"}""",
            clazz = AuthResponse::class.java
        )
    }

    // Products
    suspend fun getProducts(): List<Product> {
        val response = apiCall(
            endpoint = "/products",
            method = "GET",
            clazz = ProductListResponse::class.java
        )
        return response.data ?: emptyList()
    }

    suspend fun searchProducts(query: String, category: String?): List<Product> {
        val endpoint = if (category != null) {
            "/products?search=$query&category=$category"
        } else {
            "/products?search=$query"
        }

        val response = apiCall(
            endpoint = endpoint,
            method = "GET",
            clazz = ProductListResponse::class.java
        )
        return response.data ?: emptyList()
    }

    suspend fun createProduct(
        title: String,
        description: String,
        priceSatoshi: Long,
        images: List<String>
    ): Product {
        val response = apiCall(
            endpoint = "/products",
            method = "POST",
            body = """{"title": "$title", "description": "$description", "price_satoshi": $priceSatoshi, "images": ${images.joinToString(",", "[", "]") { "\"$it\"" } }}""",
            clazz = ProductResponse::class.java
        )
        return response.data ?: throw Exception("Failed to create product")
    }

    // Wallet
    suspend fun getWallets(): List<Wallet> {
        val response = apiCall(
            endpoint = "/wallet",
            method = "GET",
            clazz = WalletListResponse::class.java
        )
        return response.data ?: emptyList()
    }

    suspend fun createWallet(
        label: String,
        isDefault: Boolean
    ): Wallet {
        val response = apiCall(
            endpoint = "/wallet",
            method = "POST",
            body = """{"label": "$label", "is_default": $isDefault}""",
            clazz = WalletResponse::class.java
        )
        return response.data ?: throw Exception("Failed to create wallet")
    }

    suspend fun sendBitcoin(
        toAddress: String,
        amountSatoshi: Long
    ): String {
        val response = apiCall(
            endpoint = "/wallet/send",
            method = "POST",
            body = """{"to_address": "$toAddress", "amount_satoshi": $amountSatoshi}""",
            clazz = TransactionResponse::class.java
        )
        return response.data?.txId ?: throw Exception("Failed to send Bitcoin")
    }

    suspend fun getTransactions(walletId: String): List<Transaction> {
        val response = apiCall(
            endpoint = "/wallet/$walletId/transactions",
            method = "GET",
            clazz = TransactionListResponse::class.java
        )
        return response.data ?: emptyList()
    }

    // Orders
    suspend fun getOrders(): List<Order> {
        val response = apiCall(
            endpoint = "/orders",
            method = "GET",
            clazz = OrderListResponse::class.java
        )
        return response.data ?: emptyList()
    }

    suspend fun getOrder(orderId: String): Order {
        val response = apiCall(
            endpoint = "/orders/$orderId",
            method = "GET",
            clazz = OrderResponse::class.java
        )
        return response.data ?: throw Exception("Order not found")
    }

    suspend fun createOrder(
        sellerId: String,
        products: List<OrderItemDto>
    ): Order {
        val productsJson = products.joinToString(",", "[", "]") {
            """{"product_id": "${it.productId}", "quantity": ${it.quantity}}"""
        }

        val response = apiCall(
            endpoint = "/orders",
            method = "POST",
            body = """{"seller_id": "$sellerId", "products": $productsJson}""",
            clazz = OrderResponse::class.java
        )
        return response.data ?: throw Exception("Failed to create order")
    }

    suspend fun updateOrderStatus(
        orderId: String,
        status: String
    ): Order {
        val response = apiCall(
            endpoint = "/orders/$orderId/status",
            method = "PUT",
            body = """{"status": "$status"}""",
            clazz = OrderResponse::class.java
        )
        return response.data ?: throw Exception("Failed to update order status")
    }

    suspend fun completeOrder(orderId: String): Order {
        val response = apiCall(
            endpoint = "/orders/$orderId/complete",
            method = "POST",
            clazz = OrderResponse::class.java
        )
        return response.data ?: throw Exception("Failed to complete order")
    }
}

// Response models
@Serializable
data class AuthResponse(
    val success: Boolean,
    val data: AuthData?,
    val message: String?
)

@Serializable
data class AuthData(
    val user: User,
    val token: String,
    val refresh_token: String
)

@Serializable
data class User(
    val id: String,
    val public_key: String,
    val identity_hash: String,
    val reputation_score: Int,
    val verification_level: Int,
    val created_at: Long,
    val updated_at: Long
)

@Serializable
data class ProductListResponse(
    val success: Boolean,
    val data: List<Product>?,
    val pagination: Pagination?
)

@Serializable
data class ProductResponse(
    val success: Boolean,
    val data: Product?,
    val message: String?
)

@Serializable
data class WalletListResponse(
    val success: Boolean,
    val data: List<Wallet>?,
    val message: String?
)

@Serializable
data class WalletResponse(
    val success: Boolean,
    val data: Wallet?,
    val message: String?
)

@Serializable
data class TransactionListResponse(
    val data: List<Transaction>,
    val hasMore: Boolean,
    val nextOffset: Int?
)

@Serializable
data class TransactionResponse(
    val success: Boolean,
    val data: Transaction?,
    val message: String?
)

@Serializable
data class OrderListResponse(
    val success: Boolean,
    val data: List<Order>?,
    val pagination: Pagination?
)

@Serializable
data class OrderResponse(
    val success: Boolean,
    val data: Order?,
    val message: String?
)

@Serializable
data class Pagination(
    val page: Int,
    val per_page: Int,
    val total: Int,
    val total_pages: Int
)

@Serializable
data class OrderItemDto(
    val product_id: String,
    val quantity: Int
)

@Serializable
data class Product(
    val id: String,
    val seller_id: String,
    val title: String,
    val description: String,
    val price_satoshi: Long,
    val currency: String,
    val category_id: String? = null,
    val ipfs_hash: String,
    val encrypted_metadata: String? = null,
    val images: List<String>,
    val created_at: Long,
    val updated_at: Long
)

@Serializable
data class Wallet(
    val id: String,
    val user_id: String,
    val address: String,
    val balance_satoshi: Long,
    val currency: String,
    val is_default: Boolean,
    val created_at: Long,
    val updated_at: Long
)

@Serializable
data class Transaction(
    val id: String,
    val order_id: String?,
    val tx_id: String,
    val amount_satoshi: Long,
    val tx_type: String,
    val status: String,
    val created_at: Long,
    val updated_at: Long
)

@Serializable
data class Order(
    val id: String,
    val buyer_id: String,
    val seller_id: String,
    val total_satoshi: Long,
    val status: String,
    val escrow_tx_id: String? = null,
    val delivery_method: String? = null,
    val delivery_address: String? = null,
    val tracking_info: String? = null,
    val created_at: Long,
    val updated_at: Long,
    val currency: String? = null
)