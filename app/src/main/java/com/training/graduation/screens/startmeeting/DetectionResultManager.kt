package com.training.graduation.screens.startmeeting

import android.util.Log

object DetectionResultManager {
    private val detectionResults = mutableListOf<CameraManager.DetectionResult>()
    private val resultsLock = Any()

    fun addResult(result: CameraManager.DetectionResult) {
        synchronized(resultsLock) {
            detectionResults.add(result)
            Log.d("DetectionResultManager", "Added result, total: ${detectionResults.size}")
        }
    }

    fun getResults(): List<CameraManager.DetectionResult> {
        synchronized(resultsLock) {
            return detectionResults.toList()
        }
    }

    fun clearResults() {
        synchronized(resultsLock) {
            detectionResults.clear()
            Log.d("DetectionResultManager", "Cleared all results")
        }
    }
}
