package com.pixelisland.service

import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import com.pixelisland.media.MediaRepository
import com.pixelisland.settings.SettingsStore
import com.pixelisland.sources.NotificationSource

/** Доступ к медиасессиям и уведомлениям (звонки, таймеры, Live Updates). */
class MediaListenerService : NotificationListenerService() {
    override fun onListenerConnected() {
        SettingsStore.init(this)
        com.pixelisland.core.EventLog.init(this)
        com.pixelisland.core.IslandRuntime.listenerAlive.value = true
        MediaRepository.attach(applicationContext)
        runCatching { activeNotifications }.getOrNull()?.forEach {
            NotificationSource.onPosted(this, it, null)
        }
    }

    override fun onListenerDisconnected() { com.pixelisland.core.IslandRuntime.listenerAlive.value = false }

    override fun onNotificationPosted(sbn: StatusBarNotification, rankingMap: RankingMap?) {
        NotificationSource.onPosted(this, sbn, rankingMap)
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification) {
        NotificationSource.onRemoved(sbn)
    }
}
