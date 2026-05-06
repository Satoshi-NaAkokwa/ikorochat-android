package com.ikoro.android.ecommerce.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import java.util.UUID

/**
 * User data model
 */
@Entity(tableName = "users")
data class User(
    @PrimaryKey
    @SerializedName("id")
    val id: String = UUID.randomUUID().toString(),

    @SerializedName("public_key")
    val publicKey: String,

    @SerializedName("identity_hash")
    val identityHash: String,

    @SerializedName("reputation_score")
    val reputationScore: Int = 0,

    @SerializedName("verification_level")
    val verificationLevel: Int = 0,

    @SerializedName("created_at")
    val createdAt: Long = System.currentTimeMillis(),

    @SerializedName("updated_at")
    val updatedAt: Long = System.currentTimeMillis()
)

/**
 * Product data model
 */
@Entity(tableName = "products")
data class Product(
    @PrimaryKey
    @SerializedName("id")
    val id: String = UUID.randomUUID().toString(),

    @SerializedName("seller_id")
    val sellerId: String,

    @SerializedName("title")
    val title: String,

    @SerializedName("description")
    val description: String,

    @SerializedName("price_satoshi")
    val priceSatoshi: Long,

    @SerializedName("currency")
    val currency: String = "BTC",

    @SerializedName("category_id")
    val categoryId: String?,

    @SerializedName("ipfs_hash")
    val ipfsHash: String,

    @SerializedName("encrypted_metadata")
    val encryptedMetadata: String?,

    @SerializedName("images")
    val images: List<String> = emptyList(),

    @SerializedName("created_at")
    val createdAt: Long = System.currentTimeMillis(),

    @SerializedName("updated_at")
    val updatedAt: Long = System.currentTimeMillis()
)

/**
 * Order data model
 */
@Entity(tableName = "orders")
data class Order(
    @PrimaryKey
    @SerializedName("id")
    val id: String = UUID.randomUUID().toString(),

    @SerializedName("buyer_id")
    val buyerId: String,

    @SerializedName("seller_id")
    val sellerId: String,

    @SerializedName("total_satoshi")
    val totalSatoshi: Long,

    @SerializedName("status")
    val status: OrderStatus = OrderStatus.PENDING,

    @SerializedName("escrow_tx_id")
    val escrowTxId: String?,

    @SerializedName("delivery_method")
    val deliveryMethod: String?,

    @SerializedName("delivery_address")
    val deliveryAddress: String?,

    @SerializedName("tracking_info")
    val trackingInfo: String?,

    @SerializedName("created_at")
    val createdAt: Long = System.currentTimeMillis(),

    @SerializedName("updated_at")
    val updatedAt: Long = System.currentTimeMillis()
)

/**
 * Order status enum
 */
enum class OrderStatus {
    PENDING,
    PAID,
    PROCESSING,
    SHIPPED,
    DELIVERED,
    COMPLETED,
    CANCELLED,
    DISPUTED
}

/**
 * Transaction data model
 */
@Entity(tableName = "transactions")
data class Transaction(
    @PrimaryKey
    @SerializedName("id")
    val id: String = UUID.randomUUID().toString(),

    @SerializedName("order_id")
    val orderId: String?,

    @SerializedName("tx_id")
    val txId: String,

    @SerializedName("amount_satoshi")
    val amountSatoshi: Long,

    @SerializedName("tx_type")
    val txType: TransactionType,

    @SerializedName("status")
    val status: TransactionStatus = TransactionStatus.PENDING,

    @SerializedName("created_at")
    val createdAt: Long = System.currentTimeMillis(),

    @SerializedName("updated_at")
    val updatedAt: Long = System.currentTimeMillis()
)

/**
 * Transaction type enum
 */
enum class TransactionType {
    PAYMENT,
    REFUND,
    ESCROW_DEPOSIT,
    ESCROW_RELEASE,
    REWARD,
    WITHDRAWAL
}

/**
 * Transaction status enum
 */
enum class TransactionStatus {
    PENDING,
    CONFIRMED,
    FAILED,
    CANCELLED
}

/**
 * Wallet data model
 */
@Entity(tableName = "wallets")
data class Wallet(
    @PrimaryKey
    @SerializedName("id")
    val id: String = UUID.randomUUID().toString(),

    @SerializedName("user_id")
    val userId: String,

    @SerializedName("address")
    val address: String,

    @SerializedName("balance_satoshi")
    val balanceSatoshi: Long = 0L,

    @SerializedName("currency")
    val currency: String = "BTC",

    @SerializedName("is_default")
    val isDefault: Boolean = false,

    @SerializedName("created_at")
    val createdAt: Long = System.currentTimeMillis(),

    @SerializedName("updated_at")
    val updatedAt: Long = System.currentTimeMillis()
)

/**
 * Category data model
 */
@Entity(tableName = "categories")
data class Category(
    @PrimaryKey
    @SerializedName("id")
    val id: String = UUID.randomUUID().toString(),

    @SerializedName("name")
    val name: String,

    @SerializedName("description")
    val description: String?,

    @SerializedName("parent_id")
    val parentId: String?,

    @SerializedName("icon")
    val icon: String?,

    @SerializedName("sort_order")
    val sortOrder: Int = 0,

    @SerializedName("created_at")
    val createdAt: Long = System.currentTimeMillis()
)

/**
 * Message data model
 */
@Entity(tableName = "messages")
data class Message(
    @PrimaryKey
    @SerializedName("id")
    val id: String = UUID.randomUUID().toString(),

    @SerializedName("sender_id")
    val senderId: String,

    @SerializedName("receiver_id")
    val receiverId: String,

    @SerializedName("order_id")
    val orderId: String?,

    @SerializedName("content")
    val content: String,

    @SerializedName("is_encrypted")
    val isEncrypted: Boolean = true,

    @SerializedName("is_read")
    val isRead: Boolean = false,

    @SerializedName("created_at")
    val createdAt: Long = System.currentTimeMillis()
)

/**
 * Verification data model
 */
@Entity(tableName = "verifications")
data class Verification(
    @PrimaryKey
    @SerializedName("id")
    val id: String = UUID.randomUUID().toString(),

    @SerializedName("user_id")
    val userId: String,

    @SerializedName("verification_type")
    val verificationType: VerificationType,

    @SerializedName("status")
    val status: VerificationStatus = VerificationStatus.PENDING,

    @SerializedName("validators")
    val validators: List<String> = emptyList(),

    @SerializedName("proof")
    val proof: String?,

    @SerializedName("created_at")
    val createdAt: Long = System.currentTimeMillis(),

    @SerializedName("updated_at")
    val updatedAt: Long = System.currentTimeMillis()
)

/**
 * Verification type enum
 */
enum class VerificationType {
    IDENTITY,
    PHONE,
    EMAIL,
    BIO,
    VIDEO,
    SOCIAL
}

/**
 * Verification status enum
 */
enum class VerificationStatus {
    PENDING,
    APPROVED,
    REJECTED,
    EXPIRED
}

/**
 * API Response wrapper
 */
data class ApiResponse<T>(
    @SerializedName("success")
    val success: Boolean,

    @SerializedName("data")
    val data: T?,

    @SerializedName("error")
    val error: String?,

    @SerializedName("message")
    val message: String?
)

/**
 * Paginated response
 */
data class PaginatedResponse<T>(
    @SerializedName("data")
    val data: List<T>,

    @SerializedName("page")
    val page: Int,

    @SerializedName("per_page")
    val perPage: Int,

    @SerializedName("total")
    val total: Int,

    @SerializedName("total_pages")
    val totalPages: Int
)