package com.aura.launcher

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.util.Locale

class AppsAdapter(
    apps: List<AppInfo>,
    private val onClick: (AppInfo) -> Unit,
    private val onLongPress: (View, AppInfo) -> Unit = { _, _ -> },
    private val isFavorite: (AppInfo) -> Boolean = { false },
    private val isHidden: (AppInfo) -> Boolean = { false }
) : RecyclerView.Adapter<AppsAdapter.AppViewHolder>() {

    private val allApps = apps.toMutableList()
    private val visibleApps = apps.toMutableList()

    inner class AppViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val icon: ImageView = view.findViewById(R.id.appIcon)
        val name: TextView = view.findViewById(R.id.appName)
        val badge: View? = view.findViewById<View?>(R.id.appBadge) // kui lisad badge view
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AppViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_app, parent, false)
        return AppViewHolder(view)
    }

    override fun onBindViewHolder(holder: AppViewHolder, position: Int) {
        val app = visibleApps[position]

        holder.icon.setImageDrawable(app.icon)
        holder.name.text = app.label

        holder.itemView.setOnClickListener { onClick(app) }
        holder.itemView.setOnLongClickListener {
            onLongPress(it, app)
            true
        }

        val hidden = isHidden(app)
        holder.itemView.alpha = if (hidden) 0.35f else 1f

        // Lemmiku badge (kui item_app.xml-is olemas)
        holder.badge?.visibility = if (isFavorite(app)) View.VISIBLE else View.GONE
    }

    override fun getItemCount(): Int = visibleApps.size

    fun filterApps(query: String) {
        val q = query.trim().lowercase(Locale.getDefault())

        visibleApps.clear()
        val base = allApps.filter { !isHidden(it) }

        if (q.isBlank()) {
            visibleApps.addAll(base)
        } else {
            visibleApps.addAll(
                base.filter {
                    it.label.lowercase(Locale.getDefault()).contains(q) ||
                        it.packageName.lowercase(Locale.getDefault()).contains(q)
                }
            )
        }
        notifyDataSetChanged()
    }

    fun submitList(newList: List<AppInfo>) {
        allApps.clear()
        allApps.addAll(newList)
        visibleApps.clear()
        visibleApps.addAll(newList)
        notifyDataSetChanged()
    }
}
