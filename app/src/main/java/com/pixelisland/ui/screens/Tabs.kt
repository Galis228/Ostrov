package com.pixelisland.ui.screens

import androidx.compose.runtime.remember
import androidx.compose.material.icons.rounded.BugReport
import androidx.compose.material3.Button
import androidx.compose.material.icons.rounded.AutoAwesome
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
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
import androidx.compose.material.icons.rounded.CenterFocusStrong
import androidx.compose.material.icons.rounded.Colorize
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.KeyboardDoubleArrowDown
import androidx.compose.material.icons.rounded.KeyboardDoubleArrowUp
import androidx.compose.material.icons.rounded.OpenInFull
import androidx.compose.material.icons.rounded.RestartAlt
import androidx.compose.material.icons.rounded.SwapHoriz
import androidx.compose.material.icons.rounded.TouchApp
import androidx.compose.material.icons.rounded.Upload
import androidx.compose.material.icons.rounded.Campaign
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import com.pixelisland.ui.i18n.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.draw.scale
import androidx.compose.ui.unit.dp
import com.pixelisland.core.IslandController
import com.pixelisland.core.IslandRuntime
import com.pixelisland.settings.AnimStyle
import com.pixelisland.settings.AppearStyle
import com.pixelisland.settings.BgChoice
import com.pixelisland.settings.EventType
import com.pixelisland.settings.GestureAction
import com.pixelisland.settings.GradientStyle
import com.pixelisland.settings.IslandSettings
import com.pixelisland.settings.Preset
import com.pixelisland.settings.ExLayout
import com.pixelisland.settings.SettingsStore
import com.pixelisland.sources.NotificationSource
import com.pixelisland.sources.SystemEvents
import com.pixelisland.ui.bgPreview
import kotlin.math.roundToInt

private fun dp(v: Float) = "${v.roundToInt()}"
private fun pct(v: Float) = "${(v * 100).roundToInt()}%"
private fun set(block: IslandSettings.() -> IslandSettings) = SettingsStore.update(block)

/** Пока открыт раздел — на настоящем острове крутится демо-трек. Сверху — две быстрые кнопки. */
@Composable
fun LivePreviewBar() {
    WhileAppVisible(
        onShow = { IslandController.previewMode.value = true },
        onHide = { IslandController.previewMode.value = false; IslandController.collapse(); IslandController.appHidden() },
    )
    Row(Modifier.fillMaxWidth().padding(horizontal = 12.dp), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
        Text("Смотрите на остров ↑", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.weight(1f))
        AssistChip(onClick = { IslandController.togglePreviewExpanded() }, label = { Text("Раскрыть") },
            leadingIcon = { Icon(Icons.Rounded.OpenInFull, null, Modifier.size(16.dp)) })
        AssistChip(onClick = { SystemEvents.demo(EventType.HP_ON) }, label = { Text("Всплывашка") },
            leadingIcon = { Icon(Icons.Rounded.Campaign, null, Modifier.size(16.dp)) })
    }
}

@Composable
fun Tabbed(titles: List<String>, start: Int = 0, top: @Composable () -> Unit = {}, body: @Composable ColumnScope.(Int) -> Unit) {
    var tab by rememberSaveable(start) { mutableIntStateOf(start.coerceIn(0, titles.lastIndex)) }
    Column(Modifier.fillMaxSize()) {
        top()
        SubTabs(titles, tab, { tab = it })
        ScrollBody(Modifier.weight(1f)) { body(tab) }
    }
}

// =========================== ВИД ===========================

@Composable
fun LookTab(start: Int = 0, openCalibration: () -> Unit) {
    val s by SettingsStore.flow.collectAsState()
    var pick by rememberSaveable { mutableStateOf(0) } // 0 — нет, 1 — фон, 2 — цвет градиента
    Tabbed(listOf("Пресеты", "Размер", "Фон", "Эффекты", "Анимация", "Все события"), start = start, top = { LivePreviewBar(); UndoBanner() }) { tab ->
        when (tab) {
            0 -> {
                Hint("Пресет меняет оформление всех событий. Включённые события не трогаются.")
                ThemeShareRow()
                Preset.entries.chunked(2).forEach { row ->
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        row.forEach { p ->
                            Card(
                                onClick = { SettingsStore.update { p.apply(this) } },
                                shape = RoundedCornerShape(20.dp), modifier = Modifier.weight(1f),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
                            ) {
                                Column(Modifier.padding(vertical = 10.dp)) {
                                    PresetTile(p)
                                    Text(p.title, style = MaterialTheme.typography.titleSmall, modifier = Modifier.padding(start = 12.dp, top = 8.dp))
                                    Text(p.hint, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.padding(horizontal = 12.dp))
                                }
                            }
                        }
                        if (row.size == 1) Spacer(Modifier.weight(1f))
                    }
                }
            }
            1 -> {
                PillStage()
                SectionCard(null) {
                    NavRow(Icons.Rounded.CenterFocusStrong, "Калибровка выреза",
                        if (s.cutoutManual) "Вручную: ${s.cutW}×${s.cutH} px" else "По данным системы", openCalibration)
                }
                SectionCard("Ширина, dp с каждой стороны") {
                    SliderRow("Минимум", s.sideWidthDp, 0f..80f, ::dp) { set { copy(sideWidthDp = it) } }
                    SliderRow("Предел авто", s.autoMaxSideDp, 40f..220f, ::dp) { set { copy(autoMaxSideDp = it) } }
                    SliderRow("«Широкая»", s.toastSideDp, 40f..220f, ::dp) { set { copy(toastSideDp = it) } }
                    VisualRow(com.pixelisland.settings.WidthMode.entries.filter { it != com.pixelisland.settings.WidthMode.CUSTOM }, s.style(com.pixelisland.settings.EventType.HP_ON).widthMode,
                        { it.label }, { v ->
                        SettingsStore.update {
                            com.pixelisland.settings.EventType.entries.fold(this) { acc, t -> acc.withStyle(t, acc.style(t).copy(widthMode = v)) }
                        }
                    }) { m, _ -> WidthTile(m) }
                }
                SectionCard("Пилюля") {
                    SliderRow("Высота", s.pillPadDp, 0f..16f, ::dp) { set { copy(pillPadDp = it) } }
                    SliderRow("Скругление", s.roundness, 0.2f..1f, ::pct) { set { copy(roundness = it) } }
                    SliderRow("Иконки и текст", s.contentScale, 0.6f..1.4f, ::pct) { set { copy(contentScale = it) } }
                }
                SectionCard("Положение, dp") {
                    SliderRow("По горизонтали", s.offsetXDp, -80f..80f, ::dp) { set { copy(offsetXDp = it) } }
                    SliderRow("По вертикали", s.offsetYDp, -20f..60f, ::dp) { set { copy(offsetYDp = it) } }
                }
                SectionCard("Развёрнутый, dp") {
                    SliderRow("Отступ от краёв", s.expandedMarginDp, 0f..48f, ::dp) { set { copy(expandedMarginDp = it) } }
                    SliderRow("Углы", s.expandedCornerDp, 12f..60f, ::dp) { set { copy(expandedCornerDp = it) } }
                }
            }
            2 -> {
                SectionCard("Цвет фона") {
                    val cs = MaterialTheme.colorScheme
                    val choices = listOf(BgChoice.BLACK, BgChoice.MONET_DARK, BgChoice.MONET_TONAL, BgChoice.MONET_LIGHT, BgChoice.APP_TINT, BgChoice.WALLPAPER) + BgChoice.palette
                    GroupBox { Row(
                        Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically,
                    ) {
                        choices.forEachIndexed { i, c -> ColorDot(bgPreview(c, cs), s.bg == c, when (i) { in 1..3 -> "M"; 4 -> "App"; 5 -> "Обои"; else -> null }) { set { copy(bg = c) } } }
                        CustomDot(s.bg > 5 && s.bg !in BgChoice.palette, s.bg) { pick = 1 }
                    } }
                    SliderRow("Непрозрачность", s.bgOpacity, 0.2f..1f, ::pct) { set { copy(bgOpacity = it) } }
                    GroupBox { Hint("M — цвета Material You, App — оттенок открытого приложения, «Обои» — палитра ваших обоев (меняется вместе с ними).") }
                }
                SectionCard("Живой фон") {
                    VisualRow(com.pixelisland.settings.PillFx.entries, s.pillFx, com.pixelisland.settings.PillFx::label, { v -> set { copy(pillFx = v) } }, tileW = 68.dp, tileH = 40.dp) { f, _ -> PillFxTile(f) }
                    GroupBox { Hint("Анимируется, пока остров раскрыт (до минуты) и 8 секунд после смены события, — батарею не тратит.") }
                }
                SectionCard("Градиент") {
                    VisualRow(GradientStyle.entries, s.gradient, GradientStyle::label, { v -> set { copy(gradient = v) } }, tileW = 68.dp, tileH = 40.dp) { g, _ -> GradientTile(g) }
                    if (s.gradient == GradientStyle.CUSTOM || s.gradient == GradientStyle.DIAGONAL) {
                        GroupBox { Row(Modifier.padding(horizontal = 16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text("Второй цвет", style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
                            ColorDot(Color(s.bgCustom2), true) { pick = 2 }
                        } }
                    }
                }
            }
            5 -> AllEventsView()
            4 -> MotionContent()
            3 -> {
                SectionCard(null) {
                    SwitchRow("Как тема системы", "Светлый остров в светлой теме", s.followSystemTheme) { set { copy(followSystemTheme = it) } }
                    SwitchRow("Обводка", null, s.outline) { set { copy(outline = it) } }
                    SwitchRow("Тень", null, s.shadow) { set { copy(shadow = it) } }
                    SwitchRow("Свечение", "Цветной ореол", s.glow) { set { copy(glow = it) } }
                    SwitchRow("Размытие фона", "Пока остров раскрыт", s.blurBehind) { set { copy(blurBehind = it) } }
                    SwitchRow("Две пилюли", "Вторая активность — кружком рядом", s.splitPills) { set { copy(splitPills = it) } }
                    SwitchRow("Звуки", "Тихий щелчок при раскрытии и сворачивании", s.sounds) { set { copy(sounds = it) } }
                }
                SectionCard("Вкл / Выкл в пилюле") {
                    VisualRow(com.pixelisland.settings.StatusStyle.entries, s.statusStyle, com.pixelisland.settings.StatusStyle::label, { v -> set { copy(statusStyle = v) } }, tileW = 80.dp, tileH = 44.dp) { v, _ -> StatusTile(v) }
                    GroupBox { Hint("«Галочка» показывает ✓ вместо «Вкл», «Подключено» и перечёркнутую галочку вместо «Выкл», «Отключено».") }
                }
                SectionCard("Свечение вокруг камеры") {
                    SwitchRow("При новом уведомлении", "Мягкая вспышка цвета приложения", s.notifyGlow) { set { copy(notifyGlow = it) } }
                    if (s.notifyGlow) SwitchRow("Не только на блокировке", "Вспыхивать и при разблокированном экране", s.glowAlways) { set { copy(glowAlways = it) } }
                    GroupBox { Hint("При полностью выключенном экране Android не даёт ничего рисовать, поэтому свечение видно на экране блокировки и при включённом экране.") }
                }
            }
        }
    }
    if (pick == 1) ColorPickerDialog(if (s.bg > 5) s.bg else 0xFF1D2433, { pick = 0 }) { c -> set { copy(bg = c) } }
    if (pick == 2) ColorPickerDialog(s.bgCustom2, { pick = 0 }) { c -> set { copy(bgCustom2 = c) } }
}

@Composable
private fun CustomDot(selected: Boolean, color: Long, onClick: () -> Unit) {
    Box(
        Modifier.size(36.dp).clip(CircleShape)
            .background(if (selected) Color(color) else MaterialTheme.colorScheme.surfaceContainerHighest)
            .border(if (selected) 3.dp else 1.dp, MaterialTheme.colorScheme.primary, CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) { Icon(Icons.Rounded.Colorize, "Свой цвет", Modifier.size(18.dp)) }
}

// =========================== ДВИЖЕНИЕ ===========================

@Composable
private fun MotionContent() {
    val s by SettingsStore.flow.collectAsState()
    SectionCard("Характер") {
        VisualRow(AnimStyle.entries, s.animStyle, AnimStyle::label, { v -> set { copy(animStyle = v) } }, tileW = 68.dp, tileH = 46.dp) { a, _ -> AnimCurveTile(a) }
        GroupBox { Hint(s.animStyle.hint) }
        SliderRow("Скорость", s.animSpeed, 0.5f..2f, { "×%.1f".format(it) }) { set { copy(animSpeed = it) } }
    }
    SectionCard("Появление") { VisualRow(AppearStyle.entries, s.appearStyle, AppearStyle::label, { v -> set { copy(appearStyle = v) } }, tileW = 68.dp, tileH = 46.dp) { a, _ -> AppearTile(a) } }
    SectionCard("Время") {
        SliderRow("Сворачивать через", s.autoCollapseSec, 0f..15f, { if (it < 1f) "нет" else "${it.roundToInt()} с" }, steps = 14) {
            set { copy(autoCollapseSec = it) }
        }
        SliderRow("Всплывашки", s.toastSec, 1f..6f, { "%.1f с".format(it) }) { set { copy(toastSec = it) } }
    }
}

// =========================== СИСТЕМА ===========================

@Composable
fun SystemTab(start: Int = 0, permissions: @Composable () -> Unit) {
    val s by SettingsStore.flow.collectAsState()
    val ctx = LocalContext.current
    val export = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/json")) { uri ->
        if (uri != null) runCatching {
            ctx.contentResolver.openOutputStream(uri)?.use { it.write(com.pixelisland.core.Backup.export().toByteArray()) }
            Toast.makeText(ctx, com.pixelisland.ui.i18n.tr("Настройки сохранены"), Toast.LENGTH_SHORT).show()
        }
    }
    val import = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) {
            val ok = runCatching {
                ctx.contentResolver.openInputStream(uri)?.use { com.pixelisland.core.Backup.import(it.readBytes().decodeToString()) } ?: false
            }.getOrDefault(false)
            Toast.makeText(ctx, com.pixelisland.ui.i18n.tr(if (ok) "Настройки загружены" else "Не удалось прочитать файл"), Toast.LENGTH_SHORT).show()
        }
    }
    Tabbed(listOf("Доступ", "Скрытие", "Ночь", "Приложения", "Журнал", "Статистика", "Диагностика", "Данные"), start = start) { tab ->
        when (tab) {
            0 -> permissions()
            1 -> SectionCard("Прятать остров") {
                SwitchRow("В альбомной ориентации", null, s.hideLandscape) { set { copy(hideLandscape = it) } }
                SwitchRow("В полноэкранном режиме", "Видео, игры", s.hideFullscreen) { set { copy(hideFullscreen = it) } }
                SwitchRow("При открытой шторке", null, s.hideShade) { set { copy(hideShade = it) } }
                SwitchRow("Показывать на блокировке", null, s.showOnLockscreen) { set { copy(showOnLockscreen = it) } }
            }
            2 -> {
                SectionCard("Ночной режим") {
                    SwitchRow("По расписанию", "Тише и темнее ночью", s.nightEnabled) { set { copy(nightEnabled = it) } }
                    SliderRow("С", s.nightFrom.toFloat(), 0f..23f, { "${it.roundToInt()}:00" }, steps = 22) { set { copy(nightFrom = it.roundToInt()) } }
                    SliderRow("До", s.nightTo.toFloat(), 0f..23f, { "${it.roundToInt()}:00" }, steps = 22) { set { copy(nightTo = it.roundToInt()) } }
                    SwitchRow("Только важное", "Без всплывашек и вибрации, кроме заряда", s.nightQuiet) { set { copy(nightQuiet = it) } }
                    SwitchRow("Приглушить", "Тусклее текст и иконки", s.nightDim) { set { copy(nightDim = it) } }
                }
                Hint("Звонки, будильник и музыка ночью показываются как обычно.")
            }
            3 -> AppRulesView()
            4 -> EventLogView()
            5 -> StatsView()
            6 -> {
                val probs = remember { com.pixelisland.core.Watchdog.problems(ctx) }
                SectionCard("Сторож") {
                    GroupBox {
                        Text(if (probs.isEmpty()) "Оба сервиса работают" else probs.joinToString("\n"),
                            style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(horizontal = 16.dp))
                        Hint("Раз в 15 минут остров проверяет себя: переподключает слушатель уведомлений и присылает уведомление, если Android выключил сервис.")
                    }
                }
                Button(onClick = { runCatching { com.pixelisland.core.BugReport.share(ctx) } }, modifier = Modifier.fillMaxWidth()) {
                    Icon(Icons.Rounded.BugReport, null, Modifier.size(18.dp)); Spacer(Modifier.width(6.dp)); Text("Отправить отчёт об ошибке")
                }
                Hint("В отчёт попадут: модель телефона, версия Android, состояние сервисов, журнал событий, настройки и лог самого приложения. Вы сами выбираете, куда его отправить.")
                DiagnosticsView()
            }
            7 -> {
                SectionCard("Язык") {
                    ChipRow(com.pixelisland.settings.AppLanguage.entries, s.language, com.pixelisland.settings.AppLanguage::label) { v -> set { copy(language = v) } }
                }
                SectionCard("Резервная копия") {
                    GroupBox { Row(Modifier.padding(horizontal = 12.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilledTonalButton(onClick = { export.launch("ostrov-backup.json") }, modifier = Modifier.weight(1f)) {
                            Icon(Icons.Rounded.Upload, null, Modifier.size(18.dp)); Spacer(Modifier.width(6.dp)); Text("Экспорт")
                        }
                        FilledTonalButton(onClick = { import.launch(arrayOf("application/json", "text/*")) }, modifier = Modifier.weight(1f)) {
                            Icon(Icons.Rounded.Download, null, Modifier.size(18.dp)); Spacer(Modifier.width(6.dp)); Text("Импорт")
                        }
                    } }
                }
                Hint("В копию входят настройки, профили и сценарии. В окне сохранения выберите Google Диск — копия будет в облаке.")
                OutlinedButton(onClick = { SettingsStore.reset() }, modifier = Modifier.fillMaxWidth()) {
                    Icon(Icons.Rounded.RestartAlt, null, Modifier.size(18.dp)); Spacer(Modifier.width(6.dp)); Text("Сбросить все настройки")
                }
                Hint("Остров 0.9.3 · плитка для быстрых настроек есть в списке плиток шторки, виджет — в списке виджетов")
            }
        }
    }
}

// =========================== КАЛИБРОВКА ===========================

@Composable
fun CalibrationScreen() {
    val s by SettingsStore.flow.collectAsState()
    val auto by IslandRuntime.autoGeometry.collectAsState()
    val g = auto ?: rememberGeometry()
    WhileAppVisible(
        onShow = { IslandController.calibrating.value = true },
        onHide = { IslandController.calibrating.value = false },
    )
    ScrollBody {
        Hint("Красное кольцо на экране должно ровно обнимать камеру. Остров строится от него.")
        IconChoiceRow(listOf(false, true), s.cutoutManual, { if (it) "Вручную" else "Автоматически" },
            { if (it) androidx.compose.material.icons.Icons.Rounded.TouchApp else androidx.compose.material.icons.Icons.Rounded.AutoAwesome }) { manual ->
            set {
                if (!manual) copy(cutoutManual = false)
                else if (cutW > 0) copy(cutoutManual = true)
                else copy(cutoutManual = true, cutX = g.centerX, cutY = g.top, cutW = g.cutoutWidth, cutH = g.cutoutHeight)
            }
        }
        Hint("Система: центр ${g.centerX}, верх ${g.top}, ${g.cutoutWidth}×${g.cutoutHeight} px")
        if (s.cutoutManual) {
            SectionCard(null) {
                StepperRow("Центр X", s.cutX, 0..g.screenWidth) { v -> set { copy(cutX = v) } }
                StepperRow("Верх", s.cutY, 0..300) { v -> set { copy(cutY = v) } }
                StepperRow("Ширина", s.cutW, 8..600) { v -> set { copy(cutW = v) } }
                StepperRow("Высота", s.cutH, 8..300) { v -> set { copy(cutH = v) } }
                GroupBox { Row(Modifier.padding(horizontal = 12.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilledTonalButton(onClick = { set { copy(cutW = cutH) } }, modifier = Modifier.weight(1f)) { Text("Круглый") }
                    FilledTonalButton(onClick = {
                        set { copy(cutX = g.centerX, cutY = g.top, cutW = g.cutoutWidth, cutH = g.cutoutHeight) }
                    }, modifier = Modifier.weight(1f)) { Text("Как в системе") }
                } }
            }
        }
    }
}


// =========================== ЖУРНАЛ ===========================

@Composable
fun EventLogView() {
    val list by com.pixelisland.core.EventLog.entries.collectAsState()
    val fmt = androidx.compose.runtime.remember { java.text.SimpleDateFormat("dd.MM HH:mm", java.util.Locale.getDefault()) }
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Hint("Последние ${list.size} событий острова")
        Spacer(Modifier.weight(1f))
        androidx.compose.material3.TextButton(onClick = { com.pixelisland.core.EventLog.clear() }) { Text("Очистить") }
    }
    if (list.isEmpty()) Hint("Пока пусто.")
    SectionCard(null) {
        list.take(150).forEach { e ->
            GroupBox {
                Row(Modifier.padding(horizontal = 16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text(e.title.ifBlank { e.kind }, style = MaterialTheme.typography.bodyMedium, maxLines = 1)
                        Text(listOf(e.kind, e.text).filter { it.isNotBlank() }.joinToString(" · "),
                            style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
                    }
                    Text(fmt.format(java.util.Date(e.time)), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

// =========================== ДИАГНОСТИКА ===========================

@Composable
fun DiagnosticsView() {
    val ctx = LocalContext.current
    val alive by IslandRuntime.serviceAlive.collectAsState()
    val listener by IslandRuntime.listenerAlive.collectAsState()
    val large by IslandRuntime.windowLarge.collectAsState()
    val bounds by IslandRuntime.showBounds.collectAsState()
    val pres by IslandController.presentation.collectAsState()
    val visible by IslandController.visible.collectAsState()
    var fps by androidx.compose.runtime.remember { mutableIntStateOf(0) }
    var mem by androidx.compose.runtime.remember { mutableIntStateOf(0) }
    androidx.compose.runtime.LaunchedEffect(Unit) {
        var last = IslandRuntime.frames
        while (true) {
            kotlinx.coroutines.delay(1000)
            val now = IslandRuntime.frames
            fps = (now - last).toInt(); last = now
            mem = (android.os.Debug.getPss() / 1024).toInt()
        }
    }
    val pm = ctx.getSystemService(android.os.PowerManager::class.java)
    val batteryOk = pm.isIgnoringBatteryOptimizations(ctx.packageName)
    val report = """Остров 0.9.3 · Android ${android.os.Build.VERSION.RELEASE} · ${android.os.Build.MODEL}
Сервис острова: ${if (alive) "работает" else "не запущен"}; уведомления: ${if (listener) "подключены" else "нет"}
Окно: ${if (large) "большое" else "компактное"}; состояние: ${pres::class.simpleName}; активностей: ${visible.size}
Перерисовка острова: $fps кадр/с; память: $mem МБ; экономия батареи: ${if (batteryOk) "не ограничивает" else "может ограничивать"}"""
    SectionCard("Состояние") {
        DiagRow("Сервис острова", if (alive) "работает" else "не запущен", alive)
        DiagRow("Доступ к уведомлениям", if (listener) "подключён" else "нет", listener)
        DiagRow("Перерисовка острова", "$fps кадр/с", fps <= 5 || pres is com.pixelisland.core.Presentation.Expanded)
        DiagRow("Память приложения", "$mem МБ", mem < 180)
        DiagRow("Окно", if (large) "большое" else "компактное", true)
        DiagRow("Сейчас показано", "${pres::class.simpleName} · активностей ${visible.size}", true)
        DiagRow("Экономия батареи", if (batteryOk) "не ограничивает" else "может ограничивать", batteryOk)
    }
    Hint("В покое перерисовка должна быть около 0 кадр/с. Если остров свёрнут и ничего не анимирует, а цифра большая — это ошибка, пришлите отчёт.")
    SectionCard(null) {
        SwitchRow("Показать границы окна", "Красная рамка вокруг окна оверлея", bounds) { IslandRuntime.showBounds.value = it }
    }
    FilledTonalButton(onClick = {
        val log = com.pixelisland.core.EventLog.entries.value.take(50).joinToString("\n") { "${it.time} ${it.kind}: ${it.title} ${it.text}" }
        val i = android.content.Intent(android.content.Intent.ACTION_SEND).setType("text/plain")
            .putExtra(android.content.Intent.EXTRA_SUBJECT, "Остров — отчёт")
            .putExtra(android.content.Intent.EXTRA_TEXT, report + "\n\nПоследние события:\n" + log)
        ctx.startActivity(android.content.Intent.createChooser(i, "Отправить отчёт"))
    }, modifier = Modifier.fillMaxWidth()) { Text("Отправить отчёт") }
}

@Composable
private fun DiagRow(title: String, value: String, ok: Boolean) {
    GroupBox {
        Row(Modifier.padding(horizontal = 16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(8.dp).clip(CircleShape).background(if (ok) Color(0xFF5EDB8B) else Color(0xFFFFB25C)))
            Spacer(Modifier.width(10.dp))
            Text(title, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
            Text(value, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
        }
    }
}


// =========================== ПРАВИЛА ПРИЛОЖЕНИЙ ===========================

@Composable
fun AppRulesView() {
    val s by SettingsStore.flow.collectAsState()
    val seen by NotificationSource.seenPackages.collectAsState()
    var editing by rememberSaveable { mutableStateOf<String?>(null) }
    Hint("Для каждого приложения: показывать ли его на острове (уведомления, живые активности, музыка), свой цвет и своя иконка. Список пополняется по мере прихода уведомлений.")
    SectionCard(null) {
        if (seen.isEmpty()) GroupBox { Hint("Пока пусто — дождитесь пары уведомлений.") }
        seen.entries.sortedBy { it.value.lowercase() }.forEach { (pkg, label) ->
            val r = s.rule(pkg)
            Row(
                Modifier.groupItem().clickable { editing = pkg }.padding(horizontal = 16.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    Modifier.size(28.dp).clip(CircleShape).background(r?.color?.takeIf { it != 0L }?.let { Color(it) } ?: MaterialTheme.colorScheme.surfaceContainerHighest),
                    contentAlignment = Alignment.Center,
                ) {
                    r?.icon?.takeIf { it.isNotEmpty() }?.let { com.pixelisland.ui.catalogIcon(it, com.pixelisland.settings.IconVariant.ROUNDED) }?.let {
                        Icon(it, null, Modifier.size(16.dp), tint = Color.Black)
                    }
                }
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(label, style = MaterialTheme.typography.bodyMedium)
                    Text(pkg, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
                }
                androidx.compose.material3.Switch(
                    checked = s.appShown(pkg), modifier = Modifier.scale(0.85f),
                    onCheckedChange = { on ->
                        set { copy(appRules = appRules + (pkg to (rule(pkg) ?: com.pixelisland.settings.AppRule()).copy(show = on)), peekBlocked = peekBlocked - pkg) }
                    },
                )
            }
        }
    }
    editing?.let { pkg -> AppRuleDialog(pkg, seen[pkg] ?: pkg) { editing = null } }
}

@Composable
private fun AppRuleDialog(pkg: String, label: String, onDismiss: () -> Unit) {
    val s by SettingsStore.flow.collectAsState()
    val r = s.rule(pkg) ?: com.pixelisland.settings.AppRule()
    var picker by rememberSaveable { mutableStateOf(false) }
    var catalog by rememberSaveable { mutableStateOf(false) }
    fun save(n: com.pixelisland.settings.AppRule) = set { copy(appRules = appRules + (pkg to n)) }
    androidx.compose.material3.AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = { androidx.compose.material3.TextButton(onClick = onDismiss) { Text("Готово") } },
        dismissButton = { androidx.compose.material3.TextButton(onClick = { set { copy(appRules = appRules - pkg) }; onDismiss() }) { Text("Сбросить") } },
        title = { Text(label) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Цвет на острове", style = MaterialTheme.typography.labelLarge)
                Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ColorDot(MaterialTheme.colorScheme.surfaceContainerHighest, r.color == 0L, "A") { save(r.copy(color = 0L)) }
                    com.pixelisland.settings.ColorChoice.palette.forEach { c -> ColorDot(Color(c), r.color == c) { save(r.copy(color = c)) } }
                    CustomDot(r.color != 0L && r.color !in com.pixelisland.settings.ColorChoice.palette, r.color) { picker = true }
                }
                Text("Иконка", style = MaterialTheme.typography.labelLarge)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(if (r.icon.isEmpty()) "Из уведомления" else r.icon, modifier = Modifier.weight(1f))
                    FilledTonalButton(onClick = { catalog = true }) { Text("Каталог") }
                }
            }
        },
    )
    if (picker) ColorPickerDialog(if (r.color != 0L) r.color else 0xFF6EA8FF, { picker = false }) { c -> save(r.copy(color = c)) }
    if (catalog) IconCatalogSheet(EventType.NOTIF_PEEK, com.pixelisland.settings.IconVariant.ROUNDED, r.icon, { catalog = false }) { n -> save(r.copy(icon = n)) }
}


// =========================== ОБМЕН ТЕМАМИ ===========================

@Composable
private fun ThemeShareRow() {
    val ctx = LocalContext.current
    var show by rememberSaveable { mutableStateOf(false) }
    var paste by rememberSaveable { mutableStateOf(false) }
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        FilledTonalButton(onClick = { show = true }, modifier = Modifier.weight(1f)) { Text("Поделиться темой") }
        OutlinedButton(onClick = { paste = true }, modifier = Modifier.weight(1f)) { Text("Вставить тему") }
    }
    if (show) {
        val code = androidx.compose.runtime.remember { com.pixelisland.core.ThemeCode.export() }
        val qr = androidx.compose.runtime.remember(code) { com.pixelisland.core.ThemeCode.qr(code) }
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { show = false },
            confirmButton = {
                androidx.compose.material3.TextButton(onClick = {
                    val i = android.content.Intent(android.content.Intent.ACTION_SEND).setType("text/plain")
                        .putExtra(android.content.Intent.EXTRA_TEXT, code)
                    ctx.startActivity(android.content.Intent.createChooser(i, "Поделиться темой"))
                }) { Text("Отправить") }
            },
            dismissButton = {
                androidx.compose.material3.TextButton(onClick = {
                    ctx.getSystemService(android.content.ClipboardManager::class.java)
                        .setPrimaryClip(android.content.ClipData.newPlainText("Тема острова", code))
                    Toast.makeText(ctx, com.pixelisland.ui.i18n.tr("Код скопирован"), Toast.LENGTH_SHORT).show()
                }) { Text("Копировать") }
            },
            title = { Text("Ваша тема") },
            text = {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    if (qr != null) androidx.compose.foundation.Image(
                        qr.asImageBitmap(), "QR-код темы", modifier = Modifier.size(240.dp).clip(RoundedCornerShape(12.dp)),
                    )
                    Spacer(Modifier.height(8.dp))
                    Text("Отсканируйте QR камерой (Google Lens скопирует текст) или отправьте код — получатель вставит его через «Вставить тему». Включённые события и калибровка не передаются.",
                        style = MaterialTheme.typography.bodySmall)
                }
            },
        )
    }
    if (paste) {
        var text by rememberSaveable { mutableStateOf("") }
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { paste = false },
            confirmButton = {
                androidx.compose.material3.TextButton(onClick = {
                    val ok = com.pixelisland.core.ThemeCode.import(text)
                    Toast.makeText(ctx, com.pixelisland.ui.i18n.tr(if (ok) "Тема применена" else "Не похоже на код темы"), Toast.LENGTH_SHORT).show()
                    if (ok) paste = false
                }) { Text("Применить") }
            },
            dismissButton = {
                androidx.compose.material3.TextButton(onClick = {
                    text = ctx.getSystemService(android.content.ClipboardManager::class.java).primaryClip
                        ?.getItemAt(0)?.coerceToText(ctx)?.toString().orEmpty()
                }) { Text("Из буфера") }
            },
            title = { Text("Вставить тему") },
            text = {
                androidx.compose.material3.OutlinedTextField(
                    value = text, onValueChange = { text = it }, placeholder = { Text("OSTROV1:…") },
                    maxLines = 4, modifier = Modifier.fillMaxWidth(),
                )
            },
        )
    }
}


// =========================== ВСЕ СОБЫТИЯ СРАЗУ ===========================

/** Задать настройку сразу всем событиям (с возможностью отменить). */
private fun setAll(what: String, transform: (com.pixelisland.settings.EventStyle) -> com.pixelisland.settings.EventStyle) {
    val before = SettingsStore.value
    SettingsStore.update { EventType.entries.fold(this) { acc, t -> acc.withStyle(t, transform(acc.style(t))) } }
    UndoBus.pending.value = "«$what» — для всех ${EventType.entries.size} событий" to before
}

@Composable
fun AllEventsView() {
    val s by SettingsStore.flow.collectAsState()
    val ref = EventType.HP_ON
    val st = s.style(ref)
    var replay by androidx.compose.runtime.remember { mutableIntStateOf(0) }
    Hint("Здесь настройка сразу меняется у всех событий. Потом любое событие можно подправить отдельно в его редакторе.")
    SectionCard("Подложка") {
        VisualRow(com.pixelisland.settings.IconContainer.entries, st.container, com.pixelisland.settings.IconContainer::label,
            { v -> setAll("Подложка") { it.copy(container = v) } }, tileW = 64.dp) { c, _ -> IconTile(ref, st.copy(container = c, anim = com.pixelisland.settings.IconAnim.STATIC), c) }
    }
    SectionCard("Анимация иконки") {
        VisualRow(com.pixelisland.settings.IconAnim.entries, st.anim, com.pixelisland.settings.IconAnim::label,
            { v -> setAll("Анимация") { it.copy(anim = v) }; replay++ }, tileW = 64.dp) { a, sel -> IconTile(ref, st.copy(anim = a), if (sel) replay else a) }
    }
    SectionCard("Стиль Material") {
        VisualRow(com.pixelisland.settings.IconVariant.entries, st.variant, com.pixelisland.settings.IconVariant::label, { v -> setAll("Стиль Material") { it.copy(variant = v) } }, tileW = 60.dp, tileH = 46.dp) { v, _ ->
            Icon(com.pixelisland.ui.iconFor(ref, v, st.icon), null, tint = com.pixelisland.ui.LocalIslandColors.current.accent) }
    }
    SectionCard("Цвет") {
        GroupBox {
            Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(horizontal = 12.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ColorDot(MaterialTheme.colorScheme.surfaceContainerHighest, st.color == com.pixelisland.settings.ColorChoice.AUTO, "A") { setAll("Цвет") { it.copy(color = com.pixelisland.settings.ColorChoice.AUTO) } }
                ColorDot(MaterialTheme.colorScheme.primary, st.color == com.pixelisland.settings.ColorChoice.MONET, "M") { setAll("Цвет") { it.copy(color = com.pixelisland.settings.ColorChoice.MONET) } }
                ColorDot(Color.White, st.color == com.pixelisland.settings.ColorChoice.MONO, "Aa") { setAll("Цвет") { it.copy(color = com.pixelisland.settings.ColorChoice.MONO) } }
                com.pixelisland.settings.ColorChoice.palette.forEach { c -> ColorDot(Color(c), st.color == c) { setAll("Цвет") { it.copy(color = c) } } }
            }
        }
    }
    SectionCard("Текст и прогресс") {
        VisualRow(com.pixelisland.settings.TextWeight.entries, st.weight, com.pixelisland.settings.TextWeight::label, { v -> setAll("Текст") { it.copy(weight = v) } }, tileW = 60.dp, tileH = 46.dp) { w, _ -> WeightTile(w) }
        VisualRow(com.pixelisland.settings.RingStyle.entries, st.ring, com.pixelisland.settings.RingStyle::label, { v -> setAll("Прогресс") { it.copy(ring = v) } }, tileW = 60.dp, tileH = 46.dp) { r, _ ->
            val d = sampleSlotData(ref).copy(ring = 0.65f)
            com.pixelisland.ui.content.SlotView(com.pixelisland.settings.SlotContent.RING, d, st.copy(ring = r), 28.dp, com.pixelisland.ui.LocalIslandColors.current.resolve(st.color, d.auto))
        }
    }
    SectionCard("Пилюля") {
        VisualRow(com.pixelisland.settings.SlotAlign.entries, st.align, com.pixelisland.settings.SlotAlign::label, { v -> setAll("Выравнивание") { it.copy(align = v) } }) { a, _ -> AlignTile(a) }
        VisualRow(com.pixelisland.settings.WidthMode.entries - com.pixelisland.settings.WidthMode.CUSTOM, st.widthMode, com.pixelisland.settings.WidthMode::label,
            { v -> setAll("Ширина") { it.copy(widthMode = v) } }) { m, _ -> WidthTile(m) }
    }
    val ex = s.style(EventType.MUSIC)
    SectionCard("Раскрытый вид") {
        VisualRow(ExLayout.entries - ExLayout.PLAYER, if (ex.exLayout == ExLayout.PLAYER) ExLayout.STANDARD else ex.exLayout, ExLayout::label,
            { v -> setAll("Макет") { it.copy(exLayout = v) } }) { l, _ -> LayoutTile(l) }
        Hint("Кнопки")
        VisualRow(com.pixelisland.settings.ExButtons.entries, ex.exButtons, { it.label }, { v -> setAll("Кнопки") { it.copy(exButtons = v) } }, tileW = 68.dp, tileH = 40.dp) { b, _ -> ExButtonsTile(b) }
        Hint("Прогресс")
        VisualRow(com.pixelisland.settings.ExProgress.entries, ex.exProgress, { it.label }, { v -> setAll("Прогресс раскрытого") { it.copy(exProgress = v) } }, tileW = 68.dp, tileH = 40.dp) { p, _ -> ExProgressTile(p) }
        Hint("Фон")
        VisualRow(com.pixelisland.settings.ExBg.entries, ex.exBg, { it.label }, { v -> setAll("Фон раскрытого") { it.copy(exBg = v) } }, tileW = 68.dp, tileH = 46.dp) { b, _ -> ExBgTile(b, ex.exBgColor) }
        Hint("Ширина")
        VisualRow(com.pixelisland.settings.ExWidth.entries, ex.exWidth, { it.label }, { v -> setAll("Ширина раскрытого") { it.copy(exWidth = v) } }, tileW = 68.dp, tileH = 46.dp) { w, _ -> ExWidthTile(w) }
        Hint("Нажатие")
        VisualRow(com.pixelisland.settings.ExTap.entries, ex.exTap, { it.label }, { v -> setAll("Тап") { it.copy(exTap = v) } }, tileW = 68.dp, tileH = 40.dp) { t, _ -> SimpleIconTile(exTapIcon(t)) }
        SwitchRow("Шапка с названием приложения", null, ex.exHeader) { v -> setAll("Шапка") { it.copy(exHeader = v) } }
    }
}


/** Включено, пока экран на виду; сворачивание приложения (ON_STOP) или уход с экрана — выключает. */
@Composable
fun WhileAppVisible(onShow: () -> Unit, onHide: () -> Unit) {
    val owner = androidx.lifecycle.compose.LocalLifecycleOwner.current
    val show by androidx.compose.runtime.rememberUpdatedState(onShow)
    val hide by androidx.compose.runtime.rememberUpdatedState(onHide)
    DisposableEffect(owner) {
        var on = false
        fun up() { if (!on) { on = true; show() } }
        fun down() { if (on) { on = false; hide() } }
        val obs = androidx.lifecycle.LifecycleEventObserver { _, e ->
            when (e) {
                androidx.lifecycle.Lifecycle.Event.ON_START -> up()
                androidx.lifecycle.Lifecycle.Event.ON_STOP -> down()
                else -> Unit
            }
        }
        if (owner.lifecycle.currentState.isAtLeast(androidx.lifecycle.Lifecycle.State.STARTED)) up()
        owner.lifecycle.addObserver(obs)
        onDispose { owner.lifecycle.removeObserver(obs); down() }
    }
}
