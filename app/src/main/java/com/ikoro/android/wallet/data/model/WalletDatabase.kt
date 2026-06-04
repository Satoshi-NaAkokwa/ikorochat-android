package com.ikoro.android.wallet.data.model

import androidx.room.*


/**
 * Database class for wallet operations
 */
@Database(
    entities = [Wallet::class, Transaction::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class WalletDatabase : RoomDatabase() {
    abstract fun walletDao(): WalletDao
    abstract fun transactionDao(): TransactionDao
}
