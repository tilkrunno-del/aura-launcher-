package com.aura.launcher

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.view.isVisible
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.button.MaterialButton

class AppActionsBottomSheet : BottomSheetDialogFragment() {

    interface Callbacks {
        fun onRequestRefresh()
        fun onLaunch(app: AppInfo)
    }

    private var callbacks: Callbacks? = null

    fun setCallbacks(cb: Callbacks) {
        callbacks = cb
    }

    companion object {
        private const val ARG_PKG = "pkg"
        private const val ARG_LABEL = "label"
        private const val ARG_CLASS = "class"

        fun newInstance(app: AppInfo): AppActionsBottomSheet {
            val f = AppActionsBottomSheet()
            f.arguments = Bundle().apply {
                putString(ARG_PKG, app.packageName)
                putString(ARG_LABEL, app.label)
                putString(ARG_CLASS, app.className)
            }
            return f
        }
    }

    private fun requirePkg(): String = requireArguments().getString(ARG_PKG).orEmpty()
    private fun requireLabel(): String = requireArguments().getString(ARG_LABEL).orEmpty()
    private fun requireClass(): String = requireArguments().getString(ARG_CLASS).orEmpty()

    override fun getTheme(): Int = R.style.ThemeOverlay_Aura_BottomSheet

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.bottomsheet_app_actions, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val pkg = requirePkg()
        val label = requireLabel()

        val icon = view.findViewById<ImageView>(R.id.bsIcon)
        val title = view.findViewById<TextView>(R.id.bsTitle)
        val subtitle = view.findViewById<TextView>(R.id.bsSubtitle)

        val btnOpen = view.findViewById<MaterialButton>(R.id.bsOpen)
        val btnInfo = view.findViewById<MaterialButton>(R.id.bsInfo)
        val btnUninstall = view.findViewById<MaterialButton>(R.id.bsUninstall)
        val btnFav = view.findViewById<MaterialButton>(R.id.bsFavorite)
        val btnHide = view.findViewById<MaterialButton>(R.id.bsHide)

        title.text = label.ifBlank { pkg }
        subtitle.text = pkg

        // Icon
        try {
            val d = requireContext().packageManager.getApplicationIcon(pkg)
            icon.setImageDrawable(d)
        } catch (_: Throwable) {
            icon.setImageResource(android.R.drawable.sym_def_app_icon)
        }

        fun refreshButtons() {
            val fav = AppPrefs.isFavorite(requireContext(), pkg)
            val hid = AppPrefs.isHidden(requireContext(), pkg)

            btnFav.text = if (fav) getString(R.string.aura_remove_favorite) else getString(R.string.aura_add_favorite)
            btnHide.text = if (hid) getString(R.string.aura_show_app) else getString(R.string.aura_hide_app)

            // kui on hidden, siis Open nupp jääb alles (soovi korral)
            btnOpen.isVisible = true
        }

        refreshButtons()

        btnOpen.setOnClickListener {
            callbacks?.onLaunch(AppInfo(pkg, requireClass(), label, null))
            dismiss()
        }

        btnInfo.setOnClickListener {
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.parse("package:$pkg")
            }
            startActivity(intent)
        }

        btnUninstall.setOnClickListener {
            val intent = Intent(Intent.ACTION_DELETE).apply {
                data = Uri.parse("package:$pkg")
            }
            startActivity(intent)
        }

        btnFav.setOnClickListener {
            AppPrefs.toggleFavorite(requireContext(), pkg)
            refreshButtons()
            callbacks?.onRequestRefresh()
        }

        btnHide.setOnClickListener {
            AppPrefs.toggleHidden(requireContext(), pkg)
            refreshButtons()
            callbacks?.onRequestRefresh()
            Toast.makeText(requireContext(), getString(R.string.aura_done), Toast.LENGTH_SHORT).show()
        }
    }
}
