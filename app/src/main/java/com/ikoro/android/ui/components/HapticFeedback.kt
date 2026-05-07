package com.ikoro.android.ui.components

import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback

/**
 * Haptic Feedback Utilities
 * Provides convenient methods for haptic feedback with varying intensity and patterns
 */

/**
 * Types of haptic feedback
 */
enum class HapticFeedbackType {
    LIGHT_CLICK,          // Light tap for UI interactions
    MEDIUM_CLICK,         // Medium tap for confirmations
    HEAVY_CLICK,          // Heavy tap for important actions
    SUCCESS,              // Success feedback pattern
    ERROR,                // Error feedback pattern
    WARNING,              // Warning feedback pattern
    NOTIFICATION,         // Notification pulse
    TICK,                 // Subtle tick for progress
    LONG_PRESS,           // Long press feedback
    DOUBLE_CLICK,         // Double click feedback
    GESTURE_START,        // Gesture start feedback
    GESTURE_END           // Gesture end feedback
}

/**
 * Haptic Feedback Manager
 * Provides unified haptic feedback API with platform-specific optimizations
 */
class HapticFeedbackManager(private val context: android.content.Context) {

    private val vibrator: Vibrator? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val vibratorManager = context.getSystemService(android.content.Context.VIBRATOR_MANAGER_SERVICE)
                as VibratorManager
        vibratorManager.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        context.getSystemService(android.content.Context.VIBRATOR_SERVICE) as? Vibrator
    }

    /**
     * Check if device has a vibrator
     */
    fun hasVibrator(): Boolean {
        return vibrator?.hasVibrator() == true
    }

    /**
     * Perform haptic feedback with specified type
     */
    fun performFeedback(type: HapticFeedbackType) {
        if (!hasVibrator()) return

        when (type) {
            HapticFeedbackType.LIGHT_CLICK -> lightClick()
            HapticFeedbackType.MEDIUM_CLICK -> mediumClick()
            HapticFeedbackType.HEAVY_CLICK -> heavyClick()
            HapticFeedbackType.SUCCESS -> successPattern()
            HapticFeedbackType.ERROR -> errorPattern()
            HapticFeedbackType.WARNING -> warningPattern()
            HapticFeedbackType.NOTIFICATION -> notificationPulse()
            HapticFeedbackType.TICK -> tick()
            HapticFeedbackType.LONG_PRESS -> longPress()
            HapticFeedbackType.DOUBLE_CLICK -> doubleClick()
            HapticFeedbackType.GESTURE_START -> gestureStart()
            HapticFeedbackType.GESTURE_END -> gestureEnd()
        }
    }

    /**
     * Light click - subtle tap for UI interactions
     */
    private fun lightClick() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator?.vibrate(
                VibrationEffect.createOneShot(
                    10,
                    VibrationEffect.DEFAULT_AMPLITUDE
                )
            )
        } else {
            @Suppress("DEPRECATION")
            vibrator?.vibrate(10)
        }
    }

    /**
     * Medium click - standard tap for confirmations
     */
    private fun mediumClick() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator?.vibrate(
                VibrationEffect.createOneShot(
                    25,
                    VibrationEffect.DEFAULT_AMPLITUDE
                )
            )
        } else {
            @Suppress("DEPRECATION")
            vibrator?.vibrate(25)
        }
    }

    /**
     * Heavy click - strong tap for important actions
     */
    private fun heavyClick() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator?.vibrate(
                VibrationEffect.createOneShot(
                    50,
                    VibrationEffect.DEFAULT_AMPLITUDE
                )
            )
        } else {
            @Suppress("DEPRECATION")
            vibrator?.vibrate(50)
        }
    }

    /**
     * Success pattern - positive feedback
     */
    private fun successPattern() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val timings = longArrayOf(0, 30, 50, 30)
            val amplitudes = intArrayOf(0, 255, 0, 200)
            vibrator?.vibrate(
                VibrationEffect.createWaveform(timings, amplitudes, -1)
            )
        } else {
            @Suppress("DEPRECATION")
            vibrator?.vibrate(longArrayOf(0, 30, 50, 30), -1)
        }
    }

    /**
     * Error pattern - negative feedback
     */
    private fun errorPattern() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val timings = longArrayOf(0, 40, 40, 40, 40, 40)
            val amplitudes = intArrayOf(0, 200, 0, 200, 0, 200)
            vibrator?.vibrate(
                VibrationEffect.createWaveform(timings, amplitudes, -1)
            )
        } else {
            @Suppress("DEPRECATION")
            vibrator?.vibrate(longArrayOf(0, 40, 40, 40, 40, 40), -1)
        }
    }

    /**
     * Warning pattern - caution feedback
     */
    private fun warningPattern() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val timings = longArrayOf(0, 30, 30, 30)
            val amplitudes = intArrayOf(0, 255, 0, 150)
            vibrator?.vibrate(
                VibrationEffect.createWaveform(timings, amplitudes, -1)
            )
        } else {
            @Suppress("DEPRECATION")
            vibrator?.vibrate(longArrayOf(0, 30, 30, 30), -1)
        }
    }

    /**
     * Notification pulse - subtle notification
     */
    private fun notificationPulse() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val timings = longArrayOf(0, 20, 100, 20)
            val amplitudes = intArrayOf(0, 180, 0, 180)
            vibrator?.vibrate(
                VibrationEffect.createWaveform(timings, amplitudes, -1)
            )
        } else {
            @Suppress("DEPRECATION")
            vibrator?.vibrate(longArrayOf(0, 20, 100, 20), -1)
        }
    }

    /**
     * Tick - very subtle for progress
     */
    private fun tick() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator?.vibrate(
                VibrationEffect.createOneShot(
                    5,
                    VibrationEffect.DEFAULT_AMPLITUDE / 2
                )
            )
        } else {
            @Suppress("DEPRECATION")
            vibrator?.vibrate(5)
        }
    }

    /**
     * Long press - sustained vibration
     */
    private fun longPress() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator?.vibrate(
                VibrationEffect.createOneShot(
                    100,
                    VibrationEffect.DEFAULT_AMPLITUDE
                )
            )
        } else {
            @Suppress("DEPRECATION")
            vibrator?.vibrate(100)
        }
    }

    /**
     * Double click - two quick taps
     */
    private fun doubleClick() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val timings = longArrayOf(0, 20, 30, 20)
            val amplitudes = intArrayOf(0, 200, 0, 200)
            vibrator?.vibrate(
                VibrationEffect.createWaveform(timings, amplitudes, -1)
            )
        } else {
            @Suppress("DEPRECATION")
            vibrator?.vibrate(longArrayOf(0, 20, 30, 20), -1)
        }
    }

    /**
     * Gesture start - feedback when gesture begins
     */
    private fun gestureStart() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val timings = longArrayOf(0, 15, 15, 15)
            val amplitudes = intArrayOf(0, 150, 0, 100)
            vibrator?.vibrate(
                VibrationEffect.createWaveform(timings, amplitudes, -1)
            )
        } else {
            @Suppress("DEPRECATION")
            vibrator?.vibrate(longArrayOf(0, 15, 15, 15), -1)
        }
    }

    /**
     * Gesture end - feedback when gesture completes
     */
    private fun gestureEnd() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val timings = longArrayOf(0, 15, 15)
            val amplitudes = intArrayOf(0, 150, 0)
            vibrator?.vibrate(
                VibrationEffect.createWaveform(timings, amplitudes, -1)
            )
        } else {
            @Suppress("DEPRECATION")
            vibrator?.vibrate(longArrayOf(0, 15, 15), -1)
        }
    }

    /**
     * Custom vibration with specified pattern
     */
    fun customVibrate(timings: LongArray, repeat: Int = -1) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val amplitudes = IntArray(timings.size) { if (it == 0) 0 else 255 }
            vibrator?.vibrate(
                VibrationEffect.createWaveform(timings, amplitudes, repeat)
            )
        } else {
            @Suppress("DEPRECATION")
            vibrator?.vibrate(timings, repeat)
        }
    }
}

/**
 * Composable helper to access HapticFeedbackManager
 */
@Composable
fun rememberHapticFeedbackManager(): HapticFeedbackManager {
    val context = LocalContext.current
    return remember(context) { HapticFeedbackManager(context) }
}

/**
 * Extension function for convenient haptic feedback in Composables
 */
fun androidx.compose.ui.hapticfeedback.HapticFeedback.perform(
    type: HapticFeedbackType,
    manager: HapticFeedbackManager
) {
    manager.performFeedback(type)
}