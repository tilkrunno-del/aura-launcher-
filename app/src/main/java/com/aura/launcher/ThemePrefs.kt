package com.aura.launcher

import android.content.Context
import androidx.appcompat.app.AppCompatDelegate

object ThemePrefs {
    private const val PREFS = "aura_launcher_prefs"
    private const val KEY_THEME_MODE = "theme_mode" // system / light / dark
    private const val KEY_SPAN_COUNT = "span_count" // 3..5
    private const val KEY_ANIM_ENABLED = "anim_enabled"

    const val THEME_SYSTEM = "system"
    const val THEME_LIGHT = "light"
    const val THEME_DARK = "dark"

    fun applyNightMode(context: Context) {
        when (getThemeMode(context)) {
            THEME_LIGHT -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            THEME_DARK -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            else -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        }
    }

    fun getThemeMode(context: Context): String {
        return context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getString(KEY_THEME_MODE, THEME_SYSTEM) ?: THEME_SYSTEM
    }

    fun setThemeMode(context: Context, mode: String) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit().putString(KEY_THEME_MODE, mode).apply()
    }

    fun getSpanCount(context: Context): Int {
        val v = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getInt(KEY_SPAN_COUNT, 3)
        return v.coerceIn(3, 5)
    }

    fun setSpanCount(context: Context, span: Int) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit().putInt(KEY_SPAN_COUNT, span.coerceIn(3, 5)).apply()
    }

    fun isAnimEnabled(context: Context): Boolean {
        return context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getBoolean(KEY_ANIM_ENABLED, true)
    }

    fun setAnimEnabled(context: Context, enabled: Boolean) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit().putBoolean(KEY_ANIM_ENABLED, enabled).apply()
    }
}
