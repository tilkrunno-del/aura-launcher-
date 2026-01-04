package com.aura.launcher

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val searchInput = findViewById<EditText>(R.id.searchInput)

        // Ava rakendused nupp
        findViewById<Button>(R.id.btnOpenApps).setOnClickListener {
            openApps(searchInput.text?.toString())
        }

        // Määra vaikimisi launcher
        findViewById<Button>(R.id.btnSetDefaultLauncher).setOnClickListener {
            startActivity(Intent(Settings.ACTION_HOME_SETTINGS))
        }

        // Kui kasutaja vajutab Enter/Search klaviatuuril -> ava AppsActivity kohe
        searchInput.setOnEditorActionListener { _, _, _ ->
            openApps(searchInput.text?.toString())
            true
        }
    }

    private fun openApps(query: String?) {
        val intent = Intent(this, AppsActivity::class.java)
        val q = query?.trim().orEmpty()
        if (q.isNotEmpty()) {
            intent.putExtra(AppsActivity.EXTRA_QUERY, q)
        }
        startActivity(intent)
    }
}
