package YOUR.PACKAGE.NAME

import android.content.pm.ResolveInfo
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class AppsAdapter(
    private val items: List<ResolveInfo>,
    private val getLabel: (ResolveInfo) -> String,
    private val getIcon: (ResolveInfo) -> android.graphics.drawable.Drawable,
    private val onClick: (ResolveInfo) -> Unit
) : RecyclerView.Adapter<AppsAdapter.VH>() {

    class VH(v: View) : RecyclerView.ViewHolder(v) {
        val icon: ImageView = v.findViewById(R.id.appIcon)
        val name: TextView = v.findViewById(R.id.appName)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_app, parent, false)
        return VH(v)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val ri = items[position]
        holder.name.text = getLabel(ri)
        holder.icon.setImageDrawable(getIcon(ri))
        holder.itemView.setOnClickListener { onClick(ri) }
    }

    override fun getItemCount(): Int = items.size
}
