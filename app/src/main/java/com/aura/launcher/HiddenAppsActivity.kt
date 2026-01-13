package com.aura.launcher

import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Typeface
import android.os.Bundle
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class HiddenAppsActivity : AppCompatActivity() {

    private val prefs by lazy { getSharedPreferences(PREFS, Context.MODE_PRIVATE) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_hidden_apps)

        // Back nupp (sinu XML-is olemas)
        findViewById<View>(R.id.btnBack)?.setOnClickListener { finish() }

        val recycler = findViewById<RecyclerView>(R.id.hiddenRecyclerView)
        recycler.layoutManager = LinearLayoutManager(this)
        recycler.setHasFixedSize(true)

        val hidden = HiddenStore.getAll(prefs).toMutableList()
        val adapter = HiddenAdapter(
            items = hidden,
            pm = packageManager,
            onRestore = { component ->
                HiddenStore.unhide(prefs, component)
                val idx = hidden.indexOf(component)
                if (idx >= 0) {
                    hidden.removeAt(idx)
                    recycler.adapter?.notifyItemRemoved(idx)
                }
                Toast.makeText(this, getString(R.string.restore_app), Toast.LENGTH_SHORT).show()
                if (hidden.isEmpty()) finish()
            }
        )
        recycler.adapter = adapter
    }

    private class HiddenAdapter(
        private val items: List<String>,
        private val pm: PackageManager,
        private val onRestore: (String) -> Unit
    ) : RecyclerView.Adapter<HiddenAdapter.VH>() {

        class VH(val root: LinearLayout) : RecyclerView.ViewHolder(root) {
            val icon: ImageView = root.getChildAt(0) as ImageView
            val texts: LinearLayout = root.getChildAt(1) as LinearLayout
            val title: TextView = texts.getChildAt(0) as TextView
            val subtitle: TextView = texts.getChildAt(1) as TextView
            val btn: Button = root.getChildAt(2) as Button
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
            val ctx = parent.context

            val row = LinearLayout(ctx).apply {
                orientation = LinearLayout.HORIZONTAL
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
                setPadding(dp(ctx, 12), dp(ctx, 10), dp(ctx, 12), dp(ctx, 10))
            }

            val icon = ImageView(ctx).apply {
                layoutParams = LinearLayout.LayoutParams(dp(ctx, 44), dp(ctx, 44))
            }

            val texts = LinearLayout(ctx).apply {
                orientation = LinearLayout.VERTICAL
                layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f).apply {
                    marginStart = dp(ctx, 12)
                    marginEnd = dp(ctx, 12)
                }
            }

            val title = TextView(ctx).apply {
                setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f)
                setTypeface(typeface, Typeface.BOLD)
            }

            val subtitle = TextView(ctx).apply {
                setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f)
                alpha = 0.7f
            }

            val btn = Button(ctx).apply {
                text = ctx.getString(R.string.restore_app)
                layoutParams = LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
            }

            texts.addView(title)
            texts.addView(subtitle)

            row.addView(icon)
            row.addView(texts)
            row.addView(btn)

            return VH(row)
        }

        override fun onBindViewHolder(holder: VH, position: Int) {
            val component = items[position]
            val (pkg, cls) = splitComponent(component)

            // Label + icon turvaliselt
            try {
                val ai = pm.getApplicationInfo(pkg, 0)
                holder.title.text = pm.getApplicationLabel(ai)?.toString() ?: pkg
                holder.icon.setImageDrawable(pm.getApplicationIcon(ai))
            } catch (_: Exception) {
                holder.title.text = pkg
                holder.icon.setImageResource(android.R.drawable.sym_def_app_icon)
            }

            holder.subtitle.text = cls ?: pkg

            holder.btn.setOnClickListener { onRestore(component) }
        }

        override fun getItemCount(): Int = items.size

        private fun splitComponent(s: String): Pair<String, String?> {
            val idx = s.indexOf('/')
            return if (idx <= 0) s to null else s.substring(0, idx) to s.substring(idx + 1)
        }

        private fun dp(ctx: Context, v: Int): Int =
            (v * ctx.resources.displayMetrics.density).toInt()
    }

    object HiddenStore {
        fun getAll(prefs: android.content.SharedPreferences): Set<String> =
            prefs.getStringSet(KEY, emptySet()) ?: emptySet()

        fun hide(prefs: android.content.SharedPreferences, component: String) {
            val set = getAll(prefs).toMutableSet()
            set.add(component)
            prefs.edit().putStringSet(KEY, set).apply()
        }

        fun unhide(prefs: android.content.SharedPreferences, component: String) {
            val set = getAll(prefs).toMutableSet()
            set.remove(component)
            prefs.edit().putStringSet(KEY, set).apply()
        }

        fun isHidden(prefs: android.content.SharedPreferences, component: String): Boolean =
            getAll(prefs).contains(component)
    }

    companion object {
        private const val PREFS = "aura_prefs"
        private const val KEY = "hidden_components"
    }
}
