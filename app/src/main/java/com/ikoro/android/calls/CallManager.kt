package com.ikoro.android.calls

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import com.ikoro.android.identity.IdentityManager
import com.ikoro.android.nostr.NostrClient
import io.livekit.android.LiveKit
import io.livekit.android.room.Room
import io.livekit.android.room.track.LocalAudioTrack
import io.livekit.android.room.track.LocalVideoTrack
import io.livekit.android.room.track.Track
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.util.UUID

/**
 * Minimal LiveKit call manager.
 *
 * Handles audio and video calls with a remote peer via a self-hosted LiveKit server.
 * Signaling (call offer/answer) goes through Nostr DMs.
 */
class CallManager(
    private val context: Context,
    private val identityManager: IdentityManager,
    private val nostrClient: NostrClient
) {
    private var room: Room? = null
    private var localAudioTrack: LocalAudioTrack? = null
    private var localVideoTrack: LocalVideoTrack? = null

    private val _callState = MutableStateFlow<CallState>(CallState.Idle)
    val callState: StateFlow<CallState> = _callState

    private val _error = MutableStateFlow("")
    val error: StateFlow<String> = _error

    suspend fun startAudioCall(recipientNpubHex: String, serverUrl: String, token: String) {
        if (!hasAudioPermission(context)) {
            _error.value = "Microphone permission required"
            return
        }
        val roomId = "ikoro-${UUID.randomUUID()}"
        nostrClient.sendCallOffer(recipientNpubHex, roomId)
        connectToRoom(serverUrl, token, video = false)
    }

    suspend fun acceptAudioCall(serverUrl: String, token: String, roomId: String) {
        if (!hasAudioPermission(context)) {
            _error.value = "Microphone permission required"
            return
        }
        connectToRoom(serverUrl, token, video = false)
    }

    suspend fun startVideoCall(recipientNpubHex: String, serverUrl: String, token: String) {
        if (!hasAudioPermission(context) || !hasCameraPermission(context)) {
            _error.value = "Microphone and camera permissions required"
            return
        }
        val roomId = "ikoro-${UUID.randomUUID()}"
        nostrClient.sendCallOffer(recipientNpubHex, roomId)
        connectToRoom(serverUrl, token, video = true)
    }

    suspend fun acceptVideoCall(serverUrl: String, token: String, roomId: String) {
        if (!hasAudioPermission(context) || !hasCameraPermission(context)) {
            _error.value = "Microphone and camera permissions required"
            return
        }
        connectToRoom(serverUrl, token, video = true)
    }

    fun hangUp() {
        localAudioTrack?.stop()
        localAudioTrack = null
        localVideoTrack?.stop()
        localVideoTrack = null
        room?.disconnect()
        room = null
        _callState.value = CallState.Idle
    }

    fun toggleMute(muted: Boolean) {
        localAudioTrack?.enabled = !muted
    }

    fun toggleVideo(enabled: Boolean) {
        localVideoTrack?.enabled = enabled
    }

    private suspend fun connectToRoom(serverUrl: String, token: String, video: Boolean) {
        _callState.value = CallState.Connecting
        try {
            val newRoom = LiveKit.create(context)
            newRoom.connect(serverUrl, token)
            room = newRoom

            localAudioTrack = newRoom.localParticipant.createAudioTrack()
            localAudioTrack?.let { newRoom.localParticipant.publishAudioTrack(it) }

            if (video) {
                localVideoTrack = newRoom.localParticipant.createVideoTrack()
                localVideoTrack?.let { newRoom.localParticipant.publishVideoTrack(it) }
            }

            _callState.value = if (video) CallState.InVideoCall else CallState.InAudioCall
        } catch (e: Exception) {
            _error.value = e.message ?: "Call connection failed"
            _callState.value = CallState.Error
        }
    }

    private fun hasAudioPermission(context: Context): Boolean {
        return ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
    }

    private fun hasCameraPermission(context: Context): Boolean {
        return ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
    }
}

sealed class CallState {
    object Idle : CallState()
    object Connecting : CallState()
    object Ringing : CallState()
    object InAudioCall : CallState()
    object InVideoCall : CallState()
    object Error : CallState()
}
