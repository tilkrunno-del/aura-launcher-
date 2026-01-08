package com.aura.launcher

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.util.Locale

class AppsAdapter(
    private val onAppClick: (AppInfo) -> Unit,
    private val onAppLongClick: (AppInfo) -> Unit
) : RecyclerView.Adapter<AppsAdapter.AppViewHolder>() {

    private val baseApps = mutableListOf<AppInfo>()      // hetkel kuvatav “baas” (kas visible või hidden)
    private val filteredApps = mutableListOf<AppInfo>()  // tegelik renderdatav list
    private var currentQuery: String = ""

    fun submitApps(list: List<AppInfo>) {
        baseApps.clear()
        baseApps.addAll(list)
        filterApps(currentQuery)
    }

    fun filterApps(query: String) {
        currentQuery = query
        filteredApps.clear()

        if (query.isBlank()) {
            filteredApps.addAll(baseApps)
        } else {
            val q = query.lowercase(Locale.getDefault())
            filteredApps.addAll(
                baseApps.filter { it.label.lowercase(Locale.getDefault()).contains(q) }
            )
        }
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AppViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_app, parent, false)
        return AppViewHolder(view)
    }

    override fun onBindViewHolder(holder: AppViewHolder, position: Int) {
        val app = filteredApps[position]
        holder.bind(app)

        holder.itemView.setOnClickListener { onAppClick(app) }
        holder.itemView.setOnLongClickListener {
            onAppLongClick(app)
            true
        }
    }

    override fun getItemCount(): Int = filteredApps.size

    class AppViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val icon: ImageView = itemView.findViewById(R.id.appIcon)
        private val name: TextView = itemView.findViewById(R.id.appName)

        fun bind(app: AppInfo) {
            icon.setImageDrawable(app.icon)
            name.text = app.label
        }
    }
}
