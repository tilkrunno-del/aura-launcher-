package com.aura.launcher

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.core.widget.addTextChangedListener

class AppsActivity : AppCompatActivity() {

    private lateinit var recycler: RecyclerView
    private lateinit var adapter: AppsAdapter
    private lateinit var editSearch: EditText
    private lateinit var btnClear: ImageButton

    private var allApps: List<AppInfo> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_apps)

        recycler = findViewById(R.id.recyclerApps)
        editSearch = findViewById(R.id.editSearch)
        btnClear = findViewById(R.id.btnClearSearch)

        recycler.layoutManager = LinearLayoutManager(this)

        allApps = loadLaunchableApps()

        adapter = AppsAdapter(allApps) { app ->
            launchApp(app.packageName)
        }
        recycler.adapter = adapter

        // Otsingu muutumisel: filtreeri + X nähtavus + scroll üles
        editSearch.addTextChangedListener { text ->
            val q = text?.toString()?.trim()?.lowercase() ?: ""
            btnClear.isVisible = q.isNotEmpty()

            val filtered = if (q.isEmpty()) {
                allApps
            } else {
                allApps.filter { app ->
                    app.label.lowercase().contains(q) || app.packageName.lowercase().contains(q)
                }
            }

            adapter.updateApps(filtered)
            recycler.scrollToPosition(0)
        }

        // X (clear) nupp
        btnClear.setOnClickListener {
            editSearch.setText("")
            editSearch.clearFocus()
            hideKeyboard()
            recycler.scrollToPosition(0)
        }
    }

    private fun loadLaunchableApps(): List<AppInfo> {
        val pm = packageManager
        val intent = Intent(Intent.ACTION_MAIN, null).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }

        val resolved = pm.queryIntentActivities(intent, 0)

        return resolved.map { ri ->
            val label = ri.loadLabel(pm)?.toString() ?: ri.activityInfo.packageName
            val packageName = ri.activityInfo.packageName
            val icon = ri.loadIcon(pm)
            AppInfo(label = label, packageName = packageName, icon = icon)
        }.sortedBy { it.label.lowercase() }
    }

    private fun launchApp(packageName: String) {
        val launchIntent = packageManager.getLaunchIntentForPackage(packageName)
        if (launchIntent != null) {
            startActivity(launchIntent)
        }
    }

    private fun hideKeyboard() {
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(editSearch.windowToken, 0)
    }
}
