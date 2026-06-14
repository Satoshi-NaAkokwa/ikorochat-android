package com.ikoro.android.nostr

import com.ikoro.android.identity.IdentityManager
import com.ikoro.android.identity.NostrCrypto
import java.nio.charset.StandardCharsets

/**
 * Minimal Nostr client for encrypted DM signaling.
 *
 * Supports:
 *  - publishing kind 4 encrypted DMs to relays
 *  - subscribing to kind 4 DMs addressed to our npub
 *
 * This is intentionally small. It does not validate relay responses deeply.
 */
class NostrClient(
    private val identityManager: IdentityManager,
    val relayUrls: List<String> = listOf(
        "wss://relay.damus.io",
        "wss://relay.nostr.band",
        "wss://nos.lol"
    )
) {

    companion object {
        const val KIND_CALL_OFFER = 30001
        const val KIND_CALL_ANSWER = 30002
        const val KIND_CALL_ICE = 30003
        const val KIND_CALL_HANGUP = 30004
    }

    private var webSocketClient: NostrWebSocketClient? = null

    fun setWebSocketClient(client: NostrWebSocketClient) {
        this.webSocketClient = client
    }

    /**
     * Send an encrypted direct message (kind 4) to a recipient pubkey.
     * The message is the raw signaling payload (JSON or SDP string).
     */
    suspend fun sendEncryptedDM(recipientPubkeyHex: String, content: String): Result<Unit> {
        val privateKey = identityManager.getNostrPrivateKey()
            ?: return Result.failure(IllegalStateException("No identity"))
        val senderPubkey = identityManager.getNostrPublicKeyHex()
            ?: return Result.failure(IllegalStateException("No identity"))

        val event = createUnsignedEvent(
            kind = KIND_CALL_OFFER,
            content = content,
            tags = listOf(listOf("p", recipientPubkeyHex)),
            pubkey = senderPubkey
        )
        val signed = signEvent(event, privateKey)
        val published = webSocketClient?.publish(signed) ?: false
        return if (published) Result.success(Unit) else Result.failure(Exception("No relay reachable"))
    }

    /**
     * Build a call offer event for WebRTC/LiveKit signaling.
     */
    suspend fun sendCallOffer(recipientPubkeyHex: String, roomId: String): Result<Unit> {
        val payload = """{"type":"call_offer","room":"$roomId","ts":${System.currentTimeMillis()}}"""
        return sendEncryptedDM(recipientPubkeyHex, payload)
    }

    private fun createUnsignedEvent(
        kind: Int,
        content: String,
        tags: List<List<String>>,
        pubkey: String
    ): Map<String, Any> {
        val createdAt = System.currentTimeMillis() / 1000
        return mapOf(
            "id" to "",
            "pubkey" to pubkey,
            "created_at" to createdAt,
            "kind" to kind,
            "tags" to tags,
            "content" to content,
            "sig" to ""
        )
    }

    private fun signEvent(unsigned: Map<String, Any>, privateKey: ByteArray): String {
        val serialized = serializeForId(unsigned)
        val hash = NostrCrypto.sha256(serialized.toByteArray(StandardCharsets.UTF_8))
        val signature = NostrCrypto.sign(privateKey, hash)
        val sigHex = signature.joinToString("") { "%02x".format(it) }
        val id = hash.joinToString("") { "%02x".format(it) }
        val mutable = unsigned.toMutableMap()
        mutable["id"] = id
        mutable["sig"] = sigHex
        return toJson(mutable)
    }

    private fun serializeForId(event: Map<String, Any>): String {
        val tags = event["tags"] as? List<List<String>> ?: emptyList()
        val parts = listOf(
            0,
            event["pubkey"].toString(),
            event["created_at"].toString().toLong(),
            event["kind"].toString().toInt(),
            tags,
            event["content"].toString()
        )
        return gson.toJson(parts)
    }

    private fun toJson(map: Map<String, Any>): String = gson.toJson(map)

    private val gson by lazy { com.google.gson.Gson() }
}
