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

    private val shownApps: MutableList<AppInfo> = allApps.toMutableList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AppViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_app, parent, false)
        return AppViewHolder(view)
    }

    override fun onBindViewHolder(holder: AppViewHolder, position: Int) {
        val app = shownApps[position]
        holder.bind(app)
        holder.itemView.setOnClickListener { onClick(app) }
    }

    override fun getItemCount(): Int = shownApps.size

    fun filterApps(query: String) {
        val q = query.trim().lowercase(Locale.getDefault())

        shownApps.clear()
        if (q.isEmpty()) {
            shownApps.addAll(allApps)
        } else {
            shownApps.addAll(allApps.filter { it.label.lowercase(Locale.getDefault()).contains(q) })
        }
        notifyDataSetChanged()
    }

    class AppViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val appIcon: ImageView = itemView.findViewById(R.id.appIcon)
        private val appName: TextView = itemView.findViewById(R.id.appName)

        fun bind(app: AppInfo) {
            appIcon.setImageDrawable(app.icon)
            appName.text = app.label
        }
    }
}
