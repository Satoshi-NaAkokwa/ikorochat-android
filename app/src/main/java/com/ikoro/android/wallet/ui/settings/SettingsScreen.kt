package com.ikoro.android.wallet.ui.settings

import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.ikoro.android.wallet.ui.theme.WalletTheme


/**
 * Settings Screen - User preferences and wallet configuration
 */
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    onSave: (() -> Unit)? = null
) {
    val context = LocalContext.current
    var currentTheme by remember { mutableStateOf("auto") }
    var notificationEnabled by remember { mutableStateOf(true) }
    var biometricEnabled by remember { mutableStateOf(true) }
    var dataSyncInterval by remember { mutableStateOf("5") } // minutes
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Spacer(modifier = Modifier.height(16.dp))
        
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Settings",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back")
            }
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // General Settings
        SettingsSectionTitle("General")
        
        // Theme Setting
        SettingRow(
            icon = Icons.Default.Pixel,
            title = "Appearance",
            subtitle = when (currentTheme) {
                "light" -> "Always light"
                "dark" -> "Always dark"
                else -> "System default"
            },
            onClick = {
                val themes = listOf("auto", "light", "dark")
                val currentIndex = themes.indexOf(currentTheme)
                currentTheme = themes[(currentIndex + 1) % themes.size]
            }
        )
        
        // Data Sync Setting
        SettingRow(
            icon = Icons.Default.Sync,
            title = "Data Sync Interval",
            subtitle = "$dataSyncInterval minutes",
            onClick = {
                val intervals = listOf("5", "10", "30", "60")
                val currentIndex = intervals.indexOf(dataSyncInterval)
                dataSyncInterval = intervals[(currentIndex + 1) % intervals.size]
            }
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        SettingsSectionTitle("Security")
        
        // Biometric Setting
        SettingSwitchRow(
            icon = Icons.Default.Fingerprint,
            title = "Biometric Authentication",
            subtitle = "Use fingerprint/Face ID to sign transactions",
            enabled = biometricEnabled,
            onValueChange = { biometricEnabled = it }
        )
        
        // Notification Setting
        SettingSwitchRow(
            icon = Icons.Default.Notifications,
            title = "Notifications",
            subtitle = "Receive transaction and balance updates",
            enabled = notificationEnabled,
            onValueChange = { notificationEnabled = it }
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        SettingsSectionTitle("Wallet")
        
        // Reset Wallet Setting
        SettingRow(
            icon = Icons.Default.Refresh,
            title = "Force Sync",
            subtitle = "Re-sync wallets with backend",
            onClick = {
                // Trigger sync
            }
        )
        
        SettingRow(
            icon = Icons.Default.Delete,
            title = "Reset Wallet",
            subtitle = "Remove all wallet data",
            isDestructive = true,
            onClick = {
                // Show reset confirmation dialog
            }
        )
        
        Spacer(modifier = Modifier.height(32_dp))
        
        // About Section
        Divider()
        Spacer(modifier = Modifier.height(16_dp))
        
        Text(
            text = "About",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Spacer(modifier = Modifier.height(8_dp))
        
        Column {
            Text("Version: 1.0.0", style = MaterialTheme.typography.bodyMedium)
            Text("Build: ${System.currentTimeMillis()}", style = MaterialTheme.typography.bodyMedium)
            Text("Min SDK: 26", style = MaterialTheme.typography.bodyMedium)
            Text("Target SDK: 35", style = MaterialTheme.typography.bodyMedium)
        }
        
        Spacer(modifier = Modifier.height(32_dp))
        
        // Save Button
        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = { onSave?.invoke() }
        ) {
            Text("Save Changes")
        }
        
        Spacer(modifier = Modifier.height(24_dp))
    }
}

@Composable
fun SettingsSectionTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier.padding(vertical = 8_dp)
    )
}

@Composable
fun SettingRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    isDestructive: Boolean = false
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8_dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onClick() }
                .padding(16_dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                icon,
                contentDescription = null,
                modifier = Modifier.padding(end = 16_dp),
                tint = if (isDestructive) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Column {
                Text(
                    title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Spacer(modifier = Modifier.weight(1f))
            
            Icon(
                Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun SettingSwitchRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    enabled: Boolean,
    onValueChange: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8_dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16_dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                icon,
                contentDescription = null,
                modifier = Modifier.padding(end = 16_dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Switch(
                checked = enabled,
                onCheckedChange = onValueChange
            )
        }
    }
}

