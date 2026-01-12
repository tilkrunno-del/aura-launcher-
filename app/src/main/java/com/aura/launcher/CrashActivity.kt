package com.aura.launcher

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class CrashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_crash)

        val tv: TextView = findViewById(R.id.crashText)

        val text = try {
            openFileInput(AuraApp.CRASH_FILE).bufferedReader().use { it.readText() }
        } catch (e: Exception) {
            "Crash log faili ei leitud. Käivita uuesti, et crash salvestuks.\n\n" +
                    "Viga: ${e.message}"
        }

        tv.text = text.ifBlank { "Crash log on tühi." }
    }
}
