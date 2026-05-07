package com.ikoro.android.ui.components

import android.content.Context
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

/**
 * Dark Mode Toggle Component
 * Features:
 * - Switch between light, dark, and auto modes
 * - Persistent preference storage
 * - Smooth transitions
 * - Icon-based toggle button
 */
enum class ThemeMode(val value: Int, val label: String) {
    LIGHT(AppCompatDelegate.MODE_NIGHT_NO, "Light"),
    DARK(AppCompatDelegate.MODE_NIGHT_YES, "Dark"),
    AUTO(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM, "Auto")
}

class ThemeManager private constructor(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    var themeMode: ThemeMode
        get() {
            val modeValue = prefs.getInt(KEY_THEME_MODE, ThemeMode.AUTO.value)
            return ThemeMode.values().find { it.value == modeValue } ?: ThemeMode.AUTO
        }
        set(value) {
            prefs.edit().putInt(KEY_THEME_MODE, value.value).apply()
            applyTheme(value)
        }

    fun applyTheme(mode: ThemeMode) {
        AppCompatDelegate.setDefaultNightMode(mode.value)
    }

    companion object {
        private const val PREFS_NAME = "theme_prefs"
        private const val KEY_THEME_MODE = "theme_mode"

        @Volatile
        private var instance: ThemeManager? = null

        fun getInstance(context: Context): ThemeManager {
            return instance ?: synchronized(this) {
                instance ?: ThemeManager(context.applicationContext).also { instance = it }
            }
        }
    }
}

@Composable
fun DarkModeToggle(
    modifier: Modifier = Modifier,
    showLabel: Boolean = false
) {
    val context = LocalContext.current
    val themeManager = remember { ThemeManager.getInstance(context) }
    var currentMode by remember { mutableStateOf(themeManager.themeMode) }
    val isDarkMode = when (currentMode) {
        ThemeMode.DARK -> true
        ThemeMode.LIGHT -> false
        ThemeMode.AUTO -> {
            // Auto mode - check system setting
            val nightMode = context.resources.configuration.uiMode
            val isSystemDark = nightMode and android.content.res.Configuration.UI_MODE_NIGHT_MASK ==
                    android.content.res.Configuration.UI_MODE_NIGHT_YES
            isSystemDark
        }
    }

    IconButton(
        onClick = {
            // Toggle between light/dark modes
            currentMode = if (isDarkMode) ThemeMode.LIGHT else ThemeMode.DARK
            themeManager.themeMode = currentMode
        },
        modifier = modifier
    ) {
        Icon(
            imageVector = if (isDarkMode) Icons.Default.LightMode else Icons.Default.DarkMode,
            contentDescription = "Toggle theme",
            tint = MaterialTheme.colorScheme.onSurface
        )
    }

    if (showLabel) {
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = if (isDarkMode) "Light Mode" else "Dark Mode",
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
fun ThemeSelector(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val themeManager = remember { ThemeManager.getInstance(context) }
    var selectedMode by remember { mutableStateOf(themeManager.themeMode) }

    Column(
        modifier = modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "Theme",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface
        )

        ThemeMode.values().forEach { mode ->
            ThemeOption(
                mode = mode,
                isSelected = selectedMode == mode,
                onClick = {
                    selectedMode = mode
                    themeManager.themeMode = mode
                }
            )
        }
    }
}

@Composable
fun ThemeOption(
    mode: ThemeMode,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val icon = when (mode) {
        ThemeMode.LIGHT -> Icons.Default.LightMode
        ThemeMode.DARK -> Icons.Default.DarkMode
        ThemeMode.AUTO -> {
            // Auto mode - check current system preference
            val context = LocalContext.current
            val nightMode = context.resources.configuration.uiMode
            val isSystemDark = nightMode and android.content.res.Configuration.UI_MODE_NIGHT_MASK ==
                    android.content.res.Configuration.UI_MODE_NIGHT_YES
            if (isSystemDark) Icons.Default.DarkMode else Icons.Default.LightMode
        }
    }

    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        color = if (isSelected) {
            MaterialTheme.colorScheme.primaryContainer
        } else {
            Color.Transparent
        },
        border = if (isSelected) {
            BorderStroke(
                width = 1.dp,
                color = MaterialTheme.colorScheme.primary
            )
        } else {
            null
        }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = mode.label,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Selected",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
fun ThemeToggleWithIcon(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val themeManager = remember { ThemeManager.getInstance(context) }
    var currentMode by remember { mutableStateOf(themeManager.themeMode) }

    val backgroundColor = MaterialTheme.colorScheme.surfaceVariant
    val activeColor = MaterialTheme.colorScheme.primary

    Surface(
        onClick = {
            currentMode = when (currentMode) {
                ThemeMode.LIGHT -> ThemeMode.DARK
                ThemeMode.DARK -> ThemeMode.AUTO
                ThemeMode.AUTO -> ThemeMode.LIGHT
            }
            themeManager.themeMode = currentMode
        },
        modifier = modifier
            .width(120.dp)
            .height(40.dp)
            .clip(CircleShape),
        color = backgroundColor
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            ThemeModeToggleIcon(
                mode = ThemeMode.LIGHT,
                isActive = currentMode == ThemeMode.LIGHT,
                activeColor = activeColor
            )
            ThemeModeToggleIcon(
                mode = ThemeMode.DARK,
                isActive = currentMode == ThemeMode.DARK,
                activeColor = activeColor
            )
            ThemeModeToggleIcon(
                mode = ThemeMode.AUTO,
                isActive = currentMode == ThemeMode.AUTO,
                activeColor = activeColor
            )
        }
    }
}

@Composable
fun ThemeModeToggleIcon(
    mode: ThemeMode,
    isActive: Boolean,
    activeColor: Color
) {
    val icon = when (mode) {
        ThemeMode.LIGHT -> Icons.Default.LightMode
        ThemeMode.DARK -> Icons.Default.DarkMode
        ThemeMode.AUTO -> Icons.Default.AutoAwesome // Using AutoAwesome for auto mode
    }

    Box(
        modifier = Modifier.size(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = mode.label,
            tint = if (isActive) activeColor else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(20.dp)
        )
    }
}