package com.aura.launcher

import android.content.Intent
import android.os.Bundle
import android.widget.EditText
import android.widget.PopupMenu
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doOnTextChanged
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
                showAppMenu(
                    anchor = view,
                    app = app
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

        // FIX: TextWatcher mismatch / "it" unresolved
        searchInput.doOnTextChanged { text, _, _, _ ->
            adapter.filterApps(text?.toString().orEmpty())
        }
    }

    private fun showAppMenu(anchor: android.view.View, app: AppInfo) {
        val popup = PopupMenu(this, anchor)

        val isFav = favoriteApps.contains(app.packageName)
        val isHid = hiddenApps.contains(app.packageName)

        // Lisa menüü elemendid programmiliselt (ei vaja menu XML-i)
        popup.menu.add(0, 1, 0, if (isFav) "Eemalda lemmik" else "Lisa lemmik")
        popup.menu.add(0, 2, 1, if (isHid) "Too tagasi" else "Peida")
        popup.menu.add(0, 3, 2, "App info")

        popup.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                1 -> {
                    toggleFavorite(app.packageName)
                    true
                }
                2 -> {
                    toggleHidden(app.packageName)
                    true
                }
                3 -> {
                    openAppInfo(app.packageName)
                    true
                }
                else -> false
            }
        }

        popup.show()
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
        if (intent != null) startActivity(intent)
    }

    private fun openAppInfo(packageName: String) {
        val intent = Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = android.net.Uri.parse("package:$packageName")
        }
        startActivity(intent)
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
