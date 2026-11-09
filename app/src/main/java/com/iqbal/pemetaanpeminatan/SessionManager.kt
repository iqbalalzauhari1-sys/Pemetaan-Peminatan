package com.iqbal.pemetaanpeminatan

import android.content.Context
import android.content.SharedPreferences

class SessionManager(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    companion object {
        private const val PREF_NAME = "LoginPrefs"
        private const val KEY_NISN = "nisn"
        private const val KEY_USER_ROLE = "user_role"
        private const val KEY_IS_LOGGED_IN = "is_logged_in"
    }

    fun simpanSesi(nisn: String, role: String) {
        prefs.edit()
            .putString(KEY_NISN, nisn)
            .putString(KEY_USER_ROLE, role)
            .putBoolean(KEY_IS_LOGGED_IN, true)
            .apply()
    }

    fun getNisn(): String? {
        return prefs.getString(KEY_NISN, null)
    }

    fun getRole(): String? {
        return prefs.getString(KEY_USER_ROLE, null)
    }

    fun isLogin(): Boolean {
        return prefs.getBoolean(KEY_IS_LOGGED_IN, false)
    }

    fun logout() {
        prefs.edit().clear().apply()
    }

    // Helper methods for token/session management compatibility
    fun saveAuthToken(token: String) {
        prefs.edit()
            .putString(KEY_NISN, token)
            .putBoolean(KEY_IS_LOGGED_IN, true)
            .apply()
    }

    fun fetchAuthToken(): String? {
        return getNisn()
    }

    fun saveUserRole(role: String) {
        prefs.edit().putString(KEY_USER_ROLE, role).apply()
    }

    fun fetchUserRole(): String? {
        return getRole()
    }

    fun clearSession() {
        logout()
    }

    fun isLoggedIn(): Boolean {
        return isLogin()
    }
}
