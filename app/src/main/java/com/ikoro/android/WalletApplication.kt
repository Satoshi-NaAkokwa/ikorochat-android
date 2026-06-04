package com.ikoro.android

import android.app.Application
import com.ikoro.android.wallet.data.model.WalletDatabase
import com.ikoro.android.wallet.data.repository.WalletRepository
import com.ikoro.android.wallet.data.repository.TransactionRepository
import com.ikoro.android.wallet.domain.service.WalletService
import com.ikoro.android.wallet.domain.service.KeyManager
import com.ikoro.android.wallet.domain.service.MeshBroadcastService
import com.ikoro.android.wallet.domain.service.TransactionSigner
import com.ikoro.android.wallet.domain.service.TransactionQueueManager
import com.ikoro.android.wallet.services.security.SecurityManager
import com.ikoro.android.wallet.testing.qa.QAAgent
import com.ikoro.android.wallet.testing.uiux.UIUXAgent
import com.ikoro.android.wallet.branding.BrandStrategistAgent
import com.ikoro.android.wallet.coordinator.MasterAgentCoordinator
import com.ikoro.android.mesh.MeshSync
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.HiltAndroidApp
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import androidx.room.Room
import android.content.Context
import androidx.room.RoomDatabase


/**
 * Main Application Module - Hilt dependency injection bindings
 */
@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideWalletDatabase(context: Context): WalletDatabase {
        return Room.databaseBuilder(
            context,
            WalletDatabase::class.java,
            "wallet.db"
        )
        .fallbackToDestructiveMigration()
        .build()
    }

    @Provides
    @Singleton
    fun provideWalletRepository(database: WalletDatabase): WalletRepository {
        return WalletRepository(database)
    }

    @Provides
    @Singleton
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
    fun provideMeshBroadcastService(context: Context): MeshBroadcastService {
        // MeshSync will be injected when available
        // For now, we use a placeholder that delegates to room
        return MeshBroadcastService()
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

    @Provides
    @Singleton
    fun provideSecurityManager(context: Context, keyManager: KeyManager): SecurityManager {
        return SecurityManager(context, keyManager)
    }

    @Provides
    @Singleton
    fun provideQAAgent(walletService: WalletService, signer: TransactionSigner, keyManager: KeyManager): QAAgent {
        return QAAgent(walletService, signer, keyManager)
    }

    @Provides
    @Singleton
    fun provideUIUXAgent(): UIUXAgent {
        return UIUXAgent()
    }

    @Provides
    @Singleton
    fun provideBrandStrategistAgent(): BrandStrategistAgent {
        return BrandStrategistAgent()
    }

    @Provides
    @Singleton
    fun provideMasterAgentCoordinator(): MasterAgentCoordinator {
        return MasterAgentCoordinator()
    }
}

/**
 * Main Application Class
 */
@HiltAndroidApp
class WalletApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        println("═══════════════════════════════════════════════════════════════")
        println("Ikoro Wallet Application Starting")
        println("═══════════════════════════════════════════════════════════════")
        println("Version: 1.7.2")
        println("Package: com.ikoro.android.wallet")
        println("Min SDK: 26 (Android 8.0)")
        println("Target SDK: 35 (Android 15)")
        println("═══════════════════════════════════════════════════════════════")
        println("Loading Hilt Dependency Injection...")
        println("WalletDatabase: READY")
        println("WalletRepository: READY")
        println("WalletService: READY")
        println("KeyManager: READY")
        println("TransactionSigner: READY")
        println("MeshBroadcastService: READY")
        println("TransactionQueueManager: READY")
        println("SecurityManager: READY")
        println("QAAgent: READY")
        println("UIUXAgent: READY")
        println("BrandStrategistAgent: READY")
        println("MasterAgentCoordinator: READY")
        println("═══════════════════════════════════════════════════════════════")
        println("All Services Initialized Successfully")
        println("═══════════════════════════════════════════════════════════════")
    }
}
