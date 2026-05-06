package com.ikoro.android.ecommerce.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ikoro.android.ecommerce.model.Category
import com.ikoro.android.ecommerce.model.Order
import com.ikoro.android.ecommerce.model.Product
import com.ikoro.android.ecommerce.network.OfoApiService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Marketplace ViewModel
 * Manages product listing and search
 */
@HiltViewModel
class MarketplaceViewModel @Inject constructor(
    private val ofoApi: OfoApiService
) : ViewModel() {

    private val _products = MutableStateFlow<List<Product>>(emptyList())
    val products: StateFlow<List<Product>> = _products.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedCategory = MutableStateFlow<String?>(null)
    val selectedCategory: StateFlow<String?> = _selectedCategory.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        loadProducts()
    }

    fun loadProducts() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val products = ofoApi.getProducts()
                _products.value = products
            } catch (e: Exception) {
                // Handle error
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
        searchProducts()
    }

    fun selectCategory(category: String?) {
        _selectedCategory.value = category
        searchProducts()
    }

    private fun searchProducts() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val results = ofoApi.searchProducts(
                    query = _searchQuery.value,
                    category = _selectedCategory.value
                )
                _products.value = results
            } catch (e: Exception) {
                // Handle error
            } finally {
                _isLoading.value = false
            }
        }
    }
}

/**
 * Wallet ViewModel
 * Manages wallet operations and balance
 */
@HiltViewModel
class WalletViewModel @Inject constructor(
    private val ofoApi: OfoApiService
) : ViewModel() {

    private val _wallets = MutableStateFlow<List<com.ikoro.android.ecommerce.model.Wallet>>(emptyList())
    val wallets: StateFlow<List<com.ikoro.android.ecommerce.model.Wallet>> = _wallets.asStateFlow()

    private val _selectedWallet = MutableStateFlow<com.ikoro.android.ecommerce.model.Wallet?>(null)
    val selectedWallet: StateFlow<com.ikoro.android.ecommerce.model.Wallet?> = _selectedWallet.asStateFlow()

    private val _transactions = MutableStateFlow<List<com.ikoro.android.ecommerce.model.Transaction>>(emptyList())
    val transactions: StateFlow<List<com.ikoro.android.ecommerce.model.Transaction>> = _transactions.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        loadWallets()
    }

    fun loadWallets() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                _wallets.value = ofoApi.getWallets()
                if (_selectedWallet.value == null && _wallets.value.isNotEmpty()) {
                    _selectedWallet.value = _wallets.value.first()
                }
                loadTransactions()
            } catch (e: Exception) {
                // Handle error
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun loadTransactions() {
        viewModelScope.launch {
            try {
                val transactions = ofoApi.getTransactions(
                    walletId = _selectedWallet.value?.id ?: ""
                )
                _transactions.value = transactions
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    fun createWallet(label: String) {
        viewModelScope.launch {
            try {
                val wallet = ofoApi.createWallet(
                    label = label,
                    isDefault = false
                )
                loadWallets()
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    fun sendBitcoin(toAddress: String, amountSatoshi: Long) {
        viewModelScope.launch {
            try {
                val txId = ofoApi.sendBitcoin(
                    toAddress = toAddress,
                    amountSatoshi = amountSatoshi
                )
                loadTransactions()
            } catch (e: Exception) {
                // Handle error
            }
        }
    }
}

/**
 * Orders ViewModel
 * Manages order lifecycle
 */
@HiltViewModel
class OrdersViewModel @Inject constructor(
    private val ofoApi: OfoApiService
) : ViewModel() {

    private val _orders = MutableStateFlow<List<com.ikoro.android.ecommerce.model.Order>>(emptyList())
    val orders: StateFlow<List<com.ikoro.android.ecommerce.model.Order>> = _orders.asStateFlow()

    private val _selectedOrder = MutableStateFlow<com.ikoro.android.ecommerce.model.Order?>(null)
    val selectedOrder: StateFlow<com.ikoro.android.ecommerce.model.Order?> = _selectedOrder.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        loadOrders()
    }

    fun loadOrders() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                _orders.value = ofoApi.getOrders()
            } catch (e: Exception) {
                // Handle error
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun loadOrder(orderId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                _selectedOrder.value = ofoApi.getOrder(orderId)
            } catch (e: Exception) {
                // Handle error
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updateOrderStatus(orderId: String, status: String) {
        viewModelScope.launch {
            try {
                ofoApi.updateOrderStatus(orderId, status)
                loadOrders()
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    fun completeOrder(orderId: String) {
        viewModelScope.launch {
            try {
                ofoApi.completeOrder(orderId)
                loadOrders()
            } catch (e: Exception) {
                // Handle error
            }
        }
    }
}