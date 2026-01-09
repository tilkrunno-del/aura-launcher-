package com.aura.launcher

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class FavoritesAdapter(
    private val items: MutableList<AppInfo>,
    private val onClick: (AppInfo) -> Unit,
    private val onLongPress: (View, AppInfo) -> Unit
) : RecyclerView.Adapter<FavoritesAdapter.FavVH>() {

    inner class FavVH(view: View) : RecyclerView.ViewHolder(view) {
        val icon: ImageView = view.findViewById(R.id.appIcon)
        val name: TextView = view.findViewById(R.id.appName)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FavVH {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_app, parent, false)
        return FavVH(view)
    }

    override fun onBindViewHolder(holder: FavVH, position: Int) {
        val app = items[position]

        holder.icon.setImageDrawable(app.icon)
        holder.name.text = app.label

        holder.itemView.setOnClickListener { onClick(app) }
        holder.itemView.setOnLongClickListener {
            onLongPress(it, app)
            true
        }
    }

    override fun getItemCount(): Int = items.size

    fun submitList(newList: List<AppInfo>) {
        items.clear()
        items.addAll(newList)
        notifyDataSetChanged()
    }
}
