package com.aura.launcher

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import android.widget.PopupMenu
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView

class AppsActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_QUERY = "extra_query"
    }

    private lateinit var adapter: AppsAdapter
    private lateinit var searchInput: EditText

    private var allApps: List<AppInfo> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_apps)

        searchInput = findViewById(R.id.searchEditText)
        val recyclerView: RecyclerView = findViewById(R.id.appsRecyclerView)

        recyclerView.layoutManager = GridLayoutManager(this, 3)

        allApps = loadApps()

        adapter = AppsAdapter(
            apps = allApps,
            onClick = { app ->
                launchApp(app)
            },
            onLongPress = { view, app ->
                showAppMenu(view, app)
            },
            isFavorite = { app ->
                AppPrefs.isFavorite(this, makeKey(app))
            },
            isHidden = { app ->
                AppPrefs.isHidden(this, makeKey(app))
            }
        )

        recyclerView.adapter = adapter

        // Kui MainActivity saatis query kaasa
        val initial = intent.getStringExtra(EXTRA_QUERY).orEmpty()
        if (initial.isNotBlank()) {
            searchInput.setText(initial)
            searchInput.setSelection(initial.length)
            adapter.filterApps(initial)
        }

        // ✅ Kindel TextWatcher (ei vaja KTX-i)
        searchInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun afterTextChanged(s: Editable?) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                adapter.filterApps(s?.toString().orEmpty())
            }
        })
    }

    private fun showAppMenu(anchor: android.view.View, app: AppInfo) {
        val key = makeKey(app)
        val isFav = AppPrefs.isFavorite(this, key)
        val isHid = AppPrefs.isHidden(this, key)

        val popup = PopupMenu(this, anchor)

        popup.menu.add(0, 1, 0, if (isFav) "Eemalda lemmik" else "Lisa lemmik")
        popup.menu.add(0, 2, 1, if (isHid) "Too tagasi" else "Peida")
        popup.menu.add(0, 3, 2, "App info")
        popup.menu.add(0, 4, 3, "Uninstall")

        popup.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                1 -> {
                    AppPrefs.toggleFavorite(this, key)
                    adapter.notifyDataSetChanged()
                    true
                }
                2 -> {
                    AppPrefs.toggleHidden(this, key)
                    // Hidden -> eemalda listist kohe
                    allApps = loadApps()
                    adapter.submitList(allApps)
                    adapter.filterApps(searchInput.text?.toString().orEmpty())
                    true
                }
                3 -> {
                    openAppInfo(app.packageName)
                    true
                }
                4 -> {
                    uninstall(app.packageName)
                    true
                }
                else -> false
            }
        }

        popup.show()
    }

    private fun loadApps(): List<AppInfo> {
        val pm = packageManager
        val intent = Intent(Intent.ACTION_MAIN, null).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }

        val hiddenSet = AppPrefs.getHidden(this)

        return pm.queryIntentActivities(intent, 0)
            .map { ri ->
                AppInfo(
                    label = ri.loadLabel(pm).toString(),
                    packageName = ri.activityInfo.packageName,
                    className = ri.activityInfo.name, // ✅ oluline
                    icon = ri.loadIcon(pm)
                )
            }
            // ✅ ära näita peidetuid
            .filter { !hiddenSet.contains(makeKey(it)) }
            .sortedBy { it.label.lowercase() }
    }

    private fun launchApp(app: AppInfo) {
        val launchIntent = Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
            setClassName(app.packageName, app.className)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        try {
            startActivity(launchIntent)
        } catch (_: Exception) {}
    }

    private fun openAppInfo(packageName: String) {
        val intent = Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = android.net.Uri.parse("package:$packageName")
        }
        startActivity(intent)
    }

    private fun uninstall(packageName: String) {
        val intent = Intent(Intent.ACTION_DELETE).apply {
            data = android.net.Uri.parse("package:$packageName")
        }
        startActivity(intent)
    }

    private fun makeKey(app: AppInfo) = "${app.packageName}/${app.className}"
}
