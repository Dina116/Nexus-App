package com.training.graduation.screens.sharedprefrence

import android.content.Context
import android.content.SharedPreferences

class PreferenceManager(context: Context) {
    private val preferences: SharedPreferences =
        context.getSharedPreferences("language_pref", Context.MODE_PRIVATE)

    fun saveLanguage(languageCode: String) {
        preferences.edit().putString("selected_language", languageCode).apply()
    }
    fun getLanguage(): String {
        return preferences.getString("selected_language", "en") ?: "en" // Default to English
    }
    // ----- userId -----
    fun saveUserId(userId: String) {
        preferences.edit().putString("user_id", userId).apply()
    }

    fun getUserId(): String? {
        return preferences.getString("user_id", null)
    }

    fun clearUserData() {
        preferences.edit().remove("user_id").apply()
    }

    fun setFirstTime(isFirstTime: Boolean) {
        preferences.edit().putBoolean("is_first_time", isFirstTime).apply()
    }

    fun isFirstTime(): Boolean {
        return preferences.getBoolean("is_first_time", true)
    }
}
