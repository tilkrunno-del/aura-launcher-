package com.aura.launcher

import android.content.pm.PackageManager
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialog

class AppsActivity : AppCompatActivity() {

    private lateinit var adapter: AppsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_apps)

        val recycler = findViewById<RecyclerView>(R.id.appsRecyclerView)
        recycler.layoutManager = GridLayoutManager(this, 4)

        val apps = loadApps()

        adapter = AppsAdapter(
            apps = apps,
            onClick = { app ->
                LauncherUtils.launchApp(this, app)
            },
            onLongPress = { view, app ->
                showAppActions(app)
            }
        )

        recycler.adapter = adapter

        val search = findViewById<android.widget.EditText>(R.id.searchEditText)
        val clear = findViewById<View>(R.id.btnClearSearch)

        search.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val q = s?.toString().orEmpty()
                adapter.filterApps(q)
                clear.visibility = if (q.isEmpty()) View.GONE else View.VISIBLE
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        clear.setOnClickListener {
            search.setText("")
        }
    }

    private fun loadApps(): List<AppInfo> {
        val pm = packageManager
        val intent = android.content.Intent(android.content.Intent.ACTION_MAIN, null)
        intent.addCategory(android.content.Intent.CATEGORY_LAUNCHER)

        return pm.queryIntentActivities(intent, 0).map {
            AppInfo(
                label = it.loadLabel(pm).toString(),
                packageName = it.activityInfo.packageName,
                className = it.activityInfo.name,
                icon = it.loadIcon(pm)
            )
        }.sortedBy { it.label.lowercase() }
    }

    private fun showAppActions(app: AppInfo) {
        AppActionsBottomSheet(this, app).show()
    }
}
