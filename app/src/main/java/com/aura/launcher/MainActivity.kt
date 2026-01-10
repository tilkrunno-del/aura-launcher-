package com.aura.launcher

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Switch
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        ThemePrefs.applySavedTheme(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val btnApps: Button = findViewById(R.id.btnApps)
        val btnHidden: Button = findViewById(R.id.btnHiddenApps)

        val swFollowSystem: Switch = findViewById(R.id.swFollowSystem)
        val swDark: Switch = findViewById(R.id.swDark)

        btnApps.setOnClickListener {
            startActivity(Intent(this, AppsActivity::class.java))
        }

        btnHidden.setOnClickListener {
            startActivity(Intent(this, HiddenAppsActivity::class.java))
        }

        // Theme UI (lihtne)
        val mode = ThemePrefs.getThemeMode(this)
        swFollowSystem.isChecked = mode == ThemePrefs.MODE_FOLLOW_SYSTEM
        swDark.isChecked = mode == ThemePrefs.MODE_DARK

        swFollowSystem.setOnCheckedChangeListener { _, checked ->
            if (checked) {
                swDark.isChecked = false
                ThemePrefs.setThemeMode(this, ThemePrefs.MODE_FOLLOW_SYSTEM)
                recreate()
            } else if (!swDark.isChecked) {
                ThemePrefs.setThemeMode(this, ThemePrefs.MODE_LIGHT)
                recreate()
            }
        }

        swDark.setOnCheckedChangeListener { _, checked ->
            if (checked) {
                swFollowSystem.isChecked = false
                ThemePrefs.setThemeMode(this, ThemePrefs.MODE_DARK)
                recreate()
            } else if (!swFollowSystem.isChecked) {
                ThemePrefs.setThemeMode(this, ThemePrefs.MODE_LIGHT)
                recreate()
            }
        }
    }
}
