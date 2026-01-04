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
    private val onClick: (AppInfo) -> Unit
) : RecyclerView.Adapter<AppsAdapter.AppViewHolder>() {

    private val allApps: List<AppInfo> = apps
    private val shownApps: MutableList<AppInfo> = apps.toMutableList()

    class AppViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imgIcon: ImageView = itemView.findViewById(R.id.imgIcon)
        val textTitle: TextView = itemView.findViewById(R.id.textTitle)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AppViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_app, parent, false)
        return AppViewHolder(v)
    }

    override fun onBindViewHolder(holder: AppViewHolder, position: Int) {
        val app = shownApps[position]
        holder.imgIcon.setImageDrawable(app.icon)
        holder.textTitle.text = app.label

        holder.itemView.setOnClickListener { onClick(app) }
    }

    override fun getItemCount(): Int = shownApps.size

    fun filter(query: String?) {
        val q = (query ?: "").trim().lowercase(Locale.getDefault())

        shownApps.clear()

        if (q.isEmpty()) {
            shownApps.addAll(allApps)
        } else {
            shownApps.addAll(
                allApps.filter {
                    it.label.lowercase(Locale.getDefault()).contains(q) ||
                            it.packageName.lowercase(Locale.getDefault()).contains(q)
                }
            )
        }

        notifyDataSetChanged()
    }
}
