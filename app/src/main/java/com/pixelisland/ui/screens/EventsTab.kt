package com.pixelisland.ui.screens

import android.view.HapticFeedbackConstants
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.DoneAll
import androidx.compose.material.icons.rounded.RemoveDone
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import com.pixelisland.ui.i18n.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pixelisland.settings.EventCategory
import com.pixelisland.settings.EventType
import com.pixelisland.settings.SettingsStore
import com.pixelisland.ui.iconFor

@OptIn(ExperimentalLayoutApi::class, ExperimentalFoundationApi::class)
@Composable
fun EventsTab(permsMissing: Boolean, onOpenPerms: () -> Unit, onEdit: (EventType) -> Unit) {
    val s by SettingsStore.flow.collectAsState()
    val view = LocalView.current
    ScrollBody {
        var q by androidx.compose.runtime.saveable.rememberSaveable { androidx.compose.runtime.mutableStateOf("") }
        androidx.compose.material3.OutlinedTextField(
            value = q, onValueChange = { q = it }, singleLine = true,
            placeholder = { Text("Поиск событий") },
            leadingIcon = { Icon(Icons.Rounded.Search, null) },
            supportingText = { Text("Включено ${EventType.entries.count { s.on(it) }} из ${EventType.entries.size} · удержание — вкл/выкл") },
            shape = RoundedCornerShape(28.dp), modifier = Modifier.fillMaxWidth(),
        )
        if (permsMissing) {
            Card(
                onClick = onOpenPerms, shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Rounded.Warning, null, tint = MaterialTheme.colorScheme.onErrorContainer)
                    Spacer(Modifier.width(10.dp))
                    Text("Нет разрешений — нажмите, чтобы выдать", color = MaterialTheme.colorScheme.onErrorContainer,
                        style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
                }
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            FilledTonalButton(onClick = {
                SettingsStore.update { EventType.entries.fold(this) { acc, t -> acc.withStyle(t, acc.style(t).copy(enabled = true)) } }
            }, modifier = Modifier.weight(1f).height(36.dp)) { Icon(Icons.Rounded.DoneAll, null, Modifier.size(18.dp)); Spacer(Modifier.width(6.dp)); Text("Все") }
            OutlinedButton(onClick = {
                SettingsStore.update { EventType.entries.fold(this) { acc, t -> acc.withStyle(t, acc.style(t).copy(enabled = false)) } }
            }, modifier = Modifier.weight(1f).height(36.dp)) { Icon(Icons.Rounded.RemoveDone, null, Modifier.size(18.dp)); Spacer(Modifier.width(6.dp)); Text("Ничего") }
        }
        EventCategory.entries.forEach { cat ->
            val types = EventType.entries.filter { it.cat == cat && (q.isBlank() || q.trim().lowercase() in (it.title + " " + it.hint).lowercase()) }
            if (types.isEmpty()) return@forEach
            SectionCard(cat.title) { GroupBox {
                FlowRow(
                    Modifier.fillMaxWidth().padding(horizontal = 6.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.Start,
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    maxItemsInEachRow = 5,
                ) {
                    types.forEach { t ->
                        val st = s.style(t)
                        val tint = Color(t.tint)
                        Column(
                            Modifier.weight(1f, fill = true).clip(RoundedCornerShape(14.dp)).combinedClickable(
                                onClick = { onEdit(t) },
                                onLongClick = {
                                    view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
                                    SettingsStore.updateStyle(t) { copy(enabled = !enabled) }
                                },
                            ).padding(vertical = 4.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            Box(
                                Modifier.size(44.dp).clip(RoundedCornerShape(if (st.enabled) 14.dp else 22.dp))
                                    .background(if (st.enabled) tint.copy(alpha = 0.85f) else MaterialTheme.colorScheme.surfaceContainerHighest),
                                contentAlignment = Alignment.Center,
                            ) {
                                Icon(
                                    iconFor(t, st.variant, st.icon), null, modifier = Modifier.size(22.dp),
                                    tint = if (st.enabled) Color(0xFF101114) else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                )
                            }
                            Text(
                                t.title, style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, lineHeight = 12.sp),
                                textAlign = TextAlign.Center, maxLines = 2, modifier = Modifier.padding(top = 3.dp, start = 2.dp, end = 2.dp),
                                color = if (st.enabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                    repeat((5 - types.size % 5) % 5) { Spacer(Modifier.weight(1f)) }
                }
            } }
        }
    }
}
