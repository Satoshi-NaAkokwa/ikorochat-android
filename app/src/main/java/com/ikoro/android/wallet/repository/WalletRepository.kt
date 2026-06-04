package com.ikoro.android.wallet.repository

import com.ikoro.android.data.model.Currency
import com.ikoro.android.wallet.database.TransactionDao
import com.ikoro.android.wallet.database.WalletDao
import com.ikoro.android.wallet.model.CoinData
import com.ikoro.android.wallet.model.TransactionEntity
import com.ikoro.android.wallet.model.WalletEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Repository for wallet operations
 * Handles data operations and provides clean API to service layer
 */
class WalletRepository(
    private val walletDao: WalletDao,
    private val transactionDao: TransactionDao
) {
    
    // Wallet operations
    
    suspend fun createWallet(
        currency: Currency,
        walletAddress: String,
        publicKey: String,
        coinData: CoinData?
    ): Long {
        val entity = WalletEntity(
            currency = currency.name,
            walletAddress = walletAddress,
            publicKey = publicKey,
            coinData = coinData,
            lastUpdated = System.currentTimeMillis()
        )
        return walletDao.insertWallet(entity)
    }
    
    suspend fun updateWalletBalance(currency: Currency, balance: Double, pendingBalance: Double = 0.0) {
        walletDao.updateBalance(
            currency = currency.name,
            balance = balance,
            lastUpdated = System.currentTimeMillis()
        )
    }
    
    suspend fun updateWallet(wallet: WalletEntity) {
        walletDao.updateWallet(wallet)
    }
    
    suspend fun getWalletByCurrency(currency: Currency): WalletEntity? {
        return walletDao.getWalletByCurrency(currency.name)
    }
    
    fun getWalletsFlow(): Flow<List<WalletEntity>> {
        return walletDao.getAllWallets()
    }
    
    suspend fun getAllWallets(): List<WalletEntity> {
        return walletDao.getAllWalletsSync()
    }
    
    suspend fun walletExists(currency: Currency): Boolean {
        return walletDao.exists(currency.name) > 0
    }
    
    suspend fun deleteWallet(currency: Currency) {
        walletDao.deleteByCurrency(currency.name)
        transactionDao.deleteByCurrency(currency.name)
    }
    
    // Transaction operations
    
    suspend fun createTransaction(transaction: TransactionEntity): Long {
        return transactionDao.insertTransaction(transaction)
    }
    
    suspend fun createTransactions(transactions: List<TransactionEntity>): List<Long> {
        return transactionDao.insertTransactions(transactions)
    }
    
    suspend fun updateTransaction(transaction: TransactionEntity) {
        transactionDao.updateTransaction(transaction)
    }
    
    suspend fun updateTransactionStatus(
        id: String,
        status: com.ikoro.android.data.model.TransactionStatus,
        hash: String? = null,
        blockHeight: Int? = null
    ) {
        val statusName = status.name
        val updatedAt = System.currentTimeMillis()
        
        transactionDao.updateTransactionStatus(
            id = id,
            status = statusName,
            hash = hash,
            updatedAt = updatedAt
        )
    }
    
    fun getTransactionsByCurrencyFlow(currency: Currency): Flow<List<TransactionEntity>> {
        return transactionDao.getAllTransactionsByCurrency(currency.name)
    }
    
    fun getTransactionsByCurrencyFlow(currency: Currency, limit: Int, offset: Int): Flow<List<TransactionEntity>> {
        return transactionDao.getTransactionsByCurrency(currency.name, limit, offset)
    }
    
    suspend fun getTransactionsByCurrency(currency: Currency): List<TransactionEntity> {
        return transactionDao.getAllTransactionsByCurrencySync(currency.name)
    }
    
    suspend fun getPendingTransactions(currency: Currency): List<TransactionEntity> {
        return transactionDao.getAllTransactionsByCurrencySync(currency.name)
            .filter { it.isPending() }
    }
    
    suspend fun getTransactionById(id: String): TransactionEntity? {
        return transactionDao.getTransactionById(id)
    }
    
    suspend fun transactionExists(id: String): Boolean {
        return transactionDao.getTransactionById(id) != null
    }
    
    suspend fun deleteTransaction(id: String) {
        transactionDao.deleteById(id)
    }
    
    // Utility operations
    
    suspend fun getPendingTransactionCount(currency: Currency): Int {
        return transactionDao.getPendingTransactionCount(currency.name)
    }
    
    suspend fun batchUpdateTransactions(transactions: List<TransactionEntity>) {
        transactions.forEach { transactionDao.updateTransaction(it) }
    }
    
    suspend fun syncWalletsWithAgbaraWallet(wallets: Map<Currency, Double>) {
        wallets.forEach { (currency, balance) ->
            if (walletExists(currency)) {
                updateWalletBalance(currency, balance)
            } else {
                // Create default wallet if not exists
                val coinData = CoinData.createDefault(currency)
                createWallet(
                    currency = currency,
                    walletAddress = "",
                    publicKey = "",
                    coinData = coinData
                )
                updateWalletBalance(currency, balance)
            }
        }
    }
    
    suspend fun clearAllData() {
        walletDao.getAllWalletsSync().forEach { walletDao.deleteWallet(it) }
        transactionDao.getAllTransactionsByCurrencySync("ALL").forEach { transactionDao.deleteTransaction(it) }
    }
}
