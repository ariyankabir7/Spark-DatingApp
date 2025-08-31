package com.ariyan.spark.ui.home.chat

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ariyan.spark.model.Message
import com.ariyan.spark.repository.UserRepository
import com.google.firebase.Timestamp
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class ChatViewModel : ViewModel() {

    private val repository = UserRepository()

    private val _messages = MutableLiveData<List<Message>>()
    val messages: LiveData<List<Message>> = _messages

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    private var chatId: String? = null
    private val currentUserId = repository.getCurrentUserId()

    /**
     * Initializes the ViewModel, gets the chat ID, and starts listening for messages.
     */
    fun initialize(otherUserId: String) {
        if (currentUserId == null) {
            _error.value = "User not logged in"
            return
        }
        viewModelScope.launch {
            try {
                chatId = repository.getOrCreateChatId(currentUserId, otherUserId)
                listenForMessages()
            } catch (e: Exception) {
                _error.value = "Failed to initialize chat: ${e.message}"
            }
        }
    }

    /**
     * Listens for real-time message updates from the repository.
     */
    private fun listenForMessages() {
        chatId?.let { id ->
            viewModelScope.launch {
                repository.getMessages(id).collectLatest { messageList ->
                    _messages.value = messageList
                }
            }
        }
    }

    /**
     * Sends a new message.
     */
    fun sendMessage(text: String) {
        if (currentUserId == null || chatId == null || text.isBlank()) {
            return
        }
        val message = Message(
            chatId = chatId!!,
            senderId = currentUserId,
            text = text,
            timestamp = Timestamp.now()
        )
        viewModelScope.launch {
            try {
                repository.sendMessage(chatId!!, message)
            } catch (e: Exception) {
                _error.value = "Failed to send message: ${e.message}"
            }
        }
    }

    fun getCurrentUserId(): String? {
        return currentUserId
    }
}
