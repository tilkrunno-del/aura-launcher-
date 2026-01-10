package com.aura.launcher

import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Button
import android.widget.EditText
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

        // XML ID-d (need on sinu activity_apps.xml sees olemas)
        recyclerView = findViewById(R.id.appsRecyclerView)
        searchEditText = findViewById(R.id.searchEditText)
        clearButton = findViewById(R.id.btnClearSearch)

        // Grid (muuda 4 -> 5 kui tahad tihedam)
        recyclerView.layoutManager = GridLayoutManager(this, 4)

        // Adapter nõuab: (apps, onClick, onLongPress, isFavorite, isHidden)
        adapter = AppsAdapter(
            apps = emptyList(),
            onClick = { app -> launchApp(app) },
            onLongPress = { _, app ->
                Toast.makeText(this, app.label, Toast.LENGTH_SHORT).show()
            },
            isFavorite = { it.isFavorite },
            isHidden = { it.isHidden }
        )
        recyclerView.adapter = adapter

        // Lae äpid ja kuva
        allApps = loadInstalledApps()
        adapter.submitList(allApps)

        // Clear nupp alguses peitu
        clearButton.visibility = View.GONE

        // Otsing (kasutame adapter.filterApps())
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

    private fun launchApp(app: AppInfo) {
        val pkg = app.packageName
        if (pkg.isBlank()) {
            Toast.makeText(this, "Package missing", Toast.LENGTH_SHORT).show()
            return
        }

        val intent = packageManager.getLaunchIntentForPackage(pkg)
        if (intent == null) {
            Toast.makeText(this, "Cannot launch: $pkg", Toast.LENGTH_SHORT).show()
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
            val label = ri.loadLabel(pm)?.toString() ?: pkg
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

        // Sort A-Z
        return list.sortedBy { it.label.lowercase(Locale.getDefault()) }
    }

    private fun tryLoadIcon(ri: android.content.pm.ResolveInfo, pm: PackageManager): Drawable? {
        return try {
            ri.loadIcon(pm)
        } catch (_: Throwable) {
            getDrawable(android.R.drawable.sym_def_app_icon)
        }
    }
}
