package com.aura.launcher

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class AppsAdapter(
    private val items: MutableList<AppInfo>,
    private val onClick: (AppInfo) -> Unit,
    private val onLongClick: (AppInfo) -> Boolean
) : RecyclerView.Adapter<AppsAdapter.AppViewHolder>() {

    fun updateList(newItems: List<AppInfo>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AppViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_app, parent, false)
        return AppViewHolder(view)
    }

    override fun onBindViewHolder(holder: AppViewHolder, position: Int) {
        val app = items[position]
        holder.icon.setImageDrawable(app.icon)
        holder.name.text = app.label

        holder.itemView.setOnClickListener { onClick(app) }
        holder.itemView.setOnLongClickListener { onLongClick(app) }
    }

    override fun getItemCount(): Int = items.size

    class AppViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val icon: ImageView = view.findViewById(R.id.appIcon)
        // NB! item_app.xml-s on sul suure tõenäosusega appName (sama mis FavoritesAdapter)
        val name: TextView = view.findViewById(R.id.appName)
    }
}
