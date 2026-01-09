package com.aura.launcher

import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.util.Locale

class AppsActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private var searchEditText: EditText? = null
    private var clearButton: ImageButton? = null
    private var moreButton: ImageButton? = null

    private lateinit var adapter: AppsAdapter
    private var allApps: List<AppEntry> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_apps)

        recyclerView = findRequiredView(
            "recyclerViewApps",
            "appsRecyclerView",
            "recyclerApps",
            "rvApps"
        )

        searchEditText = findOptionalView(
            "searchEditText",
            "editSearch",
            "etSearch",
            "searchInput"
        )

        clearButton = findOptionalView(
            "btnClearSearch",
            "clearSearch",
            "btnClear",
            "ivClearSearch"
        )

        moreButton = findOptionalView(
            "btnMore",
            "moreButton",
            "ivMore"
        )

        if (recyclerView.layoutManager == null) {
            recyclerView.layoutManager = GridLayoutManager(this, 4)
        }

        // ✅ AppsAdapter: list + onClick (vältib "No value passed for parameter 'onClick'")
        adapter = AppsAdapter(emptyList()) { app ->
            launchApp(app)
        }
        recyclerView.adapter = adapter

        // Lae äpid
        allApps = loadInstalledApps()
        adapter = AppsAdapter(allApps) { app -> launchApp(app) }
        recyclerView.adapter = adapter

        // Otsing
        searchEditText?.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) = Unit

            override fun afterTextChanged(s: Editable?) {
                val q = s?.toString().orEmpty()
                filterApps(q)
                clearButton?.visibility = if (q.isBlank()) View.GONE else View.VISIBLE
            }
        })

        clearButton?.setOnClickListener {
            searchEditText?.setText("")
        }

        moreButton?.setOnClickListener {
            Toast.makeText(this, "More", Toast.LENGTH_SHORT).show()
        }
    }

    private fun filterApps(query: String) {
        val q = query.trim().lowercase(Locale.getDefault())

        if (q.isEmpty()) {
            adapter = AppsAdapter(allApps) { app -> launchApp(app) }
            recyclerView.adapter = adapter
            return
        }

        val filtered = allApps.filter { app ->
            val label = app.label.lowercase(Locale.getDefault())
            val pkg = app.packageName.lowercase(Locale.getDefault())
            label.contains(q) || pkg.contains(q) // ✅ Boolean (vältib "Unit but Boolean expected")
        }

        adapter = AppsAdapter(filtered) { app -> launchApp(app) }
        recyclerView.adapter = adapter
    }

    private fun launchApp(app: AppEntry) {
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

    private fun loadInstalledApps(): List<AppEntry> {
        val pm = packageManager
        val mainIntent = Intent(Intent.ACTION_MAIN, null).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }

        val resolved = pm.queryIntentActivities(mainIntent, 0)
        val list = ArrayList<AppEntry>(resolved.size)

        for (ri in resolved) {
            val pkg = ri.activityInfo.packageName ?: continue
            val cls = ri.activityInfo.name ?: ""
            val label = ri.loadLabel(pm)?.toString() ?: pkg
            val icon = tryLoadIcon(ri, pm)

            list.add(
                AppEntry(
                    packageName = pkg,
                    className = cls,
                    label = label,
                    icon = icon
                )
            )
        }

        return list.sortedBy { it.label.lowercase(Locale.getDefault()) }
    }

    private fun tryLoadIcon(ri: android.content.pm.ResolveInfo, pm: PackageManager): Drawable {
        return try {
            ri.loadIcon(pm)
        } catch (_: Throwable) {
            getDrawable(android.R.drawable.sym_def_app_icon)!!
        }
    }

    // ---- View helpers ----

    private inline fun <reified T : View> findOptionalView(vararg idNames: String): T? {
        for (name in idNames) {
            val id = resources.getIdentifier(name, "id", packageName)
            if (id != 0) {
                val v = findViewById<View>(id)
                if (v is T) return v
            }
        }
        return null
    }

    private inline fun <reified T : View> findRequiredView(vararg idNames: String): T {
        return findOptionalView<T>(*idNames)
            ?: error("Missing required view. Tried ids: ${idNames.joinToString()}")
    }
}
