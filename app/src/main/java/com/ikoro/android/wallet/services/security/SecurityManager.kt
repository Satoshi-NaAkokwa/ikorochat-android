package com.ikoro.android.wallet.services.security

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import com.ikoro.android.wallet.domain.service.KeyManager
import javax.inject.Inject
import javax.inject.Singleton


/**
 * Security Manager - handles wallet security features
 */
@Singleton
class SecurityManager @Inject constructor(
    private val context: Context,
    private val keyManager: KeyManager
) {
    companion object {
        const val SECURITY_SETTINGS = "wallet_security_settings"
        const val THRESHOLD_SECURE = 0.1 // 0.1 BTC threshold
        const val FRAUD_DETECTION_WINDOWS = 3600000L // 1 hour
    }

    private val sharedPreferences by lazy {
        val masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)
        EncryptedSharedPreferences.getPreferences(
            SECURITY_SETTINGS,
            masterKeyAlias,
            context,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    // Fraud detection rules
    fun detectFraud(txnAmount: Double, currency: String): FraudDetectionResult {
        val threshold = getTransactionThreshold()

        if (txnAmount > threshold) {
            return FraudDetectionResult(
                isFraud = true,
                reason = "Amount exceeds secure threshold",
                threshold = threshold
            )
        }

        // Check for rapid transactions
        if (isRapidTransaction()) {
            return FraudDetectionResult(
                isFraud = true,
                reason = "Rapid transaction pattern detected",
                threshold = threshold
            )
        }

        // Check for unusual patterns
        if (isUnusualPattern()) {
            return FraudDetectionResult(
                isFraud = true,
                reason = "Unusual transaction pattern",
                threshold = threshold
            )
        }

        return FraudDetectionResult(isFraud = false)
    }

    // Security check helper
    private fun getTransactionThreshold(): Double {
        return sharedPreferences.getFloat("max_transaction", THRESHOLD_SECURE.toFloat())
    }

    private fun isRapidTransaction(): Boolean {
        // Check if multiple transactions occurred recently
        return false // Placeholder
    }

    private fun isUnusualPattern(): Boolean {
        // Check for unusual patterns (new recipient, new amount, etc.)
        return false // Placeholder
    }

    // Biometric authentication
    fun supportsBiometric(): Boolean {
        // Check if device supports biometric
        return true // Placeholder
    }

    fun isBiometricEnabled(): Boolean {
        return sharedPreferences.getBoolean("biometric_enabled", false)
    }

    fun enableBiometric(enabled: Boolean) {
        sharedPreferences.edit().putBoolean("biometric_enabled", enabled).apply()
    }

    // PIN management
    fun isPinRequired(): Boolean {
        return sharedPreferences.getBoolean("pin_required", true)
    }

    fun isPinSet(): Boolean {
        return sharedPreferences.contains("pin_hash")
    }

    fun verifyPin(inputPin: String): Boolean {
        val storedHash = sharedPreferences.getString("pin_hash", null) ?: return false
        // Compare inputPin hash with stored hash
        return inputPin.md5() == storedHash
    }

    fun setPin(pin: String): Boolean {
        if (pin.length < 4) return false
        sharedPreferences.edit()
            .putString("pin_hash", pin.md5())
            .putBoolean("pin_required", true)
            .apply()
        return true
    }

    fun clearPin() {
        sharedPreferences.edit().clear().apply()
    }

    // Fraud detection result model
    data class FraudDetectionResult(
        val isFraud: Boolean,
        val reason: String? = null,
        val threshold: Double = 0.0
    ) {
        fun shouldPromptForConfirmation(): Boolean = isFraud
    }
}

// Extension for MD5
fun String.md5(): String {
    val digest = java.security.MessageDigest.getInstance("MD5")
    val hash = digest.digest(this.toByteArray())
    return hash.joinToString("") { "%02x".format(it) }
}
