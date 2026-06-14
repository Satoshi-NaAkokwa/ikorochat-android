package com.ikoro.android.calls

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.nio.charset.StandardCharsets

/**
 * Fetches a signed LiveKit token from the self-hosted token endpoint.
 */
object LiveKitTokenClient {

    private const val TOKEN_URL = "https://livekit.ugogbe.info/livekit/token"

    data class TokenResponse(
        val serverUrl: String,
        val token: String
    )

    suspend fun fetchToken(room: String, identity: String): Result<TokenResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val url = URL(TOKEN_URL)
                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "POST"
                connection.doOutput = true
                connection.setRequestProperty("Content-Type", "application/json")
                connection.connectTimeout = 15_000
                connection.readTimeout = 15_000

                val payload = JSONObject().apply {
                    put("room", room)
                    put("identity", identity)
                }.toString()

                connection.outputStream.use { os ->
                    os.write(payload.toByteArray(StandardCharsets.UTF_8))
                    os.flush()
                }

                val responseCode = connection.responseCode
                val inputStream = if (responseCode in 200..299) connection.inputStream else connection.errorStream
                val response = inputStream.bufferedReader(StandardCharsets.UTF_8).use { it.readText() }

                if (responseCode !in 200..299) {
                    return@withContext Result.failure(Exception("Token endpoint error $responseCode: $response"))
                }

                val json = JSONObject(response)
                val serverUrl = json.getString("serverUrl")
                val token = json.getString("token")
                Result.success(TokenResponse(serverUrl, token))
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
}
