package com.pixelisland.ui.content

import androidx.compose.material3.LocalTextStyle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pixelisland.core.Presentation
import com.pixelisland.settings.EventStyle
import com.pixelisland.settings.IdleContent
import com.pixelisland.settings.RingStyle
import com.pixelisland.settings.SettingsStore
import com.pixelisland.settings.SlotContent
import com.pixelisland.settings.TextWeight
import com.pixelisland.ui.IslandDims
import com.pixelisland.ui.iconFor

/**
 * Ширина пилюли для свёрнутого вида и всплывашки: меряем реальный текст и иконки обоих слотов,
 * берём более широкую сторону — так камера остаётся строго по центру, а всё содержимое помещается.
 */
@Composable
fun pillWidth(p: Presentation, dims: IslandDims): Dp {
    val s = SettingsStore.value
    return when (p) {
        is Presentation.Compact -> {
            val a = p.activity
            if (a == null) idleWidth(dims) else {
                val st = s.style(a.type)
                dims.widthFor(st, contentSide(st, slotDataFor(a), dims))
            }
        }
        is Presentation.Toast -> {
            val e = p.event
            val st = s.style(e.type)
            val d = SlotData(iconFor(e.type, st.variant, st.icon), title = e.title, value = e.value, ring = e.ring,
                auto = androidx.compose.ui.graphics.Color(e.type.tint))
            dims.widthFor(st, contentSide(st, d, dims))
        }
        else -> dims.compactW
    }
}

@Composable
private fun idleWidth(dims: IslandDims): Dp {
    val s = SettingsStore.value
    if (s.idleContent == IdleContent.EMPTY) return dims.minW
    val steps by com.pixelisland.core.IslandRuntime.steps.collectAsState()
    val text = when (s.idleContent) {
        IdleContent.STEPS -> steps?.toString() ?: "—"
        IdleContent.TIME -> "00:00"
        else -> "100%"
    }
    val w = measureText(text, 12.sp, FontWeight.SemiBold, true)
    val side = maxOf(dims.slot * 0.7f, w) * s.contentScale + dims.inset + dims.cameraGap
    return (dims.cutoutW + maxOf(side, s.sideWidthDp.dp) * 2).coerceAtMost(dims.maxW)
}

/** Ширина более широкой стороны (без отступов). */
@Composable
fun contentSide(st: EventStyle, d: SlotData, dims: IslandDims): Dp {
    val l = slotWidth(st.left, d, st, dims.slot)
    val r = slotWidth(st.right, d, st, dims.slot)
    return maxOf(l, r) * SettingsStore.value.contentScale
}

@Composable
private fun slotWidth(c: SlotContent, d: SlotData, st: EventStyle, slot: Dp): Dp {
    val weight = when (st.weight) {
        TextWeight.REGULAR -> FontWeight.Normal; TextWeight.MEDIUM -> FontWeight.SemiBold
        TextWeight.BOLD -> FontWeight.Bold; TextWeight.BLACK -> FontWeight.Black
    }
    fun ring(k: Float) = when (st.ring) {
        RingStyle.RING, RingStyle.FILL -> slot * 0.78f * k
        RingStyle.BAR -> slot * 1.5f * k
        RingStyle.DOTS -> slot * 0.16f * 5 * k + 8.dp
    }
    val value = valueTemplate(d)
    return when (c) {
        SlotContent.NONE -> 0.dp
        SlotContent.ICON, SlotContent.ART, SlotContent.EQ -> slot
        SlotContent.RING -> ring(1f)
        SlotContent.TITLE -> measureText(d.title, 13.sp, weight, st.monoDigits)
        SlotContent.ICON_TITLE -> slot + 7.dp + measureText(d.title, 13.sp, weight, st.monoDigits)
        SlotContent.VALUE -> valueWidth(value, weight, st.monoDigits, slot)
        SlotContent.VALUE_RING -> (if (value.isNotEmpty()) valueWidth(value, weight, st.monoDigits, slot) + 6.dp else 0.dp) + ring(0.92f)
    }
}

/** Для таймеров меряем формат с запасом («00:00» / «0:00:00»), чтобы ширина не прыгала каждую секунду. */
/** Слово-статус может рисоваться галочкой — тогда ширина как у иконки. */
@androidx.compose.runtime.Composable
private fun valueWidth(value: String, weight: FontWeight, tnum: Boolean, slot: Dp): Dp =
    if (SettingsStore.value.statusStyle != com.pixelisland.settings.StatusStyle.TEXT && com.pixelisland.settings.statusOf(value) != null) slot * 0.72f
    else measureText(value, 13.sp, weight, tnum)

private fun valueTemplate(d: SlotData): String {
    val base = d.chronoBase ?: return d.value
    val ms = kotlin.math.abs(base - System.currentTimeMillis())
    return if (ms >= 3_600_000) "0:00:00" else "00:00"
}

@Composable
private fun measureText(text: String, size: androidx.compose.ui.unit.TextUnit, weight: FontWeight, tnum: Boolean): Dp {
    if (text.isEmpty()) return 0.dp
    val m = rememberTextMeasurer(cacheSize = 32)
    val style = LocalTextStyle.current.merge(
        TextStyle(fontSize = size, fontWeight = weight, fontFeatureSettings = if (tnum) "tnum" else null),
    )
    val px = m.measure(text, style, maxLines = 1, softWrap = false).size.width
    return with(LocalDensity.current) { px.toDp() } + 1.dp
}
