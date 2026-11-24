package com.pixelisland.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.DoneAll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import com.pixelisland.ui.i18n.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.pixelisland.settings.EventKind
import com.pixelisland.settings.EventStyle
import com.pixelisland.settings.EventType
import com.pixelisland.settings.IslandSettings
import com.pixelisland.settings.SettingsStore
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow

/** Последнее массовое изменение — чтобы его можно было отменить. */
object UndoBus {
    val pending = MutableStateFlow<Pair<String, IslandSettings>?>(null)
}

/** Применить к выбранному охвату событий: target ← transform(target, source). */
fun applyToMany(source: EventType, targets: List<EventType>, transform: (EventStyle, EventStyle) -> EventStyle) {
    val before = SettingsStore.value
    SettingsStore.update {
        val src = style(source)
        targets.fold(this) { acc, t -> acc.withStyle(t, transform(acc.style(t), src)) }
    }
    UndoBus.pending.value = "Применено к ${targets.size} событиям" to before
}

/** Кнопка «Ко всем» в заголовке раздела редактора. */
@Composable
fun ApplyAllButton(t: EventType, what: String, liveOnly: Boolean = false, transform: (EventStyle, EventStyle) -> EventStyle) {
    var open by remember { mutableStateOf(false) }
    TextButton(onClick = { open = true }, contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp)) {
        Icon(Icons.Rounded.DoneAll, null, Modifier.size(16.dp)); Text(" Ко всем", style = MaterialTheme.typography.labelMedium)
    }
    if (!open) return
    val scopes = buildList {
        add("Все события" to EventType.entries.filter { !liveOnly || it.kind == EventKind.LIVE })
        add("Категория «${t.cat.title}»" to EventType.entries.filter { it.cat == t.cat && (!liveOnly || it.kind == EventKind.LIVE) })
        if (!liveOnly) add("Только всплывашки" to EventType.entries.filter { it.kind == EventKind.TOAST })
        add("Только живые активности" to EventType.entries.filter { it.kind == EventKind.LIVE })
    }
    var pick by remember { mutableIntStateOf(0) }
    AlertDialog(
        onDismissRequest = { open = false },
        title = { Text("«$what» ко всем") },
        text = {
            Column {
                Text("Скопировать эту настройку из «${t.title}» в:", style = MaterialTheme.typography.bodyMedium)
                scopes.forEachIndexed { i, (label, list) ->
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(selected = pick == i, onClick = { pick = i })
                        Text("$label (${list.size})", style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = { applyToMany(t, scopes[pick].second, transform); open = false }) { Text("Применить") } },
        dismissButton = { TextButton(onClick = { open = false }) { Text("Отмена") } },
    )
}

/** Плашка «Применено · Отменить» — висит 6 секунд. */
@Composable
fun UndoBanner() {
    val p by UndoBus.pending.collectAsState()
    val cur = p ?: return
    LaunchedEffect(cur) { delay(6_000); if (UndoBus.pending.value === cur) UndoBus.pending.value = null }
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.inverseSurface),
        modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 4.dp),
    ) {
        Row(Modifier.padding(start = 16.dp, end = 4.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
            Text(cur.first, color = MaterialTheme.colorScheme.inverseOnSurface, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
            TextButton(onClick = { SettingsStore.update { cur.second }; UndoBus.pending.value = null }) {
                Text("Отменить", color = MaterialTheme.colorScheme.inversePrimary)
            }
        }
    }
}
