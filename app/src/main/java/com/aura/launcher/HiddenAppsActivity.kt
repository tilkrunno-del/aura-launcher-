package com.aura.launcher

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView

class HiddenAppsActivity : AppCompatActivity() {

    private lateinit var prefs: SharedPreferences
    private lateinit var recycler: RecyclerView
    private lateinit var adapter: AppsAdapter

    private val hiddenApps = mutableListOf<AppInfo>() // siin on ainult peidetud äpid

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_hidden_apps)

        prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

        // tagasi nupp
        findViewById<ImageButton>(R.id.btnBack).setOnClickListener {
            finish()
        }

        recycler = findViewById(R.id.hiddenRecyclerView)
        recycler.layoutManager = GridLayoutManager(this, 4)

        adapter = AppsAdapter(
            apps = emptyList(),
            onClick = { app ->
                // klikiga -> taasta (unhide)
                unhideApp(app.packageName)
                Toast.makeText(this, "${app.label} taastatud", Toast.LENGTH_SHORT).show()
                reloadHiddenApps()
            },
            onLongClick = { _ ->
                // pole vaja pikka vajutust siin
                true
            }
        )

        recycler.adapter = adapter

        reloadHiddenApps()
    }

    override fun onResume() {
        super.onResume()
        reloadHiddenApps()
    }

    private fun reloadHiddenApps() {
        hiddenApps.clear()

        val hiddenSet = getHiddenSet()
        if (hiddenSet.isEmpty()) {
            adapter.updateList(emptyList())
            return
        }

        val pm = packageManager
        val launchIntent = Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }

        val resolved = pm.queryIntentActivities(launchIntent, PackageManager.MATCH_ALL)

        for (ri in resolved) {
            val pkg = ri.activityInfo.packageName
            if (!hiddenSet.contains(pkg)) continue

            val label = ri.loadLabel(pm)?.toString() ?: pkg
            val icon = ri.loadIcon(pm)
            val className = ri.activityInfo.name ?: ""

            hiddenApps.add(
                AppInfo(
                    label = label,
                    packageName = pkg,
                    className = className,
                    icon = icon
                )
            )
        }

        hiddenApps.sortBy { it.label.lowercase() }
        adapter.updateList(hiddenApps.toList())
    }

    private fun getHiddenSet(): MutableSet<String> {
        return prefs.getStringSet(KEY_HIDDEN_SET, emptySet())?.toMutableSet() ?: mutableSetOf()
    }

    private fun saveHiddenSet(set: Set<String>) {
        prefs.edit().putStringSet(KEY_HIDDEN_SET, set).apply()
    }

    private fun unhideApp(packageName: String) {
        val set = getHiddenSet()
        set.remove(packageName)
        saveHiddenSet(set)
    }

    companion object {
        private const val PREFS_NAME = "aura_launcher_prefs"
        private const val KEY_HIDDEN_SET = "hidden_apps_set"
    }
}
