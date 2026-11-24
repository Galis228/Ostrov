package com.pixelisland.ui.content

import android.os.SystemClock
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.Call
import androidx.compose.material.icons.rounded.FlashlightOn
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.SkipNext
import androidx.compose.material.icons.rounded.SkipPrevious
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.material.icons.rounded.AvTimer
import androidx.compose.material.icons.rounded.SelfImprovement
import androidx.compose.material.icons.rounded.LocalShipping
import androidx.compose.material.icons.rounded.Flag
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.offset
import com.pixelisland.ui.i18n.Text
import com.pixelisland.sources.NotificationSource
import androidx.compose.material.icons.rounded.DirectionsWalk
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pixelisland.core.ChargingActivity
import com.pixelisland.core.IslandController
import com.pixelisland.core.LiveActivity
import com.pixelisland.core.MusicActivity
import com.pixelisland.core.OngoingActivity
import com.pixelisland.core.OngoingKind
import com.pixelisland.core.PeekEvent
import com.pixelisland.core.PreviewActivity
import com.pixelisland.core.ToastEvent
import com.pixelisland.core.TorchActivity
import com.pixelisland.core.sendPending
import com.pixelisland.media.IslandActions
import com.pixelisland.media.MediaRepository
import com.pixelisland.media.MediaState
import com.pixelisland.settings.SettingsStore
import com.pixelisland.sources.SystemEvents
import com.pixelisland.sources.Tints
import com.pixelisland.ui.IslandDims
import com.pixelisland.ui.LocalIslandColors
import com.pixelisland.ui.iconFor
import androidx.compose.ui.draw.drawBehind
import com.pixelisland.core.DemoActivity
import com.pixelisland.core.PrivacyActivity
import com.pixelisland.core.AlarmSoonActivity
import com.pixelisland.core.QuickActivity
import androidx.compose.material.icons.rounded.Vibration
import androidx.compose.material.icons.rounded.Alarm
import androidx.compose.material.icons.rounded.PhotoCamera
import androidx.compose.material.icons.rounded.Wifi
import androidx.compose.material.icons.rounded.Bluetooth
import androidx.compose.material.icons.rounded.BatteryFull
import androidx.compose.material.icons.rounded.Headphones
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.PhoneMissed
import androidx.compose.material.icons.rounded.Chat
import androidx.compose.material.icons.rounded.Inventory2
import androidx.compose.material.icons.rounded.Screenshot
import androidx.compose.material.icons.rounded.VisibilityOff
import androidx.compose.material.icons.rounded.Send
import androidx.compose.runtime.collectAsState

import com.pixelisland.settings.EventType
import kotlinx.coroutines.delay

internal val CallGreen = Color(Tints.GREEN)
internal val TorchYellow = Color(Tints.YELLOW)

// ---------------- Демо-трек для настроек ----------------
private val demoTracks = listOf("Midnight City" to "M83", "Electric Feel" to "MGMT", "Breathe" to "Télépopmusik")

internal fun previewMedia(p: PreviewActivity): MediaState {
    val (t, a) = demoTracks[p.track.mod(demoTracks.size)]
    return MediaState(
        key = "preview${p.track}", packageName = "", appLabel = "Предпросмотр", title = t, artist = a,
        art = null, accent = null, isPlaying = p.playing, durationMs = 243_000, positionMs = 81_000,
        positionUpdateTime = SystemClock.elapsedRealtime(), speed = 1f, canSeek = false,
    )
}

internal object PreviewActions : IslandActions {
    override fun playPause() = IslandController.previewPlayPause()
    override fun next() = IslandController.previewSkip(1)
    override fun previous() = IslandController.previewSkip(-1)
    override fun seekTo(ms: Long) = Unit
    override fun openApp() = Unit
    override fun dismiss() = Unit
}

@Composable
internal fun accentOf(media: MediaState): Color {
    val c = LocalIslandColors.current
    val st = SettingsStore.value.style(EventType.MUSIC)
    return c.resolve(st.color, media.accent?.let { Color(it) } ?: c.accent)
}

// ======================= КОМПАКТНЫЙ ВИД =======================

/** Пустая пилюля: шаги, заряд, наушники или время. */
@Composable
private fun IdleContentView(dims: IslandDims, width: Dp) {
    val s = SettingsStore.value
    val c = LocalIslandColors.current
    val steps by com.pixelisland.core.IslandRuntime.steps.collectAsState()
    val bat by com.pixelisland.core.IslandRuntime.battery.collectAsState()
    val hp by com.pixelisland.core.IslandRuntime.headsetBattery.collectAsState()
    val (icon, value) = when (s.idleContent) {
        com.pixelisland.settings.IdleContent.EMPTY -> { Box(Modifier.size(width, dims.compactH)); return }
        com.pixelisland.settings.IdleContent.STEPS -> Icons.Rounded.DirectionsWalk to (steps?.let { formatSteps(it) } ?: "—")
        com.pixelisland.settings.IdleContent.BATTERY -> Icons.Rounded.BatteryFull to "$bat%"
        com.pixelisland.settings.IdleContent.HEADSET -> Icons.Rounded.Headphones to (hp?.let { "$it%" } ?: "—")
        com.pixelisland.settings.IdleContent.MISSED -> {
            val m by com.pixelisland.core.IslandRuntime.missed.collectAsState()
            if (m.first == 0 && m.second == 0) { Box(Modifier.size(width, dims.compactH)); return }
            CompactRow(dims, width,
                left = { if (m.first > 0) MissedBadge(Icons.Rounded.PhoneMissed, m.first, Color(Tints.RED), dims) },
                right = { if (m.second > 0) MissedBadge(Icons.Rounded.Chat, m.second, c.accent, dims) },
            )
            return
        }
        com.pixelisland.settings.IdleContent.RATE -> {
            val r by com.pixelisland.core.IslandRuntime.rate.collectAsState()
            LaunchedEffect(s.rateKind) { com.pixelisland.sources.Extras.refreshRate() }
            CompactRow(dims, width,
                left = { IslandText(s.rateKind.short, 13.sp, FontWeight.Bold, color = c.accent) },
                right = { IslandText(r ?: "…", 12.sp, FontWeight.SemiBold, dim = true) },
            )
            return
        }
        com.pixelisland.settings.IdleContent.NET_SPEED -> {
            val (rx, tx) = rememberNetSpeed()
            CompactRow(dims, width,
                left = { IslandText("↓ " + formatSpeed(rx), 11.sp, FontWeight.SemiBold, dim = true) },
                right = { IslandText("↑ " + formatSpeed(tx), 11.sp, FontWeight.SemiBold, dim = true) },
            )
            return
        }
        com.pixelisland.settings.IdleContent.TIME -> {
            val now = rememberWallClock(1000)
            Icons.Rounded.Schedule to java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault()).format(java.util.Date(now))
        }
    }
    CompactRow(dims, width,
        left = { Icon(icon, null, tint = c.dim, modifier = Modifier.size(dims.slot * 0.7f)) },
        right = { IslandText(value, 12.sp, FontWeight.SemiBold, dim = true) },
    )
}

@Composable
private fun MissedBadge(icon: androidx.compose.ui.graphics.vector.ImageVector, n: Int, color: Color, dims: IslandDims) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, tint = color, modifier = Modifier.size(dims.slot * 0.62f))
        Spacer(Modifier.width(4.dp))
        IslandText("$n", 12.sp, FontWeight.Bold, color = color)
    }
}

private fun scoreText(a: OngoingActivity): String {
    val m = NotificationSource.scoreRegex.find(a.title) ?: NotificationSource.scoreRegex.find(a.text) ?: return a.shortText ?: ""
    return "${m.groupValues[2]}:${m.groupValues[3]}"
}

private fun formatSteps(n: Int) = if (n >= 10_000) "%.1fK".format(n / 1000f) else "$n"

/** Данные для слотов из любой живой активности. */
@Composable
fun slotDataFor(a: LiveActivity): SlotData {
    val base = slotDataRaw(a)
    // Правило приложения: свой цвет и своя иконка.
    val r = SettingsStore.value.rule(IslandController.pkgOf(a)) ?: return base
    val st = SettingsStore.value.style(a.type)
    return base.copy(
        auto = if (r.color != 0L) Color(r.color) else base.auto,
        vector = if (r.icon.isNotEmpty() && st.icon.isEmpty()) com.pixelisland.ui.catalogIcon(r.icon, st.variant) ?: base.vector else base.vector,
        tintedIcon = if (r.icon.isNotEmpty()) null else base.tintedIcon,
    )
}

@Composable
private fun slotDataRaw(a: LiveActivity): SlotData {
    val st = SettingsStore.value.style(a.type)
    val v = iconFor(a.type, st.variant, st.icon)
    val auto = Color(a.type.tint)
    return when (a) {
        is MusicActivity -> musicSlot(a.media, v)
        is PreviewActivity -> musicSlot(previewMedia(a), v)
        is OngoingActivity -> SlotData(
            vector = v,
            photo = if (a.kind == OngoingKind.CALL) a.largeIcon else null,
            tintedIcon = when (a.kind) {
                OngoingKind.CALL -> null
                OngoingKind.NAV -> a.largeIcon ?: a.smallIcon   // у Карт стрелка манёвра — в большой иконке
                else -> a.smallIcon
            },
            title = a.title.ifBlank { a.appLabel },
            value = when {
                a.kind == OngoingKind.SCORE -> scoreText(a)
                a.kind == OngoingKind.NAV -> navDistance(a)
                a.incoming -> "Входящий"
                else -> a.shortText ?: a.progress?.let { "${(it * 100).toInt()}%" } ?: ""
            },
            chronoBase = if (a.incoming) null else a.chronoBase, countDown = a.countDown, ring = a.progress,
            auto = if (a.kind == OngoingKind.CALL) auto else a.accent?.let { Color(it) } ?: auto,
            key = a.id,
        )
        is TorchActivity -> SlotData(v, title = "Фонарик", value = "Вкл", auto = auto, key = a.id)
        is ChargingActivity -> SlotData(v, title = if (a.fullInMs > 0) "До 100% · " + com.pixelisland.sources.SystemEvents.shortTime(a.fullInMs) else "Зарядка",
            value = "${a.level}%", ring = a.level / 100f, auto = auto, key = a.id)
        com.pixelisland.core.StackActivity -> SlotData(v, auto = auto)
        is com.pixelisland.core.ShelfActivity -> SlotData(v, title = a.items.firstOrNull()?.label ?: "Полка", value = "${a.items.size}", auto = auto, key = a.id)
        is DemoActivity -> demoSlot(a.type, v, auto)
        is PrivacyActivity -> SlotData(v, title = if (a.type == EventType.CAMERA_IN_USE) "Камера" else "Микрофон", value = "Занят", auto = auto, key = a.id)
        is AlarmSoonActivity -> SlotData(v, title = a.label, chronoBase = a.triggerAt, countDown = true, auto = auto, key = a.id)
        QuickActivity -> SlotData(v, auto = auto)
        is com.pixelisland.core.CalendarActivity -> SlotData(v, title = a.title, chronoBase = a.begin, countDown = true, auto = auto, key = a.id)
        is com.pixelisland.core.OwnTimerActivity -> {
            val stopwatch = a.mode == com.pixelisland.core.TimerMode.STOPWATCH
            val remain = if (a.running) null else a.frozenMs
            SlotData(
                v, title = a.title,
                value = remain?.let { formatDuration(it) } ?: "",
                chronoBase = if (a.running) a.anchor else null, countDown = !stopwatch,
                ring = if (a.totalMs > 0) ((if (a.running) a.anchor - System.currentTimeMillis() else a.frozenMs).toFloat() / a.totalMs).coerceIn(0f, 1f) else null,
                auto = auto, key = a.id,
            )
        }
    }
}

@Composable
private fun musicSlot(m: MediaState, v: androidx.compose.ui.graphics.vector.ImageVector): SlotData {
    val c = LocalIslandColors.current
    return SlotData(
        vector = v, photo = m.art, title = m.title, value = m.artist, playing = m.isPlaying,
        auto = m.accent?.let { Color(it) } ?: c.accent, key = m.key,
    )
}

private val demoStart = System.currentTimeMillis()

private fun demoSlot(t: EventType, v: androidx.compose.ui.graphics.vector.ImageVector, auto: Color): SlotData = when (t) {
    EventType.MUSIC -> SlotData(v, title = "Midnight City", value = "M83", auto = auto, key = "demo")
    EventType.CALL -> SlotData(v, title = "Аня", chronoBase = demoStart - 83_000, auto = auto, key = "demo")
    EventType.TIMER -> SlotData(v, title = "Чай", chronoBase = demoStart + 5 * 60_000, countDown = true, auto = auto, key = "demo")
    EventType.LIVE_UPDATE -> SlotData(v, title = "Через 300 м налево", value = "5 мин", ring = 0.6f, auto = auto, key = "demo")
    EventType.PROGRESS -> SlotData(v, title = "video.mp4", value = "42%", ring = 0.42f, auto = auto, key = "demo")
    EventType.CHARGING_LIVE -> SlotData(v, title = "Зарядка", value = "67%", ring = 0.67f, auto = auto, key = "demo")
    EventType.RECORDING -> SlotData(v, title = "Запись экрана", chronoBase = demoStart - 14_000, auto = auto, key = "demo")
    EventType.MIC_IN_USE -> SlotData(v, title = "Микрофон", value = "Занят", auto = auto, key = "demo")
    EventType.CAMERA_IN_USE -> SlotData(v, title = "Камера", value = "Занята", auto = auto, key = "demo")
    EventType.ALARM_SOON -> SlotData(v, title = "07:00", chronoBase = demoStart + 25 * 60_000, countDown = true, auto = auto, key = "demo")
    EventType.ALARM_RING -> SlotData(v, title = "07:00", value = "Подъём", auto = auto, key = "demo")
    EventType.OWN_TIMER -> SlotData(v, title = "Таймер", chronoBase = demoStart + 4 * 60_000, countDown = true, ring = 0.8f, auto = auto, key = "demo")
    EventType.DELIVERY -> SlotData(v, title = "Курьер в пути", value = "7 мин", ring = 0.65f, auto = auto, key = "demo")
    EventType.SCORE -> SlotData(v, title = "Динамо — Шахтёр", value = "2:1", auto = auto, key = "demo")
    EventType.NAVIGATION -> SlotData(v, title = "Крещатик", value = "300 м", auto = auto, key = "demo")
    EventType.VOICE -> SlotData(v, title = "Аня · голосовое", value = "0:14", auto = auto, key = "demo")
    EventType.TRANSFER -> SlotData(v, title = "Фото.jpg", value = "58%", ring = 0.58f, auto = auto, key = "demo")
    EventType.FOCUS -> SlotData(v, title = "Режим сна", value = "Вкл", auto = auto, key = "demo")
    EventType.SHELF -> SlotData(v, title = "github.com", value = "2", auto = auto, key = "demo")
    EventType.CALENDAR -> SlotData(v, title = "Созвон с командой", chronoBase = demoStart + 9 * 60_000, countDown = true, auto = auto, key = "demo")
    else -> SlotData(v, title = t.title, value = "Вкл", auto = auto, key = "demo")
}

@Composable
fun CompactContent(a: LiveActivity?, index: Int, count: Int, dims: IslandDims, width: Dp = dims.compactW) {
    if (a == null) { IdleContentView(dims, width); return }
    val st = SettingsStore.value.style(a.type)
    val d = slotDataFor(a)
    val color = LocalIslandColors.current.resolve(st.color, d.auto)
    FillBehind(st, d.ring, color) {
        CompactRow(dims, width,
            left = { SlotView(st.left, d, st, dims.slot, color) },
            right = { SlotView(st.right, d, st, dims.slot, color) },
            leftAlign = st.align.left(), rightAlign = st.align.right(),
        )
    }
}

// ======================= РАЗВЁРНУТЫЙ ВИД =======================

@Composable
internal fun GenericExpanded(a: LiveActivity) {
    val d = slotDataFor(a)
    val st = SettingsStore.value.style(a.type)
    val color = LocalIslandColors.current.resolve(st.color, d.auto)
    Row(Modifier.fillMaxWidth().height(60.dp), verticalAlignment = Alignment.CenterVertically) {
        AnimatedIcon(d, st, 48.dp, color)
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f)) {
            IslandText(d.title.ifBlank { a.type.title }, 16.sp, FontWeight.SemiBold)
            IslandText(a.type.title, 13.sp, dim = true)
        }
        val v = if (d.chronoBase != null) {
            val now = rememberWallClock()
            formatDuration(((if (d.countDown) d.chronoBase - now else now - d.chronoBase)).coerceAtLeast(0))
        } else d.value
        IslandText(v, 18.sp, FontWeight.SemiBold, color = color)
    }
}

internal fun headerOf(a: LiveActivity) = when (a) {
    is MusicActivity -> if (a.type == EventType.VOICE) "Голосовое · " + a.media.appLabel else a.media.appLabel
    is PreviewActivity -> "Предпросмотр"
    is OngoingActivity -> a.appLabel
    is TorchActivity -> "Фонарик"
    is ChargingActivity -> if (a.wireless) "Беспроводная зарядка" else "Зарядка"
    is DemoActivity -> "Пример"
    is PrivacyActivity -> "Приватность"
    is AlarmSoonActivity -> "Будильник"
    is com.pixelisland.core.CalendarActivity -> "Календарь"
    is com.pixelisland.core.OwnTimerActivity -> a.title
    QuickActivity -> "Быстрые действия"
    com.pixelisland.core.StackActivity -> "Все события"
    is com.pixelisland.core.ShelfActivity -> "Полка"
}

@Composable
internal fun QuickExpanded() {
    val c = LocalIslandColors.current
    val ctx = LocalContext.current
    val items = listOf(
        Triple(Icons.Rounded.FlashlightOn, "Фонарик") { SystemEvents.toggleTorch() },
        Triple(Icons.Rounded.Vibration, "Звонок") { SystemEvents.cycleRinger(ctx) },
        Triple(Icons.Rounded.Alarm, "Будильник") { launch(ctx, android.content.Intent(android.provider.AlarmClock.ACTION_SHOW_ALARMS)) },
        Triple(Icons.Rounded.PhotoCamera, "Камера") { launch(ctx, android.content.Intent(android.provider.MediaStore.INTENT_ACTION_STILL_IMAGE_CAMERA)) },
        Triple(Icons.Rounded.Wifi, "Интернет") { launch(ctx, android.content.Intent(android.provider.Settings.Panel.ACTION_INTERNET_CONNECTIVITY)) },
        Triple(Icons.Rounded.Bluetooth, "Bluetooth") { launch(ctx, android.content.Intent(android.provider.Settings.ACTION_BLUETOOTH_SETTINGS)) },
        Triple(Icons.Rounded.Timer, "Таймер 5 мин") { com.pixelisland.core.OwnTimers.startTimer(5); IslandController.collapse() },
        Triple(Icons.Rounded.AvTimer, "Секундомер") { com.pixelisland.core.OwnTimers.startStopwatch(); IslandController.collapse() },
        Triple(Icons.Rounded.SelfImprovement, "Помодоро") { com.pixelisland.core.OwnTimers.startPomodoro(); IslandController.collapse() },
        Triple(Icons.Rounded.Inventory2, "Полка") { IslandController.collapse(); IslandController.expand("shelf") },
        Triple(Icons.Rounded.Screenshot, "Скриншот") {
            IslandController.collapse()
            android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                com.pixelisland.service.IslandAccessibilityService.global(android.accessibilityservice.AccessibilityService.GLOBAL_ACTION_TAKE_SCREENSHOT)
            }, 450)
        },
        Triple(Icons.Rounded.VisibilityOff, "Скрыть 10 мин") { IslandController.snooze(10) },
    )
    val (rx, tx) = rememberNetSpeed()
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
            IslandText("↓ ${formatSpeed(rx)}   ↑ ${formatSpeed(tx)}", 12.sp, FontWeight.SemiBold, dim = true)
        }
        items.chunked(3).forEach { row ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                row.forEach { (icon, label, action) ->
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(84.dp)) {
                        Surface(
                            onClick = { IslandController.touch(); action() },
                            shape = RoundedCornerShape(18.dp), color = c.fg.copy(alpha = 0.12f),
                            modifier = Modifier.size(56.dp, 44.dp),
                        ) { Box(contentAlignment = Alignment.Center) { Icon(icon, label, tint = c.accent) } }
                        IslandText(label, 11.sp, dim = true, modifier = Modifier.padding(top = 4.dp))
                    }
                }
            }
        }
    }
}

private fun launch(ctx: android.content.Context, i: android.content.Intent) {
    IslandController.collapse()
    runCatching { ctx.startActivity(i.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)) }
}

/** Круг на паузе → скруглённый квадрат во время игры (морфинг формы M3 Expressive). */
@Composable
internal fun MorphButton(playing: Boolean, accent: Color, onClick: () -> Unit) {
    // Expressive: «печенье» на паузе перетекает в скруглённый квадрат во время игры.
    val p by androidx.compose.animation.core.animateFloatAsState(if (playing) 1f else 0f, spring(0.55f, 380f), label = "m")
    val morph = remember { androidx.graphics.shapes.Morph(androidx.compose.material3.MaterialShapes.Cookie9Sided, androidx.compose.material3.MaterialShapes.Square) }
    val on = if (accent.luminance() > 0.45f) Color.Black else Color.White
    Surface(onClick = onClick, shape = MorphShape(morph, p), color = accent, modifier = Modifier.size(66.dp)) {
        Box(contentAlignment = Alignment.Center) {
            Icon(if (playing) Icons.Rounded.Pause else Icons.Rounded.PlayArrow, null, tint = on, modifier = Modifier.size(32.dp))
        }
    }
}

@Composable
internal fun TorchExpanded(a: TorchActivity) {
    val c = LocalIslandColors.current
    Row(Modifier.fillMaxWidth().height(60.dp), verticalAlignment = Alignment.CenterVertically) {
        IconBadge(Icons.Rounded.FlashlightOn, TorchYellow, 48.dp)
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f)) {
            IslandText("Фонарик", 16.sp, FontWeight.SemiBold)
            IslandText("Включён", 13.sp, dim = true)
        }
        FilledTonalButton(
            onClick = { SystemEvents.torchOff() },
            colors = ButtonDefaults.filledTonalButtonColors(containerColor = TorchYellow, contentColor = Color.Black),
        ) { IslandText("Выключить", 13.sp, color = Color.Black) }
    }
    if (a.maxStrength > 1) {
        Spacer(Modifier.height(10.dp))
        var v by remember(a.strength) { mutableFloatStateOf(a.strength.toFloat()) }
        Slider(
            value = v, valueRange = 1f..a.maxStrength.toFloat(), steps = (a.maxStrength - 2).coerceAtLeast(0),
            onValueChange = { v = it; IslandController.touch() },
            onValueChangeFinished = { SystemEvents.torchStrength(v.toInt()) },
            colors = SliderDefaults.colors(
                thumbColor = TorchYellow, activeTrackColor = TorchYellow, inactiveTrackColor = c.fg.copy(alpha = 0.18f),
                activeTickColor = Color.Transparent, inactiveTickColor = Color.Transparent,
            ),
        )
    }
}

@Composable
internal fun ChargingExpanded(a: ChargingActivity) {
    Row(Modifier.fillMaxWidth().height(64.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(contentAlignment = Alignment.Center) {
            Ring(a.level / 100f, CallGreen, 52.dp, 5.dp)
            IslandText("${a.level}", 14.sp, FontWeight.Bold, color = CallGreen)
        }
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f)) {
            IslandText(if (a.level >= 100) "Заряжено" else "Заряжается", 16.sp, FontWeight.SemiBold)
            val left = if (a.fullInMs > 0) "До полной ≈ " + humanTime(a.fullInMs) else if (a.wireless) "Беспроводная" else "Подключено к питанию"
            IslandText(left, 13.sp, dim = true)
        }
    }
}

private fun humanTime(ms: Long): String {
    val m = ms / 60_000
    return if (m >= 60) "${m / 60} ч ${m % 60} мин" else "$m мин"
}

// ======================= ВСПЛЫВАШКИ =======================

@Composable
fun ToastContent(e: ToastEvent, dims: IslandDims, width: Dp) {
    val st = SettingsStore.value.style(e.type)
    // Разблокировка: сначала закрытый замок, через мгновение он «открывается» (новый ключ — заново играет анимацию иконки).
    var opened by remember(e) { mutableStateOf(e.type != EventType.UNLOCK || st.icon.isNotEmpty()) }
    if (!opened) LaunchedEffect(e) { delay(280); opened = true }
    val vector = if (!opened) com.pixelisland.ui.catalogIcon("Lock", st.variant) ?: iconFor(e.type, st.variant)
        else iconFor(e.type, st.variant, st.icon.ifEmpty { e.iconName ?: "" })
    val d = SlotData(vector, title = e.title, value = e.value, ring = e.ring, auto = Color(e.type.tint), key = e to opened)
    val color = LocalIslandColors.current.resolve(st.color, d.auto)
    FillBehind(st, d.ring, color) {
        CompactRow(dims, width,
            left = { SlotView(st.left, d, st, dims.slot, color) },
            right = { SlotView(st.right, d, st, dims.slot, color) },
            leftAlign = st.align.left(), rightAlign = st.align.right(),
        )
    }
}

/** Стиль прогресса «Заливка фона»: пилюля заполняется цветом слева направо. */
@Composable
private fun FillBehind(st: com.pixelisland.settings.EventStyle, ring: Float?, color: Color, content: @Composable () -> Unit) {
    if (st.ring != com.pixelisland.settings.RingStyle.FILL || ring == null) { content(); return }
    val v by androidx.compose.animation.core.animateFloatAsState(ring.coerceIn(0f, 1f), label = "fill")
    Box(Modifier.drawBehind { drawRect(color.copy(alpha = 0.28f), size = size.copy(width = size.width * v)) }) { content() }
}

@Composable
fun PeekContent(e: PeekEvent, dims: IslandDims) {
    val c = LocalIslandColors.current
    val ctx = LocalContext.current
    val tint = e.accent?.let { Color(it) } ?: c.accent
    val replying by IslandController.replying.collectAsState()
    Column(Modifier.size(dims.expandedW, dims.peekH + if (replying) 56.dp else 0.dp).padding(horizontal = 20.dp)) {
        Row(Modifier.fillMaxWidth().height(dims.compactH), verticalAlignment = Alignment.CenterVertically) {
            Row(Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                BitmapImage(e.appIcon, 14.dp, 0.dp, c.dim, Icons.Rounded.Notifications, c.dim)
                Spacer(Modifier.width(6.dp))
                IslandText(e.appLabel, 12.sp, dim = true)
            }
            Spacer(Modifier.width(dims.cutoutW + 16.dp))
            Box(Modifier.weight(1f), contentAlignment = Alignment.CenterEnd) {
                if (e.actionLabel != null) Surface(
                    onClick = { e.onTap?.invoke(); IslandController.clearToast() }, shape = RoundedCornerShape(50), color = tint,
                ) { IslandText(e.actionLabel, 12.sp, FontWeight.SemiBold, color = if (tint.luminance() > 0.45f) Color.Black else Color.White,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp)) }
                else if (e.reply != null && !replying) Surface(
                    onClick = { IslandController.startReply() }, shape = RoundedCornerShape(50), color = tint.copy(alpha = 0.22f),
                ) { IslandText("Ответить", 12.sp, FontWeight.SemiBold, color = tint, modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp)) }
            }
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (e.largeIcon != null) {
                BitmapImage(e.largeIcon, 44.dp, if (e.type == EventType.SCREENSHOT) 10.dp else 22.dp, null, Icons.Rounded.Notifications, tint)
                Spacer(Modifier.width(12.dp))
            }
            Column(Modifier.weight(1f)) {
                IslandText(e.title, 15.sp, FontWeight.SemiBold)
                IslandText(e.text, 13.sp, dim = true, maxLines = 2)
            }
        }
        if (replying && e.reply != null) ReplyField(e, tint) { text ->
            sendReply(ctx, e.reply, text)
            IslandController.endReply()
        }
    }
}

/** Поле ответа прямо в острове (окно на это время получает фокус для клавиатуры). */
@Composable
private fun ReplyField(e: PeekEvent, tint: Color, onSend: (String) -> Unit) {
    val c = LocalIslandColors.current
    var text by remember(e.id) { mutableStateOf("") }
    val focus = remember { androidx.compose.ui.focus.FocusRequester() }
    LaunchedEffect(Unit) { delay(150); runCatching { focus.requestFocus() } }
    Row(
        Modifier.fillMaxWidth().padding(top = 10.dp).height(44.dp).clip(RoundedCornerShape(50)).background(c.fg.copy(alpha = 0.1f))
            .padding(start = 14.dp, end = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(Modifier.weight(1f)) {
            if (text.isEmpty()) IslandText(e.reply?.title?.ifBlank { null } ?: "Ответ…", 14.sp, dim = true)
            androidx.compose.foundation.text.BasicTextField(
                value = text, onValueChange = { text = it }, singleLine = true,
                textStyle = androidx.compose.ui.text.TextStyle(color = c.fg, fontSize = 14.sp),
                cursorBrush = androidx.compose.ui.graphics.SolidColor(tint),
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(imeAction = androidx.compose.ui.text.input.ImeAction.Send),
                keyboardActions = androidx.compose.foundation.text.KeyboardActions(onSend = { if (text.isNotBlank()) onSend(text) }),
                modifier = Modifier.fillMaxWidth().focusRequester(focus),
            )
        }
        IconButton(onClick = { if (text.isNotBlank()) onSend(text) }, enabled = text.isNotBlank()) {
            Icon(Icons.Rounded.Send, "Отправить", tint = if (text.isNotBlank()) tint else c.dim)
        }
    }
}

private fun sendReply(ctx: android.content.Context, a: com.pixelisland.core.NotifAction, text: String) {
    val inputs = a.remoteInputs ?: return
    val intent = android.content.Intent()
    val bundle = android.os.Bundle().apply { inputs.forEach { putCharSequence(it.resultKey, text) } }
    android.app.RemoteInput.addResultsToIntent(inputs, intent, bundle)
    runCatching { a.intent?.send(ctx, 0, intent) }
}

@Composable
internal fun Art(m: MediaState, accent: Color, size: Dp, corner: Dp) {
    if (m.art != null) BitmapImage(m.art, size, corner, null, Icons.Rounded.MusicNote, accent)
    else Box(
        Modifier.size(size).clip(RoundedCornerShape(corner)).background(accent.copy(alpha = 0.25f)),
        contentAlignment = Alignment.Center,
    ) { Icon(Icons.Rounded.MusicNote, null, tint = accent, modifier = Modifier.size(size * 0.6f)) }
}

// ---------- Свой таймер ----------
@Composable
internal fun OwnTimerExpanded(a: com.pixelisland.core.OwnTimerActivity) {
    val c = LocalIslandColors.current
    val st = SettingsStore.value.style(a.type)
    val color = c.resolve(st.color, Color(a.type.tint))
    val now = rememberWallClock(if (a.running) 1000 else 60_000)
    val ms = when {
        !a.running -> a.frozenMs
        a.mode == com.pixelisland.core.TimerMode.STOPWATCH -> now - a.anchor
        else -> a.anchor - now
    }.coerceAtLeast(0)
    Row(Modifier.fillMaxWidth().height(64.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(contentAlignment = Alignment.Center) {
            if (a.totalMs > 0) Ring(1f - ms.toFloat() / a.totalMs, color, 52.dp, 5.dp)
            else IconBadge(Icons.Rounded.Timer, color, 48.dp)
        }
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f)) {
            IslandText(a.title, 16.sp, FontWeight.SemiBold)
            IslandText(if (a.running) "Идёт" else "На паузе", 13.sp, dim = true)
        }
        Text(formatDuration(ms), color = color, fontSize = 30.sp, fontWeight = FontWeight.SemiBold,
            style = androidx.compose.ui.text.TextStyle(fontFeatureSettings = "tnum"))
    }
    Spacer(Modifier.height(10.dp))
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        TimerButton(if (a.running) "Пауза" else "Продолжить", color, true, Modifier.weight(1f)) { com.pixelisland.core.OwnTimers.togglePause() }
        if (a.mode != com.pixelisland.core.TimerMode.STOPWATCH)
            TimerButton("+1 мин", color, false, Modifier.weight(1f)) { com.pixelisland.core.OwnTimers.addMinute() }
        TimerButton("Стоп", Color(Tints.RED), false, Modifier.weight(1f)) { com.pixelisland.core.OwnTimers.stop(); IslandController.collapse() }
    }
}

@Composable
private fun TimerButton(label: String, color: Color, primary: Boolean, modifier: Modifier, onClick: () -> Unit) {
    val c = LocalIslandColors.current
    Surface(
        onClick = { IslandController.touch(); onClick() }, shape = RoundedCornerShape(50),
        color = if (primary) color else c.fg.copy(alpha = 0.12f), modifier = modifier.height(42.dp),
    ) {
        Box(contentAlignment = Alignment.Center) {
            IslandText(label, 14.sp, FontWeight.SemiBold,
                color = if (primary) (if (color.luminance() > 0.45f) Color.Black else Color.White) else if (label == "Стоп") color else c.fg)
        }
    }
}

// ---------- Доставка / такси ----------
@Composable
internal fun DeliveryExpanded(a: OngoingActivity) {
    val c = LocalIslandColors.current
    val color = c.resolve(SettingsStore.value.style(a.type).color, a.accent?.let { Color(it) } ?: Color(a.type.tint))
    Row(Modifier.fillMaxWidth().height(52.dp), verticalAlignment = Alignment.CenterVertically) {
        if (a.largeIcon != null) BitmapImage(a.largeIcon, 44.dp, 22.dp, null, Icons.Rounded.LocalShipping, color)
        else IconBadge(Icons.Rounded.LocalShipping, color, 44.dp)
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            IslandText(a.title.ifBlank { a.appLabel }, 16.sp, FontWeight.SemiBold)
            if (a.text.isNotBlank()) IslandText(a.text, 13.sp, dim = true)
        }
        if (a.shortText != null) IslandText(a.shortText, 18.sp, FontWeight.SemiBold, color = color)
    }
    Spacer(Modifier.height(14.dp))
    // Маршрут: старт — машина движется по прогрессу — финиш.
    val p by androidx.compose.animation.core.animateFloatAsState((a.progress ?: 0.5f).coerceIn(0f, 1f), label = "route")
    BoxWithConstraints(Modifier.fillMaxWidth().height(30.dp)) {
        val w = maxWidth
        Box(Modifier.align(Alignment.CenterStart).fillMaxWidth().height(6.dp).clip(RoundedCornerShape(50)).background(color.copy(alpha = 0.2f)))
        Box(Modifier.align(Alignment.CenterStart).fillMaxWidth(p).height(6.dp).clip(RoundedCornerShape(50)).background(color))
        Box(Modifier.align(Alignment.CenterStart).size(10.dp).clip(CircleShape).background(color))
        Icon(Icons.Rounded.Flag, null, tint = c.fg, modifier = Modifier.align(Alignment.CenterEnd).size(20.dp))
        Box(
            Modifier.align(Alignment.CenterStart).offset(x = (w - 28.dp) * p).size(28.dp).clip(CircleShape).background(color),
            contentAlignment = Alignment.Center,
        ) { Icon(Icons.Rounded.LocalShipping, null, tint = if (color.luminance() > 0.45f) Color.Black else Color.White, modifier = Modifier.size(16.dp)) }
    }
}

// ---------- Счёт матча ----------
@Composable
internal fun ScoreExpanded(a: OngoingActivity) {
    val c = LocalIslandColors.current
    val color = c.resolve(SettingsStore.value.style(a.type).color, Color(a.type.tint))
    val m = NotificationSource.scoreRegex.find(a.title) ?: NotificationSource.scoreRegex.find(a.text)
    Row(Modifier.fillMaxWidth().height(64.dp), verticalAlignment = Alignment.CenterVertically) {
        if (m != null) {
            IslandText(m.groupValues[1].trim(), 15.sp, FontWeight.SemiBold, modifier = Modifier.weight(1f))
            Text("${m.groupValues[2]} : ${m.groupValues[3]}", color = color, fontSize = 30.sp, fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 12.dp))
            Box(Modifier.weight(1f), contentAlignment = Alignment.CenterEnd) { IslandText(m.groupValues[4].trim(), 15.sp, FontWeight.SemiBold) }
        } else {
            IslandText(a.title, 16.sp, FontWeight.SemiBold, modifier = Modifier.weight(1f))
        }
    }
    val sub = a.shortText ?: a.text
    if (sub.isNotBlank()) Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) { IslandText(sub, 13.sp, dim = true) }
}
