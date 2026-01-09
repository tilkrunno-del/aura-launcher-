package com.aura.launcher

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.aura.launcher.adapter.AppsAdapter

class AppsActivity : AppCompatActivity() {

    private lateinit var appsRecyclerView: RecyclerView
    private lateinit var appsAdapter: AppsAdapter

    private val allApps = mutableListOf<AppInfo>()
    private val filteredApps = mutableListOf<AppInfo>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_apps)

        appsRecyclerView = findViewById(R.id.appsRecyclerView)

        appsAdapter = AppsAdapter(filteredApps) { app ->
            launchApp(app.packageName)
        }

        appsRecyclerView.layoutManager = GridLayoutManager(this, 4)
        appsRecyclerView.adapter = appsAdapter

        loadApps()
        setupSearch()
    }

    private fun loadApps() {
        val pm: PackageManager = packageManager
        val intent = Intent(Intent.ACTION_MAIN, null).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }

        val apps = pm.queryIntentActivities(intent, 0)

        allApps.clear()
        for (resolveInfo in apps) {
            val app = AppInfo(
                name = resolveInfo.loadLabel(pm).toString(),
                packageName = resolveInfo.activityInfo.packageName,
                icon = resolveInfo.loadIcon(pm)
            )
            allApps.add(app)
        }

        allApps.sortBy { it.name.lowercase() }

        filteredApps.clear()
        filteredApps.addAll(allApps)
        appsAdapter.notifyDataSetChanged()
    }

    private fun setupSearch() {
        val search = findViewById<android.widget.EditText>(R.id.searchEditText)
        val clear = findViewById<View>(R.id.clearSearch)

        clear.setOnClickListener {
            search.setText("")
        }

        search.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {}

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filterApps(s.toString())
            }
        })
    }

    private fun filterApps(query: String) {
        filteredApps.clear()

        if (query.isBlank()) {
            filteredApps.addAll(allApps)
        } else {
            filteredApps.addAll(
                allApps.filter {
                    it.name.contains(query, ignoreCase = true)
                }
            )
        }

        appsAdapter.notifyDataSetChanged()
    }

    private fun launchApp(packageName: String) {
        val intent = packageManager.getLaunchIntentForPackage(packageName)
        if (intent != null) {
            startActivity(intent)
        }
    }
}
