package com.ikoro.android.wallet.ui

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter
import com.ikoro.android.IkoroApplication
import com.ikoro.android.wallet.WalletBalance
import com.ikoro.android.wallet.WalletService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class WalletViewModel(private val walletService: WalletService) : ViewModel() {
    private val _balance = MutableStateFlow(WalletBalance(0, 0, 0))
    val balance: StateFlow<WalletBalance> = _balance

    private val _address = MutableStateFlow("")
    val address: StateFlow<String> = _address

    private val _sendResult = MutableStateFlow("")
    val sendResult: StateFlow<String> = _sendResult

    init {
        refresh()
    }

    fun refresh() {
        _balance.value = walletService.getBalance()
        _address.value = walletService.getReceiveAddress() ?: ""
    }

    fun send(destination: String, amountSats: Long) {
        // Placeholder: real send needs UTXO + signing
        _sendResult.value = "Send requested: ${amountSats} sats to $destination\n(UTXO sync not yet wired)"
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WalletScreen(
    onBack: () -> Unit,
    viewModel: WalletViewModel = viewModel(
        factory = WalletViewModelFactory(LocalContext.current.applicationContext as IkoroApplication)
    )
) {
    val context = LocalContext.current
    val clipboard = LocalClipboardManager.current
    val balance by viewModel.balance.collectAsState()
    val address by viewModel.address.collectAsState()
    val sendResult by viewModel.sendResult.collectAsState()
    var destination by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var selectedTab by remember { mutableIntStateOf(0) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Wallet") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
        ) {
            TabRow(selectedTabIndex = selectedTab) {
                Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }, text = { Text("Balance") })
                Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }, text = { Text("Receive") })
                Tab(selected = selectedTab == 2, onClick = { selectedTab = 2 }, text = { Text("Send") })
            }

            when (selectedTab) {
                0 -> BalanceTab(balance)
                1 -> ReceiveTab(address, clipboard, context)
                2 -> SendTab(destination, amount, { destination = it }, { amount = it }, sendResult, viewModel)
            }
        }
    }
}

@Composable
private fun BalanceTab(balance: WalletBalance) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Total Balance", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(8.dp))
        Text("${balance.totalSats} sats", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(16.dp))
        Text("Confirmed: ${balance.confirmedSats} sats")
        Text("Pending: ${balance.pendingSats} sats")
    }
}

@Composable
private fun ReceiveTab(address: String, clipboard: androidx.compose.ui.platform.ClipboardManager, context: android.content.Context) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Receive Bitcoin", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(16.dp))
        if (address.isNotBlank()) {
            val qrBitmap = remember(address) { generateQrBitmap(address, 512) }
            qrBitmap?.let {
                Image(
                    bitmap = it.asImageBitmap(),
                    contentDescription = "QR Code",
                    modifier = Modifier.size(200.dp)
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = address,
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            Button(onClick = { clipboard.setText(AnnotatedString(address)) }) {
                Icon(Icons.Default.Share, contentDescription = null)
                Spacer(modifier = Modifier.width(4.dp))
                Text("Copy Address")
            }
        } else {
            Text("No wallet available. Create an identity first.")
        }
    }
}

@Composable
private fun SendTab(
    destination: String,
    amount: String,
    onDestinationChange: (String) -> Unit,
    onAmountChange: (String) -> Unit,
    sendResult: String,
    viewModel: WalletViewModel
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp)
    ) {
        OutlinedTextField(
            value = destination,
            onValueChange = onDestinationChange,
            label = { Text("Destination Address") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = amount,
            onValueChange = onAmountChange,
            label = { Text("Amount (sats)") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = {
                val amountSats = amount.toLongOrNull() ?: 0L
                if (destination.isNotBlank() && amountSats > 0) {
                    viewModel.send(destination, amountSats)
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = destination.isNotBlank() && amount.isNotBlank()
        ) {
            Text("Send")
        }
        if (sendResult.isNotBlank()) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(sendResult, style = MaterialTheme.typography.bodySmall)
        }
    }
}

private fun generateQrBitmap(content: String, size: Int): Bitmap? {
    return try {
        val writer = QRCodeWriter()
        val matrix = writer.encode(content, BarcodeFormat.QR_CODE, size, size)
        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.RGB_565)
        for (x in 0 until size) {
            for (y in 0 until size) {
                bitmap.setPixel(x, y, if (matrix.get(x, y)) android.graphics.Color.BLACK else android.graphics.Color.WHITE)
            }
        }
        bitmap
    } catch (e: Exception) {
        null
    }
}
