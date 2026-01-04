package com.aura.launcher

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class AppsAdapter(
    private val onClick: (AppInfo) -> Unit
) : RecyclerView.Adapter<AppsAdapter.VH>() {

    private val items: MutableList<AppInfo> = mutableListOf()

    fun submitList(newItems: List<AppInfo>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }

    fun getFirstOrNull(): AppInfo? = items.firstOrNull()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_app, parent, false)
        return VH(v)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val app = items[position]
        holder.bind(app)
        holder.itemView.setOnClickListener { onClick(app) }
    }

    override fun getItemCount(): Int = items.size

    class VH(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val imgIcon: ImageView = itemView.findViewById(R.id.imgIcon)
        private val txtTitle: TextView = itemView.findViewById(R.id.textTitle)

        fun bind(app: AppInfo) {
            imgIcon.setImageDrawable(app.icon)
            txtTitle.text = app.label
        }
    }
}
