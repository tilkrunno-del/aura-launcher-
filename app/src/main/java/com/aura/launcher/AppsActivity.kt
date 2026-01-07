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

    private val allApps = mutableListOf<AppInfo>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_apps)

        recyclerView = findViewById(R.id.appsRecyclerView)
        searchEditText = findViewById(R.id.searchEditText)

        recyclerView.layoutManager = LinearLayoutManager(this)

        allApps.clear()
        allApps.addAll(loadInstalledApps(packageManager))

        adapter = AppsAdapter(allApps) { app ->
            // Avame kindlalt õige Activity (package + className)
            val i = Intent().apply {
                setClassName(app.packageName, app.className)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            startActivity(i)
        }

        recyclerView.adapter = adapter

        searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                adapter.filterApps(s?.toString().orEmpty())
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        val initialQuery = intent.getStringExtra(EXTRA_QUERY)
        if (!initialQuery.isNullOrBlank()) {
            searchEditText.setText(initialQuery)
            searchEditText.setSelection(initialQuery.length)
            adapter.filterApps(initialQuery)
        }
    }

    private fun loadInstalledApps(pm: PackageManager): List<AppInfo> {
        val intent = Intent(Intent.ACTION_MAIN, null).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }

        val resolved = pm.queryIntentActivities(intent, 0)

        return resolved.map {
            AppInfo(
                label = it.loadLabel(pm).toString(),
                packageName = it.activityInfo.packageName,
                className = it.activityInfo.name,          // ✅ SEE PARANDAB "className" errori
                icon = it.loadIcon(pm)
            )
        }.sortedBy { it.label.lowercase() }
    }
}
