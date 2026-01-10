package com.aura.launcher

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import com.google.android.material.bottomsheet.BottomSheetDialog

class AppActionsBottomSheet(
    context: Context,
    private val app: AppInfo,
    private val onOpen: (AppInfo) -> Unit
) {

    private val dialog = BottomSheetDialog(context)

    init {
        val view = LayoutInflater.from(context)
            .inflate(R.layout.sheet_app_actions, null, false)

        dialog.setContentView(view)

        val openAction = view.findViewById<TextView>(R.id.action_open)
        val title = view.findViewById<TextView>(R.id.appTitle)

        title.text = app.label

        openAction.setOnClickListener {
            dialog.dismiss()
            onOpen(app)
        }
    }

    fun show() {
        dialog.show()
    }
}
