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
 * DAO for Transaction operations
 */
@Dao
interface TransactionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(transaction: Transaction)

    @Update
    suspend fun update(transaction: Transaction)

    @Delete
    suspend fun delete(transaction: Transaction)

    @Query("SELECT * FROM transactions WHERE id = :id LIMIT 1")
    suspend fun getById(id: String): Transaction?

    @Query("SELECT * FROM transactions WHERE wallet_id = :walletId ORDER BY created_at DESC LIMIT :limit OFFSET :offset")
    fun getByWalletId(walletId: String, limit: Int = 20, offset: Int = 0): Flow<List<Transaction>>

    @Query("SELECT * FROM transactions WHERE wallet_id = :walletId AND status = :status ORDER BY created_at DESC")
    fun getByStatus(walletId: String, status: String = "PENDING"): Flow<List<Transaction>>

    @Query("SELECT * FROM transactions WHERE wallet_id = :walletId AND is_signed = 1 AND is_broadcast = 0 ORDER BY created_at ASC")
    fun getPendingBroadcast(walletId: String): Flow<List<Transaction>>

    @Query("SELECT * FROM transactions WHERE wallet_id = :walletId AND is_queued = 1 ORDER BY created_at ASC")
    fun getQueuedTransactions(walletId: String): Flow<List<Transaction>>

    @Query("SELECT COUNT(*) FROM transactions WHERE wallet_id = :walletId AND currency = :currency AND type = 'RECEIVE'")
    suspend fun countReceivedTxns(walletId: String, currency: String): Int

    @Query("DELETE FROM transactions WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("UPDATE transactions SET is_broadcast = 1, broadcast_at = :timestamp, hash = :hash, mesh_broadcast_count = mesh_broadcast_count + 1 WHERE id = :id")
    suspend fun markBroadcast(id: String, timestamp: Long, hash: String? = null)

    @Query("UPDATE transactions SET is_confirmed = 1, confirmed_at = :timestamp, status = 'COMPLETED' WHERE id = :id")
    suspend fun markConfirmed(id: String, timestamp: Long)

    @Query("UPDATE transactions SET status = :status, is_queued = :isQueued, retry_count = retry_count + 1 WHERE id = :id")
    suspend fun updateStatus(id: String, status: String, isQueued: Boolean = false)
}
