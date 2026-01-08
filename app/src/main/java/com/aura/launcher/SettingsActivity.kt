package com.aura.launcher

import android.os.Bundle
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.SeekBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat

class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        ThemePrefs.applyNightMode(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        val themeGroup = findViewById<RadioGroup>(R.id.themeGroup)
        val radioSystem = findViewById<RadioButton>(R.id.radioSystem)
        val radioLight = findViewById<RadioButton>(R.id.radioLight)
        val radioDark = findViewById<RadioButton>(R.id.radioDark)

        val columnsValue = findViewById<TextView>(R.id.columnsValue)
        val columnsSeek = findViewById<SeekBar>(R.id.columnsSeek)

        val animSwitch = findViewById<SwitchCompat>(R.id.animSwitch)

        // --- init values ---
        when (ThemePrefs.getThemeMode(this)) {
            ThemePrefs.THEME_LIGHT -> radioLight.isChecked = true
            ThemePrefs.THEME_DARK -> radioDark.isChecked = true
            else -> radioSystem.isChecked = true
        }

        val currentSpan = ThemePrefs.getSpanCount(this)
        columnsValue.text = currentSpan.toString()
        columnsSeek.max = 2              // 0..2 => 3..5
        columnsSeek.progress = currentSpan - 3

        animSwitch.isChecked = ThemePrefs.isAnimEnabled(this)

        // --- listeners ---
        themeGroup.setOnCheckedChangeListener { _, checkedId ->
            val mode = when (checkedId) {
                R.id.radioLight -> ThemePrefs.THEME_LIGHT
                R.id.radioDark -> ThemePrefs.THEME_DARK
                else -> ThemePrefs.THEME_SYSTEM
            }
            ThemePrefs.setThemeMode(this, mode)
            ThemePrefs.applyNightMode(this)
            recreate()
        }

        columnsSeek.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                val span = 3 + progress
                columnsValue.text = span.toString()
                ThemePrefs.setSpanCount(this@SettingsActivity, span)
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        animSwitch.setOnCheckedChangeListener { _, isChecked ->
            ThemePrefs.setAnimEnabled(this, isChecked)
        }
    }
}
