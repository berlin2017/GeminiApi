package com.berlin.sample1.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.berlin.sample1.model.ChatMessage
import com.berlin.sample1.model.DataState
import com.berlin.sample1.repository.GeminiRepository
import com.berlin.sample1.room.ChatMessageEntity
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class GeminiViewModel(
    private val geminiRepository: GeminiRepository
) : ViewModel() {
    val chatMessages = MutableLiveData<List<ChatMessage>>(emptyList())

    init {
        viewModelScope.launch {
//            geminiRepository.clearAllMessages()
            geminiRepository.getAllMessages().collectLatest { dataState ->
                when (dataState) {
                    is DataState.Success -> {
                        chatMessages.value = dataState.data
                    }

                    is DataState.Error -> {
                        // Handle the error state, maybe show an error message to the user
                    }

                    is DataState.Loading -> {
                        // Optionally handle the loading state, maybe show a loading indicator
                    }
                }
            }
        }
    }

    fun sendMessage(apiKey: String, message: String) {
        val userMessage = ChatMessage(message, true)
        addMessage(userMessage) // Add user message first
        viewModelScope.launch {
            geminiRepository.insertMessage(ChatMessageEntity(text = message, isUserMessage = true))
            geminiRepository.getMessageFromNetwork(apiKey, message).collectLatest { dataState ->
                when (dataState) {
                    is DataState.Success -> {
                        val geminiMessage = dataState.data
                        addMessage(geminiMessage) // Add AI message after receiving response
                    }

                    is DataState.Error -> {
                        // Handle error
                    }

                    is DataState.Loading -> {
                        //Handle loading.
                    }
                }
            }
        }
    }

    private fun addMessage(message: ChatMessage) {
        val currentList = chatMessages.value.orEmpty().toMutableList()
        currentList.add(message)
        chatMessages.value = currentList
    }
}