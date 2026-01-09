package com.aura.launcher

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.EditText
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class AppsActivity : AppCompatActivity() {

    private lateinit var appsAdapter: AppsAdapter
    private lateinit var searchEditText: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_apps)

        // ✅ Sinu XML ID-d
        searchEditText = findViewById(R.id.searchEditText)
        val appsRecyclerView = findViewById<RecyclerView>(R.id.appsRecyclerView)
        val favoritesRecyclerView = findViewById<RecyclerView>(R.id.favoritesRecyclerView)
        val btnMore = findViewById<ImageButton>(R.id.btnMore)

        // Recyclerid
        appsRecyclerView.layoutManager = LinearLayoutManager(this)
        favoritesRecyclerView.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)

        val apps = loadApps()

        appsAdapter = AppsAdapter(
            apps = apps,
            onClick = { app ->
                launchApp(app.packageName, app.className)
            },
            onLongPress = { view, app ->
                openAppInfo(app.packageName)
            },
            isFavorite = { app ->
                AppPrefs.isFavorite(this, app.packageName)
            },
            isHidden = { app ->
                AppPrefs.isHidden(this, app.packageName)
            }
        )

        appsRecyclerView.adapter = appsAdapter

        // (Praegu ei seo favoritesAdapterit, aga RecyclerView on valmis)
        // Kui sul on FavoritesAdapter olemas, saad siia lisada.

        // ✅ OTSING TÖÖLE (sinu EditText id)
        searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit
            override fun afterTextChanged(s: Editable?) = Unit
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                appsAdapter.filter(s?.toString().orEmpty())
            }
        })

        // Menüü nupp (võid hiljem popup-menüü teha)
        btnMore.setOnClickListener {
            // TODO: show menu
        }
    }

    private fun loadApps(): List<AppInfo> {
        val pm = packageManager
        val intent = Intent(Intent.ACTION_MAIN, null).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }

        return pm.queryIntentActivities(intent, 0)
            .map { ri ->
                AppInfo(
                    packageName = ri.activityInfo.packageName,
                    className = ri.activityInfo.name, // ✅ oluline: className
                    label = ri.loadLabel(pm).toString(),
                    icon = ri.loadIcon(pm)
                )
            }
            .sortedBy { it.label.lowercase() }
    }

    private fun launchApp(packageName: String, className: String) {
        // kindlam kui getLaunchIntentForPackage (mõnikord null)
        val intent = Intent().apply {
            setClassName(packageName, className)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        try {
            startActivity(intent)
        } catch (_: Exception) {
            // fallback
            val fallback = packageManager.getLaunchIntentForPackage(packageName)
            if (fallback != null) startActivity(fallback)
        }
    }

    private fun openAppInfo(packageName: String) {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.parse("package:$packageName")
        }
        startActivity(intent)
    }
}
