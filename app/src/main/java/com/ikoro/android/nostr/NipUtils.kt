package com.ikoro.android.nostr

import android.content.Context
import com.ikoro.android.identity.NostrCrypto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.math.BigInteger
import java.security.MessageDigest
import java.security.SecureRandom
import java.util.Base64
import javax.crypto.Cipher
import javax.crypto.Mac
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

/**
 * NIP-05 verifier + NIP-04 direct message encryption helpers.
 */
object NipUtils {

    private val okHttp = OkHttpClient.Builder().build()

    /**
     * Resolve a NIP-05 identifier (user@domain) to a Nostr pubkey.
     */
    suspend fun resolveNip05(identifier: String): Result<String> = withContext(Dispatchers.IO) {
        try {
            val parts = identifier.trim().lowercase().split("@")
            if (parts.size != 2) return@withContext Result.failure(IllegalArgumentException("Invalid NIP-05"))
            val name = parts[0]
            val domain = parts[1]
            val url = "https://$domain/.well-known/nostr.json?name=$name"
            val request = Request.Builder().url(url).build()
            val response = okHttp.newCall(request).execute()
            val body = response.body?.string() ?: return@withContext Result.failure(IllegalStateException("Empty NIP-05 response"))
            val json = JSONObject(body)
            val names = json.optJSONObject("names")
            val pubkey = names?.optString(name)
                ?: return@withContext Result.failure(IllegalStateException("NIP-05 name not found"))
            Result.success(pubkey)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Fetch a Nostr profile (kind 0) from a given relay and extract a named field.
     */
    suspend fun fetchProfileField(
        relays: List<String>,
        pubkeyHex: String,
        field: String
    ): Result<String> = withContext(Dispatchers.IO) {
        for (relay in relays) {
            try {
                val client = okHttp.newBuilder().build()
                val request = Request.Builder().url(relay).build()
                val ws = client.newWebSocket(request, object : okhttp3.WebSocketListener() {
                    override fun onOpen(webSocket: okhttp3.WebSocket, response: okhttp3.Response) {
                        val filter = JSONObject().apply {
                            put("authors", listOf(pubkeyHex))
                            put("kinds", listOf(0))
                            put("limit", 1)
                        }
                        webSocket.send("""["REQ","profile",${filter}]""")
                    }

                    override fun onMessage(webSocket: okhttp3.WebSocket, text: String) {
                        // Handled below with timeout
                    }
                })
                // Simplification: use REST fallback if relay supports it
                return@withContext Result.failure(IllegalStateException("WebSocket fetch not implemented"))
            } catch (e: Exception) {
                // try next relay
            }
        }
        Result.failure(IllegalStateException("No profile found"))
    }

    /**
     * NIP-04 encrypt: shared secret = secp256k1 scalar multiplication.
     * This helper returns base64(cipher) + "?iv=" + base64(iv) using AES-256-CBC.
     */
    fun encryptNip04(
        plaintext: String,
        senderPrivkeyBytes: ByteArray,
        recipientPubkeyHex: String
    ): String {
        val shared = NostrCrypto.computeSharedSecretXOnly(senderPrivkeyBytes, recipientPubkeyHex)
        val iv = ByteArray(16).apply { SecureRandom().nextBytes(this) }
        val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
        cipher.init(Cipher.ENCRYPT_MODE, SecretKeySpec(shared, "AES"), IvParameterSpec(iv))
        val encrypted = cipher.doFinal(plaintext.toByteArray(Charsets.UTF_8))
        val b64 = android.util.Base64.encodeToString(encrypted, android.util.Base64.DEFAULT)
        val ivB64 = android.util.Base64.encodeToString(iv, android.util.Base64.DEFAULT)
        return "$b64?iv=$ivB64"
    }

    /**
     * NIP-04 decrypt.
     */
    fun decryptNip04(
        payload: String,
        recipientPrivkeyBytes: ByteArray,
        senderPubkeyHex: String
    ): String {
        val shared = NostrCrypto.computeSharedSecretXOnly(recipientPrivkeyBytes, senderPubkeyHex)
        val parts = payload.split("?iv=")
        val encrypted = android.util.Base64.decode(parts[0], android.util.Base64.DEFAULT)
        val iv = android.util.Base64.decode(parts[1], android.util.Base64.DEFAULT)
        val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
        cipher.init(Cipher.DECRYPT_MODE, SecretKeySpec(shared, "AES"), IvParameterSpec(iv))
        return String(cipher.doFinal(encrypted), Charsets.UTF_8)
    }

    /**
     * Compute a NIP-26 delegation token (not implemented).
     */
    fun createDelegationToken(): Nothing = throw NotImplementedError()
}
