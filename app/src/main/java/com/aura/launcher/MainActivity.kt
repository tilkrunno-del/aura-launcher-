package com.aura.launcher

import android.content.Intent
import android.os.Bundle
import android.view.KeyEvent
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val searchInput = findViewById<EditText>(R.id.searchInput)

        // ✅ Need ID-d peavad olemas olema activity_main.xml-is
        // Ava rakendused nupp
        findViewById<android.view.View>(R.id.btnOpenApps).setOnClickListener {
            openApps(searchInput.text.toString())
        }

        // Luubi ikoon (otsing)
        findViewById<ImageView>(R.id.btnSearch).setOnClickListener {
            openApps(searchInput.text.toString())
        }

        // Määra vaikimisi avakuva (viib seadete ekraanile)
        findViewById<android.view.View>(R.id.btnSetHome).setOnClickListener {
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
        // Huawei/EMUI puhul töötavad eri seadete lehed eri moodi – proovime mitu varianti
        val intents = listOf(
            Intent(android.provider.Settings.ACTION_HOME_SETTINGS),
            Intent(android.provider.Settings.ACTION_MANAGE_DEFAULT_APPS_SETTINGS),
            Intent(android.provider.Settings.ACTION_SETTINGS)
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
