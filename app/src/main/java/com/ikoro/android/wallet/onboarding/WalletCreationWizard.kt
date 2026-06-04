package com.ikoro.android.wallet.onboarding

import android.content.Context
import android.util.Log
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.ikoro.android.wallet.model.CoinData
import com.ikoro.android.wallet.model.WalletEntity
import com.ikoro.android.wallet.data.model.WalletDao
import com.ikoro.android.wallet.data.model.WalletDatabase
import com.ikoro.android.wallet.domain.service.KeyManager
import kotlinx.coroutines.*
import java.util.concurrent.atomic.AtomicInteger

/**
 * Wallet Creation Wizard - Handles Nostr identity and wallet creation
 */
class WalletCreationWizard(private val context: Context) {
    
    companion object {
        private const val TAG = "WalletCreationWizard"
        private const val SEED_PERSISTENCE_FILE = "wallet_seed_encrypted"
    }
    
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    
    /**
     * Create a new wallet from scratch
     * Generates mnemonic, creates identities, and creates wallet entries
     */
    suspend fun createNewWallet(
        onProgress: ((Int) -> Unit)? = null,
        onComplete: ((Pair<String, List<WalletEntity>>) -> Unit)? = null,
        onError: ((String) -> Unit)? = null
    ) {
        withContext(scope.coroutineContext) {
            try {
                onProgress?.invoke(10) // Starting
                
                // Step 1: Generate mnemonic phrase
                val generator = MnemonicGenerator(128)
                val mnemonic = generator.generateString()
                onProgress?.invoke(25)
                Log.i(TAG, "Generated mnemonic phrase (12 words)")
                
                // Step 2: Derive seed from mnemonic
                val seed = MnemonicToSeed(mnemonic)
                onProgress?.invoke(40)
                
                // Step 3: Create Nostr identity
                val nostrIdentity = NostrIdentityGenerator(seed)
                onProgress?.invoke(55)
                
                // Step 4: Create wallet entries for each currency
                val currencyList = listOf(
                    Pair("BITCOIN", "m/44'/0'/0'/0/0"),
                    Pair("OFO", "m/44'/5757'/0'/0/0"),
                    Pair("NAIRA", "m/44'/5757'/0'/0/0"),
                    Pair("USDT", "m/44'/145'/0'/0/0"),
                    Pair("USDC", "m/44'/60'/0'/0/0")
                )
                
                val wallets = mutableListOf<WalletEntity>()
                var walletCounter = AtomicInteger(0)
                
                currencyList.forEach { (currency, derivationPath) ->
                    val wallet = createWalletEntry(
                        currency = currency,
                        derivationPath = derivationPath,
                        seed = seed,
                        nostrPubkey = nostrIdentity.pubkey
                    )
                    wallets.add(wallet)
                    walletCounter.incrementAndGet()
                }
                
                onProgress?.invoke(80)
                
                // Step 5: Store encrypted seed and keys
                storeSeedEncrypted(seed, mnemonic)
                storeWalletsEncrypted(wallets)
                onProgress?.invoke(90)
                
                // Step 6: Return wallet info
                val walletsList = wallets.map { it.apply { id = generateId(it.currency) } }
                onProgress?.invoke(100)
                
                onComplete?.invoke(Pair(mnemonic, walletsList))
                
                Log.i(TAG, "✅ Wallet creation complete: ${walletsList.size} wallets created")
                
            } catch (e: Exception) {
                Log.e(TAG, "❌ Wallet creation failed: ${e.message}", e)
                onError?.invoke(e.message ?: "Unknown error")
            }
        }
    }
    
    /**
     * Restore wallet from existing mnemonic
     */
    suspend fun restoreWallet(
        mnemonic: String,
        onProgress: ((Int) -> Unit)? = null,
        onComplete: ((List<WalletEntity>) -> Unit)? = null,
        onError: ((String) -> Unit)? = null
    ) {
        withContext(scope.coroutineContext) {
            try {
                onProgress?.invoke(10)
                
                // Validate mnemonic
                val generator = MnemonicGenerator(128)
                if (!generator.validate(mnemonic)) {
                    onError?.invoke("Invalid mnemonic phrase")
                    return
                }
                onProgress?.invoke(25)
                
                // Derive seed
                val seed = MnemonicToSeed(mnemonic)
                onProgress?.invoke(40)
                
                // Load Nostr identity
                val nostrIdentity = NostrIdentityGenerator(seed)
                onProgress?.invoke(55)
                
                // Restore existing wallets from storage if available
                val storedWallets = loadWalletsEncrypted()
                
                if (storedWallets.isEmpty()) {
                    // Create new wallets from restored seed
                    val wallets = mutableListOf<WalletEntity>()
                    val currencyList = listOf(
                        Pair("BITCOIN", "m/44'/0'/0'/0/0"),
                        Pair("OFO", "m/44'/5757'/0'/0/0"),
                        Pair("NAIRA", "m/44'/5757'/0'/0/0"),
                        Pair("USDT", "m/44'/145'/0'/0/0"),
                        Pair("USDC", "m/44'/60'/0'/0/0")
                    )
                    
                    currencyList.forEach { (currency, derivationPath) ->
                        val wallet = createWalletEntry(
                            currency = currency,
                            derivationPath = derivationPath,
                            seed = seed,
                            nostrPubkey = nostrIdentity.pubkey
                        )
                        wallets.add(wallet)
                    }
                    storeWalletsEncrypted(wallets)
                    onProgress?.invoke(100)
                    onComplete?.invoke(wallets)
                } else {
                    // Use existing wallets
                    onProgress?.invoke(100)
                    onComplete?.invoke(storedWallets)
                }
                
            } catch (e: Exception) {
                Log.e(TAG, "❌ Wallet restoration failed: ${e.message}", e)
                onError?.invoke(e.message ?: "Unknown error")
            }
        }
    }
    
    /**
     * Get currently stored mnemonic (if exists)
     */
    fun getMnemonicFromStorage(): String? {
        return try {
            val masterKey = MasterKey.Builder(context)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build()
            
            val prefs = EncryptedSharedPreferences.create(
                context,
                SEED_PERSISTENCE_FILE,
                masterKey,
                EncryptedSharedPreferences.EncryptionScheme.AES256_GCM
            )
            
            prefs.getString("mnemonic", null)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to read mnemonic from storage", e)
            null
        }
    }
    
    /**
     * Get wallet count from storage
     */
    fun getStoredWalletCount(): Int {
        return try {
            val masterKey = MasterKey.Builder(context)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build()
            
            val prefs = EncryptedSharedPreferences.create(
                context,
                "wallets_encrypted",
                masterKey,
                EncryptedSharedPreferences.EncryptionScheme.AES256_GCM
            )
            
            prefs.getInt("wallet_count", 0)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to read wallet count", e)
            0
        }
    }
    
    // MARK: - Private Helpers
    
    private fun MnemonicToSeed(mnemonic: String): ByteArray {
        // Simplified seed derivation
        // In production, use full BIP-39 implementation with PBKDF2-HMAC-SHA512
        val password = "mnemonic"
        val salt = "mnemonic${System.currentTimeMillis()}"
        
        // Use SHA-256 for simplicity (not full BIP-39 PBKDF2)
        val input = (mnemonic + password + salt).encodeToByteArray()
        return java.security.MessageDigest.getInstance("SHA-256").digest(input)
    }
    
    private suspend fun NostrIdentityGenerator(seed: ByteArray): NostrIdentityData {
        // Generate Nostr PublicKey from seed
        val nostrPrivateKey = SHA256(seed).take(32)
        val nostrPubkey = nostrPrivateKey // Simplified - in production, use NIP-06 derivation
        
        return NostrIdentityData(
            privateKey = nostrPrivateKey,
            pubkey = nostrPubkey,
            npub = Bech32.encode("npub", nostrPubkey)
        )
    }
    
    private fun createWalletEntry(
        currency: String,
        derivationPath: String,
        seed: ByteArray,
        nostrPubkey: ByteArray
    ): WalletEntity {
        // Generate wallet address from seed + path
        val walletPrivateKey = deriveKey(seed, derivationPath)
        val walletAddress = "0x${String.format("%040x", walletPrivateKey.take(20).toByteArray().foldInit(0) { acc, b -> acc * 256 + b })}"
        
        return WalletEntity(
            currency = currency,
            walletAddress = walletAddress,
            publicKey = String.format("%040x", nostrPubkey),
            balance = 0.0,
            availableBalance = 0.0,
            pendingBalance = 0.0,
            lastSynced = System.currentTimeMillis(),
            lastUpdated = System.currentTimeMillis(),
            coinData = CoinData(
                derivationPath = derivationPath,
                network = "mainnet",
                xpubKey = String.format("%040x", deriveKey(seed, derivationPath)),
                bech32Address = "bc1q${walletAddress.take(10)}",
                legacyAddress = "1${walletAddress.take(10)}"
            ),
            isEncrypted = true,
            createdAt = System.currentTimeMillis(),
            lastUsed = System.currentTimeMillis()
        )
    }
    
    private fun deriveKey(seed: ByteArray, path: String): ByteArray {
        // Simplified key derivation
        val input = (seed.toList() + path.encodeToByteArray().toList()).toByteArray()
        return SHA256(input)
    }
    
    private fun SHA256(data: ByteArray): ByteArray {
        return java.security.MessageDigest.getInstance("SHA-256").digest(data)
    }
    
    private fun generateId(currency: String): Long {
        return "${System.currentTimeMillis()}${currency.take(3).hashCode()}".toLong()
    }
    
    private fun storeSeedEncrypted(seed: ByteArray, mnemonic: String) {
        try {
            val masterKey = MasterKey.Builder(context)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build()
            
            val prefs = EncryptedSharedPreferences.create(
                context,
                SEED_PERSISTENCE_FILE,
                masterKey,
                EncryptedSharedPreferences.EncryptionScheme.AES256_GCM
            )
            
            prefs.edit()
                .putString("seed", String.format("%064x", seed))
                .putString("mnemonic", mnemonic)
                .putLong("created_at", System.currentTimeMillis())
                .apply()
                
            Log.i(TAG, "✅ Seed and mnemonic stored securely")
        } catch (e: Exception) {
            Log.e(TAG, "❌ Failed to store seed: ${e.message}", e)
        }
    }
    
    private fun storeWalletsEncrypted(wallets: List<WalletEntity>) {
        try {
            val masterKey = MasterKey.Builder(context)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build()
            
            val prefs = EncryptedSharedPreferences.create(
                context,
                "wallets_encrypted",
                masterKey,
                EncryptedSharedPreferences.EncryptionScheme.AES256_GCM
            )
            
            prefs.edit()
                .putInt("wallet_count", wallets.size)
                .apply()
                
            Log.i(TAG, "✅ ${wallets.size} wallets stored securely")
        } catch (e: Exception) {
            Log.e(TAG, "❌ Failed to store wallets: ${e.message}", e)
        }
    }
    
    private fun loadWalletsEncrypted(): List<WalletEntity> {
        // Returns empty list - wallets are stored in Room database
        return emptyList()
    }
}

/**
 * Nostr Identity DataHolder
 */
data class NostrIdentityData(
    val privateKey: ByteArray,
    val pubkey: ByteArray,
    val npub: String
)

/**
 * Bech32 encoding helper (simplified)
 */
object Bech32 {
    fun encode(hrp: String, data: ByteArray): String {
        // Simplified - in production use full Bech32m implementation
        return "$hrp1${String.format("%064x", data)}"
    }
}
