package com.pixelisland.ui.content

import android.graphics.Bitmap
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Unpublished
import androidx.compose.material.icons.rounded.Done
import androidx.compose.material.icons.rounded.RemoveDone
import com.pixelisland.ui.i18n.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pixelisland.settings.EventStyle
import com.pixelisland.settings.IconAnim
import com.pixelisland.settings.IconContainer
import com.pixelisland.settings.SlotContent
import com.pixelisland.ui.LocalIslandColors
import com.pixelisland.settings.TextWeight
import com.pixelisland.settings.RingStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.graphics.BlurEffect
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

/** Всё, что может понадобиться слоту. */
data class SlotData(
    val vector: ImageVector,
    val photo: Bitmap? = null,       // фото контакта / обложка — без тонировки
    val tintedIcon: Bitmap? = null,  // маленькая иконка уведомления — тонируется цветом
    val title: String = "",
    val value: String = "",
    val chronoBase: Long? = null,
    val countDown: Boolean = false,
    val ring: Float? = null,
    val playing: Boolean = true,
    val auto: Color,
    val key: Any = Unit,             // смена ключа заново проигрывает анимацию иконки
)

@Composable
fun SlotView(c: SlotContent, d: SlotData, style: EventStyle, size: Dp, color: Color) {
    when (c) {
        SlotContent.NONE -> Unit
        SlotContent.ICON -> AnimatedIcon(d, style, size, color)
        SlotContent.ART -> if (d.photo != null) BitmapImage(d.photo, size, size / 2.6f, null, d.vector, color)
        else AnimatedIcon(d, style, size, color)
        SlotContent.EQ -> Box(Modifier.size(size), contentAlignment = Alignment.Center) {
            Equalizer(d.playing, color, Modifier.size(size * 0.72f, size * 0.62f))
        }
        SlotContent.TITLE -> SlotText(d.title, null, style.marquee, style)
        SlotContent.ICON_TITLE -> Row(verticalAlignment = Alignment.CenterVertically) {
            AnimatedIcon(d, style, size, color)
            Spacer(Modifier.width(7.dp))
            SlotText(d.title, null, false, style)
        }
        SlotContent.VALUE -> { val v = valueText(d); if (!StatusIcon(v, color, size)) SlotText(v, color, false, style) }
        SlotContent.RING -> Progress(d.ring ?: 0f, color, size, style)
        SlotContent.VALUE_RING -> Row(verticalAlignment = Alignment.CenterVertically) {
            val v = valueText(d)
            if (v.isNotEmpty()) { if (!StatusIcon(v, color, size)) SlotText(v, color, false, style); Spacer(Modifier.width(6.dp)) }
            Progress(d.ring ?: 0f, color, size * 0.92f, style)
        }
    }
}

/** Галочка вместо «Вкл/Подключено», перечёркнутая — вместо «Выкл/Отключено». Возвращает true, если нарисовала. */
@Composable
private fun StatusIcon(value: String, color: Color, size: Dp): Boolean {
    val style = com.pixelisland.settings.SettingsStore.value.statusStyle
    if (style == com.pixelisland.settings.StatusStyle.TEXT) return false
    val on = com.pixelisland.settings.statusOf(value) ?: return false
    val icon = when (style) {
        com.pixelisland.settings.StatusStyle.CHECK_CIRCLE -> if (on) androidx.compose.material.icons.Icons.Rounded.CheckCircle else androidx.compose.material.icons.Icons.Rounded.Unpublished
        else -> if (on) androidx.compose.material.icons.Icons.Rounded.Done else androidx.compose.material.icons.Icons.Rounded.RemoveDone
    }
    val c = LocalIslandColors.current
    Icon(icon, value, tint = if (on) color else c.dim, modifier = Modifier.size(size * 0.72f))
    return true
}

@Composable
private fun valueText(d: SlotData): String {
    if (d.chronoBase != null) {
        val now = rememberWallClock()
        val ms = if (d.countDown) d.chronoBase - now else now - d.chronoBase
        return formatDuration(ms.coerceAtLeast(0))
    }
    return d.value
}

@Composable
private fun SlotText(text: String, color: Color?, marquee: Boolean, style: EventStyle) {
    val c = LocalIslandColors.current
    Text(
        text, color = color ?: c.fg, fontSize = 13.sp, maxLines = 1,
        fontWeight = when (style.weight) {
            TextWeight.REGULAR -> FontWeight.Normal; TextWeight.MEDIUM -> FontWeight.SemiBold
            TextWeight.BOLD -> FontWeight.Bold; TextWeight.BLACK -> FontWeight.Black
        },
        style = if (style.monoDigits) TextStyle(fontFeatureSettings = "tnum") else TextStyle.Default,
        overflow = if (marquee) TextOverflow.Clip else TextOverflow.Ellipsis,
        modifier = if (marquee) Modifier.basicMarquee(iterations = 2, initialDelayMillis = 1500) else Modifier,
    )
}

/** Индикатор прогресса в выбранном стиле (заливка фона рисуется отдельно, на всей пилюле). */
@Composable
private fun Progress(p: Float, color: Color, size: Dp, style: EventStyle) {
    val v by animateFloatAsState(p.coerceIn(0f, 1f), tween(500), label = "pr")
    when (style.ring) {
        RingStyle.RING, RingStyle.FILL -> Ring(p, color, size * 0.78f, style.ringThick.dp)
        RingStyle.BAR -> Box(Modifier.size(size * 1.5f, style.ringThick.dp + 1.dp).clip(RoundedCornerShape(50)).background(color.copy(alpha = 0.25f))) {
            Box(Modifier.fillMaxHeight().fillMaxWidth(v).clip(RoundedCornerShape(50)).background(color))
        }
        RingStyle.DOTS -> Row(horizontalArrangement = Arrangement.spacedBy(2.dp), verticalAlignment = Alignment.CenterVertically) {
            repeat(5) { i ->
                val on = v >= (i + 0.5f) / 5f
                Box(Modifier.size(size * 0.16f).clip(CircleShape).background(if (on) color else color.copy(alpha = 0.25f)))
            }
        }
    }
}

/** Иконка в выбранном контейнере с выбранной анимацией. */
@Composable
fun AnimatedIcon(d: SlotData, style: EventStyle, size: Dp, color: Color) {
    val px = with(LocalDensity.current) { size.toPx() }
    val scale = remember { Animatable(1f) }
    val rot = remember { Animatable(0f) }
    val dx = remember { Animatable(0f) }
    val alpha = remember { Animatable(1f) }
    val dy = remember { Animatable(0f) }
    val flip = remember { Animatable(0f) }
    val blur = remember { Animatable(0f) }
    val morphP = remember { Animatable(1f) }
    LaunchedEffect(d.key, style.anim) {
        morphP.snapTo(1f)
        scale.snapTo(1f); rot.snapTo(0f); dx.snapTo(0f); alpha.snapTo(1f); dy.snapTo(0f); flip.snapTo(0f); blur.snapTo(0f)
        when (style.anim) {
            IconAnim.POP -> { scale.snapTo(0.2f); scale.animateTo(1f, spring(0.42f, 600f)) }
            IconAnim.SHAKE -> rot.animateTo(0f, keyframes {
                durationMillis = 650; -16f at 80; 14f at 200; -10f at 320; 7f at 440; -3f at 560
            })
            IconAnim.SPIN -> { rot.snapTo(-180f); rot.animateTo(0f, spring(0.6f, 300f)) }
            IconAnim.SLIDE -> { dx.snapTo(-px * 0.8f); alpha.snapTo(0f)
                coroutineScope {
                    launch { alpha.animateTo(1f, tween(220)) }
                    dx.animateTo(0f, spring(0.7f, 500f))
                } }
            IconAnim.BOUNCE -> dy.animateTo(0f, keyframes {
                durationMillis = 700; -px * 0.35f at 120; 0f at 260; -px * 0.15f at 380; 0f at 500; -px * 0.05f at 600
            })
            IconAnim.BLINK -> alpha.animateTo(1f, keyframes {
                durationMillis = 900; 0.15f at 150; 1f at 300; 0.15f at 450; 1f at 600
            })
            IconAnim.FLIP -> { flip.snapTo(180f); flip.animateTo(0f, spring(0.55f, 260f)) }
            IconAnim.MORPH -> { morphP.snapTo(0f); scale.snapTo(0.6f)
                coroutineScope {
                    launch { scale.animateTo(1f, spring(0.5f, 500f)) }
                    morphP.animateTo(1f, spring(0.6f, 180f))
                } }
            IconAnim.BLUR_IN -> { blur.snapTo(1f); alpha.snapTo(0f)
                coroutineScope {
                    launch { alpha.animateTo(1f, tween(260)) }
                    blur.animateTo(0f, tween(420))
                } }
            else -> Unit
        }
    }
    // Повторяющиеся анимации ограничены по времени и читаются только при отрисовке — без перекомпоновки.
    val pulse = remember { Animatable(1f) }
    val spin = remember { Animatable(0f) }
    LaunchedEffect(d.key, style.anim) {
        pulse.snapTo(1f); spin.snapTo(0f)
        when (style.anim) {
            IconAnim.PULSE -> repeat(5) {
                pulse.animateTo(1.14f, tween(650)); pulse.animateTo(1f, tween(650))
            }
            IconAnim.HEARTBEAT -> repeat(4) {
                pulse.animateTo(1f, keyframes { durationMillis = 1100; 1.18f at 120; 1f at 240; 1.12f at 360; 1f at 520 })
            }
            IconAnim.SHAPE_SPIN -> spin.animateTo(360f, tween(4000, easing = androidx.compose.animation.core.LinearEasing))
            else -> Unit
        }
    }
    Box(
        Modifier.size(size).graphicsLayer {
            scaleX = scale.value * pulse.value; scaleY = scale.value * pulse.value
            rotationZ = rot.value; translationX = dx.value; translationY = dy.value
            rotationY = flip.value; cameraDistance = 12f * density
            this.alpha = alpha.value
            if (blur.value > 0.01f) renderEffect = BlurEffect(blur.value * 10.dp.toPx(), blur.value * 10.dp.toPx())
        },
        contentAlignment = Alignment.Center,
    ) {
        if (d.photo != null) {
            BitmapImage(d.photo, size, size / 2, null, d.vector, color)
            return@Box
        }
        val morph = remember(style.container) { style.container.morphFromCircle() }
        val poly = remember(style.container) { style.container.polygon() }
        val shape: Shape = when {
            style.anim == IconAnim.MORPH && morph != null && morphP.value < 0.999f -> MorphShape(morph, morphP.value)
            else -> style.container.shape()
        }
        val iconColor = if (style.container == IconContainer.SOLID) (if (color.luminance() > 0.45f) Color.Black else Color.White) else color
        val box = when (style.container) {
            IconContainer.SOLID -> Modifier.clip(shape).background(color)
            IconContainer.RING -> Modifier.border(1.5.dp, color.copy(alpha = 0.8f), shape)
            IconContainer.NONE -> Modifier
            else -> Modifier.clip(shape).background(color.copy(alpha = 0.24f))
        }
        val k = when (style.container) {
            IconContainer.NONE -> 0.9f
            IconContainer.DIAMOND, IconContainer.HEXAGON, IconContainer.GEM, IconContainer.PENTAGON,
            IconContainer.HEART, IconContainer.ARCH, IconContainer.SUNNY, IconContainer.BURST -> 0.5f
            else -> 0.58f
        }
        if (style.anim == IconAnim.SHAPE_SPIN && box != Modifier) {
            Box(Modifier.size(size).graphicsLayer { rotationZ = spin.value }.then(box))
        }
        Box(Modifier.size(size).then(if (style.anim == IconAnim.SHAPE_SPIN) Modifier else box), contentAlignment = Alignment.Center) {
            if (d.tintedIcon != null) BitmapImage(d.tintedIcon, size * k, 0.dp, iconColor, d.vector, iconColor)
            else Icon(d.vector, null, tint = iconColor, modifier = Modifier.size(size * k))
        }
    }
}
