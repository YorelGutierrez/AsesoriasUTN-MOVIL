package com.example.asesoriasutn.presentation

import android.content.Context
import android.content.SharedPreferences

class SessionManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("user_session_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_USER_NAME = "user_name"
        private const val KEY_USER_EMAIL = "user_email"
        private const val KEY_USER_ROLE = "user_role"
    }

    fun saveSession(name: String, email: String, role: String) {
        prefs.edit().apply {
            putString(KEY_USER_NAME, name)
            putString(KEY_USER_EMAIL, email)
            putString(KEY_USER_ROLE, role)
            apply()
        }
    }

    fun getUserName(): String? = prefs.getString(KEY_USER_NAME, "Vanessa")
    fun getUserEmail(): String? = prefs.getString(KEY_USER_EMAIL, "vanessa@utnay.edu.mx")
    fun getUserRole(): String? = prefs.getString(KEY_USER_ROLE, "alumno")

    fun clearSession() {
        prefs.edit().clear().apply()
    }
}
