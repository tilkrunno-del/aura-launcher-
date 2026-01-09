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
    private lateinit var adapter: AppsAdapter

    private var searchEditText: EditText? = null
    private var clearButton: ImageButton? = null

    private var allApps: List<AppInfo> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_apps)

        recyclerView = findViewById(R.id.recyclerViewApps)
        searchEditText = findViewById(R.id.searchEditText)
        clearButton = findViewById(R.id.btnClearSearch)

        recyclerView.layoutManager = GridLayoutManager(this, 4)

        adapter = AppsAdapter(emptyList()) { app ->
            launchApp(app)
        }
        recyclerView.adapter = adapter

        allApps = loadInstalledApps()
        adapter.updateApps(allApps)

        searchEditText?.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                val q = s?.toString()?.lowercase(Locale.getDefault()) ?: ""
                val filtered = allApps.filter {
                    it.label.lowercase(Locale.getDefault()).contains(q) ||
                    it.packageName.lowercase(Locale.getDefault()).contains(q)
                }
                adapter.updateApps(filtered)
                clearButton?.visibility = if (q.isEmpty()) View.GONE else View.VISIBLE
            }
        })

        clearButton?.setOnClickListener {
            searchEditText?.setText("")
        }
    }

    private fun launchApp(app: AppInfo) {
        val intent = packageManager.getLaunchIntentForPackage(app.packageName)
        if (intent != null) {
            startActivity(intent)
        } else {
            Toast.makeText(this, "Cannot launch app", Toast.LENGTH_SHORT).show()
        }
    }

    private fun loadInstalledApps(): List<AppInfo> {
        val pm = packageManager
        val intent = Intent(Intent.ACTION_MAIN, null).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }

        return pm.queryIntentActivities(intent, 0)
            .map {
                AppInfo(
                    packageName = it.activityInfo.packageName,
                    className = it.activityInfo.name ?: "",
                    label = it.loadLabel(pm).toString(),
                    icon = it.loadIcon(pm)
                )
            }
            .sortedBy { it.label.lowercase(Locale.getDefault()) }
    }
}
