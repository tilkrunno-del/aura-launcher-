package com.aura.launcher

import android.os.Bundle
import android.widget.Button
import android.widget.Switch
import androidx.appcompat.app.AppCompatActivity

class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        LauncherSettings.applyNightMode(this)

        findViewById<Button>(R.id.btnThemeLight).setOnClickListener {
            LauncherSettings.setTheme(this, LauncherSettings.THEME_LIGHT)
            recreate()
        }

        findViewById<Button>(R.id.btnThemeDark).setOnClickListener {
            LauncherSettings.setTheme(this, LauncherSettings.THEME_DARK)
            recreate()
        }

        findViewById<Button>(R.id.btnThemeSystem).setOnClickListener {
            LauncherSettings.setTheme(this, LauncherSettings.THEME_SYSTEM)
            recreate()
        }

        val animSwitch = findViewById<Switch>(R.id.switchAnimations)
        animSwitch.isChecked = LauncherSettings.isAnimEnabled(this)

        animSwitch.setOnCheckedChangeListener { _, isChecked ->
            LauncherSettings.setAnimEnabled(this, isChecked)
        }
    }
}
