package com.ikoro.android.wallet.services.security

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

/**
 * Security Manager - Authentication and fraud detection
 */
class SecurityManager private constructor(private val context: Context) {
    
    companion object {
        private const val TAG = "SecurityManager"
        
        // Fraud detection thresholds
        const val MAX_SINGLE_TX_THRESHOLD = 0.1 // BTC
        const val MAX_DAILY_TX_THRESHOLD = 1.0 // BTC
        const val RATE_LIMIT_WINDOW_MS = 60 * 60 * 1000 // 1 hour
        const val RATE_LIMIT_MAX = 10
        
        // Pin requirements
        const val MIN_PIN_LENGTH = 4
        const val MAX_PIN_LENGTH = 6
        
        @Volatile
        private var INSTANCE: SecurityManager? = null
    }
    
    init {
        // Initialize PIN storage
        initializePINStorage()
    }
    
    fun getInstance(): SecurityManager {
        return INSTANCE ?: synchronized(this) {
            INSTANCE ?: SecurityManager(context.applicationContext).also { INSTANCE = it }
        }
    }
    
    // PIN Storage
    private fun initializePINStorage() {
        try {
            val masterKey = MasterKey.Builder(context)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build()
            
            val prefs = EncryptedSharedPreferences.create(
                context,
                "security_settings_encrypted",
                masterKey,
                EncryptedSharedPreferences.EncryptionScheme.AES256_GCM
            )
            
            prefs.edit().apply()
            
        } catch (e: Exception) {
            throw RuntimeException("Failed to initialize PIN storage", e)
        }
    }
    
    // Authentication
    fun verifyPIN(pin: String): Boolean {
        return try {
            val masterKey = MasterKey.Builder(context)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build()
            
            val prefs = EncryptedSharedPreferences.create(
                context,
                "security_settings_encrypted",
                masterKey,
                EncryptedSharedPreferences.EncryptionScheme.AES256_GCM
            )
            
            val storedPIN = prefs.getString("pin_hash", null)
            if (storedPIN == null) return false
            
            // Hash the provided PIN and compare
            val inputHash = hashPIN(pin)
            inputHash == storedPIN
            
        } catch (e: Exception) {
            false
        }
    }
    
    fun storePIN(pin: String) {
        try {
            val masterKey = MasterKey.Builder(context)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build()
            
            val prefs = EncryptedSharedPreferences.create(
                context,
                "security_settings_encrypted",
                masterKey,
                EncryptedSharedPreferences.EncryptionScheme.AES256_GCM
            )
            
            prefs.edit()
                .putString("pin_hash", hashPIN(pin))
                .apply()
                
        } catch (e: Exception) {
            throw RuntimeException("Failed to store PIN", e)
        }
    }
    
    fun changePIN(oldPIN: String, newPIN: String): Boolean {
        if (!verifyPIN(oldPIN)) return false
        
        storePIN(newPIN)
        return true
    }
    
    // Biometric Authentication
    fun isBiometricAvailable(): Boolean {
        return android.os.Build.VERSION.SDK_INT >= 23
    }
    
    fun canUseBiometric(): Boolean {
        return isBiometricAvailable()
    }
    
    // Fraud Detection
    private val transactionCounters = HashMap<String, Int>()
    private val transactionTimestamps = HashMap<String, Long>()
    
    fun checkRateLimit(category: String): Boolean {
        val now = System.currentTimeMillis()
        val windowStart = now - RATE_LIMIT_WINDOW_MS
        
        // Clean old entries
        val expiredKeys = transactionTimestamps.filterValues { _it < windowStart }.keys
        expiredKeys.forEach {
            transactionCounters.remove(it)
            transactionTimestamps.remove(it)
        }
        
        // Check current count
        val currentCount = transactionCounters.getOrPut(category) { 0 }
        
        if (currentCount >= RATE_LIMIT_MAX) {
            return false
        }
        
        // Increment counter
        transactionCounters[category] = currentCount + 1
        transactionTimestamps[category] = now
        
        return true
    }
    
    fun checkBalanceThreshold(amount: Double): Boolean {
        return amount <= MAX_SINGLE_TX_THRESHOLD
    }
    
    // PIN Hashing
    private fun hashPIN(pin: String): String {
        val input = (pin + System.currentTimeMillis()).encodeToByteArray()
        return java.security.MessageDigest.getInstance("SHA-256").digest(input)
            .fold("", { str, byte -> str + "%02x".format(byte) })
    }
    
    // Security logging
    fun logSecurityEvent(event: String) {
        android.util.Log.d(TAG, "Security event: $event")
    }
    
    fun getSecurityStatus(): Map<String, Any> {
        return mapOf(
            "pin_set" to hasPIN(),
            "biometric_available" to isBiometricAvailable(),
            "encryption" to "AES256_GCM"
        )
    }
    
    private fun hasPIN(): Boolean {
        return try {
            val masterKey = MasterKey.Builder(context)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build()
            
            val prefs = EncryptedSharedPreferences.create(
                context,
                "security_settings_encrypted",
                masterKey,
                EncryptedSharedPreferences.EncryptionScheme.AES256_GCM
            )
            
            prefs.contains("pin_hash")
        } catch (e: Exception) {
            false
        }
    }
}
