package com.ikoro.android.nostr

import com.google.gson.Gson
import com.google.gson.JsonParser
import kotlinx.coroutines.*
import okhttp3.*
import java.util.concurrent.TimeUnit

/**
 * Minimal Nostr WebSocket client for relay publish/subscribe.
 */
class NostrWebSocketClient(
    private val relayUrls: List<String>,
    private val identityManager: com.ikoro.android.identity.IdentityManager
) {
    private val client = OkHttpClient.Builder()
        .pingInterval(30, TimeUnit.SECONDS)
        .build()
    private val gson = Gson()
    private val listeners = mutableListOf<NostrEventListener>()
    private val websockets = mutableMapOf<String, WebSocket>()
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    interface NostrEventListener {
        fun onEvent(event: NostrEvent)
    }

    data class NostrEvent(
        val id: String,
        val pubkey: String,
        val createdAt: Long,
        val kind: Int,
        val tags: List<List<String>>,
        val content: String,
        val sig: String
    )

    fun addListener(listener: NostrEventListener) {
        listeners.add(listener)
    }

    fun removeListener(listener: NostrEventListener) {
        listeners.remove(listener)
    }

    fun connect() {
        for (relay in relayUrls) {
            connectRelay(relay)
        }
    }

    private fun connectRelay(relay: String) {
        val request = Request.Builder().url(relay).build()
        val ws = client.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                val pubkey = identityManager.getNostrPublicKeyHex() ?: return
                val subId = "ikoro-${System.currentTimeMillis()}"
                val filter = mapOf(
                    "kinds" to listOf(4, 30001, 30002, 30003, 30004),
                    "#p" to listOf(pubkey),
                    "since" to System.currentTimeMillis() / 1000 - 86400
                )
                val msg = listOf("REQ", subId, filter)
                webSocket.send(gson.toJson(msg))
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                try {
                    val array = JsonParser.parseString(text).asJsonArray
                    if (array.size() >= 2 && array[0].asString == "EVENT") {
                        val eventJson = array[2].asJsonObject
                        val rawEvent = gson.fromJson(eventJson, NostrEvent::class.java)
                        val event = decryptIfNeeded(rawEvent)
                        listeners.forEach { it.onEvent(event) }
                    }
                } catch (_: Exception) { }
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                scope.launch {
                    delay(5000)
                    connectRelay(relay)
                }
            }
        })
        websockets[relay] = ws
    }

    fun publish(eventJson: String): Boolean {
        var anySuccess = false
        for ((_, ws) in websockets) {
            val msg = listOf("EVENT", JsonParser.parseString(eventJson))
            val sent = ws.send(gson.toJson(msg))
            if (sent) anySuccess = true
        }
        return anySuccess
    }

    fun close() {
        for ((_, ws) in websockets) {
            ws.cancel()
        }
        websockets.clear()
        scope.cancel()
    }

    private fun decryptIfNeeded(event: NostrEvent): NostrEvent {
        if (event.kind != NostrClient.KIND_ENCRYPTED_DM) return event
        val priv = identityManager.getNostrPrivateKey() ?: return event
        return try {
            val decrypted = NipUtils.decryptNip04(event.content, priv, event.pubkey)
            event.copy(content = decrypted)
        } catch (_: Exception) {
            event
        }
    }
}
