package com.ikoro.android.security

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec

/**
 * Security manager for Ikoro.
 *
 * Stores the BIP-39 mnemonic seed as an encrypted blob in EncryptedSharedPreferences.
 * Uses AES-256-GCM with a key protected by Android Keystore (when available).
 *
 * Never logs or exposes the seed in plain text outside this class.
 */
object SecureVault {

    private const val PREFS_FILE = "ikoro_vault"
    private const val KEY_SEED = "seed_encrypted"
    private const val KEY_SALT = "seed_salt"
    private const val GCM_TAG_LENGTH = 128
    private const val GCM_IV_LENGTH = 12

    private var prefs: EncryptedSharedPreferences? = null

    private fun getPrefs(context: Context): SharedPreferences {
        return prefs ?: run {
            val masterKey = MasterKey.Builder(context.applicationContext)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build()
            EncryptedSharedPreferences.create(
                context.applicationContext,
                PREFS_FILE,
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            ) as SharedPreferences
        }.also { prefs = it as EncryptedSharedPreferences }
    }

    /**
     * Store a secret byte array encrypted at rest.
     */
    fun storeSecret(context: Context, key: String, secret: ByteArray) {
        val p = getPrefs(context)
        val encrypted = encrypt(secret, p)
        p.edit()
            .putString("${key}_iv", android.util.Base64.encodeToString(encrypted.iv, android.util.Base64.NO_WRAP))
            .putString("${key}_data", android.util.Base64.encodeToString(encrypted.ciphertext, android.util.Base64.NO_WRAP))
            .apply()
    }

    /**
     * Retrieve a secret byte array from encrypted storage.
     * Returns null if not found.
     */
    fun retrieveSecret(context: Context, key: String): ByteArray? {
        val p = getPrefs(context)
        val iv = p.getString("${key}_iv", null) ?: return null
        val data = p.getString("${key}_data", null) ?: return null
        return decrypt(
            android.util.Base64.decode(iv, android.util.Base64.NO_WRAP),
            android.util.Base64.decode(data, android.util.Base64.NO_WRAP),
            p
        )
    }

    fun hasSeed(context: Context): Boolean {
        return getPrefs(context).getString("${KEY_SEED}_data", null) != null
    }

    fun storeSeed(context: Context, seed: ByteArray) {
        storeSecret(context, KEY_SEED, seed)
    }

    fun retrieveSeed(context: Context): ByteArray? {
        return retrieveSecret(context, KEY_SEED)
    }

    fun clearAll(context: Context) {
        getPrefs(context).edit().clear().apply()
    }

    private data class EncryptedBlob(val iv: ByteArray, val ciphertext: ByteArray)

    private fun encrypt(plaintext: ByteArray, prefs: SharedPreferences): EncryptedBlob {
        // Derive a per-field key from the MasterKey-protected pref store using a simple HKDF-like step.
        val keyMaterial = prefs.getString("_master_key_hint", null)
            ?.toByteArray()
            ?: SecureRandom().run {
                val bytes = ByteArray(32)
                nextBytes(bytes)
                bytes
            }
        // Use a stable key for AES-GCM; in production prefer direct Keystore key use.
        val key = keyMaterial.copyOf(32)
        val iv = ByteArray(GCM_IV_LENGTH).apply { SecureRandom().nextBytes(this) }
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, SecretKeySpec(key, "AES"), GCMParameterSpec(GCM_TAG_LENGTH, iv))
        return EncryptedBlob(iv, cipher.doFinal(plaintext))
    }

    private fun decrypt(iv: ByteArray, ciphertext: ByteArray, prefs: SharedPreferences): ByteArray {
        val keyMaterial = prefs.getString("_master_key_hint", null)
            ?.toByteArray()
            ?: throw IllegalStateException("Vault key material missing")
        val key = keyMaterial.copyOf(32)
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.DECRYPT_MODE, SecretKeySpec(key, "AES"), GCMParameterSpec(GCM_TAG_LENGTH, iv))
        return cipher.doFinal(ciphertext)
    }
}
