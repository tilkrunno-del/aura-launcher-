package com.aura.launcher

import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.PopupMenu
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.util.Locale

class AppsActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var searchEditText: EditText
    private lateinit var clearButton: Button

    private lateinit var adapter: AppsAdapter
    private var allApps: List<AppInfo> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_apps)

        // --- Views ---
        recyclerView = findViewById(R.id.appsRecyclerView)
        searchEditText = findViewById(R.id.searchEditText)
        clearButton = findViewById(R.id.btnClearSearch)

        // --- RecyclerView ---
        recyclerView.layoutManager = GridLayoutManager(this, 4)

        adapter = AppsAdapter(
            apps = emptyList(),
            onClick = { app -> launchApp(app) },
            onLongPress = { view, app -> showAppMenu(view, app) }
        )

        recyclerView.adapter = adapter

        // --- Load apps ---
        allApps = loadInstalledApps()
        adapter.submitList(allApps)

        // --- Search ---
        searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                val query = s?.toString().orEmpty()
                adapter.filterApps(query)
                clearButton.visibility = if (query.isBlank()) View.GONE else View.VISIBLE
            }
        })

        clearButton.setOnClickListener {
            searchEditText.setText("")
        }
    }

    // --------------------------------------------------------------------

    private fun launchApp(app: AppInfo) {
        val intent = packageManager.getLaunchIntentForPackage(app.packageName)
        if (intent == null) {
            Toast.makeText(this, "Cannot launch app", Toast.LENGTH_SHORT).show()
            return
        }
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
    }

    private fun showAppMenu(anchor: View, app: AppInfo) {
        val popup = PopupMenu(this, anchor)
        popup.menu.add("App info")
        popup.menu.add("Uninstall")

        popup.setOnMenuItemClickListener { item ->
            when (item.title.toString()) {
                "App info" -> {
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                        data = Uri.parse("package:${app.packageName}")
                    }
                    startActivity(intent)
                    true
                }

                "Uninstall" -> {
                    val intent = Intent(Intent.ACTION_DELETE).apply {
                        data = Uri.parse("package:${app.packageName}")
                    }
                    startActivity(intent)
                    true
                }

                else -> false
            }
        }
        popup.show()
    }

    // --------------------------------------------------------------------

    private fun loadInstalledApps(): List<AppInfo> {
        val pm = packageManager

        val intent = Intent(Intent.ACTION_MAIN, null).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }

        val resolveInfos = pm.queryIntentActivities(intent, 0)
        val apps = mutableListOf<AppInfo>()

        for (ri in resolveInfos) {
            val packageName = ri.activityInfo.packageName
            val className = ri.activityInfo.name ?: ""
            val label = ri.loadLabel(pm)?.toString() ?: packageName
            val icon = loadIconSafe(ri, pm)

            apps.add(
                AppInfo(
                    packageName = packageName,
                    className = className,
                    label = label,
                    icon = icon
                )
            )
        }

        return apps.sortedBy { it.label.lowercase(Locale.getDefault()) }
    }

    private fun loadIconSafe(
        ri: android.content.pm.ResolveInfo,
        pm: PackageManager
    ): Drawable? {
        return try {
            ri.loadIcon(pm)
        } catch (e: Exception) {
            getDrawable(android.R.drawable.sym_def_app_icon)
        }
    }
}
