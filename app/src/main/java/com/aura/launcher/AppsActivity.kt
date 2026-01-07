package com.aura.launcher

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView

class AppsActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_QUERY = "EXTRA_QUERY"
    }

    private lateinit var recyclerView: RecyclerView
    private lateinit var searchEditText: EditText
    private lateinit var adapter: AppsAdapter

    private val allApps = mutableListOf<AppInfo>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_apps)

        recyclerView = findViewById(R.id.appsRecyclerView)
        searchEditText = findViewById(R.id.searchEditText)

        // GRID: 4 veergu (muuda 3-ks, kui tahad suuremaid ikoone)
        recyclerView.layoutManager = GridLayoutManager(this, 4)

        allApps.clear()
        allApps.addAll(loadInstalledApps(packageManager))

        adapter = AppsAdapter(allApps) { app ->
            launchApp(app)
        }
        recyclerView.adapter = adapter

        // Otsing
        searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun afterTextChanged(s: Editable?) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                adapter.filterApps(s?.toString().orEmpty())
            }
        })

        // Kui tuli query MainActivity-st
        val initialQuery = intent.getStringExtra(EXTRA_QUERY)
        if (!initialQuery.isNullOrBlank()) {
            searchEditText.setText(initialQuery)
            searchEditText.setSelection(initialQuery.length)
            adapter.filterApps(initialQuery)
        }
    }

    private fun launchApp(app: AppInfo) {
        val pm = packageManager

        // 1) Eelistatud viis
        val primary = pm.getLaunchIntentForPackage(app.packageName)?.apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        // 2) Fallback: konkreetne komponent
        val fallback = Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
            setClassName(app.packageName, app.className)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        try {
            startActivity(primary ?: fallback)
        } catch (e: Exception) {
            Toast.makeText(this, "Ei saa avada: ${app.label}", Toast.LENGTH_SHORT).show()
        }
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
        }.sortedBy { it.label.lowercase() }
    }
}
