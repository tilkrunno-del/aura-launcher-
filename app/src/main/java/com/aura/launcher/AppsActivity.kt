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
    val packageName: String,
    val className: String?
  )

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_apps)

    val recycler = findViewById<RecyclerView>(R.id.recyclerApps)
    recycler.layoutManager = LinearLayoutManager(this)

    val apps = loadApps()
    recycler.adapter = AppsAdapter(apps) { item ->
      launchApp(item)
    }
  }

  private fun loadApps(): List<AppItem> {
    val pm = packageManager
    val intent = Intent(Intent.ACTION_MAIN, null).apply {
      addCategory(Intent.CATEGORY_LAUNCHER)
    }

    val results = pm.queryIntentActivities(intent, PackageManager.MATCH_ALL)

    return results
      .map { ri ->
        AppItem(
          label = ri.loadLabel(pm)?.toString() ?: ri.activityInfo.packageName,
          packageName = ri.activityInfo.packageName,
          className = ri.activityInfo.name
        )
      }
      .sortedBy { it.label.lowercase() }
  }

  private fun launchApp(item: AppItem) {
    val intent = Intent(Intent.ACTION_MAIN).apply {
      addCategory(Intent.CATEGORY_LAUNCHER)
      setClassName(item.packageName, item.className ?: return)
      addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    startActivity(intent)
  }
}
