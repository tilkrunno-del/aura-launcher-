package com.aura.launcher

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.widget.EditText

class AppsActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_QUERY = "extra_query"
    }

    private lateinit var adapter: AppsAdapter
    private val allApps = mutableListOf<AppInfo>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_apps)

        val recyclerView = findViewById<RecyclerView>(R.id.appsRecyclerView)
        val searchInput = findViewById<EditText>(R.id.searchInput)

        recyclerView.layoutManager = LinearLayoutManager(this)

        loadApps()
        adapter = AppsAdapter(allApps) { app ->
            launchApp(app.packageName)
        }
        recyclerView.adapter = adapter

        // algne query MainActivity-st
        val initialQuery = intent.getStringExtra(EXTRA_QUERY).orEmpty()
        if (initialQuery.isNotBlank()) {
            adapter.filter(initialQuery)
            searchInput.setText(initialQuery)
            searchInput.setSelection(initialQuery.length)
        }

        // live otsing
        searchInput.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                adapter.filter(s.toString())
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }

    private fun loadApps() {
        val pm = packageManager
        val intent = Intent(Intent.ACTION_MAIN, null)
        intent.addCategory(Intent.CATEGORY_LAUNCHER)

        val apps = pm.queryIntentActivities(intent, 0)
        for (resolveInfo in apps) {
            val label = resolveInfo.loadLabel(pm).toString()
            val packageName = resolveInfo.activityInfo.packageName
            val icon = resolveInfo.loadIcon(pm)
            allApps.add(AppInfo(label, packageName, icon))
        }

        allApps.sortBy { it.label.lowercase() }
    }

    private fun launchApp(packageName: String) {
        val intent = packageManager.getLaunchIntentForPackage(packageName)
        intent?.let { startActivity(it) }
    }
}
