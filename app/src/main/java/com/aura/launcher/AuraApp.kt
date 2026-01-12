package com.aura.launcher

import android.app.Application
import android.content.Intent
import java.io.PrintWriter
import java.io.StringWriter
import kotlin.system.exitProcess

class AuraApp : Application() {

    override fun onCreate() {
        super.onCreate()

        val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()

        Thread.setDefaultUncaughtExceptionHandler { t, e ->
            try {
                val sw = StringWriter()
                e.printStackTrace(PrintWriter(sw))
                val stack = sw.toString()

                // Salvesta faili
                openFileOutput(CRASH_FILE, MODE_PRIVATE).use { out ->
                    out.write(stack.toByteArray())
                }

                // Ava crash-ekraan
                val i = Intent(this, CrashActivity::class.java).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                }
                startActivity(i)
            } catch (_: Throwable) {
                // ignore
            } finally {
                defaultHandler?.uncaughtException(t, e)
                // kui default handler ei tapa protsessi, tapame ise
                android.os.Process.killProcess(android.os.Process.myPid())
                exitProcess(10)
            }
        }
    }

    companion object {
        const val CRASH_FILE = "last_crash.txt"
    }
}
