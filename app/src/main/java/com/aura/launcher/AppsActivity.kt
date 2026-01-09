package com.aura.launcher

import android.content.ComponentName
import android.content.Intent
import android.content.pm.ResolveInfo
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.EditText
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.util.Locale

class AppsActivity : AppCompatActivity() {

    private lateinit var appsRecyclerView: RecyclerView
    private lateinit var searchInput: EditText
    private lateinit var btnClear: ImageButton

    // Täis list + filtreeritud list
    private val allApps = mutableListOf<AppEntry>()
    private val shownApps = mutableListOf<AppEntry>()

    private lateinit var adapter: AppsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_apps)

        appsRecyclerView = findViewById(R.id.appsRecyclerView)
        searchInput = findViewById(R.id.searchInput)
        btnClear = findViewById(R.id.btnClear)

        appsRecyclerView.layoutManager = GridLayoutManager(this, 4)

        adapter = AppsAdapter(shownApps) { entry ->
            launchApp(entry)
        }
        appsRecyclerView.adapter = adapter

        loadApps()

        btnClear.setOnClickListener { clearSearch() }

        searchInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filterApps(s?.toString().orEmpty())
            }
            override fun afterTextChanged(s: Editable?) = Unit
        })
    }

    private fun loadApps() {
        val intent = Intent(Intent.ACTION_MAIN, null).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }

        val results: List<ResolveInfo> = packageManager.queryIntentActivities(intent, 0)

        allApps.clear()
        for (ri in results) {
            val label = ri.loadLabel(packageManager)?.toString().orEmpty()
            val pkg = ri.activityInfo.packageName
            val cls = ri.activityInfo.name
            val icon = ri.loadIcon(packageManager)

            // SIIN on oluline: label ja className EI TOHI puudu olla
            allApps.add(
                AppEntry(
                    label = label,
                    packageName = pkg,
                    className = cls,
                    icon = icon
                )
            )
        }

        // sorteeri
        allApps.sortBy { it.label.lowercase(Locale.getDefault()) }

        // näita alguses kõik
        shownApps.clear()
        shownApps.addAll(allApps)
        adapter.notifyDataSetChanged()

        updateClearButton()
    }

    private fun filterApps(query: String) {
        val q = query.trim().lowercase(Locale.getDefault())

        shownApps.clear()
        if (q.isEmpty()) {
            shownApps.addAll(allApps)
        } else {
            shownApps.addAll(
                allApps.filter { it.label.lowercase(Locale.getDefault()).contains(q) }
            )
        }

        adapter.notifyDataSetChanged()
        updateClearButton()
    }

    private fun clearSearch() {
        searchInput.setText("")
        searchInput.clearFocus()
        filterApps("")
    }

    private fun updateClearButton() {
        btnClear.visibility = if (searchInput.text.isNullOrEmpty()) View.INVISIBLE else View.VISIBLE
    }

    private fun launchApp(entry: AppEntry) {
        try {
            val cn = ComponentName(entry.packageName, entry.className)
            val i = Intent(Intent.ACTION_MAIN).apply {
                addCategory(Intent.CATEGORY_LAUNCHER)
                component = cn
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            startActivity(i)
        } catch (_: Throwable) {
            // vaikne: kui mõni activity ei avane
        }
    }
}
