package com.ikoro.android.wallet.data.model

import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

/**
 * DAO for Wallet operations
 */
@Dao
interface WalletDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(wallet: Wallet)

    @Update
    suspend fun update(wallet: Wallet)

    @Delete
    suspend fun delete(wallet: Wallet)

    @Query("SELECT * FROM wallets WHERE id = :id LIMIT 1")
    suspend fun getById(id: String): Wallet?

    @Query("SELECT * FROM wallets WHERE userId = :userId LIMIT 1")
    suspend fun getByUserId(userId: String): Wallet?

    @Query("SELECT * FROM wallets")
    fun getAll(): Flow<List<Wallet>>

    @Query("UPDATE wallets SET last_updated = :timestamp WHERE id = :id")
    suspend fun updateLastUpdated(id: String, timestamp: Long)

    @Query("UPDATE wallets SET last_sync = :timestamp WHERE id = :id")
    suspend fun updateLastSync(id: String, timestamp: Long)
}
