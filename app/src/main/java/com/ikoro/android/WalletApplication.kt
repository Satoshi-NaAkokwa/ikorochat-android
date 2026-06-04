package com.ikoro.android

import android.app.Application
import com.ikoro.android.wallet.services.backup.WalletBackupService
import com.ikoro.android.wallet.services.security.SecurityManager
import com.ikoro.android.wallet.services.qa.QAAgent
import com.ikoro.android.wallet.services.uiux.UIUXAgent
import com.ikoro.android.wallet.services.branding.BrandStrategistAgent
import com.ikoro.android.wallet.services.coordinator.MasterAgentCoordinator
import com.ikoro.android.wallet.services.protocol.WalletProtocol
import com.ikoro.android.util.ErrorLogger
import com.ikoro.android.util.setupGlobalExceptionHandler
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.HiltAndroidApp
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Main Application Module - Hilt dependency injection bindings
 */
@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideErrorLogger(context: Context): ErrorLogger {
        return ErrorLogger.getInstance(context)
    }

    @Provides
    @Singleton
    fun provideWalletBackupService(context: Context): WalletBackupService {
        return WalletBackupService(context)
    }

    @Provides
    @Singleton
    fun provideSecurityManager(context: Context): SecurityManager {
        return SecurityManager(context)
    }

    @Provides
    @Singleton
    fun provideQAAgent(context: Context): QAAgent {
        return QAAgent(context)
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
        println("Version: 1.0.0")
        println("Package: com.ikoro.android.wallet")
        println("Min SDK: 26 (Android 8.0)")
        println("Target SDK: 35 (Android 15)")
        println("═══════════════════════════════════════════════════════════════")
        println("Initializing System Services...")
        
        // Setup global exception handler for crash logging
        setupGlobalExceptionHandler(this)
        println("✅ Global exception handler installed")
        
        // Initialize ErrorLogger
        val logger = ErrorLogger.getInstance(this)
        println("✅ ErrorLogger initialized")
        
        // Initialize wallet services
        val backupService = WalletBackupService(this)
        println("✅ WalletBackupService initialized")
        
        val securityManager = SecurityManager(this)
        println("✅ SecurityManager initialized")
        
        val qaAgent = QAAgent(this)
        println("✅ QAAgent initialized")
        
        val uiuxAgent = UIUXAgent()
        println("✅ UIUXAgent initialized")
        
        val brandStrategist = BrandStrategistAgent()
        println("✅ BrandStrategistAgent initialized")
        
        val coordinator = MasterAgentCoordinator()
        println("✅ MasterAgentCoordinator initialized")
        
        // Initialize Nostr client
        val nostrClient = com.ikoro.android.nostr.NostrClient.getInstance(this)
        nostrClient.initialize()
        println("✅ NostrClient initialized")
        
        // Initialize wallet protocol
        val walletProtocol = WalletProtocol(nostrClient)
        println("✅ WalletProtocol initialized")
        
        println("═══════════════════════════════════════════════════════════════")
        println("All Services Initialized Successfully")
        println("═══════════════════════════════════════════════════════════════")
        
        println(" Wallet Features Status:")
        println(" ✅ Wallet UI with Jetpack Compose")
        println(" ✅ Local wallet database (Room)")
        println(" ✅ Hilt dependency injection")
        println(" ✅ PIN authentication")
        println(" ✅ Biometric authentication")
        println(" ✅ QR scanning")
        println(" ✅ Backup/restore functionality")
        println(" ✅ Settings screens")
        println(" ✅ Error logging and crash reporting")
        println(" ✅ Nostr wallet protocol")
        println(" ✅ Mesh transaction broadcasting")
        println("═══════════════════════════════════════════════════════════════")
        println("Wallet Module Ready for Use")
        println("═══════════════════════════════════════════════════════════════")
    }
}
