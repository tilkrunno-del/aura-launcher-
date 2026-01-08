package com.aura.launcher

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView

class HiddenAppsActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var searchEditText: EditText
    private lateinit var adapter: AppsAdapter

    private val hiddenApps = mutableListOf<AppInfo>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_apps)

        recyclerView = findViewById(R.id.appsRecyclerView)
        searchEditText = findViewById(R.id.searchEditText)

        // sama UI, aga ainult hidden list
        recyclerView.layoutManager = GridLayoutManager(this, 3)

        loadHidden()

        adapter = AppsAdapter(
            apps = hiddenApps,
            onClick = { app -> launchApp(app) },
            onLongPress = { view, app ->
                // ainult “Too tagasi” sisuliselt
                val key = makeKey(app)
                AppPrefs.toggleHidden(this, key) // remove hidden
                loadHidden()
                adapter.submitList(hiddenApps.toList())
            },
            isFavorite = { false }, // siin pole vaja
            isHidden = { true }
        )

        recyclerView.adapter = adapter

        searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                adapter.filterApps(s?.toString().orEmpty())
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun loadHidden() {
        hiddenApps.clear()
        val all = loadInstalledApps(packageManager)
        val hidden = AppPrefs.getHidden(this)
        hiddenApps.addAll(all.filter { hidden.contains(makeKey(it)) }.sortedBy { it.label.lowercase() })
    }

    private fun loadInstalledApps(pm: PackageManager): List<AppInfo> {
        val intent = Intent(Intent.ACTION_MAIN, null).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }
        val resolved = pm.queryIntentActivities(intent, 0)
        return resolved.map { ri ->
            AppInfo(
                label = ri.loadLabel(pm).toString(),
                packageName = ri.activityInfo.packageName,
                className = ri.activityInfo.name,
                icon = ri.loadIcon(pm)
            )
        }
    }

    private fun launchApp(app: AppInfo) {
        val launchIntent = Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
            setClassName(app.packageName, app.className)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        try { startActivity(launchIntent) } catch (_: Exception) {}
    }

    private fun makeKey(app: AppInfo) = "${app.packageName}/${app.className}"
}
