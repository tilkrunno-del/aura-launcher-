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

    private lateinit var btnApps: Button
    private lateinit var btnHiddenApps: Button

    private lateinit var swFollowSystem: Switch
    private lateinit var swDark: Switch

    override fun onCreate(savedInstanceState: Bundle?) {
        prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        applyThemeFromPrefs()

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        btnApps = findViewById(R.id.btnApps)
        btnHiddenApps = findViewById(R.id.btnHiddenApps)

        swFollowSystem = findViewById(R.id.swFollowSystem)
        swDark = findViewById(R.id.swDark)

        btnApps.setOnClickListener {
            startActivity(Intent(this, AppsActivity::class.java))
        }

        // Kui sul on PIN-lukk, kasuta seda. Kui ei ole, vaheta HiddenAppsActivity peale.
        btnHiddenApps.setOnClickListener {
            // startActivity(Intent(this, HiddenAppsActivity::class.java))
            startActivity(Intent(this, PinLockActivity::class.java))
        }

        setupSwitches()
    }

    override fun onResume() {
        super.onResume()
        syncSwitches()
    }

    private fun setupSwitches() {
        syncSwitches()

        swFollowSystem.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit().putBoolean(KEY_FOLLOW_SYSTEM, isChecked).apply()

            if (isChecked) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
            } else {
                val dark = prefs.getBoolean(KEY_DARK_MODE, false)
                AppCompatDelegate.setDefaultNightMode(
                    if (dark) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO
                )
            }

            syncSwitches()
        }

        swDark.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit().putBoolean(KEY_DARK_MODE, isChecked).apply()

            val follow = prefs.getBoolean(KEY_FOLLOW_SYSTEM, true)
            if (!follow) {
                AppCompatDelegate.setDefaultNightMode(
                    if (isChecked) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO
                )
            }

            syncSwitches()
        }
    }

    private fun syncSwitches() {
        val follow = prefs.getBoolean(KEY_FOLLOW_SYSTEM, true)
        val dark = prefs.getBoolean(KEY_DARK_MODE, false)

        // vältida "loopi"
        swFollowSystem.setOnCheckedChangeListener(null)
        swDark.setOnCheckedChangeListener(null)

        swFollowSystem.isChecked = follow
        swDark.isChecked = dark

        // follow system ON -> dark lüliti disabled (nagu su pildil)
        swDark.isEnabled = !follow

        // pane listenerid tagasi
        setupSwitchesListenersAgain()
    }

    private fun setupSwitchesListenersAgain() {
        swFollowSystem.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit().putBoolean(KEY_FOLLOW_SYSTEM, isChecked).apply()

            if (isChecked) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
            } else {
                val dark = prefs.getBoolean(KEY_DARK_MODE, false)
                AppCompatDelegate.setDefaultNightMode(
                    if (dark) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO
                )
            }

            syncSwitches()
        }

        swDark.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit().putBoolean(KEY_DARK_MODE, isChecked).apply()

            val follow = prefs.getBoolean(KEY_FOLLOW_SYSTEM, true)
            if (!follow) {
                AppCompatDelegate.setDefaultNightMode(
                    if (isChecked) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO
                )
            }

            syncSwitches()
        }
    }

    private fun applyThemeFromPrefs() {
        val follow = prefs.getBoolean(KEY_FOLLOW_SYSTEM, true)
        val dark = prefs.getBoolean(KEY_DARK_MODE, false)

        AppCompatDelegate.setDefaultNightMode(
            if (follow) AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
            else if (dark) AppCompatDelegate.MODE_NIGHT_YES
            else AppCompatDelegate.MODE_NIGHT_NO
        )
    }

    companion object {
        private const val PREFS_NAME = "aura_launcher_prefs"
        private const val KEY_FOLLOW_SYSTEM = "theme_follow_system"
        private const val KEY_DARK_MODE = "theme_dark"
    }
}
