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
import java.util.Locale

class AppsActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_QUERY = "EXTRA_QUERY"

        private const val PREFS = "aura_launcher_prefs"
        private const val KEY_HIDDEN_SET = "hidden_apps"
        private const val HIDDEN_TRIGGER = "***"
    }

    private lateinit var recyclerView: RecyclerView
    private lateinit var searchEditText: EditText
    private lateinit var adapter: AppsAdapter

    private val allApps = mutableListOf<AppInfo>()
    private val visibleApps = mutableListOf<AppInfo>()
    private val hiddenApps = mutableListOf<AppInfo>()

    private var showingHidden = false

    override fun onCreate(savedInstanceState: Bundle?) {
        // 🌙 rakenda teema
        ThemePrefs.applyNightMode(this)

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_apps)

        recyclerView = findViewById(R.id.appsRecyclerView)
        searchEditText = findViewById(R.id.searchEditText)

        // 🧱 veerud seadete järgi
        val spanCount = ThemePrefs.getSpanCount(this)
        recyclerView.layoutManager = GridLayoutManager(this, spanCount)

        // 📦 adapter
        adapter = AppsAdapter(
            onAppClick = { app -> launchApp(app) },
            onAppLongClick = { app -> toggleHidden(app) }
        )
        recyclerView.adapter = adapter

        // 📲 lae äpid
        allApps.clear()
        allApps.addAll(loadInstalledApps(packageManager))
        rebuildListsFromPrefs()

        // 🔍 otsing + hidden trigger
        searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun afterTextChanged(s: Editable?) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val q = s?.toString().orEmpty().trim()

                // *** → peidetud
                if (q == HIDDEN_TRIGGER) {
                    if (!showingHidden) {
                        showingHidden = true
                        adapter.submitApps(hiddenApps)
                        adapter.filterApps("")
                        Toast.makeText(
                            this@AppsActivity,
                            "Peidetud rakendused",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    return
                }

                // tagasi nähtavatesse
                if (showingHidden) {
                    showingHidden = false
                    adapter.submitApps(visibleApps)
                }

                adapter.filterApps(q)
                recyclerView.scrollToPosition(0)
            }
        })

        // algne query MainActivity-st
        val initialQuery = intent.getStringExtra(EXTRA_QUERY)
        if (!initialQuery.isNullOrBlank()) {
            searchEditText.setText(initialQuery)
            searchEditText.setSelection(initialQuery.length)
        } else {
            adapter.submitApps(visibleApps)
            adapter.filterApps("")
        }
    }

    // 🚀 rakenduse käivitamine
    private fun launchApp(app: AppInfo) {
        val pm = packageManager

        val primary = pm.getLaunchIntentForPackage(app.packageName)?.apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        val fallback = Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
            setClassName(app.packageName, app.className)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        try {
            startActivity(primary ?: fallback)
        } catch (_: Exception) {
            Toast.makeText(this, "Ei saa avada: ${app.label}", Toast.LENGTH_SHORT).show()
        }
    }

    // 🙈 peida / too tagasi
    private fun toggleHidden(app: AppInfo) {
        val prefs = getSharedPreferences(PREFS, MODE_PRIVATE)
        val set = prefs.getStringSet(KEY_HIDDEN_SET, emptySet())?.toMutableSet() ?: mutableSetOf()

        val key = appKey(app)
        val hiddenNow: Boolean

        if (set.contains(key)) {
            set.remove(key)
            hiddenNow = false
        } else {
            set.add(key)
            hiddenNow = true
        }

        prefs.edit().putStringSet(KEY_HIDDEN_SET, set).apply()
        rebuildListsFromPrefs()

        if (showingHidden) {
            adapter.submitApps(hiddenApps)
            adapter.filterApps("")
        } else {
            adapter.submitApps(visibleApps)
            adapter.filterApps(searchEditText.text?.toString().orEmpty())
        }

        Toast.makeText(
            this,
            if (hiddenNow) "Peidetud: ${app.label}" else "Tagasi toodud: ${app.label}",
            Toast.LENGTH_SHORT
        ).show()
    }

    private fun rebuildListsFromPrefs() {
        val prefs = getSharedPreferences(PREFS, MODE_PRIVATE)
        val hiddenSet = prefs.getStringSet(KEY_HIDDEN_SET, emptySet()) ?: emptySet()

        visibleApps.clear()
        hiddenApps.clear()

        for (app in allApps) {
            if (hiddenSet.contains(appKey(app))) hiddenApps.add(app)
            else visibleApps.add(app)
        }

        if (!showingHidden) {
            adapter.submitApps(visibleApps)
        } else {
            adapter.submitApps(hiddenApps)
        }
    }

    private fun appKey(app: AppInfo): String =
        "${app.packageName}/${app.className}"

    // 📦 äppide laadimine
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
        }.sortedBy { it.label.lowercase(Locale.getDefault()) }
    }
}
