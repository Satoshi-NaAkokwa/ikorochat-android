package com.ikoro.android.ecommerce.data.database

import androidx.room.*
import com.ikoro.android.ecommerce.data.model.*
import kotlinx.coroutines.flow.Flow

/**
 * User Data Access Object
 */
@Dao
interface UserDao {
    @Query("SELECT * FROM users WHERE id = :userId")
    suspend fun getUserById(userId: String): User?

    @Query("SELECT * FROM users WHERE public_key = :publicKey")
    suspend fun getUserByPublicKey(publicKey: String): User?

    @Query("SELECT * FROM users WHERE identity_hash = :identityHash")
    suspend fun getUserByIdentityHash(identityHash: String): User?

    @Query("SELECT * FROM users")
    fun getAllUsers(): Flow<List<User>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: User)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUsers(users: List<User>)

    @Update
    suspend fun updateUser(user: User)

    @Delete
    suspend fun deleteUser(user: User)

    @Query("DELETE FROM users")
    suspend fun deleteAllUsers()
}

/**
 * Product Data Access Object
 */
@Dao
interface ProductDao {
    @Query("SELECT * FROM products WHERE id = :productId")
    suspend fun getProductById(productId: String): Product?

    @Query("SELECT * FROM products WHERE seller_id = :sellerId")
    fun getProductsBySeller(sellerId: String): Flow<List<Product>>

    @Query("SELECT * FROM products WHERE category_id = :categoryId")
    fun getProductsByCategory(categoryId: String): Flow<List<Product>>

    @Query("SELECT * FROM products WHERE title LIKE :query OR description LIKE :query")
    fun searchProducts(query: String): Flow<List<Product>>

    @Query("SELECT * FROM products ORDER BY created_at DESC LIMIT :limit OFFSET :offset")
    fun getRecentProducts(limit: Int = 20, offset: Int = 0): Flow<List<Product>>

    @Query("SELECT * FROM products")
    fun getAllProducts(): Flow<List<Product>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProduct(product: Product)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProducts(products: List<Product>)

    @Update
    suspend fun updateProduct(product: Product)

    @Delete
    suspend fun deleteProduct(product: Product)

    @Query("DELETE FROM products WHERE id = :productId")
    suspend fun deleteProductById(productId: String)

    @Query("DELETE FROM products")
    suspend fun deleteAllProducts()
}

/**
 * Order Data Access Object
 */
@Dao
interface OrderDao {
    @Query("SELECT * FROM orders WHERE id = :orderId")
    suspend fun getOrderById(orderId: String): Order?

    @Query("SELECT * FROM orders WHERE buyer_id = :buyerId ORDER BY created_at DESC")
    fun getOrdersByBuyer(buyerId: String): Flow<List<Order>>

    @Query("SELECT * FROM orders WHERE seller_id = :sellerId ORDER BY created_at DESC")
    fun getOrdersBySeller(sellerId: String): Flow<List<Order>>

    @Query("SELECT * FROM orders WHERE status = :status")
    fun getOrdersByStatus(status: OrderStatus): Flow<List<Order>>

    @Query("SELECT * FROM orders")
    fun getAllOrders(): Flow<List<Order>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrder(order: Order)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrders(orders: List<Order>)

    @Update
    suspend fun updateOrder(order: Order)

    @Delete
    suspend fun deleteOrder(order: Order)

    @Query("DELETE FROM orders WHERE id = :orderId")
    suspend fun deleteOrderById(orderId: String)

    @Query("DELETE FROM orders")
    suspend fun deleteAllOrders()
}

/**
 * Transaction Data Access Object
 */
@Dao
interface TransactionDao {
    @Query("SELECT * FROM transactions WHERE id = :transactionId")
    suspend fun getTransactionById(transactionId: String): Transaction?

    @Query("SELECT * FROM transactions WHERE order_id = :orderId")
    fun getTransactionsByOrder(orderId: String): Flow<List<Transaction>>

    @Query("SELECT * FROM transactions WHERE tx_type = :txType")
    fun getTransactionsByType(txType: TransactionType): Flow<List<Transaction>>

    @Query("SELECT * FROM transactions WHERE status = :status")
    fun getTransactionsByStatus(status: TransactionStatus): Flow<List<Transaction>>

    @Query("SELECT * FROM transactions ORDER BY created_at DESC")
    fun getAllTransactions(): Flow<List<Transaction>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: Transaction)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransactions(transactions: List<Transaction>)

    @Update
    suspend fun updateTransaction(transaction: Transaction)

    @Delete
    suspend fun deleteTransaction(transaction: Transaction)

    @Query("DELETE FROM transactions WHERE id = :transactionId")
    suspend fun deleteTransactionById(transactionId: String)

    @Query("DELETE FROM transactions")
    suspend fun deleteAllTransactions()
}

/**
 * Wallet Data Access Object
 */
@Dao
interface WalletDao {
    @Query("SELECT * FROM wallets WHERE id = :walletId")
    suspend fun getWalletById(walletId: String): Wallet?

    @Query("SELECT * FROM wallets WHERE user_id = :userId")
    fun getWalletsByUser(userId: String): Flow<List<Wallet>>

    @Query("SELECT * FROM wallets WHERE user_id = :userId AND is_default = 1 LIMIT 1")
    suspend fun getDefaultWallet(userId: String): Wallet?

    @Query("SELECT * FROM wallets WHERE address = :address")
    suspend fun getWalletByAddress(address: String): Wallet?

    @Query("SELECT * FROM wallets")
    fun getAllWallets(): Flow<List<Wallet>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWallet(wallet: Wallet)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWallets(wallets: List<Wallet>)

    @Update
    suspend fun updateWallet(wallet: Wallet)

    @Delete
    suspend fun deleteWallet(wallet: Wallet)

    @Query("DELETE FROM wallets WHERE id = :walletId")
    suspend fun deleteWalletById(walletId: String)

    @Query("DELETE FROM wallets")
    suspend fun deleteAllWallets()
}

/**
 * Category Data Access Object
 */
@Dao
interface CategoryDao {
    @Query("SELECT * FROM categories WHERE id = :categoryId")
    suspend fun getCategoryById(categoryId: String): Category?

    @Query("SELECT * FROM categories WHERE parent_id = :parentId")
    fun getCategoriesByParent(parentId: String?): Flow<List<Category>>

    @Query("SELECT * FROM categories ORDER BY sort_order ASC")
    fun getAllCategories(): Flow<List<Category>>

    @Query("SELECT * FROM categories WHERE parent_id IS NULL ORDER BY sort_order ASC")
    fun getRootCategories(): Flow<List<Category>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategory(category: Category)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategories(categories: List<Category>)

    @Update
    suspend fun updateCategory(category: Category)

    @Delete
    suspend fun deleteCategory(category: Category)

    @Query("DELETE FROM categories WHERE id = :categoryId")
    suspend fun deleteCategoryById(categoryId: String)

    @Query("DELETE FROM categories")
    suspend fun deleteAllCategories()
}

/**
 * Message Data Access Object
 */
@Dao
interface MessageDao {
    @Query("SELECT * FROM messages WHERE id = :messageId")
    suspend fun getMessageById(messageId: String): Message?

    @Query("SELECT * FROM messages WHERE sender_id = :senderId ORDER BY created_at DESC")
    fun getMessagesBySender(senderId: String): Flow<List<Message>>

    @Query("SELECT * FROM messages WHERE receiver_id = :receiverId ORDER BY created_at DESC")
    fun getMessagesByReceiver(receiverId: String): Flow<List<Message>>

    @Query("SELECT * FROM messages WHERE (sender_id = :userId OR receiver_id = :userId) ORDER BY created_at DESC")
    fun getMessagesByUser(userId: String): Flow<List<Message>>

    @Query("SELECT * FROM messages WHERE order_id = :orderId ORDER BY created_at ASC")
    fun getMessagesByOrder(orderId: String): Flow<List<Message>>

    @Query("SELECT * FROM messages WHERE is_read = 0")
    fun getUnreadMessages(): Flow<List<Message>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: Message)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessages(messages: List<Message>)

    @Update
    suspend fun updateMessage(message: Message)

    @Query("UPDATE messages SET is_read = 1 WHERE id = :messageId")
    suspend fun markAsRead(messageId: String)

    @Delete
    suspend fun deleteMessage(message: Message)

    @Query("DELETE FROM messages WHERE id = :messageId")
    suspend fun deleteMessageById(messageId: String)

    @Query("DELETE FROM messages")
    suspend fun deleteAllMessages()
}

/**
 * Verification Data Access Object
 */
@Dao
interface VerificationDao {
    @Query("SELECT * FROM verifications WHERE id = :verificationId")
    suspend fun getVerificationById(verificationId: String): Verification?

    @Query("SELECT * FROM verifications WHERE user_id = :userId ORDER BY created_at DESC")
    fun getVerificationsByUser(userId: String): Flow<List<Verification>>

    @Query("SELECT * FROM verifications WHERE user_id = :userId AND verification_type = :type")
    fun getVerificationsByUserAndType(userId: String, type: VerificationType): Flow<List<Verification>>

    @Query("SELECT * FROM verifications WHERE status = :status")
    fun getVerificationsByStatus(status: VerificationStatus): Flow<List<Verification>>

    @Query("SELECT * FROM verifications")
    fun getAllVerifications(): Flow<List<Verification>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVerification(verification: Verification)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVerifications(verifications: List<Verification>)

    @Update
    suspend fun updateVerification(verification: Verification)

    @Delete
    suspend fun deleteVerification(verification: Verification)

    @Query("DELETE FROM verifications WHERE id = :verificationId")
    suspend fun deleteVerificationById(verificationId: String)

    @Query("DELETE FROM verifications")
    suspend fun deleteAllVerifications()
}