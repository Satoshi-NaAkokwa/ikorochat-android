package com.ikoro.android.ecommerce.data.repository

import com.ikoro.android.ecommerce.data.database.Daos
import com.ikoro.android.ecommerce.data.model.Product
import kotlinx.coroutines.flow.Flow

class ProductRepository(private val productDao: Daos.ProductDao) {

    fun getProducts(): Flow<List<Product>> {
        return productDao.getAllProducts()
    }

    fun getProductById(id: String): Flow<Product?> {
        return productDao.getProductById(id)
    }

    suspend fun insertProduct(product: Product) {
        productDao.insertProduct(product)
    }

    suspend fun updateProduct(product: Product) {
        productDao.updateProduct(product)
    }

    suspend fun deleteProduct(product: Product) {
        productDao.deleteProduct(product)
    }
}

class OrderRepository(private val orderDao: Daos.OrderDao) {

    fun getOrders(): Flow<List<com.ikoro.android.ecommerce.data.model.Order>> {
        return orderDao.getAllOrders()
    }

    fun getOrderById(id: String): Flow<com.ikoro.android.ecommerce.data.model.Order?> {
        return orderDao.getOrderById(id)
    }

    suspend fun insertOrder(order: com.ikoro.android.ecommerce.data.model.Order) {
        orderDao.insertOrder(order)
    }

    suspend fun updateOrder(order: com.ikoro.android.ecommerce.data.model.Order) {
        orderDao.updateOrder(order)
    }
}

class WalletRepository(
    private val walletDao: Daos.WalletDao,
    private val transactionDao: Daos.TransactionDao
) {

    fun getWallets(): Flow<List<com.ikoro.android.ecommerce.data.model.Wallet>> {
        return walletDao.getAllWallets()
    }

    suspend fun createWallet(wallet: com.ikoro.android.ecommerce.data.model.Wallet) {
        walletDao.insertWallet(wallet)
    }

    fun getTransactions(walletId: String): Flow<List<com.ikoro.android.ecommerce.data.model.Transaction>> {
        return transactionDao.getTransactionsByWalletId(walletId)
    }
}

class TransactionRepository(private val transactionDao: Daos.TransactionDao) {

    suspend fun insertTransaction(transaction: com.ikoro.android.ecommerce.data.model.Transaction) {
        transactionDao.insertTransaction(transaction)
    }

    fun getAllTransactions(): Flow<List<com.ikoro.android.ecommerce.data.model.Transaction>> {
        return transactionDao.getAllTransactions()
    }
}