package com.pixelisland.ui

import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.min
import com.pixelisland.core.ChargingActivity
import com.pixelisland.core.DemoActivity
import com.pixelisland.settings.EventStyle
import com.pixelisland.core.LiveActivity
import com.pixelisland.core.MusicActivity
import com.pixelisland.core.OngoingActivity
import com.pixelisland.core.PreviewActivity
import com.pixelisland.core.TorchActivity
import com.pixelisland.settings.AnimStyle
import com.pixelisland.settings.IslandSettings
import kotlin.math.max
import kotlin.math.roundToInt

/** Где на экране камера (в пикселях). */
data class IslandGeometry(
    val centerX: Int,
    val top: Int,
    val cutoutWidth: Int,
    val cutoutHeight: Int,
    val screenWidth: Int,
) {
    companion object {
        val Fallback = IslandGeometry(540, 24, 80, 80, 1080)
    }
}

/** Все размеры острова, посчитанные из геометрии камеры и настроек. */
class IslandDims(val g: IslandGeometry, val s: IslandSettings, val density: Float) {
    private fun px(dp: Dp) = (dp.value * density).roundToInt()
    private fun dp(px: Int) = (px / density).dp

    val cutoutW = dp(g.cutoutWidth)
    val cutoutH = dp(g.cutoutHeight)
    val pad = s.pillPadDp.dp
    val compactH = cutoutH + pad * 2
    val compactW = cutoutW + s.sideWidthDp.dp * 2
    val compactCorner = compactH / 2 * s.roundness
    val screenW = dp(g.screenWidth)
    val expandedW = screenW - s.expandedMarginDp.dp * 2
    val expandedCorner = s.expandedCornerDp.dp
    /** Размер иконки в слоте — от высоты пилюли (ширина подстраивается под содержимое, а не наоборот). */
    val slot: Dp = (compactH - 8.dp).coerceIn(14.dp, 40.dp)
    /** Отступ содержимого от внешнего края и зазор до камеры. */
    val inset: Dp = ((compactH - slot) / 2).coerceAtLeast(4.dp) + 2.dp
    val cameraGap: Dp = 6.dp
    val minW = cutoutW + s.sideWidthDp.dp * 2
    val maxW = expandedW

    /** Ширина пилюли по режиму события; для AUTO — по измеренному содержимому самой широкой стороны. */
    fun widthFor(style: EventStyle, contentSide: Dp): Dp {
        val side = when (style.widthMode) {
            com.pixelisland.settings.WidthMode.AUTO ->
                (contentSide + inset + cameraGap).coerceIn(s.sideWidthDp.dp, maxOf(s.autoMaxSideDp, s.sideWidthDp).dp)
            com.pixelisland.settings.WidthMode.NARROW -> s.sideWidthDp.dp
            com.pixelisland.settings.WidthMode.WIDE -> s.toastSideDp.dp
            com.pixelisland.settings.WidthMode.CUSTOM -> style.sideDp.dp
        }
        return (cutoutW + side * 2).coerceIn(cutoutW, maxW)
    }
    val margin = 18.dp                     // запас окна вокруг острова (под отскок)
    val largeH = 420.dp

    val islandTopPx = g.top - px(pad) + px(s.offsetYDp.dp)
    val centerXPx = g.centerX + px(s.offsetXDp.dp)
    val windowYPx = max(0, islandTopPx - px(margin))
    val contentTopPx = islandTopPx - windowYPx

    val bubbleGap = 6.dp
    /** Окно свёрнутого острова — под фактическую ширину пилюли (её сообщает отрисовка). */
    // Важно: свёрнутое окно всегда уже экрана — по этому отличию отрисовка узнаёт, какое окно сейчас.
    fun compactWindowW(pillW: Dp) =
        px(maxOf(pillW, compactW) + margin * 2 + (if (s.splitPills) (compactH + bubbleGap) * 2 else 0.dp)).coerceAtMost(g.screenWidth - 8)
    val compactWindowH = contentTopPx + px(compactH + margin)
    val largeWindowH = contentTopPx + px(largeH)

    fun expandedHeight(a: LiveActivity): Dp = compactH + when (a) {
        is MusicActivity, is PreviewActivity -> 206.dp
        is OngoingActivity -> when (a.kind) {
            com.pixelisland.core.OngoingKind.DELIVERY -> 110.dp
            com.pixelisland.core.OngoingKind.SCORE -> 100.dp
            com.pixelisland.core.OngoingKind.NAV -> 104.dp
            com.pixelisland.core.OngoingKind.CALL -> if (a.incoming) 136.dp else 170.dp
            else -> 74.dp + (if (a.progress != null) 18.dp else 0.dp) + (if (a.actions.isNotEmpty()) 52.dp else 0.dp)
        }
        is com.pixelisland.core.OwnTimerActivity -> 130.dp
        is TorchActivity -> if (a.maxStrength > 1) 136.dp else 90.dp
        is ChargingActivity -> 90.dp
        is DemoActivity, is com.pixelisland.core.PrivacyActivity, is com.pixelisland.core.AlarmSoonActivity, is com.pixelisland.core.CalendarActivity -> 80.dp
        com.pixelisland.core.QuickActivity -> 340.dp
        com.pixelisland.core.StackActivity -> 260.dp
        is com.pixelisland.core.ShelfActivity -> 120.dp + 54.dp * minOf(a.items.size, 5)
    }

    val peekH = compactH + 84.dp
}

fun density(d: Density) = d.density

fun <T> IslandSettings.motion(): AnimationSpec<T> {
    val k = animSpeed.coerceIn(0.4f, 2.5f)
    if (com.pixelisland.core.IslandRuntime.reduceMotion.value) return snap()
    return when (animStyle) {
        AnimStyle.SPRING -> spring(dampingRatio = 0.7f, stiffness = 800f * k * k)
        AnimStyle.JELLY -> spring(dampingRatio = 0.45f, stiffness = 480f * k * k)
        AnimStyle.SMOOTH -> tween((340 / k).toInt(), easing = CubicBezierEasing(0.2f, 0f, 0f, 1f))
        AnimStyle.SNAPPY -> spring(dampingRatio = 0.92f, stiffness = 2200f * k * k)
        AnimStyle.NONE -> snap()
    }
}

/** Тот же темп, но без перелёта — для скрытия. */
fun <T> IslandSettings.motionNoBounce(): AnimationSpec<T> {
    val k = animSpeed.coerceIn(0.4f, 2.5f)
    if (com.pixelisland.core.IslandRuntime.reduceMotion.value) return snap()
    return when (animStyle) {
        AnimStyle.NONE -> snap()
        AnimStyle.SMOOTH -> tween((300 / k).toInt(), easing = CubicBezierEasing(0.3f, 0f, 0.8f, 0.15f))
        else -> spring(dampingRatio = 1f, stiffness = 900f * k * k)
    }
}

/** Сколько ждать, пока анимация успокоится, прежде чем уменьшить окно. */
fun IslandSettings.settleMs(): Long {
    val base = when (animStyle) {
        AnimStyle.SPRING -> 600; AnimStyle.JELLY -> 1000; AnimStyle.SMOOTH -> 380
        AnimStyle.SNAPPY -> 320; AnimStyle.NONE -> 0
    }
    return (base / animSpeed.coerceIn(0.4f, 2.5f)).toLong() + 60
}

fun IslandSettings.fadeMs() = if (animStyle == AnimStyle.NONE) 0 else (150 / animSpeed.coerceIn(0.4f, 2.5f)).toInt()
