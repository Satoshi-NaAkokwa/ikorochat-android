package com.ikoro.android.wallet.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.ikoro.android.data.model.Currency
import com.ikoro.android.data.model.TransactionStatus
import com.ikoro.android.wallet.model.WalletEntity
import com.ikoro.android.wallet.model.TransactionEntity
import com.ikoro.android.wallet.converters.TypeConverter

/**
 * Room Database for wallet data persistence
 * Contains wallets and transactions tables
 */
@Database(
    entities = [WalletEntity::class, TransactionEntity::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(TypeConverter::class)
abstract class WalletDatabase : RoomDatabase() {
    
    abstract fun walletDao(): WalletDao
    abstract fun transactionDao(): TransactionDao
    
    companion object {
        @Volatile
        private var INSTANCE: WalletDatabase? = null
        
        fun getDatabase(context: Context): WalletDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    WalletDatabase::class.java,
                    "wallet_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
        
        fun clearInstance() {
            INSTANCE = null
        }
    }
}
