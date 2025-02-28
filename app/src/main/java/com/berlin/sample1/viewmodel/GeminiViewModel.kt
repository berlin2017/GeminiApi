package com.berlin.sample1.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.berlin.sample1.model.ChatMessage
import com.berlin.sample1.model.Content
import com.berlin.sample1.model.Part
import com.berlin.sample1.model.Prompt
import com.berlin.sample1.repository.GeminiRepository
import kotlinx.coroutines.launch

class GeminiViewModel(private val geminiRepository: GeminiRepository) : ViewModel() {
    val chatMessages = MutableLiveData<List<ChatMessage>>(emptyList())
    fun sendMessage(apiKey: String, message: String) {
        val userMessage = ChatMessage(message, true)
        addMessage(userMessage) // Add user message first

        viewModelScope.launch {
            val prompt = Prompt(
                listOf(
                    Content(
                        listOf(Part(message))
                    )
                )
            )
            val resp = geminiRepository.sendMessage(apiKey, prompt)
            if (resp.isSuccessful) {
                val responseMessage =
                    resp.body()?.candidates?.get(0)?.content?.parts?.get(0)?.text ?: ""
                val geminiMessage = ChatMessage(responseMessage, false)
                addMessage(geminiMessage) // Add AI message after receiving response
            }
        }
    }

    private fun addMessage(message: ChatMessage) {
        val currentList = chatMessages.value.orEmpty().toMutableList()
        currentList.add(message)
        chatMessages.value = currentList
    }
}