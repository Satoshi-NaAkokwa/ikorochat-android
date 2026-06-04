package com.ikoro.android.util

import android.content.Context
import android.util.Log
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

/**
 * Error Logger - Local-only crash logging without external dependencies
 */
class ErrorLogger private constructor(private val context: Context) {
    
    companion object {
        private const val TAG = "ErrorLogger"
        private const val ERROR_LOG_DIR = "error_logs"
        private const val MAX_LOG_SIZE = 10 * 1024 * 1024 // 10MB max
        private const val MAX_LOG_AGE = 7 * 24 * 60 * 60 * 1000 // 7 days
        
        @Volatile
        private var INSTANCE: ErrorLogger? = null
        
        fun getInstance(context: Context): ErrorLogger {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: ErrorLogger(context.applicationContext).also { INSTANCE = it }
            }
        }
    }
    
    private val logDir = File(context.filesDir, ERROR_LOG_DIR)
    
    init {
        if (!logDir.exists()) {
            logDir.mkdirs()
        }
        cleanOldLogs()
    }
    
    /**
     * Log error with context
     */
    fun logError(tag: String, message: String, throwable: Throwable? = null) {
        val timestamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.getDefault()).format(Date())
        val logMessage = "[$timestamp] ERROR [$tag]: $message"
        
        if (throwable != null) {
            val stackTrace = throwable.stackTraceToString()
            saveLog(logMessage + "\n" + stackTrace)
        } else {
            saveLog(logMessage)
        }
        
        Log.e(tag, message, throwable)
    }
    
    /**
     * Log warning
     */
    fun logWarning(tag: String, message: String) {
        val timestamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.getDefault()).format(Date())
        val logMessage = "[$timestamp] WARNING [$tag]: $message"
        
        saveLog(logMessage)
        Log.w(tag, message)
    }
    
    /**
     * Log info
     */
    fun logInfo(tag: String, message: String) {
        val timestamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.getDefault()).format(Date())
        val logMessage = "[$timestamp] INFO [$tag]: $message"
        
        saveLog(logMessage)
        Log.i(tag, message)
    }
    
    /**
     * Save log to file
     */
    private fun saveLog(message: String) {
        val logFile = File(logDir, "wallet_errors_${System.currentTimeMillis()}.log")
        
        try {
            logFile.writeText(message + "\n", append = true)
            
            // Keep log size manageable
            if (logFile.length() > MAX_LOG_SIZE) {
                mergeLogs()
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "Failed to save log: ${e.message}", e)
        }
    }
    
    /**
     * Merge multiple logs into single file
     */
    private fun mergeLogs() {
        val logFiles = logDir.listFiles { file ->
            file.name.startsWith("wallet_errors") && file.name.endsWith(".log")
        } ?: return
        
        if (logFiles.size <= 2) return
        
        // Merge all but the newest
        val mergedFile = File(logDir, "merged_errors.log")
        val writer = mergedFile.writer()
        
        try {
            logFiles.sortedBy { it.name }.drop(1).forEach { file ->
                file.forEachLine { line ->
                    writer.write(line + "\n")
                }
                file.delete()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to merge logs: ${e.message}", e)
        } finally {
            writer.close()
        }
    }
    
    /**
     * Get all error logs as string
     */
    fun getAllLogs(): String {
        return buildString {
            val logFiles = logDir.listFiles { file ->
                file.name.endsWith(".log")
            } ?: return@buildString
            
            logFiles.forEach { file ->
                file.forEachLine { line ->
                    appendLine(line)
                }
            }
        }
    }
    
    /**
     * Clear all logs
     */
    fun clearLogs() {
        logDir.listFiles()?.forEach { it.delete() }
        Log.i(TAG, "All error logs cleared")
    }
    
    /**
     * Clean old logs
     */
    private fun cleanOldLogs() {
        val cutoffTime = System.currentTimeMillis() - MAX_LOG_AGE
        
        logDir.listFiles()?.forEach { file ->
            if (file.lastModified() < cutoffTime) {
                file.delete()
            }
        }
    }
    
    /**
     * Get error count from last 24 hours
     */
    fun getErrorCount(lastHours: Int = 24): Int {
        val cutoffTime = System.currentTimeMillis() - (lastHours * 60 * 60 * 1000)
        var count = 0
        
        logDir.listFiles()?.forEach { file ->
            if (file.lastModified() >= cutoffTime) {
                count++
            }
        }
        
        return count
    }
}

/**
 * Global exception handler setup
 */
fun setupGlobalExceptionHandler(context: Context) {
    val oldHandler = Thread.getDefaultUncaughtExceptionHandler()
    
    Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
        val logger = ErrorLogger.getInstance(context)
        
        logger.logError(
            "FATAL",
            "Uncaught exception in ${thread.name}",
            throwable
        )
        
        // Let default handler handle crash
        oldHandler?.uncaughtException(thread, throwable)
    }
    
    Log.i("GlobalExceptionHandler", "✅ Global exception handler installed")
}

/**
 * Extension function for Exception
 */
fun Exception.logError(context: Context, tag: String = "Exception") {
    ErrorLogger.getInstance(context).logError(tag, this.message ?: "Unknown error", this)
}

/**
 * Extension function for Throwable
 */
fun Throwable.logError(context: Context, tag: String = "Throwable") {
    ErrorLogger.getInstance(context).logError(tag, this.message ?: "Unknown error", this)
}
