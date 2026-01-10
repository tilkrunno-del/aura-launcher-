package com.aura.launcher

import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView

class AppsActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var searchEditText: EditText
    private lateinit var clearButton: View

    private lateinit var adapter: AppsAdapter
    private var allApps: List<AppInfo> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_apps)

        // XML id-d sinu activity_apps.xml järgi
        recyclerView = findViewById(R.id.appsRecyclerView)
        searchEditText = findViewById(R.id.searchEditText)
        clearButton = findViewById(R.id.btnClearSearch)

        clearButton.visibility = View.GONE

        recyclerView.layoutManager = GridLayoutManager(this, 4)

        adapter = AppsAdapter(
            apps = emptyList(),
            onClick = { app -> launchApp(app) },
            onLongPress = { _, app -> showAppInfo(app) }
        )
        recyclerView.adapter = adapter

        // Lae appid
        allApps = loadInstalledApps()
        adapter.submitList(allApps)

        // Otsing: kasuta adapteri filterApps() (see sul olemas)
        searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) = Unit
            override fun afterTextChanged(s: Editable?) {
                val q = s?.toString().orEmpty()
                adapter.filterApps(q)
                clearButton.visibility = if (q.isBlank()) View.GONE else View.VISIBLE
            }
        })

        clearButton.setOnClickListener {
            searchEditText.setText("")
        }
    }

    private fun showAppInfo(app: AppInfo) {
        val msg = buildString {
            appendLine(app.label)
            appendLine(app.packageName)
            append(app.className)
        }
        Toast.makeText(this, msg, Toast.LENGTH_LONG).show()
    }

    private fun launchApp(app: AppInfo) {
        val intent = packageManager.getLaunchIntentForPackage(app.packageName)
        if (intent == null) {
            Toast.makeText(this, "Cannot launch: ${app.packageName}", Toast.LENGTH_SHORT).show()
            return
        }
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
    }

    private fun loadInstalledApps(): List<AppInfo> {
        val pm = packageManager

        val mainIntent = Intent(Intent.ACTION_MAIN, null).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }

        val resolved = pm.queryIntentActivities(mainIntent, 0)
        val list = ArrayList<AppInfo>(resolved.size)

        for (ri in resolved) {
            val pkg = ri.activityInfo.packageName ?: continue
            val cls = ri.activityInfo.name ?: ""
            val label = ri.loadLabel(pm)?.toString()?.trim().takeUnless { it.isNullOrBlank() } ?: pkg
            val icon = tryLoadIcon(ri, pm)

            list.add(
                AppInfo(
                    packageName = pkg,
                    className = cls,
                    label = label,
                    icon = icon
                )
            )
        }

        return list.sortedBy { it.label.lowercase() }
    }

    private fun tryLoadIcon(ri: android.content.pm.ResolveInfo, pm: PackageManager): Drawable? {
        return try {
            ri.loadIcon(pm)
        } catch (_: Throwable) {
            null
        }
    }
}
