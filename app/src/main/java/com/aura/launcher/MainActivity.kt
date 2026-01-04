package com.aura.launcher

import android.content.Intent
import android.os.Bundle
import android.view.Gravity
import android.widget.Button
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
        }

        val title = Button(this).apply {
            text = "AURA Launcher"
            isAllCaps = false
            textSize = 20f
            isEnabled = false
        }

        val openApps = Button(this).apply {
            text = "Ava rakendused"
            setOnClickListener {
                startActivity(Intent(this@MainActivity, AppsActivity::class.java))
            }
        }

        root.addView(title)
        root.addView(openApps)

        setContentView(root)
    }
}
