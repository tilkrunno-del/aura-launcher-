package com.aura.launcher

import android.content.Context
import androidx.appcompat.app.AppCompatDelegate

object LauncherUtils {

    private const val PREFS = "aura_prefs"

    // Theme
    private const val KEY_THEME_MODE = "theme_mode" // 0=system,1=light,2=dark

    // Grid
    private const val KEY_SPAN_COUNT = "span_count" // default 4

    // Animations
    private const val KEY_ANIM_ENABLED = "anim_enabled" // default true

    // Favorites/Hidden
    private const val KEY_FAVORITES = "favorites"
    private const val KEY_HIDDEN = "hidden"

    // --- Theme constants (SettingsActivity likely expects these names) ---
    const val THEME_FOLLOW_SYSTEM = 0
    const val THEME_LIGHT = 1
    const val THEME_DARK = 2

    // Backwards aliases (kui mõnes kohas kasutasid MODE_*)
    const val MODE_FOLLOW_SYSTEM = THEME_FOLLOW_SYSTEM
    const val MODE_LIGHT = THEME_LIGHT
    const val MODE_DARK = THEME_DARK

    fun getThemeMode(context: Context): Int {
        val sp = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        return sp.getInt(KEY_THEME_MODE, THEME_FOLLOW_SYSTEM)
    }

    fun setThemeMode(context: Context, mode: Int) {
        val sp = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        sp.edit().putInt(KEY_THEME_MODE, mode).apply()
    }

    fun applyThemeMode(context: Context) {
        when (getThemeMode(context)) {
            THEME_LIGHT -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            THEME_DARK -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            else -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        }
    }

    // --- Grid settings ---
    fun getSpanCount(context: Context): Int {
        val sp = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        return sp.getInt(KEY_SPAN_COUNT, 4)
    }

    fun setSpanCount(context: Context, span: Int) {
        val sp = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        sp.edit().putInt(KEY_SPAN_COUNT, span.coerceIn(3, 6)).apply()
    }

    // --- Animations settings ---
    fun isAnimEnabled(context: Context): Boolean {
        val sp = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        return sp.getBoolean(KEY_ANIM_ENABLED, true)
    }

    fun setAnimEnabled(context: Context, enabled: Boolean) {
        val sp = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        sp.edit().putBoolean(KEY_ANIM_ENABLED, enabled).apply()
    }

    // --- Helpers for string sets ---
    private fun getStringSet(context: Context, key: String): MutableSet<String> {
        val sp = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        return (sp.getStringSet(key, emptySet()) ?: emptySet()).toMutableSet()
    }

    private fun putStringSet(context: Context, key: String, set: Set<String>) {
        val sp = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        sp.edit().putStringSet(key, set).apply()
    }

    // --- Favorites ---
    fun getFavorites(context: Context): Set<String> = getStringSet(context, KEY_FAVORITES)

    fun isFavorite(context: Context, packageName: String): Boolean =
        getFavorites(context).contains(packageName)

    fun toggleFavorite(context: Context, packageName: String): Boolean {
        val set = getStringSet(context, KEY_FAVORITES)
        val nowFav = if (set.contains(packageName)) {
            set.remove(packageName); false
        } else {
            set.add(packageName); true
        }
        putStringSet(context, KEY_FAVORITES, set)
        return nowFav
    }

    // --- Hidden apps ---
    fun getHiddenApps(context: Context): Set<String> = getStringSet(context, KEY_HIDDEN)

    fun isHidden(context: Context, packageName: String): Boolean =
        getHiddenApps(context).contains(packageName)

    fun setHidden(context: Context, packageName: String, hidden: Boolean) {
        val set = getStringSet(context, KEY_HIDDEN)
        if (hidden) set.add(packageName) else set.remove(packageName)
        putStringSet(context, KEY_HIDDEN, set)
    }
}
