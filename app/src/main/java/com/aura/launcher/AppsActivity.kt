package com.aura.launcher

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class AppsActivity : AppCompatActivity() {

    data class AppItem(
        val label: String,
        val packageName: String
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_apps)

        val recycler = findViewById<RecyclerView>(R.id.recyclerApps)
        recycler.layoutManager = LinearLayoutManager(this)

        val apps = loadLaunchableApps()
        recycler.adapter = AppsAdapter(apps) { item ->
            launchApp(item.packageName)
        }
    }

    private fun loadLaunchableApps(): List<AppItem> {
        val pm = packageManager

        val intent = Intent(Intent.ACTION_MAIN, null).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }

        val resolved = pm.queryIntentActivities(intent, PackageManager.MATCH_ALL)

        return resolved
            .map { ri ->
                val label = ri.loadLabel(pm)?.toString() ?: ri.activityInfo.packageName
                AppItem(label = label, packageName = ri.activityInfo.packageName)
            }
            .sortedBy { it.label.lowercase() }
    }

    private fun launchApp(packageName: String) {
        val pm = packageManager
        val launchIntent = pm.getLaunchIntentForPackage(packageName)
        if (launchIntent != null) {
            startActivity(launchIntent)
        }
    }
}
