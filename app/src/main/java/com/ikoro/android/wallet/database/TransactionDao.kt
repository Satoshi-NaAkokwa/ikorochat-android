package com.ikoro.android.wallet.database

import androidx.room.*
import com.ikoro.android.wallet.model.TransactionEntity
import kotlinx.coroutines.flow.Flow

/**
 * DAO for Transaction operations
 */
@Dao
interface TransactionDao {
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: TransactionEntity): Long
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransactions(transactions: List<TransactionEntity>): List<Long>
    
    @Update
    suspend fun updateTransaction(transaction: TransactionEntity)
    
    @Delete
    suspend fun deleteTransaction(transaction: TransactionEntity)
    
    @Query("SELECT * FROM transactions WHERE id = :id LIMIT 1")
    suspend fun getTransactionById(id: String): TransactionEntity?
    
    @Query("SELECT * FROM transactions WHERE walletCurrency = :currency ORDER BY timestamp DESC LIMIT :limit OFFSET :offset")
    fun getTransactionsByCurrency(currency: String, limit: Int, offset: Int): Flow<List<TransactionEntity>>
    
    @Query("SELECT * FROM transactions WHERE walletCurrency = :currency ORDER BY timestamp DESC")
    fun getAllTransactionsByCurrency(currency: String): Flow<List<TransactionEntity>>
    
    @Query("SELECT * FROM transactions WHERE walletCurrency = :currency ORDER BY timestamp DESC")
    suspend fun getAllTransactionsByCurrencySync(currency: String): List<TransactionEntity>
    
    @Query("SELECT * FROM transactions WHERE walletCurrency = :currency AND status = :status ORDER BY timestamp DESC")
    fun getTransactionsByStatus(currency: String, status: String): Flow<List<TransactionEntity>>
    
    @Query("SELECT * FROM transactions WHERE id = :id")
    suspend fun transactionExists(id: String): TransactionEntity?
    
    @Query("DELETE FROM transactions WHERE id = :id")
    suspend fun deleteById(id: String)
    
    @Query("DELETE FROM transactions WHERE walletCurrency = :currency")
    suspend fun deleteByCurrency(currency: String)
    
    @Query("SELECT COUNT(*) FROM transactions WHERE walletCurrency = :currency AND status = 'PENDING'")
    suspend fun getPendingTransactionCount(currency: String): Int
    
    @Query("UPDATE transactions SET status = :status, transactionHash = :hash, updatedAt = :updatedAt WHERE id = :id")
    suspend fun updateTransactionStatus(id: String, status: String, hash: String?, updatedAt: Long)
}
