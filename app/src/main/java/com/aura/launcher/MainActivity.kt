package com.aura.launcher

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.SwitchCompat

class MainActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_QUERY = "extra_query"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        applySavedTheme() // oluline: enne setContentView

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val searchInput = findViewById<EditText>(R.id.searchInput)
        val btnOpenApps = findViewById<Button>(R.id.btnOpenApps)
        val btnSetDefaultLauncher = findViewById<Button>(R.id.btnSetDefaultLauncher)

        val switchFollowSystem = findViewById<SwitchCompat>(R.id.switchFollowSystem)
        val switchDarkMode = findViewById<SwitchCompat>(R.id.switchDarkMode)

        // --- Teema UI init ---
        val mode = AppPrefs.getThemeMode(this)

        when (mode) {
            AppPrefs.MODE_FOLLOW_SYSTEM -> {
                switchFollowSystem.isChecked = true
                switchDarkMode.isEnabled = false
                switchDarkMode.isChecked = false
            }
            AppPrefs.MODE_LIGHT -> {
                switchFollowSystem.isChecked = false
                switchDarkMode.isEnabled = true
                switchDarkMode.isChecked = false
            }
            AppPrefs.MODE_DARK -> {
                switchFollowSystem.isChecked = false
                switchDarkMode.isEnabled = true
                switchDarkMode.isChecked = true
            }
        }

        switchFollowSystem.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                // Follow System
                AppPrefs.setThemeMode(this, AppPrefs.MODE_FOLLOW_SYSTEM)
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
                switchDarkMode.isEnabled = false
                switchDarkMode.isChecked = false
            } else {
                // Kui kasutaja lülitab follow-system OFF, jääme LIGHT peale (kuni ta darki sisse paneb)
                AppPrefs.setThemeMode(this, AppPrefs.MODE_LIGHT)
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                switchDarkMode.isEnabled = true
                switchDarkMode.isChecked = false
            }
            recreate()
        }

        switchDarkMode.setOnCheckedChangeListener { _, isChecked ->
            // Kui follow-system ON, ignoreerime
            if (switchFollowSystem.isChecked) return@setOnCheckedChangeListener

            if (isChecked) {
                AppPrefs.setThemeMode(this, AppPrefs.MODE_DARK)
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            } else {
                AppPrefs.setThemeMode(this, AppPrefs.MODE_LIGHT)
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            }
            recreate()
        }

        // --- Nupud (jäävad sul samaks) ---
        btnOpenApps.setOnClickListener {
            val i = Intent(this, AppsActivity::class.java)
            i.putExtra(EXTRA_QUERY, searchInput.text?.toString().orEmpty())
            startActivity(i)
        }

        btnSetDefaultLauncher.setOnClickListener {
            // launcher default-setup (sul võib olla juba teine loogika)
            val intent = Intent(android.provider.Settings.ACTION_HOME_SETTINGS)
            startActivity(intent)
        }
    }

    private fun applySavedTheme() {
        when (AppPrefs.getThemeMode(this)) {
            AppPrefs.MODE_FOLLOW_SYSTEM ->
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
            AppPrefs.MODE_LIGHT ->
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            AppPrefs.MODE_DARK ->
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        }
    }
}
