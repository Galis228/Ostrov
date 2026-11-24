package com.pixelisland.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.RestartAlt
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import com.pixelisland.ui.i18n.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import com.pixelisland.settings.EventType
import com.pixelisland.settings.IconVariant
import com.pixelisland.ui.IconCatalog
import com.pixelisland.ui.catalogIcon
import com.pixelisland.ui.iconFor

/** Выбор своего цвета: кольцо оттенков и квадрат насыщенности/яркости. */
@Composable
fun ColorPickerDialog(initial: Long, onDismiss: () -> Unit, onPick: (Long) -> Unit) {
    val hsv = remember {
        FloatArray(3).also { android.graphics.Color.colorToHSV((initial and 0xFFFFFFFF).toInt(), it) }
    }
    var h by remember { mutableFloatStateOf(hsv[0]) }
    var s by remember { mutableFloatStateOf(hsv[1].coerceAtLeast(0.05f)) }
    var v by remember { mutableFloatStateOf(hsv[2].coerceAtLeast(0.2f)) }
    val color = Color.hsv(h, s, v)
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = { TextButton(onClick = { onPick(color.toArgb().toLong() and 0xFFFFFFFFL); onDismiss() }) { Text("Готово") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Отмена") } },
        title = { Text("Свой цвет") },
        text = {
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Box(Modifier.size(240.dp), contentAlignment = Alignment.Center) {
                    // Кольцо оттенков
                    androidx.compose.foundation.Canvas(
                        Modifier.size(240.dp).pointerInput(Unit) {
                            detectDragGestures(onDragStart = { o -> h = hueAt(o, size.width / 2f) }) { ch, _ -> h = hueAt(ch.position, size.width / 2f) }
                        }.pointerInput(Unit) { detectTapGestures { o -> h = hueAt(o, size.width / 2f) } },
                    ) {
                        val stroke = 26.dp.toPx()
                        drawCircle(
                            Brush.sweepGradient((0..6).map { Color.hsv(it * 60f % 360f, 1f, 1f) }),
                            radius = size.minDimension / 2 - stroke / 2, style = androidx.compose.ui.graphics.drawscope.Stroke(stroke),
                        )
                        val r = size.minDimension / 2 - stroke / 2
                        val a = Math.toRadians(h.toDouble())
                        val c = center + androidx.compose.ui.geometry.Offset((r * kotlin.math.cos(a)).toFloat(), (r * kotlin.math.sin(a)).toFloat())
                        drawCircle(Color.White, 12.dp.toPx(), c, style = androidx.compose.ui.graphics.drawscope.Stroke(3.dp.toPx()))
                    }
                    // Квадрат насыщенности (→) и яркости (↑)
                    androidx.compose.foundation.Canvas(
                        Modifier.size(130.dp).clip(RoundedCornerShape(12.dp)).pointerInput(Unit) {
                            fun upd(o: androidx.compose.ui.geometry.Offset) {
                                s = (o.x / size.width).coerceIn(0f, 1f); v = 1f - (o.y / size.height).coerceIn(0f, 1f)
                            }
                            detectDragGestures(onDragStart = { upd(it) }) { ch, _ -> upd(ch.position) }
                        }.pointerInput(Unit) {
                            detectTapGestures { o -> s = (o.x / size.width).coerceIn(0f, 1f); v = 1f - (o.y / size.height).coerceIn(0f, 1f) }
                        },
                    ) {
                        drawRect(Brush.horizontalGradient(listOf(Color.White, Color.hsv(h, 1f, 1f))))
                        drawRect(Brush.verticalGradient(listOf(Color.Transparent, Color.Black)))
                        drawCircle(Color.White, 9.dp.toPx(), androidx.compose.ui.geometry.Offset(s * size.width, (1 - v) * size.height),
                            style = androidx.compose.ui.graphics.drawscope.Stroke(3.dp.toPx()))
                    }
                }
                Box(Modifier.fillMaxWidth().height(40.dp).clip(RoundedCornerShape(14.dp)).background(color), contentAlignment = Alignment.Center) {
                    Text("#%06X".format(color.toArgb() and 0xFFFFFF), color = if (v > 0.6f && s < 0.5f) Color.Black else Color.White)
                }
            }
        },
    )
}

private fun hueAt(o: androidx.compose.ui.geometry.Offset, c: Float): Float {
    val deg = Math.toDegrees(kotlin.math.atan2((o.y - c).toDouble(), (o.x - c).toDouble())).toFloat()
    return (deg + 360f) % 360f
}

/** Каталог иконок с поиском и группами. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IconCatalogSheet(type: EventType, variant: IconVariant, current: String, onDismiss: () -> Unit, onPick: (String) -> Unit) {
    var query by remember { mutableStateOf("") }
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(Modifier.padding(horizontal = 16.dp)) {
            OutlinedTextField(
                value = query, onValueChange = { query = it }, singleLine = true,
                leadingIcon = { Icon(Icons.Rounded.Search, null) },
                placeholder = { Text("Поиск: bolt, wifi, звук, время…") },
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(8.dp))
            val q = query.trim().lowercase()
            val list = IconCatalog.filter { q.isEmpty() || q in it.name.lowercase() || q in it.group.lowercase() }
            LazyVerticalGrid(
                columns = GridCells.Adaptive(56.dp), modifier = Modifier.heightIn(max = 520.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    Row(
                        Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).clickable { onPick(""); onDismiss() }
                            .background(if (current.isEmpty()) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainerHigh)
                            .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(iconFor(type, variant), null); Spacer(Modifier.size(10.dp))
                        Text("Стандартная иконка события", modifier = Modifier.weight(1f))
                        Icon(Icons.Rounded.RestartAlt, null)
                    }
                }
                var lastGroup = ""
                list.forEach { ic ->
                    if (ic.group != lastGroup && q.isEmpty()) {
                        lastGroup = ic.group
                        item(span = { GridItemSpan(maxLineSpan) }) {
                            Text(ic.group, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(top = 8.dp))
                        }
                    }
                    item {
                        val sel = ic.name == current
                        Box(
                            Modifier.aspectRatio(1f).clip(RoundedCornerShape(16.dp))
                                .background(if (sel) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceContainerHigh)
                                .border(if (sel) 0.dp else 0.dp, Color.Transparent, RoundedCornerShape(16.dp))
                                .clickable { onPick(ic.name); onDismiss() },
                            contentAlignment = Alignment.Center,
                        ) {
                            catalogIcon(ic.name, variant)?.let {
                                Icon(it, ic.name, tint = if (sel) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface)
                            }
                        }
                    }
                }
                item(span = { GridItemSpan(maxLineSpan) }) { Spacer(Modifier.height(24.dp)) }
            }
        }
    }
}
