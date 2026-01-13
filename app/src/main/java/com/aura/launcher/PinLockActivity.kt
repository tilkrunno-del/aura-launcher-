package com.aura.launcher

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.security.MessageDigest

class PinLockActivity : AppCompatActivity() {

    private lateinit var prefs: SharedPreferences

    private lateinit var titleText: TextView
    private lateinit var pinEdit: EditText
    private lateinit var btnOk: Button
    private lateinit var btnCancel: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pin_lock)

        prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

        titleText = findViewById(R.id.titleText)
        pinEdit = findViewById(R.id.pinEdit)
        btnOk = findViewById(R.id.btnOk)
        btnCancel = findViewById(R.id.btnCancel)

        val existingHash = prefs.getString(KEY_PIN_HASH, null)
        val isFirstTime = existingHash.isNullOrBlank()

        titleText.text =
            if (isFirstTime) getString(R.string.set_pin_title)
            else getString(R.string.enter_pin_title)

        btnOk.setOnClickListener {
            val pin = pinEdit.text?.toString()?.trim().orEmpty()

            if (pin.length < 4) {
                Toast.makeText(this, getString(R.string.pin_too_short), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (isFirstTime) {
                // Salvesta PIN (hash)
                prefs.edit().putString(KEY_PIN_HASH, sha256(pin)).apply()
                Toast.makeText(this, getString(R.string.pin_set_ok), Toast.LENGTH_SHORT).show()
                openHiddenApps()
            } else {
                // Kontrolli PIN
                val ok = sha256(pin) == existingHash
                if (ok) {
                    openHiddenApps()
                } else {
                    Toast.makeText(this, getString(R.string.pin_wrong), Toast.LENGTH_SHORT).show()
                    pinEdit.setText("")
                }
            }
        }

        btnCancel.setOnClickListener {
            finish()
        }
    }

    private fun openHiddenApps() {
        startActivity(Intent(this, HiddenAppsActivity::class.java))
        finish()
    }

    private fun sha256(input: String): String {
        val md = MessageDigest.getInstance("SHA-256")
        val bytes = md.digest(input.toByteArray(Charsets.UTF_8))
        return bytes.joinToString("") { "%02x".format(it) }
    }

    companion object {
        private const val PREFS_NAME = "aura_launcher_prefs"
        private const val KEY_PIN_HASH = "hidden_pin_hash"
    }
}
