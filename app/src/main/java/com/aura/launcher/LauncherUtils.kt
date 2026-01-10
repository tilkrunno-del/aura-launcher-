package com.aura.launcher

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatDelegate

object LauncherUtils {

    fun launchApp(context: Context, app: AppInfo) {
        val intent = Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
            setClassName(app.packageName, app.className)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(intent)
    }

    // ===== Settings helpers =====

    const val THEME_LIGHT = 0
    const val THEME_DARK = 1
    const val THEME_SYSTEM = 2

    fun applyNightMode(mode: Int) {
        when (mode) {
            THEME_LIGHT -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            THEME_DARK -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            THEME_SYSTEM -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        }
    }

    fun getSpanCount(context: Context): Int {
        val prefs = context.getSharedPreferences("aura_settings", Context.MODE_PRIVATE)
        return prefs.getInt("span_count", 4)
    }

    fun setSpanCount(context: Context, count: Int) {
        context.getSharedPreferences("aura_settings", Context.MODE_PRIVATE)
            .edit()
            .putInt("span_count", count)
            .apply()
    }

    fun isAnimEnabled(context: Context): Boolean {
        val prefs = context.getSharedPreferences("aura_settings", Context.MODE_PRIVATE)
        return prefs.getBoolean("anim_enabled", true)
    }

    fun setAnimEnabled(context: Context, enabled: Boolean) {
        context.getSharedPreferences("aura_settings", Context.MODE_PRIVATE)
            .edit()
            .putBoolean("anim_enabled", enabled)
            .apply()
    }
}
