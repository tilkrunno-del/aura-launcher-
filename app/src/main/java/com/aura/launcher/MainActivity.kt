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

        // Klaviatuuri Search / Done / Enter -> avab AppsActivity koos query-ga
        searchInput.setOnEditorActionListener { _, actionId, event ->
            val imeAction =
                actionId == EditorInfo.IME_ACTION_SEARCH ||
                actionId == EditorInfo.IME_ACTION_DONE

            val enterKey =
                event?.keyCode == KeyEvent.KEYCODE_ENTER &&
                event.action == KeyEvent.ACTION_DOWN

            if (imeAction || enterKey) {
                openApps(searchInput.text?.toString().orEmpty())
                true
            } else {
                false
            }
        }

        // Kui MainActivity käivitatakse query-ga (valikuline laiendus)
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
}
