package com.aura.launcher

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class AppsActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_QUERY = "EXTRA_QUERY"
    }

    private lateinit var recyclerView: RecyclerView
    private lateinit var searchEditText: EditText

    private lateinit var adapter: AppsAdapter
    private val allApps: MutableList<AppInfo> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_apps)

        recyclerView = findViewById(R.id.appsRecyclerView)
        searchEditText = findViewById(R.id.searchEditText)

        recyclerView.layoutManager = LinearLayoutManager(this)

        allApps.clear()
        allApps.addAll(loadInstalledApps(packageManager))

        adapter = AppsAdapter(allApps) { app ->
            val launchIntent = packageManager.getLaunchIntentForPackage(app.packageName)
            if (launchIntent != null) startActivity(launchIntent)
        }
        recyclerView.adapter = adapter

        val initialQuery = intent.getStringExtra(EXTRA_QUERY).orEmpty()
        if (initialQuery.isNotBlank()) {
            searchEditText.setText(initialQuery)
            searchEditText.setSelection(initialQuery.length)
            adapter.filterApps(initialQuery)
        }

        searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun afterTextChanged(s: Editable?) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                adapter.filterApps(s?.toString().orEmpty())
            }
        })
    }

    private fun loadInstalledApps(pm: PackageManager): List<AppInfo> {
        val intent = Intent(Intent.ACTION_MAIN, null).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }

        val resolved = pm.queryIntentActivities(intent, 0)

        return resolved.map { ri ->
            val label = ri.loadLabel(pm)?.toString() ?: ri.activityInfo.packageName
            val pkg = ri.activityInfo.packageName
            val icon = ri.loadIcon(pm)
            AppInfo(label = label, packageName = pkg, icon = icon)
        }.sortedBy { it.label.lowercase() }
    }
}
