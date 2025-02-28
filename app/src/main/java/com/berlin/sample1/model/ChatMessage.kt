package com.berlin.sample1.model

data class ChatMessage(
    val text: String,
    val isUserMessage: Boolean // true for user, false for AI
)