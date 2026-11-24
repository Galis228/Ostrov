package com.pixelisland.ui.content

import android.graphics.Bitmap
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import com.pixelisland.ui.i18n.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pixelisland.ui.IslandDims
import com.pixelisland.ui.LocalIslandColors
import kotlinx.coroutines.delay

/** Раскладка пилюли: слот слева от камеры и слот справа. Содержимое слотов масштабируется. */
@Composable
fun CompactRow(
    dims: IslandDims, width: Dp, left: @Composable () -> Unit, right: @Composable () -> Unit,
    leftAlign: Alignment = Alignment.CenterStart, rightAlign: Alignment = Alignment.CenterEnd,
) {
    val inset = dims.inset
    val base = LocalDensity.current
    val scaled = Density(base.density * dims.s.contentScale, base.fontScale)
    Row(Modifier.size(width, dims.compactH), verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.weight(1f).padding(start = inset, end = dims.cameraGap), contentAlignment = leftAlign) {
            CompositionLocalProvider(LocalDensity provides scaled) { left() }
        }
        Spacer(Modifier.width(dims.cutoutW))
        Box(Modifier.weight(1f).padding(end = inset, start = dims.cameraGap), contentAlignment = rightAlign) {
            CompositionLocalProvider(LocalDensity provides scaled) { right() }
        }
    }
}

fun slotSize(dims: IslandDims): Dp = dims.slot

@Composable
fun IslandText(
    text: String, size: TextUnit = 14.sp, weight: FontWeight = FontWeight.Medium,
    dim: Boolean = false, color: Color? = null, maxLines: Int = 1, modifier: Modifier = Modifier,
) {
    val c = LocalIslandColors.current
    Text(
        text, color = color ?: if (dim) c.dim else c.fg, fontSize = size, fontWeight = weight,
        maxLines = maxLines, overflow = TextOverflow.Ellipsis, modifier = modifier,
    )
}

@Composable
fun BitmapImage(bmp: Bitmap?, size: Dp, corner: Dp, tint: Color? = null, fallback: ImageVector, fallbackTint: Color) {
    val shape = RoundedCornerShape(corner)
    if (bmp != null) {
        val img = remember(bmp) { bmp.asImageBitmap() }
        Image(
            img, null, contentScale = ContentScale.Crop,
            colorFilter = tint?.let { ColorFilter.tint(it) },
            modifier = Modifier.size(size).clip(shape),
        )
    } else {
        Icon(fallback, null, tint = fallbackTint, modifier = Modifier.size(size))
    }
}

/** Иконка в цветном кружке. */
@Composable
fun IconBadge(icon: ImageVector, tint: Color, size: Dp) {
    Box(
        Modifier.size(size).clip(CircleShape).background(tint.copy(alpha = 0.22f)),
        contentAlignment = Alignment.Center,
    ) { Icon(icon, null, tint = tint, modifier = Modifier.size(size * 0.58f)) }
}

@Composable
fun Ring(progress: Float, color: Color, size: Dp, stroke: Dp = 3.dp) {
    val p by animateFloatAsState(progress.coerceIn(0f, 1f), tween(500), label = "ring")
    CircularProgressIndicator(
        progress = { p }, modifier = Modifier.size(size), color = color,
        trackColor = color.copy(alpha = 0.22f), strokeWidth = stroke, gapSize = 0.dp,
    )
}

/** Тикающие «настенные» часы для хронометров. */
@Composable
fun rememberWallClock(periodMs: Long = 1000): Long {
    var now by remember { mutableLongStateOf(System.currentTimeMillis()) }
    LaunchedEffect(periodMs) { while (true) { now = System.currentTimeMillis(); delay(periodMs - now % periodMs) } }
    return now
}

fun formatDuration(ms: Long): String {
    val s = kotlin.math.abs(ms) / 1000
    val h = s / 3600
    return if (h > 0) "%d:%02d:%02d".format(h, (s / 60) % 60, s % 60) else "%d:%02d".format(s / 60, s % 60)
}

/**
 * «Прыгающие» столбики эквалайзера. Высота читается только на этапе отрисовки (graphicsLayer),
 * поэтому нет перекомпоновки каждый кадр, а на паузе анимация полностью останавливается.
 */
@Composable
fun Equalizer(playing: Boolean, color: Color, modifier: Modifier = Modifier) {
    // Эквалайзер «танцует» 6 секунд после старта/смены трека, а потом застывает на разной высоте.
    // Бесконечная анимация заставляла оверлей перерисовываться 60 раз в секунду всё время, пока играет музыка.
    val durations = listOf(420, 560, 380, 500)
    val rest = listOf(0.55f, 0.9f, 0.4f, 0.7f)
    Row(modifier, horizontalArrangement = Arrangement.spacedBy(2.dp), verticalAlignment = Alignment.CenterVertically) {
        durations.forEachIndexed { i, d ->
            val bar = remember { androidx.compose.animation.core.Animatable(0.22f) }
            LaunchedEffect(playing) {
                if (!playing) { bar.animateTo(0.22f, tween(160)); return@LaunchedEffect }
                if (com.pixelisland.core.IslandRuntime.reduceMotion.value) { bar.snapTo(rest[i]); return@LaunchedEffect }
                delay(i * 60L)
                var up = true
                val end = System.currentTimeMillis() + 6_000
                while (System.currentTimeMillis() < end) {
                    bar.animateTo(if (up) 1f else 0.25f, tween(d, easing = FastOutSlowInEasing))
                    up = !up
                }
                bar.animateTo(rest[i], tween(300))
            }
            Box(
                Modifier.weight(1f).fillMaxHeight()
                    .graphicsLayer { scaleY = bar.value }
                    .clip(RoundedCornerShape(50)).background(color),
            )
        }
    }
}

/** Точки-индикатор, если активностей несколько. */
@Composable
fun PageDots(index: Int, count: Int) {
    if (count < 2) return
    val c = LocalIslandColors.current
    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        repeat(count) { i ->
            Box(Modifier.size(if (i == index) 6.dp else 4.dp).clip(CircleShape).background(if (i == index) c.fg else c.dim.copy(alpha = 0.4f)))
        }
    }
}

/** Выравнивание содержимого слота по настройке события. */
fun com.pixelisland.settings.SlotAlign.left(): Alignment = when (this) {
    com.pixelisland.settings.SlotAlign.EDGES -> Alignment.CenterStart
    com.pixelisland.settings.SlotAlign.CAMERA -> Alignment.CenterEnd
    com.pixelisland.settings.SlotAlign.CENTER -> Alignment.Center
}

fun com.pixelisland.settings.SlotAlign.right(): Alignment = when (this) {
    com.pixelisland.settings.SlotAlign.EDGES -> Alignment.CenterEnd
    com.pixelisland.settings.SlotAlign.CAMERA -> Alignment.CenterStart
    com.pixelisland.settings.SlotAlign.CENTER -> Alignment.Center
}
