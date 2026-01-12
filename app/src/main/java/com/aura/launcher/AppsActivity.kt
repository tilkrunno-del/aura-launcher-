package com.aura.launcher

import android.content.ComponentName
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.EditText
import android.widget.ImageButton
import android.widget.PopupMenu
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView

class AppsActivity : AppCompatActivity() {

    private lateinit var appsRecyclerView: RecyclerView
    private lateinit var searchEditText: EditText
    private lateinit var btnClearSearch: ImageButton

    private lateinit var adapter: AppsAdapter

    private var allApps: List<AppInfo> = emptyList()
    private var filteredApps: List<AppInfo> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_apps)

        appsRecyclerView = findViewById(R.id.appsRecyclerView)
        searchEditText = findViewById(R.id.searchEditText)
        btnClearSearch = findViewById(R.id.btnClearSearch)

        // Grid (muuda spanCount kui tahad)
        appsRecyclerView.layoutManager = GridLayoutManager(this, 4)

        adapter = AppsAdapter(
            apps = emptyList(),
            onClick = { app -> launchApp(app) },
            onLongClick = { app ->
                showAppMenu(app)
                true
            }
        )
        appsRecyclerView.adapter = adapter

        // Lae äpid
        allApps = getInstalledApps()
        filteredApps = allApps
        adapter.updateList(filteredApps)

        // Search
        btnClearSearch.setOnClickListener {
            searchEditText.setText("")
        }

        searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val q = (s?.toString() ?: "").trim()
                btnClearSearch.visibility = if (q.isEmpty()) View.GONE else View.VISIBLE

                filteredApps = if (q.isEmpty()) {
                    allApps
                } else {
                    val lower = q.lowercase()
                    allApps.filter { it.label.lowercase().contains(lower) }
                }
                adapter.updateList(filteredApps)
            }

            override fun afterTextChanged(s: Editable?) = Unit
        })
    }

    private fun getInstalledApps(): List<AppInfo> {
        val pm = packageManager

        val intent = Intent(Intent.ACTION_MAIN, null).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }

        val resolved = pm.queryIntentActivities(intent, PackageManager.MATCH_ALL)

        val list = resolved.mapNotNull { ri ->
            val ai = ri.activityInfo ?: return@mapNotNull null
            val label = ri.loadLabel(pm)?.toString() ?: ai.packageName
            val icon = ri.loadIcon(pm)

            AppInfo(
                label = label,
                packageName = ai.packageName,
                className = ai.name,
                icon = icon
            )
        }

        // Sorteeri nime järgi
        return list.sortedBy { it.label.lowercase() }
    }

    private fun launchApp(app: AppInfo) {
        try {
            val cn = ComponentName(app.packageName, app.className)
            val i = Intent(Intent.ACTION_MAIN).apply {
                addCategory(Intent.CATEGORY_LAUNCHER)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
                component = cn
            }
            startActivity(i)
        } catch (e: Exception) {
            Toast.makeText(this, "Ei saa avada: ${app.label}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showAppMenu(app: AppInfo) {
        val anchor = appsRecyclerView // lihtne ankurdus, kui itemView’t pole siin
        val menu = PopupMenu(this, anchor)
        menu.menu.add(0, 1, 0, "Ava")
        menu.menu.add(0, 2, 1, "Rakenduse info")
        menu.menu.add(0, 3, 2, "Desinstalli")

        menu.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                1 -> {
                    launchApp(app)
                    true
                }
                2 -> {
                    openAppInfo(app.packageName)
                    true
                }
                3 -> {
                    uninstallApp(app.packageName)
                    true
                }
                else -> false
            }
        }
        menu.show()
    }

    private fun openAppInfo(pkg: String) {
        try {
            val i = Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.parse("package:$pkg")
            }
            startActivity(i)
        } catch (_: Exception) {
            Toast.makeText(this, "Ei saa avada App Info", Toast.LENGTH_SHORT).show()
        }
    }

    private fun uninstallApp(pkg: String) {
        try {
            val i = Intent(Intent.ACTION_DELETE).apply {
                data = Uri.parse("package:$pkg")
            }
            startActivity(i)
        } catch (_: Exception) {
            Toast.makeText(this, "Ei saa desinstallida", Toast.LENGTH_SHORT).show()
        }
    }
}
