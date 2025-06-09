package com.training.graduation.screens.group

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class AddMemberViewModel(
    private val chatRepository: ChatRepository,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    val groupId: String = savedStateHandle.get<String>("groupId") ?: ""

    private val _uiState = MutableStateFlow(AddMemberUiState())
    val uiState: StateFlow<AddMemberUiState> = _uiState.asStateFlow()

    private var searchJob: Job? = null

    init {
        fetchExistingMembers()
        observeSearchQuery()
    }
    private fun fetchExistingMembers() {
        viewModelScope.launch {
            Log.d("AddMemberViewModel", "Fetching existing members for group: $groupId")

            val result = chatRepository.getGroupDetails(groupId)
            if (result is Result.Success) {
                val group = result.data
                Log.d("AddMemberViewModel", "Got group details, members: ${group.members}")
                _uiState.value = _uiState.value.copy(existingMemberIds = group.members.toSet())
                Log.d("AddMemberViewModel", "Updated UI state, existingMemberIds: ${_uiState.value.existingMemberIds}")
            } else if (result is Result.Error) {
                Log.e("AddMemberViewModel", "Error fetching group details", result.exception)
            }
        }
    }
    fun onSearchQueryChanged(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query, addSuccess = false)
    }
    private fun observeSearchQuery() {
        _uiState
            .debounce(300)
            .distinctUntilChanged { old, new -> old.searchQuery == new.searchQuery }
            .filter { it.searchQuery.isNotBlank() && it.searchQuery.length > 1 }
            .onEach { _uiState.value = _uiState.value.copy(isLoading = true, error = null) }
            .flatMapLatest { state ->
                flowOf(chatRepository.searchUsers(state.searchQuery))
            }
            .onEach { result ->
                when (result) {
                    is Result.Success -> {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            searchResults = result.data
                        )
                    }
                    is Result.Error -> {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            error = result.exception.message ?: "Search failed"
                        )
                    }
                }
            }
            .launchIn(viewModelScope)
    }
    fun toggleUserSelection(userId: String) {
        val currentSelection = _uiState.value.selectedUserIds
        val newSelection = if (currentSelection.contains(userId)) {
            currentSelection - userId
        } else {
            currentSelection + userId
        }
        _uiState.value = _uiState.value.copy(selectedUserIds = newSelection)
    }
    fun addSelectedMembers() {
        val usersToAdd = _uiState.value.selectedUserIds
        if (usersToAdd.isEmpty() || groupId.isBlank() || _uiState.value.addInProgress) {
            return
        }
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(addInProgress = true, error = null)
            var successCount = 0
            var firstError: String? = null

            usersToAdd.forEach { userId ->
                val result = chatRepository.addUserToGroup(groupId, userId)
                if (result is Result.Success) {
                    successCount++
                } else if (result is Result.Error && firstError == null) {
                    firstError = result.exception.message ?: "Failed to add some members"
                }
            }
            _uiState.value = _uiState.value.copy(
                addInProgress = false,
                error = firstError,
                addSuccess = firstError == null,
                selectedUserIds = emptySet()
            )
            if (firstError == null) {
                Log.d("AddMemberViewModel", "Refreshing members after successful add")
                fetchExistingMembers()
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
    fun consumeAddSuccess() {
        _uiState.value = _uiState.value.copy(addSuccess = false)
    }
}