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
    private val originalApps: List<AppInfo>,
    private val onAppClick: (AppInfo) -> Unit,
    private val spanCount: Int = 3
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

        // Long-press menüü
        holder.itemView.setOnLongClickListener {
            showAppMenu(holder.itemView, app)
            true
        }

        // Stagger animatsioon (ainult esmakordselt)
        if (position > lastAnimatedPosition) {
            startStaggerAnim(holder.itemView, position)
            lastAnimatedPosition = position
        } else {
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
        // lase otsingu järel uuesti stagger
        lastAnimatedPosition = -1
        notifyDataSetChanged()
    }

    // -----------------------
    // Long-press menüü
    // -----------------------
    private fun showAppMenu(anchor: View, app: AppInfo) {
        val popup = PopupMenu(anchor.context, anchor)
        popup.menu.add(0, 1, 0, "App info")
        popup.menu.add(0, 2, 1, "Uninstall")

        popup.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                1 -> {
                    openAppInfo(anchor, app.packageName)
                    true
                }
                2 -> {
                    requestUninstall(anchor, app.packageName)
                    true
                }
                else -> false
            }
        }
        popup.show()
    }

    private fun openAppInfo(anchor: View, packageName: String) {
        val intent = android.content.Intent(
            android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS
        ).apply {
            data = android.net.Uri.parse("package:$packageName")
            addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        anchor.context.startActivity(intent)
    }

    private fun requestUninstall(anchor: View, packageName: String) {
        val intent = android.content.Intent(
            android.content.Intent.ACTION_DELETE
        ).apply {
            data = android.net.Uri.parse("package:$packageName")
            addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        anchor.context.startActivity(intent)
    }

    // -----------------------
    // Stagger animatsioon
    // -----------------------
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
