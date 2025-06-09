package com.training.graduation.screens.group

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class NavigationEvent {
    object NavigateBack : NavigationEvent()
}
class GroupDetailsViewModel(
    private val chatRepository: ChatRepository,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    val groupId: String = savedStateHandle.get<String>("groupId") ?: ""
    private val _uiState = MutableStateFlow(GroupDetailsUiState())
    val uiState: StateFlow<GroupDetailsUiState> = _uiState.asStateFlow()
    private val groupFlow = MutableStateFlow<Group?>(null)
    private val membersFlow = MutableStateFlow<List<User>>(emptyList())

    init {
        val currentUserId = chatRepository.getCurrentUserId()
        _uiState.value = _uiState.value.copy(currentUserId = currentUserId)

        if (groupId.isNotBlank() && currentUserId != null) {
            loadGroupAndMembers()
        } else {
            _uiState.value = _uiState.value.copy(isLoading = false, error = "Group ID or User ID missing")
        }
    }

    private fun loadGroupAndMembers() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                val result = chatRepository.getGroupDetails(groupId)

                if (result is Result.Success) {
                    val group = result.data
                    groupFlow.value = group
                    val isCreator = group.createdBy == _uiState.value.currentUserId
                    _uiState.value = _uiState.value.copy(isCurrentUserCreator = isCreator)
                    val memberDetails = fetchMemberDetails(group.members)
                    membersFlow.value = memberDetails
                    _uiState.value = _uiState.value.copy(
                        group = group,
                        members = memberDetails,
                        isLoading = false
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = (result as? Result.Error)?.exception?.message ?: "Group not found"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Failed to load details"
                )
            }
        }
    }

    private suspend fun fetchGroupDetails(id: String): Group? {
        val result = chatRepository.getGroupDetails(id)
        return if (result is Result.Success) result.data else null
    }

    private suspend fun fetchMemberDetails(memberIds: List<String>): List<User> {
        return memberIds.mapNotNull { userId ->
            try {
                val result = chatRepository.getUserById(userId)
                if (result is Result.Success) result.data else null
            } catch (e: Exception) {
                Log.e("GroupDetailsViewModel", "Error fetching member $userId", e)
                null
            }
        }
    }


    fun removeMember(userIdToRemove: String) {
        if (groupId.isBlank() || _uiState.value.actionInProgress) return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(actionInProgress = true, error = null)
            val result = chatRepository.removeUserFromGroup(groupId, userIdToRemove)
            when (result) {
                is Result.Success -> {
                    groupFlow.value?.let {
                        val updatedMembers = it.members - userIdToRemove
                        membersFlow.value = fetchMemberDetails(updatedMembers)
                        groupFlow.value = it.copy(members = updatedMembers)
                    }
                    _uiState.value = _uiState.value.copy(actionInProgress = false)
                }
                is Result.Error -> {
                    _uiState.value = _uiState.value.copy(
                        actionInProgress = false,
                        error = result.exception.message ?: "Failed to remove member"
                    )
                }
            }
        }
    }

    fun leaveGroup() {
        if (groupId.isBlank() || _uiState.value.actionInProgress) return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(actionInProgress = true, error = null)
            val result = chatRepository.leaveGroup(groupId)
            when (result) {
                is Result.Success -> {
                    _uiState.value = _uiState.value.copy(actionInProgress = false, navigationEvent = NavigationEvent.NavigateBack)
                }
                is Result.Error -> {
                    _uiState.value = _uiState.value.copy(
                        actionInProgress = false,
                        error = result.exception.message ?: "Failed to leave group"
                    )
                }
            }
        }
    }

    fun deleteGroup() {
        if (groupId.isBlank() || !_uiState.value.isCurrentUserCreator || _uiState.value.actionInProgress) return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(actionInProgress = true, error = null)
            val result = chatRepository.deleteGroup(groupId)
            when (result) {
                is Result.Success -> {
                    _uiState.value = _uiState.value.copy(actionInProgress = false, navigationEvent = NavigationEvent.NavigateBack)
                }
                is Result.Error -> {
                    _uiState.value = _uiState.value.copy(
                        actionInProgress = false,
                        error = result.exception.message ?: "Failed to delete group"
                    )
                }
            }
        }
    }
    fun consumeNavigationEvent() {
        _uiState.value = _uiState.value.copy(navigationEvent = null)
    }
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}
