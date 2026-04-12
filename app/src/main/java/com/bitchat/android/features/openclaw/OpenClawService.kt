package com.bitchat.android.features.openclaw

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.SecretKeySpec

class OpenClawService : Service() {
    companion object {
        private const val TAG = "OpenClawService"
        const val STATE_DISCONNECTED = "disconnected"
        const val STATE_CONNECTING = "connecting"
        const val STATE_CONNECTED = "connected"
        const val STATE_HANDSHAKE = "handshake"
        const val STATE_ERROR = "error"
        
        const val ACTION_CONNECT = "com.bitchat.openclaw.CONNECT"
        const val ACTION_DISCONNECT = "com.bitchat.openclaw.DISCONNECT"
        const val ACTION_REVOKE = "com.bitchat.openclaw.REVOKE"
        const val EXTRA_PAIRING_CODE = "pairing_code"
        const val EXTRA_SESSION_KEY = "session_key"
    }
    
    private val _connectionState = MutableStateFlow(STATE_DISCONNECTED)
    val connectionState: StateFlow<String> = _connectionState
    
    private val _errorState = MutableStateFlow<String?>(null)
    val errorState: StateFlow<String?> = _errorState
    
    private var sessionKey: SecretKey? = null
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    
    override fun onBind(intent: Intent?): IBinder? = null
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_CONNECT -> initiateConnection(
                intent.getStringExtra(EXTRA_PAIRING_CODE),
                intent.getStringExtra(EXTRA_SESSION_KEY)
            )
            ACTION_DISCONNECT -> disconnectGracefully("User requested")
            ACTION_REVOKE -> revokeConnection()
        }
        return START_NOT_STICKY
    }
    
    private fun initiateConnection(pairingCode: String?, sessionKeyHex: String?) {
        scope.launch {
            try {
                _connectionState.value = STATE_CONNECTING
                sessionKey = generateSessionKey()
                _connectionState.value = STATE_HANDSHAKE
                Thread.sleep(2000)
                _connectionState.value = STATE_CONNECTED
                Log.d(TAG, "OpenClaw connection established")
            } catch (e: Exception) {
                _connectionState.value = STATE_ERROR
                _errorState.value = e.message
            }
        }
    }
    
    private suspend fun generateSessionKey(): SecretKey = withContext(Dispatchers.Default) {
        val keyGenerator = KeyGenerator.getInstance("AES")
        keyGenerator.init(256)
        keyGenerator.generateKey()
    }
    
    private fun disconnectGracefully(reason: String) {
        scope.launch {
            _connectionState.value = STATE_DISCONNECTED
            _errorState.value = null
            stopSelf()
        }
    }
    
    private fun revokeConnection() {
        scope.launch {
            _connectionState.value = STATE_DISCONNECTED
            sessionKey = null
            _errorState.value = "Connection revoked"
            stopSelf()
        }
    }
    
    override fun onDestroy() {
        super.onDestroy()
        scope.cancel()
        sessionKey = null
    }
}