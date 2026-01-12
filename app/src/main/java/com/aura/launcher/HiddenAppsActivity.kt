package com.aura.launcher

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView

class HiddenAppsActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: AppsAdapter

    private val hiddenApps = mutableListOf<AppInfo>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_hidden_apps)

        recyclerView = findViewById(R.id.hiddenRecyclerView)
        val btnBack: ImageButton = findViewById(R.id.btnBack)

        recyclerView.layoutManager = GridLayoutManager(this, 4)

        adapter = AppsAdapter(
            apps = hiddenApps,
            onClick = { app ->
                // Tavavajutus → käivita app
                launchApp(app)
            },
            onLongClick = { app ->
                // Pikk vajutus → taasta (unhide)
                unhideApp(app)
                true
            }
        )

        recyclerView.adapter = adapter

        btnBack.setOnClickListener {
            finish()
        }

        loadHiddenApps()
    }

    /**
     * Laeb peidetud äpid SharedPreferences'ist
     */
    private fun loadHiddenApps() {
        hiddenApps.clear()

        val prefs = getSharedPreferences("hidden_apps", MODE_PRIVATE)
        val hiddenPackages = prefs.getStringSet("packages", emptySet()) ?: emptySet()

        val pm = packageManager
        val intent = Intent(Intent.ACTION_MAIN, null).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }

        val allApps = pm.queryIntentActivities(intent, 0)

        for (resolveInfo in allApps) {
            val pkg = resolveInfo.activityInfo.packageName

            if (hiddenPackages.contains(pkg)) {
                val label = resolveInfo.loadLabel(pm).toString()
                val icon = resolveInfo.loadIcon(pm)
                val className = resolveInfo.activityInfo.name

                hiddenApps.add(
                    AppInfo(
                        label = label,
                        packageName = pkg,
                        className = className,
                        icon = icon
                    )
                )
            }
        }

        hiddenApps.sortBy { it.label.lowercase() }
        adapter.updateList(hiddenApps)
    }

    /**
     * Taastab (unhide) äpi
     */
    private fun unhideApp(app: AppInfo) {
        val prefs = getSharedPreferences("hidden_apps", MODE_PRIVATE)
        val set = prefs.getStringSet("packages", mutableSetOf())?.toMutableSet()
            ?: mutableSetOf()

        set.remove(app.packageName)

        prefs.edit()
            .putStringSet("packages", set)
            .apply()

        Toast.makeText(this, "Taastatud: ${app.label}", Toast.LENGTH_SHORT).show()

        loadHiddenApps()
    }

    /**
     * Käivitab rakenduse
     */
    private fun launchApp(app: AppInfo) {
        try {
            val intent = Intent().apply {
                setClassName(app.packageName, app.className)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(this, "Ei saa avada rakendust", Toast.LENGTH_SHORT).show()
        }
    }
}
