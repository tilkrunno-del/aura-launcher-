package com.aura.launcher

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.KeyEvent
import android.view.inputmethod.EditorInfo
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val searchInput = findViewById<EditText>(R.id.searchInput)
        val btnOpenApps = findViewById<Button>(R.id.btnOpenApps)
        val btnSetDefaultLauncher = findViewById<Button>(R.id.btnSetDefaultLauncher)

        // Nupp: Ava rakendused
        btnOpenApps.setOnClickListener {
            openApps(searchInput.text.toString())
        }

        // Nupp: Määra AURA vaikimisi avakuvarakenduseks
        btnSetDefaultLauncher.setOnClickListener {
            openHomeSettings()
        }

        // Klaviatuuri Search / Done / Enter
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
    }

    private fun openApps(query: String) {
        val intent = Intent(this, AppsActivity::class.java)
        intent.putExtra(AppsActivity.EXTRA_QUERY, query.trim())
        startActivity(intent)
    }

    private fun openHomeSettings() {
        // Erinevatel telefonidel on erinevad seaded – proovime mitu varianti
        val intents = listOf(
            Intent(Settings.ACTION_HOME_SETTINGS),
            Intent(Settings.ACTION_MANAGE_DEFAULT_APPS_SETTINGS),
            Intent(Settings.ACTION_SETTINGS)
        )

        for (i in intents) {
            try {
                startActivity(i)
                return
            } catch (_: Exception) {
            }
        }
    }
}
