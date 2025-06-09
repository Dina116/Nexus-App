package com.training.graduation.screens.group

import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch


class GroupScreenViewModel(
    private val chatRepository: ChatRepository = ChatRepository(),
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {
    val groupId: String = savedStateHandle.get<String>("groupId") ?: ""

    private val _uiState = MutableStateFlow(ChatScreenUiState())
    val uiState: StateFlow<ChatScreenUiState> = _uiState.asStateFlow()
    private val _messageInput = MutableStateFlow("")
    val messageInput: StateFlow<String> = _messageInput.asStateFlow()

    init {
        if (groupId.isNotBlank()) {
            loadMessages()
        } else {
            _uiState.value = _uiState.value.copy(error = "Group ID not found", isLoading = false)
        }
        _uiState.value = _uiState.value.copy(currentUserId = chatRepository.getCurrentUserId())
    }

    private fun loadMessages() {
        viewModelScope.launch {
            chatRepository.getGroupMessages(groupId)
                .onStart { _uiState.value = _uiState.value.copy(isLoading = true, error = null) }
                .catch { exception ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = exception.message ?: "Failed to load messages"
                    )
                }
                .collect { messages ->
                    _uiState.value = _uiState.value.copy(
                        messages = messages,
                        isLoading = false,
                        error = null
                    )
                }
        }
    }

    fun updateMessageInput(input: String) {
        _messageInput.value = input
    }

    fun sendMessage() {
        val text = _messageInput.value.trim()
        if (text.isBlank() || groupId.isBlank()) {
            return
        }


        viewModelScope.launch {
            val result = chatRepository.sendMessage(groupId, text)

            when (result) {
                is Result.Success -> {
                    _messageInput.value = ""
                }
                is Result.Error -> {

                    _uiState.value = _uiState.value.copy(
                        error = result.exception.message ?: "Failed to send message"
                    )

                }
            }
        }
    }
    fun sendImage(imageUri: Uri) {
        if (groupId.isBlank()) return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val result = chatRepository.sendImageMessage(groupId, imageUri)
            _uiState.value = _uiState.value.copy(isLoading = false)

            if (result is Result.Error) {
                _uiState.value = _uiState.value.copy(
                    error = result.exception.message ?: "Failed to send image"
                )
            }
        }
    }
    fun sendFile(fileUri: Uri, fileName: String, mimeType: String) {
        if (groupId.isBlank()) return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val result = chatRepository.sendFileMessage(groupId, fileUri, fileName, mimeType)
            _uiState.value = _uiState.value.copy(isLoading = false)

            if (result is Result.Error) {
                _uiState.value = _uiState.value.copy(
                    error = result.exception.message ?: "Failed to send file"
                )
            }
        }
    }

    fun deleteMessage(messageId: String) {
        if (groupId.isBlank() || messageId.isBlank()) return

        viewModelScope.launch {
            val result = chatRepository.deleteMessage(groupId, messageId)

            if (result is Result.Error) {
                _uiState.value = _uiState.value.copy(
                    error = result.exception.message ?: "Failed to delete message"
                )
            }
        }
    }
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}