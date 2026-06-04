package com.ikoro.android.wallet.ui.screens.backup

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ikoro.android.wallet.services.backup.WalletBackupService
import java.io.File


/**
 * Wallet Backup Screen
 */
@Composable
fun WalletBackupScreen(
    onBack: () -> Unit,
    backupService: WalletBackupService
) {
    var showExportSuccess by remember { mutableStateOf(false) }
    var showRestoreSuccess by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Backup & Restore",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back")
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Backup Options
        Column(modifier = Modifier.fillMaxWidth()) {
            BackupOption(
                icon = Icons.Default.CloudUpload,
                title = "Auto Backup",
                description = "Automatically backs up to encrypted local storage",
                action = { backupService.backupWallets(emptyList()) },
                actionText = "Enable"
            )

            Spacer(modifier = Modifier.height(16.dp))

            BackupOption(
                icon = Icons.Default.Folder,
                title = "Export to Storage",
                description = "Export encrypted backup to Downloads folder",
                action = {
                    val path = backupService.exportBackupToSDCard()
                    if (path != null) {
                        showExportSuccess = true
                    }
                },
                actionText = "Export"
            )

            Spacer(modifier = Modifier.height(16.dp))

            BackupOption(
                icon = Icons.Default.CloudDownload,
                title = "Restore from Backup",
                description = "Restore wallet data from encrypted backup",
                action = {
                    val wallets = backupService.restoreWallets()
                    if (wallets != null && wallets.isNotEmpty()) {
                        showRestoreSuccess = true
                    }
                },
                actionText = "Restore"
            )

            Spacer(modifier = Modifier.height(16.dp))

            BackupOption(
                icon = Icons.Default.Sync,
                title = "Verify Backup",
                description = "Check backup integrity and timestamps",
                action = {
                    val valid = backupService.verifyBackup()
                    if (valid) {
                        // Show success message
                    }
                },
                actionText = "Verify"
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Backup Status
        Text(
            text = "Current Status",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )

        Spacer(modifier = Modifier.height(12.dp))

        WalletBackupStatus(backupService)
    }

    // Show success messages
    if (showExportSuccess) {
        Snackbar(
            actionLabel = "Done",
            onDismiss = { showExportSuccess = false }
        ) {
            Text("Backup exported successfully")
        }
    }
}

@Composable
fun BackupOption(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    description: String,
    action: () -> Unit,
    actionText: String
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, modifier = Modifier.padding(end = 16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleMedium)
                Text(description, style = MaterialTheme.typography.bodySmall)
            }
            Button(
                onClick = action,
                modifier = Modifier.padding(start = 8.dp)
            ) {
                Text(actionText)
            }
        }
    }
}

@Composable
fun WalletBackupStatus(backupService: WalletBackupService) {
    val lastBackupTime = remember { backupService.lastBackupTime() }

    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "Last Backup",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = lastBackupTime ?: "Never",
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(modifier = Modifier.height(16.dp))

            Divider()

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Backup Location",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "/data/data/com.ikoro.android/files/",
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

fun WalletBackupService.lastBackupTime(): String? {
    return try {
        val masterKey = kotlin.runCatching {
            androidx.security.crypto.MasterKey.Builder(
                context,
                androidx.security.crypto.MasterKey.DEFAULT_MASTER_KEY_ALIAS
            )
                .setKeyScheme(androidx.security.crypto.MasterKey.KeyScheme.AES256_GCM)
                .build()
        }.getOrNull()

        if (masterKey != null) {
            val encryptedPrefs = androidx.security.crypto.EncryptedSharedPreferences.create(
                context,
                sharedPreferencesName,
                masterKey,
                androidx.security.crypto.EncryptedSharedPreferences.EncryptionScheme.AES256_GCM
            )
            val lastBackup = encryptedPrefs.getLong("last_backup", 0)
            if (lastBackup > 0) {
                val date = java.util.Date(lastBackup)
                java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault()).format(date)
            } else {
                null
            }
        } else {
            null
        }
    } catch (e: Exception) {
        null
    }
}
