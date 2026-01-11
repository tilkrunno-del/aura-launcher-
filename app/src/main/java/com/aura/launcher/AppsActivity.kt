package com.aura.launcher

import android.content.Intent
import android.content.pm.ResolveInfo
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.EditText
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView

class AppsActivity : AppCompatActivity() {

    private lateinit var appsRecyclerView: RecyclerView
    private lateinit var searchEditText: EditText
    private lateinit var btnClearSearch: ImageButton

    private val allApps: MutableList<AppInfo> = mutableListOf()
    private val shownApps: MutableList<AppInfo> = mutableListOf()

    private lateinit var adapter: AppsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.apps_activity) // <- sinu XML nimi oligi apps_activity.xml

        appsRecyclerView = findViewById(R.id.appsRecyclerView)
        searchEditText = findViewById(R.id.searchEditText)
        btnClearSearch = findViewById(R.id.btnClearSearch)

        appsRecyclerView.layoutManager = GridLayoutManager(this, 4)

        adapter = AppsAdapter(
            apps = shownApps,
            onOpen = { app ->
                launchApp(app)
            },
            onLongClick = { app ->
                // praegu lihtsalt avab ka (et build kindlalt tööle saada)
                // hiljem saad siia panna AppActionsBottomSheet / menüü
                launchApp(app)
            }
        )

        appsRecyclerView.adapter = adapter

        loadApps()
        setupSearch()
    }

    private fun loadApps() {
        allApps.clear()

        val pm = packageManager
        val intent = Intent(Intent.ACTION_MAIN, null).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }

        val resolved: List<ResolveInfo> =
            pm.queryIntentActivities(intent, 0)

        for (ri in resolved) {
            val ai = ri.activityInfo ?: continue
            val label = ri.loadLabel(pm)?.toString() ?: ai.name
            val icon = ri.loadIcon(pm)

            val app = AppInfo(
                label = label,
                packageName = ai.packageName,
                className = ai.name,
                icon = icon
            )
            allApps.add(app)
        }

        allApps.sortBy { it.label.lowercase() }

        shownApps.clear()
        shownApps.addAll(allApps)
        adapter.notifyDataSetChanged()
    }

    private fun setupSearch() {
        btnClearSearch.setOnClickListener {
            searchEditText.setText("")
        }

        searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit
            override fun afterTextChanged(s: Editable?) = Unit

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val q = s?.toString()?.trim().orEmpty()
                btnClearSearch.visibility = if (q.isEmpty()) View.GONE else View.VISIBLE
                filterApps(q)
            }
        })
    }

    private fun filterApps(query: String) {
        val q = query.lowercase()

        shownApps.clear()
        if (q.isEmpty()) {
            shownApps.addAll(allApps)
        } else {
            shownApps.addAll(allApps.filter { it.label.lowercase().contains(q) })
        }
        adapter.notifyDataSetChanged()
    }

    private fun launchApp(app: AppInfo) {
        try {
            val intent = Intent().apply {
                setClassName(app.packageName, app.className)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            startActivity(intent)
        } catch (_: Throwable) {
            // ei crashi kui mingi app ei käivitu
        }
    }
}
