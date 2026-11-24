package com.pixelisland.core

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.SystemClock
import android.provider.Settings
import android.service.notification.NotificationListenerService
import com.pixelisland.service.IslandAccessibilityService
import com.pixelisland.service.MediaListenerService
import com.pixelisland.settings.SettingsStore

/**
 * Сторож: раз в ~15 минут проверяет, что оба сервиса живы.
 * Слушатель уведомлений просим переподключиться сами, про сервис доступности — сообщаем уведомлением.
 */
object Watchdog {
    private const val CHANNEL = "watchdog"
    private const val NOTIF_ID = 4242

    fun schedule(c: Context) {
        val am = c.getSystemService(AlarmManager::class.java) ?: return
        val pi = PendingIntent.getBroadcast(c, 1, Intent(c, WatchdogReceiver::class.java), PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
        am.setInexactRepeating(AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime() + AlarmManager.INTERVAL_FIFTEEN_MINUTES,
            AlarmManager.INTERVAL_FIFTEEN_MINUTES, pi)
    }

    fun a11yEnabled(c: Context): Boolean {
        val enabled = Settings.Secure.getString(c.contentResolver, Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES) ?: return false
        val me = ComponentName(c, IslandAccessibilityService::class.java)
        return enabled.split(':').any { ComponentName.unflattenFromString(it) == me }
    }

    fun listenerEnabled(c: Context) = androidx.core.app.NotificationManagerCompat.getEnabledListenerPackages(c).contains(c.packageName)

    /** Что сейчас не так (для главного экрана и уведомления). */
    fun problems(c: Context): List<String> = buildList {
        if (!a11yEnabled(c)) add("Сервис острова выключен в спецвозможностях")
        else if (!IslandRuntime.serviceAlive.value) add("Сервис острова включён, но не отвечает")
        if (!listenerEnabled(c)) add("Нет доступа к уведомлениям")
        else if (!IslandRuntime.listenerAlive.value) add("Слушатель уведомлений отключён системой")
    }

    fun check(c: Context) {
        SettingsStore.init(c)
        val p = c.getSharedPreferences("watchdog", Context.MODE_PRIVATE)
        if (listenerEnabled(c) && !IslandRuntime.listenerAlive.value) {
            runCatching { NotificationListenerService.requestRebind(ComponentName(c, MediaListenerService::class.java)) }
        }
        val on = a11yEnabled(c)
        val was = p.getBoolean("a11yWas", false)
        p.edit().putBoolean("a11yWas", on || was && SettingsStore.value.enabled).apply()
        val nm = c.getSystemService(NotificationManager::class.java)
        // Сообщаем один раз за «падение», а не каждые 15 минут.
        if (SettingsStore.value.enabled && was && !on) {
            if (!p.getBoolean("notified", false)) {
                notify(c, com.pixelisland.ui.i18n.tr("Остров остановлен"), com.pixelisland.ui.i18n.tr("Android выключил сервис. Нажмите, чтобы включить снова."))
                p.edit().putBoolean("notified", true).apply()
            }
        } else if (on) { nm.cancel(NOTIF_ID); p.edit().putBoolean("notified", false).apply() }
    }

    private fun notify(c: Context, title: String, text: String) {
        val nm = c.getSystemService(NotificationManager::class.java)
        nm.createNotificationChannel(NotificationChannel(CHANNEL, "Состояние острова", NotificationManager.IMPORTANCE_DEFAULT))
        val i = Intent("android.settings.ACCESSIBILITY_DETAILS_SETTINGS")
            .putExtra(Intent.EXTRA_COMPONENT_NAME, ComponentName(c, IslandAccessibilityService::class.java).flattenToString())
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        val pi = PendingIntent.getActivity(c, 2, i, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
        val n = android.app.Notification.Builder(c, CHANNEL)
            .setSmallIcon(com.pixelisland.R.drawable.ic_tile)
            .setContentTitle(title).setContentText(text)
            .setContentIntent(pi).setAutoCancel(true)
            .build()
        runCatching { nm.notify(NOTIF_ID, n) }
    }
}

class WatchdogReceiver : BroadcastReceiver() {
    override fun onReceive(c: Context, i: Intent) {
        if (i.action == Intent.ACTION_BOOT_COMPLETED || i.action == Intent.ACTION_MY_PACKAGE_REPLACED) Watchdog.schedule(c)
        Watchdog.check(c)
    }
}
