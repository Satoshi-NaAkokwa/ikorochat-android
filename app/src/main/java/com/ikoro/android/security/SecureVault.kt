package com.ikoro.android.security

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import com.ikoro.android.identity.NostrCrypto
import java.nio.charset.StandardCharsets
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

/**
 * Secure storage for mnemonic and app lock.
 */
object SecureVault {
    private const val ANDROID_KEYSTORE = "AndroidKeyStore"
    private const val KEY_ALIAS = "ikoro_vault_key"
    private const val PREFS = "ikoro_secure"
    private const val SEED_KEY = "seed_encrypted"
    private const val MNEMONIC_KEY = "mnemonic_encrypted"
    private const val PIN_HASH = "pin_hash"
    private const val USE_BIOMETRIC = "use_biometric"

    private fun generateOrGetKey(): SecretKey {
        val keystore = KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }
        keystore.getKey(KEY_ALIAS, null)?.let { return it as SecretKey }

        val keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEYSTORE)
        keyGenerator.init(
            KeyGenParameterSpec.Builder(
                KEY_ALIAS,
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
            )
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setUserAuthenticationRequired(false)
                .setRandomizedEncryptionRequired(true)
                .build()
        )
        return keyGenerator.generateKey()
    }

    private fun encrypt(plaintext: String): ByteArray {
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, generateOrGetKey())
        return cipher.iv + cipher.doFinal(plaintext.toByteArray(StandardCharsets.UTF_8))
    }

    private fun decrypt(ciphertext: ByteArray): String {
        val iv = ciphertext.copyOfRange(0, 12)
        val encrypted = ciphertext.copyOfRange(12, ciphertext.size)
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.DECRYPT_MODE, generateOrGetKey(), GCMParameterSpec(128, iv))
        return String(cipher.doFinal(encrypted), StandardCharsets.UTF_8)
    }

    fun hasSeed(context: Context): Boolean =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).contains(SEED_KEY)

    fun storeSeed(context: Context, seed: ByteArray) {
        val encrypted = encrypt(Base64.encodeToString(seed, Base64.NO_WRAP))
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .putString(SEED_KEY, Base64.encodeToString(encrypted, Base64.NO_WRAP))
            .apply()
    }

    fun retrieveSeed(context: Context): ByteArray? {
        val base64 = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getString(SEED_KEY, null) ?: return null
        return try {
            val encrypted = Base64.decode(base64, Base64.NO_WRAP)
            Base64.decode(decrypt(encrypted), Base64.NO_WRAP)
        } catch (_: Exception) { null }
    }

    fun storeMnemonic(context: Context, mnemonic: String) {
        val encrypted = encrypt(mnemonic)
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .putString(MNEMONIC_KEY, Base64.encodeToString(encrypted, Base64.NO_WRAP))
            .apply()
    }

    fun retrieveMnemonic(context: Context): String? {
        val base64 = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getString(MNEMONIC_KEY, null) ?: return null
        return try {
            val encrypted = Base64.decode(base64, Base64.NO_WRAP)
            decrypt(encrypted)
        } catch (_: Exception) { null }
    }

    fun clearAll(context: Context) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().clear().apply()
    }

    fun saveMnemonic(context: Context, mnemonic: String) {
        storeMnemonic(context, mnemonic)
    }

    fun getMnemonic(context: Context): String? = retrieveMnemonic(context)

    fun setPin(context: Context, pin: String) {
        val hash = NostrCrypto.sha256(pin.toByteArray(StandardCharsets.UTF_8))
            .joinToString("") { "%02x".format(it) }
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().putString(PIN_HASH, hash).apply()
    }

    fun checkPin(context: Context, pin: String): Boolean {
        val hash = NostrCrypto.sha256(pin.toByteArray(StandardCharsets.UTF_8))
            .joinToString("") { "%02x".format(it) }
        val stored = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getString(PIN_HASH, null)
        return stored == hash
    }

    fun hasPin(context: Context): Boolean =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).contains(PIN_HASH)

    fun setUseBiometric(context: Context, enabled: Boolean) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().putBoolean(USE_BIOMETRIC, enabled).apply()
    }

    fun useBiometric(context: Context): Boolean =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getBoolean(USE_BIOMETRIC, false)

    fun canUseBiometric(context: Context): Boolean {
        val manager = BiometricManager.from(context)
        return manager.canAuthenticate(
            BiometricManager.Authenticators.BIOMETRIC_WEAK or BiometricManager.Authenticators.DEVICE_CREDENTIAL
        ) == BiometricManager.BIOMETRIC_SUCCESS
    }

    fun showBiometricPrompt(
        activity: FragmentActivity,
        onSuccess: () -> Unit,
        onError: (String) -> Unit = {}
    ) {
        val executor = ContextCompat.getMainExecutor(activity)
        val prompt = BiometricPrompt(
            activity,
            executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    onSuccess()
                }

                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    onError(errString.toString())
                }
            }
        )
        val info = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Unlock Ikoro")
            .setSubtitle("Verify your identity")
            .setAllowedAuthenticators(
                BiometricManager.Authenticators.BIOMETRIC_WEAK or BiometricManager.Authenticators.DEVICE_CREDENTIAL
            )
            .build()
        prompt.authenticate(info)
    }
}
