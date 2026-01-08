package com.aura.launcher

import android.content.Context

object AppPrefs {
    private const val PREFS = "aura_launcher_prefs"

    private const val KEY_HIDDEN = "hidden_apps"
    private const val KEY_FAVORITES = "favorite_apps"
    private const val KEY_THEME_MODE = "theme_mode"

    // 0 = FOLLOW_SYSTEM, 1 = LIGHT, 2 = DARK
    const val MODE_FOLLOW_SYSTEM = 0
    const val MODE_LIGHT = 1
    const val MODE_DARK = 2

    private fun prefs(ctx: Context) =
        ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    // ---------- Hidden apps ----------
    fun getHidden(ctx: Context): MutableSet<String> =
        prefs(ctx).getStringSet(KEY_HIDDEN, emptySet())?.toMutableSet() ?: mutableSetOf()

    fun setHidden(ctx: Context, set: Set<String>) {
        prefs(ctx).edit().putStringSet(KEY_HIDDEN, set).apply()
    }

    fun isHidden(ctx: Context, key: String): Boolean =
        getHidden(ctx).contains(key)

    fun toggleHidden(ctx: Context, key: String): Boolean {
        val hidden = getHidden(ctx)
        val nowHidden = if (hidden.contains(key)) {
            hidden.remove(key)
            false
        } else {
            hidden.add(key)
            true
        }
        setHidden(ctx, hidden)
        return nowHidden
    }

    // ---------- Favorites ----------
    fun getFavorites(ctx: Context): MutableSet<String> =
        prefs(ctx).getStringSet(KEY_FAVORITES, emptySet())?.toMutableSet() ?: mutableSetOf()

    fun setFavorites(ctx: Context, set: Set<String>) {
        prefs(ctx).edit().putStringSet(KEY_FAVORITES, set).apply()
    }

    fun isFavorite(ctx: Context, key: String): Boolean =
        getFavorites(ctx).contains(key)

    fun toggleFavorite(ctx: Context, key: String): Boolean {
        val fav = getFavorites(ctx)
        val nowFav = if (fav.contains(key)) {
            fav.remove(key)
            false
        } else {
            fav.add(key)
            true
        }
        setFavorites(ctx, fav)
        return nowFav
    }

    // ---------- Theme mode ----------
    fun getThemeMode(ctx: Context): Int =
        prefs(ctx).getInt(KEY_THEME_MODE, MODE_FOLLOW_SYSTEM)

    fun setThemeMode(ctx: Context, mode: Int) {
        prefs(ctx).edit().putInt(KEY_THEME_MODE, mode).apply()
    }
}
