package com.aura.launcher

import android.graphics.drawable.Drawable

data class AppInfo(
    val packageName: String,
    val className: String,
    val label: String,
    val icon: Drawable? = null,
    val isFavorite: Boolean = false,
    val isHidden: Boolean = false
)
