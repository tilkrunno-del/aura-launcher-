package com.aura.launcher

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class AppsAdapter(
  private val items: List<AppsActivity.AppItem>,
  private val onClick: (AppsActivity.AppItem) -> Unit
) : RecyclerView.Adapter<AppsAdapter.VH>() {

  class VH(itemView: View) : RecyclerView.ViewHolder(itemView) {
    val title: TextView = itemView.findViewById(R.id.textTitle)
  }

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
    val v = LayoutInflater.from(parent.context).inflate(R.layout.item_app, parent, false)
    return VH(v)
  }

  override fun onBindViewHolder(holder: VH, position: Int) {
    val item = items[position]
    holder.title.text = item.label
    holder.itemView.setOnClickListener { onClick(item) }
  }

  override fun getItemCount(): Int = items.size
}
