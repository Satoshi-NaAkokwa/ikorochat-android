package com.ikoro.android.audio

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.media.MediaRecorder
import android.os.Build
import androidx.core.content.ContextCompat
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.File
import java.io.IOException
import java.util.UUID

/**
 * AudioRecorder handles audio recording with pause/resume functionality,
 * configurable compression, and multi-minute recording support (up to 10 minutes).
 */
class AudioRecorder(
    private val context: Context
) {
    // Recording states
    sealed class RecordingState {
        object Idle : RecordingState()
        data class Recording(
            val startTime: Long,
            val elapsedMs: Long,
            val currentBitrate: AudioMessage.Bitrate
        ) : RecordingState()
        data class Paused(val elapsedMs: Long) : RecordingState()
    }

    private val _recordingState = MutableStateFlow<RecordingState>(RecordingState.Idle)
    val recordingState: StateFlow<RecordingState> = _recordingState.asStateFlow()

    private val _recordingTime = MutableStateFlow(0L)
    val recordingTime: StateFlow<Long> = _recordingTime.asStateFlow()

    private val _recordingLevel = MutableStateFlow(0f)
    val recordingLevel: StateFlow<Float> = _recordingLevel.asStateFlow()

    private var mediaRecorder: MediaRecorder? = null
    private var currentFile: File? = null
    private var recordingStartTime: Long = 0
    private var pauseTime: Long = 0
    private var totalPausedDuration: Long = 0
    private var currentBitrate: AudioMessage.Bitrate = AudioMessage.Bitrate.BITRATE_128

    private val maxRecordingDurationMs = 10 * 60 * 1000L // 10 minutes
    private val updateIntervalMs = 100L // Update every 100ms

    init {
        checkPermissions()
    }

    private fun checkPermissions(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.RECORD_AUDIO
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.RECORD_AUDIO
            ) == PackageManager.PERMISSION_GRANTED
        }
    }

    fun hasPermission(): Boolean = checkPermissions()

    fun setBitrate(bitrate: AudioMessage.Bitrate) {
        currentBitrate = bitrate
    }

    /**
     * Start a new recording session.
     *
     * @param outputFile Optional file path. If null, a temporary file is created.
     * @param bitrate Compression bitrate (default: 128 kbps)
     * @return true if recording started successfully, false otherwise
     */
    fun startRecording(
        outputFile: File? = null,
        bitrate: AudioMessage.Bitrate = AudioMessage.Bitrate.BITRATE_128
    ): Boolean {
        if (!checkPermissions()) {
            return false
        }

        if (_recordingState.value is RecordingState.Recording) {
            return false // Already recording
        }

        // Clean up previous recording if needed
        stopRecordingInternal()

        try {
            currentBitrate = bitrate
            currentFile = outputFile ?: createTempFile()

            mediaRecorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                MediaRecorder(context)
            } else {
                @Suppress("DEPRECATION")
                MediaRecorder()
            }

            mediaRecorder?.apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setOutputFile(currentFile?.absolutePath)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                setAudioEncodingBitRate(bitrate.kbps * 1000)
                setAudioSamplingRate(44100)
                setAudioChannels(1) // Mono is sufficient for voice

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    setAudioMaxDurationMs(maxRecordingDurationMs.toInt())
                }

                prepare()
                start()
            }

            recordingStartTime = System.currentTimeMillis()
            pauseTime = 0
            totalPausedDuration = 0

            _recordingState.value = RecordingState.Recording(
                startTime = recordingStartTime,
                elapsedMs = 0,
                currentBitrate = currentBitrate
            )

            // Start time updates
            startTimeUpdates()

            return true

        } catch (e: IOException) {
            e.printStackTrace()
            stopRecordingInternal()
            return false
        } catch (e: IllegalStateException) {
            e.printStackTrace()
            stopRecordingInternal()
            return false
        }
    }

    /**
     * Pause the current recording.
     *
     * @return true if paused successfully, false if not recording
     */
    fun pauseRecording(): Boolean {
        val currentState = _recordingState.value
        if (currentState !is RecordingState.Recording) {
            return false
        }

        try {
            mediaRecorder?.pause()
            pauseTime = System.currentTimeMillis()

            _recordingState.value = RecordingState.Paused(currentState.elapsedMs)

            return true
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }
    }

    /**
     * Resume a paused recording.
     *
     * @return true if resumed successfully, false if not paused
     */
    fun resumeRecording(): Boolean {
        val currentState = _recordingState.value
        if (currentState !is RecordingState.Paused) {
            return false
        }

        try {
            mediaRecorder?.resume()
            val pauseDuration = System.currentTimeMillis() - pauseTime
            totalPausedDuration += pauseDuration

            val elapsedMs = currentState.elapsedMs + pauseDuration

            _recordingState.value = RecordingState.Recording(
                startTime = recordingStartTime,
                elapsedMs = elapsedMs,
                currentBitrate = currentBitrate
            )

            return true
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }
    }

    /**
     * Stop the current recording and return the audio message.
     *
     * @return AudioMessage if recording stopped successfully, null otherwise
     */
    fun stopRecording(): AudioMessage? {
        val currentState = _recordingState.value
        if (currentState !is RecordingState.Recording && currentState !is RecordingState.Paused) {
            return null
        }

        val elapsedMs = when (currentState) {
            is RecordingState.Recording -> currentState.elapsedMs
            is RecordingState.Paused -> currentState.elapsedMs
            else -> 0L
        }

        stopRecordingInternal()

        val file = currentFile ?: return null
        if (!file.exists()) {
            return null
        }

        return AudioMessage(
            id = UUID.randomUUID().toString(),
            file = file,
            durationMs = elapsedMs,
            fileSize = file.length(),
            bitrate = currentBitrate,
            timestamp = recordingStartTime
        )
    }

    /**
     * Cancel the current recording and delete the file.
     */
    fun cancelRecording() {
        stopRecordingInternal()

        currentFile?.let { file ->
            if (file.exists()) {
                file.delete()
            }
        }

        currentFile = null
        _recordingState.value = RecordingState.Idle
        _recordingTime.value = 0L
        _recordingLevel.value = 0f
    }

    /**
     * Get the current recording progress (0.0 to 1.0).
     */
    fun getProgress(): Float {
        return when (val state = _recordingState.value) {
            is RecordingState.Recording -> {
                (state.elapsedMs.toFloat() / maxRecordingDurationMs.toFloat()).coerceAtMost(1.0f)
            }
            is RecordingState.Paused -> {
                (state.elapsedMs.toFloat() / maxRecordingDurationMs.toFloat()).coerceAtMost(1.0f)
            }
            else -> 0f
        }
    }

    /**
     * Check if recording can continue (not exceeded max duration).
     */
    fun canContinueRecording(): Boolean {
        return when (val state = _recordingState.value) {
            is RecordingState.Recording -> state.elapsedMs < maxRecordingDurationMs
            is RecordingState.Paused -> state.elapsedMs < maxRecordingDurationMs
            else -> true
        }
    }

    private fun stopRecordingInternal() {
        try {
            mediaRecorder?.stop()
            mediaRecorder?.release()
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            mediaRecorder = null
        }

        _recordingState.value = RecordingState.Idle
        _recordingTime.value = 0L
        _recordingLevel.value = 0f
    }

    private fun createTempFile(): File {
        val cacheDir = context.cacheDir
        val audioDir = File(cacheDir, "audio_recordings")
        if (!audioDir.exists()) {
            audioDir.mkdirs()
        }
        return File(audioDir, "recording_${System.currentTimeMillis()}.m4a")
    }

    private fun startTimeUpdates() {
        // In a real implementation, this would use a coroutine or timer
        // For now, this is a simplified version
        // The actual implementation would update recordingTime periodically
    }

    fun cleanup() {
        stopRecordingInternal()
    }
}
