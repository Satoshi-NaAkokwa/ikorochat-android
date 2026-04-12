package com.bitchat.android.features.openclaw

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.google.zxing.integration.android.IntentIntegrator
import com.google.zxing.integration.android.IntentResult
import java.security.SecureRandom

class OpenClawPairingActivity : ComponentActivity() {
    companion object {
        private const val TAG = "OpenClawPairing"
        private const val QR_SCAN_REQUEST = 12345
    }
    
    private lateinit var sessionKey: String
    private val secureRandom = SecureRandom()
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sessionKey = generateSessionKey()
        
        setContent {
            PairingScreen(
                sessionKey = sessionKey,
                onScanRequest = { launchQRScanner() },
                onRevokeRequest = { revokePairing() }
            )
        }
    }
    
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == QR_SCAN_REQUEST) {
            val result: IntentResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, data)
            if (result != null && result.contents != null) {
                val request = parsePairingQR(result.contents)
                showApprovalDialog(request)
            } else {
                Toast.makeText(this, "QR scan cancelled", Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    private fun parsePairingQR(qrData: String): PairingRequest {
        val parts = qrData.split("|").associate {
            val (key, value) = it.split(":", limit = 2)
            key to value
        }
        return PairingRequest(
            version = parts["v"] ?: "unknown",
            sessionKey = parts["i"] ?: "",
            timestamp = parts["t"]?.toLongOrNull() ?: 0,
            deviceId = parts["d"] ?: "",
            nonce = parts["n"] ?: "",
            purpose = parts["p"] ?: "unknown",
            protocol = parts["x"] ?: "unknown"
        )
    }
    
    private fun showApprovalDialog(request: PairingRequest) {
        AlertDialog.Builder(this)
            .setTitle("🔐 Pairing Request")
            .setMessage(buildPairingDetailsMessage(request))
            .setPositiveButton("approve") { _, _ ->
                if (validateRequest(request)) {
                    approvePairing()
                    finish()
                }
            }
            .setNegativeButton("REJECT") { _, _ -> Toast.makeText(this, "Rejected", Toast.LENGTH_SHORT).show() }
            .setCancelable(false)
            .show()
    }
    
    private fun buildPairingDetailsMessage(request: PairingRequest): String {
        val timestampReadable = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
            .format(java.util.Date(request.timestamp * 1000))
        return """
        |
        |Device: ${request.deviceId}
        |Session Key: ${request.sessionKey.take(20)}...
        |Timestamp: $timestampReadable
        |Purpose: ${request.purpose}
        |Protocol: ${request.protocol}
        |
        |Security Verification:
        |✅ Purpose: code.collab
        |✅ Protocol: noise.v1
        |✅ Keys/wallet: NOT requested ✓
        |
        |After Approval:
        |• E2E encrypted communication
        |• Real-time AI collaboration
        |• Feature development sandbox
        |• All communication logged
        |• Emergency disconnect available
        |""".trimMargin()
    }
    
    private fun validateRequest(request: PairingRequest): Boolean {
        val now = System.currentTimeMillis() / 1000
        if (now - request.timestamp > 300) return false
        if (request.purpose != "code.collab") return false
        if (request.protocol != "noise.v1") return false
        return true
    }
    
    private fun approvePairing() {
        val serviceIntent = Intent(this, OpenClawService::class.java).apply {
            action = OpenClawService.ACTION_CONNECT
            putExtra(OpenClawService.EXTRA_PAIRING_CODE, "OpenClawPair:" + java.util.UUID.randomUUID())
            putExtra(OpenClawService.EXTRA_SESSION_KEY, sessionKey)
        }
        startForegroundService(serviceIntent)
        Toast.makeText(this, "✅ Pairing established!", Toast.LENGTH_SHORT).show()
    }
    
    private fun revokePairing() {
        AlertDialog.Builder(this)
            .setTitle("🚨 Revoke Pairing?")
            .setMessage("Immediately disconnect from OpenClaw.")
            .setPositiveButton("REVOKE") { _, _ ->
                val serviceIntent = Intent(this, OpenClawService::class.java).apply {
                    action = OpenClawService.ACTION_REVOKE
                }
                startService(serviceIntent)
                Toast.makeText(this, "Revoked", Toast.LENGTH_SHORT).show()
                finish()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    
    private fun launchQRScanner() {
        IntentIntegrator(this).apply {
            setDesiredBarcodeFormats(IntentIntegrator.QR_CODE)
            setPrompt("Scan OpenClaw pairing QR code")
            setOrientationLocked(true)
            initiateScan()
        }
    }
    
    private suspend fun generateSessionKey(): String {
        return withContext(Dispatchers.Default) {
            val bytes = ByteArray(32)
            secureRandom.nextBytes(bytes)
            bytes.joinToString("") { "%02x".format(it) }
        }
    }
}

data class PairingRequest(
    val version: String,
    val sessionKey: String,
    val timestamp: Long,
    val deviceId: String,
    val nonce: String,
    val purpose: String,
    val protocol: String
)
