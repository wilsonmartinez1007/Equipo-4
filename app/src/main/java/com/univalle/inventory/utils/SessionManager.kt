package com.univalle.inventory.utils

import android.content.Context
import android.content.SharedPreferences

class SessionManager(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("session", Context.MODE_PRIVATE)

    fun setLoggedIn(value: Boolean) {
        prefs.edit().putBoolean("logged_in", value).apply()
    }

    fun isLoggedIn(): Boolean = prefs.getBoolean("logged_in", false)

    fun clear() {
        prefs.edit().clear().apply()
    }
}
