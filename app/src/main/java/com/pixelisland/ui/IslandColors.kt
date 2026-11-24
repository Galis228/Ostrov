package com.pixelisland.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import com.pixelisland.settings.GradientStyle
import androidx.compose.ui.graphics.luminance
import com.pixelisland.settings.BgChoice
import com.pixelisland.settings.ColorChoice
import com.pixelisland.settings.IslandSettings

data class IslandColors(val bg: Color, val fg: Color, val dim: Color, val accent: Color, val brush: Brush? = null) {
    /** Цвет события по выбору пользователя. */
    fun resolve(choice: Long, auto: Color): Color = when (choice) {
        ColorChoice.AUTO -> auto
        ColorChoice.MONET -> accent
        ColorChoice.MONO -> fg
        else -> Color(choice)
    }
}

val LocalIslandColors = staticCompositionLocalOf { IslandColors(Color.Black, Color.White, Color(0x99FFFFFF), Color(0xFFADC6FF)) }

@Composable
fun islandColors(s: IslandSettings): IslandColors {
    val cs = MaterialTheme.colorScheme // тёмная динамическая схема
    val lightTheme = s.followSystemTheme && !androidx.compose.foundation.isSystemInDarkTheme()
    val base = when (s.bg) {
        BgChoice.BLACK -> if (lightTheme) cs.surfaceContainerHighest else Color.Black
        BgChoice.MONET_DARK -> cs.surfaceContainerHighest
        BgChoice.MONET_TONAL -> cs.primaryContainer
        BgChoice.MONET_LIGHT -> cs.inverseSurface
        BgChoice.APP_TINT -> {
            val app by com.pixelisland.core.IslandRuntime.appColor.collectAsState()
            app?.let { androidx.compose.ui.graphics.lerp(Color.Black, Color(it), 0.38f) } ?: Color.Black
        }
        BgChoice.WALLPAPER -> {
            val w by com.pixelisland.core.IslandRuntime.wallColors.collectAsState()
            w?.let { androidx.compose.ui.graphics.lerp(Color.Black, Color(it.first), 0.42f) } ?: cs.surfaceContainerHighest
        }
        else -> Color(s.bg)
    }
    val light = base.luminance() > 0.5f
    val fg = if (light) Color(0xFF15161A) else Color.White
    val wall = if (s.bg == BgChoice.WALLPAPER) com.pixelisland.core.IslandRuntime.wallColors.collectAsState().value else null
    val accent = wall?.let { androidx.compose.ui.graphics.lerp(Color(it.third), Color.White, 0.35f) } ?: if (light) cs.inversePrimary else cs.primary
    val a = s.bgOpacity.coerceIn(0.2f, 1f)
    val bg = base.copy(alpha = a)
    val brush = when (s.gradient) {
        GradientStyle.NONE -> null
        GradientStyle.ACCENT -> Brush.horizontalGradient(listOf(bg, accent.copy(alpha = 0.45f * a), bg))
        GradientStyle.CUSTOM -> Brush.horizontalGradient(listOf(bg, Color(s.bgCustom2).copy(alpha = 0.7f * a)))
        GradientStyle.DIAGONAL -> Brush.linearGradient(listOf(Color(s.bgCustom2).copy(alpha = 0.55f * a), bg, accent.copy(alpha = 0.45f * a)))
        GradientStyle.ANIMATED -> null // рисуется в IslandRoot с анимацией
    }
    val hc by com.pixelisland.core.IslandRuntime.highContrast.collectAsState()
    val night by com.pixelisland.core.IslandRuntime.night.collectAsState()
    val k = if (night && s.nightDim) 0.7f else 1f   // ночью приглушаем яркость содержимого
    return IslandColors(bg, fg.copy(alpha = k), fg.copy(alpha = (if (hc) 0.85f else 0.62f) * k), accent.copy(alpha = k), brush)
}

fun bgPreview(choice: Long, cs: androidx.compose.material3.ColorScheme): Color = when (choice) {
    BgChoice.BLACK -> Color.Black
    BgChoice.MONET_DARK -> cs.surfaceContainerHighest
    BgChoice.MONET_TONAL -> cs.primaryContainer
    BgChoice.MONET_LIGHT -> cs.inverseSurface
    BgChoice.APP_TINT -> Color(0xFF3B2F5C)
    BgChoice.WALLPAPER -> com.pixelisland.core.IslandRuntime.wallColors.value?.let { androidx.compose.ui.graphics.lerp(Color.Black, Color(it.first), 0.42f) } ?: Color(0xFF2B3A2E)
    else -> Color(choice)
}
