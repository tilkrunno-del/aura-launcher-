package com.aura.launcher

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class HiddenAppsActivity : AppCompatActivity() {

    companion object {
        // PEAB ühtima AppsActivity-ga
        const val PREFS_NAME = "aura_prefs"
        const val KEY_HIDDEN_APPS = "hidden_apps"
    }

    private lateinit var recyclerView: RecyclerView
    private lateinit var btnBack: ImageButton
    private lateinit var adapter: HiddenAppsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_hidden_apps)

        btnBack = findViewById(R.id.btnBack)
        recyclerView = findViewById(R.id.hiddenRecyclerView)

        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = HiddenAppsAdapter(
            items = emptyList(),
            onRestoreClick = { app -> confirmRestore(app) },
            onOpenClick = { app -> openApp(app) }
        )
        recyclerView.adapter = adapter

        btnBack.setOnClickListener { finish() }
    }

    override fun onResume() {
        super.onResume()
        loadHiddenApps()
    }

    private fun loadHiddenApps() {
        val prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        val hiddenPackages = prefs.getStringSet(KEY_HIDDEN_APPS, emptySet()) ?: emptySet()

        val pm = packageManager
        val apps = mutableListOf<AppInfo>()

        // NB! Ära kasuta queryIntentActivities siinsamas,
        // muidu mõned süsteemi äpid võivad "kaduda".
        for (pkg in hiddenPackages) {
            try {
                val launchIntent = pm.getLaunchIntentForPackage(pkg) ?: continue
                val ri = pm.resolveActivity(launchIntent, 0) ?: continue
                val ai = pm.getApplicationInfo(pkg, 0)

                apps.add(
                    AppInfo(
                        label = pm.getApplicationLabel(ai).toString(),
                        packageName = pkg,
                        className = ri.activityInfo.name,
                        icon = pm.getApplicationIcon(ai)
                    )
                )
            } catch (_: Exception) {
                // ignore (pakett eemaldatud vms)
            }
        }

        apps.sortBy { it.label.lowercase() }
        adapter.update(apps)

        if (apps.isEmpty()) {
            Toast.makeText(this, getString(R.string.hidden_apps) + ": 0", Toast.LENGTH_SHORT).show()
        }
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
        val prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        val set = prefs.getStringSet(KEY_HIDDEN_APPS, emptySet())?.toMutableSet() ?: mutableSetOf()

        if (set.remove(packageName)) {
            prefs.edit().putStringSet(KEY_HIDDEN_APPS, set).apply()
            loadHiddenApps()
            Toast.makeText(this, getString(R.string.restore_app), Toast.LENGTH_SHORT).show()
        } else {
            // midagi polnud eemaldada
            Toast.makeText(this, getString(R.string.ok), Toast.LENGTH_SHORT).show()
        }
    }

    private fun openApp(app: AppInfo) {
        try {
            val pm = packageManager
            val intent = pm.getLaunchIntentForPackage(app.packageName)
            if (intent == null) {
                Toast.makeText(this, getString(R.string.cannot_open_app), Toast.LENGTH_SHORT).show()
                return
            }
            startActivity(intent)
        } catch (_: Exception) {
            Toast.makeText(this, getString(R.string.cannot_open_app), Toast.LENGTH_SHORT).show()
        }
    }

    // ---------------- Adapter ----------------

    private class HiddenAppsAdapter(
        private var items: List<AppInfo>,
        private val onRestoreClick: (AppInfo) -> Unit,
        private val onOpenClick: (AppInfo) -> Unit
    ) : RecyclerView.Adapter<HiddenAppsAdapter.VH>() {

        fun update(newItems: List<AppInfo>) {
            items = newItems
            notifyDataSetChanged()
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
            val view = LayoutInflater.from(parent.context)
                .inflate(android.R.layout.simple_list_item_2, parent, false)
            return VH(view)
        }

        override fun onBindViewHolder(holder: VH, position: Int) {
            val app = items[position]
            holder.title.text = app.label
            holder.subtitle.text = app.packageName

            holder.itemView.setOnClickListener { onOpenClick(app) }
            holder.itemView.setOnLongClickListener {
                onRestoreClick(app)
                true
            }
        }

        override fun getItemCount(): Int = items.size

        class VH(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val title: TextView = itemView.findViewById(android.R.id.text1)
            val subtitle: TextView = itemView.findViewById(android.R.id.text2)
        }
    }
}
