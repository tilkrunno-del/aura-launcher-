package com.aura.launcher

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Kui vajutatakse BACK, siis ei lähe süsteemi/vanasse launcherisse.
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                // Ära tee finish()!
                // Lihtsalt "minimeeri" (nagu HOME käitumine), et ei viskaks välja.
                moveTaskToBack(true)
            }
        })

        val btn = findViewById<Button>(R.id.btnOpenApps)
        btn.setOnClickListener {
            startActivity(Intent(this, AppsActivity::class.java))
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        // Kui HOME/launcher kutsutakse uuesti ette, siis jääme MainActivity peale.
        setIntent(intent)
    }
}
