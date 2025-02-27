package com.umc.upstyle.utils

import android.content.Context
import android.content.SharedPreferences

object TokenManager {
    private const val PREFS_NAME = "AppPrefs"
    private const val KEY_JWT_TOKEN = "JWT_TOKEN"

    fun getJwtToken(context: Context): String {
        val sharedPreferences: SharedPreferences =
            context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return sharedPreferences.getString(KEY_JWT_TOKEN, "") ?: ""
    }

    fun saveJwtToken(context: Context, token: String) {
        val sharedPreferences: SharedPreferences =
            context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        sharedPreferences.edit().putString(KEY_JWT_TOKEN, token).apply()
    }

    fun clearJwtToken(context: Context) {
        val sharedPreferences: SharedPreferences =
            context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        sharedPreferences.edit().remove(KEY_JWT_TOKEN).apply()
    }
}
