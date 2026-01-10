package com.aura.launcher

import android.content.Intent
import android.content.pm.ResolveInfo
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView

class HiddenAppsActivity : AppCompatActivity(), AppActionsBottomSheet.Callback {

    private lateinit var recycler: RecyclerView
    private lateinit var btnDone: Button
    private lateinit var adapter: AppsAdapter

    private var hiddenApps: List<AppInfo> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        ThemePrefs.applySavedTheme(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_hidden_apps)

        recycler = findViewById(R.id.hiddenAppsRecyclerView)
        btnDone = findViewById(R.id.btnDone)

        recycler.layoutManager = GridLayoutManager(this, 4)

        hiddenApps = loadHiddenApps()

        adapter = AppsAdapter(
            apps = hiddenApps,
            onClick = { app -> LauncherUtils.launchApp(this, app.packageName, app.className) },
            onLongPress = { _, app ->
                AppActionsBottomSheet.newInstance(app).show(supportFragmentManager, "app_actions")
            },
            isFavorite = { AppStateStore.isFavorite(this, it.packageName) },
            isHidden = { true }
        )

        recycler.adapter = adapter

        btnDone.setOnClickListener { finish() }
    }

    private fun loadHiddenApps(): List<AppInfo> {
        val pm = packageManager
        val intent = Intent(Intent.ACTION_MAIN, null).addCategory(Intent.CATEGORY_LAUNCHER)
        val resolved: List<ResolveInfo> = pm.queryIntentActivities(intent, 0)

        val hiddenSet = AppStateStore.getHidden(this)

        val list = resolved.mapNotNull { ri ->
            val ai = ri.activityInfo ?: return@mapNotNull null
            val pkg = ai.packageName
            val cls = ai.name
            if (!hiddenSet.contains(pkg)) return@mapNotNull null

            val label = ri.loadLabel(pm)?.toString() ?: pkg
            val icon = ri.loadIcon(pm)

            AppInfo(packageName = pkg, className = cls, label = label, icon = icon)
        }.sortedBy { it.label.lowercase() }

        return list
    }

    private fun refresh() {
        hiddenApps = loadHiddenApps()
        adapter.submitList(hiddenApps)
    }

    // BottomSheet callback
    override fun onOpen(app: AppInfo) = LauncherUtils.launchApp(this, app.packageName, app.className)

    override fun onAppInfo(app: AppInfo) {
        val i = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.parse("package:${app.packageName}")
        }
        startActivity(i)
    }

    override fun onToggleFavorite(app: AppInfo, makeFavorite: Boolean) {
        AppStateStore.setFavorite(this, app.packageName, makeFavorite)
        refresh()
    }

    override fun onToggleHidden(app: AppInfo, makeHidden: Boolean) {
        // hidden listis "peidetud" tähendab eemaldada peidetust
        AppStateStore.setHidden(this, app.packageName, makeHidden)
        refresh()
    }

    override fun onUninstall(app: AppInfo) {
        val i = Intent(Intent.ACTION_DELETE).apply {
            data = Uri.parse("package:${app.packageName}")
        }
        startActivity(i)
    }
}
