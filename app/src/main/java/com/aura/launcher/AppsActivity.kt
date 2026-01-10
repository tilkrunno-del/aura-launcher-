package com.aura.launcher

import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView

class AppsActivity : AppCompatActivity() {

    private lateinit var searchEditText: EditText
    private lateinit var btnClear: Button
    private lateinit var appsRecyclerView: RecyclerView

    private lateinit var adapter: AppsAdapter
    private val allApps = mutableListOf<AppInfo>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        LauncherUtils.applyThemeMode(this)
        setContentView(R.layout.activity_apps)

        searchEditText = findViewById(R.id.searchEditText)
        btnClear = findViewById(R.id.btnClearSearch)
        appsRecyclerView = findViewById(R.id.appsRecyclerView)

        appsRecyclerView.layoutManager = GridLayoutManager(this, 4)

        adapter = AppsAdapter(
            apps = emptyList(),
            onClick = { openApp(it) },
            onLongPress = { anchor, app -> showActions(anchor, app) },
            isFavorite = { LauncherUtils.isFavorite(this, it.packageName) },
            isHidden = { LauncherUtils.isHidden(this, it.packageName) }
        )
        appsRecyclerView.adapter = adapter

        loadApps()

        btnClear.setOnClickListener {
            searchEditText.setText("")
            adapter.filterApps("")
        }

        searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                adapter.filterApps(s?.toString().orEmpty())
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun loadApps() {
        allApps.clear()

        val pm = packageManager
        val intent = Intent(Intent.ACTION_MAIN, null).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }

        val hidden = LauncherUtils.getHiddenApps(this)

        val resolveInfos = pm.queryIntentActivities(intent, PackageManager.MATCH_ALL)
        val apps = resolveInfos.map { ri ->
            val label = ri.loadLabel(pm)?.toString() ?: ri.activityInfo.name
            val icon = ri.loadIcon(pm)
            AppInfo(
                packageName = ri.activityInfo.packageName,
                className = ri.activityInfo.name,
                label = label,
                icon = icon
            )
        }
            .filter { !hidden.contains(it.packageName) } // peidetud ei kuvata siia
            .sortedBy { it.label.lowercase() }

        allApps.addAll(apps)
        adapter.submitList(allApps)
    }

    private fun openApp(app: AppInfo) {
        val launchIntent = Intent().apply {
            setClassName(app.packageName, app.className)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        runCatching { startActivity(launchIntent) }
    }

    private fun showActions(anchor: View, app: AppInfo) {
        AppActionsBottomSheet.newInstance(
            packageName = app.packageName,
            label = app.label,
            className = app.className
        ).apply {
            onChanged = {
                // refresh list (favorite/hide/uninstall impacts UI)
                loadApps()
            }
        }.show(supportFragmentManager, "AppActionsBottomSheet")
    }

    companion object {
        fun openAppInfo(activity: AppCompatActivity, packageName: String) {
            val intent = Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.parse("package:$packageName")
            }
            activity.startActivity(intent)
        }
    }
}
