package com.ikoro.android.wallet.hilt

import android.content.Context
import com.ikoro.android.wallet.database.TransactionDao
import com.ikoro.android.wallet.database.WalletDao
import com.ikoro.android.wallet.database.WalletDatabase
import com.ikoro.android.wallet.manager.KeyManager
import com.ikoro.android.wallet.manager.TransactionQueueManager
import com.ikoro.android.wallet.repository.WalletRepository
import com.ikoro.android.wallet.service.WalletService
import com.ikoro.android.wallet.service.MeshTransactionBroadcaster
import com.ikoro.android.wallet.service.signer.TransactionSigner
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ApplicationComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Singleton

/**
 * Hilt modules for dependency injection of wallet components
 */
@Module
@InstallIn(ApplicationComponent::class)
object WalletModule {
    
    @Provides
    @Singleton
    fun provideWalletDatabase(
        @ApplicationContext context: Context
    ): WalletDatabase {
        return WalletDatabase.getDatabase(context)
    }
    
    @Provides
    fun provideWalletDao(database: WalletDatabase): WalletDao {
        return database.walletDao()
    }
    
    @Provides
    fun provideTransactionDao(database: WalletDatabase): TransactionDao {
        return database.transactionDao()
    }
    
    @Provides
    @Singleton
    fun provideKeyManager(
        @ApplicationContext context: Context
    ): KeyManager {
        return KeyManager(context).apply { initialize() }
    }
    
    @Provides
    @Singleton
    fun provideTransactionSigner(
        @ApplicationContext context: Context
    ): TransactionSigner {
        return TransactionSigner(context)
    }
    
    @Provides
    @Singleton
    fun provideMeshTransactionBroadcaster(
        @ApplicationContext context: Context
    ): MeshTransactionBroadcaster {
        return MeshTransactionBroadcaster(context)
    }
    
    @Provides
    @Singleton
    fun provideWalletRepository(
        walletDao: WalletDao,
        transactionDao: TransactionDao
    ): WalletRepository {
        return WalletRepository(walletDao, transactionDao)
    }
    
    @Provides
    @Singleton
    fun provideTransactionQueueManager(
        @ApplicationContext context: Context,
        repository: WalletRepository
    ): TransactionQueueManager {
        return TransactionQueueManager(context, repository)
    }
    
    @Provides
    @Singleton
    fun provideWalletService(
        @ApplicationContext context: Context,
        repository: WalletRepository,
        keyManager: KeyManager,
        transactionSigner: TransactionSigner,
        meshBroadcaster: MeshTransactionBroadcaster
    ): WalletService {
        return WalletService(context, repository, keyManager, transactionSigner, meshBroadcaster).apply {
            initialize()
        }
    }
}

/**
 * Additional modules for AgbaraWallet integration
 */
@Module
@InstallIn(ApplicationComponent::class)
object AgbaraWalletModule {
    
    @Provides
    @Singleton
    fun provideAgbaraWalletConfig(): AgbaraWalletConfig {
        return AgbaraWalletConfig(
            backendUrl = "https://agbara-wallet-api.secretkeylabs.com",
            apiKey = "", // Should be configured via environment
            timeoutMs = 30000,
            supportCurrencies = setOf(
                "bitcoin",
                "stacks",
                "naira",
                "usdt",
                "usdc"
            )
        )
    }
}

/**
 * AgbaraWallet configuration
 */
data class AgbaraWalletConfig(
    val backendUrl: String,
    val apiKey: String,
    val timeoutMs: Long,
    val supportCurrencies: Set<String>
) {
    companion object {
        fun default(): AgbaraWalletConfig {
            return AgbaraWalletConfig(
                backendUrl = "https://agbara-wallet-api.secretkeylabs.com",
                apiKey = "",
                timeoutMs = 30000,
                supportCurrencies = setOf("bitcoin", "stacks", "naira", "usdt", "usdc")
            )
        }
    }
}
