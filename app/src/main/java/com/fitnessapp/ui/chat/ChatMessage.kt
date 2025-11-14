package com.fitnessapp.ui.chat

// Represents a single message in chat
data class ChatMessage(
    val text: String, // Actual text content of message
    val isUser: Boolean // Checks if message was sent by user or AI
)
