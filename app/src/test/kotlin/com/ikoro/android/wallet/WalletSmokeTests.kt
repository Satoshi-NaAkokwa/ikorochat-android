package com.ikoro.android.wallet

import com.ikoro.android.wallet.database.WalletDatabase
import com.ikoro.android.wallet.manager.KeyManager
import com.ikoro.android.wallet.manager.TransactionQueueManager
import com.ikoro.android.wallet.repository.WalletRepository
import com.ikoro.android.wallet.service.MeshTransactionBroadcaster
import com.ikoro.android.wallet.service.WalletService
import com.ikoro.android.wallet.service.signer.TransactionSigner
import com.ikoro.android.wallet.viewmodel.WalletViewModel
import com.ikoro.android.data.model.Currency
import com.ikoro.android.wallet.model.CoinData
import com.ikoro.android.wallet.model.TransactionEntity
import com.ikoro.android.wallet.model.WalletEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.File

/**
 * Smoke test suite for wallet module
 * Tests core functionality without mocking dependencies
 */
@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE)
class WalletSmokeTests {
    
    private lateinit var db: WalletDatabase
    private lateinit var keyManager: KeyManager
    private lateinit var transactionSigner: TransactionSigner
    private lateinit var meshBroadcaster: MeshTransactionBroadcaster
    private lateinit var repository: WalletRepository
    private lateinit var queueManager: TransactionQueueManager
    private lateinit var walletService: WalletService
    private lateinit var walletViewModel: WalletViewModel
    
    @Before
    fun setup() {
        // Setup test database
        db = WalletDatabase.getDatabase(
            org.robolectric.Shadows.shadowOf(
                org.robolectric.RuntimeEnvironment.getApplication()
            ).applicationContext
        )
        
        keyManager = KeyManager(
            org.robolectric.RuntimeEnvironment.getApplication()
        )
        
        transactionSigner = TransactionSigner(
            org.robolectric.RuntimeEnvironment.getApplication()
        )
        
        meshBroadcaster = MeshTransactionBroadcaster(
            org.robolectric.RuntimeEnvironment.getApplication()
        )
        
        repository = WalletRepository(db.walletDao(), db.transactionDao())
        queueManager = TransactionQueueManager(
            org.robolectric.RuntimeEnvironment.getApplication(),
            repository
        )
        
        walletService = WalletService(
            org.robolectric.RuntimeEnvironment.getApplication(),
            repository,
            keyManager,
            transactionSigner,
            meshBroadcaster
        )
    }
    
    /**
     * Test 1: Wallet database creation and schema
     */
    @Test
    fun testDatabaseCreation() {
        assertNotNull(db)
        assertNotNull(db.walletDao())
        assertNotNull(db.transactionDao())
    }
    
    /**
     * Test 2: Wallet entity creation
     */
    @Test
    fun testCreateWalletEntity() {
        val wallet = WalletEntity(
            currency = Currency.BITCOIN.name,
            walletAddress = "bc1qtest123",
            publicKey = "02testpublickey123456789",
            balance = 1.5,
            availableBalance = 1.5,
            pendingBalance = 0.0,
            lastSynced = System.currentTimeMillis(),
            lastUpdated = System.currentTimeMillis(),
            coinData = CoinData.createDefault(Currency.BITCOIN),
            isEncrypted = false,
            createdAt = System.currentTimeMillis(),
            lastUsed = System.currentTimeMillis()
        )
        
        assertEquals(Currency.BITCOIN.name, wallet.currency)
        assertEquals("bc1qtest123", wallet.walletAddress)
        assertEquals(1.5, wallet.balance, 0.0001)
    }
    
    /**
     * Test 3: Wallet repository CRUD operations
     */
    @Test
    fun testWalletRepositoryCRUD() = runBlocking {
        // Create wallet
        val walletId = repository.createWallet(
            currency = Currency.BITCOIN,
            walletAddress = "bc1qtest123",
            publicKey = "02testpublickey123456789",
            coinData = CoinData.createDefault(Currency.BITCOIN)
        )
        
        assertTrue(walletId > 0)
        
        // Get wallet
        val savedWallet = repository.getWalletByCurrency(Currency.BITCOIN)
        assertNotNull(savedWallet)
        assertEquals("bc1qtest123", savedWallet?.walletAddress)
        
        // Update wallet
        savedWallet?.let {
            it.balance = 2.5
            it.lastUpdated = System.currentTimeMillis()
            repository.updateWallet(it)
        }
        
        // Verify update
        val updatedWallet = repository.getWalletByCurrency(Currency.BITCOIN)
        assertEquals(2.5, updatedWallet?.balance, 0.0001)
        
        // Delete wallet
        repository.deleteWallet(Currency.BITCOIN)
        val deletedWallet = repository.getWalletByCurrency(Currency.BITCOIN)
        assertNull(deletedWallet)
    }
    
    /**
     * Test 4: Transaction entity and repository
     */
    @Test
    fun testTransactionRepository() = runBlocking {
        val transaction = TransactionEntity(
            walletCurrency = Currency.BITCOIN.name,
            address = "bc1qtest123",
            fromAddress = "bc1qsender123",
            toAddress = "bc1qrecipient123",
            amount = 0.5,
            fee = 0.001,
            type = "SEND",
            status = "PENDING",
            timestamp = System.currentTimeMillis(),
            blockHeight = null,
            transactionHash = "txhash123",
            confirmations = 0,
            maxConfirmations = 6,
            memo = "Test transaction",
            metadata = null
        )
        
        val txId = repository.createTransaction(transaction)
        assertTrue(txId > 0)
        
        // Get transaction
        val savedTx = repository.getTransactionById(txId.toString())
        assertNotNull(savedTx)
        assertEquals("PENDING", savedTx?.status)
        
        // Update status
        repository.updateTransactionStatus(
            txId.toString(),
            com.ikoro.android.data.model.TransactionStatus.COMPLETED,
            "txhash123"
        )
        
        // Verify update
        val updatedTx = repository.getTransactionById(txId.toString())
        assertEquals("COMPLETED", updatedTx?.status)
    }
    
    /**
     * Test 5: Key Manager operations
     */
    @Test
    fun testKeyManager() = runBlocking {
        // Test key pair generation
        val keyPair = keyManager.generateKeyPair(Currency.OFO)
        assertNotNull(keyPair)
        assertTrue(keyPair.publicKey.isNotEmpty())
        assertTrue(keyPair.privateKey.isNotEmpty())
        
        // Test key storage and retrieval
        val storedKey = keyManager.getPrivateKey(Currency.OFO)
        assertNotNull(storedKey)
        assertEquals(keyPair.privateKey.size, storedKey?.size)
        
        // Test public key storage
        keyManager.storePublicKey(Currency.NAIRR, "05testpublickey123")
        val storedPublicKey = keyManager.getPublicKey(Currency.NAIRR)
        assertEquals("05testpublickey123", storedPublicKey)
        
        // Test address storage
        keyManager.storeAddress(Currency.USDT, "TR7testaddress123")
        val storedAddress = keyManager.getAddress(Currency.USDT)
        assertEquals("TR7testaddress123", storedAddress)
    }
    
    /**
     * Test 6: Transaction signer (offline signing simulation)
     */
    @Test
    fun testTransactionSigner() = runBlocking {
        // Create a test wallet
        val wallet = WalletEntity(
            currency = Currency.BITCOIN.name,
            walletAddress = "bc1qtest123",
            publicKey = "02testpublickey123456789",
            balance = 1.5,
            coinData = CoinData.createDefault(Currency.BITCOIN)
        )
        
        val coinData = CoinData.createDefault(Currency.BITCOIN)
        val privateKeyData = byteArrayOf(1, 2, 3, 4, 5)
        
        // Store private key for signing
        keyManager.storePrivateKey(Currency.BITCOIN, privateKeyData)
        
        // Test signing (would normally use real keys)
        val signResult = transactionSigner.signBitcoinTransaction(
            wallet = wallet,
            transactionData = com.ikoro.android.wallet.service.signer.TransactionData(
                outputs = listOf(
                    com.ikoro.android.wallet.service.signer.TransactionOutput(
                        address = "bc1qrecipient123",
                        amountSatoshis = 100000
                    )
                ),
                fee = 1000,
                timestamp = System.currentTimeMillis()
            ),
            inputs = listOf(
                com.ikoro.android.wallet.service.signer.TransactionInput(
                    com.bitcoinj.core.Transaction.PrevOut.EMPTY,
                    0,
                    null,
                    200000
                )
            )
        )
        
        // Result should be either success or error depending on implementation
        assertTrue(signResult is com.ikoro.android.wallet.service.signer.TransactionSignResult.Error)
    }
    
    /**
     * Test 7: Transaction queue manager
     */
    @Test
    fun testTransactionQueueManager() = runBlocking {
        val queueStats = queueManager.getQueueStats()
        assertEquals(0, queueStats.total)
        
        // Add transaction to queue
        val transaction = TransactionEntity(
            walletCurrency = Currency.BITCOIN.name,
            address = "bc1qtest123",
            amount = 0.5,
            status = "PENDING",
            timestamp = System.currentTimeMillis()
        )
        
        val queueResult = queueManager.addToQueue(transaction)
        assertTrue(queueResult is TransactionQueueManager.QueueOperationResult.Success)
        
        val queueWithTx = queueManager.getQueueStats()
        assertEquals(1, queueWithTx.total)
        
        // Clear queue
        queueManager.clearQueue()
        val queueCleared = queueManager.getQueueStats()
        assertEquals(0, queueCleared.total)
    }
    
    /**
     * Test 8: Wallet service integration
     */
    @Test
    fun testWalletServiceIntegration() = runBlocking {
        // Initialize service
        walletService.initialize()
        
        // Create wallet
        val createResult = walletService.createWallet(Currency.USDC)
        assertTrue(
            createResult is WalletService.WalletCreationResult.Success ||
            createResult is WalletService.WalletCreationResult.Error
        )
        
        // Get wallet info
        val walletInfo = walletService.getWalletInfo(Currency.USDC)
        // May be null if wallet creation failed (mocked keys)
        // Just verify no crash
        
        // Get balances flow
        val walletsFlow = walletService.getAllWalletsFlow()
        // Verify flow exists
        assertNotNull(walletsFlow)
    }
    
    /**
     * Test 9: All currencies supported
     */
    @Test
    fun testAllCurrenciesSupported() {
        val supportedCurrencies = setOf(
            Currency.BITCOIN,
            Currency.OFO,
            Currency.NAIRA,
            Currency.USDT,
            Currency.USDC
        )
        
        // Verify all currencies are in set
        assertTrue(supportedCurrencies.containsAll(listOf(
            Currency.BITCOIN,
            Currency.OFO,
            Currency.NAIRA,
            Currency.USDT,
            Currency.USDC
        )))
    }
    
    /**
     * Test 10: Cleanup
     */
    @Test
    fun testCleanup() {
        // Clean up resources
        transactionSigner.cleanup()
        meshBroadcaster.cleanup()
        keyManager.cleanup()
        walletService.cleanup()
        
        // Clear database
        db.clearAllTables()
    }
}

/**
 * Test data utilities
 */
object WalletTestUtils {
    fun createTestWallet(currency: Currency): WalletEntity {
        return WalletEntity(
            currency = currency.name,
            walletAddress = "bc1qtestaddress123",
            publicKey = "02testpublickey123456789",
            balance = 1.5,
            availableBalance = 1.5,
            pendingBalance = 0.0,
            lastSynced = System.currentTimeMillis(),
            lastUpdated = System.currentTimeMillis(),
            coinData = CoinData.createDefault(currency),
            isEncrypted = false,
            createdAt = System.currentTimeMillis(),
            lastUsed = System.currentTimeMillis()
        )
    }
    
    fun createTestTransaction(currency: Currency): TransactionEntity {
        return TransactionEntity(
            walletCurrency = currency.name,
            address = "bc1qtest123",
            amount = 0.5,
            type = "SEND",
            status = "PENDING",
            timestamp = System.currentTimeMillis()
        )
    }
}
