package com.aura.launcher

import android.content.Intent
import android.os.Bundle
import android.view.KeyEvent
import android.view.inputmethod.EditorInfo
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        // 🌙 rakenda teema enne UI-d
        ThemePrefs.applyNightMode(this)

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val searchInput = findViewById<EditText>(R.id.searchInput)
        val btnOpenApps = findViewById<Button>(R.id.btnOpenApps)
        val btnSettings = findViewById<Button>(R.id.btnSettings)

        // 🔍 Otsing: Enter / Search / Done
        searchInput.setOnEditorActionListener { _, actionId, event ->
            val imeAction =
                actionId == EditorInfo.IME_ACTION_SEARCH ||
                actionId == EditorInfo.IME_ACTION_DONE

            val enterKey =
                event?.keyCode == KeyEvent.KEYCODE_ENTER &&
                event.action == KeyEvent.ACTION_DOWN

            if (imeAction || enterKey) {
                openApps(searchInput.text.toString())
                true
            } else {
                false
            }
        }

        // 📱 Ava kõik rakendused
        btnOpenApps.setOnClickListener {
            openApps(searchInput.text.toString())
        }

        // ⚙️ Seaded
        btnSettings.setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }

        // Kui Activity avatakse query-ga (nt tulevikus)
        intent.getStringExtra(AppsActivity.EXTRA_QUERY)?.let { query ->
            if (query.isNotBlank()) {
                openApps(query)
            }
        }
    }

    private fun openApps(query: String) {
        val intent = Intent(this, AppsActivity::class.java)
        intent.putExtra(AppsActivity.EXTRA_QUERY, query.trim())
        startActivity(intent)
    }
}
