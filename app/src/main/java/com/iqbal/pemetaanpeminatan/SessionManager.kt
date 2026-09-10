package com.iqbal.pemetaanpeminatan // Sesuaikan jika package Anda berbeda

import android.content.Context
import android.content.SharedPreferences

class SessionManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("SPK_SESSION", Context.MODE_PRIVATE)

    fun simpanSesi(nisn: String, role: String) {
        prefs.edit().putBoolean("IS_LOGIN", true)
            .putString("NISN", nisn)
            .putString("ROLE", role).apply()
    }

    fun isLogin(): Boolean = prefs.getBoolean("IS_LOGIN", false)
    fun getRole(): String? = prefs.getString("ROLE", null)
    fun getNisn(): String? = prefs.getString("NISN", null)

    fun logout() {
        prefs.edit().clear().apply()
    }
}