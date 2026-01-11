package com.aura.launcher

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.aura.launcher.databinding.ActivityAppsBinding

class AppsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAppsBinding
    private lateinit var adapter: AppsAdapter

    private val allApps = mutableListOf<AppInfo>()
    private val filteredApps = mutableListOf<AppInfo>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityAppsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRecycler()
        loadApps()
        setupSearch()
    }

    private fun setupRecycler() {
        adapter = AppsAdapter(
            apps = filteredApps,
            onOpen = { app ->
                LauncherUtils.launchApp(this, app)
            },
            onLongClick = { app ->
                AppActionsBottomSheet(this, app).show()
            }
        )

        binding.appsRecyclerView.layoutManager =
            GridLayoutManager(this, 4, RecyclerView.VERTICAL, false)

        binding.appsRecyclerView.adapter = adapter
    }

    private fun loadApps() {
        val pm = packageManager
        val intent = pm.getLaunchIntentForPackage(packageName)

        val launchIntent = android.content.Intent(android.content.Intent.ACTION_MAIN).apply {
            addCategory(android.content.Intent.CATEGORY_LAUNCHER)
        }

        val resolveInfos = pm.queryIntentActivities(launchIntent, 0)

        allApps.clear()

        for (info in resolveInfos) {
            val label = info.loadLabel(pm).toString()
            val packageName = info.activityInfo.packageName
            val className = info.activityInfo.name
            val icon = info.loadIcon(pm)

            allApps.add(
                AppInfo(
                    label = label,
                    packageName = packageName,
                    className = className,
                    icon = icon
                )
            )
        }

        allApps.sortBy { it.label.lowercase() }

        filteredApps.clear()
        filteredApps.addAll(allApps)
        adapter.notifyDataSetChanged()
    }

    private fun setupSearch() {
        binding.searchEditText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                filterApps(s?.toString().orEmpty())
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        binding.btnClearSearch.setOnClickListener {
            binding.searchEditText.text.clear()
        }
    }

    private fun filterApps(query: String) {
        filteredApps.clear()

        if (query.isBlank()) {
            filteredApps.addAll(allApps)
            binding.btnClearSearch.visibility = android.view.View.GONE
        } else {
            val q = query.lowercase()
            filteredApps.addAll(
                allApps.filter { it.label.lowercase().contains(q) }
            )
            binding.btnClearSearch.visibility = android.view.View.VISIBLE
        }

        adapter.notifyDataSetChanged()
    }
}
