package com.aura.launcher

import android.content.Context

object AppStateStore {
    private const val PREFS = "aura_app_state"
    private const val KEY_FAVORITES = "favorites"
    private const val KEY_HIDDEN = "hidden"

    private fun sp(context: Context) =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    fun getFavorites(context: Context): Set<String> =
        sp(context).getStringSet(KEY_FAVORITES, emptySet()) ?: emptySet()

    fun setFavorite(context: Context, packageName: String, favorite: Boolean) {
        val set = getFavorites(context).toMutableSet()
        if (favorite) set.add(packageName) else set.remove(packageName)
        sp(context).edit().putStringSet(KEY_FAVORITES, set).apply()
    }

    fun isFavorite(context: Context, packageName: String): Boolean =
        getFavorites(context).contains(packageName)

    fun getHidden(context: Context): Set<String> =
        sp(context).getStringSet(KEY_HIDDEN, emptySet()) ?: emptySet()

    fun setHidden(context: Context, packageName: String, hidden: Boolean) {
        val set = getHidden(context).toMutableSet()
        if (hidden) set.add(packageName) else set.remove(packageName)
        sp(context).edit().putStringSet(KEY_HIDDEN, set).apply()
    }

    fun isHidden(context: Context, packageName: String): Boolean =
        getHidden(context).contains(packageName)
}
