package com.aura.launcher

import android.content.Context

object AppPrefs {
    private const val PREFS = "aura_launcher_prefs"
    private const val KEY_FAVORITES = "favorites"
    private const val KEY_HIDDEN = "hidden"

    private fun prefs(ctx: Context) = ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    fun getFavorites(ctx: Context): MutableSet<String> =
        prefs(ctx).getStringSet(KEY_FAVORITES, emptySet())?.toMutableSet() ?: mutableSetOf()

    fun setFavorites(ctx: Context, set: Set<String>) {
        prefs(ctx).edit().putStringSet(KEY_FAVORITES, set).apply()
    }

    fun toggleFavorite(ctx: Context, key: String): Boolean {
        val fav = getFavorites(ctx)
        val nowFav = if (fav.contains(key)) {
            fav.remove(key); false
        } else {
            fav.add(key); true
        }
        setFavorites(ctx, fav)
        return nowFav
    }

    fun isFavorite(ctx: Context, key: String): Boolean =
        getFavorites(ctx).contains(key)

    fun getHidden(ctx: Context): MutableSet<String> =
        prefs(ctx).getStringSet(KEY_HIDDEN, emptySet())?.toMutableSet() ?: mutableSetOf()

    fun setHidden(ctx: Context, set: Set<String>) {
        prefs(ctx).edit().putStringSet(KEY_HIDDEN, set).apply()
    }

    fun toggleHidden(ctx: Context, key: String): Boolean {
        val hidden = getHidden(ctx)
        val nowHidden = if (hidden.contains(key)) {
            hidden.remove(key); false
        } else {
            hidden.add(key); true
        }
        setHidden(ctx, hidden)
        return nowHidden
    }

    fun isHidden(ctx: Context, key: String): Boolean =
        getHidden(ctx).contains(key)
}
