package com.aura.launcher

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.util.Locale

class AppsAdapter(
    private val originalList: List<AppInfo>,
    private val onClick: (AppInfo) -> Unit
) : RecyclerView.Adapter<AppsAdapter.VH>() {

    private val filteredList = mutableListOf<AppInfo>().apply {
        addAll(originalList)
    }

    class VH(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val icon: ImageView = itemView.findViewById(R.id.appIcon)
        val name: TextView = itemView.findViewById(R.id.appName)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_app, parent, false)
        return VH(view)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val app = filteredList[position]
        holder.name.text = app.label
        holder.icon.setImageDrawable(app.icon)
        holder.itemView.setOnClickListener { onClick(app) }
    }

    override fun getItemCount(): Int = filteredList.size

    fun filterApps(query: String) {
        val q = query.lowercase(Locale.getDefault())
        filteredList.clear()

        if (q.isBlank()) {
            filteredList.addAll(originalList)
        } else {
            filteredList.addAll(
                originalList.filter {
                    it.label.lowercase(Locale.getDefault()).contains(q)
                }
            )
        }
        notifyDataSetChanged()
    }
}
