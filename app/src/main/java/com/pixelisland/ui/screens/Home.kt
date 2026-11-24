package com.pixelisland.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.BugReport
import androidx.compose.material.icons.rounded.CenterFocusStrong
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Gesture
import androidx.compose.material.icons.rounded.Inventory2
import androidx.compose.material.icons.rounded.Layers
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material.icons.rounded.SystemUpdate
import androidx.compose.material.icons.rounded.BarChart
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import com.pixelisland.ui.i18n.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.LifecycleResumeEffect
import com.pixelisland.core.IslandController
import com.pixelisland.core.IslandRuntime
import com.pixelisland.core.Profiles
import com.pixelisland.core.Stats
import com.pixelisland.core.Watchdog
import com.pixelisland.settings.EventType
import com.pixelisland.settings.SettingsStore
import com.pixelisland.ui.iconFor

/** Куда может вести главный экран и поиск. */
sealed interface Dest {
    data class Tab(val tab: Int, val sub: Int = 0) : Dest   // 0 главная, 1 события, 2 вид, 3 функции, 4 система
    data class Event(val type: EventType) : Dest
    data object Calibration : Dest
}

/** Пункт поиска по настройкам. */
private data class SearchItem(val title: String, val where: String, val words: String, val dest: Dest)

private val searchIndex: List<SearchItem> by lazy {
    val look = listOf("Пресеты", "Размер", "Фон", "Эффекты", "Анимация", "Все события")
    val feat = listOf("Жесты", "Сценарии", "Пустая пилюля", "Полка", "Профили")
    val sys = listOf("Доступ", "Скрытие", "Ночь", "Приложения", "Журнал", "Статистика", "Диагностика", "Данные")
    buildList {
        look.forEachIndexed { i, t -> add(SearchItem(t, "Вид", t, Dest.Tab(2, i))) }
        feat.forEachIndexed { i, t -> add(SearchItem(t, "Функции", t, Dest.Tab(3, i))) }
        sys.forEachIndexed { i, t -> add(SearchItem(t, "Система", t, Dest.Tab(4, i))) }
        add(SearchItem("Калибровка выреза", "Вид → Размер", "камера вырез калибровка положение", Dest.Calibration))
        add(SearchItem("Ширина и высота пилюли", "Вид → Размер", "ширина высота скругление сдвиг положение", Dest.Tab(2, 1)))
        add(SearchItem("Цвет фона и градиент", "Вид → Фон", "цвет фон градиент обои живой фон частицы волна", Dest.Tab(2, 2)))
        add(SearchItem("Тень, свечение, звуки", "Вид → Эффекты", "тень свечение обводка звуки размытие уведомления камера", Dest.Tab(2, 3)))
        add(SearchItem("Скорость и характер анимации", "Вид → Анимация", "анимация скорость пружина появление время сворачивать", Dest.Tab(2, 4)))
        add(SearchItem("Свайпы, нажатия, удержание", "Функции → Жесты", "жест свайп нажатие удержание шторка двойное", Dest.Tab(3, 0)))
        add(SearchItem("Если — то", "Функции → Сценарии", "сценарий автоматизация если то наушники", Dest.Tab(3, 1)))
        add(SearchItem("Курс, шаги, время в покое", "Функции → Пустая пилюля", "курс доллар биткоин шаги время пропущенные скорость сети", Dest.Tab(3, 2)))
        add(SearchItem("Резервная копия", "Система → Данные", "резервная копия экспорт импорт google drive язык english", Dest.Tab(4, 7)))
        add(SearchItem("Отчёт об ошибке", "Система → Диагностика", "ошибка баг отчёт лог", Dest.Tab(4, 6)))
        add(SearchItem("Ночной режим", "Система → Ночь", "ночь расписание тише", Dest.Tab(4, 2)))
        add(SearchItem("Правила приложений", "Система → Приложения", "приложение правило фильтр цвет", Dest.Tab(4, 3)))
        EventType.entries.forEach { add(SearchItem(it.title, "Событие", it.title + " " + it.hint, Dest.Event(it))) }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun HomeTab(go: (Dest) -> Unit) {
    val s by SettingsStore.flow.collectAsState()
    val ctx = LocalContext.current
    var query by rememberSaveable { mutableStateOf("") }
    var problems by remember { mutableStateOf(Watchdog.problems(ctx)) }
    LifecycleResumeEffect(Unit) { problems = Watchdog.problems(ctx); onPauseOrDispose { } }
    val alive by IslandRuntime.serviceAlive.collectAsState()

    ScrollBody {
        // ---- Поиск ----
        OutlinedTextField(
            value = query, onValueChange = { query = it }, singleLine = true,
            placeholder = { Text("Найти настройку или событие") },
            leadingIcon = { Icon(Icons.Rounded.Search, null) },
            trailingIcon = if (query.isNotEmpty()) { { IconButton(onClick = { query = "" }) { Icon(Icons.Rounded.Close, "Очистить") } } } else null,
            shape = RoundedCornerShape(28.dp), modifier = Modifier.fillMaxWidth(),
        )
        if (query.isNotBlank()) {
            val q = query.trim().lowercase()
            val found = searchIndex.filter { q in it.title.lowercase() || q in it.words.lowercase() || q in com.pixelisland.ui.i18n.I18n.tr(it.title).lowercase() }.take(12)
            SectionCard(if (found.isEmpty()) "Ничего не найдено" else "Найдено") {
                found.forEach { f ->
                    val icon = (f.dest as? Dest.Event)?.let { iconFor(it.type, s.style(it.type).variant, s.style(it.type).icon) } ?: Icons.Rounded.Search
                    NavRow(icon, f.title, f.where) { query = ""; go(f.dest) }
                }
            }
            return@ScrollBody
        }

        // ---- Состояние ----
        Card(
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = if (problems.isEmpty()) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.errorContainer),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(Modifier.padding(18.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(if (problems.isEmpty()) Icons.Rounded.CheckCircle else Icons.Rounded.Warning, null, modifier = Modifier.size(28.dp))
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text(
                            when { !s.enabled -> "Остров выключен"; problems.isEmpty() -> "Остров работает"; else -> "Нужно внимание" },
                            style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold,
                        )
                        Text(
                            if (problems.isEmpty()) "Событий включено: ${EventType.entries.count { s.on(it) }} из ${EventType.entries.size}"
                            else problems.joinToString("\n"),
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }
                    Switch(checked = s.enabled, onCheckedChange = { v -> SettingsStore.update { copy(enabled = v) }; com.pixelisland.service.IslandWidget.refresh(ctx) })
                }
                if (problems.isNotEmpty()) TextButton(onClick = { go(Dest.Tab(4, 0)) }) { Text("Исправить") }
                if (IslandController.snoozed) TextButton(onClick = { IslandController.unsnooze() }) { Text("Остров скрыт на 10 минут — показать") }
            }
        }

        UpdateCard()

        // ---- Быстрые переключатели ----
        SectionCard("Быстро") {
            GroupBox {
                FlowRow(Modifier.padding(horizontal = 12.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    QuickChip("Ночной режим", s.nightEnabled) { SettingsStore.update { copy(nightEnabled = !nightEnabled) } }
                    QuickChip("Пустая пилюля", s.showIdlePill) { SettingsStore.update { copy(showIdlePill = !showIdlePill) } }
                    QuickChip("Свечение камеры", s.notifyGlow) { SettingsStore.update { copy(notifyGlow = !notifyGlow) } }
                    QuickChip("Две пилюли", s.splitPills) { SettingsStore.update { copy(splitPills = !splitPills) } }
                    QuickChip("Звуки", s.sounds) { SettingsStore.update { copy(sounds = !sounds) } }
                    QuickChip("Вибрация", s.haptics) { SettingsStore.update { copy(haptics = !haptics) } }
                }
            }
        }

        // ---- Профили ----
        val profiles by Profiles.list.collectAsState()
        val active by Profiles.active.collectAsState()
        SectionCard("Профиль", action = { TextButton(onClick = { go(Dest.Tab(3, 4)) }) { Text("Все") } }) {
            GroupBox {
                FlowRow(Modifier.padding(horizontal = 12.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    profiles.forEach { p -> QuickChip(p.name, p.id == active) { Profiles.apply(p) } }
                    QuickChip("+ Сохранить текущий", false) { Profiles.saveCurrent("") }
                }
            }
        }

        // ---- Сегодня ----
        val days by Stats.days.collectAsState()
        val today = days[Stats.today()].orEmpty()
        SectionCard("Сегодня", action = { TextButton(onClick = { go(Dest.Tab(4, 5)) }) { Text("Статистика") } }) {
            GroupBox {
                Row(Modifier.padding(horizontal = 16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text("${today.values.sum()}", style = MaterialTheme.typography.displaySmall, color = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text("событий показано", style = MaterialTheme.typography.bodyMedium)
                        val top = today.entries.sortedByDescending { it.value }.take(3)
                        Text(if (top.isEmpty()) "Пока тихо" else top.joinToString(" · ") { "${it.key.title} ${it.value}" },
                            style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 2)
                    }
                }
            }
        }

        // ---- Ярлыки ----
        SectionCard("Перейти") {
            val tiles = listOf(
                Triple(Icons.Rounded.Palette, "Пресеты", Dest.Tab(2, 0)),
                Triple(Icons.Rounded.CenterFocusStrong, "Калибровка", Dest.Calibration),
                Triple(Icons.Rounded.Gesture, "Жесты", Dest.Tab(3, 0)),
                Triple(Icons.Rounded.AutoAwesome, "Сценарии", Dest.Tab(3, 1)),
                Triple(Icons.Rounded.Inventory2, "Полка", Dest.Tab(3, 3)),
                Triple(Icons.Rounded.Layers, "Профили", Dest.Tab(3, 4)),
                Triple(Icons.Rounded.BarChart, "Статистика", Dest.Tab(4, 5)),
                Triple(Icons.Rounded.BugReport, "Отчёт", Dest.Tab(4, 6)),
            )
            GroupBox {
                tiles.chunked(4).forEach { row ->
                    Row(Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp)) {
                        row.forEach { (icon, title, dest) -> HomeTile(icon, title, Modifier.weight(1f)) { go(dest) } }
                    }
                }
            }
        }
        if (!alive && s.enabled) Hint("Пока сервис острова не запущен, изменения видны только здесь.")
    }
}

@Composable
private fun QuickChip(label: String, on: Boolean, onClick: () -> Unit) {
    FilterChip(selected = on, onClick = onClick, label = { Text(label, style = MaterialTheme.typography.labelMedium) })
}

@Composable
private fun HomeTile(icon: ImageVector, title: String, modifier: Modifier, onClick: () -> Unit) {
    Column(modifier.clip(RoundedCornerShape(18.dp)).clickable(onClick = onClick).padding(vertical = 8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Box(Modifier.size(48.dp).clip(RoundedCornerShape(16.dp)).background(MaterialTheme.colorScheme.secondaryContainer), contentAlignment = Alignment.Center) {
            Icon(icon, null, tint = MaterialTheme.colorScheme.onSecondaryContainer)
        }
        Text(title, style = MaterialTheme.typography.labelSmall, modifier = Modifier.padding(top = 4.dp), maxLines = 1)
    }
}

/** Карточка «Доступна новая версия» — только когда она есть. */
@Composable
fun UpdateCard() {
    val ctx = LocalContext.current
    val u by com.pixelisland.core.Updater.available.collectAsState()
    val prog by com.pixelisland.core.Updater.progress.collectAsState()
    val err by com.pixelisland.core.Updater.error.collectAsState()
    val info = u ?: return
    Card(
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(Modifier.padding(18.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(androidx.compose.material.icons.Icons.Rounded.SystemUpdate, null, modifier = Modifier.size(26.dp))
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text("Доступна новая версия", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    Text("${com.pixelisland.core.Updater.currentName(ctx)} → ${info.name}", style = MaterialTheme.typography.bodySmall)
                }
            }
            if (info.notes.isNotEmpty()) {
                Spacer(Modifier.height(8.dp))
                info.notes.take(6).forEach { Text("• $it", style = MaterialTheme.typography.bodySmall) }
            }
            Spacer(Modifier.height(10.dp))
            val p = prog
            if (p != null) {
                androidx.compose.material3.LinearWavyProgressIndicator(progress = { p }, modifier = Modifier.fillMaxWidth())
                Text("Скачивание… ${(p * 100).toInt()}%", style = MaterialTheme.typography.labelSmall, modifier = Modifier.padding(top = 4.dp))
            } else Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                androidx.compose.material3.Button(onClick = {
                    if (com.pixelisland.core.Updater.canInstall(ctx)) com.pixelisland.core.Updater.downloadAndInstall(ctx, info)
                    else com.pixelisland.core.Updater.openInstallPermission(ctx)
                }) { Text(if (com.pixelisland.core.Updater.canInstall(ctx)) "Обновить" else "Разрешить установку") }
                TextButton(onClick = { com.pixelisland.core.Updater.openInBrowser(ctx, info) }) { Text("В браузере") }
            }
            err?.let { Text(it, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(top = 4.dp)) }
            if (!com.pixelisland.core.Updater.canInstall(ctx) && p == null)
                Text("Один раз разрешите Острову устанавливать приложения — дальше обновление будет в одно нажатие. Настройки сохранятся.",
                    style = MaterialTheme.typography.labelSmall, modifier = Modifier.padding(top = 6.dp))
        }
    }
}
