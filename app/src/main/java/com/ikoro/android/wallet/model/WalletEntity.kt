package com.ikoro.android.wallet.model

import androidx.room.*
import com.ikoro.android.data.model.Currency
import java.util.concurrent.TimeUnit

/**
 * Entity representing a wallet for a specific currency
 */
@Entity(
    tableName = "wallets",
    primaryKeys = ["currency"],
    indices = [
        Index(value = ["currency"], unique = true)
    ]
)
data class WalletEntity(
    @ColumnInfo(name = "currency") val currency: String,
    
    @ColumnInfo(name = "wallet_address") val walletAddress: String,
    
    @ColumnInfo(name = "public_key") val publicKey: String,
    
    @ColumnInfo(name = "balance") val balance: Double = 0.0,
    
    @ColumnInfo(name = "available_balance") val availableBalance: Double = 0.0,
    
    @ColumnInfo(name = "pending_balance") val pendingBalance: Double = 0.0,
    
    @ColumnInfo(name = "last_synced") val lastSynced: Long = System.currentTimeMillis(),
    
    @ColumnInfo(name = "last_updated") val lastUpdated: Long = System.currentTimeMillis(),
    
    @ColumnInfo(name = "coin_data") val coinData: CoinData? = null,
    
    @ColumnInfo(name = "is_encrypted") val isEncrypted: Boolean = false,
    
    @ColumnInfo(name = "created_at") val createdAt: Long = System.currentTimeMillis(),
    
    @ColumnInfo(name = "last_used") val lastUsed: Long = System.currentTimeMillis()
) {
    fun getCurrencyEnum(): Currency {
        return Currency.valueOf(currency)
    }
}

/**
 * Data class for currency-specific coin data
 */
data class CoinData(
    val derivationPath: String = "m/44'/0'/0'/0/0",
    val network: String = "mainnet",
    val chainCode: String? = null,
    val extendedPublicKey: String? = null,
    val xpubKey: String? = null,
    val bech32Address: String? = null,
    val legacyAddress: String? = null,
    val p2shAddress: String? = null
) {
    fun isValidBitcoinAddress(): Boolean {
        return !xpubKey.isNullOrBlank() || !bech32Address.isNullOrBlank()
    }
    
    fun getPrimaryAddress(): String? {
        return bech32Address ?: xpubKey ?: legacyAddress
    }
    
    companion object {
        fun createDefault(currency: Currency): CoinData {
            return CoinData(
                derivationPath = when (currency) {
                    Currency.BITCOIN -> "m/44'/0'/0'/0/0"
                    Currency.OFO -> "m/44'/5757'/0'/0/0"
                    Currency.NAIRA -> "m/44'/5757'/0'/0/0"
                    Currency.USDT -> "m/44'/145'/0'/0/0"
                    Currency.USDC -> "m/44'/60'/0'/0/0"
                },
                network = "mainnet"
            )
        }
    }
}
