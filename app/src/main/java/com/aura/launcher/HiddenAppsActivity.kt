package com.aura.launcher

import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialog

class HiddenAppsActivity : AppCompatActivity() {

    private lateinit var recycler: RecyclerView
    private lateinit var adapter: FavoritesAdapter

    private val allApps = mutableListOf<AppInfo>()
    private val hiddenApps = mutableListOf<AppInfo>()

    private val prefs by lazy { getSharedPreferences("aura_prefs", Context.MODE_PRIVATE) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_hidden_apps)

        recycler = findViewById(R.id.hiddenRecyclerView)

        adapter = FavoritesAdapter(
            items = hiddenApps,
            onClick = { app -> openApp(app.packageName) },
            onLongPress = { anchor, app -> showHiddenActions(anchor, app) }
        )

        recycler.layoutManager = GridLayoutManager(this, 4)
        recycler.adapter = adapter

        // tagasi nupp (kui sul layoutis on)
        findViewById<ImageButton?>(R.id.btnBack)?.setOnClickListener { finish() }

        loadApps()
        refreshHiddenList()
    }

    private fun loadApps() {
        allApps.clear()

        val pm = packageManager
        val intent = Intent(Intent.ACTION_MAIN, null).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }

        val resolved = pm.queryIntentActivities(intent, 0)
        val apps = resolved.map { ri ->
            val pkg = ri.activityInfo.packageName
            val label = ri.loadLabel(pm)?.toString() ?: pkg
            val icon = ri.loadIcon(pm)
            AppInfo(pkg, label, icon)
        }.sortedBy { it.label.lowercase() }

        allApps.addAll(apps)
    }

    private fun refreshHiddenList() {
        val hiddenSet = prefs.getStringSet("hidden_apps", emptySet()) ?: emptySet()
        val list = allApps.filter { hiddenSet.contains(it.packageName) }

        hiddenApps.clear()
        hiddenApps.addAll(list)
        adapter.submitList(hiddenApps.toList())

        if (hiddenApps.isEmpty()) {
            Toast.makeText(this, "Peidetud rakendusi pole", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showHiddenActions(anchor: View, app: AppInfo) {
        val dialog = BottomSheetDialog(this)
        dialog.setContentView(R.layout.sheet_hidden_actions)

        dialog.findViewById<View>(R.id.action_unhide)?.setOnClickListener {
            unhideApp(app.packageName)
            dialog.dismiss()
        }

        dialog.findViewById<View>(R.id.action_info)?.setOnClickListener {
            openAppInfo(app.packageName)
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun unhideApp(packageName: String) {
        val set = (prefs.getStringSet("hidden_apps", emptySet()) ?: emptySet()).toMutableSet()
        set.remove(packageName)
        prefs.edit().putStringSet("hidden_apps", set).apply()
        refreshHiddenList()
        Toast.makeText(this, "Rakendus nähtavaks tehtud", Toast.LENGTH_SHORT).show()
    }

    private fun openApp(packageName: String) {
        val launch = packageManager.getLaunchIntentForPackage(packageName)
        if (launch != null) startActivity(launch)
        else Toast.makeText(this, "Ei saa avada", Toast.LENGTH_SHORT).show()
    }

    private fun openAppInfo(packageName: String) {
        val i = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.parse("package:$packageName")
        }
        startActivity(i)
    }
}
