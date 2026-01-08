package com.aura.launcher

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.util.Locale
import kotlin.math.max

class AppsAdapter(
    private val originalApps: List<AppInfo>,
    private val onAppClick: (AppInfo) -> Unit,
    private val spanCount: Int = 3 // 3 veergu
) : RecyclerView.Adapter<AppsAdapter.AppViewHolder>() {

    private val filteredApps = originalApps.toMutableList()
    private var lastAnimatedPosition = -1

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AppViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_app, parent, false)
        return AppViewHolder(view)
    }

    override fun onBindViewHolder(holder: AppViewHolder, position: Int) {
        val app = filteredApps[position]
        holder.bind(app)

        holder.itemView.setOnClickListener { onAppClick(app) }

        // Stagger ainult esmakordselt (vältib “vilkumist” scrollil)
        if (position > lastAnimatedPosition) {
            startStaggerAnim(holder.itemView, position)
            lastAnimatedPosition = position
        } else {
            // Kui juba animitud, hoia lõppseis
            holder.itemView.alpha = 1f
            holder.itemView.scaleX = 1f
            holder.itemView.scaleY = 1f
            holder.itemView.translationY = 0f
        }
    }

    override fun onViewDetachedFromWindow(holder: AppViewHolder) {
        super.onViewDetachedFromWindow(holder)
        holder.itemView.animate().cancel()
    }

    override fun getItemCount(): Int = filteredApps.size

    fun filterApps(query: String) {
        filteredApps.clear()
        if (query.isBlank()) {
            filteredApps.addAll(originalApps)
        } else {
            val q = query.lowercase(Locale.getDefault())
            filteredApps.addAll(
                originalApps.filter {
                    it.label.lowercase(Locale.getDefault()).contains(q)
                }
            )
        }
        // Otsingu järel lase uuesti “stagger” (ilus efekt)
        lastAnimatedPosition = -1
        notifyDataSetChanged()
    }

    private fun startStaggerAnim(view: View, position: Int) {
        val cols = max(1, spanCount)
        val col = position % cols
        val row = position / cols

        // Veergude kaupa “wave”: vasak->parem + ridade viide
        val colDelay = 35L   // veeru vahe
        val rowDelay = 55L   // rea vahe
        val delay = (row * rowDelay) + (col * colDelay)

        view.alpha = 0f
        view.scaleX = 0.92f
        view.scaleY = 0.92f
        view.translationY = 14f

        view.animate()
            .alpha(1f)
            .scaleX(1f)
            .scaleY(1f)
            .translationY(0f)
            .setStartDelay(delay)
            .setDuration(220L)
            .setInterpolator(android.view.animation.DecelerateInterpolator())
            .start()
    }

    class AppViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val icon: ImageView = itemView.findViewById(R.id.appIcon)
        private val name: TextView = itemView.findViewById(R.id.appName)

        fun bind(app: AppInfo) {
            icon.setImageDrawable(app.icon)
            name.text = app.label
        }
    }
}
