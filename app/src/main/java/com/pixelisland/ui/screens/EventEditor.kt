package com.pixelisland.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Colorize
import androidx.compose.material.icons.rounded.GridView
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.RestartAlt
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import com.pixelisland.ui.i18n.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.pixelisland.settings.ColorChoice
import com.pixelisland.settings.EventKind
import com.pixelisland.settings.EventStyle
import com.pixelisland.settings.EventType
import com.pixelisland.settings.IconAnim
import com.pixelisland.settings.IconContainer
import com.pixelisland.settings.IconVariant
import com.pixelisland.settings.RingStyle
import com.pixelisland.settings.SettingsStore
import com.pixelisland.settings.SlotContent
import com.pixelisland.settings.TextWeight
import com.pixelisland.sources.SystemEvents
import com.pixelisland.ui.iconFor
import com.pixelisland.ui.content.SlotView
import kotlin.math.roundToInt

/** Редактор события: превью закреплено сверху, настройки разложены по подвкладкам. */
@Composable
fun EventEditor(t: EventType) {
    val s by SettingsStore.flow.collectAsState()
    val st = s.style(t)
    fun edit(block: EventStyle.() -> EventStyle) = SettingsStore.updateStyle(t, block)
    val tabs = buildList {
        add("Раскладка")
        if (t.kind != EventKind.CARD) { add("Иконка"); add("Стиль"); add("Цвет") }
        if (t.kind == EventKind.LIVE) add("Раскрытие")
    }
    var tab by rememberSaveable(t) { mutableIntStateOf(0) }
    var catalog by rememberSaveable { mutableStateOf(false) }
    var picker by rememberSaveable { mutableStateOf(false) }
    var animReplay by androidx.compose.runtime.remember { mutableIntStateOf(0) }

    Column(Modifier.fillMaxSize()) {
        UndoBanner()
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp),
        ) {
            Row(Modifier.padding(start = 16.dp, end = 8.dp, top = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                Text(t.hint, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.weight(1f))
                IconButton(onClick = { SystemEvents.demo(t) }) { Icon(Icons.Rounded.PlayArrow, "Показать на острове") }
                IconButton(onClick = { SettingsStore.update { copy(events = events - t) } }) { Icon(Icons.Rounded.RestartAlt, "Сброс") }
                Switch(st.enabled, { v -> edit { copy(enabled = v) } })
            }
            EventPreview(t)
        }
        if (tabs.size > 1) SubTabs(tabs, tab.coerceAtMost(tabs.lastIndex), { tab = it })
        ScrollBody(Modifier.weight(1f)) {
            when (tabs.getOrNull(tab.coerceAtMost(tabs.lastIndex))) {
                "Раскладка" -> {
                    if (t.slots.isNotEmpty()) {
                        SectionCard("Слева от камеры") { VisualRow(t.slots, st.left, SlotContent::label, { v -> edit { copy(left = v) } }) { c, _ -> SlotTile(t, st, c) } }
                        SectionCard("Справа от камеры") { VisualRow(t.slots, st.right, SlotContent::label, { v -> edit { copy(right = v) } }) { c, _ -> SlotTile(t, st, c) } }
                    }
                    if (t.kind != EventKind.CARD) SectionCard("Выравнивание", { ApplyAllButton(t, "Выравнивание") { a, b -> a.copy(align = b.align) } }) {
                        VisualRow(com.pixelisland.settings.SlotAlign.entries, st.align, com.pixelisland.settings.SlotAlign::label,
                            { v -> edit { copy(align = v) } }) { al, _ -> AlignTile(al) }
                    }
                    if (t.kind != EventKind.CARD) SectionCard("Ширина пилюли", { ApplyAllButton(t, "Ширина") { a, b -> a.copy(widthMode = b.widthMode, sideDp = b.sideDp) } }) {
                        VisualRow(com.pixelisland.settings.WidthMode.entries, st.widthMode, com.pixelisland.settings.WidthMode::label,
                            { v -> edit { copy(widthMode = v) } }) { m, _ -> WidthTile(m) }
                        when (st.widthMode) {
                            com.pixelisland.settings.WidthMode.CUSTOM ->
                                SliderRow("С каждой стороны", st.sideDp, 0f..180f, { "${it.roundToInt()} dp" }) { v -> edit { copy(sideDp = v) } }
                            com.pixelisland.settings.WidthMode.AUTO ->
                                GroupBox { Hint("Ширина подстраивается под текст и иконки — всё помещается. Пределы задаются во вкладке Вид → Размер.") }
                            com.pixelisland.settings.WidthMode.NARROW ->
                                GroupBox { Hint("Минимальная ширина из Вид → Размер. Длинный текст может обрезаться.") }
                            com.pixelisland.settings.WidthMode.WIDE ->
                                GroupBox { Hint("Ширина «Широкая» из Вид → Размер.") }
                        }
                    }
                    if (t.kind != EventKind.LIVE) SectionCard(null) {
                        SliderRow("Показывать", st.durationSec, 0f..8f, {
                            if (it < 0.5f) "общая" else "${"%.1f".format(it)} с"
                        }) { v -> edit { copy(durationSec = if (v < 0.5f) 0f else (v * 2).roundToInt() / 2f) } }
                    }
                }
                "Иконка" -> {
                    SectionCard("Иконка") {
                        GroupBox { Row(Modifier.padding(horizontal = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                Modifier.size(48.dp).clip(RoundedCornerShape(14.dp)).background(MaterialTheme.colorScheme.primaryContainer),
                                contentAlignment = Alignment.Center,
                            ) { Icon(iconFor(t, st.variant, st.icon), null, tint = MaterialTheme.colorScheme.onPrimaryContainer) }
                            Spacer(Modifier.width(12.dp))
                            Text(if (st.icon.isEmpty()) "Стандартная" else st.icon, modifier = Modifier.weight(1f))
                            FilledTonalButton(onClick = { catalog = true }) {
                                Icon(Icons.Rounded.GridView, null, Modifier.size(18.dp)); Spacer(Modifier.width(6.dp)); Text("Каталог")
                            }
                        } }
                    }
                    SectionCard("Стиль Material", { ApplyAllButton(t, "Стиль Material") { a, b -> a.copy(variant = b.variant) } }) {
                        GroupBox { Row(
                            Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(horizontal = 12.dp, vertical = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            IconVariant.entries.forEach { v ->
                                val sel = st.variant == v
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Box(
                                        Modifier.size(46.dp).clip(RoundedCornerShape(if (sel) 14.dp else 23.dp))
                                            .background(if (sel) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceContainerHighest)
                                            .clickable { edit { copy(variant = v) } },
                                        contentAlignment = Alignment.Center,
                                    ) { Icon(iconFor(t, v, st.icon), v.label, tint = if (sel) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface) }
                                    Text(v.label, style = MaterialTheme.typography.labelSmall)
                                }
                            }
                        } }
                    }
                    SectionCard("Подложка", { ApplyAllButton(t, "Подложка") { a, b -> a.copy(container = b.container) } }) { VisualRow(IconContainer.entries, st.container, IconContainer::label, { v -> edit { copy(container = v) } }, tileW = 64.dp) { c, _ -> IconTile(t, st.copy(container = c, anim = IconAnim.STATIC), c) } }
                }
                "Стиль" -> {
                    SectionCard("Анимация иконки", { ApplyAllButton(t, "Анимация") { a, b -> a.copy(anim = b.anim) } }) { VisualRow(IconAnim.entries, st.anim, IconAnim::label, { v -> edit { copy(anim = v) }; animReplay++ }, tileW = 64.dp) { a, sel -> IconTile(t, st.copy(anim = a), if (sel) animReplay else a) } }
                    SectionCard("Текст", { ApplyAllButton(t, "Текст") { a, b -> a.copy(weight = b.weight, monoDigits = b.monoDigits, marquee = b.marquee) } }) {
                        VisualRow(TextWeight.entries, st.weight, TextWeight::label, { v -> edit { copy(weight = v) } }, tileW = 60.dp, tileH = 46.dp) { w, _ -> WeightTile(w) }
                        SwitchRow("Ровные цифры", "Таймер не «прыгает»", st.monoDigits) { v -> edit { copy(monoDigits = v) } }
                        SwitchRow("Бегущая строка", "Для длинных названий", st.marquee) { v -> edit { copy(marquee = v) } }
                    }
                    SectionCard("Прогресс", { ApplyAllButton(t, "Прогресс") { a, b -> a.copy(ring = b.ring, ringThick = b.ringThick) } }) {
                        VisualRow(RingStyle.entries, st.ring, RingStyle::label, { v -> edit { copy(ring = v) } }) { r, _ ->
                            val d = sampleSlotData(t).copy(ring = 0.65f)
                            SlotView(SlotContent.RING, d, st.copy(ring = r), 28.dp, com.pixelisland.ui.LocalIslandColors.current.resolve(st.color, d.auto))
                        }
                        SliderRow("Толщина", st.ringThick, 1f..6f, { "${it.roundToInt()} dp" }) { v -> edit { copy(ringThick = v) } }
                    }
                }
                "Цвет" -> SectionCard("Цвет", { ApplyAllButton(t, "Цвет") { a, b -> a.copy(color = b.color) } }) { GroupBox {
                    Row(
                        Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically,
                    ) {
                        AutoDot(Color(t.tint), st.color == ColorChoice.AUTO) { edit { copy(color = ColorChoice.AUTO) } }
                        ColorDot(MaterialTheme.colorScheme.primary, st.color == ColorChoice.MONET, "M") { edit { copy(color = ColorChoice.MONET) } }
                        ColorDot(Color.White, st.color == ColorChoice.MONO, "Aa") { edit { copy(color = ColorChoice.MONO) } }
                        val custom = st.color > 2 && st.color !in ColorChoice.palette
                        Box(
                            Modifier.size(36.dp).clip(CircleShape)
                                .background(if (custom) Color(st.color) else MaterialTheme.colorScheme.surfaceContainerHighest)
                                .border(if (custom) 3.dp else 1.dp, MaterialTheme.colorScheme.primary, CircleShape)
                                .clickable { picker = true },
                            contentAlignment = Alignment.Center,
                        ) { Icon(Icons.Rounded.Colorize, "Свой цвет", Modifier.size(18.dp)) }
                    }
                    Row(
                        Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(horizontal = 12.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) { ColorChoice.palette.forEach { c -> ColorDot(Color(c), st.color == c) { edit { copy(color = c) } } } }
                    Hint(
                        when (st.color) {
                            ColorChoice.AUTO -> if (t == EventType.MUSIC) "Авто: из обложки" else "Авто: фирменный цвет события"
                            ColorChoice.MONET -> "Monet: из ваших обоев"
                            ColorChoice.MONO -> "Моно: цвет текста острова"
                            else -> "Свой цвет"
                        },
                    )
                } }
                "Раскрытие" -> ExpandedEditor(t, st, ::edit)
                else -> Unit
            }
        }
    }
    if (catalog) IconCatalogSheet(t, st.variant, st.icon, { catalog = false }) { name -> edit { copy(icon = name) } }
    if (picker) ColorPickerDialog(if (st.color > 2) st.color else t.tint, { picker = false }) { c -> edit { copy(color = c) } }
}

@Composable
private fun AutoDot(color: Color, selected: Boolean, onClick: () -> Unit) {
    val border = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
    Box(
        Modifier.size(36.dp).border(if (selected) 3.dp else 1.dp, border, CircleShape).padding(if (selected) 4.dp else 1.dp)
            .clip(CircleShape)
            .background(Brush.sweepGradient(listOf(color, Color(0xFFB89CFF), Color(0xFF5CE1E6), color)))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) { Text("A", color = Color.Black, style = MaterialTheme.typography.labelMedium) }
}
