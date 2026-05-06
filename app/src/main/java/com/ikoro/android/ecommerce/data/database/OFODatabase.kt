package com.ikoro.android.ecommerce.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.ikoro.android.ecommerce.data.model.*

/**
 * OFO Room Database
 * Local database for offline functionality
 */
@Database(
    entities = [
        User::class,
        Product::class,
        Order::class,
        Transaction::class,
        Wallet::class,
        Category::class,
        Message::class,
        Verification::class
    ],
    version = 1,
    exportSchema = true
)
abstract class OFODatabase : RoomDatabase() {

    abstract fun userDao(): UserDao
    abstract fun productDao(): ProductDao
    abstract fun orderDao(): OrderDao
    abstract fun transactionDao(): TransactionDao
    abstract fun walletDao(): WalletDao
    abstract fun categoryDao(): CategoryDao
    abstract fun messageDao(): MessageDao
    abstract fun verificationDao(): VerificationDao

    companion object {
        const val DATABASE_NAME = "ofo_database"
    }
}