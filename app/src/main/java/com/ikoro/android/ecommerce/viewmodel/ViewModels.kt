package com.ikoro.android.ecommerce.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ikoro.android.ecommerce.data.model.Product
import com.ikoro.android.ecommerce.domain.repository.ProductRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Product ViewModel
 * Manages product listing and search
 */
@HiltViewModel
class ProductViewModel @Inject constructor(
    private val productRepository: ProductRepository
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
                _products.value = productRepository.getProducts()
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
                val results = productRepository.searchProducts(
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
 * Manages wallet operations
 */
@HiltViewModel
class WalletViewModel @Inject constructor(
    private val walletRepository: WalletRepository
) : ViewModel() {

    private val _wallets = MutableStateFlow<List<com.ofo.data.model.Wallet>>(emptyList())
    val wallets: StateFlow<List<com.ofo.data.model.Wallet>> = _wallets.asStateFlow()

    private val _selectedWallet = MutableStateFlow<com.ofo.data.model.Wallet?>(null)
    val selectedWallet: StateFlow<com.ofo.data.model.Wallet?> = _selectedWallet.asStateFlow()

    private val _transactions = MutableStateFlow<List<com.ofo.data.model.Transaction>>(emptyList())
    val transactions: StateFlow<List<com.ofo.data.model.Transaction>> = _transactions.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        loadWallets()
    }

    fun loadWallets() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                _wallets.value = walletRepository.getWallets()
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

    fun selectWallet(wallet: com.ofo.data.model.Wallet) {
        _selectedWallet.value = wallet
        loadTransactions()
    }

    private fun loadTransactions() {
        viewModelScope.launch {
            try {
                _transactions.value = walletRepository.getTransactions(
                    walletId = _selectedWallet.value?.id
                )
            } catch (e: Exception) {
                // Handle error
            }
        }
    }
}

/**
 * Order ViewModel
 * Manages order operations
 */
@HiltViewModel
class OrderViewModel @Inject constructor(
    private val orderRepository: OrderRepository
) : ViewModel() {

    private val _orders = MutableStateFlow<List<com.ofo.data.model.Order>>(emptyList())
    val orders: StateFlow<List<com.ofo.data.model.Order>> = _orders.asStateFlow()

    private val _selectedOrder = MutableStateFlow<com.ofo.data.model.Order?>(null)
    val selectedOrder: StateFlow<com.ofo.data.model.Order?> = _selectedOrder.asStateFlow()

    private val _timeline = MutableStateFlow<List<Any>>(emptyList())
    val timeline: StateFlow<List<Any>> = _timeline.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        loadOrders()
    }

    fun loadOrders() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                _orders.value = orderRepository.getOrders()
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
                val order = orderRepository.getOrder(orderId)
                _selectedOrder.value = order
                _timeline.value = orderRepository.getOrderTimeline(orderId)
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
                orderRepository.updateOrderStatus(orderId, status)
                loadOrders()
            } catch (e: Exception) {
                // Handle error
            }
        }
    }
}