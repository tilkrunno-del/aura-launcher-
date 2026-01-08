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
    private val onLongPress: (View, AppInfo) -> Unit,
    private val isFavorite: (AppInfo) -> Boolean,
    private val isHidden: (AppInfo) -> Boolean
) : RecyclerView.Adapter<AppsAdapter.AppViewHolder>() {

    private val allApps = apps.toMutableList()
    private val visibleApps = apps.toMutableList()

    inner class AppViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val icon: ImageView = view.findViewById(R.id.appIcon)
        val name: TextView = view.findViewById(R.id.appName)
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

        holder.itemView.setOnClickListener {
            onClick(app)
        }

        holder.itemView.setOnLongClickListener {
            onLongPress(it, app)
            true
        }

        // (valmis tulevikuks – visuaalne state)
        holder.itemView.alpha = if (isHidden(app)) 0.4f else 1f
    }

    override fun getItemCount(): Int = visibleApps.size

    fun filterApps(query: String) {
        visibleApps.clear()
        if (query.isBlank()) {
            visibleApps.addAll(allApps)
        } else {
            val q = query.lowercase(Locale.getDefault())
            visibleApps.addAll(
                allApps.filter {
                    it.label.lowercase(Locale.getDefault()).contains(q)
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
