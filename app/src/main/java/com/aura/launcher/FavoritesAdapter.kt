package com.aura.launcher

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView

class FavoritesAdapter(
    private val onClick: (AppInfo) -> Unit,
    private val onLongClick: (AppInfo) -> Unit
) : RecyclerView.Adapter<FavoritesAdapter.VH>() {

    private val items = mutableListOf<AppInfo>()

    fun submit(list: List<AppInfo>) {
        items.clear()
        items.addAll(list)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_favorite, parent, false)
        return VH(v)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val app = items[position]
        holder.icon.setImageDrawable(app.icon)

        holder.itemView.setOnClickListener { onClick(app) }
        holder.itemView.setOnLongClickListener {
            onLongClick(app)
            true
        }
    }

    override fun getItemCount(): Int = items.size

    class VH(view: View) : RecyclerView.ViewHolder(view) {
        val icon: ImageView = view.findViewById(R.id.favIcon)
    }
}
