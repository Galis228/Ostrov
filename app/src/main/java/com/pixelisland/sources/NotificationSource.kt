package com.pixelisland.sources

import android.app.Notification
import android.app.NotificationManager
import android.content.Context
import android.graphics.Bitmap
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.os.SystemClock
import androidx.core.graphics.drawable.toBitmap
import com.pixelisland.core.IslandController
import com.pixelisland.core.NotifAction
import com.pixelisland.core.OngoingActivity
import com.pixelisland.core.OngoingKind
import com.pixelisland.core.PeekEvent
import com.pixelisland.settings.SettingsStore

/** Звонки, таймеры, навигация, Live Updates и загрузки из уведомлений + «подглядывание» новых уведомлений. */
object NotificationSource {
    private const val FLAG_PROMOTED_ONGOING = 0x00040000
    private const val EXTRA_SHORT_CRITICAL_TEXT = "android.shortCriticalText"
    private val peekSeen = com.pixelisland.core.Lru<String, Int>(200)
    private val labelCache = mutableMapOf<String, String>()

    private val handler = android.os.Handler(android.os.Looper.getMainLooper())
    private val lastSet = com.pixelisland.core.Lru<String, Long>(64)
    private val pending = HashMap<String, Runnable>()
    private val iconCache = com.pixelisland.core.Lru<String, Bitmap>(48)

    fun idOf(sbn: StatusBarNotification) = "n:${sbn.key}"

    fun onPosted(ctx: Context, sbn: StatusBarNotification, ranking: NotificationListenerService.RankingMap?) {
        if (sbn.packageName == ctx.packageName) return
        if (sbn.packageName !in seenPackages.value) seenPackages.value = seenPackages.value + (sbn.packageName to label(ctx, sbn.packageName))
        if ("MediaStyle" in sbn.notification.extras.getString(Notification.EXTRA_TEMPLATE).orEmpty()) {
            // Обложку из уведомления берём, только если плеер не отдал её в метаданных.
            if (com.pixelisland.media.MediaRepository.wantsNotificationArt(sbn.packageName)) {
                com.pixelisland.media.MediaRepository.onMediaNotificationArt(sbn.packageName, sbn.notification.getLargeIcon()?.let { bmp(ctx, it, 300) })
            }
            return
        }
        trackMissed(sbn, true)
        if (maybeNowPlaying(ctx, sbn, ranking)) return
        val ongoing = classify(sbn)
        if (ongoing != null) {
            // Загрузки и навигация могут обновляться много раз в секунду — склеиваем до 1 раза в секунду.
            val id = idOf(sbn)
            val now = SystemClock.uptimeMillis()
            val last = lastSet[id] ?: 0L
            pending.remove(id)?.let(handler::removeCallbacks)
            if (now - last < 1000) {
                val r = Runnable {
                    pending.remove(id)
                    lastSet[id] = SystemClock.uptimeMillis()
                    IslandController.set(build(ctx, sbn, ongoing))
                }
                pending[id] = r
                handler.postAtTime(r, last + 1000)
            } else {
                lastSet[id] = now
                IslandController.set(build(ctx, sbn, ongoing))
            }
            return
        }
        IslandController.remove(idOf(sbn)) // уведомление перестало быть «живым»
        if (ranking != null && !sbn.isOngoing) maybeGlow(sbn, ranking)
        if (ranking != null && !sbn.isOngoing && maybePayment(ctx, sbn)) return
        maybePeek(ctx, sbn, ranking)
    }

    fun onRemoved(sbn: StatusBarNotification) {
        trackMissed(sbn, false)
        val id = idOf(sbn)
        pending.remove(id)?.let(handler::removeCallbacks)
        lastSet.remove(id)
        IslandController.remove(id)
        peekSeen.remove(sbn.key)
    }

    private fun classify(sbn: StatusBarNotification): OngoingKind? {
        val n = sbn.notification
        val ex = n.extras
        val template = ex.getString(Notification.EXTRA_TEMPLATE).orEmpty()
        if ("MediaStyle" in template) return null              // музыка идёт через медиасессии (обложку берём в onPosted)
        val callType = ex.getInt(EXTRA_CALL_TYPE, 0)
        if ((n.category == Notification.CATEGORY_CALL || "CallStyle" in template) &&
            (sbn.isOngoing || n.flags and Notification.FLAG_FOREGROUND_SERVICE != 0 || callType == 1 || n.fullScreenIntent != null)) return OngoingKind.CALL
        if (n.category == Notification.CATEGORY_ALARM && (sbn.isOngoing || n.fullScreenIntent != null)) return OngoingKind.ALARM
        if (!sbn.isOngoing) return null
        if (sbn.packageName == WELLBEING) return OngoingKind.FOCUS
        if (isScreenRecord(sbn)) return OngoingKind.RECORDING
        if (n.category == Notification.CATEGORY_NAVIGATION || (sbn.packageName in navPkgs && navDist.containsMatchIn(
                ex.getCharSequence(Notification.EXTRA_TITLE)?.toString().orEmpty() + " " + ex.getCharSequence(Notification.EXTRA_TEXT)?.toString().orEmpty()))) return OngoingKind.NAV
        val chrono = ex.getBoolean(Notification.EXTRA_SHOW_CHRONOMETER)
        if (chrono && isRecording(sbn)) return OngoingKind.RECORDING
        if (chrono) return OngoingKind.TIMER
        val live = n.flags and FLAG_PROMOTED_ONGOING != 0 || "ProgressStyle" in template || ex.getBoolean(EXTRA_REQUEST_PROMOTED)
        val text = (ex.getCharSequence(Notification.EXTRA_TITLE)?.toString().orEmpty() + " " +
            ex.getCharSequence(Notification.EXTRA_TEXT)?.toString().orEmpty())
        val hasProgress = ex.getInt(Notification.EXTRA_PROGRESS_MAX, 0) > 0
        if (hasProgress && isTransfer(sbn, text)) return OngoingKind.TRANSFER
        if ((live || hasProgress) && isScore(text)) return OngoingKind.SCORE
        if ((live || hasProgress) && (sbn.packageName in deliveryPkgs || deliveryWords.containsMatchIn(text))) return OngoingKind.DELIVERY
        if (live) return OngoingKind.LIVE
        val max = ex.getInt(Notification.EXTRA_PROGRESS_MAX, 0)
        if (max > 0 && !ex.getBoolean(Notification.EXTRA_PROGRESS_INDETERMINATE)) return OngoingKind.PROGRESS
        return null
    }

    private const val EXTRA_CALL_TYPE = "android.callType"
    private const val EXTRA_REQUEST_PROMOTED = "android.requestPromotedOngoing"
    private const val WELLBEING = "com.google.android.apps.wellbeing"
    private val navDist = Regex("\\d+[.,]?\\d*\\s?(м|км|m|km|ft|mi)\\b", RegexOption.IGNORE_CASE)
    private val navPkgs = setOf("com.google.android.apps.maps", "com.waze", "com.google.android.apps.mapslite", "ru.yandex.yandexnavi", "com.here.app.maps", "com.sygic.aura")
    private val transferPkgs = setOf("com.google.android.gms", "com.google.android.apps.nearby", "com.samsung.android.app.sharelive")
    private val transferWords = Regex("quick share|nearby|обмен|поблизост|поруч|получени|отправк|передач|sending|receiving|transfer", RegexOption.IGNORE_CASE)
    private fun isTransfer(sbn: StatusBarNotification, text: String): Boolean {
        val ch = sbn.notification.channelId.orEmpty().lowercase()
        return (sbn.packageName in transferPkgs && ("shar" in ch || "nearby" in ch || transferWords.containsMatchIn(text))) ||
            (sbn.packageName in transferPkgs && transferWords.containsMatchIn(text))
    }
    private fun isScreenRecord(sbn: StatusBarNotification): Boolean {
        if (sbn.packageName != "com.android.systemui") return false
        val ch = sbn.notification.channelId.orEmpty().lowercase()
        val title = sbn.notification.extras.getCharSequence(Notification.EXTRA_TITLE)?.toString().orEmpty().lowercase()
        return "screen_record" in ch || "screenrecord" in ch || "запис" in title && "экран" in title || "screen record" in title
    }

    // ---------- Now Playing (Pixel) ----------
    private const val NOW_PLAYING_PKG = "com.google.intelligence.sense"
    private var lastSong = ""
    private fun maybeNowPlaying(ctx: Context, sbn: StatusBarNotification, ranking: NotificationListenerService.RankingMap?): Boolean {
        if (sbn.packageName != NOW_PLAYING_PKG) return false
        if (ranking == null) return true
        val ex = sbn.notification.extras
        val title = ex.getCharSequence(Notification.EXTRA_TITLE)?.toString().orEmpty()
        val text = ex.getCharSequence(Notification.EXTRA_TEXT)?.toString().orEmpty()
        if (title.isBlank() || title == lastSong) return true
        lastSong = title
        // «Песня — Исполнитель» / «Song by Artist» в заголовке или исполнитель отдельной строкой.
        val m = Regex("^(.+?)\\s+(?:by|—|-|–|от|від)\\s+(.+)$").find(title)
        val song = m?.groupValues?.get(1) ?: title
        val artist = m?.groupValues?.get(2) ?: text
        IslandController.showToast(com.pixelisland.core.ToastEvent("nowplaying", com.pixelisland.settings.EventType.NOW_PLAYING, song, artist))
        return true
    }

    // ---------- Пропущенные звонки и сообщения ----------
    private val missedCalls = HashSet<String>()
    private val messages = HashSet<String>()
    private val missedWords = Regex("пропущен|missed call|пропущений", RegexOption.IGNORE_CASE)
    private fun trackMissed(sbn: StatusBarNotification, added: Boolean) {
        val n = sbn.notification
        if (n.flags and Notification.FLAG_GROUP_SUMMARY != 0) return
        val k = sbn.key
        if (!added) { if (missedCalls.remove(k) or messages.remove(k)) publishMissed(); return }
        val title = n.extras.getCharSequence(Notification.EXTRA_TITLE)?.toString().orEmpty()
        val missed = n.category == Notification.CATEGORY_MISSED_CALL || (n.category == null && missedWords.containsMatchIn(title))
        val msg = !missed && n.category == Notification.CATEGORY_MESSAGE && !sbn.isOngoing
        val changed = if (missed) missedCalls.add(k) else if (msg) messages.add(k) else false
        if (changed) publishMissed()
    }
    private fun publishMissed() { com.pixelisland.core.IslandRuntime.missed.value = missedCalls.size to messages.size }

    private val deliveryPkgs = setOf(
        "com.ubercab", "com.ubercab.eats", "ee.mtakso.client", "com.glovo", "com.wolt.android",
        "com.bolt.deliveryclient", "ua.uklon.client", "com.ubercab.driver",
    )
    private val deliveryWords = Regex("курьер|кур'єр|доставк|достав|такси|таксі|водител|водій|прибуд|приед|в пути|в дорозі|courier|driver|delivery|arriv|on the way", RegexOption.IGNORE_CASE)
    val scoreRegex = Regex("(.{2,32}?)\\s+(\\d{1,3})\\s*[-–:]\\s*(\\d{1,3})\\s+(.{2,32})")
    private fun isScore(t: String) = scoreRegex.containsMatchIn(t.trim())

    private val moneyWords = Regex("покупк|оплат|списан|зачислен|зарахуван|переказ|перевод|платеж|платіж|поповнен|пополнен|payment|purchase|paid|debit|credit|transfer|received", RegexOption.IGNORE_CASE)
    private val moneyRegex = Regex("([+\\-−–]?\\s?\\d{1,3}(?:[\\s\\u00a0.,]\\d{3})*(?:[.,]\\d{1,2})?)\\s?(₴|грн\\.?|UAH|₽|руб\\.?|RUB|USD|EUR|PLN|zł|\\$|€|£)|([\\$€£])\\s?(\\d[\\d,]*(?:\\.\\d{1,2})?)")

    /** Банк/магазин: сумма из уведомления → компактная всплывашка. */
    private fun maybePayment(ctx: Context, sbn: StatusBarNotification): Boolean {
        if (!SettingsStore.value.on(com.pixelisland.settings.EventType.PAYMENT)) return false
        val ex = sbn.notification.extras
        val text = ex.getCharSequence(Notification.EXTRA_TITLE)?.toString().orEmpty() + " " +
            (ex.getCharSequence(Notification.EXTRA_BIG_TEXT) ?: ex.getCharSequence(Notification.EXTRA_TEXT))?.toString().orEmpty()
        if (!moneyWords.containsMatchIn(text)) return false
        val m = moneyRegex.find(text) ?: return false
        val amount = m.value.trim().replace(Regex("\\s+"), " ")
        if (!SettingsStore.value.appShown(sbn.packageName)) return true
        IslandController.showToast(com.pixelisland.core.ToastEvent("pay:${sbn.key}", com.pixelisland.settings.EventType.PAYMENT, label(ctx, sbn.packageName), amount))
        return true
    }

    private val recorderPkgs = setOf("com.google.android.apps.recorder", "com.android.soundrecorder", "com.sec.android.app.voicenote")

    /** Запись экрана (SystemUI) и диктофоны: по пакету, каналу или заголовку. */
    private fun isRecording(sbn: StatusBarNotification): Boolean {
        if (sbn.packageName in recorderPkgs) return true
        val ch = sbn.notification.channelId.orEmpty().lowercase()
        val title = sbn.notification.extras.getCharSequence(Notification.EXTRA_TITLE)?.toString().orEmpty().lowercase()
        return "record" in ch || "запис" in title || "record" in title
    }

    /** Пакеты, присылавшие уведомления, — для фильтра в настройках. */
    val seenPackages = kotlinx.coroutines.flow.MutableStateFlow<Map<String, String>>(emptyMap())

    private fun build(ctx: Context, sbn: StatusBarNotification, kind: OngoingKind): OngoingActivity {
        val n = sbn.notification
        val ex = n.extras
        val max = ex.getInt(Notification.EXTRA_PROGRESS_MAX, 0)
        val progress = if (max > 0 && !ex.getBoolean(Notification.EXTRA_PROGRESS_INDETERMINATE))
            ex.getInt(Notification.EXTRA_PROGRESS, 0).toFloat() / max else null
        val chrono = ex.getBoolean(Notification.EXTRA_SHOW_CHRONOMETER) ||
            (kind == OngoingKind.CALL && n.`when` > 0 && n.`when` <= System.currentTimeMillis())
        return OngoingActivity(
            id = idOf(sbn),
            kind = kind,
            packageName = sbn.packageName,
            appLabel = label(ctx, sbn.packageName),
            title = ex.getCharSequence(Notification.EXTRA_TITLE)?.toString().orEmpty(),
            text = (ex.getCharSequence(Notification.EXTRA_TEXT) ?: ex.getCharSequence(Notification.EXTRA_SUB_TEXT))?.toString().orEmpty(),
            shortText = ex.getCharSequence(EXTRA_SHORT_CRITICAL_TEXT)?.toString(),
            smallIcon = n.smallIcon?.let { bmp(ctx, it, 64) },
            largeIcon = n.getLargeIcon()?.let { bmp(ctx, it, 160) },
            accent = n.color.takeIf { it != Notification.COLOR_DEFAULT && it != 0 },
            chronoBase = if (chrono) n.`when` else null,
            countDown = ex.getBoolean(Notification.EXTRA_CHRONOMETER_COUNT_DOWN),
            progress = progress,
            contentIntent = n.contentIntent,
            actions = n.actions.orEmpty().take(3).map { NotifAction(it.title?.toString().orEmpty(), it.actionIntent) },
            subText = ex.getCharSequence(Notification.EXTRA_SUB_TEXT)?.toString().orEmpty(),
            incoming = kind == OngoingKind.CALL && (ex.getInt(EXTRA_CALL_TYPE, 0) == 1 ||
                (ex.getInt(EXTRA_CALL_TYPE, 0) == 0 && n.fullScreenIntent != null && !ex.getBoolean(Notification.EXTRA_SHOW_CHRONOMETER))),
        )
    }

    private val glowSeen = com.pixelisland.core.Lru<String, Boolean>(100)
    /** Свечение вокруг камеры: на экране блокировки (или всегда) при новом уведомлении. */
    private fun maybeGlow(sbn: StatusBarNotification, rankingMap: NotificationListenerService.RankingMap) {
        val st = SettingsStore.value
        if (!st.notifyGlow || !st.appShown(sbn.packageName)) return
        if (!st.glowAlways && !com.pixelisland.core.IslandRuntime.locked.value) return
        if (sbn.notification.flags and Notification.FLAG_GROUP_SUMMARY != 0) return
        if (glowSeen[sbn.key] == true) return
        val r = NotificationListenerService.Ranking()
        if (rankingMap.getRanking(sbn.key, r) && (r.importance < NotificationManager.IMPORTANCE_DEFAULT || !r.matchesInterruptionFilter())) return
        glowSeen[sbn.key] = true
        val color = st.rule(sbn.packageName)?.color?.takeIf { it != 0L }?.toInt()
            ?: sbn.notification.color.takeIf { it != 0 && it != Notification.COLOR_DEFAULT } ?: 0xFF6EA8FF.toInt()
        com.pixelisland.core.IslandRuntime.glow.value = color to System.currentTimeMillis()
    }

    private fun maybePeek(ctx: Context, sbn: StatusBarNotification, rankingMap: NotificationListenerService.RankingMap?) {
        if (!SettingsStore.value.on(com.pixelisland.settings.EventType.NOTIF_PEEK) || rankingMap == null) return // null = стартовый проход, не спамим
        if (!SettingsStore.value.appShown(sbn.packageName)) return
        val n = sbn.notification
        if (n.flags and Notification.FLAG_GROUP_SUMMARY != 0) return
        val ex = n.extras
        val title = ex.getCharSequence(Notification.EXTRA_TITLE)?.toString().orEmpty()
        val text = (ex.getCharSequence(Notification.EXTRA_BIG_TEXT) ?: ex.getCharSequence(Notification.EXTRA_TEXT))?.toString().orEmpty()
        if (title.isBlank()) return
        val r = NotificationListenerService.Ranking()
        if (rankingMap?.getRanking(sbn.key, r) == true) {
            if (r.importance < NotificationManager.IMPORTANCE_DEFAULT || !r.matchesInterruptionFilter()) return
        }
        val hash = (title + text).hashCode()
        if (peekSeen[sbn.key] == hash) return
        peekSeen[sbn.key] = hash
        IslandController.showToast(
            PeekEvent(
                id = "peek:${sbn.key}",
                appLabel = label(ctx, sbn.packageName),
                appIcon = n.smallIcon?.let { bmp(ctx, it, 64) },
                title = title, text = text,
                largeIcon = n.getLargeIcon()?.let { bmp(ctx, it, 160) },
                accent = SettingsStore.value.rule(sbn.packageName)?.color?.takeIf { it != 0L }?.toInt() ?: n.color.takeIf { it != 0 },
                contentIntent = n.contentIntent,
                reply = n.actions?.firstOrNull { it.remoteInputs?.isNotEmpty() == true }?.let {
                    NotifAction(it.title?.toString().orEmpty(), it.actionIntent, it.remoteInputs)
                },
            ),
        )
    }

    private fun bmp(ctx: Context, icon: android.graphics.drawable.Icon, size: Int): Bitmap? {
        // Иконки-ресурсы одинаковы от раза к разу — не декодируем их заново при каждом обновлении.
        val key = if (icon.type == android.graphics.drawable.Icon.TYPE_RESOURCE) "${icon.resPackage}:${icon.resId}:$size" else null
        key?.let { k -> iconCache[k]?.let { return it } }
        val b = runCatching { icon.loadDrawable(ctx)?.toBitmap(size, size) }.getOrNull()
        if (key != null && b != null) iconCache[key] = b
        return b
    }

    private fun label(ctx: Context, pkg: String) = labelCache.getOrPut(pkg) {
        runCatching {
            val pm = ctx.packageManager
            pm.getApplicationLabel(pm.getApplicationInfo(pkg, 0)).toString()
        }.getOrDefault(pkg)
    }
}
