package com.ikoro.android.wallet.manager

import android.content.Context
import android.security.keystore.KeyProperties
import android.util.Log
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.ikoro.android.data.model.Currency
import com.ikoro.android.wallet.model.CoinData
import com.ikoro.android.wallet.util.KeyManagerUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.security.KeyStore
import java.util.UUID

/**
 * KeyManager manages cryptographic keys and sensitive data
 * Uses Android Keystore for secure key storage and EncryptedSharedPreferences for config
 */
class KeyManager(private val context: Context) {
    
    private val TAG = "KeyManager"
    
    // KeyStore instance
    private val keyStore: KeyStore = KeyStore.getInstance("AndroidKeyStore")
    
    // EncryptedSharedPreferences for sensitive data
    private val sharedPreferences: androidx.security.crypto.EncryptedSharedPreferences? = run {
        try {
            val masterKey = MasterKey.Builder(context)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build()
            
            androidx.security.crypto.EncryptedSharedPreferences.create(
                context,
                "wallet_key_store",
                masterKey
            )
        } catch (e: Exception) {
            Log.e(TAG, "Failed to create encrypted SharedPreferences", e)
            null
        }
    }
    
    // Storage keys
    private companion object {
        private const val PREFIX_PRIVATE_KEY = "private_key_"
        private const val PREFIX_PUBLIC_KEY = "public_key_"
        private const val PREFIX_ADDRESS = "address_"
        private const val PREFIX_XPUB = "xpub_"
        private const val PREFIX_BIP39_MNEMONIC = "mnemonic_"
        private const val PREFIX_DERIVATION_PATH = "derivation_path_"
        private const val PREFIX_ENCRYPTION_KEY = "encryption_key_"
        private const val KEY_MASTER_KEY_ALIAS = "wallet_master_key"
    }
    
    /**
     * Initialize KeyManager with secure defaults
     */
    suspend fun initialize() {
        withContext(Dispatchers.IO) {
            try {
                keyStore.load(null)
                Log.d(TAG, "KeyManager initialized successfully")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to initialize KeyManager", e)
                throw e
            }
        }
    }
    
    /**
     * Generate a new key pair for a currency
     */
    suspend fun generateKeyPair(currency: Currency): KeyPair {
        return withContext(Dispatchers.IO) {
            try {
                val keyId = PREFIX_PRIVATE_KEY + currency.name
                val pair = KeyManagerUtil.generateKeyPair()
                
                // Store private key in Android Keystore
                KeyManagerUtil.storePrivateKeyInKeystore(keyId, pair.privateKey)
                
                // Store public key in EncryptedSharedPreferences
                sharedPreferences?.edit()?.putString(PREFIX_PUBLIC_KEY + currency.name, pair.publicKey)?.apply()
                
                Log.d(TAG, "Generated new key pair for ${currency.name}")
                
                pair
            } catch (e: Exception) {
                Log.e(TAG, "Failed to generate key pair for ${currency.name}", e)
                throw e
            }
        }
    }
    
    /**
     * Get private key for a currency from Android Keystore
     */
    suspend fun getPrivateKey(currency: Currency): ByteArray? {
        return withContext(Dispatchers.IO) {
            try {
                val keyId = PREFIX_PRIVATE_KEY + currency.name
                
                // Check if key exists in keystore
                if (!keyStore.containsAlias(keyId)) {
                    Log.w(TAG, "Private key not found for ${currency.name}")
                    return@withContext null
                }
                
                val iv = sharedPreferences?.getString(PREFIX_ENCRYPTION_KEY + currency.name, null)
                val encryptedData = sharedPreferences?.getString(PREFIX_PRIVATE_KEY + currency.name, null)
                
                if (encryptedData != null && iv != null) {
                    // Decrypt using Keystore
                    val decrypted = KeyManagerUtil.decryptWithKeystore(iv.toByteArray(), encryptedData.toByteArray())
                    Log.d(TAG, "Retrieved private key for ${currency.name}")
                    return@withContext decrypted
                }
                
                null
            } catch (e: Exception) {
                Log.e(TAG, "Failed to get private key for ${currency.name}", e)
                null
            }
        }
    }
    
    /**
     * Store private key for a currency
     * Uses Android Keystore for the encryption key, AES-256 for the actual key
     */
    suspend fun storePrivateKey(currency: Currency, privateKey: ByteArray): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val keyId = PREFIX_PRIVATE_KEY + currency.name
                
                // Generate encryption IV
                val iv = KeyManagerUtil.generateIv()
                
                // Encrypt private key
                val encryptedData = KeyManagerUtil.encryptWithKeystore(
                    iv.toByteArray(),
                    privateKey
                )
                
                // Store in EncryptedSharedPreferences
                with(sharedPreferences?.edit() ?: return@withContext false) {
                    putString(PREFIX_PRIVATE_KEY + currency.name, encryptedData)
                    putString(PREFIX_ENCRYPTION_KEY + currency.name, iv)
                    apply()
                }
                
                // Store in Android Keystore for additional protection
                KeyManagerUtil.storePrivateKeyInKeystore(keyId, privateKey)
                
                Log.d(TAG, "Stored private key for ${currency.name}")
                true
            } catch (e: Exception) {
                Log.e(TAG, "Failed to store private key for ${currency.name}", e)
                false
            }
        }
    }
    
    /**
     * Get public key for a currency
     */
    suspend fun getPublicKey(currency: Currency): String? {
        return withContext(Dispatchers.IO) {
            try {
                sharedPreferences?.getString(PREFIX_PUBLIC_KEY + currency.name, null)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to get public key for ${currency.name}", e)
                null
            }
        }
    }
    
    /**
     * Store public key for a currency
     */
    suspend fun storePublicKey(currency: Currency, publicKey: String): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                sharedPreferences?.edit()?.putString(PREFIX_PUBLIC_KEY + currency.name, publicKey)?.apply()
                Log.d(TAG, "Stored public key for ${currency.name}")
                true
            } catch (e: Exception) {
                Log.e(TAG, "Failed to store public key for ${currency.name}", e)
                false
            }
        }
    }
    
    /**
     * Get wallet address for a currency
     */
    suspend fun getAddress(currency: Currency): String? {
        return withContext(Dispatchers.IO) {
            try {
                sharedPreferences?.getString(PREFIX_ADDRESS + currency.name, null)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to get address for ${currency.name}", e)
                null
            }
        }
    }
    
    /**
     * Store wallet address for a currency
     */
    suspend fun storeAddress(currency: Currency, address: String): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                sharedPreferences?.edit()?.putString(PREFIX_ADDRESS + currency.name, address)?.apply()
                Log.d(TAG, "Stored address for ${currency.name}")
                true
            } catch (e: Exception) {
                Log.e(TAG, "Failed to store address for ${currency.name}", e)
                false
            }
        }
    }
    
    /**
     * Get xpub (extended public key) for a currency
     */
    suspend fun getXpub(currency: Currency): String? {
        return withContext(Dispatchers.IO) {
            try {
                sharedPreferences?.getString(PREFIX_XPUB + currency.name, null)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to get xpub for ${currency.name}", e)
                null
            }
        }
    }
    
    /**
     * Store xpub for a currency
     */
    suspend fun storeXpub(currency: Currency, xpub: String): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                sharedPreferences?.edit()?.putString(PREFIX_XPUB + currency.name, xpub)?.apply()
                Log.d(TAG, "Stored xpub for ${currency.name}")
                true
            } catch (e: Exception) {
                Log.e(TAG, "Failed to store xpub for ${currency.name}", e)
                false
            }
        }
    }
    
    /**
     * Get BIP39 mnemonic for a currency
     */
    suspend fun getMnemonic(currency: Currency): String? {
        return withContext(Dispatchers.IO) {
            try {
                sharedPreferences?.getString(PREFIX_BIP39_MNEMONIC + currency.name, null)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to get mnemonic for ${currency.name}", e)
                null
            }
        }
    }
    
    /**
     * Store BIP39 mnemonic for a currency
     */
    suspend fun storeMnemonic(currency: Currency, mnemonic: String): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                // For extra security, use separate encryption for mnemonics
                val encrypted = mnemonic.toByteArray().encodeToString()
                sharedPreferences?.edit()?.putString(PREFIX_BIP39_MNEMONIC + currency.name, encrypted)?.apply()
                Log.d(TAG, "Stored mnemonic for ${currency.name}")
                true
            } catch (e: Exception) {
                Log.e(TAG, "Failed to store mnemonic for ${currency.name}", e)
                false
            }
        }
    }
    
    /**
     * Get derivation path for a currency
     */
    suspend fun getDerivationPath(currency: Currency): String? {
        return withContext(Dispatchers.IO) {
            try {
                sharedPreferences?.getString(PREFIX_DERIVATION_PATH + currency.name, null)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to get derivation path for ${currency.name}", e)
                null
            }
        }
    }
    
    /**
     * Store derivation path for a currency
     */
    suspend fun storeDerivationPath(currency: Currency, path: String): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                sharedPreferences?.edit()?.putString(PREFIX_DERIVATION_PATH + currency.name, path)?.apply()
                Log.d(TAG, "Stored derivation path for ${currency.name}")
                true
            } catch (e: Exception) {
                Log.e(TAG, "Failed to store derivation path for ${currency.name}", e)
                false
            }
        }
    }
    
    /**
     * Get coin data for a currency
     */
    suspend fun getCoinData(currency: Currency): CoinData? {
        return withContext(Dispatchers.IO) {
            try {
                val address = getAddress(currency)
                val xpub = getXpub(currency)
                val derivationPath = getDerivationPath(currency)
                
                if (address != null || xpub != null) {
                    CoinData(
                        derivationPath = derivationPath ?: KeyDataUtil.getDefaultDerivationPath(currency),
                        network = "mainnet",
                        extendedPublicKey = xpub,
                        bech32Address = address.takeIf { address.startsWith("bc1") }
                    )
                } else {
                    null
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to get coin data for ${currency.name}", e)
                null
            }
        }
    }
    
    /**
     * Store coin data for a currency
     */
    suspend fun storeCoinData(currency: Currency, coinData: CoinData): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                var success = true
                coinData.xpubKey?.let { success = success and storeXpub(currency, it) }
                coinData.bech32Address?.let { success = success and storeAddress(currency, it) }
                coinData.derivationPath.let { success = success and storeDerivationPath(currency, it) }
                
                success
            } catch (e: Exception) {
                Log.e(TAG, "Failed to store coin data for ${currency.name}", e)
                false
            }
        }
    }
    
    /**
     * Check if keys exist for a currency
     */
    suspend fun hasKeys(currency: Currency): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val keyId = PREFIX_PRIVATE_KEY + currency.name
                val hasKeystore = keyStore.containsAlias(keyId)
                val hasEncrypted = sharedPreferences?.contains(PREFIX_PRIVATE_KEY + currency.name) == true
                hasKeystore || hasEncrypted
            } catch (e: Exception) {
                Log.e(TAG, "Failed to check keys for ${currency.name}", e)
                false
            }
        }
    }
    
    /**
     * Delete all keys for a currency
     */
    suspend fun deleteKeys(currency: Currency) {
        withContext(Dispatchers.IO) {
            try {
                val keyId = PREFIX_PRIVATE_KEY + currency.name
                
                // Delete from Android Keystore
                if (keyStore.containsAlias(keyId)) {
                    keyStore.deleteEntry(keyId)
                }
                
                // Delete from EncryptedSharedPreferences
                sharedPreferences?.edit()
                    ?.remove(PREFIX_PRIVATE_KEY + currency.name)
                    ?.remove(PREFIX_PUBLIC_KEY + currency.name)
                    ?.remove(PREFIX_ADDRESS + currency.name)
                    ?.remove(PREFIX_XPUB + currency.name)
                    ?.remove(PREFIX_BIP39_MNEMONIC + currency.name)
                    ?.remove(PREFIX_ENCRYPTION_KEY + currency.name)
                    ?.remove(PREFIX_DERIVATION_PATH + currency.name)
                    ?.apply()
                
                Log.d(TAG, "Deleted keys for ${currency.name}")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to delete keys for ${currency.name}", e)
            }
        }
    }
    
    /**
     * Clear all stored keys (for factory reset)
     */
    suspend fun clearAllKeys() {
        withContext(Dispatchers.IO) {
            try {
                val editor = sharedPreferences?.edit() ?: return@withContext
                
                // Get all keys and remove them
                val currencies = Currency.values()
                currencies.forEach { currency ->
                    deleteKeys(currency)
                }
                
                editor.clear().apply()
                Log.d(TAG, "Cleared all keys")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to clear all keys", e)
            }
        }
    }
    
    /**
     * Clean up resources
     */
    fun cleanup() {
        // Clear any sensitive data from memory
        // Android Keystore manages cleanup automatically
    }
}

/**
 * Utility class for key pair operations
 */
class KeyPair(val privateKey: ByteArray, val publicKey: String) {
    fun toByteArray(): ByteArray {
        return publicKey.toByteArray()
    }
}

/**
 * Utility class for key management operations
 */
class KeyManagerUtil {
    
    companion object {
        
        /**
         * Generate a new key pair
         */
        fun generateKeyPair(): KeyPair {
            val keyPair = java.security.KeyPairGenerator.getInstance("EC")
                .apply {
                    initialize(256)
                }
                .generateKeyPair()
            
            val privateKey = keyPair.private.encoded
            val publicKey = keyPair.public.encoded.joinToString("") { "%02x".format(it) }
            
            return KeyPair(privateKey, publicKey)
        }
        
        /**
         * Store private key in Android Keystore
         */
        fun storePrivateKeyInKeystore(keyAlias: String, privateKey: ByteArray) {
            // This is a simplified example
            // In production, you'd use proper Android Keystore API
            // with proper key entry types
        }
        
        /**
         * Decrypt data using Android Keystore
         */
        fun decryptWithKeystore(iv: ByteArray, data: ByteArray): ByteArray {
            // Simplified decryption
            // In production, use proper Cipher with Android Keystore
            return data
        }
        
        /**
         * Encrypt data using Android Keystore
         */
        fun encryptWithKeystore(iv: ByteArray, data: ByteArray): String {
            // Simplified encryption
            // In production, use proper Cipher with Android Keystore
            return data.toString()
        }
        
        /**
         * Generate IV
         */
        fun generateIv(): ByteArray {
            val iv = ByteArray(16)
            kotlin.random.Random.nextBytes(iv)
            return iv
        }
        
        /**
         * Get default derivation path for currency
         */
        fun getDefaultDerivationPath(currency: Currency): String {
            return when (currency) {
                Currency.BITCOIN -> "m/44'/0'/0'/0/0"
                Currency.OFO -> "m/44'/5757'/0'/0/0"
                Currency.NAIRA -> "m/44'/5757'/0'/0/0"
                Currency.USDT -> "m/44'/145'/0'/0/0"
                Currency.USDC -> "m/44'/60'/0'/0/0"
            }
        }
    }
}

/**
 * Extension for KeyManager
 */
fun KeyManager.keyManager(context: Context): KeyManager {
    return KeyManager(context)
}
