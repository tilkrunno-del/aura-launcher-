package com.aura.launcher

import android.content.Context
import androidx.appcompat.app.AppCompatDelegate

object LauncherSettings {

    const val THEME_LIGHT = 0
    const val THEME_DARK = 1
    const val THEME_SYSTEM = 2

    private const val PREFS = "aura_settings"
    private const val KEY_THEME = "theme"
    private const val KEY_SPAN = "span"
    private const val KEY_ANIM = "anim"

    fun applyNightMode(context: Context) {
        when (getTheme(context)) {
            THEME_LIGHT ->
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            THEME_DARK ->
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            THEME_SYSTEM ->
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        }
    }

    fun setTheme(context: Context, theme: Int) {
        prefs(context).edit().putInt(KEY_THEME, theme).apply()
        applyNightMode(context)
    }

    fun getTheme(context: Context): Int =
        prefs(context).getInt(KEY_THEME, THEME_SYSTEM)

    fun getSpanCount(context: Context): Int =
        prefs(context).getInt(KEY_SPAN, 4)

    fun setSpanCount(context: Context, span: Int) {
        prefs(context).edit().putInt(KEY_SPAN, span).apply()
    }

    fun isAnimEnabled(context: Context): Boolean =
        prefs(context).getBoolean(KEY_ANIM, true)

    fun setAnimEnabled(context: Context, enabled: Boolean) {
        prefs(context).edit().putBoolean(KEY_ANIM, enabled).apply()
    }

    private fun prefs(context: Context) =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
}
