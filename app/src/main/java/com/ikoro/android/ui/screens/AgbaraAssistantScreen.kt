//
// AgbaraAssistantScreen.kt
// Ikoro - ₿ỌFỌ Platform
//
// AI Assistant interface
//

package com.ikoro.android.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ikoro.android.ai.AgbaraAssistant
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AgbaraAssistantScreen(modifier: Modifier = Modifier) {
    var messageText by remember { mutableStateOf("") }
    var selectedLanguage by remember { mutableStateOf(AgbaraAssistant.Language.ENGLISH) }
    val messages = remember { mutableStateListOf<ChatMessage>() }
    val coroutineScope = rememberCoroutineScope()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
    ) {
        TopAppBar(
            title = { Text("Agbara AI Assistant") },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Color(0xFF6200EE),
                titleContentColor = Color.White
            ),
            actions = {
                // Language selector
                var showLanguageMenu by remember { mutableStateOf(false) }
                IconButton(onClick = { showLanguageMenu = true }) {
                    Icon(Icons.Default.Language, contentDescription = "Language")
                }
                DropdownMenu(
                    expanded = showLanguageMenu,
                    onDismissRequest = { showLanguageMenu = false }
                ) {
                    AgbaraAssistant.Language.values().forEach { lang ->
                        DropdownMenuItem(
                            text = { Text(lang.displayName) },
                            onClick = {
                                selectedLanguage = lang
                                showLanguageMenu = false
                            }
                        )
                    }
                }
            }
        )

        // Chat messages
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(messages) { message ->
                ChatBubble(
                    message = message,
                    isUser = message.isUser
                )
            }
        }

        // AI capabilities info
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFE8EAF6))
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    "Agbara AI Capabilities:",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "• Chat Intelligence: Suggest replies, summarize conversations\n• Marketplace: Product recommendations, price comparisons\n• Wallet: Spending insights, budget recommendations\n• Translation: Multi-language support",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }

        // Input area
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = messageText,
                onValueChange = { messageText = it },
                modifier = Modifier.weight(1f),
                placeholder = { Text("Ask Agbara anything...") },
                shape = MaterialTheme.shapes.medium
            )

            Spacer(modifier = Modifier.width(8.dp))

            Button(
                onClick = {
                    if (messageText.isNotBlank()) {
                        messages.add(ChatMessage(messageText, isUser = true))
                        val userMessage = messageText

                        // Simulate AI response
                        coroutineScope.launch {
                            val response = generateAIResponse(userMessage, selectedLanguage)
                            messages.add(ChatMessage(response, isUser = false))
                        }

                        messageText = ""
                    }
                },
                shape = MaterialTheme.shapes.medium
            ) {
                Icon(Icons.Default.Send, contentDescription = "Send")
            }
        }
    }
}

@Composable
fun ChatBubble(message: ChatMessage, isUser: Boolean) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
    ) {
        Card(
            colors = CardDefaults.cardColors(
                containerColor = if (isUser) Color(0xFF6200EE) else Color.White
            ),
            shape = MaterialTheme.shapes.medium
        ) {
            Text(
                message.text,
                modifier = Modifier.padding(12.dp),
                color = if (isUser) Color.White else Color.Black
            )
        }
    }
}

data class ChatMessage(
    val text: String,
    val isUser: Boolean,
    val timestamp: Long = System.currentTimeMillis()
)

suspend fun generateAIResponse(message: String, language: AgbaraAssistant.Language): String {
    // Simplified AI responses - in production, this would call actual AI models
    val lowerMessage = message.lowercase()

    return when {
        lowerMessage.contains("hello") || lowerMessage.contains("hi") ->
            "Hello! I'm Agbara, your AI assistant. How can I help you today with Ikoro ₿ỌFỌ?"

        lowerMessage.contains("price") ->
            "I can help you check prices and find the best deals. What product are you looking for?"

        lowerMessage.contains("wallet") ->
            "For your wallet, I recommend: Use ₿ỌFỌ for everyday transactions (lowest fees), ₿ for long-term savings, and ₦ for local purchases when internet is available."

        lowerMessage.contains("translate") ->
            "I can translate between English, Hausa, Yoruba, Igbo, and Pidgin. What would you like me to translate?"

        lowerMessage.contains("market") || lowerMessage.contains("shop") ->
            "Looking for something in the marketplace? I can help you find products, compare prices, and recommend sellers based on your preferences."

        lowerMessage.contains("money") || lowerMessage.contains("payment") ->
            "I can help with payments! You can use QR codes, NFC, or payment links. Would you like me to explain how?"

        else -> "I understand you're asking about \"$message\". Let me help you with that. As Agbara, I can assist with chat intelligence, marketplace recommendations, wallet insights, translations, and more. Could you be more specific?"

    }
}