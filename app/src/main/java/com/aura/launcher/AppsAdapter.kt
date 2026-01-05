package com.aura.launcher

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.util.Locale

class AppsAdapter(
    private val allApps: List<AppInfo>,
    private val onClick: (AppInfo) -> Unit
) : RecyclerView.Adapter<AppsAdapter.AppViewHolder>() {

    private val visibleApps: MutableList<AppInfo> = allApps.toMutableList()

    class AppViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val icon: ImageView = itemView.findViewById(R.id.appIcon)
        val name: TextView = itemView.findViewById(R.id.appName)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AppViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_app, parent, false)
        return AppViewHolder(v)
    }

    override fun onBindViewHolder(holder: AppViewHolder, position: Int) {
        val app = visibleApps[position]
        holder.icon.setImageDrawable(app.icon)
        holder.name.text = app.label

        holder.itemView.setOnClickListener { onClick(app) }
    }

    override fun getItemCount(): Int = visibleApps.size

    fun filterApps(query: String) {
        val q = query.trim().lowercase(Locale.getDefault())

        visibleApps.clear()
        if (q.isEmpty()) {
            visibleApps.addAll(allApps)
        } else {
            visibleApps.addAll(allApps.filter { it.label.lowercase(Locale.getDefault()).contains(q) })
        }
        notifyDataSetChanged()
    }
}
