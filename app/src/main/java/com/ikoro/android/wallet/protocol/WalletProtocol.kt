package com.ikoro.android.wallet.protocol

import android.util.Log
import com.ikoro.android.nostr.NostrClient
import com.ikoro.android.nostr.NostrEvent
import com.ikoro.android.nostr.NostrProtocol
import com.ikoro.android.wallet.data.model.WalletEntity
import com.ikoro.android.wallet.data.model.Transaction
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * Wallet Protocol - Communication between ikorochat wallet and AgbaraWallet backend
 * Uses Nostr NIP-17 (encrypted DMs) for secure communication
 */
class WalletProtocol(
    private val nostrClient: NostrClient
) {
    
    companion object {
        private const val TAG = "WalletProtocol"
        private const val WALLET_REQUEST_TYPE = "WALLET_REQUEST"
        private const val WALLET_RESPONSE_TYPE = "WALLET_RESPONSE"
    }
    
    // Connection state
    private val _isConnected = MutableStateFlow(false)
    val isConnected: StateFlow<Boolean> = _isConnected
    
    private val _backendPeerNpub = MutableStateFlow<String?>(null)
    val backendPeerNpub: StateFlow<String?> = _backendPeerNpub
    
    private val responsehandlers = HashMap<String, (WalletResponse) -> Unit>()
    
    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    
    init {
        // Subscribe to wallet responses
        subscribeToWalletResponses()
        Log.i(TAG, "✅ WalletProtocol initialized")
    }
    
    /**
     * Connect to AgbaraWallet backend peer
     */
    suspend fun connectToBackend(
        backendNpub: String,
        onConnected: (() -> Unit)? = null,
        onError: ((String) -> Unit)? = null
    ) {
        withContext(scope.coroutineContext) {
            try {
                // Validate npub format
                if (!backendNpub.startsWith("npub1")) {
                    throw IllegalArgumentException("Invalid Nostr pubkey format")
                }
                
                _backendPeerNpub.value = backendNpub
                _isConnected.value = true
                
                onConnected?.invoke()
                Log.i(TAG, "✅ Connected to AgbaraWallet backend: $backendNpub")
                
            } catch (e: Exception) {
                Log.e(TAG, "❌ Failed to connect to backend: ${e.message}", e)
                _isConnected.value = false
                onError?.invoke(e.message ?: "Connection failed")
            }
        }
    }
    
    /**
     * Disconnect from backend
     */
    suspend fun disconnect() {
        withContext(scope.coroutineContext) {
            _isConnected.value = false
            _backendPeerNpub.value = null
            responsehandlers.clear()
            Log.i(TAG, "Disconnected from backend")
        }
    }
    
    /**
     * Send wallet request to backend
     */
    suspend fun sendRequest(
        request: WalletRequest,
        onResponse: ((WalletResponse) -> Unit)? = null,
        onError: ((String) -> Unit)? = null
    ): String {
        return withContext(scope.coroutineContext) {
            try {
                val requestId = generateRequestId()
                val backendNpub = _backendPeerNpub.value ?: throw IllegalStateException("Not connected")
                
                responsehandlers[requestId] = { response ->
                    onResponse?.invoke(response)
                    Log.d(TAG, "Received response for request $requestId: ${response.type}")
                }
                
                // Create gift wraps for encrypted message
                val event = createWalletRequestEvent(
                    requestId = requestId,
                    request = request,
                    recipientNpub = backendNpub
                )
                
                // Send via Nostr
                nostrClient.sendPrivateMessage(
                    content = event.content,
                    recipientNpub = backendNpub,
                    onSuccess = { Log.i(TAG, "Sent wallet request: $requestId") },
                    onError = { error ->
                        Log.e(TAG, "Failed to send wallet request: $error")
                        onError?.invoke(error)
                        responsehandlers.remove(requestId)
                    }
                )
                
                requestId
                
            } catch (e: Exception) {
                Log.e(TAG, "❌ Failed to send request: ${e.message}", e)
                onError?.invoke(e.message ?: "Request failed")
                ""
            }
        }
    }
    
    /**
     * Subscribe to wallet responses from backend
     */
    private fun subscribeToWalletResponses() {
        nostrClient.subscribeToPrivateMessages { content, senderNpub, timestamp ->
            scope.launch {
                try {
                    // Try to parse as wallet response
                    val response = parseWalletResponse(content)
                    if (response != null) {
                        Log.d(TAG, "Received wallet response from $senderNpub")
                        
                        // Route to handler
                        val handler = responsehandlers[response.requestId]
                        handler?.invoke(response)
                    } else {
                        Log.v(TAG, "Non-wallet message received")
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error parsing message: ${e.message}", e)
                }
            }
        }
    }
    
    /**
     * Request wallet balance for specific currency
     */
    suspend fun requestBalance(
        currency: String,
        onResponse: ((Double) -> Unit)? = null,
        onError: ((String) -> Unit)? = null
    ): String {
        val request = WalletRequest(
            requestId = generateRequestId(),
            type = WalletRequestType.BALANCE,
            payload = mapOf("currency" to currency)
        )
        return sendRequest(request, {
            if (it.type == WalletResponseType.BALANCE) {
                val balance = it.payload["balance"] as? Double ?: 0.0
                onResponse?.invoke(balance)
            }
        }, onError)
    }
    
    /**
     * Request address for wallet
     */
    suspend fun requestAddress(
        currency: String,
        onResponse: ((String) -> Unit)? = null,
        onError: ((String) -> Unit)? = null
    ): String {
        val request = WalletRequest(
            requestId = generateRequestId(),
            type = WalletRequestType.ADDRESS,
            payload = mapOf("currency" to currency)
        )
        return sendRequest(request, {
            if (it.type == WalletResponseType.ADDRESS) {
                val address = it.payload["address"] as? String ?: ""
                onResponse?.invoke(address)
            }
        }, onError)
    }
    
    /**
     * Submit transaction for signing and broadcasting
     */
    suspend fun submitTransaction(
        transaction: Transaction,
        onResponse: ((Transaction) -> Unit)? = null,
        onError: ((String) -> Unit)? = null
    ): String {
        val request = WalletRequest(
            requestId = generateRequestId(),
            type = WalletRequestType.SUBMIT_TX,
            payload = mapOf(
                "transaction" to transaction,
                "signature" to transaction.signature
            )
        )
        return sendRequest(request, {
            if (it.type == WalletResponseType.TX_SUBMITTED) {
                val updatedTx = it.payload["transaction"] as? Transaction
                updatedTx?.let { onResponse?.invoke(it) }
            }
        }, onError)
    }
    
    /**
     * Sync transaction history from backend
     */
    suspend fun syncTransactions(
        since: Long? = null,
        currency: String? = null,
        onResponse: ((List<Transaction>) -> Unit)? = null,
        onError: ((String) -> Unit)? = null
    ): String {
        val request = WalletRequest(
            requestId = generateRequestId(),
            type = WalletRequestType.SYNC_TX,
            payload = mapOf(
                "since" to since,
                "currency" to currency
            )
        )
        return sendRequest(request, {
            if (it.type == WalletResponseType.SYNC_TX) {
                val txList = it.payload["transactions"] as? List<Transaction> ?: emptyList()
                onResponse?.invoke(txList)
            }
        }, onError)
    }
    
    // MARK: - Event Construction
    
    private fun createWalletRequestEvent(
        requestId: String,
        request: WalletRequest,
        recipientNpub: String
    ): NostrEvent {
        // Create gift wrap (NIP-17)
        val content = """
            {
                "request_id": "$requestId",
                "type": "${request.type}",
                "payload": ${request.payload}
            }
        """
        
        // This would normally use NostrProtocol.createPrivateMessage
        // For now, return a simplified event structure
        return NostrEvent(
            id = requestId,
            pubkey = "sender_pubkey_placeholder",
            createdAt = System.currentTimeMillis() / 1000,
            kind = 13100, // Custom wallet protocol kind
            tags = listOf(
                listOf("p", recipientNpub),
                listOf("request_id", requestId)
            ),
            content = content,
            sig = "pending_signature"
        )
    }
    
    private fun parseWalletResponse(content: String): WalletResponse? {
        return try {
            // Parse JSON content
            if (content.contains("\"type\"") && content.contains("\"request_id\"")) {
                // Simplified parsing
                val requestId = "request_id_placeholder"
                val type = WalletResponseType.MESSAGE
                val payload = emptyMap<String, Any>()
                WalletResponse(requestId, type, payload)
            } else {
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing response: ${e.message}", e)
            null
        }
    }
    
    private fun generateRequestId(): String {
        return "req_${System.currentTimeMillis()}_${(0..99999).random()}"
    }
}

/**
 * Wallet Request Types
 */
enum class WalletRequestType {
    BALANCE,
    ADDRESS,
    SUBMIT_TX,
    SYNC_TX,
    SIGN_TX,
    MESSAGE
}

/**
 * Wallet Request Structure
 */
data class WalletRequest(
    val requestId: String,
    val type: WalletRequestType,
    val payload: Map<String, Any>
)

/**
 * Wallet Response Types
 */
enum class WalletResponseType {
    BALANCE,
    ADDRESS,
    TX_SUBMITTED,
    SYNC_TX,
    SIGN_TX,
    MESSAGE,
    ERROR
}

/**
 * Wallet Response Structure
 */
data class WalletResponse(
    val requestId: String,
    val type: WalletResponseType,
    val payload: Map<String, Any>
)
