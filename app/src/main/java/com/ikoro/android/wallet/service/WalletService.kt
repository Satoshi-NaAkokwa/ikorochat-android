package com.ikoro.android.wallet.service

import android.content.Context
import android.util.Log
import com.ikoro.android.data.model.*
import com.ikoro.android.wallet.manager.KeyManager
import com.ikoro.android.wallet.model.*
import com.ikoro.android.wallet.repository.WalletRepository
import com.ikoro.android.wallet.service.signer.*
import com.ikoro.android.wallet.service.MeshTransactionBroadcaster
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.retry
import java.util.concurrent.TimeUnit

/**
 * WalletService - Business logic layer for wallet operations
 * Acts as an orchestrator between repository, signer, and mesh broadcast
 */
class WalletService(
    private val context: Context,
    private val repository: WalletRepository,
    private val keyManager: KeyManager,
    private val transactionSigner: TransactionSigner,
    private val meshBroadcaster: MeshTransactionBroadcaster
) {
    
    private val TAG = "WalletService"
    
    // Currency support
    private val supportedCurrencies = setOf(
        Currency.BITCOIN,
        Currency.OFO,
        Currency.NAIRA,
        Currency.USDT,
        Currency.USDC
    )
    
    /**
     * Initialize wallet service
     */
    suspend fun initialize() {
        keyManager.initialize()
        Log.i(TAG, "WalletService initialized")
    }
    
    /**
     * Create a new wallet for a currency
     */
    suspend fun createWallet(currency: Currency): WalletCreationResult {
        if (!supportedCurrencies.contains(currency)) {
            return WalletCreationResult.Error("Currency $currency not supported")
        }
        
        return try {
            // Generate key pair
            val keyPair = keyManager.generateKeyPair(currency)
            
            // Generate address from public key
            val address = when (currency) {
                Currency.BITCOIN -> generateBitcoinAddress(keyPair.publicKey)
                Currency.OFO -> generateStacksAddress(keyPair.publicKey)
                Currency.NAIRA -> generateNairaAddress(keyPair.publicKey)
                Currency.USDT -> generateUsdtAddress(keyPair.publicKey)
                Currency.USDC -> generateUsdcAddress(keyPair.publicKey)
            }
            
            // Store wallet data
            val coinData = CoinData.createDefault(currency).copy(
                extendedPublicKey = keyPair.publicKey,
                bech32Address = address
            )
            
            val walletId = repository.createWallet(
                currency = currency,
                walletAddress = address,
                publicKey = keyPair.publicKey,
                coinData = coinData
            )
            
            // Sync with AgbaraWallet backend
            val backendResult = syncWithAgbaraWallet(currency)
            if (backendResult is AgbaraWalletSyncResult.Error) {
                Log.w(TAG, "Failed to sync wallet with backend: ${backendResult.message}")
            }
            
            WalletCreationResult.Success(
                walletId,
                WalletInfo(
                    currency = currency,
                    address = address,
                    publicKey = keyPair.publicKey,
                    createdAt = System.currentTimeMillis()
                )
            )
        } catch (e: Exception) {
            Log.e(TAG, "Failed to create wallet for ${currency.name}", e)
            WalletCreationResult.Error(e.message ?: "Unknown error")
        }
    }
    
    /**
     * Get wallet info for a currency
     */
    suspend fun getWalletInfo(currency: Currency): WalletInfo? {
        val walletEntity = repository.getWalletByCurrency(currency)
        return walletEntity?.toWalletInfo()
    }
    
    /**
     * Get all wallets
     */
    fun getAllWalletsFlow(): Flow<List<WalletInfo>> {
        return repository.getWalletsFlow()
            .map { wallets ->
                wallets.map { it.toWalletInfo() }
            }
    }
    
    /**
     * Sync wallet balance from backend
     */
    suspend fun syncWalletBalance(currency: Currency): BalanceSyncResult {
        return try {
            // Get wallet entity
            val walletEntity = repository.getWalletByCurrency(currency) ?: return BalanceSyncResult.Error("Wallet not found")
            
            // Sync with AgbaraWallet
            val backendResult = syncWithAgbaraWallet(currency)
            
            when (backendResult) {
                is AgbaraWalletSyncResult.Success -> {
                    val newBalance = backendResult.balance
                    repository.updateWalletBalance(
                        currency = currency,
                        balance = newBalance,
                        pendingBalance = 0.0
                    )
                    
                    // Update wallet entity
                    walletEntity.balance = newBalance
                    walletEntity.lastSynced = System.currentTimeMillis()
                    repository.updateWallet(walletEntity)
                    
                    BalanceSyncResult.Success(currency, newBalance)
                }
                is AgbaraWalletSyncResult.Error -> {
                    // Use cached balance if backend sync fails
                    BalanceSyncResult.Success(currency, walletEntity.balance)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to sync wallet balance for ${currency.name}", e)
            BalanceSyncResult.Error(e.message ?: "Unknown error")
        }
    }
    
    /**
     * Send transaction
     */
    suspend fun sendTransaction(
        currency: Currency,
        toAddress: String,
        amount: Double,
        memo: String? = null
    ): TransactionResult {
        return try {
            // Validate sender wallet
            val fromWallet = repository.getWalletByCurrency(currency) ?: return TransactionResult.Error("Sender wallet not found")
            
            // Calculate fee
            val fee = calculateFee(currency, amount)
            
            // Build transaction data
            val transactionData = buildTransactionData(currency, toAddress, amount, fee, memo)
            
            // Sign transaction offline
            val signResult = signTransaction(currency, fromWallet, transactionData)
            
            if (signResult is TransactionSignResult.Error) {
                return TransactionResult.Error(signResult.message)
            }
            
            val successResult = signResult as TransactionSignResult.Success
            
            // Create transaction entity
            val transaction = TransactionEntity(
                walletCurrency = currency.name,
                address = toAddress,
                fromAddress = fromWallet.walletAddress,
                amount = amount,
                fee = successResult.inputCount.toDouble(),
                type = TransactionType.SEND.name,
                status = TransactionStatus.PENDING.name,
                transactionHash = successResult.transactionHash,
                timestamp = System.currentTimeMillis()
            )
            
            // Save to repository
            val txId = repository.createTransaction(transaction)
            
            // Broadcast via mesh
            meshBroadcaster.broadcastTransaction(
                currency = currency,
                transactionHash = successResult.transactionHash,
                signedData = successResult.signedData
            )
            
            TransactionResult.Success(
                transactionId = txId.toString(),
                transactionHash = successResult.transactionHash,
                amount = amount,
                currency = currency
            )
        } catch (e: Exception) {
            Log.e(TAG, "Failed to send transaction for ${currency.name}", e)
            TransactionResult.Error(e.message ?: "Unknown error")
        }
    }
    
    /**
     * Receive transaction (generate receive address)
     */
    suspend fun getReceiveAddress(currency: Currency): String? {
        val walletEntity = repository.getWalletByCurrency(currency)
        return walletEntity?.walletAddress
    }
    
    /**
     * Get transaction history
     */
    fun getTransactionHistoryFlow(currency: Currency): Flow<List<TransactionEntity>> {
        return repository.getTransactionsByCurrencyFlow(currency)
    }
    
    /**
     * Get pending transactions
     */
    suspend fun getPendingTransactions(currency: Currency): List<TransactionEntity> {
        return repository.getPendingTransactions(currency)
    }
    
    /**
     * Get transaction by ID
     */
    suspend fun getTransaction(txId: String): TransactionEntity? {
        return repository.getTransactionById(txId)
    }
    
    /**
     * Update transaction status
     */
    suspend fun updateTransactionStatus(
        txId: String,
        status: TransactionStatus,
        hash: String? = null
    ): Boolean {
        return try {
            repository.updateTransactionStatus(txId, status, hash)
            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to update transaction status: ${txId}", e)
            false
        }
    }
    
    /**
     * Get total balance across all currencies
     */
    suspend fun getTotalBalance(): Map<Currency, Double> {
        return supportedCurrencies.associateWith { currency ->
            repository.getWalletByCurrency(currency)?.availableBalance ?: 0.0
        }
    }
    
    /**
     * Exchange currency
     */
    suspend fun exchangeCurrency(
        fromCurrency: Currency,
        toCurrency: Currency,
        amount: Double
    ): ExchangeResult {
        return try {
            if (fromCurrency == toCurrency) {
                return ExchangeResult.Error("Cannot exchange same currency")
            }
            
            // Get exchange rate (simplified - in production would call AgbaraWallet)
            val exchangeRate = getExchangeRate(fromCurrency, toCurrency)
            
            if (exchangeRate <= 0) {
                return ExchangeResult.Error("Invalid exchange rate")
            }
            
            val toAmount = amount * exchangeRate
            val fee = calculateExchangeFee(fromCurrency, amount)
            
            ExchangeResult.Success(
                fromCurrency = fromCurrency,
                toCurrency = toCurrency,
                fromAmount = amount,
                toAmount = toAmount,
                rate = exchangeRate,
                fee = fee
            )
        } catch (e: Exception) {
            Log.e(TAG, "Failed to exchange currency", e)
            ExchangeResult.Error(e.message ?: "Unknown error")
        }
    }
    
    /**
     * Cleanup resources
     */
    fun cleanup() {
        transactionSigner.cleanup()
        meshBroadcaster.cleanup()
    }
    
    // Private helper methods
    
    private suspend fun generateBitcoinAddress(publicKey: String): String {
        // Simplified - in production would use BitcoinJ or similar
        // This uses Bech32 format as default
        return "bc1q${publicKey.take(38)}"
    }
    
    private suspend fun generateStacksAddress(publicKey: String): String {
        return "SP3${publicKey.take(35)}"
    }
    
    private suspend fun generateNairaAddress(publicKey: String): String {
        return "0x${publicKey.take(40)}"
    }
    
    private suspend fun generateUsdtAddress(publicKey: String): String {
        return "TR7${publicKey.take(33)}"
    }
    
    private suspend fun generateUsdcAddress(publicKey: String): String {
        return "0x${publicKey.take(40)}"
    }
    
    private suspend fun buildTransactionData(
        currency: Currency,
        toAddress: String,
        amount: Double,
        fee: Double,
        memo: String?
    ): Any {
        return when (currency) {
            Currency.BITCOIN -> TransactionData(
                outputs = listOf(
                    TransactionOutput(
                        address = toAddress,
                        amountSatoshis = (amount * 100_000_000).toLong()
                    )
                ),
                fee = fee.toLong(),
                timestamp = System.currentTimeMillis()
            )
            Currency.OFO -> StacksTransactionData(
                recipient = toAddress,
                amount = (amount * 1_000_000).toLong(),
                feeRate = 100,
                nonce = System.currentTimeMillis() / 1000,
                publicKey = "",
                transactionHash = "TX_${System.currentTimeMillis()}"
            )
            Currency.NAIRA -> NairaTransactionData(
                recipient = toAddress,
                amount = amount,
                timestamp = System.currentTimeMillis()
            )
            Currency.USDT -> UsdtTransactionData(
                recipient = toAddress,
                contractAddress = "TR7NHqjeKQxGTCi8q8ZY4pL8otSgXN5p6d",
                amount = (amount * 1_000_000).toLong(),
                feeLimit = 1000000,
                callValue = 0,
                hash = "TX_${System.currentTimeMillis()}"
            )
            Currency.USDC -> UsdcTransactionData(
                recipient = toAddress,
                contractAddress = "0x0000000000000000000000000000000000000000",
                amount = amount.toString(),
                gasLimit = 100000,
                gasPrice = "50000000000",
                nonce = System.currentTimeMillis() / 1000,
                eip712Domain = "USDC_EIP712_DOMAIN",
                hash = "TX_${System.currentTimeMillis()}"
            )
        }
    }
    
    private suspend fun signTransaction(
        currency: Currency,
        wallet: WalletEntity,
        transactionData: Any
    ): TransactionSignResult {
        return when (currency) {
            Currency.BITCOIN -> {
                val data = transactionData as TransactionData
                val inputs = listOf(
                    TransactionInput(
                        previousTxId = com.bitcoinj.core.Transaction.PrevOut.EMPTY,
                        vout = 0,
                        scriptPubKey = null,
                        valueSatoshis = (data.outputs.sumOf { it.amountSatoshis }).also { it + data.fee },
                        sequenceNumber = 0xFFFFFFFF
                    )
                )
                transactionSigner.signBitcoinTransaction(wallet, data, inputs)
            }
            Currency.OFO -> {
                val data = transactionData as StacksTransactionData
                transactionSigner.signStacksTransaction(wallet, data)
            }
            Currency.NAIRA -> {
                val data = transactionData as NairaTransactionData
                transactionSigner.signNairaTransaction(wallet, data)
            }
            Currency.USDT -> {
                val data = transactionData as UsdtTransactionData
                transactionSigner.signUsdtTransaction(wallet, data)
            }
            Currency.USDC -> {
                val data = transactionData as UsdcTransactionData
                transactionSigner.signUsdcTransaction(wallet, data)
            }
        }
    }
    
    private suspend fun calculateFee(currency: Currency, amount: Double): Double {
        return when (currency) {
            Currency.BITCOIN -> 0.00001 // 1 sat/vB minimum
            Currency.OFO -> 0.000001
            Currency.NAIRA -> 0.01
            Currency.USDT -> 1.0 // TRC20 constant fee
            Currency.USDC -> 1.0 // EVM gas based
        }
    }
    
    private suspend fun calculateExchangeFee(fromCurrency: Currency, amount: Double): Double {
        return amount * 0.005 // 0.5% fee
    }
    
    private suspend fun getExchangeRate(from: Currency, to: Currency): Double {
        // Simplified - in production would call AgbaraWallet or exchange API
        return when {
            from == to -> 1.0
            from == Currency.BITCOIN && to == Currency.USDT -> 65000.0
            from == Currency.USDT && to == Currency.BITCOIN -> 0.000015
            from == Currency.OFO && to == Currency.BITCOIN -> 0.95
            from == Currency.BITCOIN && to == Currency.OFO -> 1.05
            from == Currency.NAIRA && to == Currency.USDT -> 0.0006
            from == Currency.USDT && to == Currency.NAIRA -> 1666.67
            else -> 1.0
        }
    }
    
    private suspend fun syncWithAgbaraWallet(currency: Currency): AgbaraWalletSyncResult {
        // Integration with AgbaraWallet backend
        // In production, this would call the actual AgbaraWallet API
        
        return try {
            // Mock sync - in production would fetch balance from AgbaraWallet
            AgbaraWalletSyncResult.Success(0.0) // Placeholder
        } catch (e: Exception) {
            AgbaraWalletSyncResult.Error(e.message ?: "Sync failed")
        }
    }
    
    private fun WalletEntity.toWalletInfo(): WalletInfo {
        return WalletInfo(
            currency = Currency.valueOf(currency),
            address = walletAddress,
            publicKey = publicKey,
            balance = balance,
            availableBalance = availableBalance,
            createdAt = createdAt,
            lastSynced = lastSynced
        )
    }
}

/**
 * Result classes for wallet operations
 */
sealed interface WalletCreationResult {
    data class Success(val walletId: Long, val info: WalletInfo) : WalletCreationResult
    data class Error(val message: String) : WalletCreationResult
}

data class WalletInfo(
    val currency: Currency,
    val address: String,
    val publicKey: String,
    val balance: Double = 0.0,
    val availableBalance: Double = 0.0,
    val createdAt: Long = System.currentTimeMillis(),
    val lastSynced: Long = System.currentTimeMillis()
)

sealed interface AgbaraWalletSyncResult {
    data class Success(val balance: Double) : AgbaraWalletSyncResult
    data class Error(val message: String) : AgbaraWalletSyncResult
}

sealed interface BalanceSyncResult {
    data class Success(val currency: Currency, val balance: Double) : BalanceSyncResult
    data class Error(val message: String) : BalanceSyncResult
}

sealed interface TransactionResult {
    data class Success(
        val transactionId: String,
        val transactionHash: String,
        val amount: Double,
        val currency: Currency
    ) : TransactionResult
    
    data class Error(val message: String) : TransactionResult
}

sealed interface ExchangeResult {
    data class Success(
        val fromCurrency: Currency,
        val toCurrency: Currency,
        val fromAmount: Double,
        val toAmount: Double,
        val rate: Double,
        val fee: Double
    ) : ExchangeResult
    
    data class Error(val message: String) : ExchangeResult
}
