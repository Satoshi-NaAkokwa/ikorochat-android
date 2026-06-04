package com.ikoro.android.wallet.services.backup

import android.content.Context
import android.util.Base64
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.google.gson.Gson
import com.ikoro.android.wallet.data.model.Wallet
import com.ikoro.android.wallet.data.model.Transaction
import com.ikoro.android.wallet.domain.service.KeyManager
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStream
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.spec.GCMParameterSpec


/**
 * Wallet Backup Service
 */
class WalletBackupService(
    private val context: Context,
    private val keyManager: KeyManager
) {

    private val gson = Gson()
    private val sharedPreferencesName = "wallet_backup_encrypted"
    private val secureRandom = SecureRandom()

    /**
     * Back up all wallet data to encrypted local storage
     */
    fun backupWallets(wallets: List<Wallet>): Boolean {
        try {
            val masterKey = MasterKey.Builder(context, MasterKey.DEFAULT_MASTER_KEY_ALIAS)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build()

            val encryptedPrefs = EncryptedSharedPreferences.create(
                context,
                sharedPreferencesName,
                masterKey,
                EncryptedSharedPreferences.EncryptionScheme.AES256_GCM
            )

            val walletJson = gson.toJson(wallets)

            encryptedPrefs.edit().putString("wallets_backup", walletJson).apply()

            // Also create a timestamped backup file
            val timestamp = System.currentTimeMillis()
            val backupFile = File(context.filesDir, "wallet_backup_$timestamp.json")

            val cipher = Cipher.getInstance("AES/GCM/NoPadding")
            val keyGenerator = KeyGenerator.getInstance("AES")
            keyGenerator.init(256, secureRandom)
            val secretKey = keyGenerator.generateKey()

            cipher.init(Cipher.ENCRYPT_MODE, secretKey)
            val gcmParameterSpec = GCMParameterSpec(128, cipher.iv)
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, gcmParameterSpec)

            val encryptedData = cipher.doFinal(walletJson.toByteArray())

            // Store iv + encrypted data
            val combined = cipher.iv + encryptedData
            val encoded = Base64.encodeToString(combined, Base64.DEFAULT)

            FileOutputStream(backupFile).use { out ->
                out.write(encoded.toByteArray())
            }

            encryptedPrefs.edit().putLong("last_backup", timestamp).apply()

            return true
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }
    }

    /**
     * Restore wallet data from encrypted backup
     */
    fun restoreWallets(): List<Wallet>? {
        try {
            val masterKey = MasterKey.Builder(context, MasterKey.DEFAULT_MASTER_KEY_ALIAS)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build()

            val encryptedPrefs = EncryptedSharedPreferences.create(
                context,
                sharedPreferencesName,
                masterKey,
                EncryptedSharedPreferences.EncryptionScheme.AES256_GCM
            )

            val backupJson = encryptedPrefs.getString("wallets_backup", null)
            if (backupJson != null) {
                return gson.fromJson(backupJson, Array<Wallet>::class.java).toList()
            }

            // Try loading from timestamped file
            val lastBackup = encryptedPrefs.getLong("last_backup", 0)
            if (lastBackup > 0) {
                val backupFile = File(context.filesDir, "wallet_backup_$lastBackup.json")
                if (backupFile.exists()) {
                    FileInputStream(backupFile).use { fis ->
                        val encoded = String(fis.readBytes())
                        val decoded = Base64.decode(encoded, Base64.DEFAULT)

                        // Extract IV (first 12 bytes for GCM)
                        val iv = decoded.copyOfRange(0, 12)
                        val encryptedData = decoded.copyOfRange(12, decoded.size)

                        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
                        val secretKey = keyManager.getMasterKey() // Use stored key

                        cipher.init(Cipher.DECRYPT_MODE, secretKey, GCMParameterSpec(128, iv))
                        val decryptedData = cipher.doFinal(encryptedData)

                        return gson.fromJson(String(decryptedData), Array<Wallet>::class.java).toList()
                    }
                }
            }

            return null
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }

    /**
     * Export wallet backup to file (for USB/Folder backup)
     */
    fun exportBackupToSDCard(filename: String = "ikoro_wallet_backup.json"): String? {
        return try {
            val masterKey = MasterKey.Builder(context, MasterKey.DEFAULT_MASTER_KEY_ALIAS)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build()

            val encryptedPrefs = EncryptedSharedPreferences.create(
                context,
                sharedPreferencesName,
                masterKey,
                EncryptedSharedPreferences.EncryptionScheme.AES256_GCM
            )

            val backupJson = encryptedPrefs.getString("wallets_backup", null)
            if (backupJson != null) {
                val exportDir = android.os.Environment.getExternalStoragePublicDirectory(
                    android.os.Environment.DIRECTORY_DOWNLOADS
                )
                val exportFile = File(exportDir, filename)

                FileOutputStream(exportFile).use { out ->
                    out.write(backupJson.toByteArray())
                }

                exportFile.absolutePath
            } else {
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Verify backup integrity
     */
    fun verifyBackup(): Boolean {
        val masterKey = MasterKey.Builder(context, MasterKey.DEFAULT_MASTER_KEY_ALIAS)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()

        val encryptedPrefs = EncryptedSharedPreferences.create(
            context,
            sharedPreferencesName,
            masterKey,
            EncryptedSharedPreferences.EncryptionScheme.AES256_GCM
        )

        val backupJson = encryptedPrefs.getString("wallets_backup", null)
        val lastBackup = encryptedPrefs.getLong("last_backup", 0)

        return backupJson != null && lastBackup > 0 && validateBackup(backupJson)
    }

    private fun validateBackup(json: String): Boolean {
        return try {
            val wallets = gson.fromJson(json, Array<Wallet>::class.java)
            wallets.isNotEmpty() && wallets.any { it.id > 0 }
        } catch (e: Exception) {
            false
        }
    }
}
