package com.pixelisland.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import com.pixelisland.ui.i18n.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pixelisland.core.DemoActivity
import com.pixelisland.core.PreviewActivity
import com.pixelisland.core.Presentation
import com.pixelisland.settings.BgChoice
import com.pixelisland.settings.ColorChoice
import com.pixelisland.settings.EventKind
import com.pixelisland.settings.EventStyle
import com.pixelisland.settings.EventType
import com.pixelisland.settings.Preset
import com.pixelisland.settings.SettingsStore
import com.pixelisland.sources.SystemEvents
import com.pixelisland.ui.IslandDims
import com.pixelisland.ui.LocalIslandColors
import com.pixelisland.ui.bgPreview
import com.pixelisland.ui.content.AnimatedIcon
import com.pixelisland.ui.content.CompactContent
import com.pixelisland.ui.content.SlotData
import com.pixelisland.ui.content.SlotView
import com.pixelisland.ui.content.pillWidth
import com.pixelisland.ui.content.slotDataFor
import com.pixelisland.ui.iconFor
import com.pixelisland.ui.islandColors
import com.pixelisland.ui.theme.IslandTheme
import kotlin.math.roundToInt

/** Данные-пример для плиток события. */
@Composable
fun sampleSlotData(t: EventType): SlotData {
    val st = SettingsStore.value.style(t)
    return if (t.kind == EventKind.LIVE) slotDataFor(DemoActivity(t)) else {
        val e = SystemEvents.sample(t)
        SlotData(iconFor(t, st.variant, st.icon), title = e.title, value = e.value, ring = e.ring, auto = Color(t.tint))
    }
}

/** Тёмная «витрина» в цветах острова для плиток. */
@Composable
fun IslandSurfaceBox(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    val s by SettingsStore.flow.collectAsState()
    IslandTheme {
        val c = islandColors(s)
        CompositionLocalProvider(LocalIslandColors provides c) {
            Box(modifier.then(if (c.brush != null) Modifier.background(c.brush) else Modifier.background(c.bg)), contentAlignment = Alignment.Center) {
                content()
            }
        }
    }
}

/** Горизонтальный ряд плиток-превью с подписью. */
@Composable
fun <T> VisualRow(
    options: List<T>, selected: T, label: (T) -> String, onSelect: (T) -> Unit,
    tileW: Dp = 72.dp, tileH: Dp = 52.dp, tile: @Composable (T, Boolean) -> Unit,
) {
    Row(
        Modifier.groupItem().horizontalScroll(rememberScrollState()).padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        options.forEach { o ->
            val sel = o == selected
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(tileW)) {
                IslandSurfaceBox(
                    Modifier.size(tileW, tileH).clip(RoundedCornerShape(16.dp))
                        .border(if (sel) 3.dp else 0.dp, if (sel) MaterialTheme.colorScheme.primary else Color.Transparent, RoundedCornerShape(16.dp))
                        .clickable { onSelect(o) },
                ) { tile(o, sel) }
                Text(
                    label(o), style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, lineHeight = 12.sp), maxLines = 1,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis, textAlign = TextAlign.Center,
                    color = if (sel) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp),
                )
            }
        }
    }
}

/** Плитка «содержимое слота». */
@Composable
fun SlotTile(t: EventType, st: EventStyle, c: com.pixelisland.settings.SlotContent) {
    val d = sampleSlotData(t)
    val color = LocalIslandColors.current.resolve(st.color, d.auto)
    Box(Modifier.graphicsLayer { scaleX = 0.85f; scaleY = 0.85f }) {
        if (c == com.pixelisland.settings.SlotContent.NONE) Text("—", color = LocalIslandColors.current.dim)
        else SlotView(c, d, st.copy(marquee = false), 26.dp, color)
    }
}

/** Плитка «иконка с подложкой / анимацией»: анимация проигрывается при выборе. */
@Composable
fun IconTile(t: EventType, st: EventStyle, replayKey: Any) {
    val d = sampleSlotData(t).copy(key = replayKey)
    val color = LocalIslandColors.current.resolve(st.color, d.auto)
    AnimatedIcon(d, st, 30.dp, color)
}

/**
 * Сцена «пилюля у камеры»: тяни пилюлю — сдвиг, правую ручку — минимальная ширина, нижнюю — высота.
 */
@Composable
fun PillStage() {
    val s by SettingsStore.flow.collectAsState()
    val g = rememberGeometry()
    val density = LocalDensity.current.density
    val dims = remember(g, s, density) { IslandDims(g, s, density) }
    val cs = MaterialTheme.colorScheme
    val k = 1.6f
    val pres = Presentation.Compact(PreviewActivity(true, 0), 0, 1)
    var dragging by remember { mutableStateOf(false) }
    val shifted = kotlin.math.abs(s.offsetXDp) >= 0.5f || kotlin.math.abs(s.offsetYDp) >= 0.5f
    BoxWithConstraints(Modifier.fillMaxWidth().height(150.dp).clip(RoundedCornerShape(24.dp)).background(cs.surfaceContainerHigh)) {
        val w = pillWidth(pres, dims)
        val h = dims.compactH
        // Единая точка отсчёта: центр камеры на сцене. Всё остальное считается от неё.
        val camCx = maxWidth / 2
        val camCy = 34.dp + dims.cutoutH * k / 2
        val pillCx = camCx + s.offsetXDp.dp * k
        val pillCy = camCy + s.offsetYDp.dp * k
        val pillW = w * k; val pillH = h * k
        val minW = dims.minW * k
        val dot = minOf(dims.cutoutW, dims.cutoutH) * k

        // минимальная ширина — отдельным цветом (третичный), под пилюлей
        Canvas(Modifier.offset(pillCx - minW / 2, pillCy - pillH / 2).requiredSize(minW, pillH)) {
            val r = androidx.compose.ui.geometry.CornerRadius(size.height / 2)
            drawRoundRect(cs.tertiary.copy(alpha = 0.18f), cornerRadius = r)
        }
        // пилюля: масштаб от центра, контейнер того же размера, что и видимая пилюля
        Box(
            Modifier.offset(pillCx - pillW / 2, pillCy - pillH / 2).requiredSize(pillW, pillH)
                .pointerInput(Unit) {
                    detectDragGesturesAfterLongPress(
                        onDragStart = { dragging = true },
                        onDragEnd = { dragging = false },
                        onDragCancel = { dragging = false },
                    ) { ch, drag ->
                        ch.consume()
                        SettingsStore.update {
                            copy(
                                offsetXDp = (offsetXDp + drag.x / density / k).coerceIn(-80f, 80f),
                                offsetYDp = (offsetYDp + drag.y / density / k).coerceIn(-20f, 60f),
                            )
                        }
                    }
                },
            contentAlignment = Alignment.Center,
        ) {
            IslandSurfaceBox(Modifier.requiredSize(w, h).graphicsLayer { scaleX = k; scaleY = k }.clip(RoundedCornerShape(dims.compactCorner))) {
                CompactContent(PreviewActivity(true, 0), 0, 1, dims, w)
            }
            if (dragging) Box(Modifier.matchParentSize().border(2.dp, cs.primary, RoundedCornerShape(pillH / 2)))
        }
        // контур минимальной ширины — поверх пилюли, тем же третичным цветом
        Canvas(Modifier.offset(pillCx - minW / 2, pillCy - pillH / 2).requiredSize(minW, pillH)) {
            drawRoundRect(
                cs.tertiary, style = Stroke(1.5.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 8f))),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(size.height / 2),
            )
        }
        // камера — всегда в точке отсчёта
        Box(Modifier.offset(camCx - dot / 2, camCy - dot / 2).size(dot).clip(CircleShape).background(Color(0xFF07080A)))
        if (shifted) {
            // где остров стоял бы без сдвига
            Box(Modifier.offset(camCx - 0.75.dp, camCy - pillH / 2 - 6.dp).size(1.5.dp, pillH + 12.dp).background(cs.error.copy(alpha = 0.6f)))
        }
        // ручка ширины — на правом краю минимальной ширины
        Handle(Modifier.offset(pillCx + minW / 2 - 10.dp, pillCy - 10.dp)) { d ->
            SettingsStore.update { copy(sideWidthDp = (sideWidthDp + d.x / density / k).coerceIn(0f, 80f)) }
        }
        // ручка высоты — по центру нижнего края пилюли
        Handle(Modifier.offset(pillCx - 10.dp, pillCy + pillH / 2 - 10.dp)) { d ->
            SettingsStore.update { copy(pillPadDp = (pillPadDp + d.y / density / k / 2).coerceIn(0f, 16f)) }
        }
        if (shifted) {
            androidx.compose.material3.FilledTonalButton(
                onClick = { SettingsStore.update { copy(offsetXDp = 0f, offsetYDp = 0f) } },
                modifier = Modifier.align(Alignment.TopEnd).padding(6.dp).height(32.dp),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp),
            ) { Text("Вернуть к камере", style = MaterialTheme.typography.labelMedium) }
        }
        Text(
            if (shifted) "Остров сдвинут: ${s.offsetXDp.roundToInt()} / ${s.offsetYDp.roundToInt()} dp от камеры"
            else "Удерживайте пилюлю, чтобы сдвинуть. Пунктир — минимальная ширина",
            style = MaterialTheme.typography.labelSmall,
            color = if (shifted) cs.error else cs.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 8.dp, start = 12.dp, end = 12.dp),
        )
    }
}

@Composable
private fun Handle(modifier: Modifier, onDrag: (Offset) -> Unit) {
    Box(
        modifier.size(20.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primary)
            .border(3.dp, MaterialTheme.colorScheme.onPrimary, CircleShape)
            .pointerInput(Unit) { detectDragGestures { ch, d -> ch.consume(); onDrag(d) } },
    )
}

/** Мини-пилюля пресета: фон, подложка и цвета именно этого пресета. */
@Composable
fun PresetTile(p: Preset) {
    val cur by SettingsStore.flow.collectAsState()
    val ps = remember(cur, p) { p.apply(cur) }
    val st = ps.style(EventType.HP_ON)
    val cs = MaterialTheme.colorScheme
    val bg = bgPreview(ps.bg, cs).copy(alpha = ps.bgOpacity)
    val accent = cs.primary
    val color = when (st.color) {
        ColorChoice.AUTO -> Color(EventType.HP_ON.tint); ColorChoice.MONET -> accent; ColorChoice.MONO -> Color.White
        else -> Color(st.color)
    }
    var replay by remember { mutableIntStateOf(0) }
    Box(
        Modifier.fillMaxWidth().height(44.dp).padding(horizontal = 6.dp).clip(RoundedCornerShape(22.dp)).background(bg)
            .then(if (ps.outline) Modifier.border(1.dp, accent.copy(alpha = 0.6f), RoundedCornerShape(22.dp)) else Modifier)
            .clickable { replay++ },
        contentAlignment = Alignment.Center,
    ) {
        Row(Modifier.fillMaxWidth().padding(horizontal = 8.dp), verticalAlignment = Alignment.CenterVertically) {
            IslandTheme {
                CompositionLocalProvider(LocalIslandColors provides com.pixelisland.ui.IslandColors(bg, Color.White, Color(0x99FFFFFF), accent)) {
                    AnimatedIcon(SlotData(iconFor(EventType.HP_ON, st.variant, st.icon), auto = color, key = replay), st, 28.dp, color)
                }
            }
            Spacer(Modifier.weight(1f))
            Box(Modifier.size(14.dp).clip(CircleShape).background(Color(0xFF07080A)))
            Spacer(Modifier.weight(1f))
            Text("84%", color = color, fontSize = 13.sp)
        }
    }
}

/** Мини-пилюля для режима ширины. */
@Composable
fun WidthTile(mode: com.pixelisland.settings.WidthMode) {
    val frac = when (mode) {
        com.pixelisland.settings.WidthMode.AUTO -> 0.7f; com.pixelisland.settings.WidthMode.NARROW -> 0.45f
        com.pixelisland.settings.WidthMode.WIDE -> 0.95f; com.pixelisland.settings.WidthMode.CUSTOM -> 0.6f
    }
    Box(Modifier.fillMaxWidth(frac).height(16.dp).clip(RoundedCornerShape(50)).background(Color(0xFF2A2D35)), contentAlignment = Alignment.Center) {
        Box(Modifier.size(10.dp).clip(CircleShape).background(Color(0xFF07080A)))
    }
}
