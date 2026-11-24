package com.pixelisland.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.ArrowForward
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.KeyboardDoubleArrowDown
import androidx.compose.material.icons.rounded.KeyboardDoubleArrowUp
import androidx.compose.material.icons.rounded.Layers
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Save
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.SwapHoriz
import androidx.compose.material.icons.rounded.TouchApp
import androidx.compose.material.icons.rounded.Inventory2
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import com.pixelisland.ui.i18n.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.pixelisland.core.AutoAction
import com.pixelisland.core.Automation
import com.pixelisland.core.Automations
import com.pixelisland.core.Profile
import com.pixelisland.core.Profiles
import com.pixelisland.core.Shelf
import com.pixelisland.settings.EventType
import com.pixelisland.settings.GestureAction
import com.pixelisland.settings.IdleContent
import com.pixelisland.settings.IdleGestures
import com.pixelisland.settings.RateKind
import com.pixelisland.settings.SettingsStore
import com.pixelisland.ui.iconFor
import kotlin.math.roundToInt

private fun set(block: com.pixelisland.settings.IslandSettings.() -> com.pixelisland.settings.IslandSettings) = SettingsStore.update(block)

/** Раздел «Функции»: жесты, сценарии, пустая пилюля, полка, профили. */
@Composable
fun FeaturesTab(start: Int = 0) {
    Tabbed(listOf("Жесты", "Сценарии", "Пустая пилюля", "Полка", "Профили"), start = start) { tab ->
        when (tab) {
            0 -> GesturesView()
            1 -> AutomationsView()
            2 -> IdleView()
            3 -> ShelfView()
            4 -> ProfilesView()
        }
    }
}

@Composable
private fun GesturesView() {
    val s by SettingsStore.flow.collectAsState()
    val all = GestureAction.entries
    SectionCard("Пилюля с событием") {
        GestureRow("Нажатие", Icons.Rounded.TouchApp, all, s.gTap) { v -> set { copy(gTap = v) } }
        GestureRow("Двойное", Icons.Rounded.TouchApp, all, s.gDoubleTap) { v -> set { copy(gDoubleTap = v) } }
        GestureRow("Удержание", Icons.Rounded.TouchApp, all, s.gLongPress) { v -> set { copy(gLongPress = v) } }
        GestureRow("Свайп вниз", Icons.Rounded.KeyboardDoubleArrowDown, all, s.gSwipeDown) { v -> set { copy(gSwipeDown = v) } }
        GestureRow("Свайп вверх", Icons.Rounded.KeyboardDoubleArrowUp, all, s.gSwipeUp) { v -> set { copy(gSwipeUp = v) } }
        GestureRow("Свайп вбок", Icons.Rounded.SwapHoriz, all, s.gSwipeSide) { v -> set { copy(gSwipeSide = v) } }
    }
    SectionCard("Пустая пилюля") {
        GestureRow("Нажатие", Icons.Rounded.TouchApp, IdleGestures, s.gIdleTap) { v -> set { copy(gIdleTap = v) } }
        GestureRow("Двойное", Icons.Rounded.TouchApp, IdleGestures, s.gIdleDouble) { v -> set { copy(gIdleDouble = v) } }
        GestureRow("Удержание", Icons.Rounded.TouchApp, IdleGestures, s.gIdleLong) { v -> set { copy(gIdleLong = v) } }
        GestureRow("Свайп вниз", Icons.Rounded.KeyboardDoubleArrowDown, IdleGestures, s.gIdleSwipeDown) { v -> set { copy(gIdleSwipeDown = v) } }
        GestureRow("Свайп вверх", Icons.Rounded.KeyboardDoubleArrowUp, IdleGestures, s.gIdleSwipeUp) { v -> set { copy(gIdleSwipeUp = v) } }
    }
    SectionCard("Особые жесты") {
        SwitchRow("Жесты музыки", "Свайп вбок — трек, удержание и вверх/вниз — громкость", s.mediaGestures) { set { copy(mediaGestures = it) } }
        SwitchRow("Тап по таймеру — пауза", "Свой таймер и таймер из Часов", s.tapPausesTimers) { set { copy(tapPausesTimers = it) } }
        SwitchRow("Вибрация при касаниях", null, s.haptics) { set { copy(haptics = it) } }
    }
    Hint("Пустая пилюля видна, если она включена в «Функции → Пустая пилюля». В раскрытом виде: тап — открыть приложение, свайп вверх — свернуть.")
}

// ---------------- Сценарии ----------------

@Composable
private fun AutomationsView() {
    val rules by Automations.rules.collectAsState()
    val ctx = LocalContext.current
    var adding by remember { mutableStateOf(false) }
    Hint("Событие острова запускает действие. Например: подключил наушники → открыть Spotify.")
    if (rules.isEmpty()) SectionCard(null) { GroupBox { Text("Сценариев пока нет", style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(horizontal = 16.dp)) } }
    else SectionCard("Сценарии") {
        rules.forEach { r ->
            Row(Modifier.groupItem().padding(horizontal = 12.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                val st = SettingsStore.value.style(r.trigger)
                Icon(iconFor(r.trigger, st.variant, st.icon), null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(22.dp))
                Spacer(Modifier.width(6.dp))
                Icon(Icons.Rounded.ArrowForward, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(8.dp))
                Column(Modifier.weight(1f)) {
                    Text(r.trigger.title, style = MaterialTheme.typography.bodyMedium)
                    Text(Automations.describe(r), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                IconButton(onClick = { Automations.run(ctx, r) }) { Icon(Icons.Rounded.PlayArrow, "Проверить") }
                Switch(checked = r.enabled, onCheckedChange = { Automations.toggle(r.id) })
                IconButton(onClick = { Automations.remove(r.id) }) { Icon(Icons.Rounded.Delete, "Удалить") }
            }
        }
    }
    Button(onClick = { adding = true }, modifier = Modifier.fillMaxWidth()) {
        Icon(Icons.Rounded.Add, null); Spacer(Modifier.width(6.dp)); Text("Новый сценарий")
    }
    if (adding) AutomationDialog { adding = false }
}

@Composable
private fun AutomationDialog(onDone: () -> Unit) {
    val ctx = LocalContext.current
    var trigger by remember { mutableStateOf(EventType.HP_ON) }
    var action by remember { mutableStateOf(AutoAction.MEDIA_PLAY) }
    var arg by remember { mutableStateOf("") }
    var argLabel by remember { mutableStateOf("") }
    var pickApp by remember { mutableStateOf(false) }
    AlertDialog(
        onDismissRequest = onDone,
        title = { Text("Новый сценарий") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                SectionCard(null) {
                    DropdownRow("Если", null, Automations.triggers, trigger, EventType::title) { trigger = it }
                    DropdownRow("То", null, AutoAction.entries, action, AutoAction::label) { action = it; arg = ""; argLabel = "" }
                    when (action) {
                        AutoAction.OPEN_APP -> NavRow(Icons.Rounded.Layers, "Приложение", argLabel.ifBlank { "Выбрать…" }) { pickApp = true }
                        AutoAction.TIMER -> SliderRow("Минуты", (arg.toIntOrNull() ?: 5).toFloat(), 1f..60f, { "${it.roundToInt()}" }) { arg = it.roundToInt().toString() }
                        AutoAction.MEDIA_VOLUME -> SliderRow("Громкость", (arg.toIntOrNull() ?: 50).toFloat(), 0f..100f, { "${it.roundToInt()}%" }) { arg = it.roundToInt().toString() }
                        else -> Unit
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                enabled = action != AutoAction.OPEN_APP || arg.isNotBlank(),
                onClick = {
                    val a = when (action) { AutoAction.TIMER -> arg.ifBlank { "5" }; AutoAction.MEDIA_VOLUME -> arg.ifBlank { "50" }; else -> arg }
                    Automations.add(Automation(System.currentTimeMillis(), trigger, action, a, argLabel))
                    onDone()
                },
            ) { Text("Добавить") }
        },
        dismissButton = { TextButton(onClick = onDone) { Text("Отмена") } },
    )
    if (pickApp) AppPickerDialog({ pickApp = false }) { pkg, label -> arg = pkg; argLabel = label; pickApp = false }
}

/** Выбор приложения из установленных (с иконкой запуска). */
@Composable
fun AppPickerDialog(onDismiss: () -> Unit, onPick: (String, String) -> Unit) {
    val ctx = LocalContext.current
    val apps = remember {
        val pm = ctx.packageManager
        pm.queryIntentActivities(android.content.Intent(android.content.Intent.ACTION_MAIN).addCategory(android.content.Intent.CATEGORY_LAUNCHER), 0)
            .map { it.activityInfo.packageName to it.loadLabel(pm).toString() }
            .distinctBy { it.first }.sortedBy { it.second.lowercase() }
    }
    var q by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Приложение") },
        text = {
            Column {
                OutlinedTextField(q, { q = it }, singleLine = true, placeholder = { Text("Поиск") }, modifier = Modifier.fillMaxWidth())
                LazyColumn(Modifier.padding(top = 8.dp).size(width = 320.dp, height = 360.dp)) {
                    items(apps.filter { q.isBlank() || q.lowercase() in it.second.lowercase() }) { (pkg, label) ->
                        TextButton(onClick = { onPick(pkg, label) }, modifier = Modifier.fillMaxWidth()) {
                            Text(label, modifier = Modifier.fillMaxWidth())
                        }
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Закрыть") } },
    )
}

// ---------------- Пустая пилюля ----------------

@Composable
private fun IdleView() {
    val s by SettingsStore.flow.collectAsState()
    SectionCard(null) {
        SwitchRow("Пустая пилюля", "Остров виден всегда, даже без событий", s.showIdlePill) { set { copy(showIdlePill = it) } }
    }
    if (s.showIdlePill) {
        SectionCard("Что показывать в покое") {
            IconChoiceRow(IdleContent.entries, s.idleContent, IdleContent::label, ::idleIcon) { v -> set { copy(idleContent = v) } }
            when (s.idleContent) {
                IdleContent.RATE -> {
                    VisualRow(RateKind.entries, s.rateKind, RateKind::label, { v -> set { copy(rateKind = v) }; com.pixelisland.sources.Extras.refreshRate(true) }, tileW = 72.dp, tileH = 42.dp) { r, _ -> RateTile(r) }
                    GroupBox { Hint("Обновляется раз в 30 минут: open.er-api.com для валют, CoinGecko для криптовалют.") }
                }
                IdleContent.MISSED -> GroupBox { Hint("Слева — пропущенные звонки, справа — непрочитанные сообщения из уведомлений.") }
                IdleContent.NET_SPEED -> GroupBox { Hint("Скорость приёма и отдачи. Считается раз в секунду, только пока пилюля видна.") }
                IdleContent.STEPS -> GroupBox { Hint("Нужно разрешение «Физическая активность» (Система → Доступ).") }
                IdleContent.EMPTY -> SwitchRow("Как часть камеры", "В покое остров почти не виден", s.cameraBlend) { set { copy(cameraBlend = it) } }
                else -> Unit
            }
        }
        SectionCard("Касания") {
            SwitchRow("Быстрые действия", "Фонарик, таймеры, полка, скриншот…", s.quickActions) { set { copy(quickActions = it) } }
        }
        Hint("Жесты пустой пилюли — в «Жестах»: по умолчанию двойное нажатие включает фонарик, свайп вниз открывает шторку, вверх — прячет остров на 10 минут.")
    }
}

// ---------------- Полка ----------------

@Composable
private fun ShelfView() {
    val s by SettingsStore.flow.collectAsState()
    val items by Shelf.items.collectAsState()
    val ctx = LocalContext.current
    SectionCard(null) {
        SwitchRow("Полка", "Перетащите текст, ссылку или файл на остров", s.on(EventType.SHELF), icon = Icons.Rounded.Inventory2) { v ->
            SettingsStore.updateStyle(EventType.SHELF) { copy(enabled = v) }
        }
    }
    if (items.isNotEmpty()) SectionCard("На полке", action = { TextButton(onClick = { Shelf.clear() }) { Text("Очистить") } }) {
        items.forEach { it ->
            Row(Modifier.groupItem().padding(horizontal = 16.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                Text(it.label, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f), maxLines = 1)
                TextButton(onClick = { Shelf.share(ctx, it) }) { Text("Поделиться") }
                IconButton(onClick = { Shelf.remove(it.id) }) { Icon(Icons.Rounded.Delete, "Убрать") }
            }
        }
    }
    Hint("Удерживайте текст или картинку в любом приложении и тащите к острову, пока он виден. Текст и ссылки работают везде; файлы — только из приложений, которые разрешают перенос в другие окна.")
    Hint("Открыть полку: «Быстрые действия → Полка» или жест (по умолчанию — удержание пустой пилюли).")
}

// ---------------- Профили ----------------

@Composable
private fun ProfilesView() {
    val list by Profiles.list.collectAsState()
    val active by Profiles.active.collectAsState()
    var naming by remember { mutableStateOf(false) }
    var editing by remember { mutableStateOf<Profile?>(null) }
    Hint("Профиль — снимок всех настроек. Можно переключать вручную или по расписанию (например, «Работа» с 9 до 18).")
    if (list.isEmpty()) SectionCard(null) { GroupBox { Text("Профилей пока нет", style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(horizontal = 16.dp)) } }
    else SectionCard("Профили") {
        list.forEach { p ->
            Row(Modifier.groupItem().padding(horizontal = 12.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Rounded.Layers, null, tint = if (p.id == active) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.width(10.dp))
                Column(Modifier.weight(1f)) {
                    Text(p.name + if (p.id == active) " · активен" else "", style = MaterialTheme.typography.bodyMedium)
                    Text(if (p.scheduled) "С ${p.from}:00 до ${p.to}:00" else "Вручную", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                TextButton(onClick = { Profiles.apply(p) }) { Text("Включить") }
                IconButton(onClick = { editing = p }) { Icon(Icons.Rounded.Schedule, "Расписание") }
            }
        }
    }
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Button(onClick = { naming = true }, modifier = Modifier.weight(1f)) {
            Icon(Icons.Rounded.Save, null); Spacer(Modifier.width(6.dp)); Text("Сохранить текущие")
        }
        if (active != 0L) FilledTonalButton(onClick = { Profiles.overwrite(active) }, modifier = Modifier.weight(1f)) { Text("Обновить активный") }
    }
    if (naming) {
        var name by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { naming = false },
            title = { Text("Название профиля") },
            text = { OutlinedTextField(name, { name = it }, singleLine = true, placeholder = { Text("Например, «Работа»") }) },
            confirmButton = { TextButton(onClick = { Profiles.saveCurrent(name); naming = false }) { Text("Сохранить") } },
            dismissButton = { TextButton(onClick = { naming = false }) { Text("Отмена") } },
        )
    }
    editing?.let { p ->
        var sch by remember(p.id) { mutableStateOf(p.scheduled) }
        var from by remember(p.id) { mutableStateOf(p.from.toFloat()) }
        var to by remember(p.id) { mutableStateOf(p.to.toFloat()) }
        AlertDialog(
            onDismissRequest = { editing = null },
            title = { Text(p.name) },
            text = {
                SectionCard(null) {
                    SwitchRow("По расписанию", null, sch) { sch = it }
                    if (sch) {
                        SliderRow("С", from, 0f..23f, { "${it.roundToInt()}:00" }, steps = 22) { from = it }
                        SliderRow("До", to, 0f..23f, { "${it.roundToInt()}:00" }, steps = 22) { to = it }
                    }
                }
            },
            confirmButton = { TextButton(onClick = { Profiles.update(p.copy(scheduled = sch, from = from.roundToInt(), to = to.roundToInt())); editing = null }) { Text("Готово") } },
            dismissButton = { TextButton(onClick = { Profiles.delete(p.id); editing = null }) { Text("Удалить") } },
        )
    }
}
