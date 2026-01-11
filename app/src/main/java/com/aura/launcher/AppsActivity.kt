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

    private lateinit var recycler: RecyclerView
    private lateinit var searchEditText: EditText
    private lateinit var btnClear: ImageButton

    private lateinit var adapter: AppsAdapter

    private val allApps: MutableList<AppInfo> = mutableListOf()
    private val filteredApps: MutableList<AppInfo> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_apps) // <-- PEAB klappima activity_apps.xml-iga

        recycler = findViewById(R.id.appsRecyclerView)
        searchEditText = findViewById(R.id.searchEditText)
        btnClear = findViewById(R.id.btnClearSearch)

        recycler.layoutManager = GridLayoutManager(this, 4)

        adapter = AppsAdapter(
            items = filteredApps,
            onClick = { app -> launchApp(app) },
            onLongClick = { app ->
                // Kui sul on AppActionsBottomSheet, siis näita seda siin.
                // Muidu jäta praegu tühjaks (ei crashi).
                try {
                    AppActionsBottomSheet(this, app).show()
                } catch (_: Throwable) { }
            }
        )
        recycler.adapter = adapter

        loadApps()
        applyFilter("")

        btnClear.setOnClickListener {
            searchEditText.setText("")
        }

        searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val text = s?.toString() ?: ""
                btnClear.visibility = if (text.isEmpty()) View.GONE else View.VISIBLE
                applyFilter(text)
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

        val resolved: List<ResolveInfo> = pm.queryIntentActivities(intent, 0)

        for (ri in resolved) {
            val ai = ri.activityInfo ?: continue
            val label = ri.loadLabel(pm)?.toString() ?: ai.packageName
            val icon = ri.loadIcon(pm)

            allApps.add(
                AppInfo(
                    label = label,
                    packageName = ai.packageName,
                    className = ai.name,
                    icon = icon
                )
            )
        }

        allApps.sortBy { it.label.lowercase() }
    }

    private fun applyFilter(query: String) {
        val q = query.trim().lowercase()

        filteredApps.clear()
        if (q.isEmpty()) {
            filteredApps.addAll(allApps)
        } else {
            filteredApps.addAll(allApps.filter { it.label.lowercase().contains(q) })
        }

        adapter.submitList(filteredApps.toList())
    }

    private fun launchApp(app: AppInfo) {
        val launchIntent = packageManager.getLaunchIntentForPackage(app.packageName)
        if (launchIntent != null) {
            startActivity(launchIntent)
        }
    }
}
