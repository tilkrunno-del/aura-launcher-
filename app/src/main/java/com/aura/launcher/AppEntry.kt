package com.aura.launcher

import android.graphics.drawable.Drawable

data class AppEntry(
    val label: String,
    val packageName: String,
    val className: String,
    val icon: Drawable
)
