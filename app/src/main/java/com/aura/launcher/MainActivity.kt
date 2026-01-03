package com.aura.launcher

import android.os.Bundle
import android.view.Gravity
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val tv = TextView(this).apply {
            text = "AURA Launcher"
            gravity = Gravity.CENTER
            textSize = 22f
        }

        setContentView(tv)
    }
}
