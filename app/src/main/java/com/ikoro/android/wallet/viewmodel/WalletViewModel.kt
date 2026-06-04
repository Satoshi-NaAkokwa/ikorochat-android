package com.ikoro.android.wallet.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ikoro.android.data.model.Currency
import com.ikoro.android.data.model.TransactionStatus
import com.ikoro.android.wallet.model.TransactionEntity
import com.ikoro.android.wallet.service.WalletService
import com.ikoro.android.wallet.service.WalletCreationResult
import com.ikoro.android.wallet.service.WalletInfo
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

/**
 * WalletViewModel - Exposes wallet state to UI layer
 */
@HiltViewModel
class WalletViewModel @Inject constructor(
    private val walletService: WalletService
) : ViewModel() {
    
    // Wallets state
    val wallets: StateFlow<List<com.ikoro.android.wallet.service.WalletInfo>> =
        walletService.getAllWalletsFlow()
            .stateIn(
                viewModelScope,
                SharingStarted.Lazily,
                emptyList()
            )
    
    // Current selected wallet
    private val _selectedWallet = kotlin.mutableStateOf<com.ikoro.android.wallet.service.WalletInfo?>(null)
    val selectedWallet: StateFlow<com.ikoro.android.wallet.service.WalletInfo?> = _selectedWallet
    
    // Selected currency
    private val _selectedCurrency = kotlin.mutableStateOf<Currency?>(null)
    val selectedCurrency: StateFlow<Currency?> = _selectedCurrency
    
    // Balance sync status
    private val _syncingBalances = kotlin.mutableStateOf(false)
    val syncingBalances: StateFlow<Boolean> = _syncingBalances
    
    // Transaction history
    fun getTransactionsFlow(currency: Currency): StateFlow<List<TransactionEntity>> {
        return walletService.getTransactionHistoryFlow(currency)
            .stateIn(
                viewModelScope,
                SharingStarted.Lazily,
                emptyList()
            )
    }
    
    // Send transaction
    suspend fun sendTransaction(
        currency: Currency,
        toAddress: String,
        amount: Double,
        memo: String? = null
    ): com.ikoro.android.wallet.service.TransactionResult {
        return walletService.sendTransaction(currency, toAddress, amount, memo)
    }
    
    // Receive address
    suspend fun getReceiveAddress(currency: Currency): String? {
        return walletService.getReceiveAddress(currency)
    }
    
    // Sync balance
    suspend fun syncBalance(currency: Currency): com.ikoro.android.wallet.service.BalanceSyncResult {
        return walletService.syncWalletBalance(currency)
    }
    
    // Create new wallet
    suspend fun createWallet(currency: Currency): WalletCreationResult {
        return walletService.createWallet(currency)
    }
    
    // Exchange
    suspend fun exchangeCurrency(
        fromCurrency: Currency,
        toCurrency: Currency,
        amount: Double
    ): com.ikoro.android.wallet.service.ExchangeResult {
        return walletService.exchangeCurrency(fromCurrency, toCurrency, amount)
    }
    
    // Update transaction status
    suspend fun updateTransactionStatus(
        txId: String,
        status: TransactionStatus
    ): Boolean {
        return walletService.updateTransactionStatus(txId, status)
    }
    
    // Get pending transactions
    suspend fun getPendingTransactions(currency: Currency): List<TransactionEntity> {
        return walletService.getPendingTransactions(currency)
    }
    
    // Get transaction by ID
    suspend fun getTransaction(txId: String): TransactionEntity? {
        return walletService.getTransaction(txId)
    }
    
    // Get total balance
    suspend fun getTotalBalance(): Map<Currency, Double> {
        return walletService.getTotalBalance()
    }
    
    // Select wallet
    fun selectWallet(wallet: com.ikoro.android.wallet.service.WalletInfo) {
        _selectedWallet.value = wallet
        _selectedCurrency.value = wallet.currency
    }
    
    // Select currency
    fun selectCurrency(currency: Currency) {
        _selectedCurrency.value = currency
        val wallet = wallets.value.find { it.currency == currency }
        _selectedWallet.value = wallet
    }
    
    // Clear selected
    fun clearSelection() {
        _selectedWallet.value = null
        _selectedCurrency.value = null
    }
    
    // Sync all balances
    suspend fun syncAllBalances(): Map<Currency, com.ikoro.android.wallet.service.BalanceSyncResult> {
        _syncingBalances.value = true
        val results = mutableMapOf<Currency, com.ikoro.android.wallet.service.BalanceSyncResult>()
        
        for (currency in Currency.values()) {
            results[currency] = syncBalance(currency)
        }
        
        _syncingBalances.value = false
        return results
    }
}
