package com.pixelisland.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import com.pixelisland.ui.i18n.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.pixelisland.core.Stats
import com.pixelisland.settings.EventType
import com.pixelisland.settings.SettingsStore
import com.pixelisland.ui.iconFor
import java.util.Calendar

/** Статистика за неделю: столбики по дням и самые частые события. */
@Composable
fun StatsView() {
    val days by Stats.days.collectAsState()
    val keys = Stats.lastDays(7)
    val totals = keys.map { days[it]?.values?.sum() ?: 0 }
    val max = (totals.maxOrNull() ?: 0).coerceAtLeast(1)
    val primary = MaterialTheme.colorScheme.primary
    val track = MaterialTheme.colorScheme.surfaceContainerHighest
    val week = totals.sum()
    SectionCard("За 7 дней") {
        GroupBox {
            Row(Modifier.padding(horizontal = 16.dp), verticalAlignment = Alignment.Bottom) {
                Text("$week", style = MaterialTheme.typography.displaySmall, color = primary)
                Spacer(Modifier.width(8.dp))
                Text("событий · в среднем ${week / 7} в день", style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(bottom = 8.dp))
            }
            Canvas(Modifier.fillMaxWidth().height(120.dp).padding(horizontal = 16.dp, vertical = 8.dp)) {
                val n = totals.size
                val gap = 10.dp.toPx()
                val bw = (size.width - gap * (n - 1)) / n
                totals.forEachIndexed { i, v ->
                    val x = i * (bw + gap)
                    drawRoundRect(track, Offset(x, 0f), Size(bw, size.height), CornerRadius(bw / 3))
                    val h = size.height * v / max
                    if (h > 0) drawRoundRect(if (i == n - 1) primary else primary.copy(alpha = 0.55f), Offset(x, size.height - h), Size(bw, h), CornerRadius(bw / 3))
                }
            }
            Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
                val names = listOf("Вс", "Пн", "Вт", "Ср", "Чт", "Пт", "Сб")
                keys.forEachIndexed { i, _ ->
                    val dow = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -(keys.size - 1 - i)) }.get(Calendar.DAY_OF_WEEK)
                    Text(names[dow - 1], style = MaterialTheme.typography.labelSmall, textAlign = TextAlign.Center, modifier = Modifier.weight(1f),
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
    val byType = keys.flatMap { days[it]?.entries.orEmpty() }.groupBy({ it.key }, { it.value }).mapValues { it.value.sum() }
        .entries.sortedByDescending { it.value }.take(8)
    if (byType.isNotEmpty()) SectionCard("Чаще всего") {
        val top = byType.first().value.coerceAtLeast(1)
        byType.forEach { (t, n) ->
            val st = SettingsStore.value.style(t)
            Row(Modifier.groupItem().padding(horizontal = 16.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(iconFor(t, st.variant, st.icon), null, tint = Color(t.tint), modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(t.title, style = MaterialTheme.typography.bodyMedium)
                    Canvas(Modifier.fillMaxWidth().height(6.dp)) {
                        drawRoundRect(track, cornerRadius = CornerRadius(size.height / 2))
                        drawRoundRect(Color(t.tint), size = Size(size.width * n / top, size.height), cornerRadius = CornerRadius(size.height / 2))
                    }
                }
                Spacer(Modifier.width(12.dp))
                Text("$n", style = MaterialTheme.typography.labelLarge, color = primary)
            }
        }
    } else Hint("Здесь появится статистика, когда остров покажет первые события.")
    OutlinedButton(onClick = { Stats.clear() }, modifier = Modifier.fillMaxWidth()) { Text("Сбросить статистику") }
}
