package com.aura.launcher

import android.content.Context

object HiddenAppsStore {

    private const val PREFS = "aura_hidden_apps"
    private const val KEY = "hidden"

    fun getHidden(context: Context): Set<String> {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        return prefs.getStringSet(KEY, emptySet()) ?: emptySet()
    }

    fun hide(context: Context, pkg: String) {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val set = getHidden(context).toMutableSet()
        set.add(pkg)
        prefs.edit().putStringSet(KEY, set).apply()
    }

    fun show(context: Context, pkg: String) {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val set = getHidden(context).toMutableSet()
        set.remove(pkg)
        prefs.edit().putStringSet(KEY, set).apply()
    }
}
