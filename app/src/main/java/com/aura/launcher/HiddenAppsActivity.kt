package com.aura.launcher

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class HiddenAppsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        finish() // ajutiselt välja
    }
}
