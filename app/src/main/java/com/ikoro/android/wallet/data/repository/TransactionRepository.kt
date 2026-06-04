package com.ikoro.android.wallet.data.repository

import com.ikoro.android.wallet.data.model.Transaction
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton


/**
 * Transaction Repository - transaction-specific access patterns
 */
@Singleton
class TransactionRepository @Inject constructor(
    private val walletRepository: WalletRepository
) {
    // Get all transactions
    fun getAllTransactions(walletId: String): Flow<List<Transaction>> =
        walletRepository.getTransactionsByWalletId(walletId)

    // Get pending transactions (waiting to be broadcast)
    fun getPendingBroadcastTransactions(walletId: String): Flow<List<Transaction>> =
        walletRepository.getSignedButNotBroadcast(walletId)

    // Get queued transactions (offline)
    fun getQueuedTransactions(walletId: String): Flow<List<Transaction>> =
        walletRepository.getQueuedTransactions(walletId)

    // Insert new transaction
    suspend fun insertTransaction(transaction: Transaction) {
        walletRepository.insertTransaction(transaction)
    }

    // Update transaction after signing
    suspend fun markSigned(transactionId: String) {
        // Get transaction and mark as signed
        val txn = walletRepository.getTransactionById(transactionId)
        txn?.let {
            it.isSigned = true
            walletRepository.updateTransaction(it)
        }
    }

    // Broadcast transaction via mesh
    suspend fun broadcastTransaction(transactionId: String) {
        // Get transaction and broadcast via mesh
        val txn = walletRepository.getTransactionById(transactionId)
        txn?.let {
            walletRepository.markBroadcast(transactionId, "pending_hash")
        }
    }

    // Mark transaction as confirmed
    suspend fun confirmTransaction(transactionId: String) {
        walletRepository.markConfirmed(transactionId)
    }

    // Queue transaction for offline broadcast
    suspend fun queueTransaction(transactionId: String) {
        walletRepository.updateTransactionStatus(transactionId, "PENDING", isQueued = true)
    }

    // Process pending broadcast transactions
    suspend fun processPendingBroadcast(walletId: String) {
        // Get all transactions that are signed but not broadcast
        // Broadcast them via mesh
        // Update their status
    }
}
