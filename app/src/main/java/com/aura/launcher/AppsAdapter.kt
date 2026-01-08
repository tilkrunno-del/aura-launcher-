package com.aura.launcher

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.PopupMenu
import androidx.recyclerview.widget.RecyclerView
import java.util.Locale
import kotlin.math.max

class AppsAdapter(
    private val onAppClick: (AppInfo) -> Unit,
    private val onMenuAction: (AppInfo, MenuAction) -> Unit,
    private val spanCount: Int = 3,
    private val animEnabled: Boolean = true
) : RecyclerView.Adapter<AppsAdapter.AppViewHolder>() {

    enum class MenuAction { TOGGLE_FAVORITE, TOGGLE_HIDDEN, APP_INFO, UNINSTALL }

    private val baseApps = mutableListOf<AppInfo>()
    private val filteredApps = mutableListOf<AppInfo>()
    private var currentQuery: String = ""
    private var lastAnimatedPosition = -1

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
            filteredApps.addAll(baseApps.filter { it.label.lowercase(Locale.getDefault()).contains(q) })
        }

        lastAnimatedPosition = -1
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
            showMenu(holder.itemView, app)
            true
        }

        if (animEnabled) {
            if (position > lastAnimatedPosition) {
                startStaggerAnim(holder.itemView, position)
                lastAnimatedPosition = position
            } else {
                holder.itemView.alpha = 1f
                holder.itemView.scaleX = 1f
                holder.itemView.scaleY = 1f
                holder.itemView.translationY = 0f
            }
        } else {
            holder.itemView.animate().cancel()
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

    private fun showMenu(anchor: View, app: AppInfo) {
        val popup = PopupMenu(anchor.context, anchor)
        popup.menu.add(0, 1, 0, "Lisa/Eemalda lemmik")
        popup.menu.add(0, 2, 1, "Peida/Too tagasi")
        popup.menu.add(0, 3, 2, "App info")
        popup.menu.add(0, 4, 3, "Uninstall")

        popup.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                1 -> { onMenuAction(app, MenuAction.TOGGLE_FAVORITE); true }
                2 -> { onMenuAction(app, MenuAction.TOGGLE_HIDDEN); true }
                3 -> { onMenuAction(app, MenuAction.APP_INFO); true }
                4 -> { onMenuAction(app, MenuAction.UNINSTALL); true }
                else -> false
            }
        }
        popup.show()
    }

    private fun startStaggerAnim(view: View, position: Int) {
        val cols = max(1, spanCount)
        val col = position % cols
        val row = position / cols

        val colDelay = 35L
        val rowDelay = 55L
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
            .setInterpolator(DecelerateInterpolator())
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
