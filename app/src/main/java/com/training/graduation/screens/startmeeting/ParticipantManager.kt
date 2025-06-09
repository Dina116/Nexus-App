package com.training.graduation.screens.startmeeting

import android.util.Log

class ParticipantManager {
    private val participants = mutableMapOf<String, String>()
    private val participantsLock = Any()
    private var currentParticipantId: String? = null
    fun addParticipant(participantId: String, displayName: String) {
        synchronized(participantsLock) {
            participants[participantId] = displayName
            Log.d("ParticipantManager", "Added participant: $displayName ($participantId)")
        }
    }
    fun removeParticipant(participantId: String) {
        synchronized(participantsLock) {
            val displayName = participants.remove(participantId)
            Log.d("ParticipantManager", "Removed participant: $displayName ($participantId)")
        }
    }
    fun getParticipants(): Map<String, String> {
        synchronized(participantsLock) {
            return participants.toMap()
        }
    }
    fun getCurrentParticipantId(): String? {
        synchronized(participantsLock) {
            return currentParticipantId
        }
    }

    fun setCurrentParticipantId(participantId: String) {
        synchronized(participantsLock) {
            currentParticipantId = participantId
        }
    }



    fun getParticipantName(participantId: String): String {
        synchronized(participantsLock) {
            return participants[participantId] ?: "Unknown Participant"
        }
    }
    fun hasParticipant(participantId: String): Boolean {
        synchronized(participantsLock) {
            return participants.containsKey(participantId)
        }
    }
    fun getParticipantCount(): Int {
        synchronized(participantsLock) {
            return participants.size
        }
    }
    fun clearParticipants() {
        synchronized(participantsLock) {
            participants.clear()
        }
    }

    companion object {
        @Volatile
        private var instance: ParticipantManager? = null

        fun getInstance(): ParticipantManager {
            return instance ?: synchronized(this) {
                instance ?: ParticipantManager().also { instance = it }
            }
        }
    }

//    companion object {
//        private var instance: ParticipantManager? = null
//
//        fun getInstance(): ParticipantManager {
//            if (instance == null) {
//                instance = ParticipantManager()
//            }
//            return instance!!
//        }
//    }
}