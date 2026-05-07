package com.ikoro.android.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

/**
 * Bottom Navigation Bar Component
 * Features:
 * - Modern Material 3 design
 * - Dark mode support
 * - Badge indicators for unread messages
 * - Smooth transitions
 * - Haptic feedback on navigation
 */
sealed class BottomNavItem(
    val route: String,
    val icon: ImageVector,
    val label: String,
    val badgeCount: Int = 0
) {
    object Chat : BottomNavItem("chat", Icons.Default.Chat, "Chat")
    object Contacts : BottomNavItem("contacts", Icons.Default.People, "Contacts")
    object Channels : BottomNavItem("channels", Icons.Default.Forum, "Channels")
    object Settings : BottomNavItem("settings", Icons.Default.Settings, "Settings")
}

@Composable
fun BottomNavigationBar(
    selectedItem: BottomNavItem,
    onItemSelected: (BottomNavItem) -> Unit,
    modifier: Modifier = Modifier
) {
    val hapticFeedback = androidx.compose.ui.platform.LocalHapticFeedback.current

    NavigationBar(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 8.dp
    ) {
        BottomNavItem.values().forEach { item ->
            NavigationBarItem(
                selected = selectedItem == item,
                onClick = {
                    hapticFeedback.performHapticFeedback(
                        androidx.compose.ui.hapticfeedback.HapticFeedbackType.TextHandleMove
                    )
                    onItemSelected(item)
                },
                icon = {
                    BadgedBox(
                        badge = {
                            if (item.badgeCount > 0) {
                                Badge {
                                    Text(
                                        text = if (item.badgeCount > 99) "99+" else item.badgeCount.toString()
                                    )
                                }
                            }
                        }
                    ) {
                        Icon(
                            imageVector = item.icon,
                            contentDescription = item.label
                        )
                    }
                },
                label = { Text(item.label) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.primary,
                    selectedTextColor = MaterialTheme.colorScheme.primary,
                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    indicatorColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    }
}

/**
 * Update badge counts for navigation items
 */
@Composable
fun rememberUpdatedNavItems(
    chatUnread: Int = 0,
    contactsUnread: Int = 0,
    channelsUnread: Int = 0
): List<BottomNavItem> {
    return remember(chatUnread, contactsUnread, channelsUnread) {
        listOf(
            BottomNavItem.Chat.copy(badgeCount = chatUnread),
            BottomNavItem.Contacts.copy(badgeCount = contactsUnread),
            BottomNavItem.Channels.copy(badgeCount = channelsUnread),
            BottomNavItem.Settings
        )
    }
}