package com.aura.launcher

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val btn = findViewById<Button>(R.id.btnOpenApps)
        btn.setOnClickListener {
            startActivity(Intent(this, AppsActivity::class.java))
        }
    }

    // Kuna launchMode="singleTask", siis HOME vajutus võib tulla siia ilma uut activity't tegemata
    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        if (intent != null) {
            setIntent(intent)
        }
    }

    // BACK ei tohi launcherit “kinni panna” – liigume lihtsalt taustale
    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        moveTaskToBack(true)
    }
}
