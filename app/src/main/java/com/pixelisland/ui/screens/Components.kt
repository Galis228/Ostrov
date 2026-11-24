package com.pixelisland.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Remove
import androidx.compose.material.icons.rounded.UnfoldMore
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import com.pixelisland.ui.i18n.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

/** Прокручиваемое тело экрана с плотными отступами. */
@Composable
fun ScrollBody(modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 12.dp, vertical = 4.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) { content(); Spacer(Modifier.height(16.dp)) }
}

/** Подвкладки внутри раздела — чтобы всё помещалось на один экран. */
@Composable
fun SubTabs(titles: List<String>, selected: Int, onSelect: (Int) -> Unit, modifier: Modifier = Modifier) {
    if (titles.size > 4) {
        androidx.compose.material3.PrimaryScrollableTabRow(
            selectedTabIndex = selected, edgePadding = 12.dp, modifier = modifier.fillMaxWidth(),
            containerColor = androidx.compose.ui.graphics.Color.Transparent, divider = {},
        ) {
            titles.forEachIndexed { i, t ->
                androidx.compose.material3.Tab(selected = selected == i, onClick = { onSelect(i) },
                    text = { Text(t, maxLines = 1, style = MaterialTheme.typography.labelLarge) })
            }
        }
        return
    }
    SingleChoiceSegmentedButtonRow(modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 4.dp)) {
        titles.forEachIndexed { i, t ->
            SegmentedButton(
                selected = selected == i, onClick = { onSelect(i) },
                shape = SegmentedButtonDefaults.itemShape(i, titles.size),
                icon = {},
            ) { Text(t, maxLines = 1, overflow = TextOverflow.Ellipsis, style = MaterialTheme.typography.labelMedium) }
        }
    }
}

/**
 * Группа в стиле настроек Android 16/17 на Pixel: пункты — отдельные плитки с зазором 2 dp,
 * снаружи большое скругление, между пунктами — маленькое.
 */
@Composable
fun SectionCard(title: String?, action: (@Composable () -> Unit)? = null, content: @Composable () -> Unit) {
    Column(Modifier.fillMaxWidth()) {
        if (title != null) Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                title, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.weight(1f).padding(start = 16.dp, top = 4.dp, bottom = 6.dp),
            )
            action?.invoke()
        }
        Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(24.dp)), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            content()
        }
    }
}

/** Фон одной плитки внутри группы. */
@Composable
fun Modifier.groupItem(): Modifier =
    this.fillMaxWidth().background(MaterialTheme.colorScheme.surfaceContainer, RoundedCornerShape(5.dp))

/** Произвольное содержимое как плитка группы. */
@Composable
fun GroupBox(content: @Composable ColumnScope.() -> Unit) {
    Column(Modifier.groupItem().padding(vertical = 8.dp)) { content() }
}

/** Одна строка: подпись — ползунок — значение. */
@Composable
fun SliderRow(
    title: String, value: Float, range: ClosedFloatingPointRange<Float>, format: (Float) -> String,
    steps: Int = 0, onChange: (Float) -> Unit,
) {
    Row(Modifier.groupItem().height(52.dp).padding(horizontal = 16.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(title, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(0.42f), maxLines = 2)
        Slider(value = value, onValueChange = onChange, valueRange = range, steps = steps, modifier = Modifier.weight(0.46f))
        Text(
            format(value), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.weight(0.16f).padding(start = 6.dp), maxLines = 1,
        )
    }
}

@Composable
fun SwitchRow(
    title: String, subtitle: String? = null, checked: Boolean, icon: ImageVector? = null,
    onTest: (() -> Unit)? = null, onChange: (Boolean) -> Unit,
) {
    Row(
        Modifier.groupItem().clickable { onChange(!checked) }.padding(horizontal = 16.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (icon != null) { Icon(icon, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(20.dp)); Spacer(Modifier.width(12.dp)) }
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyMedium)
            if (subtitle != null) Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Switch(
            checked = checked, onCheckedChange = onChange, modifier = Modifier.scale(0.85f),
            thumbContent = if (checked) { { Icon(Icons.Rounded.Check, null, Modifier.size(SwitchDefaults.IconSize)) } } else null,
        )
    }
}

@Composable
fun NavRow(icon: ImageVector, title: String, subtitle: String, onClick: () -> Unit) {
    Row(
        Modifier.groupItem().clickable(onClick = onClick).padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(icon, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(22.dp))
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyLarge)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Icon(Icons.AutoMirrored.Rounded.KeyboardArrowRight, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
fun <T> ChipRow(options: List<T>, selected: T, label: (T) -> String, onSelect: (T) -> Unit) {
    LazyRow(
        modifier = Modifier.groupItem().padding(vertical = 4.dp),
        contentPadding = PaddingValues(horizontal = 14.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        items(options.size) { i ->
            val o = options[i]
            FilterChip(
                selected = o == selected, onClick = { onSelect(o) },
                label = { Text(label(o), style = MaterialTheme.typography.labelMedium) },
                leadingIcon = if (o == selected) { { Icon(Icons.Rounded.Check, null, Modifier.size(16.dp)) } } else null,
            )
        }
    }
}

@Composable
fun ColorDot(color: Color, selected: Boolean, label: String? = null, size: Int = 36, onClick: () -> Unit) {
    val border = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
    Box(
        Modifier.size(size.dp).border(if (selected) 3.dp else 1.dp, border, CircleShape)
            .padding(if (selected) 4.dp else 1.dp).clip(CircleShape).background(color).clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        val on = if (color.luminance() > 0.5f) Color.Black else Color.White
        if (label != null) Text(label, color = on, style = MaterialTheme.typography.labelSmall)
        else if (selected) Icon(Icons.Rounded.Check, null, tint = on, modifier = Modifier.size(16.dp))
    }
}

/** Ползунок с кнопками ±1 для точной подгонки. */
@Composable
fun StepperRow(title: String, value: Int, range: IntRange, onChange: (Int) -> Unit) {
    Row(Modifier.groupItem().padding(horizontal = 8.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.width(76.dp).padding(start = 6.dp)) {
            Text(title, style = MaterialTheme.typography.bodyMedium)
            Text("$value px", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
        }
        FilledTonalIconButton(onClick = { onChange((value - 1).coerceIn(range)) }, modifier = Modifier.size(36.dp)) {
            Icon(Icons.Rounded.Remove, "Меньше")
        }
        Slider(
            value = value.toFloat(), onValueChange = { onChange(it.toInt()) },
            valueRange = range.first.toFloat()..range.last.toFloat(), modifier = Modifier.weight(1f).padding(horizontal = 4.dp),
        )
        FilledTonalIconButton(onClick = { onChange((value + 1).coerceIn(range)) }, modifier = Modifier.size(36.dp)) {
            Icon(Icons.Rounded.Add, "Больше")
        }
    }
}

/** Строка с выпадающим выбором. */
@Composable
fun <T> DropdownRow(title: String, icon: ImageVector?, options: List<T>, selected: T, label: (T) -> String, onSelect: (T) -> Unit) {
    var open by remember { mutableStateOf(false) }
    Row(
        Modifier.groupItem().clickable { open = true }.padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (icon != null) { Icon(icon, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(20.dp)); Spacer(Modifier.width(12.dp)) }
        Text(title, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
        Box {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(label(selected), style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                Icon(Icons.Rounded.UnfoldMore, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(18.dp))
            }
            DropdownMenu(expanded = open, onDismissRequest = { open = false }) {
                options.forEach { o ->
                    DropdownMenuItem(
                        text = { Text(label(o)) }, onClick = { onSelect(o); open = false },
                        trailingIcon = if (o == selected) { { Icon(Icons.Rounded.Check, null) } } else null,
                    )
                }
            }
        }
    }
}

@Composable
fun Hint(text: String) {
    Text(
        text, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 2.dp),
    )
}
