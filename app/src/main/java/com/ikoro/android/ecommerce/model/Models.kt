package com.ikoro.android.ecommerce.model

import java.util.UUID

/**
 * Product data model for ₿ Ọ F Ọ e-commerce
 */
data class Product(
    val id: String = UUID.randomUUID().toString(),
    val sellerId: String,
    val title: String,
    val description: String,
    val priceSatoshi: Long,
    val currency: String = "BTC",
    val categoryId: String? = null,
    val ipfsHash: String,
    val encryptedMetadata: String? = null,
    val images: List<String> = emptyList(),
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

/**
 * Order data model
 */
data class Order(
    val id: String = UUID.randomUUID().toString(),
    val buyerId: String,
    val sellerId: String,
    val totalSatoshi: Long,
    val status: OrderStatus = OrderStatus.PENDING,
    val escrowTxId: String? = null,
    val deliveryMethod: String? = null,
    val deliveryAddress: String? = null,
    val trackingInfo: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
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
data class Transaction(
    val id: String = UUID.randomUUID().toString(),
    val orderId: String? = null,
    val txId: String,
    val amountSatoshi: Long,
    val txType: TransactionType,
    val status: TransactionStatus = TransactionStatus.PENDING,
    val createdAt: Long = System.currentTimeMillis(),
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
data class Wallet(
    val id: String = UUID.randomUUID().toString(),
    val userId: String,
    val address: String,
    val balanceSatoshi: Long = 0L,
    val currency: String = "BTC",
    val isDefault: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

/**
 * Category data model
 */
data class Category(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val description: String? = null,
    val parentId: String? = null,
    val icon: String? = null,
    val sortOrder: Int = 0,
    val createdAt: Long = System.currentTimeMillis()
)

/**
 * Message data model
 */
data class Message(
    val id: String = UUID.randomUUID().toString(),
    val senderId: String,
    val receiverId: String,
    val orderId: String? = null,
    val content: String,
    val isEncrypted: Boolean = true,
    val isRead: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

/**
 * Verification data model
 */
data class Verification(
    val id: String = UUID.randomUUID().toString(),
    val userId: String,
    val verificationType: VerificationType,
    val status: VerificationStatus = VerificationStatus.PENDED,
    val validators: List<String> = emptyList(),
    val proof: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
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