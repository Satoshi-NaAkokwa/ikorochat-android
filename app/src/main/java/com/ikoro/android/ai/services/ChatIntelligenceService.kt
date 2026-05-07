//
// ChatIntelligenceService.kt
// Ikoro - ₿ỌFỌ Platform
//
// AI-powered chat intelligence features
//

package com.ikoro.android.ai.services

import android.content.Context
import com.ikoro.android.ai.AgbaraAssistant
import com.ikoro.android.chat.Message
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.*

/**
 * Chat Intelligence Service - AI-powered chat features
 */
class ChatIntelligenceService(private val context: Context) {

    private val memory = HashMap<String, List<String>>()

    /**
     * Suggest a reply based on conversation context
     */
    suspend fun suggestReply(messages: List<Message>): String? = withContext(Dispatchers.IO) {
        if (messages.isEmpty()) return@withContext null

        val lastMessage = messages.last()
        val lastText = lastMessage.text ?: return@withContext null

        // Simple pattern matching for reply suggestions
        when {
            lastText.contains("?") -> "I can help with that! Tell me more."
            lastText.contains("hello", ignoreCase = true) -> "Hi there! How can I help you today?"
            lastText.contains("price", ignoreCase = true) -> "Let me check the current prices for you."
            lastText.contains("order", ignoreCase = true) -> "I can help you place an order or track an existing one."
            else -> null
        }
    }

    /**
     * Summarize a chat conversation
     */
    suspend fun summarizeChat(messages: List<Message>): String = withContext(Dispatchers.IO) {
        if (messages.isEmpty()) return@withContext "No messages to summarize"

        val senderMessages = messages.filter { it.isIncoming }
        val myMessages = messages.filter { !it.isIncoming }

        val topics = extractTopics(senderMessages)
        val mainTopic = topics.maxByOrNull { it.value }?.key ?: "general discussion"

        """
        Chat Summary:
        - Messages exchanged: ${messages.size}
        - Main topic: $mainTopic
        - Participants involved: ${messages.map { it.sender }.distinct().size}
        - Last activity: ${formatTime(messages.last().timestamp)}

        Key points discussed:
        ${senderMessages.take(5).joinToString("\n") { "• ${it.text?.take(100)}" }}
        """.trimIndent()
    }

    /**
     * Detect if a message is spam
     */
    suspend fun detectSpam(message: Message): Boolean = withContext(Dispatchers.IO) {
        val text = message.text ?: return@withContext false

        val spamIndicators = listOf(
            "win", "prize", "lottery", "claim", "urgent", "act now",
            "limited time", "exclusive offer", "click here", "subscribe"
        )

        val indicatorCount = spamIndicators.count { text.contains(it, ignoreCase = true) }
        indicatorCount >= 3
    }

    /**
     * Translate a message to target language
     */
    suspend fun translateMessage(text: String, targetLanguage: AgbaraAssistant.Language): String = withContext(Dispatchers.IO) {
        // Offline translation (simplified - real implementation would use ML Kit)
        "[Translated to ${targetLanguage.displayName}]: $text"
    }

    /**
     * Extract topics from messages
     */
    private fun extractTopics(messages: List<Message>): Map<String, Int> {
        val topicKeywords = mapOf(
            "market" to listOf("buy", "sell", "price", "shop", "order", "product"),
            "food" to listOf("food", "eat", "restaurant", "meal", "hungry"),
            "transport" to listOf("ride", "drive", "car", "bus", "taxi", "travel"),
            "health" to listOf("doctor", "medicine", "hospital", "health", "sick"),
            "education" to listOf("school", "learn", "class", "teacher", "study")
        )

        val topics = HashMap<String, Int>()
        messages.forEach { message ->
            val text = message.text?.lowercase() ?: return@forEach
            topicKeywords.forEach { (topic, keywords) ->
                if (keywords.any { text.contains(it) }) {
                    topics[topic] = topics.getOrDefault(topic, 0) + 1
                }
            }
        }

        return topics
    }

    /**
     * Format timestamp
     */
    private fun formatTime(timestamp: Long): String {
        val date = Date(timestamp)
        return java.text.SimpleDateFormat("HH:mm, MMM dd", Locale.getDefault()).format(date)
    }
}