package com.aura.launcher

import android.content.Context

object HiddenAppsStore {
    private const val PREFS = "aura_hidden_apps"
    private const val KEY_HIDDEN = "hidden"

    fun getHidden(context: Context): Set<String> {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        return prefs.getStringSet(KEY_HIDDEN, emptySet()) ?: emptySet()
    }

    fun hide(context: Context, pkg: String) {
        val set = getHidden(context).toMutableSet()
        set.add(pkg)
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .putStringSet(KEY_HIDDEN, set)
            .apply()
    }

    fun show(context: Context, pkg: String) {
        val set = getHidden(context).toMutableSet()
        set.remove(pkg)
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .putStringSet(KEY_HIDDEN, set)
            .apply()
    }
}
