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
        private const val PREFS_NAME = "aura_launcher_prefs"
        private const val KEY_HIDDEN_SET = "hidden_apps"
        private const val HIDDEN_TRIGGER = "***" // kirjuta otsingusse *** et näha peidetud äppe
    }

    private lateinit var recyclerView: RecyclerView
    private lateinit var searchEditText: EditText
    private lateinit var adapter: AppsAdapter

    private val allApps = mutableListOf<AppInfo>()
    private val visibleApps = mutableListOf<AppInfo>()
    private val hiddenApps = mutableListOf<AppInfo>()

    private var showingHidden = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_apps)

        recyclerView = findViewById(R.id.appsRecyclerView)
        searchEditText = findViewById(R.id.searchEditText)

        // 3 veergu (nagu sul juba “ok, 3 veergu”)
        recyclerView.layoutManager = GridLayoutManager(this, 3)

        adapter = AppsAdapter(
            onAppClick = { app -> launchApp(app) },
            onAppLongClick = { app -> toggleHidden(app) }
        )
        recyclerView.adapter = adapter

        // Lae äpid
        allApps.clear()
        allApps.addAll(loadInstalledApps(packageManager))
        rebuildListsFromPrefs()

        // Otsing + hidden trigger
        searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun afterTextChanged(s: Editable?) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val q = s?.toString().orEmpty().trim()

                if (q == HIDDEN_TRIGGER) {
                    if (!showingHidden) {
                        showingHidden = true
                        adapter.submitApps(hiddenApps)
                        adapter.filterApps("") // ära filtreeri *** sõna järgi
                        Toast.makeText(this@AppsActivity, "Peidetud rakendused", Toast.LENGTH_SHORT).show()
                    }
                    return
                }

                if (showingHidden) {
                    // kui lahkusid *** režiimist, mine tagasi nähtavate peale
                    showingHidden = false
                    adapter.submitApps(visibleApps)
                }

                adapter.filterApps(q)
            }
        })

        // Algne query MainActivity-st
        val initialQuery = intent.getStringExtra(EXTRA_QUERY)
        if (!initialQuery.isNullOrBlank()) {
            searchEditText.setText(initialQuery)
            searchEditText.setSelection(initialQuery.length)
            // TextWatcher teeb ülejäänu ise
        } else {
            // kui query puudub, näita kohe nähtavaid
            adapter.submitApps(visibleApps)
            adapter.filterApps("")
        }
    }

    private fun launchApp(app: AppInfo) {
        val launchIntent = Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
            setClassName(app.packageName, app.className)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        try {
            startActivity(launchIntent)
        } catch (_: Exception) {
            Toast.makeText(this, "Ei saanud avada: ${app.label}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun toggleHidden(app: AppInfo) {
        val prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        val set = prefs.getStringSet(KEY_HIDDEN_SET, emptySet())?.toMutableSet() ?: mutableSetOf()

        val key = appKey(app)
        val nowHidden: Boolean

        if (set.contains(key)) {
            set.remove(key)
            nowHidden = false
        } else {
            set.add(key)
            nowHidden = true
        }

        prefs.edit().putStringSet(KEY_HIDDEN_SET, set).apply()

        rebuildListsFromPrefs()

        if (showingHidden) {
            adapter.submitApps(hiddenApps)
            adapter.filterApps("") // hidden listis ei filtreeri automaatselt
            Toast.makeText(this, if (nowHidden) "Peidetud" else "Tagasi toodud", Toast.LENGTH_SHORT).show()
        } else {
            adapter.submitApps(visibleApps)
            // hoia praegune otsing alles
            adapter.filterApps(searchEditText.text?.toString().orEmpty().trim())
            Toast.makeText(this, if (nowHidden) "Peidetud: ${app.label}" else "Tagasi: ${app.label}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun rebuildListsFromPrefs() {
        val prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        val hiddenSet = prefs.getStringSet(KEY_HIDDEN_SET, emptySet()) ?: emptySet()

        visibleApps.clear()
        hiddenApps.clear()

        for (app in allApps) {
            if (hiddenSet.contains(appKey(app))) hiddenApps.add(app) else visibleApps.add(app)
        }

        // kui adapter pole veel listi saanud, anna talle nähtavad
        if (!showingHidden) {
            adapter.submitApps(visibleApps)
        } else {
            adapter.submitApps(hiddenApps)
        }
    }

    private fun appKey(app: AppInfo): String = "${app.packageName}/${app.className}"

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
