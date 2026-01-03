package com.aura.launcher

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.widget.TextView

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val tv = TextView(this)
        tv.text = "AURA on aktiivne launcher"
        tv.textSize = 22f
        tv.textAlignment = TextView.TEXT_ALIGNMENT_CENTER

        setContentView(tv)
    }
}
