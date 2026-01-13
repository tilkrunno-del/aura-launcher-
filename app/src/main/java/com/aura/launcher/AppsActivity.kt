package com.aura.launcher

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView

class AppsActivity : AppCompatActivity() {

    private lateinit var prefs: SharedPreferences

    private lateinit var recyclerView: RecyclerView
    private lateinit var searchEditText: EditText
    private lateinit var btnClearSearch: ImageButton

    private lateinit var adapter: AppsAdapter

    private val allApps = mutableListOf<AppInfo>()       // kõik äpid
    private val visibleApps = mutableListOf<AppInfo>()   // nähtavad (peidetud välja jäetud)
    private val filteredApps = mutableListOf<AppInfo>()  // otsingufilter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_apps)

        prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

        recyclerView = findViewById(R.id.appsRecyclerView)
        searchEditText = findViewById(R.id.searchEditText)
        btnClearSearch = findViewById(R.id.btnClearSearch)

        recyclerView.layoutManager = GridLayoutManager(this, 4)

        adapter = AppsAdapter(
            apps = emptyList(),
            onClick = { app -> openApp(app) },
            onLongClick = { app ->
                hideApp(app)
                true
            }
        )

        recyclerView.adapter = adapter

        btnClearSearch.setOnClickListener {
            searchEditText.setText("")
        }

        searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                btnClearSearch.visibility = if (s.isNullOrBlank()) View.GONE else View.VISIBLE
                applyFilter(s?.toString().orEmpty())
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        loadAllApps()
        applyHiddenFilter()
        applyFilter(searchEditText.text?.toString().orEmpty())
    }

    override fun onResume() {
        super.onResume()
        // kui HiddenAppsActivity-st tullakse tagasi, siis värskenda nähtavaid
        applyHiddenFilter()
        applyFilter(searchEditText.text?.toString().orEmpty())
    }

    private fun loadAllApps() {
        allApps.clear()

        val pm = packageManager
        val intent = Intent(Intent.ACTION_MAIN).apply { addCategory(Intent.CATEGORY_LAUNCHER) }
        val resolved = pm.queryIntentActivities(intent, PackageManager.MATCH_ALL)

        for (ri in resolved) {
            val pkg = ri.activityInfo.packageName
            val cls = ri.activityInfo.name ?: ""
            val label = ri.loadLabel(pm)?.toString() ?: pkg
            val icon = ri.loadIcon(pm)

            allApps.add(
                AppInfo(
                    label = label,
                    packageName = pkg,
                    className = cls,
                    icon = icon
                )
            )
        }

        allApps.sortBy { it.label.lowercase() }
    }

    private fun applyHiddenFilter() {
        visibleApps.clear()

        val hiddenSet = getHiddenSet()
        for (app in allApps) {
            if (!hiddenSet.contains(app.packageName)) {
                visibleApps.add(app)
            }
        }
    }

    private fun applyFilter(query: String) {
        val q = query.trim().lowercase()

        filteredApps.clear()

        if (q.isEmpty()) {
            filteredApps.addAll(visibleApps)
        } else {
            for (app in visibleApps) {
                if (app.label.lowercase().contains(q) || app.packageName.lowercase().contains(q)) {
                    filteredApps.add(app)
                }
            }
        }

        adapter.updateList(filteredApps.toList())
    }

    private fun hideApp(app: AppInfo) {
        val set = getHiddenSet()
        set.add(app.packageName)
        saveHiddenSet(set)

        Toast.makeText(this, "${app.label} peidetud", Toast.LENGTH_SHORT).show()

        // uuenda kohe listi
        applyHiddenFilter()
        applyFilter(searchEditText.text?.toString().orEmpty())
    }

    private fun openApp(app: AppInfo) {
        try {
            val launchIntent = packageManager.getLaunchIntentForPackage(app.packageName)
            if (launchIntent != null) {
                startActivity(launchIntent)
            } else {
                Toast.makeText(this, "Ei saa avada: ${app.label}", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Viga avamisel: ${app.label}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun getHiddenSet(): MutableSet<String> {
        return prefs.getStringSet(KEY_HIDDEN_SET, emptySet())?.toMutableSet() ?: mutableSetOf()
    }

    private fun saveHiddenSet(set: Set<String>) {
        prefs.edit().putStringSet(KEY_HIDDEN_SET, set).apply()
    }

    companion object {
        private const val PREFS_NAME = "aura_launcher_prefs"
        private const val KEY_HIDDEN_SET = "hidden_apps_set"
    }
}
