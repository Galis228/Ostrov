package com.pixelisland.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import com.pixelisland.core.DemoActivity
import com.pixelisland.core.IslandRuntime
import com.pixelisland.core.PeekEvent
import com.pixelisland.settings.EventKind
import com.pixelisland.settings.EventType
import com.pixelisland.settings.SettingsStore
import com.pixelisland.sources.SystemEvents
import com.pixelisland.ui.IslandDims
import com.pixelisland.ui.IslandGeometry
import com.pixelisland.ui.LocalIslandColors
import com.pixelisland.ui.content.CompactContent
import com.pixelisland.ui.content.PeekContent
import com.pixelisland.ui.content.ToastContent
import com.pixelisland.ui.islandColors
import com.pixelisland.ui.theme.IslandTheme

@Composable
fun rememberGeometry(): IslandGeometry {
    val rt by IslandRuntime.geometry.collectAsState()
    val view = LocalView.current
    val d = LocalDensity.current
    val w = with(d) { LocalConfiguration.current.screenWidthDp.dp.roundToPx() }
    return rt ?: remember(w) {
        val r = view.rootWindowInsets?.displayCutout?.boundingRectTop?.takeIf { !it.isEmpty }
        if (r != null) IslandGeometry(r.centerX(), r.top, r.width(), r.height(), w)
        else IslandGeometry(w / 2, with(d) { 10.dp.roundToPx() }, with(d) { 26.dp.roundToPx() }, with(d) { 26.dp.roundToPx() }, w)
    }
}

/** Пилюля события в настоящем стиле, увеличенная для наглядности. Тап — проиграть анимацию ещё раз. */
@Composable
fun EventPreview(type: EventType, modifier: Modifier = Modifier) {
    val s by SettingsStore.flow.collectAsState()
    val g = rememberGeometry()
    val density = LocalDensity.current.density
    val dims = remember(g, s, density) { IslandDims(g, s, density) }
    var replay by remember { mutableIntStateOf(0) }
    val st = s.style(type)
    val (w, h, corner) = when (type.kind) {
        EventKind.TOAST -> Triple(
            dims.widthFor(st, com.pixelisland.ui.content.contentSide(st, previewToastData(type), dims)), dims.compactH, dims.compactCorner,
        )
        EventKind.LIVE -> Triple(
            dims.widthFor(st, com.pixelisland.ui.content.contentSide(st, com.pixelisland.ui.content.slotDataFor(DemoActivity(type)), dims)),
            dims.compactH, dims.compactCorner,
        )
        EventKind.CARD -> Triple(dims.expandedW, dims.peekH, dims.expandedCorner)
    }
    BoxWithConstraints(
        modifier.fillMaxWidth().height(if (type.kind == EventKind.CARD) 150.dp else 92.dp).clickable { replay++ },
        contentAlignment = Alignment.Center,
    ) {
        val k = minOf(if (type.kind == EventKind.CARD) 1f else 1.7f, (maxWidth / w) * 0.94f)
        IslandTheme {
            val colors = islandColors(s)
            CompositionLocalProvider(LocalIslandColors provides colors) {
                Box(
                    Modifier.graphicsLayer { scaleX = k; scaleY = k }.requiredSize(w, h)
                        .clip(RoundedCornerShape(corner))
                        .then(if (colors.brush != null) Modifier.background(colors.brush) else Modifier.background(colors.bg)),
                ) {
                    // «Камера» по центру — видна на светлом фоне.
                    val dot = minOf(dims.cutoutW, dims.cutoutH)
                    Box(
                        Modifier.align(Alignment.TopCenter).offset(y = dims.pad + (dims.cutoutH - dot) / 2)
                            .size(dot).clip(CircleShape).background(Color(0xFF07080A)),
                    )
                    key(replay) {
                        when (type.kind) {
                            EventKind.TOAST -> ToastContent(SystemEvents.sample(type).copy(id = "p$replay"), dims, w)
                            EventKind.LIVE -> CompactContent(DemoActivity(type), 0, 1, dims, w)
                            EventKind.CARD -> PeekContent(
                                PeekEvent("p", "Сообщения", null, "Аня", "Ты уже дома? Я купила тот самый торт", null, null, null), dims,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun previewToastData(type: EventType): com.pixelisland.ui.content.SlotData {
    val e = SystemEvents.sample(type)
    val st = SettingsStore.value.style(type)
    return com.pixelisland.ui.content.SlotData(
        com.pixelisland.ui.iconFor(type, st.variant, st.icon), title = e.title, value = e.value, ring = e.ring, auto = Color(type.tint),
    )
}
