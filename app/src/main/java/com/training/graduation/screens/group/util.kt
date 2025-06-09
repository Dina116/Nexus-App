package com.training.graduation.screens.group

import androidx.annotation.Keep
import com.google.firebase.firestore.PropertyName
import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

@Keep
data class User(
    @get:PropertyName("id") val uid: String = "",
    @get:PropertyName("name") val displayName: String = "",
    val email: String = "",
    @get:PropertyName("imageUrl") val photoUrl: String? = null,
    val groups: List<String> = emptyList()
)

data class Group(
    val groupId: String = "",
    val groupName: String = "",
    @ServerTimestamp val createdAt: Date? = null,
    val createdBy: String = "",
    val members: List<String> = emptyList(),
    val lastMessage: String? = null,
    @ServerTimestamp val lastMessageTimestamp: Date? = null
) {
    constructor() : this("", "", null, "", emptyList(), null, null)
}
data class Message(
    val messageId: String = "",
    val text: String = "",
    val imageUrl: String? = null,
    val fileUrl: String? = null,
    val fileMimeType: String? = null,
    val fileName: String? = null,
    val senderId: String = "",
    val senderName: String = "",
    @ServerTimestamp val timestamp: Date? = null,
    val type: String = "text"
) {
    constructor() : this("", "", "", "", null, "text")
}

data class AddMemberUiState(
    val searchQuery: String = "",
    val searchResults: List<User> = emptyList(),
    val selectedUserIds: Set<String> = emptySet(),
    val existingMemberIds: Set<String> = emptySet(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val addInProgress: Boolean = false,
    val addSuccess: Boolean = false
)

data class GroupDetailsUiState(
    val group: Group? = null,
    val members: List<User> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null,
    val currentUserId: String? = null,
    val isCurrentUserCreator: Boolean = false,
    val actionInProgress: Boolean = false,
    val navigationEvent: NavigationEvent? = null
)
data class ChatListUiState(
    val groups: List<Group> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)
data class ChatScreenUiState(
    val messages: List<Message> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val currentUserId: String? = null
)