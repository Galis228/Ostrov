package com.pixelisland.ui.content

import android.os.SystemClock
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.SkipNext
import androidx.compose.material.icons.rounded.SkipPrevious
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
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
import com.pixelisland.core.PreviewActivity
import com.pixelisland.core.TorchActivity
import com.pixelisland.core.sendPending
import com.pixelisland.media.IslandActions
import com.pixelisland.media.MediaRepository
import com.pixelisland.media.MediaState
import com.pixelisland.settings.EventStyle
import com.pixelisland.settings.ExBg
import com.pixelisland.settings.ExButtons
import com.pixelisland.settings.ExLayout
import com.pixelisland.settings.ExProgress
import com.pixelisland.settings.SettingsStore
import com.pixelisland.sources.Tints
import com.pixelisland.ui.IslandDims
import com.pixelisland.ui.LocalIslandColors
import kotlinx.coroutines.delay

/**
 * Раскрытый вид. Высота не задаётся заранее: содержимое меряется и сообщает свою высоту,
 * поэтому при любом макете и наборе элементов всё помещается.
 */
@Composable
fun ExpandedContent(a: LiveActivity, index: Int, count: Int, dims: IslandDims, onMeasured: (Dp) -> Unit = {}) {
    val st = SettingsStore.value.style(a.type)
    val density = LocalDensity.current
    Box(Modifier.fillMaxWidth().onSizeChanged { onMeasured(with(density) { it.height.toDp() }) }) {
        ExpandedBackground(a, st, Modifier.matchParentSize())
        Column(Modifier.fillMaxWidth().padding(start = 20.dp, end = 20.dp, bottom = 16.dp)) {
            // Полоса на уровне камеры: заголовок слева, точки справа (камера посередине остаётся свободной).
            Row(Modifier.fillMaxWidth().height(dims.compactH), verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.weight(1f)) { if (st.exHeader) IslandText(headerOf(a), 12.sp, dim = true) }
                Spacer(Modifier.width(dims.cutoutW + 16.dp))
                Box(Modifier.weight(1f), contentAlignment = Alignment.CenterEnd) { PageDots(index, count) }
            }
            when (a) {
                is MusicActivity -> MusicView(a.media, MediaRepository, st)
                is PreviewActivity -> MusicView(previewMedia(a), PreviewActions, st)
                is OngoingActivity -> when (a.kind) {
                    OngoingKind.NAV -> NavExpanded(a)
                    OngoingKind.CALL -> CallExpanded(a)
                    OngoingKind.DELIVERY -> DeliveryExpanded(a)
                    OngoingKind.SCORE -> ScoreExpanded(a)
                    else -> OngoingView(a, st)
                }
                is com.pixelisland.core.OwnTimerActivity -> OwnTimerExpanded(a)
                is TorchActivity -> TorchExpanded(a)
                is ChargingActivity -> ChargingExpanded(a)
                com.pixelisland.core.QuickActivity -> QuickExpanded()
                com.pixelisland.core.StackActivity -> StackExpanded()
                is com.pixelisland.core.ShelfActivity -> ShelfExpanded(a)
                else -> GenericExpanded(a)
            }
        }
    }
}

/** Фон раскрытого вида: как пилюля, размытая обложка, оттенок или свой цвет. */
@Composable
private fun ExpandedBackground(a: LiveActivity, st: EventStyle, modifier: Modifier) {
    val c = LocalIslandColors.current
    val auto = c.resolve(st.color, slotDataFor(a).auto)
    when (st.exBg) {
        ExBg.SAME -> Unit
        ExBg.ART_BLUR -> {
            val bmp = when (a) {
                is MusicActivity -> a.media.art
                is OngoingActivity -> a.largeIcon
                else -> null
            }
            if (bmp != null) {
                val img = remember(bmp) { bmp.asImageBitmap() }
                Image(img, null, contentScale = ContentScale.Crop, alpha = 0.7f, modifier = modifier.blur(40.dp))
                Box(modifier.background(Color.Black.copy(alpha = 0.38f)))
            } else Box(modifier.background(auto.copy(alpha = 0.25f)))
        }
        ExBg.TINT -> Box(modifier.background(auto.copy(alpha = 0.25f)))
        ExBg.CUSTOM -> Box(modifier.background(Color(st.exBgColor)))
    }
}

// ======================= МУЗЫКА =======================

@Composable
private fun MusicView(m: MediaState, actions: IslandActions, st: EventStyle) {
    val accent = accentOf(m)
    when (st.exLayout) {
        ExLayout.MINIMAL -> Row(verticalAlignment = Alignment.CenterVertically) {
            Art(m, accent, 44.dp, 12.dp)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                IslandText(m.title, 15.sp, FontWeight.SemiBold)
                IslandText(m.artist, 13.sp, dim = true)
            }
            if (st.exButtons != ExButtons.NONE) PlayControl(m.isPlaying, accent, st.exButtons, 48.dp) { IslandController.touch(); actions.playPause() }
        }
        ExLayout.PLAYER -> Row(verticalAlignment = Alignment.CenterVertically) {
            Art(m, accent, 96.dp, 20.dp)
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                IslandText(m.title, 16.sp, FontWeight.SemiBold)
                IslandText(m.artist, 13.sp, dim = true)
                Spacer(Modifier.height(4.dp))
                MusicProgress(m, accent, st.exProgress, actions, compact = true)
                if (st.exButtons != ExButtons.NONE) MusicControls(m, accent, st.exButtons, actions, 44.dp, 26.dp)
            }
        }
        else -> {
            val large = st.exLayout == ExLayout.LARGE
            val art = if (large) 88.dp else st.exIcon.dp.dp
            Row(verticalAlignment = Alignment.CenterVertically) {
                Art(m, accent, art, art / 3.5f)
                Spacer(Modifier.width(14.dp))
                Column(Modifier.weight(1f)) {
                    IslandText(m.title, if (large) 20.sp else 17.sp, FontWeight.SemiBold)
                    IslandText(m.artist, if (large) 15.sp else 14.sp, dim = true)
                }
                Spacer(Modifier.width(12.dp))
                Equalizer(m.isPlaying, accent, Modifier.size(22.dp, 18.dp))
            }
            Spacer(Modifier.height(10.dp))
            MusicProgress(m, accent, st.exProgress, actions, compact = false)
            if (st.exButtons != ExButtons.NONE) {
                Spacer(Modifier.height(6.dp))
                MusicControls(m, accent, st.exButtons, actions, if (large) 72.dp else 62.dp, if (large) 36.dp else 32.dp)
            }
        }
    }
}

@Composable
private fun MusicProgress(m: MediaState, accent: Color, kind: ExProgress, actions: IslandActions, compact: Boolean) {
    if (kind == ExProgress.NONE || m.durationMs <= 0) return
    val c = LocalIslandColors.current
    var now by remember { mutableLongStateOf(SystemClock.elapsedRealtime()) }
    LaunchedEffect(m.isPlaying, m.positionUpdateTime) {
        while (true) { now = SystemClock.elapsedRealtime(); delay(if (m.isPlaying) 500 else 5_000) }
    }
    var dragging by remember { mutableStateOf(false) }
    var dragValue by remember { mutableFloatStateOf(0f) }
    val duration = m.durationMs.coerceAtLeast(1)
    val pos = if (dragging) (dragValue * duration).toLong() else m.positionAt(now)
    val frac = (pos.toFloat() / duration).coerceIn(0f, 1f)

    if (kind == ExProgress.RING) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 4.dp)) {
            Ring(frac, accent, 26.dp, 3.dp)
            Spacer(Modifier.width(10.dp))
            IslandText("${formatDuration(pos)} / ${formatDuration(duration)}", 12.sp, dim = true)
        }
        return
    }
    // Волна «дышит» 8 секунд после раскрытия / смены трека / касания и затихает.
    var waveOn by remember { mutableStateOf(true) }
    LaunchedEffect(m.key, m.isPlaying, dragging) { waveOn = true; delay(8_000); waveOn = false }
    val amp by animateFloatAsState(if (kind == ExProgress.WAVE && m.isPlaying && !dragging && waveOn) 1f else 0f, label = "amp")
    Box(Modifier.fillMaxWidth().height(if (compact) 22.dp else 28.dp), contentAlignment = Alignment.Center) {
        if (amp > 0.01f) androidx.compose.material3.LinearWavyProgressIndicator(
            progress = { frac }, color = accent, trackColor = c.fg.copy(alpha = 0.18f), amplitude = { amp }, modifier = Modifier.fillMaxWidth(),
        ) else LinearProgressIndicator(progress = { frac }, color = accent, trackColor = c.fg.copy(alpha = 0.18f), modifier = Modifier.fillMaxWidth())
        if (m.canSeek) Slider(
            value = frac,
            onValueChange = { dragging = true; dragValue = it; IslandController.touch() },
            onValueChangeFinished = { actions.seekTo((dragValue * duration).toLong()); dragging = false },
            colors = SliderDefaults.colors(thumbColor = Color.Transparent, activeTrackColor = Color.Transparent, inactiveTrackColor = Color.Transparent),
            modifier = Modifier.fillMaxWidth(),
        )
    }
    if (!compact) Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        IslandText(formatDuration(pos), 11.sp, dim = true)
        IslandText("−" + formatDuration(duration - pos), 11.sp, dim = true)
    }
}

@Composable
private fun MusicControls(m: MediaState, accent: Color, style: ExButtons, actions: IslandActions, play: Dp, icon: Dp) {
    val c = LocalIslandColors.current
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly, verticalAlignment = Alignment.CenterVertically) {
        IconButton(onClick = { IslandController.touch(); actions.previous() }) {
            Icon(Icons.Rounded.SkipPrevious, "Назад", tint = c.fg, modifier = Modifier.size(icon))
        }
        PlayControl(m.isPlaying, accent, style, play) { IslandController.touch(); actions.playPause() }
        IconButton(onClick = { IslandController.touch(); actions.next() }) {
            Icon(Icons.Rounded.SkipNext, "Вперёд", tint = c.fg, modifier = Modifier.size(icon))
        }
    }
}

@Composable
private fun PlayControl(playing: Boolean, accent: Color, style: ExButtons, size: Dp, onClick: () -> Unit) {
    val c = LocalIslandColors.current
    when (style) {
        ExButtons.FILLED -> Box(Modifier.size(size)) { MorphButton(playing, accent, onClick) }
        ExButtons.TONAL -> Surface(onClick = onClick, shape = CircleShape, color = accent.copy(alpha = 0.22f), modifier = Modifier.size(size)) {
            Box(contentAlignment = Alignment.Center) {
                Icon(if (playing) Icons.Rounded.Pause else Icons.Rounded.PlayArrow, null, tint = accent, modifier = Modifier.size(size * 0.5f))
            }
        }
        ExButtons.ICONS, ExButtons.NONE -> IconButton(onClick = onClick, modifier = Modifier.size(size)) {
            Icon(if (playing) Icons.Rounded.Pause else Icons.Rounded.PlayArrow, null, tint = c.fg, modifier = Modifier.size(size * 0.6f))
        }
    }
}

// ======================= ЗВОНКИ / ТАЙМЕРЫ / LIVE UPDATES =======================

@Composable
private fun OngoingView(a: OngoingActivity, st: EventStyle) {
    val c = LocalIslandColors.current
    val ctx = LocalContext.current
    val tint = c.resolve(st.color, if (a.kind == OngoingKind.CALL) CallGreen else a.accent?.let { Color(it) } ?: Color(a.type.tint))
    val large = st.exLayout == ExLayout.LARGE
    val minimal = st.exLayout == ExLayout.MINIMAL
    val icon = when { minimal -> 36.dp; large -> 72.dp; else -> st.exIcon.dp.dp }
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        if (a.largeIcon != null) BitmapImage(a.largeIcon, icon, icon / 2, null, Icons.Rounded.Notifications, tint)
        else IconBadge(if (a.kind == OngoingKind.CALL) Icons.Rounded.Call else Icons.Rounded.Timer, tint, icon)
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f)) {
            IslandText(a.title.ifBlank { a.appLabel }, if (large) 19.sp else 16.sp, FontWeight.SemiBold)
            if (!minimal && a.text.isNotBlank()) IslandText(a.text, 13.sp, dim = true, maxLines = if (large) 2 else 1)
        }
        if (st.exProgress == ExProgress.RING && a.progress != null) { Ring(a.progress, tint, 30.dp, 3.dp); Spacer(Modifier.width(8.dp)) }
        if (st.exBigValue) {
            val size = if (large) 30.sp else if (minimal) 16.sp else 22.sp
            if (a.chronoBase != null) {
                val now = rememberWallClock()
                val ms = if (a.countDown) a.chronoBase - now else now - a.chronoBase
                IslandText(formatDuration(ms.coerceAtLeast(0)), size, FontWeight.SemiBold, color = tint)
            } else if (a.shortText != null) IslandText(a.shortText, size, FontWeight.SemiBold, color = tint)
        }
    }
    if (!minimal && a.progress != null && (st.exProgress == ExProgress.LINE || st.exProgress == ExProgress.WAVE)) {
        Spacer(Modifier.height(10.dp))
        LinearProgressIndicator(
            progress = { a.progress }, color = tint, trackColor = tint.copy(alpha = 0.2f),
            modifier = Modifier.fillMaxWidth().height(6.dp).clip(CircleShape),
        )
    }
    if (a.actions.isNotEmpty() && st.exButtons != ExButtons.NONE) {
        Spacer(Modifier.height(10.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            a.actions.forEachIndexed { i, act ->
                val danger = a.kind == OngoingKind.CALL && i == a.actions.lastIndex
                val click = { IslandController.touch(); ctx.sendPending(act.intent); Unit }
                when (st.exButtons) {
                    ExButtons.ICONS -> TextButton(onClick = click, modifier = Modifier.weight(1f)) {
                        IslandText(act.title, 13.sp, FontWeight.SemiBold, color = if (danger) Color(Tints.RED) else tint)
                    }
                    else -> {
                        val filled = st.exButtons == ExButtons.FILLED
                        val bg = when { danger -> Color(Tints.RED); filled -> tint; else -> c.fg.copy(alpha = 0.12f) }
                        val fg = when { danger -> Color.White; filled -> if (tint.luminance() > 0.45f) Color.Black else Color.White; else -> c.fg }
                        FilledTonalButton(
                            onClick = click, colors = ButtonDefaults.filledTonalButtonColors(containerColor = bg, contentColor = fg),
                            contentPadding = PaddingValues(horizontal = 8.dp), modifier = Modifier.weight(1f).height(40.dp),
                        ) { IslandText(act.title, 13.sp, color = fg) }
                    }
                }
            }
        }
    }
}
