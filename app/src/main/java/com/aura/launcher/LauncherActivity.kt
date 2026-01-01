package com.aura.launcher

import android.app.Activity
import android.os.Bundle
import android.view.Gravity
import android.widget.TextView

class LauncherActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val textView = TextView(this).apply {
            text = "AURA Launcher is running"
            textSize = 24f
            gravity = Gravity.CENTER
        }

        setContentView(textView)
    }
}
