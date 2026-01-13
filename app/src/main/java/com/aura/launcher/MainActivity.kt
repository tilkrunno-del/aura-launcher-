package com.aura.launcher

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Button
import android.widget.Switch
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate

class MainActivity : AppCompatActivity() {

    private lateinit var prefs: SharedPreferences

    private lateinit var btnOpenApps: Button
    private lateinit var btnHiddenApps: Button

    private lateinit var switchFollowSystem: Switch
    private lateinit var switchDarkMode: Switch

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

        // Rakenda teema enne layouti
        applyThemeFromPrefs()

        setContentView(R.layout.activity_main)

        btnOpenApps = findViewById(R.id.btnOpenApps)
        btnHiddenApps = findViewById(R.id.btnHiddenApps)

        switchFollowSystem = findViewById(R.id.switchFollowSystem)
        switchDarkMode = findViewById(R.id.switchDarkMode)

        setupButtons()
        setupThemeSwitches()
    }

    override fun onResume() {
        super.onResume()
        // Kui keegi muutis teemat mujal, sünkrooni lülitid
        syncSwitchStates()
    }

    private fun setupButtons() {
        btnOpenApps.setOnClickListener {
            startActivity(Intent(this, AppsActivity::class.java))
        }

        // PIN-lukk enne peidetud rakendusi
        btnHiddenApps.setOnClickListener {
            startActivity(Intent(this, PinLockActivity::class.java))
        }
    }

    private fun setupThemeSwitches() {
        syncSwitchStates()

        switchFollowSystem.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit().putBoolean(KEY_FOLLOW_SYSTEM, isChecked).apply()

            // Kui follow system ON -> dark switch ei määra midagi (võib olla disable)
            if (isChecked) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
            } else {
                val dark = prefs.getBoolean(KEY_DARK_MODE, false)
                AppCompatDelegate.setDefaultNightMode(
                    if (dark) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO
                )
            }

            // värskenda UI
            syncSwitchStates()
        }

        switchDarkMode.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit().putBoolean(KEY_DARK_MODE, isChecked).apply()

            val follow = prefs.getBoolean(KEY_FOLLOW_SYSTEM, true)
            if (!follow) {
                AppCompatDelegate.setDefaultNightMode(
                    if (isChecked) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO
                )
            }

            // värskenda UI
            syncSwitchStates()
        }
    }

    private fun syncSwitchStates() {
        val follow = prefs.getBoolean(KEY_FOLLOW_SYSTEM, true)
        val dark = prefs.getBoolean(KEY_DARK_MODE, false)

        // Et vältida “loopi”, eemalda listenerid korraks
        switchFollowSystem.setOnCheckedChangeListener(null)
        switchDarkMode.setOnCheckedChangeListener(null)

        switchFollowSystem.isChecked = follow
        switchDarkMode.isChecked = dark

        // Kui follow system ON, siis dark toggle disable (sul oli pildil hall)
        switchDarkMode.isEnabled = !follow

        // Pane listenerid tagasi
        setupThemeSwitchListenersAgain()
    }

    private fun setupThemeSwitchListenersAgain() {
        switchFollowSystem.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit().putBoolean(KEY_FOLLOW_SYSTEM, isChecked).apply()

            if (isChecked) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
            } else {
                val dark = prefs.getBoolean(KEY_DARK_MODE, false)
                AppCompatDelegate.setDefaultNightMode(
                    if (dark) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO
                )
            }

            syncSwitchStates()
        }

        switchDarkMode.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit().putBoolean(KEY_DARK_MODE, isChecked).apply()

            val follow = prefs.getBoolean(KEY_FOLLOW_SYSTEM, true)
            if (!follow) {
                AppCompatDelegate.setDefaultNightMode(
                    if (isChecked) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO
                )
            }

            syncSwitchStates()
        }
    }

    private fun applyThemeFromPrefs() {
        val follow = prefs.getBoolean(KEY_FOLLOW_SYSTEM, true)
        val dark = prefs.getBoolean(KEY_DARK_MODE, false)

        AppCompatDelegate.setDefaultNightMode(
            if (follow) {
                AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
            } else {
                if (dark) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO
            }
        )
    }

    companion object {
        private const val PREFS_NAME = "aura_launcher_prefs"
        private const val KEY_FOLLOW_SYSTEM = "theme_follow_system"
        private const val KEY_DARK_MODE = "theme_dark"
    }
}
