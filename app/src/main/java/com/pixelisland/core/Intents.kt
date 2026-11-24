package com.pixelisland.core

import android.app.ActivityOptions
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build

/** Отправка PendingIntent с разрешением на запуск activity из фона (Android 14+). */
fun Context.sendPending(pi: PendingIntent?): Boolean {
    if (pi == null) return false
    return runCatching {
        val opts = ActivityOptions.makeBasic()
        if (Build.VERSION.SDK_INT >= 34) {
            @Suppress("DEPRECATION")
            opts.setPendingIntentBackgroundActivityStartMode(ActivityOptions.MODE_BACKGROUND_ACTIVITY_START_ALLOWED)
        }
        pi.send(this, 0, null, null, null, null, opts.toBundle())
        true
    }.getOrDefault(false)
}

fun Context.launchPackage(pkg: String) {
    packageManager.getLaunchIntentForPackage(pkg)?.let {
        it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        runCatching { startActivity(it) }
    }
}
