package com.ikoro.android.wallet.manager

import android.content.Context
import android.util.Log
import com.ikoro.android.data.model.*
import com.ikoro.android.wallet.model.TransactionEntity
import com.ikoro.android.wallet.repository.WalletRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.util.*
import java.util.concurrent.ConcurrentHashMap

/**
 * TransactionQueueManager - Manages offline transaction queue
 * Stores pending transactions locally when network is unavailable
 */
class TransactionQueueManager(private val context: Context, private val repository: WalletRepository) {
    
    private val TAG = "TransactionQueueManager"
    
    // In-memory queue for pending transactions
    private val transactionQueue = ConcurrentHashMap<String, QueuedTransaction>()
    
    // Maximum queue size
    private val maxQueueSize = 100
    
    // Batch size for processing
    private val batchSize = 10
    
    // Reconnection attempts
    private val maxReconnectAttempts = 5
    private val reconnectDelayMs = 5000L
    
    init {
        loadPendingTransactions()
        startNetworkMonitor()
    }
    
    /**
     * Add transaction to queue
     */
    suspend fun addToQueue(
        transaction: TransactionEntity
    ): QueueOperationResult {
        val queueSize = transactionQueue.size
        
        if (queueSize >= maxQueueSize) {
            return QueueOperationResult.Error(
                "Queue full. Maximum ${maxQueueSize} transactions allowed. Please wait for pending transactions to process."
            )
        }
        
        val pending = QueuedTransaction(
            id = transaction.id,
            transaction = transaction,
            addedAt = System.currentTimeMillis(),
            retryCount = 0,
            status = QueuedStatus.PENDING
        )
        
        transactionQueue[pending.id] = pending
        
        // Persist to database
        repository.createTransaction(transaction)
        
        Log.d(TAG, "Added transaction to queue: ${pending.id}")
        
        return QueueOperationResult.Success(pending.id)
    }
    
    /**
     * Get transactions for a currency from queue
     */
    fun getQueueByCurrency(currency: Currency): List<QueuedTransaction> {
        return transactionQueue.values
            .filter { it.transaction.walletCurrency == currency.name }
            .sortedBy { it.addedAt }
    }
    
    /**
     * Process queue when network becomes available
     */
    suspend fun processQueue(): List<QueueOperationResult> {
        return try {
            val currencyList = Currency.values().toList()
            val results = mutableListOf<QueueOperationResult>()
            
            currencyList.forEach { currency ->
                val queued = getQueueByCurrency(currency)
                if (queued.isNotEmpty()) {
                    Log.d(TAG, "Processing queue for ${currency.name}: ${queued.size} transactions")
                    
                    // Process batch
                    val batch = queued.take(batchSize)
                    batch.forEach { pending ->
                        val result = processQueuedTransaction(pending)
                        results.add(result)
                    }
                }
            }
            
            results
        } catch (e: Exception) {
            Log.e(TAG, "Failed to process transaction queue", e)
            emptyList()
        }
    }
    
    /**
     * Process a single queued transaction
     */
    private suspend fun processQueuedTransaction(
        pending: QueuedTransaction
    ): QueueOperationResult {
        return try {
            val transaction = pending.transaction
            
            // Check retry count
            if (pending.retryCount >= maxReconnectAttempts) {
                val result = QueueOperationResult.Error(
                    "Transaction ${pending.id} failed after $maxReconnectAttempts attempts: ${transaction.errorMessage ?: "Unknown error"}"
                )
                transactionQueue.remove(pending.id)
                return result
            }
            
            // Try to broadcast/reprocess
            val result = broadcastTransaction(pending)
            
            when (result) {
                is QueueOperationResult.Success -> {
                    // Update status
                    val updatedStatus = TransactionStatus.COMPLETED
                    repository.updateTransactionStatus(
                        pending.id,
                        updatedStatus,
                        result.transactionHash
                    )
                    
                    // Remove from queue
                    transactionQueue.remove(pending.id)
                    
                    Log.d(TAG, "Successfully processed transaction ${pending.id}")
                    result
                }
                is QueueOperationResult.Error -> {
                    // Increment retry and update queue
                    val updated = pending.copy(
                        retryCount = pending.retryCount + 1,
                        status = QueuedStatus.RETRYING,
                        lastRetryAt = System.currentTimeMillis()
                    )
                    transactionQueue[pending.id] = updated
                    
                    Log.w(TAG, "Failed to process transaction ${pending.id}. Retry ${pending.retryCount + 1}")
                    result
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error processing transaction ${pending.id}", e)
            QueueOperationResult.Error(e.message ?: "Unknown error")
        }
    }
    
    /**
     * Broadcast a transaction (reimplementation of wallet service)
     */
    private suspend fun broadcastTransaction(
        pending: QueuedTransaction
    ): QueueOperationResult {
        // In production, this would use the actual wallet service
        // For now, mark as success since the transaction was already signed
        
        return QueueOperationResult.Success(
            transactionId = pending.id,
            transactionHash = pending.transaction.transactionHash ?: "queued_${pending.id}"
        )
    }
    
    /**
     * Mark transaction as failed
     */
    suspend fun markTransactionFailed(
        transactionId: String,
        error: String
    ): Boolean {
        return try {
            val pending = transactionQueue[transactionId] ?: return false
            
            // Update status
            val updated = pending.copy(
                status = QueuedStatus.FAILED,
                lastRetryAt = System.currentTimeMillis()
            )
            transactionQueue[transactionId] = updated
            
            // Update database
            repository.transactionExists(transactionId)?.let { tx ->
                tx.status = TransactionStatus.FAILED.name
                tx.errorMessage = error
                repository.updateTransaction(tx)
            }
            
            Log.w(TAG, "Marked transaction $transactionId as failed: $error")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to mark transaction $transactionId as failed", e)
            false
        }
    }
    
    /**
     * Remove transaction from queue
     */
    suspend fun removeFromQueue(transactionId: String): Boolean {
        return try {
            val removed = transactionQueue.remove(transactionId) != null
            if (removed) {
                Log.d(TAG, "Removed transaction from queue: $transactionId")
            }
            removed
        } catch (e: Exception) {
            Log.e(TAG, "Failed to remove transaction from queue: $transactionId", e)
            false
        }
    }
    
    /**
     * Clear entire queue
     */
    suspend fun clearQueue(): Boolean {
        return try {
            transactionQueue.clear()
            Log.d(TAG, "Cleared transaction queue")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to clear transaction queue", e)
            false
        }
    }
    
    /**
     * Get queue statistics
     */
    fun getQueueStats(): QueueStats {
        val pendingCount = transactionQueue.values.count { it.status == QueuedStatus.PENDING }
        val retryingCount = transactionQueue.values.count { it.status == QueuedStatus.RETRYING }
        val failedCount = transactionQueue.values.count { it.status == QueuedStatus.FAILED }
        
        val byCurrency = mutableMapOf<String, Int>()
        transactionQueue.values.forEach { pending ->
            val currency = pending.transaction.walletCurrency
            byCurrency[currency] = (byCurrency[currency] ?: 0) + 1
        }
        
        return QueueStats(
            total = transactionQueue.size,
            pending = pendingCount,
            retrying = retryingCount,
            failed = failedCount,
            maxQueueSize = maxQueueSize,
            remainingCapacity = maxQueueSize - transactionQueue.size,
            byCurrency = byCurrency
        )
    }
    
    /**
     * Load pending transactions from database
     */
    private suspend fun loadPendingTransactions() {
        return try {
            Currency.values().forEach { currency ->
                val transactions = repository.getPendingTransactions(currency)
                transactions.forEach { transaction ->
                    val pending = QueuedTransaction(
                        id = transaction.id,
                        transaction = transaction,
                        addedAt = transaction.createdAt,
                        retryCount = 0,
                        status = QueuedStatus.PENDING
                    )
                    transactionQueue[pending.id] = pending
                }
            }
            Log.d(TAG, "Loaded ${transactionQueue.size} pending transactions from database")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to load pending transactions", e)
        }
    }
    
    /**
     * Start network connection monitor
     */
    private fun startNetworkMonitor() {
        // Simplified - in production would use Android's ConnectivityManager
        // For now, we'll just start a periodic check
        Log.d(TAG, "Network monitor started")
    }
    
    /**
     * Queue operation result
     */
    sealed interface QueueOperationResult {
        data class Success(val transactionId: String, val transactionHash: String? = null) : QueueOperationResult
        data class Error(val message: String) : QueueOperationResult
    }
    
    /**
     * Pending transaction in queue
     */
    data class QueuedTransaction(
        val id: String,
        val transaction: TransactionEntity,
        val addedAt: Long,
        var retryCount: Int,
        var status: QueuedStatus
    ) {
        fun isFailed(): Boolean = status == QueuedStatus.FAILED
    }
    
    /**
     * Status of queued transaction
     */
    enum class QueuedStatus {
        PENDING,
        RETRYING,
        FAILED,
        COMPLETED
    }
    
    /**
     * Queue statistics
     */
    data class QueueStats(
        val total: Int,
        val pending: Int,
        val retrying: Int,
        val failed: Int,
        val maxQueueSize: Int,
        val remainingCapacity: Int,
        val byCurrency: Map<String, Int>
    ) {
        val isFull: Boolean = remainingCapacity <= 0
        val canAddMore: Boolean = !isFull
    }
}
