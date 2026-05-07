package com.ikoro.android.audio

import java.io.File

/**
 * Data class representing an audio message in the Ikoro chat system.
 *
 * @property id Unique identifier for this audio message
 * @property file The audio file stored locally
 * @property durationMs Duration of the audio in milliseconds
 * @property fileSize Size of the audio file in bytes
 * @property bitrate Bitrate used for compression (64, 128, or 256 kbps)
 * @property timestamp When the audio was recorded/created
 * @property senderId Optional sender identifier (null for self-created messages)
 * @property waveform Optional waveform data for visualization
 */
data class AudioMessage(
    val id: String,
    val file: File,
    val durationMs: Long,
    val fileSize: Long,
    val bitrate: Bitrate,
    val timestamp: Long = System.currentTimeMillis(),
    val senderId: String? = null,
    val waveform: FloatArray? = null
) {
    enum class Bitrate(val kbps: Int, val displayName: String) {
        BITRATE_64(64, "64 kbps"),
        BITRATE_128(128, "128 kbps"),
        BITRATE_256(256, "256 kbps")
    }

    /**
     * Human-readable duration string (e.g., "2:34")
     */
    val durationFormatted: String
        get() {
            val seconds = (durationMs / 1000).toInt()
            val minutes = seconds / 60
            val remainingSeconds = seconds % 60
            return "%d:%02d".format(minutes, remainingSeconds)
        }

    /**
     * File size in human-readable format (e.g., "2.4 MB")
     */
    val fileSizeFormatted: String
        get() {
            val kb = fileSize / 1024.0
            return when {
                kb < 1024 -> "%.1f KB".format(kb)
                else -> "%.1f MB".format(kb / 1024)
            }
        }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as AudioMessage

        if (id != other.id) return false
        if (file != other.file) return false
        if (durationMs != other.durationMs) return false
        if (fileSize != other.fileSize) return false
        if (bitrate != other.bitrate) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + file.hashCode()
        result = 31 * result + durationMs.hashCode()
        result = 31 * result + fileSize.hashCode()
        result = 31 * result + bitrate.hashCode()
        return result
    }
}
