package com.training.graduation.screens.group


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
//import com.example.yourapp.data.repository.ChatRepository
//import com.example.yourapp.data.repository.Result // Assuming Result wrapper is in repository package
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class CreateGroupUiState(
    val groupName: String = "",
    val isLoading: Boolean = false,
    val error: String? = null
)

class CreateGroupViewModel(
    private val chatRepository: ChatRepository = ChatRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(CreateGroupUiState())
    val uiState: StateFlow<CreateGroupUiState> = _uiState.asStateFlow()

    private val _creationSuccessEvent = MutableSharedFlow<Pair<String, String>>()
    val creationSuccessEvent = _creationSuccessEvent.asSharedFlow()

    fun updateGroupName(name: String) {
        _uiState.value = _uiState.value.copy(groupName = name, error = null)
    }

    fun createGroup() {
        val groupName = _uiState.value.groupName.trim()
        if (groupName.isBlank()) {
            _uiState.value = _uiState.value.copy(error = "Group name cannot be empty")
            return
        }

        _uiState.value = _uiState.value.copy(isLoading = true, error = null)

        viewModelScope.launch {
            val result = chatRepository.createGroup(groupName, emptyList())

            when (result) {
                is Result.Success -> {
                    val groupId = result.data
                    val groupName = _uiState.value.groupName

                    _uiState.value = _uiState.value.copy(isLoading = false)
                    _creationSuccessEvent.emit(Pair(groupId, groupName))
                }
                is Result.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = result.exception.message ?: "Failed to create group"
                    )
                }
            }
        }
    }
}

