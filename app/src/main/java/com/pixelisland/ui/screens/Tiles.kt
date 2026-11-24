package com.pixelisland.ui.screens

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.OpenInNew
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pixelisland.settings.AnimStyle
import com.pixelisland.settings.AppearStyle
import com.pixelisland.settings.ExBg
import com.pixelisland.settings.ExButtons
import com.pixelisland.settings.ExIconSize
import com.pixelisland.settings.ExProgress
import com.pixelisland.settings.ExTap
import com.pixelisland.settings.ExWidth
import com.pixelisland.settings.GestureAction
import com.pixelisland.settings.GradientStyle
import com.pixelisland.settings.IdleContent
import com.pixelisland.settings.PillFx
import com.pixelisland.settings.RateKind
import com.pixelisland.settings.SettingsStore
import com.pixelisland.settings.StatusStyle
import com.pixelisland.settings.TextWeight
import com.pixelisland.ui.LocalIslandColors
import com.pixelisland.ui.drawPillFx
import com.pixelisland.ui.i18n.Text

// Плитки-картинки для выбора настроек: показывают результат, а не название.

private val Dim = Color(0xFF8F95A3)
private val PillBg = Color(0xFF2A2D35)
private val Cam = Color(0xFF07080A)

/** Характер анимации — кривая движения (как график в инструментах дизайнера). */
@Composable
fun AnimCurveTile(a: AnimStyle) {
    val ac = MaterialTheme.colorScheme.primary
    Canvas(Modifier.size(54.dp, 34.dp)) {
        val w = size.width; val h = size.height
        drawLine(Dim.copy(alpha = 0.4f), Offset(0f, h * 0.25f), Offset(w, h * 0.25f), 1f)
        val p = Path()
        val n = 40
        for (i in 0..n) {
            val t = i / n.toFloat()
            val v = when (a) {
                AnimStyle.SPRING -> 1 - kotlin.math.exp(-6f * t) * kotlin.math.cos(9f * t)
                AnimStyle.JELLY -> 1 - kotlin.math.exp(-3.2f * t) * kotlin.math.cos(14f * t)
                AnimStyle.SMOOTH -> 1 - (1 - t) * (1 - t) * (1 - t)
                AnimStyle.SNAPPY -> (1 - kotlin.math.exp(-14f * t)).toFloat()
                AnimStyle.NONE -> if (t < 0.02f) 0f else 1f
            }
            val x = t * w; val y = h - v * h * 0.75f
            if (i == 0) p.moveTo(x, y) else p.lineTo(x, y)
        }
        drawPath(p, ac, style = Stroke(2.5.dp.toPx(), cap = androidx.compose.ui.graphics.StrokeCap.Round))
    }
}

/** Появление — схема: из камеры / всплытие / растворение / сверху. */
@Composable
fun AppearTile(a: AppearStyle) {
    val ac = MaterialTheme.colorScheme.primary
    Canvas(Modifier.size(56.dp, 36.dp)) {
        val w = size.width; val h = size.height
        val pw = w * 0.8f; val ph = h * 0.36f
        val top = h * 0.5f - ph / 2
        fun pill(scale: Float, alpha: Float, dy: Float = 0f) {
            val sw = pw * scale; val sh = ph * scale
            drawRoundRect(ac.copy(alpha = alpha), Offset((w - sw) / 2, top + (ph - sh) / 2 + dy), Size(sw, sh), CornerRadius(sh / 2))
        }
        when (a) {
            AppearStyle.FROM_CAMERA -> { pill(1f, 0.25f); pill(0.65f, 0.45f); pill(0.32f, 0.8f) }
            AppearStyle.POP -> { pill(1f, 0.9f); drawRoundRect(ac.copy(alpha = 0.35f), Offset((w - pw * 1.15f) / 2, top - ph * 0.08f), Size(pw * 1.15f, ph * 1.16f), CornerRadius(ph), style = Stroke(1.5f.dp.toPx())) }
            AppearStyle.FADE -> drawRoundRect(Brush.horizontalGradient(listOf(ac.copy(alpha = 0.1f), ac, ac.copy(alpha = 0.1f))), Offset((w - pw) / 2, top), Size(pw, ph), CornerRadius(ph / 2))
            AppearStyle.DROP -> { pill(1f, 0.3f, -h * 0.32f); pill(1f, 0.9f) }
        }
        drawCircle(Cam, ph * 0.28f, Offset(w / 2, h / 2))
    }
}

/** Мини-пилюля с градиентом. */
@Composable
fun GradientTile(g: GradientStyle) {
    val c = LocalIslandColors.current
    val second = Color(SettingsStore.value.bgCustom2)
    val shimmer = rememberInfiniteTransition(label = "g").animateFloat(0f, 1f, infiniteRepeatable(tween(2400, easing = LinearEasing)), label = "s")
    Canvas(Modifier.size(56.dp, 22.dp)) {
        val r = CornerRadius(size.height / 2)
        val base = Color(0xFF15161A)
        val brush = when (g) {
            GradientStyle.NONE -> androidx.compose.ui.graphics.SolidColor(base)
            GradientStyle.ACCENT -> Brush.horizontalGradient(listOf(base, c.accent.copy(alpha = 0.6f), base))
            GradientStyle.CUSTOM -> Brush.horizontalGradient(listOf(base, second.copy(alpha = 0.8f)))
            GradientStyle.DIAGONAL -> Brush.linearGradient(listOf(second.copy(alpha = 0.7f), base, c.accent.copy(alpha = 0.6f)))
            GradientStyle.ANIMATED -> {
                val t = shimmer.value; val w0 = size.width
                Brush.linearGradient(listOf(base, c.accent.copy(alpha = 0.6f), second.copy(alpha = 0.5f), base),
                    start = Offset(-w0 + 2 * w0 * t, 0f), end = Offset(w0 * t, size.height))
            }
        }
        drawRoundRect(brush, cornerRadius = r)
        drawCircle(Cam, size.height * 0.28f, center)
    }
}

/** Мини-пилюля с живым фоном — анимируется прямо в плитке. */
@Composable
fun PillFxTile(fx: PillFx) {
    val c = LocalIslandColors.current
    val t = rememberInfiniteTransition(label = "fx").animateFloat(0f, 8f, infiniteRepeatable(tween(8000, easing = LinearEasing)), label = "t")
    Canvas(Modifier.size(58.dp, 24.dp)) {
        drawRoundRect(Color(0xFF15161A), cornerRadius = CornerRadius(size.height / 2))
        drawPillFx(if (fx == PillFx.NONE) PillFx.NONE else fx, t.value, c.accent, Color(SettingsStore.value.bgCustom2), true)
        drawCircle(Cam, size.height * 0.26f, center)
    }
}

/** Как выглядит «Вкл»: словом или галочкой. */
@Composable
fun StatusTile(st: StatusStyle) {
    val c = LocalIslandColors.current
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        when (st) {
            StatusStyle.TEXT -> { Text("Вкл", color = c.accent, fontSize = 12.sp, fontWeight = FontWeight.SemiBold); Text("Выкл", color = c.dim, fontSize = 12.sp) }
            StatusStyle.CHECK -> { Icon(Icons.Rounded.Done, null, tint = c.accent, modifier = Modifier.size(20.dp)); Icon(Icons.Rounded.RemoveDone, null, tint = c.dim, modifier = Modifier.size(20.dp)) }
            StatusStyle.CHECK_CIRCLE -> { Icon(Icons.Rounded.CheckCircle, null, tint = c.accent, modifier = Modifier.size(20.dp)); Icon(Icons.Rounded.Unpublished, null, tint = c.dim, modifier = Modifier.size(20.dp)) }
        }
    }
}

/** Толщина текста — «Aa» этой толщины. */
@Composable
fun WeightTile(w: TextWeight) {
    val c = LocalIslandColors.current
    androidx.compose.material3.Text("Aa", color = c.fg, fontSize = 20.sp, fontWeight = when (w) {
        TextWeight.REGULAR -> FontWeight.Normal; TextWeight.MEDIUM -> FontWeight.SemiBold
        TextWeight.BOLD -> FontWeight.Bold; TextWeight.BLACK -> FontWeight.Black
    })
}

/** Размер иконки в раскрытом виде. */
@Composable
fun ExIconTile(s: ExIconSize) {
    val ac = MaterialTheme.colorScheme.primary
    Canvas(Modifier.size(52.dp, 34.dp)) {
        val k = s.dp / 80f
        val sz = size.height * (0.4f + 0.6f * k)
        drawRoundRect(ac, Offset(0f, (size.height - sz) / 2), Size(sz, sz), CornerRadius(sz / 3))
        drawRoundRect(Dim, Offset(sz + 6f, size.height * 0.32f), Size(size.width - sz - 6f, size.height * 0.14f), CornerRadius(4f))
        drawRoundRect(Dim.copy(alpha = 0.6f), Offset(sz + 6f, size.height * 0.56f), Size((size.width - sz) * 0.6f, size.height * 0.12f), CornerRadius(4f))
    }
}

/** Прогресс в раскрытом виде. */
@Composable
fun ExProgressTile(p: ExProgress) {
    val ac = MaterialTheme.colorScheme.primary
    Canvas(Modifier.size(52.dp, 30.dp)) {
        val y = size.height / 2
        when (p) {
            ExProgress.WAVE -> {
                val path = Path(); var x = 0f
                path.moveTo(0f, y)
                while (x <= size.width * 0.65f) { path.lineTo(x, y + kotlin.math.sin(x / 5f) * 4f); x += 2f }
                drawPath(path, ac, style = Stroke(3.dp.toPx(), cap = androidx.compose.ui.graphics.StrokeCap.Round))
                drawLine(ac.copy(alpha = 0.3f), Offset(size.width * 0.7f, y), Offset(size.width, y), 3.dp.toPx(), androidx.compose.ui.graphics.StrokeCap.Round)
            }
            ExProgress.LINE -> {
                drawLine(ac.copy(alpha = 0.3f), Offset(0f, y), Offset(size.width, y), 5.dp.toPx(), androidx.compose.ui.graphics.StrokeCap.Round)
                drawLine(ac, Offset(0f, y), Offset(size.width * 0.65f, y), 5.dp.toPx(), androidx.compose.ui.graphics.StrokeCap.Round)
            }
            ExProgress.RING -> {
                val r = size.height * 0.38f
                drawCircle(ac.copy(alpha = 0.3f), r, center, style = Stroke(3.dp.toPx()))
                drawArc(ac, -90f, 234f, false, Offset(center.x - r, center.y - r), Size(2 * r, 2 * r), style = Stroke(3.dp.toPx(), cap = androidx.compose.ui.graphics.StrokeCap.Round))
            }
            ExProgress.NONE -> drawLine(Dim.copy(alpha = 0.35f), Offset(0f, y), Offset(size.width, y), 2f, pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(6f, 6f)))
        }
    }
}

/** Кнопки в раскрытом виде. */
@Composable
fun ExButtonsTile(b: ExButtons) {
    val ac = MaterialTheme.colorScheme.primary
    Canvas(Modifier.size(56.dp, 22.dp)) {
        val bw = (size.width - 6f) / 2
        repeat(2) { i ->
            val x = i * (bw + 6f)
            when (b) {
                ExButtons.FILLED -> drawRoundRect(ac, Offset(x, 0f), Size(bw, size.height), CornerRadius(size.height / 2))
                ExButtons.TONAL -> drawRoundRect(Dim.copy(alpha = 0.35f), Offset(x, 0f), Size(bw, size.height), CornerRadius(size.height / 2))
                ExButtons.ICONS -> drawCircle(ac, size.height * 0.28f, Offset(x + bw / 2, size.height / 2))
                ExButtons.NONE -> drawRoundRect(Dim.copy(alpha = 0.4f), Offset(x, 0f), Size(bw, size.height), CornerRadius(size.height / 2),
                    style = Stroke(1.5f, pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(5f, 5f))))
            }
        }
    }
}

/** Фон раскрытого вида. */
@Composable
fun ExBgTile(b: ExBg, custom: Long) {
    val c = LocalIslandColors.current
    Canvas(Modifier.size(56.dp, 34.dp)) {
        val r = CornerRadius(10.dp.toPx())
        when (b) {
            ExBg.SAME -> drawRoundRect(Color(0xFF15161A), cornerRadius = r)
            ExBg.ART_BLUR -> drawRoundRect(Brush.linearGradient(listOf(Color(0xFFFF8AD8), Color(0xFF6EA8FF), Color(0xFF5CE1E6))), alpha = 0.75f, cornerRadius = r)
            ExBg.TINT -> { drawRoundRect(Color(0xFF15161A), cornerRadius = r); drawRoundRect(c.accent.copy(alpha = 0.35f), cornerRadius = r) }
            ExBg.CUSTOM -> drawRoundRect(Color(custom), cornerRadius = r)
        }
        drawRoundRect(Color.White.copy(alpha = 0.55f), Offset(size.width * 0.12f, size.height * 0.4f), Size(size.width * 0.5f, size.height * 0.14f), CornerRadius(4f))
    }
}

/** Ширина раскрытого вида относительно экрана. */
@Composable
fun ExWidthTile(w: ExWidth) {
    val ac = MaterialTheme.colorScheme.primary
    Canvas(Modifier.size(56.dp, 34.dp)) {
        drawRoundRect(Dim.copy(alpha = 0.5f), cornerRadius = CornerRadius(6.dp.toPx()), style = Stroke(1.5f))
        val f = when (w) { ExWidth.FULL -> 0.9f; ExWidth.MEDIUM -> 0.72f; ExWidth.CONTENT -> 0.5f }
        val bw = size.width * f
        drawRoundRect(ac, Offset((size.width - bw) / 2, size.height * 0.15f), Size(bw, size.height * 0.4f), CornerRadius(size.height * 0.2f))
    }
}

@Composable
fun SimpleIconTile(icon: ImageVector) {
    val c = LocalIslandColors.current
    Icon(icon, null, tint = c.accent, modifier = Modifier.size(24.dp))
}

fun exTapIcon(t: ExTap): ImageVector = when (t) {
    ExTap.OPEN -> Icons.AutoMirrored.Rounded.OpenInNew
    ExTap.COLLAPSE -> Icons.Rounded.UnfoldLess
    ExTap.NOTHING -> Icons.Rounded.DoNotTouch
}

fun idleIcon(i: IdleContent): ImageVector = when (i) {
    IdleContent.EMPTY -> Icons.Rounded.RadioButtonUnchecked
    IdleContent.STEPS -> Icons.Rounded.DirectionsWalk
    IdleContent.BATTERY -> Icons.Rounded.BatteryFull
    IdleContent.HEADSET -> Icons.Rounded.Headphones
    IdleContent.TIME -> Icons.Rounded.Schedule
    IdleContent.MISSED -> Icons.Rounded.PhoneMissed
    IdleContent.RATE -> Icons.Rounded.CurrencyExchange
    IdleContent.NET_SPEED -> Icons.Rounded.Speed
}

/** Курс — символами валют. */
@Composable
fun RateTile(r: RateKind) {
    val c = LocalIslandColors.current
    val txt = when (r) {
        RateKind.USD_UAH -> "$→₴"; RateKind.EUR_UAH -> "€→₴"; RateKind.USD_EUR -> "$→€"
        RateKind.PLN_UAH -> "zł→₴"; RateKind.BTC_USD -> "₿"; RateKind.ETH_USD -> "Ξ"
    }
    androidx.compose.material3.Text(txt, color = c.accent, fontSize = 17.sp, fontWeight = FontWeight.Bold)
}

fun gestureIcon(g: GestureAction): ImageVector = when (g) {
    GestureAction.NONE -> Icons.Rounded.Block
    GestureAction.EXPAND -> Icons.Rounded.OpenInFull
    GestureAction.OPEN_APP -> Icons.AutoMirrored.Rounded.OpenInNew
    GestureAction.DISMISS -> Icons.Rounded.Close
    GestureAction.SWITCH -> Icons.Rounded.SwapHoriz
    GestureAction.PLAY_PAUSE -> Icons.Rounded.PlayArrow
    GestureAction.NEXT_TRACK -> Icons.Rounded.SkipNext
    GestureAction.PREV_TRACK -> Icons.Rounded.SkipPrevious
    GestureAction.STACK -> Icons.Rounded.ViewAgenda
    GestureAction.SHADE -> Icons.Rounded.VerticalAlignBottom
    GestureAction.HIDE_10 -> Icons.Rounded.VisibilityOff
    GestureAction.QUICK -> Icons.Rounded.Apps
    GestureAction.SHELF -> Icons.Rounded.Inventory2
    GestureAction.TORCH -> Icons.Rounded.FlashlightOn
    GestureAction.CAMERA -> Icons.Rounded.PhotoCamera
    GestureAction.SCREENSHOT -> Icons.Rounded.Screenshot
    GestureAction.ASSISTANT -> Icons.Rounded.Mic
}

/** Строка жеста: слева — жест, справа — иконка действия. Тап открывает сетку иконок. */
@Composable
fun GestureRow(title: String, icon: ImageVector, options: List<GestureAction>, selected: GestureAction, onSelect: (GestureAction) -> Unit) {
    var open by remember { mutableStateOf(false) }
    val cs = MaterialTheme.colorScheme
    Row(Modifier.groupItem().clickable { open = true }.padding(horizontal = 16.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, tint = cs.onSurfaceVariant, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(12.dp))
        Text(title, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(Modifier.size(44.dp, 36.dp).clip(RoundedCornerShape(12.dp)).background(cs.secondaryContainer), contentAlignment = Alignment.Center) {
                Icon(gestureIcon(selected), selected.label, tint = cs.onSecondaryContainer, modifier = Modifier.size(22.dp))
            }
            Text(selected.label, style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp), color = cs.onSurfaceVariant,
                maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.width(72.dp), textAlign = TextAlign.Center)
        }
    }
    if (open) AlertDialog(
        onDismissRequest = { open = false },
        title = { Text(title) },
        text = {
            LazyVerticalGrid(GridCells.Fixed(4), Modifier.height(320.dp), verticalArrangement = Arrangement.spacedBy(8.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                items(options) { o ->
                    val sel = o == selected
                    Column(Modifier.clip(RoundedCornerShape(14.dp)).clickable { onSelect(o); open = false }.padding(vertical = 4.dp),
                        horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            Modifier.size(48.dp).clip(RoundedCornerShape(if (sel) 14.dp else 24.dp))
                                .background(if (sel) cs.primary else cs.surfaceContainerHighest),
                            contentAlignment = Alignment.Center,
                        ) { Icon(gestureIcon(o), o.label, tint = if (sel) cs.onPrimary else cs.onSurface) }
                        Text(o.label, style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp, lineHeight = 11.sp), maxLines = 2,
                            textAlign = TextAlign.Center, modifier = Modifier.padding(top = 2.dp))
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = { open = false }) { Text("Закрыть") } },
    )
}

/** Ряд плиток с иконками Material в стиле приложения (не острова) — для общих настроек. */
@Composable
fun <T> IconChoiceRow(options: List<T>, selected: T, label: (T) -> String, icon: (T) -> ImageVector, onSelect: (T) -> Unit) {
    val cs = MaterialTheme.colorScheme
    Row(
        Modifier.groupItem().horizontalScrollCompat().padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        options.forEach { o ->
            val sel = o == selected
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(60.dp).clip(RoundedCornerShape(14.dp)).clickable { onSelect(o) }) {
                Box(
                    Modifier.size(52.dp, 44.dp).clip(RoundedCornerShape(if (sel) 14.dp else 22.dp))
                        .background(if (sel) cs.primary else cs.surfaceContainerHighest),
                    contentAlignment = Alignment.Center,
                ) { Icon(icon(o), label(o), tint = if (sel) cs.onPrimary else cs.onSurface) }
                Text(label(o), style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp), maxLines = 1, overflow = TextOverflow.Ellipsis,
                    color = if (sel) cs.primary else cs.onSurfaceVariant, modifier = Modifier.padding(top = 3.dp))
            }
        }
    }
}

@Composable
private fun Modifier.horizontalScrollCompat(): Modifier = this.then(Modifier.horizontalScroll(androidx.compose.foundation.rememberScrollState()))
