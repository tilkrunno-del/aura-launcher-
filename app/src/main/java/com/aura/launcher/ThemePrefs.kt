package com.aura.launcher

import android.content.Context
import androidx.appcompat.app.AppCompatDelegate

object ThemePrefs {
    private const val PREFS = "aura_prefs"
    private const val KEY_MODE = "theme_mode"

    const val MODE_FOLLOW_SYSTEM = 0
    const val MODE_LIGHT = 1
    const val MODE_DARK = 2

    fun getThemeMode(context: Context): Int {
        val sp = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        return sp.getInt(KEY_MODE, MODE_FOLLOW_SYSTEM)
    }

    fun setThemeMode(context: Context, mode: Int) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .putInt(KEY_MODE, mode)
            .apply()
        applyTheme(mode)
    }

    fun applySavedTheme(context: Context) {
        applyTheme(getThemeMode(context))
    }

    private fun applyTheme(mode: Int) {
        val nightMode = when (mode) {
            MODE_LIGHT -> AppCompatDelegate.MODE_NIGHT_NO
            MODE_DARK -> AppCompatDelegate.MODE_NIGHT_YES
            else -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
        }
        AppCompatDelegate.setDefaultNightMode(nightMode)
    }
}
