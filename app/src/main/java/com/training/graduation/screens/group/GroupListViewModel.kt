package com.training.graduation.screens.group

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch

class GroupListViewModel(
    private val chatRepository: ChatRepository = ChatRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChatListUiState())
    val uiState: StateFlow<ChatListUiState> = _uiState.asStateFlow()

    init {
        loadUserGroups()
    }

    private fun loadUserGroups() {
        viewModelScope.launch {
            chatRepository.getUserGroups()
                .onStart { _uiState.value = _uiState.value.copy(isLoading = true, error = null) }
                .catch { exception ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = exception.message ?: "Failed to load groups"
                    )
                }
                .collect { groups ->
                    _uiState.value = _uiState.value.copy(
                        groups = groups,
                        isLoading = false,
                        error = null
                    )
                }
        }
    }
    fun refreshGroups() {
        loadUserGroups()
    }
}
