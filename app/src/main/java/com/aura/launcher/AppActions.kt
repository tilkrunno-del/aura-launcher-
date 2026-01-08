package com.aura.launcher

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings

object AppActions {

    fun openAppInfo(ctx: Context, packageName: String) {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.parse("package:$packageName")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        try { ctx.startActivity(intent) } catch (_: Exception) {}
    }

    fun uninstallApp(ctx: Context, packageName: String) {
        val intent = Intent(Intent.ACTION_DELETE).apply {
            data = Uri.parse("package:$packageName")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        try { ctx.startActivity(intent) } catch (_: Exception) {}
    }
}
