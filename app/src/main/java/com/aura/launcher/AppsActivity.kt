package com.aura.launcher

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import android.widget.Toast
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
            launchApp(app)
        }

        recyclerView.adapter = adapter

        // algne otsing MainActivity-st
        val query = intent.getStringExtra(EXTRA_QUERY).orEmpty()
        if (query.isNotBlank()) {
            searchEditText.setText(query)
            searchEditText.setSelection(query.length)
            adapter.filterApps(query)
        }

        // ⚠️ ÕIGE TextWatcher (MITTE lambda)
        searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun afterTextChanged(s: Editable?) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                adapter.filterApps(s?.toString().orEmpty())
            }
        })
    }

    private fun launchApp(app: AppInfo) {
        try {
            val intent = packageManager.getLaunchIntentForPackage(app.packageName)

            if (intent == null) {
                Toast.makeText(this, "Ei saa avada: ${app.label}", Toast.LENGTH_SHORT).show()
                return
            }

            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)

        } catch (e: Exception) {
            Toast.makeText(this, "Viga: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun loadInstalledApps(pm: PackageManager): List<AppInfo> {
        val intent = Intent(Intent.ACTION_MAIN, null).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }

        val apps = pm.queryIntentActivities(intent, 0)

        return apps.map {
            AppInfo(
                label = it.loadLabel(pm).toString(),
                packageName = it.activityInfo.packageName,
                icon = it.loadIcon(pm)
            )
        }.sortedBy { it.label.lowercase() }
    }
}
