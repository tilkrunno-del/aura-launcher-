package com.aura.launcher

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialog

class AppActionsBottomSheet(
    context: Context,
    private val app: AppInfo
) {

    private val dialog = BottomSheetDialog(context)

    fun show() {
        val view = LayoutInflater.from(dialog.context)
            .inflate(R.layout.sheet_app_actions, null)

        view.findViewById<View>(R.id.action_open)?.setOnClickListener {
            LauncherUtils.launchApp(dialog.context, app)
            dialog.dismiss()
        }

        dialog.setContentView(view)
        dialog.show()
    }
}
