package com.aura.launcher

import android.content.Context
import android.content.Intent

object LauncherUtils {

    fun launchApp(context: Context, app: AppInfo) {
        val intent = Intent(Intent.ACTION_MAIN).apply {
            setClassName(app.packageName, app.className)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    }

    fun getHiddenApps(context: Context): List<AppInfo> {
        return emptyList() // hiljem teeme päris peitmise
    }
}
