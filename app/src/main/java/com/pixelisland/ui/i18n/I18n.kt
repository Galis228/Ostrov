package com.pixelisland.ui.i18n

import android.content.Context
import androidx.compose.material3.LocalTextStyle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit
import com.pixelisland.settings.AppLanguage
import com.pixelisland.settings.SettingsStore
import java.util.Locale

/**
 * Перевод интерфейса. Исходные строки — русские; английский словарь лежит в assets/en.tsv.
 * Строки с числами внутри переводятся по шаблонам, остальные остаются как есть.
 */
object I18n {
    private var map: Map<String, String> = emptyMap()
    private var loaded = false

    fun init(c: Context) {
        if (loaded) return
        loaded = true
        map = runCatching {
            c.applicationContext.assets.open("en.tsv").bufferedReader().readLines().mapNotNull { line ->
                val i = line.indexOf('\t'); if (i <= 0) null else line.substring(0, i) to line.substring(i + 1)
            }.toMap()
        }.getOrDefault(emptyMap())
    }

    fun english(lang: AppLanguage): Boolean = when (lang) {
        AppLanguage.RU -> false
        AppLanguage.EN -> true
        AppLanguage.AUTO -> Locale.getDefault().language !in setOf("ru", "uk", "be", "kk")
    }

    /** Сейчас английский? (для мест вне Compose: тосты, уведомления). */
    val en get() = english(SettingsStore.value.language)

    fun tr(s: String): String {
        if (s.isEmpty() || !hasCyrillic(s)) return s
        map[s]?.let { return it }
        val t = s.trim()
        map[t]?.let { return s.replace(t, it) }
        return patterns(s)
    }

    private fun hasCyrillic(s: String) = s.any { it in 'Ѐ'..'ӿ' }

    private val rules: List<Pair<Regex, String>> = listOf(
        Regex("Событий включено: (\\d+) из (\\d+)") to "Events enabled: $1 of $2",
        Regex("Включено (\\d+) из (\\d+) · удержание — вкл/выкл") to "Enabled $1 of $2 · long press — on/off",
        Regex("Дождь через (\\d+) мин") to "Rain in $1 min",
        Regex("Заряд (\\d+)%") to "Battery $1%",
        Regex("событий · в среднем (\\d+) в день") to "events · $1 per day on average",
        Regex("С (\\d+):00 до (\\d+):00") to "From $1:00 to $2:00",
        Regex("Профиль (\\d+)") to "Profile $1",
        Regex("Вручную: (.+)") to "Manual: $1",
        Regex("(\\d+) ч (\\d+) мин") to "$1 h $2 min",
        Regex("(\\d+) мин") to "$1 min",
        Regex("(\\d+[.,]?\\d*) с\\b") to "$1 s",
        Regex("(\\d+[.,]?\\d*) МБ/с") to "$1 MB/s",
        Regex("(\\d+) КБ/с") to "$1 KB/s",
        Regex("(\\d+) Б/с") to "$1 B/s",
        Regex("^До 100% · ") to "To 100% · ",
        Regex("^До полной ≈ ") to "Until full ≈ ",
        Regex("^Голосовое · ") to "Voice message · ",
        Regex("^Всем: ") to "All: ",
        Regex(" · активен$") to " · active",
    )

    private fun patterns(s: String): String {
        var r = s
        for ((re, rep) in rules) r = re.replace(r, rep)
        // Простые слова-кусочки из словаря (например, «Зарядка · 1 ч 10 мин»).
        if (hasCyrillic(r)) r = r.split(" · ").joinToString(" · ") { part -> map[part.trim()] ?: part }
        return r
    }
}

/** Английский ли интерфейс сейчас — меняется сразу при выборе языка. */
val LocalEnglish = staticCompositionLocalOf { false }

@Composable
fun ProvideLanguage(content: @Composable () -> Unit) {
    val s by SettingsStore.flow.collectAsState()
    CompositionLocalProvider(LocalEnglish provides I18n.english(s.language), content = content)
}

/** Text из Material 3, но с переводом строки. */
@Composable
fun Text(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = Color.Unspecified,
    fontSize: TextUnit = TextUnit.Unspecified,
    fontStyle: FontStyle? = null,
    fontWeight: FontWeight? = null,
    fontFamily: FontFamily? = null,
    letterSpacing: TextUnit = TextUnit.Unspecified,
    textDecoration: TextDecoration? = null,
    textAlign: TextAlign? = null,
    lineHeight: TextUnit = TextUnit.Unspecified,
    overflow: TextOverflow = TextOverflow.Clip,
    softWrap: Boolean = true,
    maxLines: Int = Int.MAX_VALUE,
    minLines: Int = 1,
    onTextLayout: ((TextLayoutResult) -> Unit)? = null,
    style: TextStyle = LocalTextStyle.current,
) {
    val t = if (LocalEnglish.current) I18n.tr(text) else text
    androidx.compose.material3.Text(
        text = t, modifier = modifier, color = color, fontSize = fontSize, fontStyle = fontStyle, fontWeight = fontWeight,
        fontFamily = fontFamily, letterSpacing = letterSpacing, textDecoration = textDecoration, textAlign = textAlign,
        lineHeight = lineHeight, overflow = overflow, softWrap = softWrap, maxLines = maxLines, minLines = minLines,
        onTextLayout = onTextLayout, style = style,
    )
}

/** Перевод вне Compose-текста (тосты, подписи). */
fun tr(s: String) = if (I18n.en) I18n.tr(s) else s
