package com.aura.launcher

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView

class AppsActivity : AppCompatActivity() {

    private lateinit var recycler: RecyclerView
    private lateinit var search: EditText
    private lateinit var clear: ImageButton

    private lateinit var adapter: AppsAdapter
    private val allApps = mutableListOf<AppInfo>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // IMPORTANT: peab vastama päris layout failile
        // Veendu, et sul on: res/layout/activity_apps.xml
        setContentView(R.layout.activity_apps)

        // Kui ID-d ei klapi, ära crashi — ütle ja lõpeta
        recycler = findViewById<RecyclerView?>(R.id.appsRecyclerView) ?: run {
            Toast.makeText(this, "UI error: appsRecyclerView missing", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        search = findViewById<EditText?>(R.id.searchEditText) ?: run {
            Toast.makeText(this, "UI error: searchEditText missing", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        clear = findViewById<ImageButton?>(R.id.btnClearSearch) ?: run {
            Toast.makeText(this, "UI error: btnClearSearch missing", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        recycler.layoutManager = GridLayoutManager(this, 4)

        adapter = AppsAdapter(
            apps = emptyList(),
            onClick = { app ->
                LauncherUtils.launchApp(this, app)
            },
            onLongClick = { app ->
                // praegu: ei tee midagi (et ei crashiks)
                // hiljem: ava bottomsheet / peida app
                true
            }
        )
        recycler.adapter = adapter

        loadAppsSafely()

        clear.setOnClickListener {
            search.setText("")
        }

        search.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) = Unit
            override fun afterTextChanged(s: Editable?) {
                val q = s?.toString()?.trim().orEmpty()
                clear.visibility = if (q.isEmpty()) android.view.View.GONE else android.view.View.VISIBLE

                val filtered = if (q.isEmpty()) {
                    allApps
                } else {
                    allApps.filter { it.label.contains(q, ignoreCase = true) }
                }
                adapter.updateList(filtered)
            }
        })
    }

    private fun loadAppsSafely() {
        try {
            allApps.clear()
            allApps.addAll(LauncherUtils.getInstalledApps(this))
            adapter.updateList(allApps)
        } catch (t: Throwable) {
            Toast.makeText(this, "Load apps failed: ${t.message}", Toast.LENGTH_LONG).show()
        }
    }
}
