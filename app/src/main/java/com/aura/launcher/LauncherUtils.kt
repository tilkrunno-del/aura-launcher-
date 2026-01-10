package com.aura.launcher

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.widget.Toast

object LauncherUtils {

    /**
     * Käivita rakendus package + class nimega.
     * Kui className ei tööta, proovitakse launch intenti package järgi.
     */
    fun launchApp(
        context: Context,
        packageName: String,
        className: String? = null
    ) {
        try {
            val intent = if (!className.isNullOrBlank()) {
                Intent().apply {
                    setClassName(packageName, className)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
            } else {
                context.packageManager.getLaunchIntentForPackage(packageName)
            }

            if (intent != null) {
                context.startActivity(intent)
            } else {
                Toast.makeText(
                    context,
                    "Rakendust ei saa avada",
                    Toast.LENGTH_SHORT
                ).show()
            }
        } catch (e: Exception) {
            Toast.makeText(
                context,
                "Viga rakenduse avamisel",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    /**
     * Kontrollib, kas rakendus on süsteemis olemas
     */
    fun isAppInstalled(context: Context, packageName: String): Boolean {
        return try {
            context.packageManager.getPackageInfo(packageName, 0)
            true
        } catch (e: PackageManager.NameNotFoundException) {
            false
        }
    }
}
