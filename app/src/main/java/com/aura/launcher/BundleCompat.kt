package com.aura.launcher

import android.os.Build
import android.os.Bundle
import android.os.Parcelable

@Suppress("DEPRECATION")
inline fun <reified T : Parcelable> Bundle.getParcelableCompat(key: String): T? {
    return if (Build.VERSION.SDK_INT >= 33) getParcelable(key, T::class.java) else getParcelable(key)
}

@Suppress("DEPRECATION")
fun <T : Parcelable> Bundle.putParcelableCompat(key: String, value: T) {
    putParcelable(key, value)
}
