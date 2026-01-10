package com.aura.launcher

import android.graphics.drawable.Drawable
import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class AppInfo(
    val packageName: String,
    val className: String,
    val label: String,
    @kotlinx.parcelize.IgnoredOnParcel
    val icon: Drawable? = null
) : Parcelable
