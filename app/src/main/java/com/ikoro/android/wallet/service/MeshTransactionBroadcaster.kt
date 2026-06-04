package com.ikoro.android.wallet.service

import android.content.Context
import android.util.Log
import com.ikoro.android.data.model.Currency
import com.ikoro.android.mesh.BluetoothMeshService
import com.ikoro.android.model.IkoroMessage
import com.ikoro.android.protocol.MessageType
import com.ikoro.android.util.AppConstants
import com.ikoro.android.util.toHexString
import kotlinx.coroutines.*
import java.util.*

/**
 * MeshTransactionBroadcaster - Broadcasts transactions via mesh network (Bluetooth LE)
 * Implements offline-first architecture with mesh-based transaction broadcasting
 */
class MeshTransactionBroadcaster(private val context: Context) {
    
    private val TAG = "MeshTransactionBroadcaster"
    
    // Mesh service for broadcasting
    private var meshService: BluetoothMeshService? = null
    
    // Coroutines
    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    
    // Transaction broadcast queue
    private val pendingBroadcasts = mutableMapOf<String, PendingBroadcast>()
    
    // Max retry attempts
    private val maxRetries = 3
    
    // Broadcast delay between retries (ms)
    private val retryDelayMs = 5000
    
    init {
        initMeshService()
    }
    
    /**
     * Initialize mesh service
     */
    private fun initMeshService() {
        try {
            meshService = BluetoothMeshService(context)
            Log.d(TAG, "Mesh service initialized")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize mesh service", e)
        }
    }
    
    /**
     * Broadcast a transaction via mesh network
     * Uses Bluetooth LE for peer-to-peer transaction propagation
     */
    fun broadcastTransaction(
        currency: Currency,
        transactionHash: String,
        signedData: ByteArray,
        recipients: List<String>? = null
    ): String {
        val broadcastId = UUID.randomUUID().toString()
        val peerId = recipients?.firstOrNull() ?: "broadcast"
        
        Log.d(TAG, "Broadcasting transaction $transactionHash to $peerId (broadcastId: $broadcastId)")
        
        // Create transaction broadcast message
        val message = buildTransactionBroadcastMessage(
            currency = currency,
            transactionHash = transactionHash,
            signedData = signedData,
            broadcastId = broadcastId
        )
        
        // Add to pending broadcasts
        pendingBroadcasts[broadcastId] = PendingBroadcast(
            transactionHash = transactionHash,
            currency = currency,
            signedData = signedData,
            peerId = peerId,
            broadcastId = broadcastId,
            retryCount = 0
        )
        
        // Send via mesh
        sendTransactionBroadcastMessage(message, peerId)
        
        // Schedule retry if needed
        scheduleRetry(broadcastId)
        
        return broadcastId
    }
    
    /**
     * Build transaction broadcast message for IkoroMessage format
     */
    private fun buildTransactionBroadcastMessage(
        currency: Currency,
        transactionHash: String,
        signedData: ByteArray,
        broadcastId: String
    ): IkoroMessage {
        // Transaction broadcast payload format
        val payload = mapOf(
            "type" to "transaction_broadcast",
            "currency" to currency.name,
            "transaction_hash" to transactionHash,
            "signed_data_hex" to signedData.toHexString(),
            "broadcast_id" to broadcastId,
            "timestamp" to System.currentTimeMillis(),
            "broadcasters" to listOf("mesh")
        )
        
        return IkoroMessage(
            type = MessageType.TRANSACTION_BROADCAST,
            senderPeerID = "wallet-service",
            channel = null,
            content = payload,
            timestamp = System.currentTimeMillis()
        )
    }
    
    /**
     * Send transaction broadcast via mesh service
     */
    private fun sendTransactionBroadcastMessage(message: IkoroMessage, recipient: String) {
        serviceScope.launch {
            try {
                val mesh = meshService ?: return@launch
                
                // Check if recipient is a specific peer or broadcast
                if (recipient == "broadcast") {
                    // Broadcast to all peers
                    mesh.connectionManager.broadcastPacket(
                        com.ikoro.android.model.RoutedPacket(
                            com.ikoro.android.protocol.IkoroPacket(
                                version = 1u,
                                type = MessageType.TRANSACTION_BROADCAST.value,
                                senderID = byteArrayOf(),
                                recipientID = com.ikoro.android.protocol.SpecialRecipients.BROADCAST,
                                timestamp = System.currentTimeMillis().toULong(),
                                payload = message.content.toString().toByteArray(),
                                ttl = AppConstants.MESSAGE_TTL_HOPS
                            )
                        )
                    )
                    Log.d(TAG, "Broadcasted transaction to all peers")
                } else {
                    // Send to specific peer
                    mesh.connectionManager.sendPacketToPeer(
                        recipient,
                        com.ikoro.android.protocol.IkoroPacket(
                            version = 1u,
                            type = MessageType.TRANSACTION_BROADCAST.value,
                            senderID = byteArrayOf(),
                            recipientID = hexStringToByteArray(recipient),
                            timestamp = System.currentTimeMillis().toULong(),
                            payload = message.content.toString().toByteArray(),
                            ttl = AppConstants.MESSAGE_TTL_HOPS
                        )
                    )
                    Log.d(TAG, "Sent transaction to peer $recipient")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to send transaction broadcast", e)
            }
        }
    }
    
    /**
     * Schedule retry for failed broadcast
     */
    private fun scheduleRetry(broadcastId: String) {
        val pending = pendingBroadcasts[broadcastId] ?: return
        
        if (pending.retryCount < maxRetries) {
            serviceScope.launch {
                delay(retryDelayMs.toLong())
                retryBroadcast(broadcastId)
            }
        } else {
            Log.w(TAG, "Max retries exceeded for broadcast $broadcastId")
            pendingBroadcasts.remove(broadcastId)
        }
    }
    
    /**
     * Retry a failed broadcast
     */
    private fun retryBroadcast(broadcastId: String) {
        val pending = pendingBroadcasts[broadcastId] ?: return
        
        val newCount = pending.retryCount + 1
        Log.d(TAG, "Retrying broadcast $broadcastId (attempt $newCount/$maxRetries)")
        
        pendingBroadcasts[broadcastId] = pending.copy(retryCount = newCount)
        
        // Re-send via mesh
        sendTransactionBroadcastMessage(
            buildTransactionBroadcastMessage(
                currency = pending.currency,
                transactionHash = pending.transactionHash,
                signedData = pending.signedData,
                broadcastId = broadcastId
            ),
            pending.peerId
        )
    }
    
    /**
     * Cancel a pending broadcast
     */
    fun cancelBroadcast(broadcastId: String): Boolean {
        return pendingBroadcasts.remove(broadcastId) != null
    }
    
    /**
     * Get pending broadcast count
     */
    fun getPendingBroadcastCount(): Int {
        return pendingBroadcasts.size
    }
    
    /**
     * Get pending broadcasts for a currency
     */
    fun getPendingBroadcasts(currency: Currency): List<PendingBroadcast> {
        return pendingBroadcasts.values.filter { it.currency == currency }
    }
    
    /**
     * Cleanup resources
     */
    fun cleanup() {
        serviceScope.cancel()
        pendingBroadcasts.clear()
    }
    
    /**
     * Format byte array to hex string
     */
    private fun ByteArray.toHexString(): String {
        return joinToString("") { "%02x".format(it) }
    }
    
    /**
     * Format hex string to byte array
     */
    private fun hexStringToByteArray(hex: String): ByteArray {
        return hex.chunked(2).map { it.toInt(16).toByte() }.toByteArray()
    }
    
    /**
     * Status of a transaction broadcast
     */
    data class PendingBroadcast(
        val transactionHash: String,
        val currency: Currency,
        val signedData: ByteArray,
        val peerId: String,
        val broadcastId: String,
        var retryCount: Int
    ) {
        fun isComplete(): Boolean {
            return retryCount >= maxRetries
        }
    }
}
