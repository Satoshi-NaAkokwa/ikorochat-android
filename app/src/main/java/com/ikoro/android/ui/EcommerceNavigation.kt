package com.ikoro.android.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier

/**
 * ₿ Ọ F Ọ E-commerce UI (Simplified Offline-First)
 * Bottom navigation with Chat, Marketplace, Media, AI, Wallet, Orders, and Errand tabs
 * All features work 100% offline with mesh network support
 */
enum class EcommerceTab {
    CHAT,
    MARKETPLACE,
    MEDIA,
    AI,
    WALLET,
    ORDERS,
    ERRAND
}

@Composable
fun EcommerceNavigation(
    chatScreen: @Composable () -> Unit,
) {
    var selectedTab by remember { mutableStateOf(EcommerceTab.CHAT) }

    Scaffold(
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = selectedTab == EcommerceTab.CHAT,
                    onClick = { selectedTab = EcommerceTab.CHAT },
                    icon = { Icon(Icons.Default.Chat, contentDescription = null) },
                    label = { Text("Chat") }
                )
                NavigationBarItem(
                    selected = selectedTab == EcommerceTab.MARKETPLACE,
                    onClick = { selectedTab = EcommerceTab.MARKETPLACE },
                    icon = { Icon(Icons.Default.ShoppingCart, contentDescription = null) },
                    label = { Text("Marketplace") }
                )
                NavigationBarItem(
                    selected = selectedTab == EcommerceTab.MEDIA,
                    onClick = { selectedTab = EcommerceTab.MEDIA },
                    icon = { Icon(Icons.Default.PhotoLibrary, contentDescription = null) },
                    label = { Text("Media") }
                )
                NavigationBarItem(
                    selected = selectedTab == EcommerceTab.AI,
                    onClick = { selectedTab = EcommerceTab.AI },
                    icon = { Icon(Icons.Default.Psychology, contentDescription = null) },
                    label = { Text("AI") }
                )
                NavigationBarItem(
                    selected = selectedTab == EcommerceTab.WALLET,
                    onClick = { selectedTab = EcommerceTab.WALLET },
                    icon = { Icon(Icons.Default.AccountBalanceWallet, contentDescription = null) },
                    label = { Text("Wallet") }
                )
                NavigationBarItem(
                    selected = selectedTab == EcommerceTab.ORDERS,
                    onClick = { selectedTab = EcommerceTab.ORDERS },
                    icon = { Icon(Icons.Default.ReceiptLong, contentDescription = null) },
                    label = { Text("Orders") }
                )
                NavigationBarItem(
                    selected = selectedTab == EcommerceTab.ERRAND,
                    onClick = { selectedTab = EcommerceTab.ERRAND },
                    icon = { Icon(Icons.Default.DeliveryDining, contentDescription = null) },
                    label = { Text("Errand") }
                )
            }
        }
    ) { innerPadding ->
        when (selectedTab) {
            EcommerceTab.CHAT -> {
                Box(modifier = Modifier.padding(innerPadding)) {
                    chatScreen()
                }
            }
            EcommerceTab.MARKETPLACE -> {
                com.ikoro.android.ui.screens.MarketplaceScreen(modifier = Modifier.padding(innerPadding))
            }
            EcommerceTab.MEDIA -> {
                com.ikoro.android.ui.screens.MediaScreen(modifier = Modifier.padding(innerPadding))
            }
            EcommerceTab.AI -> {
                com.ikoro.android.ui.screens.AgbaraAssistantScreen(modifier = Modifier.padding(innerPadding))
            }
            EcommerceTab.WALLET -> {
                com.ikoro.android.ui.screens.WalletScreen(modifier = Modifier.padding(innerPadding))
            }
            EcommerceTab.ORDERS -> {
                com.ikoro.android.ui.screens.OrdersScreen(modifier = Modifier.padding(innerPadding))
            }
            EcommerceTab.ERRAND -> {
                com.ikoro.android.ui.screens.ErrandScreen(modifier = Modifier.padding(innerPadding))
            }
        }
    }
}