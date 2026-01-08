package com.aura.launcher

import android.content.Context

object AppPrefs {
    private const val PREFS_NAME = "aura_prefs"
    private const val KEY_THEME_MODE = "theme_mode"

    // 0 = FOLLOW_SYSTEM, 1 = LIGHT, 2 = DARK
    const val MODE_FOLLOW_SYSTEM = 0
    const val MODE_LIGHT = 1
    const val MODE_DARK = 2

    private fun prefs(context: Context) =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun getThemeMode(context: Context): Int =
        prefs(context).getInt(KEY_THEME_MODE, MODE_FOLLOW_SYSTEM)

    fun setThemeMode(context: Context, mode: Int) {
        prefs(context).edit().putInt(KEY_THEME_MODE, mode).apply()
    }
}
