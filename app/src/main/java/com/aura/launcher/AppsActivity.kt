package com.aura.launcher

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.ImageButton
import android.widget.PopupMenu
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doOnTextChanged
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView

class AppsActivity : AppCompatActivity() {

    private lateinit var appsAdapter: AppsAdapter

    // Ajutine mälu (hiljem võib panna SharedPrefs / AppPrefs)
    private val favoriteApps = mutableSetOf<String>()
    private val hiddenApps = mutableSetOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_apps)

        val searchEditText: EditText = findViewById(R.id.searchEditText)
        val appsRecyclerView: RecyclerView = findViewById(R.id.appsRecyclerView)
        val btnMore: ImageButton = findViewById(R.id.btnMore)

        // Grid 3 veergu
        appsRecyclerView.layoutManager = GridLayoutManager(this, 3)

        val apps = loadApps()

        appsAdapter = AppsAdapter(
            apps = apps,
            onClick = { app ->
                launchApp(app.packageName)
            },
            onLongPress = { view, app ->
                showAppMenu(view, app)
            },
            isFavorite = { app ->
                favoriteApps.contains(app.packageName)
            },
            isHidden = { app ->
                hiddenApps.contains(app.packageName)
            }
        )

        appsRecyclerView.adapter = appsAdapter

        // 🔍 OTSING – töötab
        searchEditText.doOnTextChanged { text, _, _, _ ->
            appsAdapter.filterApps(text?.toString().orEmpty())
        }

        // ☰ Ülemine menüü (nt Settings / Hidden apps)
        btnMore.setOnClickListener {
            showTopMenu(it)
        }
    }

    // ------------------------------------
    // Andmete laadimine
    // ------------------------------------

    private fun loadApps(): List<AppInfo> {
        val pm = packageManager
        val intent = Intent(Intent.ACTION_MAIN, null).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }

        return pm.queryIntentActivities(intent, 0)
            .map {
                AppInfo(
                    packageName = it.activityInfo.packageName,
                    className = it.activityInfo.name,
                    label = it.loadLabel(pm).toString(),
                    icon = it.loadIcon(pm)
                )
            }
            .sortedBy { it.label.lowercase() }
    }

    // ------------------------------------
    // App actions
    // ------------------------------------

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

    // ------------------------------------
    // Menüü – long press appil
    // ------------------------------------

    private fun showAppMenu(anchor: View, app: AppInfo) {
        val popup = PopupMenu(this, anchor)

        val isFav = favoriteApps.contains(app.packageName)
        val isHidden = hiddenApps.contains(app.packageName)

        popup.menu.add(
            0, 1, 0,
            if (isFav) "Eemalda lemmikutest" else "Lisa lemmikutesse"
        )
        popup.menu.add(
            0, 2, 1,
            if (isHidden) "Too tagasi" else "Peida"
        )
        popup.menu.add(0, 3, 2, "Rakenduse info")

        popup.setOnMenuItemClickListener {
            when (it.itemId) {
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

    // ------------------------------------
    // Ülemine menüü (☰)
    // ------------------------------------

    private fun showTopMenu(anchor: View) {
        val popup = PopupMenu(this, anchor)

        popup.menu.add(0, 1, 0, "Seaded")
        popup.menu.add(0, 2, 1, "Peidetud rakendused")

        popup.setOnMenuItemClickListener {
            when (it.itemId) {
                1 -> {
                    startActivity(Intent(this, SettingsActivity::class.java))
                    true
                }
                2 -> {
                    startActivity(Intent(this, HiddenAppsActivity::class.java))
                    true
                }
                else -> false
            }
        }

        popup.show()
    }

    // ------------------------------------
    // State muutmine
    // ------------------------------------

    private fun toggleFavorite(packageName: String) {
        if (!favoriteApps.add(packageName)) {
            favoriteApps.remove(packageName)
        }
        appsAdapter.notifyDataSetChanged()
    }

    private fun toggleHidden(packageName: String) {
        if (!hiddenApps.add(packageName)) {
            hiddenApps.remove(packageName)
        }
        appsAdapter.notifyDataSetChanged()
    }
}
