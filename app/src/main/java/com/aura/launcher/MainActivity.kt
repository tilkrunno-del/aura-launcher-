package com.aura.launcher

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.KeyEvent
import android.view.MotionEvent
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

        // 1) Ava rakendused nupp
        btnOpenApps.setOnClickListener {
            openApps(searchInput.text?.toString().orEmpty())
        }

        // 2) Määra vaikimisi avakuvarakenduseks (avan Seaded -> Home app)
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
            } else false
        }

        // Luubi (drawableEnd) vajutus EditText sees
        searchInput.setOnTouchListener { v, event ->
            if (event.action == MotionEvent.ACTION_UP) {
                val editText = v as EditText
                val drawableEnd = editText.compoundDrawablesRelative[2] // END drawable

                if (drawableEnd != null) {
                    val drawableWidth = drawableEnd.bounds.width()
                    val touchX = event.x

                    if (touchX >= (editText.width - editText.paddingEnd - drawableWidth)) {
                        openApps(editText.text.toString())
                        return@setOnTouchListener true
                    }
                }
            }
            false
        }

        // Kui MainActivity käivitatakse query-ga
        intent.getStringExtra(AppsActivity.EXTRA_QUERY)?.let { query ->
            if (query.isNotBlank()) {
                openApps(query)
            }
        }
    }

    private fun openApps(query: String) {
        val i = Intent(this, AppsActivity::class.java)
        i.putExtra(AppsActivity.EXTRA_QUERY, query.trim())
        startActivity(i)
    }

    private fun openHomeSettings() {
        // Proovime otse "Home app" seadet (töötab paljudel)
        try {
            startActivity(Intent(Settings.ACTION_HOME_SETTINGS))
            return
        } catch (_: Exception) {}

        // Fallback: Default apps üldvaade
        try {
            startActivity(Intent(Settings.ACTION_MANAGE_DEFAULT_APPS_SETTINGS))
            return
        } catch (_: Exception) {}

        // Viimane fallback: App info ekraan (sealt saab ka "Set as default" jm)
        try {
            startActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = android.net.Uri.parse("package:$packageName")
            })
        } catch (_: Exception) {}
    }
}
