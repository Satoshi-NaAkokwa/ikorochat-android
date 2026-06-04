package com.ikoro.android.wallet.database

import androidx.room.*
import com.ikoro.android.wallet.model.WalletEntity
import kotlinx.coroutines.flow.Flow

/**
 * DAO for Wallet operations
 */
@Dao
interface WalletDao {
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWallet(wallet: WalletEntity): Long
    
    @Update
    suspend fun updateWallet(wallet: WalletEntity)
    
    @Delete
    suspend fun deleteWallet(wallet: WalletEntity)
    
    @Query("SELECT * FROM wallets WHERE currency = :currency LIMIT 1")
    suspend fun getWalletByCurrency(currency: String): WalletEntity?
    
    @Query("SELECT * FROM wallets ORDER BY currency")
    fun getAllWallets(): Flow<List<WalletEntity>>
    
    @Query("SELECT * FROM wallets ORDER BY currency")
    suspend fun getAllWalletsSync(): List<WalletEntity>
    
    @Query("SELECT COUNT(*) FROM wallets WHERE currency = :currency")
    suspend fun exists(currency: String): Int
    
    @Query("DELETE FROM wallets WHERE currency = :currency")
    suspend fun deleteByCurrency(currency: String)
    
    @Query("UPDATE wallets SET balance = :balance, lastUpdated = :lastUpdated WHERE currency = :currency")
    suspend fun updateBalance(currency: String, balance: Double, lastUpdated: Long)
}
