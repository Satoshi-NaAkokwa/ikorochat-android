package com.ikoro.android.wallet.di

import com.ikoro.android.wallet.data.model.WalletDatabase
import com.ikoro.android.wallet.data.repository.WalletRepository
import com.ikoro.android.wallet.data.repository.TransactionRepository
import com.ikoro.android.wallet.domain.service.WalletService
import com.ikoro.android.wallet.domain.service.KeyManager
import com.ikoro.android.wallet.domain.service.MeshBroadcastService
import com.ikoro.android.wallet.domain.service.TransactionSigner
import com.ikoro.android.wallet.domain.service.TransactionQueueManager
import com.ikoro.android.wallet.ui.viewmodel.WalletViewModel
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.components.ServiceComponent
import dagger.hilt.android.scopes.ViewModelScoped
import dagger.hilt.android.scopes.ServiceScoped
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import androidx.room.Room
import androidx.room.RoomDatabase
import android.content.Context


/**
 * Wallet module - Hilt dependency injection bindings
 */
@Module
@InstallIn(SingletonComponent::class)
object WalletModule {

    @Provides
    @Singleton
    fun provideWalletDatabase(context: Context): WalletDatabase {
        return Room.databaseBuilder(
            context,
            WalletDatabase::class.java,
            "wallet.db"
        )
        .build()
    }

    @Provides
    fun provideWalletRepository(database: WalletDatabase): WalletRepository {
        return WalletRepository(database)
    }

    @Provides
    fun provideTransactionRepository(walletRepository: WalletRepository): TransactionRepository {
        return TransactionRepository(walletRepository)
    }

    @Provides
    @Singleton
    fun provideKeyManager(context: Context): KeyManager {
        return KeyManager(context)
    }

    @Provides
    @Singleton
    fun provideWalletService(context: Context): WalletService {
        return WalletService(context)
    }

    @Provides
    @Singleton
    fun provideMeshBroadcastService(walletRepository: WalletRepository): MeshBroadcastService {
        return MeshBroadcastService()
        // TODO: Inject MeshSync properly
    }

    @Provides
    @Singleton
    fun provideTransactionSigner(keyManager: KeyManager): TransactionSigner {
        return TransactionSigner(keyManager)
    }

    @Provides
    @Singleton
    fun provideTransactionQueueManager(walletService: WalletService): TransactionQueueManager {
        return TransactionQueueManager(walletService)
    }
}
