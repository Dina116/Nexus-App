package com.training.graduation.screens.startmeeting

import android.util.Log

class ParticipantManager {
    private val participants = mutableMapOf<String, String>()
    private val participantsLock = Any()
    private var currentParticipantId: String? = null
    private var activeParticipantId: String? = null

    companion object {
        @Volatile
        private var instance: ParticipantManager? = null

        fun getInstance(): ParticipantManager {
            return instance ?: synchronized(this) {
                instance ?: ParticipantManager().also { instance = it }
            }
        }
    }
fun addParticipant(participantId: String, displayName: String) {
    participants[participantId] = displayName
    if (activeParticipantId == null) {
        activeParticipantId = participantId
    }
}
fun removeParticipant(participantId: String) {
    participants.remove(participantId)
    if (activeParticipantId == participantId) {
        activeParticipantId = participants.keys.firstOrNull()
    }
}
    fun setActiveParticipant(participantId: String) {
        if (participants.containsKey(participantId)) {
            activeParticipantId = participantId
        }
    }

    fun getParticipants(): Map<String, String> = participants.toMap()

    fun getCurrentParticipantId(): String? {
        return activeParticipantId
    }
    fun setCurrentParticipantId(participantId: String) {
        synchronized(participantsLock) {
            currentParticipantId = participantId
        }
    }
    fun getParticipantName(participantId: String): String? = participants[participantId]
    fun hasParticipant(participantId: String): Boolean {
        synchronized(participantsLock) {
            return participants.containsKey(participantId)
        }
    }
    fun getParticipantCount(): Int = participants.size
    fun clearParticipants() {
        synchronized(participantsLock) {
            participants.clear()
            activeParticipantId = null
        }
    }
}