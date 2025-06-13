package com.training.graduation.screens.startmeeting

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.gson.Gson
import java.util.UUID

object FirebaseManager {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private fun getCurrentUserDisplayName(uid: String, onComplete: (String?) -> Unit) {
        db.collection("users").document(uid).get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val displayName = document.getString("name")
                    onComplete(displayName)
                } else {
                    onComplete(null)
                }
            }
            .addOnFailureListener {
                onComplete(null)
            }
    }
    fun createMeeting(meetingId: String, isCheatingDetectionEnabled: Boolean, callback: (Boolean) -> Unit) {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            auth.signInAnonymously()
                .addOnSuccessListener {
                    createMeetingInternal(meetingId, isCheatingDetectionEnabled, callback)
                }
                .addOnFailureListener {
                    Log.e("FirebaseManager", "Failed to sign in anonymously: ${it.message}")
                    callback(false)
                }
        } else {
            createMeetingInternal(meetingId, isCheatingDetectionEnabled, callback)
        }
    }

    private fun createMeetingInternal(meetingId: String, isCheatingDetectionEnabled: Boolean, callback: (Boolean) -> Unit) {
        val currentUser = auth.currentUser ?: return
        getCurrentUserDisplayName(currentUser.uid) { hostDisplayName ->
            val displayName = hostDisplayName ?: "Host"

            val meeting = MeetingData(
                meetingId = meetingId,
                hostId = currentUser.uid,
                startTime = System.currentTimeMillis(),
                isCheatingDetectionEnabled = isCheatingDetectionEnabled,
                endTime = System.currentTimeMillis(),
                participants = mutableListOf(currentUser.uid)
            )

            val meetingRef = db.collection("meetings").document(meetingId)

            meetingRef.get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        Log.d("FirebaseManager", "Meeting already exists: $meetingId")
                        callback(true)
                    } else {
                        meetingRef.set(meeting)
                            .addOnSuccessListener {
                                Log.d("FirebaseManager", "Meeting created: $meetingId")
                                val participant = ParticipantData(
                                    participantId = currentUser.uid,
                                    displayName = displayName,
                                    meetingId = meetingId,
                                    joinTime = System.currentTimeMillis()
                                )
                                db.collection("participants")
                                    .document("$meetingId-${currentUser.uid}")
                                    .set(participant)
                                    .addOnSuccessListener {
                                        callback(true)
                                    }
                                    .addOnFailureListener {
                                        Log.e("FirebaseManager", "Failed to add host as participant: ${it.message}")
                                        callback(false)
                                    }
                            }
                            .addOnFailureListener {
                                Log.e("FirebaseManager", "Failed to create meeting: ${it.message}")
                                callback(false)
                            }
                    }
                }
                .addOnFailureListener {
                    Log.e("FirebaseManager", "Error checking if meeting exists: ${it.message}")
                    callback(false)
                }
        }
    }
//    private fun createMeetingInternal(meetingId: String, isCheatingDetectionEnabled: Boolean, callback: (Boolean) -> Unit) {
//        val currentUser = auth.currentUser ?: return
//
//        val meeting = MeetingData(
//            meetingId = meetingId,
//            hostId = currentUser.uid,
//            startTime = System.currentTimeMillis(),
//            isCheatingDetectionEnabled = isCheatingDetectionEnabled,
//            endTime = System.currentTimeMillis(),
//            participants = mutableListOf(currentUser.uid)
//        )
//
//        val meetingRef = db.collection("meetings").document(meetingId)
//
//        meetingRef.get()
//            .addOnSuccessListener { document ->
//                if (document.exists()) {
//                    Log.d("FirebaseManager", "Meeting already exists: $meetingId")
//                    callback(true)
//                } else {
//                    meetingRef.set(meeting)
//                        .addOnSuccessListener {
//                            Log.d("FirebaseManager", "Meeting created: $meetingId")
//
//                            // 👇 إنشاء وثيقة المشارك للمضيف
//                            val participant = ParticipantData(
//                                participantId = currentUser.uid,
//                                displayName = "Host",
//                                meetingId = meetingId,
//                                joinTime = System.currentTimeMillis()
//                            )
//                            db.collection("participants")
//                                .document("$meetingId-${currentUser.uid}")
//                                .set(participant)
//                                .addOnSuccessListener {
//                                    callback(true)
//                                }
//                                .addOnFailureListener {
//                                    Log.e("FirebaseManager", "Failed to add host as participant: ${it.message}")
//                                    callback(false)
//                                }
//                        }
//                        .addOnFailureListener {
//                            Log.e("FirebaseManager", "Failed to create meeting: ${it.message}")
//                            callback(false)
//                        }
//                }
//            }
//            .addOnFailureListener {
//                Log.e("FirebaseManager", "Error checking if meeting exists: ${it.message}")
//                callback(false)
//            }
//    }



//    private fun createMeetingInternal(meetingId: String, isCheatingDetectionEnabled: Boolean,  callback: (Boolean) -> Unit) {
//        val currentUser = auth.currentUser ?: return
//
//        val meeting = MeetingData(
//            meetingId = meetingId,
//            hostId = currentUser.uid,
//            startTime = System.currentTimeMillis(),
//            participants = mutableListOf(hostId),
//            isCheatingDetectionEnabled = isCheatingDetectionEnabled,
//
//        )
//
//        db.collection("meetings")
//            .document(meetingId)
//            .set(meeting)
//            .addOnSuccessListener {
//                Log.d("FirebaseManager", "Meeting created: $meetingId")
//                callback(true)
//            }
//            .addOnFailureListener {
//                Log.e("FirebaseManager", "Failed to create meeting: ${it.message}")
//                callback(false)
//            }
//    }

    // إضافة مشارك إلى ميتنج
//    fun addParticipant(meetingId: String, participantId: String, displayName: String, callback: (Boolean) -> Unit) {
//
//        //val currentUser = auth.currentUser
//        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: "local-user"
//        if ( uid == null) {
//            auth.signInAnonymously()
//                .addOnSuccessListener {
//                    addParticipantInternal(meetingId, participantId, displayName, callback)
//                }
//                .addOnFailureListener {
//                    Log.e("FirebaseManager", "Failed to sign in anonymously: ${it.message}")
//                    callback(false)
//                }
//        } else {
//            addParticipantInternal(meetingId, participantId, displayName, callback)
//        }
//    }
    fun addParticipant(meetingId: String, displayName: String, callback: (Boolean) -> Unit) {
        val currentUser = FirebaseAuth.getInstance().currentUser

        if (currentUser == null) {
            // تسجيل دخول مجهول
            FirebaseAuth.getInstance().signInAnonymously()
                .addOnSuccessListener { authResult ->
                    val uid = authResult.user?.uid
                    if (uid != null) {
                        addParticipantInternal(meetingId, uid, displayName, callback)
                    } else {
                        Log.e("FirebaseManager", "Anonymous user uid is null")
                        callback(false)
                    }
                }
                .addOnFailureListener {
                    Log.e("FirebaseManager", "Failed to sign in anonymously: ${it.message}")
                    callback(false)
                }
        } else {
            val uid = currentUser.uid
            addParticipantInternal(meetingId, uid, displayName, callback)
        }
    }
private fun addParticipantInternal(
    meetingId: String,
    participantId: String,
    displayName: String,
    callback: (Boolean) -> Unit
) {
    if (meetingId.isEmpty()) {
        Log.e("FirebaseManager", "Invalid meetingId: cannot be empty")
        callback(false)
        return
    }

    val meetingRef = db.collection("meetings").document(meetingId)

    meetingRef.get().addOnSuccessListener { document ->
        if (document.exists()) {
            meetingRef.update("participants", FieldValue.arrayUnion(participantId))
                .addOnSuccessListener {
                    saveParticipantData(meetingId, participantId, displayName, callback)
                }
                .addOnFailureListener {
                    Log.e("FirebaseManager", "Failed to update meeting: ${it.message}")
                    callback(false)
                }
        } else {
            val data = hashMapOf(
                "participants" to arrayListOf(participantId),
                "hostId" to participantId,
                "meetingId" to meetingId,
                "startTime" to System.currentTimeMillis(),
                "cheatingDetectionEnabled" to false
            )
            meetingRef.set(data)
                .addOnSuccessListener {
                    saveParticipantData(meetingId, participantId, displayName, callback)
                }
                .addOnFailureListener {
                    Log.e("FirebaseManager", "Failed to create meeting: ${it.message}")
                    callback(false)
                }
        }
    }.addOnFailureListener {
        Log.e("FirebaseManager", "Failed to fetch meeting: ${it.message}")
        callback(false)
    }
}

    private fun saveParticipantData(
        meetingId: String,
        participantId: String,
        displayName: String,
        callback: (Boolean) -> Unit
    ) {
        val participant = ParticipantData(
            participantId = participantId,
            displayName = displayName,
            meetingId = meetingId,
            joinTime = System.currentTimeMillis(),
            leaveTime = System.currentTimeMillis()
        )

        db.collection("participants")
            .document("$meetingId-$participantId")
            .set(participant)
            .addOnSuccessListener {
                Log.d("FirebaseManager", "Participant added: $participantId to meeting: $meetingId")
                callback(true)
            }
            .addOnFailureListener {
                Log.e("FirebaseManager", "Failed to add participant: ${it.message}")
                callback(false)
            }
    }


    fun removeParticipant(meetingId: String, participantId: String, callback: (Boolean) -> Unit) {
        // تحديث وقت مغادرة المشارك
        db.collection("participants")
            .document("$meetingId-$participantId")
            .update("leaveTime", System.currentTimeMillis())
            .addOnSuccessListener {
                Log.d("FirebaseManager", "Participant removed: $participantId from meeting: $meetingId")
                callback(true)
            }
            .addOnFailureListener {
                Log.e("FirebaseManager", "Failed to remove participant: ${it.message}")
                callback(false)
            }
    }

    fun endMeeting(meetingId: String, callback: (Boolean) -> Unit) {
        if (meetingId.isEmpty()) {
            Log.e("FirebaseManager", "Invalid meetingId: cannot be empty")
            callback(false)
            return
        }
        val meetingRef = db.collection("meetings").document(meetingId)
        meetingRef.get()
            .addOnSuccessListener { document ->
                if (!document.exists()) {
                    Log.e("FirebaseManager", "Meeting document does not exist: $meetingId")
                    callback(false)
                } else {
                    meetingRef.update("endTime", System.currentTimeMillis())
                        .addOnSuccessListener {
                            Log.d("FirebaseManager", "Meeting ended: $meetingId")
                            callback(true)
                        }
                        .addOnFailureListener {
                            Log.e("FirebaseManager", "Failed to end meeting: ${it.message}")
                            callback(false)
                        }
                }
            }
            .addOnFailureListener {
                Log.e("FirebaseManager", "Failed to get meeting document: ${it.message}")
                callback(false)
            }
    }

    fun addDetectionResult(meetingId: String, participantId: String, result: Any, callback: (Boolean) -> Unit) {
        val resultId = UUID.randomUUID().toString()
        val timestamp = System.currentTimeMillis()
        val resultData = when (result) {
            is CheatingResult -> {
                DetectionResultData(
                    resultId = resultId,
                    meetingId = meetingId,
                    participantId = participantId,
                    timestamp = timestamp,
                    resultType = "cheating",
                    resultJson = Gson().toJson(result),
                    isMobile = result.Mobile,
                    noAttendanceTime = result.No_attendance_time,
                    peopleCount = result.People_count,
                    peopleTime = result.People_time,
                    percentage = result.Percentage_of_cheating,
                    sleepTime = result.Sleep_time
                )
            }
            is AttentionResult -> {
                DetectionResultData(
                    resultId = resultId,
                    meetingId = meetingId,
                    participantId = participantId,
                    timestamp = timestamp,
                    resultType = "attention",
                    resultJson = Gson().toJson(result),
                    isMobile = result.Mobile,
                    noAttendanceTime = result.No_attendance_time,
                    peopleCount = result.People_count,
                    peopleTime = result.People_time,
                    percentage = result.Percentage_of_attention,
                    sleepTime = result.Sleep_time
                )
            }
            else -> null
        }

        if (resultData != null) {
            db.collection("results")
                .document(resultId)
                .set(resultData)
                .addOnSuccessListener {
                    Log.d("FirebaseManager", "Result added: $resultId for participant: $participantId in meeting: $meetingId")
                    callback(true)
                }
                .addOnFailureListener {
                    Log.e("FirebaseManager", "Failed to add result: ${it.message}")
                    callback(false)
                }
        } else {
            Log.e("FirebaseManager", "Invalid result type")
            callback(false)
        }
    }
    fun getMeetingResults(meetingId: String, callback: (List<DetectionResultData>) -> Unit) {
        db.collection("results")
            .whereEqualTo("meetingId", meetingId)
            .get()
            .addOnSuccessListener { documents ->
                val results = documents.map { it.toObject(DetectionResultData::class.java) }
                Log.d("FirebaseManager", "Got ${results.size} results for meeting: $meetingId")
                callback(results)
            }
            .addOnFailureListener {
                Log.e("FirebaseManager", "Failed to get meeting results: ${it.message}")
                callback(emptyList())
            }
    }
    fun getMeetingParticipants(meetingId: String, callback: (List<ParticipantData>) -> Unit) {
        db.collection("participants")
            .whereEqualTo("meetingId", meetingId)
            .get()
            .addOnSuccessListener { documents ->
                val participants = documents.map { it.toObject(ParticipantData::class.java) }
                Log.d("FirebaseManager", "Got ${participants.size} participants for meeting: $meetingId")
                callback(participants)
            }
            .addOnFailureListener {
                Log.e("FirebaseManager", "Failed to get meeting participants: ${it.message}")
                callback(emptyList())
            }
    }
}

