package com.aura.launcher

import android.content.Context

object AppPrefs {
    private const val PREFS = "aura_prefs"
    private const val KEY_FAVS = "favorites"
    private const val KEY_HIDDEN = "hidden"

    private fun prefs(ctx: Context) = ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    fun isFavorite(ctx: Context, pkg: String): Boolean =
        prefs(ctx).getStringSet(KEY_FAVS, emptySet())?.contains(pkg) == true

    fun isHidden(ctx: Context, pkg: String): Boolean =
        prefs(ctx).getStringSet(KEY_HIDDEN, emptySet())?.contains(pkg) == true

    fun toggleFavorite(ctx: Context, pkg: String): Boolean {
        val p = prefs(ctx)
        val set = (p.getStringSet(KEY_FAVS, emptySet()) ?: emptySet()).toMutableSet()
        val nowFav = if (set.contains(pkg)) {
            set.remove(pkg); false
        } else {
            set.add(pkg); true
        }
        p.edit().putStringSet(KEY_FAVS, set).apply()
        return nowFav
    }

    fun toggleHidden(ctx: Context, pkg: String): Boolean {
        val p = prefs(ctx)
        val set = (p.getStringSet(KEY_HIDDEN, emptySet()) ?: emptySet()).toMutableSet()
        val nowHidden = if (set.contains(pkg)) {
            set.remove(pkg); false
        } else {
            set.add(pkg); true
        }
        p.edit().putStringSet(KEY_HIDDEN, set).apply()
        return nowHidden
    }
}
