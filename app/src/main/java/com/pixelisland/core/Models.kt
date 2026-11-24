package com.pixelisland.core

import android.app.PendingIntent
import android.graphics.Bitmap
import com.pixelisland.media.MediaState
import com.pixelisland.settings.EventType

/** Долгоживущая «активность» острова (аналог Live Activity). */
sealed interface LiveActivity {
    val id: String
    val priority: Int
    /** Пока этот ключ не изменится, смахнутая активность остаётся скрытой. */
    val dismissKey: String
    val type: EventType
}

data class MusicActivity(val media: MediaState) : LiveActivity {
    override val id = "music"
    override val priority = 30
    override val type get() = if (isVoice(media)) EventType.VOICE else EventType.MUSIC
    override val dismissKey get() = media.key
}

private val VOICE_PKGS = setOf("org.telegram.messenger", "org.telegram.messenger.web", "org.thunderdog.challegram", "com.whatsapp", "com.whatsapp.w4b", "com.viber.voip", "com.discord")
/** Голосовое: мессенджер и короткая запись. */
fun isVoice(m: MediaState) = m.packageName in VOICE_PKGS && m.durationMs in 1..600_000

enum class OngoingKind { CALL, TIMER, LIVE, PROGRESS, RECORDING, ALARM, DELIVERY, SCORE, NAV, TRANSFER, FOCUS }

data class NotifAction(val title: String, val intent: PendingIntent?, val remoteInputs: Array<android.app.RemoteInput>? = null)

data class OngoingActivity(
    override val id: String,
    val kind: OngoingKind,
    val packageName: String,
    val appLabel: String,
    val title: String,
    val text: String,
    val shortText: String?,
    val smallIcon: Bitmap?,
    val largeIcon: Bitmap?,
    val accent: Int?,
    val chronoBase: Long?,     // System.currentTimeMillis()-основа хронометра
    val countDown: Boolean,
    val progress: Float?,      // 0..1
    val contentIntent: PendingIntent?,
    val actions: List<NotifAction>,
    val subText: String = "",
    val incoming: Boolean = false,   // входящий звонок (ещё не принят)
) : LiveActivity {
    override val priority = when (kind) {
        OngoingKind.ALARM -> 110; OngoingKind.CALL -> 100; OngoingKind.RECORDING -> 90
        OngoingKind.TIMER -> 60; OngoingKind.DELIVERY -> 55; OngoingKind.SCORE -> 52
        OngoingKind.LIVE -> 50; OngoingKind.PROGRESS -> 20
        OngoingKind.NAV -> 70; OngoingKind.TRANSFER -> 25; OngoingKind.FOCUS -> 4
    }.let { if (incoming) 120 else it }
    override val dismissKey get() = "$id|$title"
    override val type get() = when (kind) {
        OngoingKind.CALL -> EventType.CALL; OngoingKind.TIMER -> EventType.TIMER
        OngoingKind.LIVE -> EventType.LIVE_UPDATE; OngoingKind.PROGRESS -> EventType.PROGRESS
        OngoingKind.RECORDING -> EventType.RECORDING; OngoingKind.ALARM -> EventType.ALARM_RING
        OngoingKind.DELIVERY -> EventType.DELIVERY; OngoingKind.SCORE -> EventType.SCORE
        OngoingKind.NAV -> EventType.NAVIGATION; OngoingKind.TRANSFER -> EventType.TRANSFER
        OngoingKind.FOCUS -> EventType.FOCUS
    }
}

data class TorchActivity(val cameraId: String, val strength: Int, val maxStrength: Int) : LiveActivity {
    override val id = "torch"
    override val priority = 40
    override val dismissKey = "torch"
    override val type get() = EventType.TORCH_ON
}

data class ChargingActivity(val level: Int, val wireless: Boolean, val fullInMs: Long) : LiveActivity {
    override val id = "charging"
    override val priority = 10
    override val dismissKey = "charging"
    override val type get() = EventType.CHARGING_LIVE
}

/** Демо-активность для экрана настроек. */
data class PreviewActivity(val playing: Boolean, val track: Int) : LiveActivity {
    override val id = "preview"
    override val priority = 1000
    override val dismissKey = "preview"
    override val type get() = EventType.MUSIC
}

/** Камера или микрофон заняты. */
data class PrivacyActivity(override val type: EventType) : LiveActivity {
    override val id = type.name
    override val priority = 5
    override val dismissKey = type.name
}

/** Скоро сработает будильник. */
data class AlarmSoonActivity(val triggerAt: Long, val label: String) : LiveActivity {
    override val id = "alarm_soon"
    override val priority = 15
    override val dismissKey = "alarm_soon|$triggerAt"
    override val type get() = EventType.ALARM_SOON
}

/** Скоро событие календаря. */
data class CalendarActivity(val title: String, val begin: Long) : LiveActivity {
    override val id = "calendar"
    override val priority = 16
    override val dismissKey = "calendar|$title|$begin"
    override val type get() = EventType.CALENDAR
}

enum class TimerMode { TIMER, STOPWATCH, POMODORO }

/** Таймер / секундомер / помодоро, запущенный прямо из острова. */
data class OwnTimerActivity(
    val mode: TimerMode,
    val running: Boolean,
    val anchor: Long,        // TIMER/POMODORO: момент окончания; STOPWATCH: момент старта (System.currentTimeMillis)
    val frozenMs: Long,      // остаток (или прошедшее время) на паузе
    val totalMs: Long,       // длительность текущего отрезка (для кольца)
    val work: Boolean = true,
    val round: Int = 1,
) : LiveActivity {
    override val id = "own_timer"
    override val priority = 65
    override val dismissKey = "own_timer"
    override val type get() = EventType.OWN_TIMER
    val title get() = when (mode) {
        TimerMode.TIMER -> "Таймер"; TimerMode.STOPWATCH -> "Секундомер"
        TimerMode.POMODORO -> if (work) "Работа · $round" else "Перерыв"
    }
}

/** Быстрые действия при раскрытии пустой пилюли. */
data object QuickActivity : LiveActivity {
    override val id = "quick"
    override val priority = 0
    override val dismissKey = "quick"
    override val type get() = EventType.NOTIF_PEEK
}

/** Все активные события списком (удержание). */
data object StackActivity : LiveActivity {
    override val id = "stack"
    override val priority = 0
    override val dismissKey = "stack"
    override val type get() = EventType.NOTIF_PEEK
}

/** Полка: что бросили на остров. */
data class ShelfItem(val id: Long, val text: String?, val uri: android.net.Uri?, val mime: String?, val label: String, val thumb: Bitmap? = null)

data class ShelfActivity(val items: List<ShelfItem>) : LiveActivity {
    override val id = "shelf"
    override val priority = 3
    override val dismissKey = "shelf|${items.size}"
    override val type get() = EventType.SHELF
}

/** Временная демо-активность из редактора событий. */
data class DemoActivity(override val type: EventType) : LiveActivity {
    override val id = "demo"
    override val priority = 2000
    override val dismissKey = "demo"
}

/** Короткое всплывающее событие в форме вытянутой пилюли. */
data class ToastEvent(
    val id: String,
    val type: EventType,
    val title: String,
    val value: String = "",
    val ring: Float? = null,       // 0..1 — кольцо (заряд)
    val iconName: String? = null,  // иконка из каталога вместо стандартной (погода)
)

/** Всплывающее уведомление в форме карточки. */
data class PeekEvent(
    val id: String,
    val appLabel: String,
    val appIcon: Bitmap?,
    val title: String,
    val text: String,
    val largeIcon: Bitmap?,
    val accent: Int?,
    val contentIntent: PendingIntent?,
    val type: EventType = EventType.NOTIF_PEEK,
    val reply: NotifAction? = null,
    val onTap: (() -> Unit)? = null,   // своё действие по тапу (например, автоповорот)
    val actionLabel: String? = null,
)

sealed interface Presentation {
    data object Hidden : Presentation
    data class Compact(val activity: LiveActivity?, val index: Int, val count: Int) : Presentation
    data class Expanded(val activity: LiveActivity, val index: Int, val count: Int) : Presentation
    data class Toast(val event: ToastEvent) : Presentation
    data class Peek(val event: PeekEvent) : Presentation
    data object Calibrate : Presentation
}

/** Нужен ли большой (на всю ширину) оверлей. */
val Presentation.isLarge get() = this is Presentation.Expanded || this is Presentation.Toast || this is Presentation.Peek || this is Presentation.Calibrate
val Presentation.touchable get() = this is Presentation.Compact || this is Presentation.Expanded || this is Presentation.Peek
