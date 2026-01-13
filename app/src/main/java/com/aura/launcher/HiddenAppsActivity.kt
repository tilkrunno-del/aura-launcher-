package com.aura.launcher

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView

class HiddenAppsActivity : AppCompatActivity() {

    private lateinit var prefs: SharedPreferences

    private lateinit var recyclerView: RecyclerView
    private lateinit var btnBack: ImageButton
    private lateinit var adapter: AppsAdapter

    private val hiddenApps = mutableListOf<AppInfo>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_hidden_apps)

        prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

        btnBack = findViewById(R.id.btnBack)
        recyclerView = findViewById(R.id.hiddenRecyclerView)

        recyclerView.layoutManager = GridLayoutManager(this, 4)

        adapter = AppsAdapter(
            apps = emptyList(),
            onClick = { app -> openApp(app) },
            onLongClick = { app ->
                confirmRestore(app)
                true
            }
        )

        recyclerView.adapter = adapter

        btnBack.setOnClickListener { finish() }
    }

    override fun onResume() {
        super.onResume()
        loadHiddenApps()
    }

    private fun loadHiddenApps() {
        hiddenApps.clear()

        val hiddenSet = getHiddenSet()
        if (hiddenSet.isEmpty()) {
            adapter.updateList(emptyList())
            Toast.makeText(this, getString(R.string.hidden_apps) + ": 0", Toast.LENGTH_SHORT).show()
            return
        }

        val pm = packageManager

        // Kindel: resolve peidetud pakettidest (mitte queryIntentActivities kaudu)
        for (pkg in hiddenSet) {
            try {
                val launchIntent = pm.getLaunchIntentForPackage(pkg) ?: continue
                val ri = pm.resolveActivity(launchIntent, 0) ?: continue
                val ai = pm.getApplicationInfo(pkg, 0)

                hiddenApps.add(
                    AppInfo(
                        label = pm.getApplicationLabel(ai).toString(),
                        packageName = pkg,
                        className = ri.activityInfo.name,
                        icon = pm.getApplicationIcon(ai)
                    )
                )
            } catch (_: Exception) {
                // ignore
            }
        }

        hiddenApps.sortBy { it.label.lowercase() }
        adapter.updateList(hiddenApps.toList())
    }

    private fun confirmRestore(app: AppInfo) {
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.restore_app))
            .setMessage(app.label)
            .setPositiveButton(getString(R.string.ok)) { _, _ ->
                restoreApp(app.packageName)
            }
            .setNegativeButton(getString(R.string.cancel), null)
            .show()
    }

    private fun restoreApp(packageName: String) {
        val set = getHiddenSet()
        if (set.remove(packageName)) {
            saveHiddenSet(set)
            Toast.makeText(this, getString(R.string.restore_app), Toast.LENGTH_SHORT).show()
            loadHiddenApps()
        }
    }

    private fun openApp(app: AppInfo) {
        try {
            val launchIntent = packageManager.getLaunchIntentForPackage(app.packageName)
            if (launchIntent != null) {
                startActivity(launchIntent)
            } else {
                Toast.makeText(this, getString(R.string.cannot_open_app), Toast.LENGTH_SHORT).show()
            }
        } catch (_: Exception) {
            Toast.makeText(this, getString(R.string.cannot_open_app), Toast.LENGTH_SHORT).show()
        }
    }

    private fun getHiddenSet(): MutableSet<String> {
        return prefs.getStringSet(KEY_HIDDEN_SET, emptySet())?.toMutableSet() ?: mutableSetOf()
    }

    private fun saveHiddenSet(set: Set<String>) {
        prefs.edit().putStringSet(KEY_HIDDEN_SET, set).apply()
    }

    companion object {
        // PEAB ÜHTIMA AppsActivity-ga
        private const val PREFS_NAME = "aura_launcher_prefs"
        private const val KEY_HIDDEN_SET = "hidden_apps_set"
    }
}
