package com.aura.llauncher
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView

class AppsActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var searchInput: EditText
    private lateinit var searchIcon: ImageView
    private lateinit var emptyText: TextView

    private lateinit var adapter: AppsAdapter

    private val allApps = mutableListOf<AppInfo>()
    private val filteredApps = mutableListOf<AppInfo>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_apps)

        recyclerView = findViewById(R.id.recyclerApps)
        searchInput = findViewById(R.id.searchInput)
        searchIcon = findViewById(R.id.searchIcon)
        emptyText = findViewById(R.id.emptyText)

        // Grid columns: kui sul on oma prefs util, saad siia pärast asendada.
        val spanCount = 4
        recyclerView.layoutManager = GridLayoutManager(this, spanCount)

        adapter = AppsAdapter(
            apps = filteredApps,
            onClick = { app -> launchApp(app) },
            onLongClick = { app -> openAppActions(app); true }
        )
        recyclerView.adapter = adapter

        loadApps()
        applySearch()

        searchIcon.setOnClickListener {
            // mugav: vajutus paneb kursori otsingusse
            searchInput.requestFocus()
        }
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

            // OLULINE: AppInfo peab olema (packageName, className, label, icon)
            allApps.add(
                AppInfo(
                    packageName = pkg,
                    className = cls,
                    label = label,
                    icon = icon
                )
            )
        }

        // Esialgne list
        filteredApps.clear()
        filteredApps.addAll(allApps)
        adapter.notifyDataSetChanged()

        updateEmptyState()
    }

    private fun applySearch() {
        searchInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                val q = s?.toString()?.trim()?.lowercase().orEmpty()

                filteredApps.clear()
                if (q.isEmpty()) {
                    filteredApps.addAll(allApps)
                } else {
                    filteredApps.addAll(
                        allApps.filter { it.label.lowercase().contains(q) || it.packageName.lowercase().contains(q) }
                    )
                }

                adapter.notifyDataSetChanged()
                updateEmptyState()
            }
        })
    }

    private fun updateEmptyState() {
        val isEmpty = filteredApps.isEmpty()
        emptyText.visibility = if (isEmpty) View.VISIBLE else View.GONE
        recyclerView.visibility = if (isEmpty) View.GONE else View.VISIBLE
    }

    private fun launchApp(app: AppInfo) {
        try {
            val launchIntent = Intent().apply {
                setClassName(app.packageName, app.className)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            startActivity(launchIntent)
        } catch (e: Exception) {
            // fallback: package launch intent
            try {
                val fallback = packageManager.getLaunchIntentForPackage(app.packageName)
                if (fallback != null) {
                    startActivity(fallback)
