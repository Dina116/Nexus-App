package com.training.graduation.screens.Authentication

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.training.graduation.db.model.User
import com.training.graduation.onesignal.scheduleNotificationFromApp

class AuthViewModel : ViewModel() {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val firestore = Firebase.firestore

    private val _authState = MutableLiveData<AuthState>()
    val authState: LiveData<AuthState> = _authState

    private val _currentUser = MutableLiveData<User?>()
    val currentUser: LiveData<User?> = _currentUser

    fun login(email: String, password: String) {
        _authState.value = AuthState.Loading
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val uid = auth.currentUser?.uid
                    if (uid != null) {
                        firestore.collection("users").document(uid).get()
                            .addOnSuccessListener { doc ->
                                if (doc.exists()) {
                                    val user = doc.toObject(User::class.java)
                                    _currentUser.value = user
                                    _authState.value = AuthState.Authenticated
                                } else {
                                    _authState.value = AuthState.Error("User not found in database")
                                }
                            }
                            .addOnFailureListener { e ->
                                _authState.value = AuthState.Error(e.message ?: "Error loading user")
                            }
                    }
                } else {
                    _authState.value = AuthState.Error(task.exception?.message ?: "Login failed")
                }
            }
    }

    fun signup(
        email: String,
        password: String,
        userName: String,
        selectedRole: String,
        defaultImageUrl: String,
//        playerId:String
    ) {
        _authState.value = AuthState.Loading
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val uid = auth.currentUser?.uid ?: return@addOnCompleteListener
                    val user = User(
                        id = uid,
                        name = userName,
                        email = email,
                        password = password,
                        host = selectedRole == "Foundation",
                        hostORfoundation = if (selectedRole == "Foundation") "foundation" else "user",
                        imageUrl = defaultImageUrl,
//                        playerId = playerId
                    )

                    firestore.collection(User.CollectionNameUser)
                        .document(uid)
                        .set(user)
                        .addOnSuccessListener {
                            _currentUser.value = user
                            _authState.value = AuthState.Authenticated
                        }
                        .addOnFailureListener { e ->
                            _authState.value = AuthState.Error(e.message ?: "User registration failed")
                        }
                } else {
                    _authState.value = AuthState.Error(task.exception?.message ?: "Signup failed")
                }
            }
    }

    fun fetchCurrentUser() {
        val uid = auth.currentUser?.uid ?: return
        firestore.collection("users").document(uid).get()
            .addOnSuccessListener { doc ->
                if (doc.exists()) {
                    _currentUser.value = doc.toObject(User::class.java)
                }
            }
    }

    fun updateUser(updatedUser: User) {
        val uid = auth.currentUser?.uid ?: return
        firestore.collection("users").document(uid)
            .set(updatedUser)
            .addOnSuccessListener {
                _currentUser.value = updatedUser
            }
    }

    fun addMeetingToUserSchedule(
        userId: String,
        title: String,
        description: String,
        meetingTimeMillis: Long,
        meetingLink: String
    ) {
        val db = FirebaseFirestore.getInstance()

        val meeting = hashMapOf(
            "title" to title,
            "description" to description,
            "meetingTime" to meetingTimeMillis,
            "meetingLink" to meetingLink,
            "notified" to false
        )

        db.collection("users")
            .document(userId)
            .collection("schedule")
            .add(meeting)
            .addOnSuccessListener {
                db.collection("users")
                    .document(userId)
                    .get()
                    .addOnSuccessListener { doc ->
                        val user = doc.toObject(User::class.java)
                        val playerId = user?.playerId
                        if (!playerId.isNullOrEmpty()) {
                            scheduleNotificationFromApp(title, meetingTimeMillis, playerId)
                        } else {
                            Log.e("OneSignal", "⚠️ playerId is null or empty")
                        }
                    }
                    .addOnFailureListener { e ->
                        Log.e("Firestore", "❌ Failed to get user data", e)
                    }
            }
            .addOnFailureListener { e ->
                Log.e("Firestore", "❌ Error adding meeting", e)
            }
    }

    fun getUserScheduledMeetings(
        userId: String,
        onResult: (List<Map<String, Any>>) -> Unit,
        onError: (Exception) -> Unit
    ) {
        val db = FirebaseFirestore.getInstance()

        db.collection("users")
            .document(userId)
            .collection("schedule")
            .get()
            .addOnSuccessListener { result ->
                val meetings = result.documents.mapNotNull { it.data }
                onResult(meetings)
            }
            .addOnFailureListener { exception ->
                onError(exception)
            }
    }
    fun deleteMeeting(userId: String, meetingId: String, onSuccess: () -> Unit, onError: (Exception) -> Unit) {
        FirebaseFirestore.getInstance()
            .collection("users")
            .document(userId)
            .collection("schedule")
            .document(meetingId)
            .delete()
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { e -> onError(e) }
    }


    fun signout() {
        auth.signOut()
        _authState.value = AuthState.Unauthenticated
        _currentUser.value = null
    }

    fun resetAuthState() {
        _authState.value = AuthState.Unauthenticated
    }



}

sealed class AuthState {
    object Authenticated : AuthState()
    object Unauthenticated : AuthState()
    object Loading : AuthState()
    data class Error(val message: String) : AuthState()
}
