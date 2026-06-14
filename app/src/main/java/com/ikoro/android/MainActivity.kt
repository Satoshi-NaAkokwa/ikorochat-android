package com.ikoro.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.Contacts
import androidx.compose.material.icons.filled.Wallet
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.ikoro.android.security.AppLockScreen
import com.ikoro.android.security.FirstTimeLockSetup
import com.ikoro.android.security.SecureVault
import com.ikoro.android.ui.ChatScreen
import com.ikoro.android.ui.ChatViewModel
import com.ikoro.android.ui.IncomingCallDialog
import com.ikoro.android.ui.theme.BitchatTheme
import com.ikoro.android.wallet.ui.WalletScreen

class MainActivity : ComponentActivity() {

    private val chatViewModel: ChatViewModel by viewModels()

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allPermissionsGranted = permissions.values.all { it }
        if (!allPermissionsGranted) {
            finish()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestPermissions()

        setContent {
            BitchatTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    var locked by remember { mutableStateOf(true) }
                    var needsSetup by remember { mutableStateOf(false) }
                    val context = LocalContext.current

                    LaunchedEffect(Unit) {
                        val hasPin = SecureVault.hasPin(context)
                        needsSetup = !hasPin
                        locked = hasPin
                    }

                    when {
                        needsSetup -> FirstTimeLockSetup(
                            onDone = {
                                needsSetup = false
                                locked = false
                            }
                        )
                        locked -> AppLockScreen(
                            onUnlocked = { locked = false }
                        )
                        else -> IkoroNavHost(chatViewModel = chatViewModel)
                    }
                }
            }
        }
    }

    private fun requestPermissions() {
        val permissions = mutableListOf<String>()

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            permissions.addAll(listOf(
                android.Manifest.permission.BLUETOOTH_ADVERTISE,
                android.Manifest.permission.BLUETOOTH_CONNECT,
                android.Manifest.permission.BLUETOOTH_SCAN
            ))
        } else {
            permissions.addAll(listOf(
                android.Manifest.permission.BLUETOOTH,
                android.Manifest.permission.BLUETOOTH_ADMIN
            ))
        }

        permissions.addAll(listOf(
            android.Manifest.permission.ACCESS_COARSE_LOCATION,
            android.Manifest.permission.ACCESS_FINE_LOCATION
        ))

        permissions.addAll(listOf(
            android.Manifest.permission.RECORD_AUDIO,
            android.Manifest.permission.CAMERA,
            android.Manifest.permission.MODIFY_AUDIO_SETTINGS
        ))

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            permissions.add(android.Manifest.permission.POST_NOTIFICATIONS)
        }

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            permissionLauncher.launch(permissions.toTypedArray())
        }
    }
}

sealed class Screen(val route: String, val label: String, val icon: androidx.compose.ui.graphics.vector.ImageVector) {
    object Chat : Screen("chat", "Chat", Icons.Default.ChatBubble)
    object Contacts : Screen("contacts", "People", Icons.Default.Contacts)
    object Wallet : Screen("wallet", "Wallet", Icons.Default.Wallet)
    object Calls : Screen("calls", "Calls", Icons.Default.Call)
}

@Composable
fun IkoroNavHost(chatViewModel: ChatViewModel) {
    val navController = rememberNavController()

    val incomingRoom by chatViewModel.incomingCallRoom.observeAsState()
    val incomingPeer by chatViewModel.incomingCallPeer.observeAsState()

    if (incomingRoom != null) {
        IncomingCallDialog(
            peer = incomingPeer,
            onAccept = {
                chatViewModel.acceptIncomingCall { _, _ ->
                    navController.navigate(Screen.Calls.route)
                }
            },
            onReject = { chatViewModel.rejectIncomingCall() }
        )
    }

    Scaffold(
        bottomBar = {
            NavigationBar {
                val items = listOf(Screen.Chat, Screen.Contacts, Screen.Wallet, Screen.Calls)
                val currentRoute = navController.currentBackStackEntry?.destination?.route
                items.forEach { screen ->
                    NavigationBarItem(
                        icon = { Icon(screen.icon, contentDescription = screen.label) },
                        label = { Text(screen.label) },
                        selected = currentRoute == screen.route,
                        onClick = { navController.navigate(screen.route) }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Chat.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Chat.route) {
                ChatScreen(
                    viewModel = chatViewModel,
                    onNavigateToCalls = { navController.navigate(Screen.Calls.route) }
                )
            }
            composable(Screen.Contacts.route) {
                // Placeholder contacts screen
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Contacts (Nostr identity layer)")
                }
            }
            composable(Screen.Wallet.route) {
                WalletScreen(onBack = { navController.navigate(Screen.Chat.route) })
            }
            composable(Screen.Calls.route) {
                com.ikoro.android.calls.ui.CallScreen(callManager = chatViewModel.callManager)
            }
        }
    }
}
