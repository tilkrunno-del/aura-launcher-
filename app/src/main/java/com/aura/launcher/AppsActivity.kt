package com.aura.launcher

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.util.Locale

class AppsActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_QUERY = "EXTRA_QUERY"

        private const val PREFS = "aura_launcher_prefs"
        private const val KEY_HIDDEN_SET = "hidden_apps"
        private const val KEY_FAVORITES_SET = "favorite_apps"
        private const val HIDDEN_TRIGGER = "***"
    }

    private lateinit var recyclerView: RecyclerView
    private lateinit var favoritesRecyclerView: RecyclerView
    private lateinit var favoritesTitle: TextView
    private lateinit var emptyText: TextView
    private lateinit var searchEditText: EditText

    private lateinit var appsAdapter: AppsAdapter
    private lateinit var favoritesAdapter: FavoritesAdapter

    private val allApps = mutableListOf<AppInfo>()
    private val visibleApps = mutableListOf<AppInfo>()
    private val hiddenApps = mutableListOf<AppInfo>()
    private val favoriteApps = mutableListOf<AppInfo>()

    private var showingHidden = false

    override fun onCreate(savedInstanceState: Bundle?) {
        ThemePrefs.applyNightMode(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_apps)

        recyclerView = findViewById(R.id.appsRecyclerView)
        favoritesRecyclerView = findViewById(R.id.favoritesRecyclerView)
        favoritesTitle = findViewById(R.id.favoritesTitle)
        emptyText = findViewById(R.id.emptyText)
        searchEditText = findViewById(R.id.searchEditText)

        val spanCount = ThemePrefs.getSpanCount(this)
        recyclerView.layoutManager = GridLayoutManager(this, spanCount)

        favoritesRecyclerView.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)

        appsAdapter = AppsAdapter(
            onAppClick = { app -> launchApp(app) },
            onMenuAction = { app, action -> handleMenuAction(app, action) },
            spanCount = spanCount,
            animEnabled = ThemePrefs.isAnimEnabled(this)
        )
        recyclerView.adapter = appsAdapter

        favoritesAdapter = FavoritesAdapter(
            onClick = { app -> launchApp(app) },
            onLongClick = { app ->
                // long press lemmikus -> eemalda lemmikutest
                toggleFavorite(app)
            }
        )
        favoritesRecyclerView.adapter = favoritesAdapter

        allApps.clear()
        allApps.addAll(loadInstalledApps(packageManager))

        rebuildListsFromPrefs()
        showFavoritesRow()

        // Search + hidden trigger
        searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun afterTextChanged(s: Editable?) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val q = s?.toString().orEmpty().trim()

                if (q == HIDDEN_TRIGGER) {
                    if (!showingHidden) {
                        showingHidden = true
                        appsAdapter.submitApps(hiddenApps)
                        appsAdapter.filterApps("")
                        Toast.makeText(this@AppsActivity, "Peidetud rakendused", Toast.LENGTH_SHORT).show()
                    }
                    updateEmptyState()
                    return
                }

                if (showingHidden) {
                    showingHidden = false
                    appsAdapter.submitApps(visibleApps)
                }

                appsAdapter.filterApps(q)
                recyclerView.scrollToPosition(0)
                updateEmptyState()
            }
        })

        val initialQuery = intent.getStringExtra(EXTRA_QUERY)
        if (!initialQuery.isNullOrBlank()) {
            searchEditText.setText(initialQuery)
            searchEditText.setSelection(initialQuery.length)
        } else {
            appsAdapter.submitApps(visibleApps)
            appsAdapter.filterApps("")
            updateEmptyState()
        }
    }

    // ---------- MENU ACTIONS ----------
    private fun handleMenuAction(app: AppInfo, action: AppsAdapter.MenuAction) {
        when (action) {
            AppsAdapter.MenuAction.TOGGLE_FAVORITE -> toggleFavorite(app)
            AppsAdapter.MenuAction.TOGGLE_HIDDEN -> toggleHidden(app)
            AppsAdapter.MenuAction.APP_INFO -> openAppInfo(app.packageName)
            AppsAdapter.MenuAction.UNINSTALL -> requestUninstall(app.packageName)
        }
    }

    // ---------- FAVORITES ----------
    private fun toggleFavorite(app: AppInfo) {
        val prefs = getSharedPreferences(PREFS, MODE_PRIVATE)
        val set = prefs.getStringSet(KEY_FAVORITES_SET, emptySet())?.toMutableSet() ?: mutableSetOf()

        val key = appKey(app)
        val nowFav: Boolean

        if (set.contains(key)) {
            set.remove(key)
            nowFav = false
        } else {
            set.add(key)
            nowFav = true
        }

        prefs.edit().putStringSet(KEY_FAVORITES_SET, set).apply()
        rebuildListsFromPrefs()
        showFavoritesRow()

        Toast.makeText(this, if (nowFav) "Lisatud lemmikutesse" else "Eemaldatud lemmikutest", Toast.LENGTH_SHORT).show()
    }

    private fun showFavoritesRow() {
        if (favoriteApps.isEmpty()) {
            favoritesTitle.visibility = android.view.View.GONE
            favoritesRecyclerView.visibility = android.view.View.GONE
        } else {
            favoritesTitle.visibility = android.view.View.VISIBLE
            favoritesRecyclerView.visibility = android.view.View.VISIBLE
            favoritesAdapter.submit(favoriteApps)
        }
    }

    // ---------- HIDDEN ----------
    private fun toggleHidden(app: AppInfo) {
        val prefs = getSharedPreferences(PREFS, MODE_PRIVATE)
        val set = prefs.getStringSet(KEY_HIDDEN_SET, emptySet())?.toMutableSet() ?: mutableSetOf()

        val key = appKey(app)
        val hiddenNow: Boolean

        if (set.contains(key)) {
            set.remove(key)
            hiddenNow = false
        } else {
            set.add(key)
            hiddenNow = true
        }

        prefs.edit().putStringSet(KEY_HIDDEN_SET, set).apply()

        rebuildListsFromPrefs()
        showFavoritesRow()

        if (showingHidden) {
            appsAdapter.submitApps(hiddenApps)
            appsAdapter.filterApps("")
        } else {
            appsAdapter.submitApps(visibleApps)
            appsAdapter.filterApps(searchEditText.text?.toString().orEmpty().trim())
        }
        updateEmptyState()

        Toast.makeText(this, if (hiddenNow) "Peidetud: ${app.label}" else "Tagasi toodud: ${app.label}", Toast.LENGTH_SHORT).show()
    }

    private fun rebuildListsFromPrefs() {
        val prefs = getSharedPreferences(PREFS, MODE_PRIVATE)
        val hiddenSet = prefs.getStringSet(KEY_HIDDEN_SET, emptySet()) ?: emptySet()
        val favSet = prefs.getStringSet(KEY_FAVORITES_SET, emptySet()) ?: emptySet()

        visibleApps.clear()
        hiddenApps.clear()
        favoriteApps.clear()

        for (app in allApps) {
            val key = appKey(app)

            if (favSet.contains(key)) {
                favoriteApps.add(app)
            }

            if (hiddenSet.contains(key)) hiddenApps.add(app) else visibleApps.add(app)
        }

        favoriteApps.sortBy { it.label.lowercase(Locale.getDefault()) }
        visibleApps.sortBy { it.label.lowercase(Locale.getDefault()) }
        hiddenApps.sortBy { it.label.lowercase(Locale.getDefault()) }

        if (!showingHidden) appsAdapter.submitApps(visibleApps) else appsAdapter.submitApps(hiddenApps)
    }

    // ---------- APP ACTIONS ----------
    private fun launchApp(app: AppInfo) {
        val pm = packageManager
        val primary = pm.getLaunchIntentForPackage(app.packageName)?.apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        val fallback = Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
            setClassName(app.packageName, app.className)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        try {
            startActivity(primary ?: fallback)
        } catch (_: Exception) {
            Toast.makeText(this, "Ei saa avada: ${app.label}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun openAppInfo(packageName: String) {
        val intent = Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = android.net.Uri.parse("package:$packageName")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        startActivity(intent)
    }

    private fun requestUninstall(packageName: String) {
        val intent = Intent(Intent.ACTION_DELETE).apply {
            data = android.net.Uri.parse("package:$packageName")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        startActivity(intent)
    }

    // ---------- EMPTY STATE ----------
    private fun updateEmptyState() {
        val isEmpty = appsAdapter.itemCount == 0
        emptyText.visibility = if (isEmpty) android.view.View.VISIBLE else android.view.View.GONE
        recyclerView.visibility = if (isEmpty) android.view.View.GONE else android.view.View.VISIBLE
    }

    private fun appKey(app: AppInfo): String = "${app.packageName}/${app.className}"

    private fun loadInstalledApps(pm: PackageManager): List<AppInfo> {
        val intent = Intent(Intent.ACTION_MAIN, null).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }
        val resolved = pm.queryIntentActivities(intent, 0)

        return resolved.map { ri ->
            AppInfo(
                label = ri.loadLabel(pm).toString(),
                packageName = ri.activityInfo.packageName,
                className = ri.activityInfo.name,
                icon = ri.loadIcon(pm)
            )
        }
    }
}
