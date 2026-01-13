package com.aura.launcher

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.util.Locale

class HiddenAppsActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: AppsAdapter

    private val hiddenApps = mutableListOf<AppInfo>()

    // Sama prefs + key mis AppsActivity patchis
    private val prefs by lazy { getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_hidden_apps)

        // Back
        findViewById<ImageButton>(R.id.btnBack).setOnClickListener { finish() }

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
        val intent = Intent(Intent.ACTION_MAIN).apply { addCategory(Intent.CATEGORY_LAUNCHER) }
        val resolved = pm.queryIntentActivities(intent, PackageManager.MATCH_ALL)

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

        hiddenApps.sortBy { it.label.lowercase(Locale.getDefault()) }
        adapter.updateList(hiddenApps.toList())
    }

    private fun openApp(app: AppInfo) {
        try {
            // Turvaline avamine: className abil (kui getLaunchIntent on null, see töötab tihti paremini)
            val i = Intent(Intent.ACTION_MAIN).apply {
                addCategory(Intent.CATEGORY_LAUNCHER)
                setClassName(app.packageName, app.className)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            startActivity(i)
        } catch (_: Exception) {
            // fallback
            val fallback = packageManager.getLaunchIntentForPackage(app.packageName)
            if (fallback != null) startActivity(fallback)
            else Toast.makeText(this, getString(R.string.cannot_open_app), Toast.LENGTH_SHORT).show()
        }
    }

    private fun confirmRestore(app: AppInfo) {
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.restore_app))
            .setMessage("Kas soovid “${app.label}” tagasi nähtavaks teha?")
            .setPositiveButton(getString(R.string.ok)) { _, _ ->
                restoreApp(app.packageName)
                Toast.makeText(this, "${app.label} taastatud", Toast.LENGTH_SHORT).show()
                reloadHiddenApps()
            }
            .setNegativeButton(getString(R.string.cancel), null)
            .show()
    }

    private fun restoreApp(packageName: String) {
        val set = getHiddenSet().toMutableSet()
        set.remove(packageName)
        prefs.edit().putStringSet(KEY_HIDDEN_SET, set).apply()
    }

    private fun getHiddenSet(): Set<String> {
        return prefs.getStringSet(KEY_HIDDEN_SET, emptySet())?.toSet() ?: emptySet()
    }

    companion object {
        private const val PREFS_NAME = "aura_launcher_prefs"
        private const val KEY_HIDDEN_SET = "hidden_apps_set"
    }
}
