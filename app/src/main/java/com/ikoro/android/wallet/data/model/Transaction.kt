package com.ikoro.android.wallet.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.ikoro.android.data.model.TransactionStatus
import com.ikoro.android.data.model.TransactionType

/**
 * Transaction entity - stores all wallet transactions
 */
@Entity(tableName = "transactions")
data class Transaction(
    @PrimaryKey val id: String,
    
    // Identification
    @ColumnInfo(name = "wallet_id") val walletId: String,
    
    // Amount details
    val amount: Double,
    val currency: String, // BITCOIN, OFO, NAIRA, USDT, USDC
    
    // Type
    val type: String, // SEND, RECEIVE, EXCHANGE
    
    // Status
    val status: String = TransactionStatus.PENDING.name,
    
    // Addresses
    @ColumnInfo(name = "from_address") val fromAddress: String,
    @ColumnInfo(name = "to_address") val toAddress: String? = null,
    
    // Timestamps
    @ColumnInfo(name = "created_at") val createdAt: Long = System.currentTimeMillis(),
    @ColumnInfo(name = "signed_at") val signedAt: Long? = null,
    @ColumnInfo(name = "broadcast_at") val broadcastAt: Long? = null,
    @ColumnInfo(name = "confirmed_at") val confirmedAt: Long? = null,
    
    // Transaction details
    val fee: Double? = null,
    val hash: String? = null,
    val nonce: Long? = null,
    
    // Signing info
    @ColumnInfo(name = "is_signed") val isSigned: Boolean = false,
    @ColumnInfo(name = "is_broadcast") val isBroadcast: Boolean = false,
    @ColumnInfo(name = "is_confirmed") val isConfirmed: Boolean = false,
    
    // Mesh broadcast status
    @ColumnInfo(name = "mesh_broadcast_count") val meshBroadcastCount: Int = 0,
    @ColumnInfo(name = "mesh_verified") val meshVerified: Boolean = false,
    
    // Metadata
    val description: String? = null,
    val tags: String? = null,
    
    // Offline queue
    @ColumnInfo(name = "is_queued") val isQueued: Boolean = false,
    @ColumnInfo(name = "retry_count") val retryCount: Int = 0
) {
    fun isPending(): Boolean = status == TransactionStatus.PENDING.name
    fun isCompleted(): Boolean = status == TransactionStatus.COMPLETED.name
    fun isFailed(): Boolean = status == TransactionStatus.FAILED.name
}
