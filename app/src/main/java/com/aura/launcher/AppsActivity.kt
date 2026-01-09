package com.aura.launcher

import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView

class AppsActivity : AppCompatActivity() {

    private lateinit var appsRecyclerView: RecyclerView
    private lateinit var searchEditText: EditText
    private lateinit var btnClearSearch: Button

    private val allApps = mutableListOf<AppEntry>()
    private val shownApps = mutableListOf<AppEntry>()

    private lateinit var adapter: AppsGridAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_apps)

        appsRecyclerView = findViewById(R.id.appsRecyclerView)
        searchEditText = findViewById(R.id.searchEditText)
        btnClearSearch = findViewById(R.id.btnClearSearch)

        adapter = AppsGridAdapter(
            items = shownApps,
            onClick = { app -> launchApp(app.packageName) }
        )

        val spanCount = 4 // muuda kui tahad (3/4/5)
        appsRecyclerView.layoutManager = GridLayoutManager(this, spanCount)
        appsRecyclerView.adapter = adapter

        // Kui sul on GridSpacingItemDecoration.kt olemas, võid selle sisse lülitada:
        // appsRecyclerView.addItemDecoration(GridSpacingItemDecoration(spanCount, 16, true))

        loadApps()
        setupSearch()
        setupClear()
    }

    private fun loadApps() {
        allApps.clear()

        val pm: PackageManager = packageManager
        val intent = Intent(Intent.ACTION_MAIN, null).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }

        val resolved = pm.queryIntentActivities(intent, 0)

        for (ri in resolved) {
            val pkg = ri.activityInfo.packageName ?: continue
            val label = ri.loadLabel(pm)?.toString() ?: pkg
            val icon = ri.loadIcon(pm)

            allApps.add(
                AppEntry(
                    packageName = pkg,
                    label = label,
                    icon = icon
                )
            )
        }

        allApps.sortBy { it.label.lowercase() }

        shownApps.clear()
        shownApps.addAll(allApps)
        adapter.notifyDataSetChanged()
    }

    private fun setupSearch() {
        searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun afterTextChanged(s: Editable?) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filterApps(s?.toString().orEmpty())
            }
        })
    }

    private fun setupClear() {
        btnClearSearch.setOnClickListener {
            searchEditText.setText("")
            filterApps("")
        }
    }

    private fun filterApps(query: String) {
        val q = query.trim().lowercase()

        shownApps.clear()
        if (q.isEmpty()) {
            shownApps.addAll(allApps)
        } else {
            shownApps.addAll(
                allApps.filter { it.label.lowercase().contains(q) || it.packageName.lowercase().contains(q) }
            )
        }

        adapter.notifyDataSetChanged()
    }

    private fun launchApp(packageName: String) {
        val intent = packageManager.getLaunchIntentForPackage(packageName)
        if (intent != null) {
            startActivity(intent)
        } else {
            Toast.makeText(this, "Ei saa avada: $packageName", Toast.LENGTH_SHORT).show()
        }
    }

    // --- Andmemudel ---
    data class AppEntry(
        val packageName: String,
        val label: String,
        val icon: Drawable
    )

    // --- RecyclerView adapter (iseseisev, et build kindlasti töötaks) ---
    private class AppsGridAdapter(
        private val items: List<AppEntry>,
        private val onClick: (AppEntry) -> Unit
    ) : RecyclerView.Adapter<AppsGridAdapter.VH>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_app, parent, false)
            return VH(view)
        }

        override fun onBindViewHolder(holder: VH, position: Int) {
            val app = items[position]
            holder.bind(app, onClick)
        }

        override fun getItemCount(): Int = items.size

        class VH(itemView: View) : RecyclerView.ViewHolder(itemView) {
            private val icon: ImageView = itemView.findViewById(R.id.appIcon)
            private val name: TextView = itemView.findViewById(R.id.appName)

            fun bind(app: AppEntry, onClick: (AppEntry) -> Unit) {
                icon.setImageDrawable(app.icon)
                name.text = app.label
                itemView.setOnClickListener { onClick(app) }
            }
        }
    }
}
