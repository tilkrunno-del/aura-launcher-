package com.aura.launcher

import android.content.Intent
import android.content.pm.ResolveInfo
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView

class AppsActivity : AppCompatActivity(), AppActionsBottomSheet.Callback {

    private lateinit var recycler: RecyclerView
    private lateinit var search: EditText
    private lateinit var clearBtn: Button

    private var allApps: List<AppInfo> = emptyList()
    private lateinit var adapter: AppsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        ThemePrefs.applySavedTheme(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_apps)

        recycler = findViewById(R.id.appsRecyclerView)
        search = findViewById(R.id.searchEditText)
        clearBtn = findViewById(R.id.btnClearSearch)

        recycler.layoutManager = GridLayoutManager(this, 4)

        allApps = loadLaunchableApps(includeHidden = false)

        adapter = AppsAdapter(
            apps = allApps,
            onClick = { app ->
                LauncherUtils.launchApp(this, app.packageName, app.className)
            },
            onLongPress = { _, app ->
                AppActionsBottomSheet.newInstance(app).show(supportFragmentManager, "app_actions")
            },
            isFavorite = { AppStateStore.isFavorite(this, it.packageName) },
            isHidden = { AppStateStore.isHidden(this, it.packageName) }
        )

        recycler.adapter = adapter

        search.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                adapter.filterApps(s?.toString().orEmpty())
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        clearBtn.setOnClickListener {
            search.setText("")
            adapter.filterApps("")
        }
    }

    private fun loadLaunchableApps(includeHidden: Boolean): List<AppInfo> {
        val pm = packageManager
        val intent = Intent(Intent.ACTION_MAIN, null).addCategory(Intent.CATEGORY_LAUNCHER)
        val resolved: List<ResolveInfo> = pm.queryIntentActivities(intent, 0)

        val hiddenSet = AppStateStore.getHidden(this)

        val list = resolved.mapNotNull { ri ->
            val ai = ri.activityInfo ?: return@mapNotNull null
            val pkg = ai.packageName
            val cls = ai.name
            if (!includeHidden && hiddenSet.contains(pkg)) return@mapNotNull null

            val label = ri.loadLabel(pm)?.toString() ?: pkg
            val icon = ri.loadIcon(pm)

            AppInfo(
                packageName = pkg,
                className = cls,
                label = label,
                icon = icon
            )
        }.sortedBy { it.label.lowercase() }

        return list
    }

    private fun refreshList() {
        allApps = loadLaunchableApps(includeHidden = false)
        adapter.submitList(allApps)
        adapter.filterApps(search.text?.toString().orEmpty())
    }

    // BottomSheet callback
    override fun onOpen(app: AppInfo) {
        LauncherUtils.launchApp(this, app.packageName, app.className)
    }

    override fun onAppInfo(app: AppInfo) {
        val i = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.parse("package:${app.packageName}")
        }
        startActivity(i)
    }

    override fun onToggleFavorite(app: AppInfo, makeFavorite: Boolean) {
        AppStateStore.setFavorite(this, app.packageName, makeFavorite)
        refreshList()
    }

    override fun onToggleHidden(app: AppInfo, makeHidden: Boolean) {
        AppStateStore.setHidden(this, app.packageName, makeHidden)
        refreshList()
    }

    override fun onUninstall(app: AppInfo) {
        val i = Intent(Intent.ACTION_DELETE).apply {
            data = Uri.parse("package:${app.packageName}")
        }
        startActivity(i)
    }
}
