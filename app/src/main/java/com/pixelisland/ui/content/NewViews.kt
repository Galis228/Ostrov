package com.pixelisland.ui.content

import android.net.TrafficStats
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Call
import androidx.compose.material.icons.rounded.CallEnd
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.Inventory2
import androidx.compose.material.icons.rounded.Link
import androidx.compose.material.icons.rounded.Notes
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material.icons.rounded.TurnRight
import androidx.compose.material.icons.rounded.VolumeUp
import androidx.compose.material.icons.rounded.Description
import androidx.compose.material3.Icon
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pixelisland.core.IslandController
import com.pixelisland.core.LiveActivity
import com.pixelisland.core.OngoingActivity
import com.pixelisland.core.Shelf
import com.pixelisland.core.ShelfActivity
import com.pixelisland.core.ShelfItem
import com.pixelisland.core.sendPending
import com.pixelisland.settings.SettingsStore
import com.pixelisland.sources.Tints
import com.pixelisland.ui.LocalIslandColors
import kotlinx.coroutines.delay

// ---------- Навигация ----------

/** Расстояние до манёвра: Google Maps кладёт его в заголовок («300 м»), Waze — в текст. */
fun navDistance(a: OngoingActivity): String {
    val re = Regex("(\\d+[.,]?\\d*)\\s?(м|км|m|km|ft|mi|фт|миль)\\b", RegexOption.IGNORE_CASE)
    return re.find(a.title)?.value ?: re.find(a.text)?.value ?: a.shortText ?: ""
}

@Composable
internal fun NavExpanded(a: OngoingActivity) {
    val c = LocalIslandColors.current
    val color = c.resolve(SettingsStore.value.style(a.type).color, a.accent?.let { Color(it) } ?: Color(a.type.tint))
    val dist = navDistance(a)
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(64.dp).clip(RoundedCornerShape(20.dp)).background(color.copy(alpha = 0.2f)), contentAlignment = Alignment.Center) {
            val arrow = a.largeIcon ?: a.smallIcon
            if (arrow != null) {
                val img = remember(arrow) { arrow.asImageBitmap() }
                androidx.compose.foundation.Image(img, null, colorFilter = ColorFilter.tint(color), modifier = Modifier.size(44.dp))
            } else Icon(Icons.Rounded.TurnRight, null, tint = color, modifier = Modifier.size(40.dp))
        }
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f)) {
            if (dist.isNotEmpty()) androidx.compose.material3.Text(dist, color = color, fontSize = 28.sp, fontWeight = FontWeight.Bold)
            val street = if (dist.isNotEmpty() && a.title.contains(dist)) a.text else a.title
            IslandText(street.ifBlank { a.appLabel }, 15.sp, FontWeight.SemiBold, maxLines = 2)
        }
    }
    val eta = a.subText.ifBlank { if (dist.isNotEmpty() && !a.title.contains(dist)) "" else "" }
    if (eta.isNotBlank()) {
        Spacer(Modifier.height(8.dp))
        IslandText(eta, 13.sp, dim = true)
    }
}

// ---------- Звонок ----------

@android.annotation.SuppressLint("MissingPermission")
@Composable
internal fun CallExpanded(a: OngoingActivity) {
    val c = LocalIslandColors.current
    val ctx = LocalContext.current
    val green = Color(Tints.GREEN); val red = Color(Tints.RED)
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        BitmapImage(a.largeIcon, 52.dp, 26.dp, null, Icons.Rounded.Call, green)
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f)) {
            IslandText(a.title.ifBlank { a.appLabel }, 17.sp, FontWeight.SemiBold)
            IslandText(if (a.incoming) "Входящий звонок" else a.text.ifBlank { a.appLabel }, 13.sp, dim = true)
        }
        if (!a.incoming && a.chronoBase != null) {
            val now = rememberWallClock()
            IslandText(formatDuration((now - a.chronoBase).coerceAtLeast(0)), 20.sp, FontWeight.SemiBold, color = green)
        }
    }
    Spacer(Modifier.height(12.dp))
    // Кнопки звонилки, если она их дала; если нет — принимаем/сбрасываем через Telecom.
    val accept = a.actions.firstOrNull { Regex("ответ|принять|answer|accept|відпов", RegexOption.IGNORE_CASE).containsMatchIn(it.title) }
    val decline = a.actions.firstOrNull { Regex("отклон|сброс|заверш|положить|decline|hang|end|відхил|заверш", RegexOption.IGNORE_CASE).containsMatchIn(it.title) }
    val others = a.actions.filter { it != accept && it != decline }
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        CallButton(Icons.Rounded.CallEnd, if (a.incoming) "Отклонить" else "Завершить", red, Modifier.weight(1f)) {
            if (decline == null || !ctx.sendPending(decline.intent)) telecom(ctx) { it.endCall() }
        }
        others.take(1).forEach { act ->
            CallButton(Icons.Rounded.VolumeUp, act.title, c.fg.copy(alpha = 0.14f), Modifier.weight(1f), dark = true) { ctx.sendPending(act.intent) }
        }
        if (a.incoming) CallButton(Icons.Rounded.Call, "Принять", green, Modifier.weight(1f)) {
            if (accept == null || !ctx.sendPending(accept.intent)) telecom(ctx) { it.acceptRingingCall() }
        }
    }
    if (!a.incoming) {
        // Громкость разговора — единственное, что Android разрешает менять не-звонилке.
        val am = remember { ctx.getSystemService(android.media.AudioManager::class.java) }
        val max = remember { am.getStreamMaxVolume(android.media.AudioManager.STREAM_VOICE_CALL).coerceAtLeast(1) }
        var v by remember { mutableFloatStateOf(am.getStreamVolume(android.media.AudioManager.STREAM_VOICE_CALL).toFloat()) }
        Spacer(Modifier.height(6.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Rounded.VolumeUp, null, tint = c.dim, modifier = Modifier.size(18.dp))
            Slider(
                value = v, valueRange = 0f..max.toFloat(), onValueChange = {
                    v = it; IslandController.touch()
                    runCatching { am.setStreamVolume(android.media.AudioManager.STREAM_VOICE_CALL, it.toInt(), 0) }
                },
                colors = SliderDefaults.colors(thumbColor = green, activeTrackColor = green, inactiveTrackColor = c.fg.copy(alpha = 0.18f)),
                modifier = Modifier.weight(1f).padding(start = 8.dp),
            )
        }
    }
}

@android.annotation.SuppressLint("MissingPermission")
private fun telecom(ctx: android.content.Context, block: (android.telecom.TelecomManager) -> Unit) {
    if (ctx.checkSelfPermission(android.Manifest.permission.ANSWER_PHONE_CALLS) != android.content.pm.PackageManager.PERMISSION_GRANTED) return
    runCatching { block(ctx.getSystemService(android.telecom.TelecomManager::class.java)) }
}

@Composable
private fun CallButton(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, bg: Color, modifier: Modifier, dark: Boolean = false, onClick: () -> Unit) {
    val c = LocalIslandColors.current
    val fg = if (dark) c.fg else if (bg.luminance() > 0.45f) Color.Black else Color.White
    Surface(onClick = { IslandController.touch(); onClick() }, shape = RoundedCornerShape(50), color = bg, modifier = modifier.height(46.dp)) {
        Row(horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null, tint = fg, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(6.dp))
            IslandText(label, 13.sp, FontWeight.SemiBold, color = fg)
        }
    }
}

// ---------- Стопка: все активности ----------

@Composable
internal fun StackExpanded() {
    val list by IslandController.visible.collectAsState()
    val c = LocalIslandColors.current
    if (list.isEmpty()) { IslandText("Сейчас ничего не происходит", 14.sp, dim = true, modifier = Modifier.padding(vertical = 12.dp)); return }
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        list.take(6).forEach { a -> StackRow(a, c.fg) }
    }
}

@Composable
private fun StackRow(a: LiveActivity, fg: Color) {
    val st = SettingsStore.value.style(a.type)
    val d = slotDataFor(a)
    val color = LocalIslandColors.current.resolve(st.color, d.auto)
    Surface(
        onClick = { IslandController.collapse(); IslandController.select(a.id); IslandController.expand(a.id) },
        shape = RoundedCornerShape(18.dp), color = fg.copy(alpha = 0.08f), modifier = Modifier.fillMaxWidth().height(52.dp),
    ) {
        Row(Modifier.padding(horizontal = 10.dp), verticalAlignment = Alignment.CenterVertically) {
            if (d.photo != null) BitmapImage(d.photo, 34.dp, 10.dp, null, d.vector, color) else AnimatedIcon(d, st, 34.dp, color)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                IslandText(d.title.ifBlank { a.type.title }, 14.sp, FontWeight.SemiBold)
                IslandText(headerOf(a), 11.sp, dim = true)
            }
            val v = if (d.chronoBase != null) {
                val now = rememberWallClock()
                formatDuration((if (d.countDown) d.chronoBase - now else now - d.chronoBase).coerceAtLeast(0))
            } else d.value
            if (v.isNotEmpty()) IslandText(v, 14.sp, FontWeight.SemiBold, color = color)
        }
    }
}

// ---------- Полка ----------

@Composable
internal fun ShelfExpanded(a: ShelfActivity) {
    val items by Shelf.items.collectAsState()
    val dragging by Shelf.dragging.collectAsState()
    val c = LocalIslandColors.current
    val ctx = LocalContext.current
    val accent = Color(Tints.ORANGE)
    if (items.isEmpty()) {
        Column(Modifier.fillMaxWidth().padding(vertical = 10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(Icons.Rounded.Inventory2, null, tint = accent, modifier = Modifier.size(36.dp))
            Spacer(Modifier.height(6.dp))
            IslandText(if (dragging) "Отпустите здесь" else "Полка пуста", 15.sp, FontWeight.SemiBold)
            IslandText("Перетащите текст или ссылку на остров", 12.sp, dim = true)
        }
        return
    }
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        items.take(5).forEach { ShelfRow(it, accent) }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
            Surface(onClick = { Shelf.clear(); IslandController.collapse() }, shape = RoundedCornerShape(50), color = c.fg.copy(alpha = 0.1f)) {
                IslandText("Очистить", 12.sp, FontWeight.SemiBold, modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp))
            }
        }
    }
}

@Composable
private fun ShelfRow(it: ShelfItem, accent: Color) {
    val c = LocalIslandColors.current
    val ctx = LocalContext.current
    Row(
        Modifier.fillMaxWidth().height(48.dp).clip(RoundedCornerShape(16.dp)).background(c.fg.copy(alpha = 0.08f)).padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        val icon = when {
            it.uri != null -> Icons.Rounded.Description
            it.text?.startsWith("http") == true -> Icons.Rounded.Link
            else -> Icons.Rounded.Notes
        }
        if (it.thumb != null) BitmapImage(it.thumb, 34.dp, 8.dp, null, icon, accent) else IconBadge(icon, accent, 34.dp)
        Spacer(Modifier.width(10.dp))
        Surface(onClick = { IslandController.collapse(); Shelf.open(ctx, it) }, color = Color.Transparent, modifier = Modifier.weight(1f)) {
            IslandText(it.label, 13.sp, FontWeight.Medium)
        }
        SmallIcon(Icons.Rounded.ContentCopy) { Shelf.copy(ctx, it); com.pixelisland.sources.SystemEvents.copied(it.label) }
        SmallIcon(Icons.Rounded.Share) { IslandController.collapse(); Shelf.share(ctx, it) }
        SmallIcon(Icons.Rounded.Close) { Shelf.remove(it.id) }
    }
}

@Composable
private fun SmallIcon(icon: androidx.compose.ui.graphics.vector.ImageVector, onClick: () -> Unit) {
    val c = LocalIslandColors.current
    Surface(onClick = { IslandController.touch(); onClick() }, shape = CircleShape, color = Color.Transparent, modifier = Modifier.size(36.dp)) {
        Box(contentAlignment = Alignment.Center) { Icon(icon, null, tint = c.dim, modifier = Modifier.size(18.dp)) }
    }
}

// ---------- Скорость сети ----------

/** Скорость приёма/отдачи, байт/с. Считается раз в секунду только пока виджет на экране. */
@Composable
fun rememberNetSpeed(): Pair<Long, Long> {
    val v by produceState(0L to 0L) {
        var rx = TrafficStats.getTotalRxBytes(); var tx = TrafficStats.getTotalTxBytes()
        while (true) {
            delay(1000)
            val r = TrafficStats.getTotalRxBytes(); val t = TrafficStats.getTotalTxBytes()
            value = (r - rx).coerceAtLeast(0) to (t - tx).coerceAtLeast(0)
            rx = r; tx = t
        }
    }
    return v
}

fun formatSpeed(b: Long): String = when {
    b >= 1_000_000 -> "%.1f МБ/с".format(b / 1_000_000f)
    b >= 1_000 -> "${b / 1_000} КБ/с"
    else -> "$b Б/с"
}
