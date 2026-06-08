package com.ikoro.android.wallet.ui.scanner

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.video.Recorder
import androidx.camera.video.Recording
import androidx.camera.video.VideoCapture
import androidx.camera.video.VideoCaptureMode
import androidx.camera.video.VideoFile
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.lifecycleScope
import com.google.mlkit.vision.barcode.Barcode
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.Barcodes
import com.google.mlkit.vision.barcode.InputImage
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.BarcodeScanningOptions
import com.ikoro.android.wallet.ui.theme.*
import kotlinx.coroutines.launch


/**
 * QR Scanner Screen - Uses CameraX + ML Kit for scanning Bitcoin addresses
 */
@Composable
fun QRScannerScreen(
    onScanResult: (String) -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    
    varpermissionGranted by remember { mutableStateOf(false) }
    varhasCameraPermission by remember { context.packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA) }
    varscanningStarted by remember { mutableStateOf(false) }
    varlastResult by remember { mutableStateOf<String?>(null) }
    varfocusPoint by remember { mutableStateOf<android.graphics.PointF?>(null) }
    
    // Check permissions
    LaunchedEffect(Unit) {
        val cameraPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.CAMERA
        )
        permissionGranted = cameraPermission == PackageManager.PERMISSION_GRANTED
    }
    
    // Handle permission request
    if (!permissionGranted) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = "Lock",
                    modifier = Modifier.size(64.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Camera Permission Required",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Please grant camera permission to scan QR codes",
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(24.dp))
                Button(onClick = {
                    ActivityCompat.requestPermissions(
                        context as Activity,
                        arrayOf(Manifest.permission.CAMERA),
                        1001
                    )
                }) {
                    Text("Grant Permission")
                }
            }
        }
        return
    }
    
    // Camera implementation
    var cameraProvider: LiveData<ProcessCameraProvider?> by remember { mutableStateOf(null) }
    var previewUseCase: LiveData<Preview?> by remember { mutableStateOf(null) }
    
    CameraProviderListener(
        context = context,
        lifecycleOwner = lifecycleOwner,
        onCameraAvailable = { cameraProvider ->
            cameraProviderLiveData = cameraProvider
            setupCamera(cameraProvider, context, onScanResult)
        }
    )
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF1A1A1A)),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
            }
            Text(
                text = "Scan QR Code",
                style = MaterialTheme.typography.titleMedium,
                color = Color.White
            )
            Spacer(modifier = Modifier.width(48.dp)) // Spacer for balance
        }
        
        // Camera preview area (if camera available)
        if (cameraProviderLiveData != null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .aspectRatio(1f)
                    .background(Color.Black)
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                // Overlay reticle
                Box(
                    modifier = Modifier
                        .size(200.dp)
                        .border(
                            2.dp,
                            Color(0xFF00FF00),
                            RoundedCornerShape(12.dp)
                        )
                        .alpha(0.5f)
                )
                
                // Scanning indicator
                if (scanningStarted && !lastResult.isNullOrEmpty()) {
                    Text(
                        text = "QR Code Scanned!",
                        style = MaterialTheme.typography.bodyLarge.copy(color = Color(0xFF00FF00)),
                        fontSize = 18.sp
                    )
                } else {
                    Text(
                        text = "Position QR code within the frame",
                        style = MaterialTheme.typography.bodyMedium.copy(color = Color.Gray),
                        fontSize = 14.sp
                    )
                }
            }
        } else {
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .aspectRatio(1f)
                    .background(Color(0xFF333333))
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Color.White)
                Text(
                    text = "Initializing camera...",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )
            }
        }
        
        // Actions
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedButton(
                modifier = Modifier.weight(1f),
                onClick = {
                    scanningStarted = true
                },
                enabled = !scanningStarted
            ) {
                Icon(Icons.Default.QrCode, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Start Scanning")
            }
            
            OutlinedButton(
                modifier = Modifier.weight(1f),
                onClick = { onBack() }
            ) {
                Icon(Icons.Default.Close, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Cancel")
            }
        }
        
        Spacer(modifier = Modifier.height(32.dp))
    }
}

/**
 * Camera setup using CameraX
 */
private fun setupCamera(
    cameraProvider: ProcessCameraProvider,
    context: Context,
    onScanResult: (String) -> Unit
) {
    val preview = Preview.Builder().build().also {
        it.setSurfaceProvider(context)
    }
    
    // Create image analyzer for QR code detection
    val analyzer = ImageAnalysis.Builder()
        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
        .build()
        .also {
            it.setAnalyzer(
                ContextCompat.getMainExecutor(context),
                QRCodeAnalyzer(onScanResult)
            )
        }
    
    // Bind to lifecycle
    try {
        cameraProvider.bindToLifecycle(context as AppCompatActivity, 
            CameraSelector.DEFAULT_BACK_CAMERA,
            preview,
            analyzer
        )
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

/**
 * QR Code Analyzer - ML Kit barcode detection
 */
class QRCodeAnalyzer(private val onScanResult: (String) -> Unit) : ImageAnalysis.Analyzer {
    
    private val options = BarcodeScanningOptions.Builder()
        .setBarcodeFormats(Barcode.FORMAT_QR_CODE)
        .build()
    
    private val scanner = BarcodeScanning.getClient(options)
    
    override fun analyze(image: ImageProxy) {
        val mediaImage = image.image!!
        val inputImage = InputImage.fromMediaImage(mediaImage, image.imageInfo.rotationDegrees)
        
        scanner.process(inputImage)
            .addOnSuccessListener { barcodes ->
                for (barcode in barcodes) {
                    val rawValue = barcode.rawValue
                    if (!rawValue.isNullOrEmpty()) {
                        onScanResult(rawValue)
                        break
                    }
                }
            }
            .addOnFailureListener { e ->
                e.printStackTrace()
            }
            .addOnCompleteListener {
                image.close()
            }
    }
}

/**
 * Camera Provider Lifecycle Listener
 */
@Composable
fun CameraProviderListener(
    context: Context,
    lifecycleOwner: LifecycleOwner,
    onCameraAvailable: (ProcessCameraProvider) -> Unit
) {
    val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
    
    cameraProviderFuture.addListener(Runnable {
        try {
            val cameraProvider = cameraProviderFuture.get()
            onCameraAvailable(cameraProvider)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }, ContextCompat.getMainExecutor(context))
}

/**
 * Extension for Context to get AppCompatActivity
 */
val Context.activity: AppCompatActivity
    get() = this as AppCompatActivity

/**
 * Extension for Context to get MainActivity
 */
val Context.mainActivity: MainActivity
    get() = this as MainActivity

/**
 * Check if package has camera feature
 */
fun Context.hasSystemFeature(feature: String): Boolean {
    return packageManager.hasSystemFeature(feature)
}
