//
// AgbaraAssistant.kt
// Ikoro - ₿ỌFỌ Platform
//
// AI Assistant tab definitions and navigation
//

package com.ikoro.android.ai

/**
 * Agbara AI Assistant - Tab Navigation Constants
 */
object AgbaraAssistant {
    const val TAB_CHAT = "chat"
    const val TAB_MARKETPLACE = "marketplace"
    const val TAB_WALLET = "wallet"
    const val TAB_ORDERS = "orders"
    const val TAB_AI = "ai"
    const val TAB_ERRAND = "errand"
    const val TAB_CREATOR = "creator"

    /**
     * AI Capabilities
     */
    enum class Capability {
        CONVERSATION,
        TRANSLATION,
        SUMMARIZATION,
        RECOMMENDATION,
        CREATIVE,
        ANALYSIS
    }

    /**
     * Supported Languages for AI
     */
    enum class Language(val displayName: String, val code: String) {
        ENGLISH("English", "en"),
        HAUSA("Hausa", "ha"),
        YORUBA("Yoruba", "yo"),
        IGBO("Igbo", "ig"),
        PIDGIN("Pidgin", "pid")
    }

    /**
     * AI Response Types
     */
    data class AIResponse(
        val text: String,
        val confidence: Float,
        val language: Language,
        val timestamp: Long
    )
}