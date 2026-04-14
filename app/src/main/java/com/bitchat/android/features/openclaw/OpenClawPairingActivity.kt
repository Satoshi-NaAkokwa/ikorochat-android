package com.bitchat.android.features.openclaw

import android.Manifest
import android.graphics.Bitmap
import androidx.camera.compose.CameraXViewfinder
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.viewfinder.core.ImplementationMode
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.QrCodeScanner
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.graphics.createBitmap
import androidx.core.graphics.set
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionStatus
import com.google.accompanist.permissions.rememberPermissionState
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import com.google.zxing.BarcodeFormat
import com.google.zxing.common.BitMatrix
import com.google.zxing.qrcode.QRCodeWriter

/**
 * OpenClaw Pairing Activity
 *
 * Handles device pairing with OpenClaw by:
 * 1. Generate secure pairing QR code
 * 2. Scan incoming pairing QR codes
 * 3. Handle pairing state machine (requests, approval, rejection)
 * 4. Show pairing status to user
 *
 * SECURITY: All pairing info is ephemeral, no persistent storage unless approved
 */
class OpenClawPairingActivity {
    
    companion object {
        private const val TAG = "OpenClawPairing"
        const val EXTRA_PAIRING_CODE = "pairing_code"
        const val EXTRA_PAIRING_REQUEST_ID = "pairing_request_id"
    }
    
    /**
     * Pairing states
     */
    enum class PairingState {
        IDLE,           // No pairing in progress
        WAITING,        // Waiting for user scan/confirm
        APPROVED,       // Pairing approved by user
        COMPLETED,      // Pairing complete
        REJECTED        // Pairing rejected
    }
    
    /**
     * Pairing request data
     */
    data class PairingRequest(
        val requestId: String,
        val agentId: String,
        val agentName: String,
        val publicKey: String,
        val timestamp: Long,
        val status: PairingState = PairingState.WAITING
    )
}

/**
 * Data class for OpenClaw pairing configuration
 */
data class OpenClawConfig(
    val agentId: String = "openclaw-agbara",
    val agentName: String = "Agbara",
    val deviceId: String = "", // Device-specific identifier
    val capabilities: List<String> = listOf()
)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun OpenClawPairingScreen(
    modifier: Modifier = Modifier,
    config: OpenClawConfig = OpenClawConfig(),
    onPairingComplete: (String) -> Unit = {},
    onPairingRejected: () -> Unit = {},
    onGeneratePairingCode: () -> String = { generatePairingCode() }
) {
    var selectedTab by remember { mutableStateOf(0) } // 0 = My Code, 1 = Scan
    val pairingCode = remember { mutableStateOf(onGeneratePairingCode()) }
    val scannedCode = remember { mutableStateOf("") }
    val pairingState = remember { mutableStateOf(OpenClawPairingActivity.PairingState.IDLE) }
    val pendingRequest = remember { mutableStateOf<OpenClawPairingActivity.PairingRequest?>(null) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "OpenClaw Pairing",
                        fontFamily = FontFamily.Monospace
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Tab row
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = Color.Transparent,
                contentColor = MaterialTheme.colorScheme.primary
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("My QR Code", fontFamily = FontFamily.Monospace) },
                    icon = { Icon(Icons.Outlined.QrCodeScanner, contentDescription = null) }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Scan Pairing Code", fontFamily = FontFamily.Monospace) },
                    icon = { Icon(Icons.Outlined.QrCodeScanner, contentDescription = null) }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Content
            when (selectedTab) {
                0 -> MyQRCodeTabContent(
                    pairingCode = pairingCode.value,
                    config = config,
                    onRefresh = { pairingCode.value = onGeneratePairingCode() }
                )
                1 -> ScanQRCodeTabContent(
                    onCodeScanned = { code ->
                        scannedCode.value = code
                        // Parse and show approval dialog
                        val request = parsePairingRequest(code)
                        if (request != null) {
                            pendingRequest.value = request
                        }
                    }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Status display
            PairingStatusCard(
                pairingState = pairingState.value,
                pendingRequest = pendingRequest.value,
                onApprove = {
                    pairingState.value = OpenClawPairingActivity.PairingState.APPROVED
                    onPairingComplete(scannedCode.value)
                },
                onReject = {
                    pairingState.value = OpenClawPairingActivity.PairingState.REJECTED
                    pendingRequest.value = null
                    onPairingRejected()
                }
            )
        }
    }
}

@Composable
private fun MyQRCodeTabContent(
    pairingCode: String,
    config: OpenClawConfig,
    onRefresh: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Your Pairing Code",
            style = MaterialTheme.typography.titleMedium,
            fontFamily = FontFamily.Monospace,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(16.dp))

        // QR Code display
        Box(
            modifier = Modifier
                .size(280.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(Color.White)
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            QRCodeImage(data = pairingCode, size = 248.dp)
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Agency info
        Text(
            text = "Agent: ${config.agentName}",
            fontFamily = FontFamily.Monospace,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Text(
            text = "ID: ${config.agentId}",
            fontFamily = FontFamily.Monospace,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Refresh button
        Button(
            onClick = onRefresh,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary
            )
        ) {
            Text("Generate New Code", fontFamily = FontFamily.Monospace)
        }
    }
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
private fun ScanQRCodeTabContent(
    onCodeScanned: (String) -> Unit
) {
    val cameraPermission = rememberPermissionState(Manifest.permission.CAMERA)

    if (cameraPermission.status is PermissionStatus.Granted) {
        QRScannerView(
            onCodeScanned = onCodeScanned,
            modifier = Modifier
                .fillMaxWidth()
                .height(400.dp)
                .clip(RoundedCornerShape(16.dp))
                .border(
                    2.dp,
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                    RoundedCornerShape(16.dp)
                )
        )
    } else {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(400.dp)
                .background(
                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                    RoundedCornerShape(16.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Camera Permission Required",
                    fontFamily = FontFamily.Monospace,
                    style = MaterialTheme.typography.titleSmall
                )
                Button(
                    onClick = { cameraPermission.launchPermissionRequest() },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text("Grant Permission", fontFamily = FontFamily.Monospace)
                }
            }
        }
    }
}

@Composable
private fun PairingStatusCard(
    pairingState: OpenClawPairingActivity.PairingState,
    pendingRequest: OpenClawPairingActivity.PairingRequest?,
    onApprove: () -> Unit,
    onReject: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Pairing Status",
                style = MaterialTheme.typography.titleSmall,
                fontFamily = FontFamily.Monospace
            )

            when (pairingState) {
                OpenClawPairingActivity.PairingState.IDLE -> {
                    Text(
                        text = "No pairing in progress",
                        fontFamily = FontFamily.Monospace,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                OpenClawPairingActivity.PairingState.WAITING -> {
                    if (pendingRequest != null) {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                text = "Incoming Pairing Request",
                                fontFamily = FontFamily.Monospace,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "Agent: ${pendingRequest.agentName}",
                                fontFamily = FontFamily.Monospace
                            )
                            Text(
                                text = "ID: ${pendingRequest.agentId}",
                                fontFamily = FontFamily.Monospace
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    modifier = Modifier.weight(1f),
                                    onClick = onReject,
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.error
                                    )
                                ) {
                                    Text("Reject", fontFamily = FontFamily.Monospace)
                                }
                                Button(
                                    modifier = Modifier.weight(1f),
                                    onClick = onApprove,
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.primary
                                    )
                                ) {
                                    Text("Approve", fontFamily = FontFamily.Monospace)
                                }
                            }
                        }
                    } else {
                        Text(
                            text = "Waiting for scan...",
                            fontFamily = FontFamily.Monospace,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                OpenClawPairingActivity.PairingState.APPROVED -> {
                    Text(
                        text = "✓ Pairing Approved",
                        fontFamily = FontFamily.Monospace,
                        color = Color.Green
                    )
                }
                OpenClawPairingActivity.PairingState.COMPLETED -> {
                    Text(
                        text = "✓ Pairing Complete",
                        fontFamily = FontFamily.Monospace,
                        color = Color.Green
                    )
                }
                OpenClawPairingActivity.PairingState.REJECTED -> {
                    Text(
                        text = "✗ Pairing Rejected",
                        fontFamily = FontFamily.Monospace,
                        color = Color.Red
                    )
                }
            }
        }
    }
}

@Composable
private fun QRCodeImage(data: String, size: androidx.compose.ui.unit.Dp) {
    val sizePx = with(LocalDensity.current) { size.toPx().toInt() }
    val bitmap = remember(data, sizePx) { generateQrBitmap(data, sizePx) }
    if (bitmap != null) {
        Image(
            bitmap = bitmap.asImageBitmap(),
            contentDescription = null,
            modifier = Modifier.size(size)
        )
    }
}

private fun generateQrBitmap(data: String, sizePx: Int): Bitmap? {
    if (data.isBlank() || sizePx <= 0) return null
    return try {
        val matrix = QRCodeWriter().encode(data, BarcodeFormat.QR_CODE, sizePx, sizePx)
        bitmapFromMatrix(matrix)
    } catch (_: Exception) {
        null
    }
}

private fun bitmapFromMatrix(matrix: BitMatrix): Bitmap {
    val width = matrix.width
    val height = matrix.height
    val bitmap = createBitmap(width, height)
    for (x in 0 until width) {
        for (y in 0 until height) {
            bitmap.set(x, y,
                if (matrix[x, y]) android.graphics.Color.BLACK else android.graphics.Color.WHITE
            )
        }
    }
    return bitmap
}

/**
 * Parse pairing request from QR code
 */
fun parsePairingRequest(qrData: String): OpenClawPairingActivity.PairingRequest? {
    return try {
        val parts = qrData.split("|")
        if (parts.size >= 4) {
            OpenClawPairingActivity.PairingRequest(
                requestId = parts[0],
                agentId = parts[1],
                agentName = parts[2],
                publicKey = parts[3],
                timestamp = System.currentTimeMillis()
            )
        } else {
            null
        }
    } catch (e: Exception) {
        null
    }
}

/**
 * Generate unique pairing code
 */
fun generatePairingCode(): String {
    val timestamp = System.currentTimeMillis()
    return "openclaw|agbara|$timestamp"
}

/**
 * QR Scanner View using CameraX and MLKit
 */
@Composable
private fun QRScannerView(
    onCodeScanned: (String) -> Unit,
    modifier: Modifier = Modifier,
    context: android.content.Context = LocalContext.current,
    lifecycleOwner: androidx.lifecycle.LifecycleOwner = LocalLifecycleOwner.current
) {
    val surfaceRequests = remember { kotlinx.coroutines.flow.MutableStateFlow<androidx.camera.core.SurfaceRequest?>(null) }
    val cameraExecutor = remember { java.util.concurrent.Executors.newSingleThreadExecutor() }
    val hasScanned = remember { mutableStateOf(false) }

    DisposableEffect(Unit) {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
        val cameraProvider = cameraProviderFuture.get()

        val preview = androidx.camera.core.Preview.Builder().build()
        val imageAnalysis = ImageAnalysis.Builder()
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .build()

        val scanner = BarcodeScanning.getClient(
            BarcodeScannerOptions.Builder()
                .setBarcodeFormats(Barcode.FORMAT_QR_CODE)
                .build()
        )

        imageAnalysis.setAnalyzer(cameraExecutor) { imageProxy ->
            if (hasScanned.value) {
                imageProxy.close()
                return@setAnalyzer
            }

            val mediaImage = imageProxy.image ?: run {
                imageProxy.close()
                return@setAnalyzer
            }

            val inputImage = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
            scanner.process(inputImage)
                .addOnSuccessListener { barcodes ->
                    val code = barcodes.firstOrNull()?.rawValue
                    if (!code.isNullOrBlank()) {
                        hasScanned.value = true
                        onCodeScanned(code)
                    }
                }
                .addOnCompleteListener { imageProxy.close() }
        }

        val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

        try {
            cameraProvider.unbindAll()
            cameraProvider.bindToLifecycle(
                lifecycleOwner,
                cameraSelector,
                preview,
                imageAnalysis
            )

            preview.setSurfaceProvider { request ->
                surfaceRequests.value = request
            }
        } catch (e: Exception) {
            // Handle binding errors
        }

        onDispose {
            runCatching { cameraProvider.unbindAll() }
            cameraExecutor.shutdown()
        }
    }

    surfaceRequests.value?.let { request ->
        CameraXViewfinder(
            surfaceRequest = request,
            implementationMode = ImplementationMode.EMBEDDED,
            modifier = modifier.fillMaxSize()
        )
    }
}