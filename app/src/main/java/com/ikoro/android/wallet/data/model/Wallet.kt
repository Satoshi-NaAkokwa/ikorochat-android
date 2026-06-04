package com.ikoro.android.wallet.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

/**
 * Wallet entity - stores wallet state and metadata
 */
@Serializable
@Entity(tableName = "wallets")
data class Wallet(
    @PrimaryKey val id: String = "default",
    val userId: String,
    
    // Public keys
    @ColumnInfo(name = "public_key") val publicKey: String,
    @ColumnInfo(name = "private_key_encrypted") val privateKeyEncrypted: String? = null,
    
    // Balances
    @ColumnInfo(name = "bitcoin_balance") val bitcoinBalance: Double = 0.0,
    @ColumnInfo(name = "of o_balance") val ofoBalance: Double = 0.0,
    @ColumnInfo(name = "naira_balance") val nairaBalance: Double = 0.0,
    @ColumnInfo(name = "usdt_balance") val usdtBalance: Double = 0.0,
    @ColumnInfo(name = "usdc_balance") val usdcBalance: Double = 0.0,
    
    // Metadata
    @ColumnInfo(name = "created_at") val createdAt: Long = System.currentTimeMillis(),
    @ColumnInfo(name = "last_updated") val lastUpdated: Long = System.currentTimeMillis(),
    @ColumnInfo(name = "last_sync") val lastSync: Long? = null,
    
    // Security settings
    @ColumnInfo(name = "requires_pin") val requiresPin: Boolean = true,
    @ColumnInfo(name = "requires_biometric") val requiresBiometric: Boolean = false,
    @ColumnInfo(name = "max_transaction_amount") val maxTransactionAmount: Double = 0.1,
    
    // State
    @ColumnInfo(name = "is_initialized") val isInitialized: Boolean = false
) {
    fun getTotalBalance(): Double = bitcoinBalance + ofoBalance + nairaBalance + usdtBalance + usdcBalance
}
