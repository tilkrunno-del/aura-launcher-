package com.aura.launcher

import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.util.Locale

class AppsActivity : AppCompatActivity(), AppActionsBottomSheet.Callbacks {

    private lateinit var recyclerView: RecyclerView
    private lateinit var searchEditText: EditText
    private lateinit var clearButton: Button

    private lateinit var adapter: AppsAdapter
    private var allApps: List<AppInfo> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_apps)

        recyclerView = findViewById(R.id.appsRecyclerView)
        searchEditText = findViewById(R.id.searchEditText)
        clearButton = findViewById(R.id.btnClearSearch)

        recyclerView.layoutManager = GridLayoutManager(this, 4)

        adapter = AppsAdapter(
            apps = emptyList(),
            onClick = { app -> launchApp(app) },
            onLongPress = { _, app -> openBottomSheet(app) },
            isFavorite = { app -> AppPrefs.isFavorite(this, app.packageName) },
            isHidden = { app -> AppPrefs.isHidden(this, app.packageName) }
        )

        recyclerView.adapter = adapter

        reloadApps()

        searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val q = s?.toString().orEmpty()
                adapter.filterApps(q)
                clearButton.visibility = if (q.isBlank()) View.GONE else View.VISIBLE
            }
        })

        clearButton.setOnClickListener { searchEditText.setText("") }
    }

    private fun reloadApps() {
        allApps = loadInstalledApps()
        adapter.submitList(allApps)
        adapter.filterApps(searchEditText.text?.toString().orEmpty())
    }

    private fun openBottomSheet(app: AppInfo) {
        val sheet = AppActionsBottomSheet.newInstance(app)
        sheet.setCallbacks(this)
        sheet.show(supportFragmentManager, "AppActionsBottomSheet")
    }

    override fun onRequestRefresh() {
        // Kui favorite/hide muutub → värskenda listi + otsingut
        adapter.submitList(allApps)
        adapter.filterApps(searchEditText.text?.toString().orEmpty())
    }

    override fun onLaunch(app: AppInfo) {
        launchApp(app)
    }

    private fun launchApp(app: AppInfo) {
        val intent = packageManager.getLaunchIntentForPackage(app.packageName) ?: return
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
    }

    private fun loadInstalledApps(): List<AppInfo> {
        val pm = packageManager
        val mainIntent = Intent(Intent.ACTION_MAIN, null).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }

        val resolved = pm.queryIntentActivities(mainIntent, 0)
        val list = ArrayList<AppInfo>(resolved.size)

        for (ri in resolved) {
            val pkg = ri.activityInfo.packageName ?: continue
            val cls = ri.activityInfo.name ?: ""
            val label = ri.loadLabel(pm)?.toString() ?: pkg
            val icon = tryLoadIcon(ri, pm)

            list.add(
                AppInfo(
                    packageName = pkg,
                    className = cls,
                    label = label,
                    icon = icon
                )
            )
        }

        // Sort A-Z
        return list.sortedBy { it.label.lowercase(Locale.getDefault()) }
    }

    private fun tryLoadIcon(ri: android.content.pm.ResolveInfo, pm: PackageManager): Drawable? {
        return try {
            ri.loadIcon(pm)
        } catch (_: Throwable) {
            getDrawable(android.R.drawable.sym_def_app_icon)
        }
    }
}
