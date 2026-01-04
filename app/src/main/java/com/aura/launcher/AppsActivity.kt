package com.aura.launcher

import android.content.Intent
import android.content.pm.ResolveInfo
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class AppsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_apps)

        val recycler = findViewById<RecyclerView>(R.id.recyclerApps)
        recycler.layoutManager = LinearLayoutManager(this)

        val apps = loadLaunchableApps()
        recycler.adapter = AppsAdapter(apps) { app ->
            launchApp(app.packageName)
        }
    }

    private fun loadLaunchableApps(): List<AppInfo> {
        val intent = Intent(Intent.ACTION_MAIN, null).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }

        val resolved: List<ResolveInfo> =
            packageManager.queryIntentActivities(intent, 0)

        return resolved
            .map { ri ->
                val label = ri.loadLabel(packageManager)?.toString() ?: ri.activityInfo.packageName
                val pkg = ri.activityInfo.packageName
                val icon = ri.loadIcon(packageManager)

                AppInfo(
                    label = label,
                    packageName = pkg,
                    icon = icon
                )
            }
            .sortedBy { it.label.lowercase() }
    }

    private fun launchApp(packageName: String) {
        val launchIntent = packageManager.getLaunchIntentForPackage(packageName)
        if (launchIntent != null) {
            startActivity(launchIntent)
        } else {
            Toast.makeText(this, "Ei saa avada: $packageName", Toast.LENGTH_SHORT).show()
        }
    }
}
