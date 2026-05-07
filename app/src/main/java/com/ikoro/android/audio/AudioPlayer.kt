package com.ikoro.android.audio

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.Binder
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * AudioPlayer handles audio playback with speed control, background support,
 * and progress tracking.
 */
class AudioPlayer(private val context: Context) {
    // Playback states
    sealed class PlaybackState {
        object Idle : PlaybackState()
        data class Loading(val audioMessage: AudioMessage) : PlaybackState()
        data class Playing(
            val audioMessage: AudioMessage,
            val positionMs: Long,
            val speed: Float
        ) : PlaybackState()
        data class Paused(
            val audioMessage: AudioMessage,
            val positionMs: Long,
            val speed: Float
        ) : PlaybackState()
        data class Completed(
            val audioMessage: AudioMessage,
            val speed: Float
        ) : PlaybackState()
        data class Error(val message: String) : PlaybackState()
    }

    private val _playbackState = MutableStateFlow<PlaybackState>(PlaybackState.Idle)
    val playbackState: StateFlow<PlaybackState> = _playbackState.asStateFlow()

    private val _playbackProgress = MutableStateFlow(0f) // 0.0 to 1.0
    val playbackProgress: StateFlow<Float> = _playbackProgress.asStateFlow()

    private val _currentPosition = MutableStateFlow(0L)
    val currentPosition: StateFlow<Long> = _currentPosition.asStateFlow()

    private val _playbackSpeed = MutableStateFlow(1.0f)
    val playbackSpeed: StateFlow<Float> = _playbackSpeed.asStateFlow()

    private var mediaPlayer: MediaPlayer? = null
    private var currentAudioMessage: AudioMessage? = null
    private var audioManager: AudioManager? = null
    private var audioFocusRequest: AudioFocusRequest? = null
    private var isBackgroundEnabled = false

    // Playback speeds
    val availableSpeeds = listOf(0.5f, 1.0f, 1.5f, 2.0f)

    companion object {
        private const val NOTIFICATION_ID = 1001
        private const val CHANNEL_ID = "audio_playback_channel"
        private const val ACTION_PLAY_PAUSE = "com.ikoro.audio.PLAY_PAUSE"
        private const val ACTION_STOP = "com.ikoro.audio.STOP"
        private const val ACTION_SPEED_UP = "com.ikoro.audio.SPEED_UP"
        private const val ACTION_SPEED_DOWN = "com.ikoro.audio.SPEED_DOWN"
    }

    init {
        audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        setupAudioFocus()
    }

    /**
     * Enable or disable background playback with notification.
     */
    fun enableBackgroundPlayback(enabled: Boolean) {
        isBackgroundEnabled = enabled
        if (enabled) {
            createNotificationChannel()
        }
    }

    /**
     * Load and prepare an audio message for playback.
     *
     * @param audioMessage The audio message to play
     * @param startPositionMs Optional start position in milliseconds
     * @return true if loaded successfully, false otherwise
     */
    fun loadAudio(audioMessage: AudioMessage, startPositionMs: Long = 0): Boolean {
        stopPlayback()

        try {
            _playbackState.value = PlaybackState.Loading(audioMessage)

            mediaPlayer = MediaPlayer().apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .build()
                )

                setDataSource(audioMessage.file.absolutePath)
                setOnPreparedListener {
                    _playbackState.value = PlaybackState.Paused(
                        audioMessage,
                        startPositionMs,
                        _playbackSpeed.value
                    )
                    seekTo(startPositionMs.toInt())
                    updateProgress()
                }

                setOnCompletionListener {
                    _playbackState.value = PlaybackState.Completed(audioMessage, _playbackSpeed.value)
                    _playbackProgress.value = 1.0f
                    _currentPosition.value = audioMessage.durationMs
                    if (isBackgroundEnabled) {
                        hideNotification()
                    }
                }

                setOnErrorListener { _, what, extra ->
                    _playbackState.value = PlaybackState.Error("Playback error: $what, $extra")
                    false
                }

                setOnSeekCompleteListener {
                    updateProgress()
                }

                prepareAsync()
            }

            currentAudioMessage = audioMessage
            return true

        } catch (e: Exception) {
            e.printStackTrace()
            _playbackState.value = PlaybackState.Error("Failed to load audio: ${e.message}")
            return false
        }
    }

    /**
     * Start playback from the current position or specified position.
     *
     * @param positionMs Optional position to start from
     * @return true if playback started, false otherwise
     */
    fun play(positionMs: Long? = null): Boolean {
        val currentState = _playbackState.value
        val audioMessage = when (currentState) {
            is PlaybackState.Paused -> currentState.audioMessage
            is PlaybackState.Playing -> currentState.audioMessage
            is PlaybackState.Loading -> {
                // Wait for loading to complete
                return false
            }
            else -> return false
        }

        if (!requestAudioFocus()) {
            return false
        }

        try {
            mediaPlayer?.let { player ->
                positionMs?.let { player.seekTo(it.toInt()) }
                player.start()

                _playbackState.value = PlaybackState.Playing(
                    audioMessage,
                    positionMs ?: _currentPosition.value,
                    _playbackSpeed.value
                )

                if (isBackgroundEnabled) {
                    showNotification(audioMessage, true)
                }

                startProgressUpdates()

                return true
            }
            return false

        } catch (e: Exception) {
            e.printStackTrace()
            _playbackState.value = PlaybackState.Error("Failed to play: ${e.message}")
            return false
        }
    }

    /**
     * Pause the current playback.
     *
     * @return true if paused successfully, false otherwise
     */
    fun pause(): Boolean {
        val currentState = _playbackState.value
        if (currentState !is PlaybackState.Playing) {
            return false
        }

        try {
            mediaPlayer?.pause()

            _playbackState.value = PlaybackState.Paused(
                currentState.audioMessage,
                currentState.positionMs,
                currentState.speed
            )

            if (isBackgroundEnabled) {
                showNotification(currentState.audioMessage, false)
            }

            return true

        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }
    }

    /**
     * Stop the current playback and reset.
     */
    fun stopPlayback() {
        try {
            mediaPlayer?.stop()
            mediaPlayer?.reset()
            mediaPlayer?.release()
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            mediaPlayer = null
            currentAudioMessage = null
            _playbackState.value = PlaybackState.Idle
            _playbackProgress.value = 0f
            _currentPosition.value = 0L

            if (isBackgroundEnabled) {
                hideNotification()
            }

            abandonAudioFocus()
        }
    }

    /**
     * Seek to a specific position in the audio.
     *
     * @param positionMs Position in milliseconds
     * @return true if seek initiated successfully, false otherwise
     */
    fun seekTo(positionMs: Long): Boolean {
        try {
            mediaPlayer?.seekTo(positionMs.toInt())
            return true
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }
    }

    /**
     * Set playback speed.
     *
     * @param speed Playback speed multiplier (0.5x, 1x, 1.5x, 2x)
     * @return true if speed set successfully, false otherwise
     */
    fun setPlaybackSpeed(speed: Float): Boolean {
        if (speed !in availableSpeeds) {
            return false
        }

        return try {
            mediaPlayer?.playbackParams?.let { params ->
                val newParams = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    params.setSpeed(speed)
                } else {
                    params
                }
                mediaPlayer?.playbackParams = newParams
                _playbackSpeed.value = speed

                // Update state with new speed
                val currentState = _playbackState.value
                when (currentState) {
                    is PlaybackState.Playing -> {
                        _playbackState.value = currentState.copy(speed = speed)
                    }
                    is PlaybackState.Paused -> {
                        _playbackState.value = currentState.copy(speed = speed)
                    }
                    else -> {}
                }

                true
            } ?: false

        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    /**
     * Increase playback speed to the next available speed.
     */
    fun increaseSpeed() {
        val currentSpeed = _playbackSpeed.value
        val currentIndex = availableSpeeds.indexOf(currentSpeed)
        if (currentIndex < availableSpeeds.size - 1) {
            setPlaybackSpeed(availableSpeeds[currentIndex + 1])
        }
    }

    /**
     * Decrease playback speed to the previous available speed.
     */
    fun decreaseSpeed() {
        val currentSpeed = _playbackSpeed.value
        val currentIndex = availableSpeeds.indexOf(currentSpeed)
        if (currentIndex > 0) {
            setPlaybackSpeed(availableSpeeds[currentIndex - 1])
        }
    }

    /**
     * Get the current playback speed multiplier.
     */
    fun getCurrentSpeed(): Float = _playbackSpeed.value

    /**
     * Get the current audio message being played.
     */
    fun getCurrentAudioMessage(): AudioMessage? = currentAudioMessage

    /**
     * Check if currently playing.
     */
    fun isPlaying(): Boolean {
        return _playbackState.value is PlaybackState.Playing
    }

    /**
     * Check if currently paused.
     */
    fun isPaused(): Boolean {
        return _playbackState.value is PlaybackState.Paused
    }

    /**
     * Check if an audio is loaded.
     */
    fun isLoaded(): Boolean {
        return _playbackState.value is PlaybackState.Playing ||
               _playbackState.value is PlaybackState.Paused
    }

    private fun requestAudioFocus(): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            audioFocusRequest = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
                .setOnAudioFocusChangeListener(audioFocusChangeListener)
                .build()

            return audioManager?.requestAudioFocus(audioFocusRequest!!) ==
                   AudioManager.AUDIOFOCUS_REQUEST_GRANTED
        } else {
            @Suppress("DEPRECATION")
            return audioManager?.requestAudioFocus(
                audioFocusChangeListener,
                AudioManager.STREAM_MUSIC,
                AudioManager.AUDIOFOCUS_GAIN
            ) == AudioManager.AUDIOFOCUS_REQUEST_GRANTED
        }
    }

    private fun abandonAudioFocus() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            audioFocusRequest?.let {
                audioManager?.abandonAudioFocusRequest(it)
            }
        } else {
            @Suppress("DEPRECATION")
            audioManager?.abandonAudioFocus(audioFocusChangeListener)
        }
    }

    private val audioFocusChangeListener = AudioManager.OnAudioFocusChangeListener { focusChange ->
        when (focusChange) {
            AudioManager.AUDIOFOCUS_LOSS -> {
                pause()
            }
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT -> {
                pause()
            }
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK -> {
                // Optionally reduce volume
            }
            AudioManager.AUDIOFOCUS_GAIN -> {
                // Optionally resume if paused due to transient loss
            }
        }
    }

    private fun startProgressUpdates() {
        // In a real implementation, this would use a coroutine or timer
        // to periodically update playbackProgress and currentPosition
    }

    private fun updateProgress() {
        mediaPlayer?.let { player ->
            val duration = player.duration
            val position = player.currentPosition

            if (duration > 0) {
                _playbackProgress.value = position.toFloat() / duration.toFloat()
            }
            _currentPosition.value = position.toLong()
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Audio Playback",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Audio playback controls"
                setShowBadge(false)
            }

            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE)
                as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun showNotification(audioMessage: AudioMessage, isPlaying: Boolean) {
        // Notification implementation would go here
        // This would show a persistent notification with play/pause controls
        // when playback is enabled in background
    }

    private fun hideNotification() {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE)
            as NotificationManager
        notificationManager.cancel(NOTIFICATION_ID)
    }

    fun cleanup() {
        stopPlayback()
    }
}
