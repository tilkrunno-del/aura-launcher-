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
    private val onAppClick: (AppInfo) -> Unit
) : RecyclerView.Adapter<AppsAdapter.AppViewHolder>() {

    private val filteredApps: MutableList<AppInfo> = allApps.toMutableList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AppViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_app, parent, false)
        return AppViewHolder(v)
    }

    override fun onBindViewHolder(holder: AppViewHolder, position: Int) {
        val app = filteredApps[position]
        holder.appName.text = app.label
        holder.appIcon.setImageDrawable(app.icon)

        holder.itemView.setOnClickListener {
            onAppClick(app)
        }
    }

    override fun getItemCount(): Int = filteredApps.size

    fun filterApps(query: String) {
        val q = query.trim().lowercase(Locale.getDefault())
        filteredApps.clear()

        if (q.isEmpty()) {
            filteredApps.addAll(allApps)
        } else {
            filteredApps.addAll(
                allApps.filter { it.label.lowercase(Locale.getDefault()).contains(q) }
            )
        }

        notifyDataSetChanged()
    }

    class AppViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val appIcon: ImageView = itemView.findViewById(R.id.appIcon)
        val appName: TextView = itemView.findViewById(R.id.appName)
    }
}
