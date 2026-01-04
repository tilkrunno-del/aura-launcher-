package com.aura.launcher

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class AppsAdapter(
    private var apps: List<AppInfo>,
    private val onClick: (AppInfo) -> Unit
) : RecyclerView.Adapter<AppsAdapter.AppViewHolder>() {

    fun updateApps(newApps: List<AppInfo>) {
        apps = newApps
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AppViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_app, parent, false)
        return AppViewHolder(v)
    }

    override fun onBindViewHolder(holder: AppViewHolder, position: Int) {
        val app = apps[position]
        holder.bind(app)
        holder.itemView.setOnClickListener { onClick(app) }
    }

    override fun getItemCount(): Int = apps.size

    class AppViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val imgIcon: ImageView = itemView.findViewById(R.id.imgIcon)
        private val textTitle: TextView = itemView.findViewById(R.id.textTitle)

        fun bind(app: AppInfo) {
            imgIcon.setImageDrawable(app.icon)
            textTitle.text = app.label
        }
    }
}
