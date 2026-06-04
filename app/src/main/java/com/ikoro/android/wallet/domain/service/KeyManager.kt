package com.ikoro.android.wallet.domain.service

import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec
import java.security.SecureRandom
import kotlin.math.min


/**
 * Key Manager - manages wallet cryptographic keys
 */
class KeyManager(
    private val context: Context
) {
    companion object {
        private const val AES_ALGORITHM = "AES"
        private const val TRANSFORMATION = "AES/GCM/NoPadding"
        private const val KEY_SIZE = 256
        private const val IV_LENGTH = 12
        private const val TAG_LENGTH = 128
        private const val ENCRYPTED_PRIVATE_KEY = "encrypted_private_key"
        private const val ENCRYPTED_SEED_PHRASE = "encrypted_seed_phrase"
    }

    private val preferences = context.getSharedPreferences("wallet_keys", Context.MODE_PRIVATE)
    private val secureRandom = SecureRandom()

    // Generate wallet key pair
    fun generateWalletKeyPair(): WalletKeyPair {
        val secp256k1 = ECKeyPairGenerator()
        val keyPair = secp256k1.generateKeyPair()
        return WalletKeyPair(
            publicKey = Base64.encodeToString(keyPair.publicKey.encoded),
            privateKey = Base64.encodeToString(keyPair.privateKey.encoded)
        )
    }

    // Encrypt private key with PIN
    fun encryptPrivateKey(privateKey: String, pin: String): String {
        val key = deriveKeyFromPin(pin)
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, key, GCMParameterSpec(TAG_LENGTH, generateIv()))
        val encrypted = cipher.doFinal(privateKey.toByteArray())
        return Base64.encodeToString(cipher.iv) + ":" + Base64.encodeToString(encrypted)
    }

    // Decrypt private key with PIN
    fun decryptPrivateKey(encrypted: String, pin: String): String? {
        val parts = encrypted.split(":")
        if (parts.size != 2) return null

        val iv = Base64.decode(parts[0])
        val encryptedData = Base64.decode(parts[1])
        val key = deriveKeyFromPin(pin)

        try {
            val cipher = Cipher.getInstance(TRANSFORMATION)
            cipher.init(Cipher.DECRYPT_MODE, key, GCMParameterSpec(TAG_LENGTH, iv))
            val decrypted = cipher.doFinal(encryptedData)
            return String(decrypted)
        } catch (e: Exception) {
            return null
        }
    }

    // Generate deterministic key from PIN
    private fun deriveKeyFromPin(pin: String): SecretKey {
        val digest = MessageDigest.getInstance("SHA-256")
        val pinBytes = pin.toByteArray()
        val hash = digest.digest(pinBytes)

        val keyBytes = hash.copyOfRange(0, KEY_SIZE / 8)
        return SecretKeySpec(keyBytes, AES_ALGORITHM)
    }

    // Generate IV
    private fun generateIv(): ByteArray {
        val iv = ByteArray(IV_LENGTH)
        secureRandom.nextBytes(iv)
        return iv
    }

    // Store encrypted private key in SharedPreferences
    fun storeEncryptedPrivateKey(encryptedKey: String) {
        preferences.edit().putString(ENCRYPTED_PRIVATE_KEY, encryptedKey).apply()
    }

    // Retrieve encrypted private key
    fun getEncryptedPrivateKey(): String? {
        return preferences.getString(ENCRYPTED_PRIVATE_KEY, null)
    }

    // Clear all keys (for logout/delete wallet)
    fun clearAllKeys() {
        preferences.edit().clear().apply()
    }

    // Check if wallet is initialized
    fun isWalletInitialized(): Boolean {
        return getEncryptedPrivateKey() != null
    }
}

// Key pair model
data class WalletKeyPair(
    val publicKey: String,
    val privateKey: String
)

// EC key pair generator (placeholder - would use actual implementation)
class ECKeyPairGenerator {
    fun generateKeyPair(): KeyPair {
        // Replace with actual Ed25519 or secp256k1 implementation
        return KeyPair(
            object : java.security.PublicKey {
                override fun getAlgorithm() = "Ed25519"
                override fun getFormat() = "RAW"
                override fun getEncoded() = ByteArray(32)
            },
            object : java.security.PrivateKey {
                override fun getAlgorithm() = "Ed25519"
                override fun getFormat() = "RAW"
                override fun getEncoded() = ByteArray(64)
            }
        )
    }
}
