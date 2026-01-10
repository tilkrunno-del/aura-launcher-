package com.aura.launcher

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private lateinit var btnOpenApps: Button
    private lateinit var btnSetDefaultLauncher: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Nupud (proovib mitu varianti ID-d, et ei crashiks kui nimi erineb)
        btnOpenApps = findRequiredButton(
            "btnOpenApps",
            "btnApps",
            "openAppsButton",
            "buttonOpenApps"
        )

        btnSetDefaultLauncher = findRequiredButton(
            "btnSetDefaultLauncher",
            "btnSetLauncher",
            "setDefaultLauncherButton",
            "buttonSetDefaultLauncher"
        )

        btnOpenApps.setOnClickListener {
            startActivity(Intent(this, AppsActivity::class.java))
        }

        btnSetDefaultLauncher.setOnClickListener {
            openDefaultAppsSettings()
        }
    }

    private fun openDefaultAppsSettings() {
        try {
            // Kõige tavalisem koht "Default apps / Home app"
            startActivity(Intent(Settings.ACTION_MANAGE_DEFAULT_APPS_SETTINGS))
        } catch (_: Throwable) {
            try {
                // Alternatiivne
                startActivity(Intent(Settings.ACTION_SETTINGS))
            } catch (e: Throwable) {
                Toast.makeText(this, "Ei saa seadeid avada: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun findRequiredButton(vararg idNames: String): Button {
        for (name in idNames) {
            val id = resources.getIdentifier(name, "id", packageName)
            if (id != 0) {
                val v = findViewById<Button>(id)
                if (v != null) return v
            }
        }
        error("Missing required Button. Tried ids: ${idNames.joinToString()}")
    }
}
