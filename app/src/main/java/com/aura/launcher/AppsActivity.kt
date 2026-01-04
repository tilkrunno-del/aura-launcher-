package com.aura.launcher

import android.content.Intent
import android.os.Bundle
import android.view.KeyEvent
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.core.widget.doAfterTextChanged

class AppsActivity : AppCompatActivity() {

    private lateinit var recycler: RecyclerView
    private lateinit var search: EditText
    private lateinit var adapter: AppsAdapter

    private var allApps: List<AppInfo> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_apps)

        recycler = findViewById(R.id.recyclerApps)
        search = findViewById(R.id.editSearch)

        recycler.layoutManager = LinearLayoutManager(this)

        adapter = AppsAdapter { app ->
            launchApp(app.packageName)
        }
        recycler.adapter = adapter

        allApps = loadLaunchableApps()
        adapter.submitList(allApps)

        // ✅ AUTOFOKUS + KLAVIATUUR
        search.requestFocus()
        search.post {
            val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
            imm.showSoftInput(search, InputMethodManager.SHOW_IMPLICIT)
        }

        // ✅ LIVE FILTER
        search.doAfterTextChanged { text ->
            val q = text?.toString()?.trim()?.lowercase().orEmpty()
            val filtered = if (q.isEmpty()) {
                allApps
            } else {
                allApps.filter { app ->
                    app.label.lowercase().contains(q) ||
                            app.packageName.lowercase().contains(q)
                }
            }
            adapter.submitList(filtered)
        }

        // ✅ ENTER / SEARCH -> AVA ESIMENE TULEMUS
        search.setOnEditorActionListener { _, actionId, event ->
            val isEnter =
                actionId == EditorInfo.IME_ACTION_SEARCH ||
                actionId == EditorInfo.IME_ACTION_DONE ||
                (event?.keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_DOWN)

            if (isEnter) {
                val first = adapter.getFirstOrNull()
                if (first != null) launchApp(first.packageName)
                true
            } else {
                false
            }
        }
    }

    private fun launchApp(packageName: String) {
        val intent = packageManager.getLaunchIntentForPackage(packageName)
        if (intent != null) {
            startActivity(intent)
        }
    }

    private fun loadLaunchableApps(): List<AppInfo> {
        val pm = packageManager
        val intent = Intent(Intent.ACTION_MAIN, null).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }
        val resolved = pm.queryIntentActivities(intent, 0)

        val apps = resolved.map { ri ->
            val label = ri.loadLabel(pm).toString()
            val pkg = ri.activityInfo.packageName
            val icon = ri.loadIcon(pm)
            AppInfo(label = label, packageName = pkg, icon = icon)
        }

        return apps.sortedBy { it.label.lowercase() }
    }
}
