package com.aura.launcher

import android.content.Intent
import android.os.Bundle
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val searchInput = findViewById<EditText>(R.id.searchInput)

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

        // Luubi (drawableEnd) vajutus EditText sees
        searchInput.setOnTouchListener { v, event ->
            if (event.action == MotionEvent.ACTION_UP) {
                val editText = v as EditText
                val drawableEnd = editText.compoundDrawablesRelative[2] // END drawable

                if (drawableEnd != null) {
                    val drawableWidth = drawableEnd.bounds.width()
                    val touchX = event.x

                    // Kui vajutus on paremal ikooni alas
                    if (touchX >= (editText.width - editText.paddingEnd - drawableWidth)) {
                        openApps(editText.text.toString())
                        return@setOnTouchListener true
                    }
                }
            }
            false
        }

        // Kui MainActivity käivitatakse query-ga (nt hilisem laiendus)
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
