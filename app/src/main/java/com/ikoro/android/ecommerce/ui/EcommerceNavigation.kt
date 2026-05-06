package com.ikoro.android.ecommerce.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.ikoro.android.ecommerce.viewmodel.WalletViewModel
import com.ikoro.android.ecommerce.viewmodel.OrderViewModel

/**
 * ₿ Ọ F Ọ E-commerce Navigation (Offline-First)
 * Bottom navigation with Chat, Marketplace, Wallet, and Orders tabs
 * All features work 100% offline with mesh network support
 */
enum class EcommerceScreen(val route: String, val label: String, val icon: androidx.compose.ui.graphics.vector.ImageVector) {
    CHAT("chat", "Chat", Icons.Default.Chat),
    MARKETPLACE("marketplace", "Marketplace", Icons.Default.Storefront),
    WALLET("wallet", "Wallet", Icons.Default.AccountBalanceWallet),
    ORDERS("orders", "Orders", Icons.Default.ReceiptLong)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EcommerceNavigation(
    chatScreen: @Composable () -> Unit,
    navController: NavController = rememberNavController()
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    Scaffold(
        bottomBar = {
            NavigationBar {
                EcommerceScreen.values().forEach { screen ->
                    NavigationBarItem(
                        selected = currentDestination?.route == screen.route,
                        onClick = {
                            navController.navigate(screen.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = {
                            Icon(
                                imageVector = screen.icon,
                                contentDescription = screen.label
                            )
                        },
                        label = {
                            Text(screen.label)
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = EcommerceScreen.CHAT.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            // Chat screen (existing Ikoro mesh messaging)
            composable(EcommerceScreen.CHAT.route) {
                chatScreen()
            }

            // Marketplace screen
            composable(EcommerceScreen.MARKETPLACE.route) {
                ProductListScreen(
                    onProductClick = { productId ->
                        // Navigate to product detail (offline)
                        navController.navigate("product/$productId")
                    },
                    viewModel = hiltViewModel()
                )
            }

            // Wallet screen
            composable(EcommerceScreen.WALLET.route) {
                WalletScreen(
                    onSendClick = { /* Show send dialog */ },
                    onReceiveClick = { /* Show receive dialog */ },
                    viewModel = hiltViewModel()
                )
            }

            // Orders screen
            composable(EcommerceScreen.ORDERS.route) {
                OrderScreen(
                    onOrderClick = { orderId ->
                        navController.navigate("order/$orderId")
                    },
                    viewModel = hiltViewModel()
                )
            }

            // Product detail screen (placeholder)
            composable("product/{productId}") { backStackEntry ->
                val productId = backStackEntry.arguments?.getString("productId") ?: ""
                // ProductDetailScreen(productId = productId)
            }

            // Order detail screen
            composable("order/{orderId}") { backStackEntry ->
                val orderId = backStackEntry.arguments?.getString("orderId") ?: ""
                // OrderDetailScreen(orderId = orderId, onBackClick = { navController.popBackStack() })
            }
        }
    }
}