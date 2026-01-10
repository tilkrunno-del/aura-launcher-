package com.aura.launcher

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class AppActionsBottomSheet : BottomSheetDialogFragment() {

    interface Callback {
        fun onOpen(app: AppInfo)
        fun onAppInfo(app: AppInfo)
        fun onToggleFavorite(app: AppInfo, makeFavorite: Boolean)
        fun onToggleHidden(app: AppInfo, makeHidden: Boolean)
        fun onUninstall(app: AppInfo)
    }

    private var callback: Callback? = null
    private lateinit var app: AppInfo

    override fun getTheme(): Int = R.style.ThemeOverlay_Aura_BottomSheet

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callback = when {
            parentFragment is Callback -> parentFragment as Callback
            context is Callback -> context
            else -> null
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        app = requireArguments().getParcelableCompat(ARG_APP)
            ?: error("AppInfo missing")
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val v = inflater.inflate(R.layout.bottomsheet_app_actions, container, false)

        val title: TextView = v.findViewById(R.id.bsTitle)
        val sub: TextView = v.findViewById(R.id.bsSubtitle)

        val btnOpen: Button = v.findViewById(R.id.btnOpen)
        val btnInfo: Button = v.findViewById(R.id.btnAppInfo)
        val btnFav: Button = v.findViewById(R.id.btnFavorite)
        val btnHide: Button = v.findViewById(R.id.btnHideShow)
        val btnUninstall: Button = v.findViewById(R.id.btnUninstall)

        title.text = app.label
        sub.text = "${app.packageName}\n${app.className}"

        val isFav = AppStateStore.isFavorite(requireContext(), app.packageName)
        val isHidden = AppStateStore.isHidden(requireContext(), app.packageName)

        btnFav.text = if (isFav) getString(R.string.aura_remove_favorite) else getString(R.string.aura_add_favorite)
        btnHide.text = if (isHidden) getString(R.string.aura_show_app) else getString(R.string.aura_hide_app)

        btnOpen.setOnClickListener { callback?.onOpen(app); dismiss() }
        btnInfo.setOnClickListener { callback?.onAppInfo(app); dismiss() }

        btnFav.setOnClickListener {
            callback?.onToggleFavorite(app, !isFav)
            dismiss()
        }

        btnHide.setOnClickListener {
            callback?.onToggleHidden(app, !isHidden)
            dismiss()
        }

        btnUninstall.setOnClickListener {
            callback?.onUninstall(app)
            dismiss()
        }

        return v
    }

    companion object {
        private const val ARG_APP = "arg_app"

        fun newInstance(app: AppInfo): AppActionsBottomSheet {
            return AppActionsBottomSheet().apply {
                arguments = Bundle().apply {
                    putParcelableCompat(ARG_APP, app)
                }
            }
        }
    }
}
