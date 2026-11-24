package com.pixelisland.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.pixelisland.core.ChargingActivity
import com.pixelisland.core.DemoActivity
import com.pixelisland.core.LiveActivity
import com.pixelisland.core.NotifAction
import com.pixelisland.core.OngoingActivity
import com.pixelisland.core.OngoingKind
import com.pixelisland.core.OwnTimerActivity
import com.pixelisland.core.PreviewActivity
import com.pixelisland.core.TimerMode
import com.pixelisland.core.TorchActivity
import com.pixelisland.settings.EventStyle
import com.pixelisland.settings.EventType
import com.pixelisland.settings.ExBg
import com.pixelisland.settings.ExButtons
import com.pixelisland.settings.ExIconSize
import com.pixelisland.settings.ExLayout
import com.pixelisland.settings.ExProgress
import com.pixelisland.settings.ExTap
import com.pixelisland.settings.ExWidth
import com.pixelisland.settings.SettingsStore
import com.pixelisland.ui.IslandDims
import com.pixelisland.ui.LocalIslandColors
import com.pixelisland.ui.content.ExpandedContent
import com.pixelisland.ui.expandedWidth
import com.pixelisland.ui.islandColors
import com.pixelisland.ui.theme.IslandTheme
import kotlin.math.roundToInt

private val sampleStart = System.currentTimeMillis()

/** Правдоподобный пример раскрытого вида для каждого типа. */
fun sampleActivity(t: EventType): LiveActivity {
    val now = sampleStart
    fun ongoing(kind: OngoingKind, title: String, text: String, short: String? = null, chrono: Long? = null,
                countDown: Boolean = false, progress: Float? = null, actions: List<String> = emptyList()) =
        OngoingActivity("sample_${t.name}", kind, "", "Пример", title, text, short, null, null, null, chrono, countDown, progress, null,
            actions.map { NotifAction(it, null) })
    return when (t) {
        EventType.MUSIC -> PreviewActivity(true, 0)
        EventType.CALL -> ongoing(OngoingKind.CALL, "Аня", "Мобильный", chrono = now - 83_000, actions = listOf("Без звука", "Динамик", "Сбросить"))
        EventType.TIMER -> ongoing(OngoingKind.TIMER, "Таймер", "Чай", chrono = now + 4 * 60_000, countDown = true, actions = listOf("Пауза", "Сброс"))
        EventType.LIVE_UPDATE -> ongoing(OngoingKind.LIVE, "Через 300 м налево", "ул. Шевченко", short = "5 мин", progress = 0.6f)
        EventType.PROGRESS -> ongoing(OngoingKind.PROGRESS, "video.mp4", "Загрузка", short = "42%", progress = 0.42f, actions = listOf("Отмена"))
        EventType.RECORDING -> ongoing(OngoingKind.RECORDING, "Запись экрана", "Идёт запись", chrono = now - 14_000, actions = listOf("Стоп"))
        EventType.ALARM_RING -> ongoing(OngoingKind.ALARM, "07:00", "Подъём", actions = listOf("Отложить", "Выключить"))
        EventType.DELIVERY -> ongoing(OngoingKind.DELIVERY, "Курьер в пути", "Заказ №1284", short = "7 мин", progress = 0.55f)
        EventType.SCORE -> ongoing(OngoingKind.SCORE, "Динамо 2 – 1 Шахтёр", "Второй тайм", short = "67'")
        EventType.OWN_TIMER -> OwnTimerActivity(TimerMode.TIMER, true, now + 4 * 60_000, 0, 5 * 60_000)
        EventType.TORCH_ON -> TorchActivity("0", 3, 5)
        EventType.CHARGING_LIVE -> ChargingActivity(67, false, 40 * 60_000L)
        else -> DemoActivity(t)
    }
}

/** Превью раскрытого вида с настоящими компонентами острова, уменьшенное под ширину экрана настроек. */
@Composable
fun ExpandedPreview(t: EventType) {
    val s by SettingsStore.flow.collectAsState()
    val g = rememberGeometry()
    val density = LocalDensity.current.density
    val dims = remember(g, s, density) { IslandDims(g, s, density) }
    val a = remember(t) { sampleActivity(t) }
    val st = s.style(t)
    var h by remember { mutableFloatStateOf(200f) }
    BoxWithConstraints(Modifier.fillMaxWidth().padding(vertical = 8.dp), contentAlignment = Alignment.TopCenter) {
        val w = expandedWidth(a, dims)
        val k = minOf(1f, maxWidth / w * 0.96f)
        Box(Modifier.height((h * k).dp), contentAlignment = Alignment.TopCenter) {
            IslandTheme {
                val c = islandColors(s)
                CompositionLocalProvider(LocalIslandColors provides c) {
                    val corner = st.exCorner.takeIf { it > 0f }?.dp ?: dims.expandedCorner
                    Box(
                        Modifier.graphicsLayer { scaleX = k; scaleY = k; transformOrigin = androidx.compose.ui.graphics.TransformOrigin(0.5f, 0f) }
                            .requiredWidth(w).wrapContentHeight(unbounded = true)
                            .clip(RoundedCornerShape(corner))
                            .background(c.bg.copy(alpha = c.bg.alpha * st.exOpacity))
                            .onSizeChanged { h = it.height / density },
                    ) {
                        Box(Modifier.align(Alignment.TopCenter).padding(top = dims.pad).size(minOf(dims.cutoutW, dims.cutoutH)).clip(CircleShape).background(Color(0xFF07080A)))
                        ExpandedContent(a, 0, 1, dims)
                    }
                }
            }
        }
    }
}

/** Схематичная плитка макета. */
@Composable
fun LayoutTile(l: ExLayout) {
    val fg = Color(0xFF8F95A3)
    val ac = MaterialTheme.colorScheme.primary
    Canvas(Modifier.size(52.dp, 36.dp)) {
        fun r(x: Float, y: Float, w: Float, h: Float, c: Color = fg, rad: Float = 3f) =
            drawRoundRect(c, Offset(x * size.width, y * size.height), Size(w * size.width, h * size.height), CornerRadius(rad * density))
        when (l) {
            ExLayout.STANDARD -> { r(0f, 0f, .28f, .4f, ac); r(.34f, .05f, .5f, .12f); r(.34f, .25f, .35f, .1f); r(0f, .58f, 1f, .07f); r(.35f, .78f, .3f, .22f, ac, 8f) }
            ExLayout.LARGE -> { r(0f, 0f, .45f, .6f, ac); r(.52f, .1f, .48f, .15f); r(.52f, .35f, .35f, .12f); r(0f, .75f, 1f, .1f) }
            ExLayout.MINIMAL -> { r(0f, .3f, .22f, .4f, ac); r(.3f, .35f, .45f, .12f); r(.82f, .3f, .18f, .4f, ac, 10f) }
            ExLayout.PLAYER -> { r(0f, .05f, .4f, .9f, ac); r(.47f, .1f, .5f, .13f); r(.47f, .45f, .53f, .07f); r(.6f, .7f, .25f, .25f, ac, 8f) }
        }
    }
}

/** Вкладка «Раскрытие» в редакторе события. */
@Composable
fun ExpandedEditor(t: EventType, st: EventStyle, edit: (EventStyle.() -> EventStyle) -> Unit) {
    var picker by rememberSaveable { mutableStateOf(false) }
    SectionCard("Превью раскрытого вида") { GroupBox { ExpandedPreview(t) } }
    val layouts = if (t == EventType.MUSIC) ExLayout.entries else ExLayout.entries - ExLayout.PLAYER
    SectionCard("Макет", { ApplyAllButton(t, "Макет", liveOnly = true) { a, b -> a.copy(exLayout = if (b.exLayout == ExLayout.PLAYER && a != b) ExLayout.STANDARD else b.exLayout) } }) {
        VisualRow(layouts, st.exLayout, ExLayout::label, { v -> edit { copy(exLayout = v) } }) { l, _ -> LayoutTile(l) }
    }
    SectionCard("Элементы", { ApplyAllButton(t, "Элементы", liveOnly = true) { a, b ->
        a.copy(exHeader = b.exHeader, exIcon = b.exIcon, exProgress = b.exProgress, exButtons = b.exButtons, exBigValue = b.exBigValue)
    } }) {
        SwitchRow("Шапка", "Название приложения у камеры", st.exHeader) { v -> edit { copy(exHeader = v) } }
        VisualRow(ExIconSize.entries, st.exIcon, { "Иконка: " + it.label }, { v -> edit { copy(exIcon = v) } }, tileW = 68.dp, tileH = 44.dp) { s, _ -> ExIconTile(s) }
        VisualRow(ExProgress.entries, st.exProgress, { "Прогресс: " + it.label }, { v -> edit { copy(exProgress = v) } }, tileW = 68.dp, tileH = 40.dp) { p, _ -> ExProgressTile(p) }
        VisualRow(ExButtons.entries, st.exButtons, { "Кнопки: " + it.label }, { v -> edit { copy(exButtons = v) } }, tileW = 68.dp, tileH = 40.dp) { b, _ -> ExButtonsTile(b) }
        SwitchRow("Крупное значение", "Таймер, счёт, проценты", st.exBigValue) { v -> edit { copy(exBigValue = v) } }
    }
    SectionCard("Фон", { ApplyAllButton(t, "Фон", liveOnly = true) { a, b -> a.copy(exBg = b.exBg, exBgColor = b.exBgColor, exOpacity = b.exOpacity) } }) {
        VisualRow(ExBg.entries, st.exBg, ExBg::label, { v -> edit { copy(exBg = v) } }, tileW = 68.dp, tileH = 46.dp) { b, _ -> ExBgTile(b, st.exBgColor) }
        if (st.exBg == ExBg.CUSTOM) GroupBox {
            Row(Modifier.padding(horizontal = 16.dp), verticalAlignment = Alignment.CenterVertically) {
                com.pixelisland.ui.i18n.Text("Цвет фона", modifier = Modifier.weight(1f))
                ColorDot(Color(st.exBgColor), true) { picker = true }
            }
        }
        SliderRow("Непрозрачность", st.exOpacity, 0.3f..1f, { "${(it * 100).roundToInt()}%" }) { v -> edit { copy(exOpacity = v) } }
    }
    SectionCard("Форма", { ApplyAllButton(t, "Форма", liveOnly = true) { a, b -> a.copy(exWidth = b.exWidth, exCorner = b.exCorner) } }) {
        VisualRow(ExWidth.entries, st.exWidth, ExWidth::label, { v -> edit { copy(exWidth = v) } }, tileW = 68.dp, tileH = 46.dp) { w, _ -> ExWidthTile(w) }
        SliderRow("Углы", st.exCorner, 0f..60f, { if (it < 1f) "общие" else "${it.roundToInt()} dp" }) { v -> edit { copy(exCorner = if (v < 1f) 0f else v) } }
    }
    SectionCard("Поведение", { ApplyAllButton(t, "Поведение", liveOnly = true) { a, b -> a.copy(exCollapse = b.exCollapse, exTap = b.exTap) } }) {
        SliderRow("Сворачивать через", st.exCollapse, 0f..30f, { if (it < 1f) "общее" else "${it.roundToInt()} с" }) { v -> edit { copy(exCollapse = if (v < 1f) 0f else v.roundToInt().toFloat()) } }
        VisualRow(ExTap.entries, st.exTap, { "Тап: " + it.label }, { v -> edit { copy(exTap = v) } }, tileW = 68.dp, tileH = 40.dp) { t, _ -> SimpleIconTile(exTapIcon(t)) }
    }
    if (picker) ColorPickerDialog(st.exBgColor, { picker = false }) { c -> edit { copy(exBgColor = c) } }
}

/** Плитка выравнивания: где стоят иконки в «ушах». */
@Composable
fun AlignTile(a: com.pixelisland.settings.SlotAlign) {
    val ac = MaterialTheme.colorScheme.primary
    Canvas(Modifier.size(60.dp, 22.dp)) {
        drawRoundRect(Color(0xFF2A2D35), cornerRadius = CornerRadius(size.height / 2))
        drawCircle(Color(0xFF07080A), size.height * 0.3f, center)
        val r = size.height * 0.22f
        val (lx, rx) = when (a) {
            com.pixelisland.settings.SlotAlign.EDGES -> size.height * 0.5f to size.width - size.height * 0.5f
            com.pixelisland.settings.SlotAlign.CAMERA -> center.x - size.height * 0.75f to center.x + size.height * 0.75f
            com.pixelisland.settings.SlotAlign.CENTER -> size.width * 0.27f to size.width * 0.73f
        }
        drawCircle(ac, r, Offset(lx, center.y)); drawCircle(ac, r, Offset(rx, center.y))
    }
}
