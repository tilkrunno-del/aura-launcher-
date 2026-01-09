package com.aura.launcher

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.EditText
import android.widget.ImageButton
import android.widget.PopupMenu
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.util.Locale

class AppsActivity : AppCompatActivity() {

    private lateinit var appsAdapter: AppsAdapter
    private lateinit var favoritesAdapter: FavoritesAdapter

    private lateinit var searchEditText: EditText
    private lateinit var appsRecyclerView: RecyclerView
    private lateinit var favoritesRecyclerView: RecyclerView
    private lateinit var btnMore: ImageButton

    private var allApps: List<AppInfo> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_apps)

        searchEditText = findViewById(R.id.searchEditText)
        appsRecyclerView = findViewById(R.id.appsRecyclerView)
        favoritesRecyclerView = findViewById(R.id.favoritesRecyclerView)
        btnMore = findViewById(R.id.btnMore)

        // Grid 3 veergu (sa saad selle hiljem seadistusse viia)
        appsRecyclerView.layoutManager = GridLayoutManager(this, 3)

        // Favorites row: horisontaalne
        favoritesRecyclerView.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)

        // Lae äpid
        allApps = loadApps()

        // Apps grid adapter
        appsAdapter = AppsAdapter(
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
        appsRecyclerView.adapter = appsAdapter

        // Favorites adapter (kasutab sama item_app.xml)
        favoritesAdapter = FavoritesAdapter(
            items = mutableListOf(),
            onClick = { app -> launchApp(app) },
            onLongPress = { view, app -> showFavoriteMenu(view, app) }
        )
        favoritesRecyclerView.adapter = favoritesAdapter

        // Täida pinned row kohe
        refreshFavoritesRow()

        // 🔍 OTSING (kindel TextWatcher, ei sõltu ktx-ist)
        searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun afterTextChanged(s: Editable?) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                appsAdapter.filterApps(s?.toString().orEmpty())
            }
        })

        // ☰ ülemine menüü
        btnMore.setOnClickListener { showTopMenu(it) }
    }

    // ----------------------------
    // Lae kõik launcher-äpid
    // ----------------------------
    private fun loadApps(): List<AppInfo> {
        val pm = packageManager
        val intent = Intent(Intent.ACTION_MAIN, null).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }

        return pm.queryIntentActivities(intent, 0)
            .map { ri ->
                AppInfo(
                    packageName = ri.activityInfo.packageName,
                    className = ri.activityInfo.name,
                    label = ri.loadLabel(pm).toString(),
                    icon = ri.loadIcon(pm)
                )
            }
            .sortedBy { it.label.lowercase(Locale.getDefault()) }
    }

    // ----------------------------
    // Favorites row update
    // ----------------------------
    private fun refreshFavoritesRow() {
        val favKeys = AppPrefs.getFavorites(this)
        val favApps = allApps
            .filter { favKeys.contains(makeKey(it)) }
            .sortedBy { it.label.lowercase(Locale.getDefault()) }

        favoritesAdapter.submitList(favApps)

        // Kui lemmikuid pole, võid rea peita (soovi korral)
        favoritesRecyclerView.visibility = if (favApps.isEmpty()) View.GONE else View.VISIBLE
    }

    // ----------------------------
    // App launch / info
    // ----------------------------
    private fun launchApp(app: AppInfo) {
        val intent = Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
            setClassName(app.packageName, app.className)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        try {
            startActivity(intent)
        } catch (_: Exception) {
            val fallback = packageManager.getLaunchIntentForPackage(app.packageName)
            if (fallback != null) startActivity(fallback)
        }
    }

    private fun openAppInfo(packageName: String) {
        val intent = Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.parse("package:$packageName")
        }
        startActivity(intent)
    }

    // ----------------------------
    // Long-press menüü gridis
    // ----------------------------
    private fun showAppMenu(anchor: View, app: AppInfo) {
        val key = makeKey(app)
        val isFav = AppPrefs.isFavorite(this, key)
        val isHid = AppPrefs.isHidden(this, key)

        val popup = PopupMenu(this, anchor)

        popup.menu.add(0, 1, 0, if (isFav) "Eemalda lemmikutest" else "Lisa lemmikutesse")
        popup.menu.add(0, 2, 1, if (isHid) "Too tagasi" else "Peida")
        popup.menu.add(0, 3, 2, "Rakenduse info")

        popup.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                1 -> {
                    AppPrefs.toggleFavorite(this, key)
                    refreshFavoritesRow()
                    appsAdapter.notifyDataSetChanged()
                    true
                }
                2 -> {
                    AppPrefs.toggleHidden(this, key)
                    // refresh list: peidetud võiks hiljem päriselt listist eemaldada
                    appsAdapter.notifyDataSetChanged()
                    true
                }
                3 -> {
                    openAppInfo(app.packageName)
                    true
                }
                else -> false
            }
        }

        popup.show()
    }

    // ----------------------------
    // Long-press menüü lemmikureas
    // ----------------------------
    private fun showFavoriteMenu(anchor: View, app: AppInfo) {
        val key = makeKey(app)
        val popup = PopupMenu(this, anchor)

        popup.menu.add(0, 1, 0, "Eemalda lemmikutest")
        popup.menu.add(0, 2, 1, "Rakenduse info")

        popup.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                1 -> {
                    AppPrefs.toggleFavorite(this, key) // eemaldab
                    refreshFavoritesRow()
                    appsAdapter.notifyDataSetChanged()
                    true
                }
                2 -> {
                    openAppInfo(app.packageName)
                    true
                }
                else -> false
            }
        }

        popup.show()
    }

    // ----------------------------
    // Ülemine menüü (☰)
    // ----------------------------
    private fun showTopMenu(anchor: View) {
        val popup = PopupMenu(this, anchor)

        popup.menu.add(0, 1, 0, "Seaded")
        popup.menu.add(0, 2, 1, "Peidetud rakendused")

        popup.setOnMenuItemClickListener {
            when (it.itemId) {
                1 -> {
                    startActivity(Intent(this, SettingsActivity::class.java))
                    true
                }
                2 -> {
                    startActivity(Intent(this, HiddenAppsActivity::class.java))
                    true
                }
                else -> false
            }
        }

        popup.show()
    }

    private fun makeKey(app: AppInfo) = "${app.packageName}/${app.className}"
}
