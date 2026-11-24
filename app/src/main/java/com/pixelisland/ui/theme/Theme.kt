package com.pixelisland.ui.theme

import android.graphics.Typeface
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialExpressiveTheme
import androidx.compose.material3.MotionScheme
import androidx.compose.material3.Typography
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import com.pixelisland.settings.SettingsStore

/**
 * Шрифт интерфейса Pixel. На Pixel в системе есть семейства google-sans / google-sans-text;
 * на других телефонах Typeface.create вернёт системный шрифт по умолчанию.
 */
private val PixelDisplay = FontFamily(Typeface.create("google-sans", Typeface.NORMAL))
private val PixelText = FontFamily(Typeface.create("google-sans-text", Typeface.NORMAL))

private fun pixelTypography(): Typography {
    val t = Typography()
    fun TextStyle.d() = copy(fontFamily = PixelDisplay)
    fun TextStyle.x() = copy(fontFamily = PixelText)
    return Typography(
        displayLarge = t.displayLarge.d(), displayMedium = t.displayMedium.d(), displaySmall = t.displaySmall.d(),
        headlineLarge = t.headlineLarge.d(), headlineMedium = t.headlineMedium.d(), headlineSmall = t.headlineSmall.d(),
        titleLarge = t.titleLarge.d(), titleMedium = t.titleMedium.d(), titleSmall = t.titleSmall.d(),
        bodyLarge = t.bodyLarge.x(), bodyMedium = t.bodyMedium.x(), bodySmall = t.bodySmall.x(),
        labelLarge = t.labelLarge.x(), labelMedium = t.labelMedium.x(), labelSmall = t.labelSmall.x(),
    )
}

private val PixelType = pixelTypography()

/** Остров: по умолчанию всегда тёмный (сливается с камерой) или следует теме системы. */
@Composable
fun IslandTheme(content: @Composable () -> Unit) {
    val s by SettingsStore.flow.collectAsState()
    val ctx = LocalContext.current
    val dark = !s.followSystemTheme || isSystemInDarkTheme()
    MaterialExpressiveTheme(
        colorScheme = if (dark) dynamicDarkColorScheme(ctx) else dynamicLightColorScheme(ctx),
        typography = PixelType,
        motionScheme = MotionScheme.expressive(),
        content = { com.pixelisland.ui.i18n.ProvideLanguage(content) },
    )
}

@Composable
fun AppTheme(content: @Composable () -> Unit) {
    val ctx = LocalContext.current
    val scheme = if (isSystemInDarkTheme()) dynamicDarkColorScheme(ctx) else dynamicLightColorScheme(ctx)
    MaterialExpressiveTheme(colorScheme = scheme, typography = PixelType, motionScheme = MotionScheme.expressive()) {
        com.pixelisland.ui.i18n.ProvideLanguage(content)
    }
}
