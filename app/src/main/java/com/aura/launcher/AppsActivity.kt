package com.aura.launcher

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView

class AppsActivity : AppCompatActivity() {

    private lateinit var adapter: AppsAdapter
    private val hiddenApps = mutableSetOf<String>()
    private val favoriteApps = mutableSetOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_apps)

        val searchInput: EditText = findViewById(R.id.searchEditText)
        val recyclerView: RecyclerView = findViewById(R.id.appsRecyclerView)

        recyclerView.layoutManager = GridLayoutManager(this, 3)

        val apps = loadApps()

        adapter = AppsAdapter(
            apps = apps,
            onClick = { app ->
                launchApp(app.packageName)
            },
            onLongPress = { view, app ->
                AppContextMenu.show(
                    context = this,
                    anchor = view,
                    app = app,
                    isFavorite = favoriteApps.contains(app.packageName),
                    isHidden = hiddenApps.contains(app.packageName),
                    onToggleFavorite = {
                        toggleFavorite(app.packageName)
                    },
                    onToggleHidden = {
                        toggleHidden(app.packageName)
                    }
                )
            },
            isFavorite = { app ->
                favoriteApps.contains(app.packageName)
            },
            isHidden = { app ->
                hiddenApps.contains(app.packageName)
            }
        )

        recyclerView.adapter = adapter

        searchInput.addTextChangedListener {
            adapter.filterApps(it.toString())
        }
    }

    private fun loadApps(): List<AppInfo> {
        val pm = packageManager
        val intent = Intent(Intent.ACTION_MAIN, null).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }

        return pm.queryIntentActivities(intent, 0)
            .map {
                AppInfo(
                    label = it.loadLabel(pm).toString(),
                    packageName = it.activityInfo.packageName,
                    icon = it.loadIcon(pm)
                )
            }
            .sortedBy { it.label.lowercase() }
    }

    private fun launchApp(packageName: String) {
        val intent = packageManager.getLaunchIntentForPackage(packageName)
        if (intent != null) {
            startActivity(intent)
        }
    }

    private fun toggleFavorite(packageName: String) {
        if (!favoriteApps.add(packageName)) {
            favoriteApps.remove(packageName)
        }
        adapter.notifyDataSetChanged()
    }

    private fun toggleHidden(packageName: String) {
        if (!hiddenApps.add(packageName)) {
            hiddenApps.remove(packageName)
        }
        adapter.notifyDataSetChanged()
    }
}
