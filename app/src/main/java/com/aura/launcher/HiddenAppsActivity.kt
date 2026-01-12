package com.aura.launcher

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView

/**
 * Hidden apps screen:
 * - Shows only apps that are marked as hidden
 * - Tap  -> open app
 * - Long -> "Taasta" (unhide) or "Jäta peidetuks"
 *
 * Storage: SharedPreferences string-set of package names.
 */
class HiddenAppsActivity : AppCompatActivity() {

    private lateinit var prefs: SharedPreferences

    private lateinit var recycler: RecyclerView
    private var emptyText: TextView? = null

    private lateinit var adapter: AppsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_hidden_apps)

        prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

        recycler = findViewById(R.id.hiddenAppsRecyclerView)
        emptyText = findViewById<TextView?>(R.id.emptyText)

        // Grid: 4 columns (muuda kui tahad)
        recycler.layoutManager = GridLayoutManager(this, 4)

        adapter = AppsAdapter(
            apps = emptyList(),
            onClick = { app -> openApp(app.packageName) },
            onLongClick = { app ->
                showUnhideDialog(app)
                true
            }
        )
        recycler.adapter = adapter

        // Back nupp (kui sul layoutis olemas)
        // Kui sul on teise ID-ga, muuda siit:
        tryBindBackButton()

        refresh()
    }

    override fun onResume() {
        super.onResume()
        refresh()
    }

    private fun refresh() {
        val hidden = getHiddenPackages(this)
        val hiddenApps = loadLaunchableApps()
            .filter { hidden.contains(it.packageName) }
            .sortedBy { it.label.lowercase() }

        adapter.updateList(hiddenApps)
        updateEmptyState(hiddenApps.isEmpty())
    }

    private fun updateEmptyState(isEmpty: Boolean) {
        emptyText?.let { tv ->
            tv.text = getString(R.string.no_hidden_apps) // kui olemas
            tv.visibility = if (isEmpty) android.view.View.VISIBLE else android.view.View.GONE
        }
        recycler.visibility = if (isEmpty) android.view.View.GONE else android.view.View.VISIBLE
    }

    private fun showUnhideDialog(app: AppInfo) {
        AlertDialog.Builder(this)
            .setTitle("Taasta rakendus?")
            .setMessage("Kas soovid rakenduse “${app.label}” peidetud nimekirjast eemaldada?")
            .setPositiveButton("Taasta") { _, _ ->
                unhidePackage(this, app.packageName)
                Toast.makeText(this, "Taastatud: ${app.label}", Toast.LENGTH_SHORT).show()
                refresh()
            }
            .setNegativeButton("Jäta peidetuks", null)
            .show()
    }

    private fun openApp(packageName: String) {
        val intent: Intent? = packageManager.getLaunchIntentForPackage(packageName)
        if (intent == null) {
            Toast.makeText(this, "Ei saa avada (puudub launcher intent)", Toast.LENGTH_SHORT).show()
            return
        }
        startActivity(intent)
    }

    /**
     * Load only apps that have launcher intent (ehk "päris" rakendused).
     */
    private fun loadLaunchableApps(): List<AppInfo> {
        val pm = packageManager
        val apps = pm.getInstalledApplications(0)

        val result = ArrayList<AppInfo>()
        for (ai in apps) {
            val pkg = ai.packageName ?: continue
            val launchIntent = pm.getLaunchIntentForPackage(pkg) ?: continue

            // (optional) ignore ourselves
            if (pkg == packageName) continue

            val label = pm.getApplicationLabel(ai)?.toString() ?: pkg
            val icon = pm.getApplicationIcon(ai)

            // AppInfo peab sisaldama vähemalt: label, packageName, icon
            result.add(AppInfo(label = label, packageName = pkg, icon = icon))
        }
        return result
    }

    /**
     * Kui sul on activity_hidden_apps.xml-is back nupp.
     * Muuda ID vastavalt enda layoutile.
     */
    private fun tryBindBackButton() {
        // Kui sul on nt ImageButton id=btnBack
        val back = findViewById<android.view.View?>(R.id.btnBack)
        back?.setOnClickListener { finish() }
    }

    // -------------------------
    // Static helpers (peida/taasta)
    // -------------------------
    companion object {
        private const val PREFS_NAME = "aura_prefs"
        private const val KEY_HIDDEN_APPS = "hidden_apps"

        fun getHiddenPackages(context: Context): Set<String> {
            val p = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            return p.getStringSet(KEY_HIDDEN_APPS, emptySet())?.toSet() ?: emptySet()
        }

        fun hidePackage(context: Context, packageName: String) {
            val p = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val current = p.getStringSet(KEY_HIDDEN_APPS, emptySet())?.toMutableSet() ?: mutableSetOf()
            current.add(packageName)
            p.edit().putStringSet(KEY_HIDDEN_APPS, current).apply()
        }

        fun unhidePackage(context: Context, packageName: String) {
            val p = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val current = p.getStringSet(KEY_HIDDEN_APPS, emptySet())?.toMutableSet() ?: mutableSetOf()
            current.remove(packageName)
            p.edit().putStringSet(KEY_HIDDEN_APPS, current).apply()
        }

        fun isHidden(context: Context, packageName: String): Boolean {
            return getHiddenPackages(context).contains(packageName)
        }
    }
}
