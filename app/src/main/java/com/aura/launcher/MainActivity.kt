package com.aura.launcher

import android.content.Intent
import android.os.Bundle
import android.view.KeyEvent
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val searchInput = findViewById<EditText>(R.id.searchInput)

        // ✅ Kui sul on ainult klaviatuuri otsing, siis vähemalt see töötab alati
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
            } else false
        }

        // ✅ Kui XML-is on olemas "Ava rakendused" nupp, siis pane talle listener
        // (kui pole, siis ei juhtu midagi ja build ei kuku)
        runCatching {
            findViewById<android.view.View>(R.id.btnOpenApps).setOnClickListener {
                openApps(searchInput.text.toString())
            }
        }

        // ✅ Kui XML-is on olemas "Määra AURA..." nupp, siis ava seaded
        runCatching {
            findViewById<android.view.View>(R.id.btnSetDefaultHome).setOnClickListener {
                openHomeSettings()
            }
        }
    }

    private fun openApps(query: String) {
        val intent = Intent(this, AppsActivity::class.java)
        intent.putExtra(AppsActivity.EXTRA_QUERY, query.trim())
        startActivity(intent)
    }

    private fun openHomeSettings() {
        val intents = listOf(
            Intent(android.provider.Settings.ACTION_HOME_SETTINGS),
            Intent(android.provider.Settings.ACTION_MANAGE_DEFAULT_APPS_SETTINGS),
            Intent(android.provider.Settings.ACTION_SETTINGS)
        )
        for (i in intents) {
            try {
                startActivity(i)
                return
            } catch (_: Exception) {}
        }
    }
}
