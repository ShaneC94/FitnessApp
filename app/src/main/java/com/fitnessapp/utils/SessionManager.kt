package com.fitnessapp.utils

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit

// Manages the user login session
// Stores, retrieves, and clears session data using SharedPreferences
// Allows the app to persist user login after app closure
class SessionManager(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("fitness_session", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_USER_ID = "user_id"
    }

    fun saveUserSession(userId: Int) {
        prefs.edit { putInt(KEY_USER_ID, userId) }
    }

    fun getUserId(): Int? {
        val id = prefs.getInt(KEY_USER_ID, -1)
        return if (id == -1) null else id
    }

    fun clearSession() {
        prefs.edit { clear() }
    }
}
