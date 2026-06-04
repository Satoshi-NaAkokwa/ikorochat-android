package com.ikoro.android.wallet.model

import androidx.room.*
import com.ikoro.android.data.model.TransactionStatus
import com.ikoro.android.data.model.TransactionType
import java.util.UUID

/**
 * Entity representing a transaction
 */
@Entity(
    tableName = "transactions",
    primaryKeys = ["id"],
    indices = [
        Index(value = ["walletCurrency"]),
        Index(value = ["walletCurrency", "status"]),
        Index(value = ["walletCurrency", "timestamp"]),
        Index(value = ["walletCurrency", "address"]),
    ]
)
data class TransactionEntity(
    @ColumnInfo(name = "id") val id: String = UUID.randomUUID().toString(),
    
    @ColumnInfo(name = "walletCurrency") val walletCurrency: String,
    
    @ColumnInfo(name = "address") val address: String,
    
    @ColumnInfo(name = "from_address") val fromAddress: String? = null,
    
    @ColumnInfo(name = "to_address") val toAddress: String? = null,
    
    @ColumnInfo(name = "amount") val amount: Double,
    
    @ColumnInfo(name = "fee") val fee: Double = 0.0,
    
    @ColumnInfo(name = "type") val type: String = TransactionType.SEND.name,
    
    @ColumnInfo(name = "status") val status: String = TransactionStatus.PENDING.name,
    
    @ColumnInfo(name = "timestamp") val timestamp: Long = System.currentTimeMillis(),
    
    @ColumnInfo(name = "block_height") val blockHeight: Int? = null,
    
    @ColumnInfo(name = "transaction_hash") val transactionHash: String? = null,
    
    @ColumnInfo(name = "confirmations") val confirmations: Int = 0,
    
    @ColumnInfo(name = "max_confirmations") val maxConfirmations: Int = 6,
    
    @ColumnInfo(name = "memo") val memo: String? = null,
    
    @ColumnInfo(name = "metadata") val metadata: String? = null,
    
    @ColumnInfo(name = "created_at") val createdAt: Long = System.currentTimeMillis(),
    
    @ColumnInfo(name = "updated_at") val updatedAt: Long = System.currentTimeMillis(),
    
    @ColumnInfo(name = "error_message") val errorMessage: String? = null
) {
    fun getStatusEnum(): TransactionStatus {
        return TransactionStatus.valueOf(status)
    }
    
    fun getTypeEnum(): TransactionType {
        return TransactionType.valueOf(type)
    }
    
    fun getCurrencyEnum(): String {
        return walletCurrency
    }
    
    fun isPending(): Boolean {
        return status == TransactionStatus.PENDING.name
    }
    
    fun isCompleted(): Boolean {
        return status == TransactionStatus.COMPLETED.name
    }
    
    fun isFailed(): Boolean {
        return status == TransactionStatus.FAILED.name
    }
    
    fun canBroadcast(): Boolean {
        return isPending() && confirmations < maxConfirmations
    }
    
    fun getConfirmationsRemaining(): Int {
        return max(0, maxConfirmations - confirmations)
    }
    
    fun isFullyConfirmed(): Boolean {
        return confirmations >= maxConfirmations
    }
    
    fun markAsConfirmed(confirmations: Int) {
        this.confirmations = max(confirmations, maxConfirmations)
        this.status = TransactionStatus.COMPLETED.name
        this.updatedAt = System.currentTimeMillis()
    }
    
    fun markAsFailed(error: String) {
        this.status = TransactionStatus.FAILED.name
        this.errorMessage = error
        this.updatedAt = System.currentTimeMillis()
    }
}
