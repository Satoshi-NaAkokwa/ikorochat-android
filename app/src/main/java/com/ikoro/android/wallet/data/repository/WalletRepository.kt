package com.ikoro.android.wallet.data.repository

import androidx.room.DatabaseConfiguration
import androidx.room.Room
import androidx.room.RoomDatabase
import com.ikoro.android.wallet.data.model.*
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton


/**
 * Wallet Repository - provides data access to wallet entities
 */
@Singleton
class WalletRepository @Inject constructor(
    private val database: WalletDatabase
) {
    private val walletDao = database.walletDao()
    private val transactionDao = database.transactionDao()

    // Wallet operations
    suspend fun insertWallet(wallet: Wallet) = walletDao.insert(wallet)
    suspend fun updateWallet(wallet: Wallet) = walletDao.update(wallet)
    suspend fun getWalletById(id: String): Wallet? = walletDao.getById(id)
    suspend fun getWalletByUserId(userId: String): Wallet? = walletDao.getByUserId(userId)
    fun getAllWallets(): Flow<List<Wallet>> = walletDao.getAll()
    suspend fun updateWalletLastUpdated(id: String) = walletDao.updateLastUpdated(id, System.currentTimeMillis())

    // Transaction operations
    suspend fun insertTransaction(transaction: Transaction) = transactionDao.insert(transaction)
    suspend fun updateTransaction(transaction: Transaction) = transactionDao.update(transaction)
    suspend fun getTransactionById(id: String): Transaction? = transactionDao.getById(id)
    fun getTransactionsByWalletId(walletId: String, limit: Int = 20, offset: Int = 0): Flow<List<Transaction>> =
        transactionDao.getByWalletId(walletId, limit, offset)
    fun getPendingTransactions(walletId: String): Flow<List<Transaction>> =
        transactionDao.getByStatus(walletId, "PENDING")
    fun getSignedButNotBroadcast(walletId: String): Flow<List<Transaction>> =
        transactionDao.getPendingBroadcast(walletId)
    fun getQueuedTransactions(walletId: String): Flow<List<Transaction>> =
        transactionDao.getQueuedTransactions(walletId)

    suspend fun markBroadcast(id: String, hash: String? = null) = transactionDao.markBroadcast(id, System.currentTimeMillis(), hash)
    suspend fun markConfirmed(id: String) = transactionDao.markConfirmed(id, System.currentTimeMillis())
    suspend fun updateTransactionStatus(id: String, status: String, isQueued: Boolean = false) =
        transactionDao.updateStatus(id, status, isQueued)

    // Utility methods
    suspend fun updateWalletBalances(walletId: String) {
        // Recalculate balances from transactions
        // This would query transactions and update wallet balance fields
    }

    suspend fun syncWithMesh() {
        // Sync local database with mesh network state
        // This would query for transactions that need broadcasting
    }
}
