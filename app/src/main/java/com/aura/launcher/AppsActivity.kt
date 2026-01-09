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
    private var allApps: List<AppInfo> = emptyList()

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

        // Layout manager (grid)
        if (recyclerView.layoutManager == null) {
            recyclerView.layoutManager = GridLayoutManager(this, 4)
        }

        // Adapter: OOTAB List<AppInfo> + onClick
        adapter = AppsAdapter(emptyList()) { app ->
            launchApp(app)
        }
        recyclerView.adapter = adapter

        // Lae äpid
        allApps = loadInstalledApps()
        setAdapterList(allApps)

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

        // "More" nupp (kui sul on)
        moreButton?.setOnClickListener {
            // Pane siia oma menüü/tegevus hiljem
            Toast.makeText(this, "More", Toast.LENGTH_SHORT).show()
        }
    }

    private fun filterApps(query: String) {
        val q = query.trim().lowercase(Locale.getDefault())
        if (q.isEmpty()) {
            setAdapterList(allApps)
            return
        }

        val filtered = allApps.filter { app ->
            val label = safeLabel(app).lowercase(Locale.getDefault())
            val pkg = safePackage(app).lowercase(Locale.getDefault())
            label.contains(q) || pkg.contains(q)
        }
        setAdapterList(filtered)
    }

    /**
     * AppsAdapter võib olla tehtud kahel viisil:
     * 1) constructor(list, onClick) + adapter.update(list)
     * 2) constructor(list, onClick) + adapter.submitList(list)
     *
     * Proovin mõlemat (kompileerib, kui üks neist olemas).
     */
    private fun setAdapterList(list: List<AppInfo>) {
        try {
            // Kui sul on AppsAdapteris submitList(...)
            val m = adapter::class.java.methods.firstOrNull { it.name == "submitList" && it.parameterTypes.size == 1 }
            if (m != null) {
                m.invoke(adapter, list)
                return
            }
        } catch (_: Throwable) {}

        try {
            // Kui sul on AppsAdapteris updateApps(...) või updateData(...)
            val m = adapter::class.java.methods.firstOrNull {
                (it.name == "updateApps" || it.name == "updateData" || it.name == "setData") && it.parameterTypes.size == 1
            }
            if (m != null) {
                m.invoke(adapter, list)
                return
            }
        } catch (_: Throwable) {}

        // Viimane variant: tee uus adapter (kindel)
        adapter = AppsAdapter(list) { app -> launchApp(app) }
        recyclerView.adapter = adapter
    }

    private fun launchApp(app: AppInfo) {
        val pkg = safePackage(app)
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

            // AppInfo parameetrid olid sul varem "className" ja "label" -> panen mõlemad.
            // Kui AppInfo'l on rohkem/vähem välju, siis anna mulle AppInfo.kt sisu ja teen 100% täpseks.
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

    private fun tryLoadIcon(ri: android.content.pm.ResolveInfo, pm: PackageManager): Drawable {
        return try {
            ri.loadIcon(pm)
        } catch (_: Throwable) {
            getDrawable(android.R.drawable.sym_def_app_icon)!!
        }
    }

    // ---- SAFE getters (juhuks kui AppInfo väljade nimed erinevad) ----

    private fun safePackage(app: AppInfo): String {
        return try {
            app.packageName
        } catch (_: Throwable) {
            ""
        }
    }

    private fun safeLabel(app: AppInfo): String {
        return try {
            app.label
        } catch (_: Throwable) {
            safePackage(app)
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
