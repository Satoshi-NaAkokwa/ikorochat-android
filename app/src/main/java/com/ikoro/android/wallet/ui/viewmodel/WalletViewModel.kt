package com.ikoro.android.wallet.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ikoro.android.wallet.data.model.Transaction
import com.ikoro.android.wallet.data.repository.TransactionRepository
import com.ikoro.android.wallet.data.model.Wallet
import com.ikoro.android.wallet.data.repository.WalletRepository
import com.ikoro.android.wallet.domain.service.WalletService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject


/**
 * Wallet State Management ViewModel
 */
@HiltViewModel
class WalletViewModel @Inject constructor(
    private val walletRepository: WalletRepository,
    private val transactionRepository: TransactionRepository,
    private val walletService: WalletService
) : ViewModel() {

    // Current selected currency
    private val _selectedCurrency = MutableStateFlow("BITCOIN")
    val selectedCurrency: StateFlow<String> = _selectedCurrency.asStateFlow()

    // Wallet state
    private val _wallets = MutableStateFlow<List<Wallet>>(emptyList())
    val wallets: StateFlow<List<Wallet>> = _wallets.asStateFlow()

    // Current active wallet (default to first wallet)
    private val _activeWallet = MutableStateFlow<Wallet?>(null)
    val activeWallet: StateFlow<Wallet?> = _activeWallet.asStateFlow()

    // Transactions list
    private val _transactions = MutableStateFlow<List<Transaction>>(emptyList())
    val transactions: StateFlow<List<Transaction>> = _transactions.asStateFlow()

    // Error state
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    // Loading state
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        loadWallets()
        loadTransactions()
    }

    fun loadWallets() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val wallets = walletRepository.getAllWallets()
                _wallets.value = wallets
                if (wallets.isNotEmpty()) {
                    _activeWallet.value = wallets.first()
                }
            } catch (e: Exception) {
                _error.value = "Failed to load wallets: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun loadTransactions() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val wallet = _activeWallet.value
                if (wallet != null) {
                    val transactions = transactionRepository.getTransactionsForWallet(wallet.id)
                    _transactions.value = transactions
                }
            } catch (e: Exception) {
                _error.value = "Failed to load transactions: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun getBalance(): String {
        val wallet = _activeWallet.value
        return wallet?.balances?.get(_selectedCurrency.value) ?: "0.00"
    }

    fun createTransaction(
        amount: Double,
        toAddress: String,
        description: String?,
        type: String = "SEND",
        toCurrency: String? = null
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val activeWallet = _activeWallet.value
                if (activeWallet == null) throw IllegalStateException("No active wallet")

                val currency = toCurrency ?: _selectedCurrency.value
                val transaction = Transaction(
                    id = 0,
                    walletId = activeWallet.id,
                    type = type,
                    amount = amount,
                    currency = currency,
                    toAddress = toAddress,
                    fromAddress = activeWallet.address,
                    status = "PENDING",
                    createdAt = System.currentTimeMillis(),
                    description = description
                )

                val result = transactionRepository.insertTransaction(transaction)
                if (result > 0) {
                    transactionRepository.submitToQueue(result)
                }
                loadTransactions()
            } catch (e: Exception) {
                _error.value = "Failed to create transaction: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updateActiveWallet(walletId: Long) {
        viewModelScope.launch {
            val wallets = _wallets.value
            val updatedWallet = wallets.find { it.id == walletId }
            _activeWallet.value = updatedWallet
            loadTransactions()
        }
    }

    fun changeCurrency(currency: String) {
        _selectedCurrency.value = currency
    }

    fun syncWithBackend() {
        viewModelScope.launch {
            val wallet = _activeWallet.value
            if (wallet != null) {
                walletService.syncBalance(wallet, _selectedCurrency.value)
                loadWallets()
            }
        }
    }

    fun restartWallet() {
        walletService.restart(_activeWallet.value)
    }

    fun signTransaction(amount: Double, toAddress: String): String {
        return walletService.signTransaction(
            wallet = _activeWallet.value,
            amount = amount,
            toAddress = toAddress
        )
    }

    fun clearError() {
        _error.value = null
    }
}
