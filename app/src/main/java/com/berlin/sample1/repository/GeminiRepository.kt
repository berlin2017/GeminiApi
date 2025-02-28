package com.berlin.sample1.repository

import com.berlin.sample1.model.ChatMessage
import com.berlin.sample1.model.DataState
import com.berlin.sample1.model.GeminiResponse
import com.berlin.sample1.model.Prompt
import com.berlin.sample1.network.GeminiApiService
import com.berlin.sample1.room.ChatMessageDao
import com.berlin.sample1.room.ChatMessageEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import retrofit2.Response

class GeminiRepository(
    private val geminiApiService: GeminiApiService,
    private val chatMessageDao: ChatMessageDao//添加构造参数
) {

    suspend fun sendMessage(apiKey: String, prompt: Prompt): Response<GeminiResponse> {
        return geminiApiService.sendMessage(apiKey, prompt)
    }

    suspend fun insertMessage(message: ChatMessageEntity) {
        chatMessageDao.insertMessage(message)
    }

    fun getAllMessages(): Flow<DataState<List<ChatMessage>>> {
        return chatMessageDao.getAllMessages().map { list ->
            DataState.Success(list.map {
                ChatMessage(
                    text = it.text,
                    isUserMessage = it.isUserMessage
                )
            })
        }
    }

    suspend fun clearAllMessages() {
        chatMessageDao.clearAllMessages()
    }

    fun getMessageFromNetwork(apiKey: String, message: String): Flow<DataState<ChatMessage>> =
        flow {
            emit(DataState.Loading)
            val prompt = Prompt(
                listOf(
                    com.berlin.sample1.model.Content(
                        listOf(com.berlin.sample1.model.Part(message))
                    )
                )
            )
            try {
                val resp = sendMessage(apiKey, prompt)
                if (resp.isSuccessful) {
                    val responseMessage =
                        resp.body()?.candidates?.get(0)?.content?.parts?.get(0)?.text ?: ""
                    emit(DataState.Success(ChatMessage(responseMessage, false)))
                    insertMessage(ChatMessageEntity(text = responseMessage, isUserMessage = false))
                } else {
                    emit(DataState.Error(Exception(resp.message())))
                }
            } catch (e: Exception) {
                emit(DataState.Error(e))
            }
        }

}