package com.ikoro.android.audio

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

/**
 * Custom View for displaying audio waveforms with interactive playback controls.
 *
 * Features:
 * - Visual waveform display from audio amplitude data
 * - Real-time audio visualization during recording/playback
 * - Playback position indicator with scrubbing support
 * - Interactive controls (tap to seek, scrub to navigate)
 */
class WaveformView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    // Configuration
    private val waveformColor = Color.parseColor("#FF6B35")
    private val waveformBackgroundColor = Color.parseColor("#F5F5F5")
    private val playbackLineColor = Color.parseColor("#4CAF50")
    private val playbackHeadColor = Color.parseColor("#2196F3")

    private val waveformPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = waveformColor
        style = Paint.Style.STROKE
        strokeWidth = 2f
    }

    private val backgroundPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = waveformBackgroundColor
        style = Paint.Style.FILL
    }

    private val playbackLinePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = playbackLineColor
        style = Paint.Style.STROKE
        strokeWidth = 3f
    }

    private val playbackHeadPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = playbackHeadColor
        style = Paint.Style.FILL
    }

    // Waveform data
    private var waveformSamples: FloatArray? = null
    private var maxAmplitude: Float = 1f

    // Playback state
    private var playbackPosition: Float = 0f // 0.0 to 1.0
    private var isPlaying: Boolean = false

    // Interaction
    private var isScrubbing: Boolean = false
    private var onSeekListener: ((Float) -> Unit)? = null
    private var onPlaybackToggleListener: (() -> Unit)? = null

    // Layout
    private var waveformPath: Path = Path()
    private var displayWidth: Int = 0
    private var displayHeight: Int = 0

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        displayWidth = w
        displayHeight = h
        rebuildWaveformPath()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        // Draw background
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), backgroundPaint)

        // Draw waveform
        if (waveformSamples != null && waveformSamples!!.isNotEmpty()) {
            canvas.drawPath(waveformPath, waveformPaint)
        }

        // Draw playback line
        val playbackX = width * playbackPosition
        canvas.drawLine(
            playbackX,
            0f,
            playbackX,
            height.toFloat(),
            playbackLinePaint
        )

        // Draw playback head
        val centerY = height / 2f
        canvas.drawCircle(playbackX, centerY, 6f, playbackHeadPaint)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                isScrubbing = true
                updatePlaybackPositionFromTouch(event.x)
                return true
            }
            MotionEvent.ACTION_MOVE -> {
                if (isScrubbing) {
                    updatePlaybackPositionFromTouch(event.x)
                }
                return true
            }
            MotionEvent.ACTION_UP -> {
                if (isScrubbing) {
                    updatePlaybackPositionFromTouch(event.x)
                    isScrubbing = false
                    onSeekListener?.invoke(playbackPosition)
                }
                return true
            }
        }
        return super.onTouchEvent(event)
    }

    private fun updatePlaybackPositionFromTouch(x: Float) {
        playbackPosition = (x / width).coerceIn(0f, 1f)
        invalidate()
    }

    private fun rebuildWaveformPath() {
        if (waveformSamples == null || waveformSamples!!.isEmpty()) {
            waveformPath.reset()
            return
        }

        waveformPath.reset()

        val samples = waveformSamples!!
        val sampleCount = samples.size

        if (sampleCount == 0) return

        val centerY = height / 2f
        val stepX = width.toFloat() / sampleCount.toFloat()
        val scaleY = (height / 2f) * 0.9f // Leave some padding

        waveformPath.moveTo(0f, centerY)

        for (i in samples.indices) {
            val x = i * stepX
            val amplitude = samples[i] / maxAmplitude
            val y = centerY + (amplitude * scaleY)
            waveformPath.lineTo(x, y)
        }
    }

    /**
     * Set waveform data from audio samples.
     *
     * @param samples Array of amplitude values (typically 0.0 to 1.0)
     */
    fun setWaveformSamples(samples: FloatArray) {
        this.waveformSamples = samples

        // Find maximum amplitude for normalization
        maxAmplitude = samples.maxOrNull()?.let { if (it > 0f) it else 1f } ?: 1f

        rebuildWaveformPath()
        invalidate()
    }

    /**
     * Update waveform in real-time during recording.
     *
     * @param amplitude Current audio amplitude (0.0 to 1.0)
     * @param currentTimeMs Current recording time in milliseconds
     * @param durationMs Total expected duration in milliseconds
     */
    fun updateRealtimeWaveform(amplitude: Float, currentTimeMs: Long, durationMs: Long) {
        if (waveformSamples == null) {
            // Initialize with expected sample count
            val sampleCount = min(1000, durationMs / 100) // 1 sample per 100ms max
            waveformSamples = FloatArray(sampleCount)
        }

        val samples = waveformSamples!!
        val currentIndex = ((currentTimeMs.toFloat() / durationMs.toFloat()) * samples.size).toInt()
            .coerceIn(0, samples.size - 1)

        samples[currentIndex] = max(samples[currentIndex], amplitude)
        maxAmplitude = max(maxAmplitude, amplitude)

        rebuildWaveformPath()
        invalidate()
    }

    /**
     * Set the playback position (0.0 to 1.0).
     */
    fun setPlaybackPosition(position: Float) {
        this.playbackPosition = position.coerceIn(0f, 1f)
        invalidate()
    }

    /**
     * Set the playing state.
     */
    fun setPlaying(playing: Boolean) {
        this.isPlaying = playing
        // Could change playback head appearance based on state
        invalidate()
    }

    /**
     * Set listener for seek events.
     */
    fun setOnSeekListener(listener: (Float) -> Unit) {
        this.onSeekListener = listener
    }

    /**
     * Set listener for playback toggle events (double-tap).
     */
    fun setOnPlaybackToggleListener(listener: () -> Unit) {
        this.onPlaybackToggleListener = listener
    }

    /**
     * Clear the waveform.
     */
    fun clearWaveform() {
        this.waveformSamples = null
        this.playbackPosition = 0f
        this.isPlaying = false
        waveformPath.reset()
        invalidate()
    }
}

/**
 * Compose wrapper for WaveformView.
 */
@Composable
fun WaveformViewCompose(
    waveformSamples: FloatArray? = null,
    playbackPosition: Float = 0f,
    isPlaying: Boolean = false,
    onSeek: ((Float) -> Unit)? = null,
    modifier: Modifier = Modifier,
    height: androidx.compose.ui.unit.Dp = 80.dp
) {
    val density = LocalDensity.current

    androidx.compose.foundation.Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .pointerInput(Unit) {
                detectTapGestures { offset ->
                    val relativeX = offset.x / size.width
                    onSeek?.invoke(relativeX.coerceIn(0f, 1f))
                }
            }
    ) {
        val width = size.width
        val height = size.height

        // Background
        drawRect(
            color = androidx.compose.ui.graphics.Color(android.graphics.Color.parseColor("#F5F5F5")),
            size = size
        )

        // Waveform
        waveformSamples?.let { samples ->
            val path = Path().apply {
                val maxAmp = samples.maxOrNull()?.let { if (it > 0f) it else 1f } ?: 1f
                val stepX = width / samples.size
                val centerY = height / 2
                val scaleY = (height / 2) * 0.9f

                moveTo(0f, centerY)

                for (i in samples.indices) {
                    val x = i * stepX
                    val amplitude = samples[i] / maxAmp
                    val y = centerY + (amplitude * scaleY)
                    lineTo(x, y)
                }
            }

            drawPath(
                path = path,
                color = androidx.compose.ui.graphics.Color(android.graphics.Color.parseColor("#FF6B35")),
                style = Stroke(width = 2.dp.toPx())
            )
        }

        // Playback line
        val playbackX = width * playbackPosition
        drawLine(
            color = androidx.compose.ui.graphics.Color(android.graphics.Color.parseColor("#4CAF50")),
            start = Offset(playbackX, 0f),
            end = Offset(playbackX, height),
            strokeWidth = 3.dp.toPx()
        )

        // Playback head
        drawCircle(
            color = androidx.compose.ui.graphics.Color(android.graphics.Color.parseColor("#2196F3")),
            radius = 6.dp.toPx(),
            center = Offset(playbackX, height / 2)
        )
    }
}
