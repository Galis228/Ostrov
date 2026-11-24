package com.pixelisland.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import com.pixelisland.ui.i18n.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.pixelisland.settings.Preset
import com.pixelisland.settings.SettingsStore

/** Мастер первого запуска: 5 шагов — знакомство, два разрешения, оформление, подсказки. */
@Composable
fun Onboarding(permissions: @Composable (Int) -> Unit, onDone: () -> Unit) {
    var step by rememberSaveable { mutableIntStateOf(0) }
    val last = 4
    Surface(color = MaterialTheme.colorScheme.surface, modifier = Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding().padding(20.dp)) {
            Row(Modifier.fillMaxWidth().padding(vertical = 8.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                repeat(last + 1) { i ->
                    Box(Modifier.weight(1f).height(4.dp).clip(CircleShape)
                        .background(if (i <= step) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceContainerHighest))
                }
            }
            AnimatedContent(
                step, label = "step", modifier = Modifier.weight(1f),
                transitionSpec = { (slideInHorizontally { it / 3 } + fadeIn()) togetherWith (slideOutHorizontally { -it / 3 } + fadeOut()) },
            ) { st ->
                Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(top = 24.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    when (st) {
                        0 -> {
                            FakeIsland()
                            Title("Остров для Pixel")
                            Body("Живая пилюля вокруг фронтальной камеры: музыка, звонки, навигация, таймеры, зарядка и десятки системных событий — в стиле Material 3.")
                            Body("Настройка займёт минуту.")
                        }
                        1 -> {
                            Title("Рисовать вокруг камеры")
                            Body("Android разрешает рисовать поверх статус-бара только сервисам специальных возможностей. Остров смотрит на положение окон и подпись нажатой кнопки — ничего не сохраняет и никуда не отправляет.")
                            permissions(1)
                        }
                        2 -> {
                            Title("Знать, что происходит")
                            Body("Доступ к уведомлениям нужен для музыки, звонков, навигации, таймеров и Live Updates. Уведомления от самого острова — чтобы сообщить, если система его остановит.")
                            permissions(2)
                        }
                        3 -> {
                            Title("Выберите стиль")
                            Body("Позже всё можно поменять во вкладке «Вид».")
                            Preset.entries.chunked(2).forEach { row ->
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    row.forEach { p ->
                                        Card(
                                            onClick = { SettingsStore.update { p.apply(this) } },
                                            shape = RoundedCornerShape(20.dp), modifier = Modifier.weight(1f),
                                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
                                        ) {
                                            Column(Modifier.padding(vertical = 10.dp)) {
                                                PresetTile(p)
                                                Text(p.title, style = MaterialTheme.typography.titleSmall, modifier = Modifier.padding(start = 12.dp, top = 8.dp))
                                            }
                                        }
                                    }
                                    if (row.size == 1) Spacer(Modifier.weight(1f))
                                }
                            }
                        }
                        else -> {
                            Title("Пара жестов")
                            Tip("Нажатие", "раскрыть событие")
                            Tip("Удержание", "все события списком")
                            Tip("Свайп вниз", "открыть шторку")
                            Tip("Свайп вверх", "спрятать остров на 10 минут")
                            Tip("Двойное нажатие по пустой пилюле", "фонарик")
                            Tip("Перетащить текст на остров", "положить на полку")
                            Body("Жесты, сценарии «если — то» и профили — во вкладке «Функции».")
                        }
                    }
                }
            }
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                if (step > 0) TextButton(onClick = { step-- }) { Text("Назад") } else TextButton(onClick = onDone) { Text("Пропустить") }
                Spacer(Modifier.weight(1f))
                Button(onClick = { if (step == last) onDone() else step++ }) { Text(if (step == last) "Начать" else "Далее") }
            }
        }
    }
}

@Composable private fun Title(t: String) = Text(t, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.SemiBold)
@Composable private fun Body(t: String) = Text(t, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)

@Composable
private fun Tip(gesture: String, what: String) {
    Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp)).background(MaterialTheme.colorScheme.surfaceContainer).padding(14.dp)) {
        Text(gesture, style = MaterialTheme.typography.titleSmall, modifier = Modifier.weight(1f))
        Text(what, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary, textAlign = TextAlign.End, modifier = Modifier.weight(1f))
    }
}

/** Картинка-пилюля для приветствия. */
@Composable
private fun FakeIsland() {
    Box(Modifier.fillMaxWidth().height(120.dp).clip(RoundedCornerShape(28.dp)).background(MaterialTheme.colorScheme.primaryContainer), contentAlignment = Alignment.Center) {
        Box(Modifier.size(220.dp, 48.dp).clip(CircleShape).background(Color.Black), contentAlignment = Alignment.Center) {
            Row(Modifier.fillMaxWidth().padding(horizontal = 14.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(28.dp).clip(RoundedCornerShape(8.dp)).background(MaterialTheme.colorScheme.primary))
                Spacer(Modifier.weight(1f))
                Box(Modifier.size(18.dp).clip(CircleShape).background(Color(0xFF1A1A1A)))
                Spacer(Modifier.weight(1f))
                Text("3:21", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.titleSmall)
            }
        }
    }
}
