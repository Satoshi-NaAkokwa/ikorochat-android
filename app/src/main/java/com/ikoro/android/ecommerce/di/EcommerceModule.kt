package com.ikoro.android.ecommerce.di

import android.content.Context
import androidx.room.Room
import com.ikoro.android.ecommerce.data.database.OFODatabase
import com.ikoro.android.ecommerce.data.database.Daos
import com.ikoro.android.ecommerce.data.repository.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object EcommerceModule {

    @Provides
    @Singleton
    fun provideOFODatabase(@ApplicationContext context: Context): OFODatabase {
        return Room.databaseBuilder(
            context,
            OFODatabase::class.java,
            "ofo_database"
        )
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    @Singleton
    fun provideProductDao(database: OFODatabase): Daos.ProductDao {
        return database.productDao()
    }

    @Provides
    @Singleton
    fun provideOrderDao(database: OFODatabase): Daos.OrderDao {
        return database.orderDao()
    }

    @Provides
    @Singleton
    fun provideWalletDao(database: OFODatabase): Daos.WalletDao {
        return database.walletDao()
    }

    @Provides
    @Singleton
    fun provideTransactionDao(database: OFODatabase): Daos.TransactionDao {
        return database.transactionDao()
    }

    @Provides
    @Singleton
    fun provideProductRepository(productDao: Daos.ProductDao): ProductRepository {
        return ProductRepository(productDao)
    }

    @Provides
    @Singleton
    fun provideOrderRepository(orderDao: Daos.OrderDao): OrderRepository {
        return OrderRepository(orderDao)
    }

    @Provides
    @Singleton
    fun provideWalletRepository(walletDao: Daos.WalletDao, transactionDao: Daos.TransactionDao): WalletRepository {
        return WalletRepository(walletDao, transactionDao)
    }

    @Provides
    @Singleton
    fun provideTransactionRepository(transactionDao: Daos.TransactionDao): TransactionRepository {
        return TransactionRepository(transactionDao)
    }
}