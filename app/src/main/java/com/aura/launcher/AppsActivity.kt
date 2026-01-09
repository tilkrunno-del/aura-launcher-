package com.aura.launcher

import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.widget.EditText
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.PopupMenu
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class AppsActivity : AppCompatActivity() {

    private lateinit var appsRecyclerView: RecyclerView
    private lateinit var favoritesRecyclerView: RecyclerView
    private lateinit var searchEditText: EditText
    private lateinit var btnMore: ImageButton

    private lateinit var prefs: SharedPreferences

    private var allApps: List<AppInfo> = emptyList()

    private lateinit var appsAdapter: AppsAdapter
    private lateinit var favoritesAdapter: AppsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_apps)

        prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)

        appsRecyclerView = findViewById(R.id.appsRecyclerView)
        favoritesRecyclerView = findViewById(R.id.favoritesRecyclerView)
        searchEditText = findViewById(R.id.searchEditText)
        btnMore = findViewById(R.id.btnMore)

        // Favoriidid (pinned row)
        favoritesRecyclerView.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)

        // Kõik äpid (grid)
        appsRecyclerView.layoutManager = GridLayoutManager(this, 3)

        // Adapters
        favoritesAdapter = AppsAdapter(
            apps = emptyList(),
            onClick = { app -> launchApp(app) },
            onLongPress = { anchor, app -> showAppMenu(anchor, app) },
            isFavorite = { isFavorite(it) },
            isHidden = { isHidden(it) }
        )

        appsAdapter = AppsAdapter(
            apps = emptyList(),
            onClick = { app -> launchApp(app) },
            onLongPress = { anchor, app -> showAppMenu(anchor, app) },
            isFavorite = { isFavorite(it) },
            isHidden = { isHidden(it) }
        )

        favoritesRecyclerView.adapter = favoritesAdapter
        appsRecyclerView.adapter = appsAdapter

        // Otsing
        searchEditText.addTextChangedListener { text ->
            val q = text?.toString().orEmpty()
            appsAdapter.filterApps(q)

            // Kui tahad, et pinned row filtreerub ka otsinguga:
            // - kui otsing tühi -> näita tavalisi favoriite
            // - kui otsinguga -> näita ainult sobivaid favoriite
            if (q.isBlank()) {
                refreshFavoritesRow()
            } else {
                val favKeys = getFavorites()
                val favApps = allApps
                    .filter { favKeys.contains(makeKey(it)) }
                    .filter { it.label.contains(q, ignoreCase = true) }
                favoritesAdapter.submitList(favApps)
                favoritesRecyclerView.visibility = if (favApps.isEmpty()) View.GONE else View.VISIBLE
            }
        }

        btnMore.setOnClickListener { v ->
            val menu = PopupMenu(this, v)
            menu.menu.add(0, MENU_REFRESH, 0, "Värskenda loend")
            menu.menu.add(0, MENU_CLEAR_SEARCH, 1, "Tühjenda otsing")
            menu.setOnMenuItemClickListener { item ->
                when (item.itemId) {
                    MENU_REFRESH -> {
                        loadApps()
                        true
                    }
                    MENU_CLEAR_SEARCH -> {
                        searchEditText.setText("")
                        true
                    }
                    else -> false
                }
            }
            menu.show()
        }

        loadApps()
    }

    private fun loadApps() {
        allApps = getInstalledLaunchableApps()

        // Peida hidden-id listist (soovi korral)
        val filtered = allApps.filterNot { isHidden(it) }

        appsAdapter.submitList(filtered)
        refreshFavoritesRow()
    }

    private fun getInstalledLaunchableApps(): List<AppInfo> {
        val pm = packageManager
        val intent = Intent(Intent.ACTION_MAIN, null).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }

        return pm.queryIntentActivities(intent, 0)
            .map { ri ->
                AppInfo(
                    packageName = ri.activityInfo.packageName,
                    className = ri.activityInfo.name, // <-- SEE parandab "className missing" vea
                    label = ri.loadLabel(pm).toString(),
                    icon = ri.loadIcon(pm)
                )
            }
            .sortedBy { it.label.lowercase() }
    }

    private fun refreshFavoritesRow() {
        val favKeys = getFavorites()
        val favApps = allApps.filter { favKeys.contains(makeKey(it)) }

        favoritesAdapter.submitList(favApps)
        favoritesRecyclerView.visibility = if (favApps.isEmpty()) View.GONE else View.VISIBLE
    }

    private fun launchApp(app: AppInfo) {
        try {
            val intent = Intent().apply {
                setClassName(app.packageName, app.className)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            startActivity(intent)
        } catch (_: Throwable) {
            // fallback
            val fallback = packageManager.getLaunchIntentForPackage(app.packageName)
            if (fallback != null) startActivity(fallback)
        }
    }

    private fun openAppInfo(app: AppInfo) {
        val i = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.parse("package:${app.packageName}")
        }
        startActivity(i)
    }

    private fun showAppMenu(anchor: View, app: AppInfo) {
        val menu = PopupMenu(this, anchor)

        val favNow = isFavorite(app)
        val hiddenNow = isHidden(app)

        menu.menu.add(0, MENU_TOGGLE_FAVORITE, 0, if (favNow) "Eemalda lemmikutest" else "Lisa lemmikutesse")
        menu.menu.add(0, MENU_TOGGLE_HIDDEN, 1, if (hiddenNow) "Näita (unhide)" else "Peida (hide)")
        menu.menu.add(0, MENU_APP_INFO, 2, "Rakenduse info")

        menu.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                MENU_TOGGLE_FAVORITE -> {
                    toggleFavorite(app)
                    refreshFavoritesRow()
                    true
                }
                MENU_TOGGLE_HIDDEN -> {
                    toggleHidden(app)
                    loadApps()
                    true
                }
                MENU_APP_INFO -> {
                    openAppInfo(app)
                    true
                }
                else -> false
            }
        }

        menu.show()
    }

    // ====== Favorites / Hidden storage ======

    private fun makeKey(app: AppInfo): String = "${app.packageName}/${app.className}"

    private fun getFavorites(): Set<String> =
        prefs.getStringSet(KEY_FAVORITES, emptySet()) ?: emptySet()

    private fun getHidden(): Set<String> =
        prefs.getStringSet(KEY_HIDDEN, emptySet()) ?: emptySet()

    private fun isFavorite(app: AppInfo): Boolean = getFavorites().contains(makeKey(app))
    private fun isHidden(app: AppInfo): Boolean = getHidden().contains(makeKey(app))

    private fun toggleFavorite(app: AppInfo) {
        val key = makeKey(app)
        val set = getFavorites().toMutableSet()
        if (set.contains(key)) set.remove(key) else set.add(key)
        prefs.edit().putStringSet(KEY_FAVORITES, set).apply()
    }

    private fun toggleHidden(app: AppInfo) {
        val key = makeKey(app)
        val set = getHidden().toMutableSet()
        if (set.contains(key)) set.remove(key) else set.add(key)
        prefs.edit().putStringSet(KEY_HIDDEN, set).apply()
    }

    companion object {
        private const val PREFS_NAME = "aura_launcher_prefs"
        private const val KEY_FAVORITES = "favorites"
        private const val KEY_HIDDEN = "hidden"

        private const val MENU_TOGGLE_FAVORITE = 1
        private const val MENU_TOGGLE_HIDDEN = 2
        private const val MENU_APP_INFO = 3

        private const val MENU_REFRESH = 10
        private const val MENU_CLEAR_SEARCH = 11
    }
}
