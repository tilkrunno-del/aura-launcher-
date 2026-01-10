package com.aura.launcher

import android.content.Context
import androidx.appcompat.app.AppCompatDelegate

object LauncherUtils {

    private const val PREFS = "aura_prefs"
    private const val KEY_THEME_MODE = "theme_mode" // 0=system,1=light,2=dark
    private const val KEY_FAVORITES = "favorites"
    private const val KEY_HIDDEN = "hidden"

    const val MODE_FOLLOW_SYSTEM = 0
    const val MODE_LIGHT = 1
    const val MODE_DARK = 2

    fun getThemeMode(context: Context): Int {
        val sp = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        return sp.getInt(KEY_THEME_MODE, MODE_FOLLOW_SYSTEM)
    }

    fun setThemeMode(context: Context, mode: Int) {
        val sp = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        sp.edit().putInt(KEY_THEME_MODE, mode).apply()
    }

    fun applyThemeMode(context: Context) {
        when (getThemeMode(context)) {
            MODE_LIGHT -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            MODE_DARK -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            else -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        }
    }

    private fun getStringSet(context: Context, key: String): MutableSet<String> {
        val sp = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        return (sp.getStringSet(key, emptySet()) ?: emptySet()).toMutableSet()
    }

    private fun putStringSet(context: Context, key: String, set: Set<String>) {
        val sp = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        sp.edit().putStringSet(key, set).apply()
    }

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

    fun getHiddenApps(context: Context): Set<String> = getStringSet(context, KEY_HIDDEN)

    fun isHidden(context: Context, packageName: String): Boolean =
        getHiddenApps(context).contains(packageName)

    fun setHidden(context: Context, packageName: String, hidden: Boolean) {
        val set = getStringSet(context, KEY_HIDDEN)
        if (hidden) set.add(packageName) else set.remove(packageName)
        putStringSet(context, KEY_HIDDEN, set)
    }
}
