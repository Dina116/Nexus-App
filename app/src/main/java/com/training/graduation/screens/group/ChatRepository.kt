package com.training.graduation.screens.group

import android.net.Uri
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.Date
import java.util.UUID

sealed class Result<out T> {
    data class Success<out T>(val data: T) : Result<T>()
    data class Error(val exception: Exception) : Result<Nothing>()
}
class ChatRepository {
    private val firestore: FirebaseFirestore = Firebase.firestore
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val storage: FirebaseStorage = Firebase.storage

    private val usersCollection = firestore.collection("users")
    private val groupsCollection = firestore.collection("groups")
    private val storageRef = storage.reference

    fun getCurrentUserId(): String? {
        return auth.currentUser?.uid
    }
    suspend fun getCurrentUserDisplayName(): String? {
        val userId = getCurrentUserId() ?: return null
        return try {
            val document = usersCollection.document(userId).get().await()
            document.getString("name")
        } catch (e: Exception) {
            null
        }
    }
    fun getUserGroups(): Flow<List<Group>> = callbackFlow {
        val userId = getCurrentUserId()
        if (userId == null) {
            trySend(emptyList())
            close(IllegalStateException("User not logged in"))
            return@callbackFlow
        }
        val listenerRegistration = groupsCollection
            .whereArrayContains("members", userId)
            .orderBy("lastMessageTimestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshots, e ->
                if (e != null) {
                    close(e)
                    return@addSnapshotListener
                }
                if (snapshots != null) {
                    val groups = snapshots.documents.mapNotNull { it.toObject<Group>() }
                    trySend(groups)
                }
            }
        awaitClose { listenerRegistration.remove() }
    }

    fun getGroupMessages(groupId: String): Flow<List<Message>> = callbackFlow {
        val listenerRegistration = groupsCollection.document(groupId)
            .collection("messages")
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshots, e ->
                if (e != null) {
                    close(e)
                    return@addSnapshotListener
                }
                if (snapshots != null) {
                    val messages = snapshots.documents.mapNotNull { it.toObject<Message>() }
                    trySend(messages)
                }
            }
        awaitClose { listenerRegistration.remove() }
    }
    suspend fun createGroup(groupName: String, initialMemberIds: List<String>): Result<String> {
        val userId = getCurrentUserId()
        if (userId == null) {
            return Result.Error(IllegalStateException("User not logged in"))
        }
        val allMemberIds = (initialMemberIds + userId).distinct()
        val newGroup = Group(
            groupName = groupName,
            createdBy = userId,
            members = allMemberIds,
            createdAt = Date()
        )

        return try {
            val groupRef = groupsCollection.add(newGroup).await()
            val groupId = groupRef.id
            groupsCollection.document(groupId).update("groupId", groupId).await()
            firestore.runBatch { batch ->
                allMemberIds.forEach {
                    val userRef = usersCollection.document(it)
                    batch.update(userRef, "groups", FieldValue.arrayUnion(groupId))
                }
            }.await()

            Result.Success(groupId)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
    suspend fun sendMessage(groupId: String, text: String): Result<Unit> {
        val userId = getCurrentUserId()
        val senderName = getCurrentUserDisplayName()

        if (userId == null || senderName == null) {
            return Result.Error(IllegalStateException("User not logged in or display name not found"))
        }

        val message = Message(
            text = text,
            senderId = userId,
            senderName = senderName,
            timestamp = Date(),
            type = "text"
        )
        return try {
            val groupDocRef = groupsCollection.document(groupId)
            val messagesColRef = groupDocRef.collection("messages")
            val messageRef = messagesColRef.add(message).await()
            val messageId = messageRef.id
            firestore.runBatch { batch ->
                batch.update(messagesColRef.document(messageId), "messageId", messageId)
                batch.update(groupDocRef,
                    mapOf(
                        "lastMessage" to text,
                        "lastMessageTimestamp" to FieldValue.serverTimestamp()
                    )
                )
            }.await()

            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
    suspend fun sendImageMessage(groupId: String, imageUri: Uri): Result<Unit> {
        val userId = getCurrentUserId()
        val senderName = getCurrentUserDisplayName()

        if (userId == null || senderName == null) {
            return Result.Error(IllegalStateException("User not logged in or display name not found"))
        }

        return try {
            val fileName = "images/${UUID.randomUUID()}.jpg"
            val imageRef = storageRef.child(fileName)
            imageRef.putFile(imageUri).await()
            val imageUrl = imageRef.downloadUrl.await().toString()

            val message = Message(
                imageUrl = imageUrl,
                senderId = userId,
                senderName = senderName,
                timestamp = Date(),
                type = "image"
            )

            val groupDocRef = groupsCollection.document(groupId)
            val messagesColRef = groupDocRef.collection("messages")

            val messageRef = messagesColRef.add(message).await()
            val messageId = messageRef.id

            firestore.runBatch { batch ->
                batch.update(messagesColRef.document(messageId), "messageId", messageId)
                batch.update(groupDocRef,
                    mapOf(
                        "lastMessage" to "[Image]",
                        "lastMessageTimestamp" to FieldValue.serverTimestamp()
                    )
                )
            }.await()

            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
    suspend fun sendFileMessage(groupId: String, fileUri: Uri, fileName: String, mimeType: String): Result<Unit> {
        val userId = getCurrentUserId()
        val senderName = getCurrentUserDisplayName()

        if (userId == null || senderName == null) {
            return Result.Error(IllegalStateException("User not logged in or display name not found"))
        }

        return try {
            val storageFileName = "files/${UUID.randomUUID()}_$fileName"
            val fileRef = storageRef.child(storageFileName)
            fileRef.putFile(fileUri).await()
            val fileUrl = fileRef.downloadUrl.await().toString()

            val message = Message(
                fileUrl = fileUrl,
                fileName = fileName,
                fileMimeType = mimeType,
                senderId = userId,
                senderName = senderName,
                timestamp = Date(),
                type = "file"
            )

            val groupDocRef = groupsCollection.document(groupId)
            val messagesColRef = groupDocRef.collection("messages")

            val messageRef = messagesColRef.add(message).await()
            val messageId = messageRef.id

            firestore.runBatch { batch ->
                batch.update(messagesColRef.document(messageId), "messageId", messageId)
                batch.update(groupDocRef,
                    mapOf(
                        "lastMessage" to "[File] $fileName",
                        "lastMessageTimestamp" to FieldValue.serverTimestamp()
                    )
                )
            }.await()
            Log.d("Upload", "Uploading file with name: $storageFileName")
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    suspend fun addUserToGroup(groupId: String, userIdToAdd: String): Result<Unit> {
        val currentUserId = getCurrentUserId() ?: return Result.Error(IllegalStateException("User not logged in"))

        return try {
            val groupDocRef = groupsCollection.document(groupId)
            val groupSnapshot = groupDocRef.get().await()
            val group = groupSnapshot.toObject<Group>()

            if (group == null) {
                return Result.Error(IllegalArgumentException("Group not found"))
            }
            if (group.createdBy != currentUserId) {
                return Result.Error(SecurityException("Only the group creator can add members"))
            }
            if (group.members.contains(userIdToAdd)) {
                return Result.Success(Unit)
            }
            val userToAddDoc = usersCollection.document(userIdToAdd).get().await()
            if (!userToAddDoc.exists()) {
                return Result.Error(IllegalArgumentException("User to add not found"))
            }
            firestore.runBatch { batch ->
                batch.update(groupDocRef, "members", FieldValue.arrayUnion(userIdToAdd))
                batch.update(usersCollection.document(userIdToAdd), "groups", FieldValue.arrayUnion(groupId))
            }.await()
            val updatedGroup = groupsCollection.document(groupId).get().await().toObject<Group>()
            Log.d("ChatRepository", "Group after adding member: $updatedGroup")
            Log.d("ChatRepository", "Updated members list: ${updatedGroup?.members}")
            notifyUserAddedToGroup(userIdToAdd, groupId, group.groupName)
            Result.Success(Unit)
        } catch (e: Exception) {
            Log.e("ChatRepository", "Error adding user $userIdToAdd to group $groupId", e)
            Result.Error(e)
        }
    }
    suspend fun removeUserFromGroup(groupId: String, userIdToRemove: String): Result<Unit> {
        val currentUserId = getCurrentUserId() ?: return Result.Error(IllegalStateException("User not logged in"))

        return try {
            val groupDocRef = groupsCollection.document(groupId)
            val groupSnapshot = groupDocRef.get().await()
            val group = groupSnapshot.toObject<Group>()

            if (group == null) {
                return Result.Error(IllegalArgumentException("Group not found"))
            }
            if (group.createdBy != currentUserId) {
                return Result.Error(SecurityException("Only the group creator can remove members"))
            }
            if (userIdToRemove == group.createdBy) {
                return Result.Error(IllegalArgumentException("Creator cannot be removed using this function."))
            }
            if (!group.members.contains(userIdToRemove)) {
                return Result.Success(Unit)
            }
            firestore.runBatch { batch ->
                batch.update(groupDocRef, "members", FieldValue.arrayRemove(userIdToRemove))
                batch.update(usersCollection.document(userIdToRemove), "groups", FieldValue.arrayRemove(groupId))
            }.await()

            Result.Success(Unit)
        } catch (e: Exception) {
            Log.e("ChatRepository", "Error removing user $userIdToRemove from group $groupId", e)
            Result.Error(e)
        }
    }

    suspend fun deleteMessage(groupId: String, messageId: String): Result<Unit> {
        val currentUserId = getCurrentUserId() ?: return Result.Error(IllegalStateException("User not logged in"))
        return try {
            val messageDocRef = groupsCollection.document(groupId).collection("messages").document(messageId)
            val messageSnapshot = messageDocRef.get().await()
            val message = messageSnapshot.toObject<Message>()

            if (message == null) {
                return Result.Error(IllegalArgumentException("Message not found"))
            }
            if (message.senderId != currentUserId) {
                return Result.Error(SecurityException("You can only delete your own messages"))
            }
            messageDocRef.delete().await()
            Result.Success(Unit)
        } catch (e: Exception) {
            Log.e("ChatRepository", "Error deleting message $messageId from group $groupId", e)
            Result.Error(e)
        }
    }

    suspend fun leaveGroup(groupId: String): Result<Unit> {
        val currentUserId = getCurrentUserId() ?: return Result.Error(IllegalStateException("User not logged in"))

        return try {
            val groupDocRef = groupsCollection.document(groupId)
            val groupSnapshot = groupDocRef.get().await()
            val group = groupSnapshot.toObject<Group>()

            if (group == null) {
                return Result.Error(IllegalArgumentException("Group not found"))
            }
            if (!group.members.contains(currentUserId)) {
                return Result.Success(Unit)
            }
            if (group.createdBy == currentUserId && group.members.size == 1) {
                Log.w("ChatRepository", "Creator is leaving the last member of the group $groupId. Consider deleting the group.")
            }
            firestore.runBatch { batch ->
                batch.update(groupDocRef, "members", FieldValue.arrayRemove(currentUserId))
                batch.update(usersCollection.document(currentUserId), "groups", FieldValue.arrayRemove(groupId))
            }.await()

            Result.Success(Unit)
        } catch (e: Exception) {
            Log.e("ChatRepository", "Error leaving group $groupId", e)
            Result.Error(e)
        }
    }

    suspend fun deleteGroup(groupId: String): Result<Unit> {
        val currentUserId = getCurrentUserId() ?: return Result.Error(IllegalStateException("User not logged in"))

        return try {
            val groupDocRef = groupsCollection.document(groupId)
            val groupSnapshot = groupDocRef.get().await()
            val group = groupSnapshot.toObject<Group>()

            if (group == null) {
                return Result.Error(IllegalArgumentException("Group not found"))
            }
            if (group.createdBy != currentUserId) {
                return Result.Error(SecurityException("Only the group creator can delete the group"))
            }
            firestore.runBatch { batch ->
                group.members.forEach {
                    val userRef = usersCollection.document(it)
                    batch.update(userRef, "groups", FieldValue.arrayRemove(groupId))
                }
                batch.delete(groupDocRef)
            }.await()

            Result.Success(Unit)
        } catch (e: Exception) {
            Log.e("ChatRepository", "Error deleting group $groupId", e)
            Result.Error(e)
        }
    }

    suspend fun searchUsers(query: String): Result<List<User>> {
        if (query.isBlank()) {
            return Result.Success(emptyList())
        }
        val currentUserId = getCurrentUserId()

        return try {
            val snapshot = usersCollection
                .orderBy("email")
                .startAt(query)
                .endAt(query + '\uf8ff')
                .limit(20)
                .get()
                .await()

            val users = snapshot.documents.mapNotNull { doc ->
                try {
                    val uid = doc.id
                    val email = doc.getString("email") ?: ""
                    val name = doc.getString("name") ?: ""
                    val photoUrl = doc.getString("imageUrl")
                    val groups = doc.get("groups") as? List<String> ?: emptyList()

                    User(
                        uid = uid,
                        displayName = name,
                        email = email,
                        photoUrl = photoUrl,
                        groups = groups
                    )

                } catch (ex: Exception) {
                    Log.e("ChatRepository", "Error parsing user document ${doc.id}", ex)
                    null
                }
            }.filter { it.uid != currentUserId }

            Result.Success(users)
        } catch (e: Exception) {
            Log.e("ChatRepository", "Error searching users with query '$query'", e)
            Result.Error(e)
        }
    }

    suspend fun notifyUserAddedToGroup(userId: String, groupId: String, groupName: String): Result<Unit> {
        return try {
            val notification = hashMapOf(
                "userId" to userId,
                "type" to "group_invitation",
                "groupId" to groupId,
                "groupName" to groupName,
                "timestamp" to FieldValue.serverTimestamp(),
                "isRead" to false
            )

            firestore.collection("notifications").add(notification).await()
            Result.Success(Unit)
        } catch (e: Exception) {
            Log.e("ChatRepository", "Error sending notification to user $userId", e)
            Result.Error(e)


        }
    }

    suspend fun getGroupDetails(groupId: String): Result<Group> {
        return try {
            val groupDoc = groupsCollection.document(groupId).get().await()

            Log.d("ChatRepository", "Group document data: ${groupDoc.data}")

            val group = groupDoc.toObject<Group>()

            if (group != null) {
                Log.d("ChatRepository", "Group members: ${group.members}")
                Result.Success(group)
            } else {
                Result.Error(Exception("Group not found"))
            }
        } catch (e: Exception) {
            Log.e("ChatRepository", "Error getting group details", e)
            Result.Error(e)
        }
    }

    suspend fun getUserById(userId: String): Result<User> {
        return try {
            val userDoc = usersCollection.document(userId).get().await()

            if (userDoc.exists()) {
                val uid = userDoc.id
                val email = userDoc.getString("email") ?: ""
                val name = userDoc.getString("name") ?: ""
                val photoUrl = userDoc.getString("imageUrl")
                val groups = userDoc.get("groups") as? List<String> ?: emptyList()

                val user = User(
                    uid = uid,
                    displayName = name,
                    email = email,
                    photoUrl = photoUrl,
                    groups = groups
                )

                Result.Success(user)
            } else {
                Result.Error(Exception("User not found"))
            }
        } catch (e: Exception) {
            Log.e("ChatRepository", "Error getting user $userId", e)
            Result.Error(e)
        }
    }
}