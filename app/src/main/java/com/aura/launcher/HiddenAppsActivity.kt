package com.aura.launcher

import android.os.Bundle
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView

class HiddenAppsActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: AppsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_hidden_apps)

        // Back nupp (kui sul layoutis olemas)
        findViewById<ImageButton?>(R.id.btnBack)?.setOnClickListener { finish() }

        recyclerView = findViewById(R.id.hiddenAppsRecyclerView)
        recyclerView.layoutManager = GridLayoutManager(this, 4)

        adapter = AppsAdapter(
            apps = emptyList(),
            onClick = { app ->
                // Hidden listist appi avamine (sama loogika mis mujal)
                LauncherUtils.launchApp(this, app.packageName, app.className)
            },
            onLongPress = { _, _ ->
                // Hiljem: "Unhide" või actions bottomsheet
            },
            isFavorite = { false },
            isHidden = { true }
        )

        recyclerView.adapter = adapter

        // ✅ Ajutine: hidden apps pole veel implementeeritud => tühi list
        // Hiljem asendame reaalse allikaga (SharedPreferences/DB).
        adapter.submitList(emptyList())
    }
}
