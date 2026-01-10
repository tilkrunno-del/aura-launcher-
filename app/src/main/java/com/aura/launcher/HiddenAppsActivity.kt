package com.aura.launcher

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView

class HiddenAppsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_apps)

        val recycler = findViewById<RecyclerView>(R.id.appsRecyclerView)
        recycler.layoutManager = GridLayoutManager(this, 4)

        val hiddenApps = LauncherUtils.getHiddenApps(this)

        recycler.adapter = AppsAdapter(
            apps = hiddenApps,
            onClick = { app ->
                LauncherUtils.launchApp(this, app)
            },
            onLongPress = { _, _ -> }
        )
    }
}
