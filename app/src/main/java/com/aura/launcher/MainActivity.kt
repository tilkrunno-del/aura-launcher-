package com.aura.launcher

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Ava kohe rakenduste nimekiri
        startActivity(Intent(this, AppsActivity::class.java))
        finish()
    }
}
