package com.aura.launcher

import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.text.Editable
import android.text.TextWatcher
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView

class AppsActivity : AppCompatActivity() {

    private lateinit var appsRecyclerView: RecyclerView
    private lateinit var searchEditText: EditText
    private lateinit var btnClearSearch: ImageButton

    private lateinit var adapter: AppsAdapter

    private val allApps = mutableListOf<AppInfo>()
    private val filteredApps = mutableListOf<AppInfo>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_apps)

        appsRecyclerView = findViewById(R.id.appsRecyclerView)
        searchEditText = findViewById(R.id.searchEditText)
        btnClearSearch = findViewById(R.id.btnClearSearch)

        // Grid columns (hiljem saad siia panna prefs-ist väärtuse)
        val spanCount = 4
        appsRecyclerView.layoutManager = GridLayoutManager(this, spanCount)

        adapter = AppsAdapter(
            apps = filteredApps,
            onClick = { app -> launchApp(app) },
            onLongClick = { app ->
                openAppActions(app)
                true
            }
        )
        appsRecyclerView.adapter = adapter

        loadApps()
        setupSearch()
        setupClearButton()
    }

    private fun setupClearButton() {
        btnClearSearch.setOnClickListener {
            searchEditText.setText("")
        }
    }

    private fun setupSearch() {
        // Klaviatuuri "Search" vajutus
        searchEditText.setOnEditorActionListener { _, actionId, event ->
            val isSearch =
                actionId == EditorInfo.IME_ACTION_SEARCH ||
                    (event?.keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_DOWN)

            if (isSearch) {
                // lihtsalt sulgeb klaviatuuri (kui sul on util, võid siia panna hideKeyboard())
                searchEditText.clearFocus()
                true
            } else {
                false
            }
        }

        searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                val q = s?.toString()?.trim()?.lowercase().orEmpty()

                btnClearSearch.visibility = if (q.isEmpty()) View.GONE else View.VISIBLE

                filteredApps.clear()
                if (q.isEmpty()) {
                    filteredApps.addAll(allApps)
                } else {
                    filteredApps.addAll(
                        allApps.filter {
                            it.label.lowercase().contains(q) || it.packageName.lowercase().contains(q)
                        }
                    )
                }

                adapter.notifyDataSetChanged()
            }
        })
    }

    private fun loadApps() {
        allApps.clear()

        val pm = packageManager
        val intent = Intent(Intent.ACTION_MAIN, null).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }

        val activities = pm.queryIntentActivities(intent, PackageManager.MATCH_ALL)
            .sortedWith(compareBy({ it.loadLabel(pm).toString().lowercase() }))

        for (ri in activities) {
            val pkg = ri.activityInfo.packageName
            val cls = ri.activityInfo.name
            val label = ri.loadLabel(pm)?.toString() ?: pkg
            val icon = ri.loadIcon(pm)

            // NB! AppInfo peab olema: (packageName, className, label, icon)
            allApps.add(
                AppInfo(
                    packageName = pkg,
                    className = cls,
                    label = label,
                    icon = icon
                )
            )
        }

        filteredApps.clear()
        filteredApps.addAll(allApps)
        adapter.notifyDataSetChanged()
    }

    private fun launchApp(app: AppInfo) {
        try {
            val launchIntent = Intent().apply {
                setClassName(app.packageName, app.className)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            startActivity(launchIntent)
        } catch (_: Exception) {
            // fallback: package default launcher
            try {
                val fallback = packageManager.getLaunchIntentForPackage(app.packageName)
                if (fallback != null) startActivity(fallback)
            } catch (_: Exception) {
            }
        }
    }

    private fun openAppActions(app: AppInfo) {
        // Ajutine: avab "App info" (stabiilne ja ei vaja bottomsheeti)
        openAppInfo(app.packageName)
    }

    private fun openAppInfo(packageName: String) {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.parse("package:$packageName")
        }
        startActivity(intent)
    }
}
