package com.ikoro.android.wallet.ui

import android.graphics.Bitmap
import android.widget.ImageView
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter
import com.ikoro.android.IkoroApplication
import com.ikoro.android.identity.NostrCrypto
import com.ikoro.android.security.SecureVault
import com.ikoro.android.wallet.Asset
import com.ikoro.android.wallet.WalletService
import com.ikoro.android.wallet.WalletState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import breez_sdk_liquid.Payment

class WalletViewModel(private val walletService: WalletService) : ViewModel() {
    val state: StateFlow<WalletState> = walletService.state
    val balanceSat: StateFlow<Long> = walletService.balanceSat
    val balanceUsdt: StateFlow<Long> = walletService.balanceUsdt
    val payments: StateFlow<List<Payment>> = walletService.payments
    val error: StateFlow<String?> = walletService.error

    private val _receiveInvoice = MutableStateFlow("")
    val receiveInvoice: StateFlow<String> = _receiveInvoice

    private val _sendResult = MutableStateFlow("")
    val sendResult: StateFlow<String> = _sendResult

    private val _selectedAsset = MutableStateFlow<Asset>(Asset.BTC)
    val selectedAsset: StateFlow<Asset> = _selectedAsset

    fun setAsset(asset: Asset) { _selectedAsset.value = asset }

    fun connect() {
        viewModelScope.launch {
            walletService.connect().onFailure { /* error surfaced via error flow */ }
        }
    }

    fun refresh() {
        viewModelScope.launch {
            walletService.refreshBalance()
        }
    }

    fun receive(amountSat: Long?) {
        viewModelScope.launch {
            walletService.receivePayment(amountSat, asset = _selectedAsset.value)
                .onSuccess { _receiveInvoice.value = it }
        }
    }

    fun send(destination: String) {
        viewModelScope.launch {
            walletService.sendPayment(destination, asset = _selectedAsset.value)
                .onSuccess { _sendResult.value = "Sent: $it" }
        }
    }
}

class WalletViewModelFactory(private val context: android.content.Context) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        val app = context.applicationContext as IkoroApplication
        val service = WalletService(app, app.identityManager)
        return WalletViewModel(service) as T
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WalletScreen(onBack: () -> Unit = {}) {
    val context = LocalContext.current
    val viewModel: WalletViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
        factory = WalletViewModelFactory(context)
    )
    val state by viewModel.state.collectAsState()
    val balance by viewModel.balanceSat.collectAsState()
    val balanceUsdt by viewModel.balanceUsdt.collectAsState()
    val invoice by viewModel.receiveInvoice.collectAsState()
    val sendResult by viewModel.sendResult.collectAsState()
    val error by viewModel.error.collectAsState()
    val selectedAsset by viewModel.selectedAsset.collectAsState()
    val clipboard = LocalClipboardManager.current

    var sendInput by remember { mutableStateOf("") }
    var receiveAmount by remember { mutableStateOf("") }

    val assetLabel = when (selectedAsset) {
        Asset.BTC -> "BTC"
        Asset.LUSDT -> "L-USDT"
    }

    LaunchedEffect(Unit) {
        viewModel.connect()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Wallet") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = when (state) {
                    is WalletState.NotInitialized -> "Initializing wallet..."
                    is WalletState.Connecting -> "Connecting to Breez..."
                    is WalletState.Ready -> "Wallet ready"
                    is WalletState.Error -> "Wallet error"
                },
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Balance: $balance sats",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "L-USDT: $balanceUsdt",
                style = MaterialTheme.typography.titleMedium
            )

            Row {
                Button(onClick = { viewModel.setAsset(Asset.BTC) }, enabled = selectedAsset != Asset.BTC) {
                    Text("BTC")
                }
                Spacer(modifier = Modifier.width(8.dp))
                Button(onClick = { viewModel.setAsset(Asset.LUSDT) }, enabled = selectedAsset != Asset.LUSDT) {
                    Text("L-USDT")
                }
            }

            Button(onClick = { viewModel.refresh() }) {
                Text("Refresh Balance")
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Receive section
            Text("Receive $assetLabel", style = MaterialTheme.typography.titleLarge)
            OutlinedTextField(
                value = receiveAmount,
                onValueChange = { receiveAmount = it },
                label = { Text("Amount (${if (selectedAsset == Asset.BTC) "sats" else "units"})") },
                modifier = Modifier.fillMaxWidth()
            )
            Button(onClick = { viewModel.receive(receiveAmount.toLongOrNull()) }) {
                Text("Generate Invoice")
            }

            if (invoice.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Image(
                    bitmap = generateQrBitmap(invoice).asImageBitmap(),
                    contentDescription = "Receive QR",
                    modifier = Modifier.size(200.dp)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(invoice.take(60) + "...", style = MaterialTheme.typography.bodySmall)
                Button(onClick = { clipboard.setText(AnnotatedString(invoice)) }) {
                    Text("Copy Invoice")
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Send section
            Text("Send $assetLabel", style = MaterialTheme.typography.titleLarge)
            OutlinedTextField(
                value = sendInput,
                onValueChange = { sendInput = it },
                label = { Text("Invoice / Address / LNURL") },
                modifier = Modifier.fillMaxWidth()
            )
            Button(onClick = { viewModel.send(sendInput) }) {
                Text("Send Payment")
            }

            if (sendResult.isNotBlank()) {
                Text(sendResult, color = MaterialTheme.colorScheme.primary)
            }

            error?.let {
                Spacer(modifier = Modifier.height(8.dp))
                Text(it, color = MaterialTheme.colorScheme.error)
            }
        }
    }
}

private fun generateQrBitmap(content: String): Bitmap {
    val writer = QRCodeWriter()
    val bitMatrix = writer.encode(content, BarcodeFormat.QR_CODE, 512, 512)
    val width = bitMatrix.width
    val height = bitMatrix.height
    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)
    for (x in 0 until width) {
        for (y in 0 until height) {
            bitmap.setPixel(x, y, if (bitMatrix[x, y]) android.graphics.Color.BLACK else android.graphics.Color.WHITE)
        }
    }
    return bitmap
}
