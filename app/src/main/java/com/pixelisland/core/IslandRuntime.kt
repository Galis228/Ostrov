package com.pixelisland.core

import com.pixelisland.ui.IslandGeometry
import kotlinx.coroutines.flow.MutableStateFlow

/** Живые данные от сервиса, которые нужны экрану настроек. */
object IslandRuntime {
    /** Что вернула система для выреза камеры. */
    val autoGeometry = MutableStateFlow<IslandGeometry?>(null)
    /** Что реально используется (с учётом ручной калибровки). */
    val geometry = MutableStateFlow<IslandGeometry?>(null)
    /** В системе выключены анимации. */
    val reduceMotion = MutableStateFlow(false)
    /** Включён повышенный контраст (Android 14+). */
    val highContrast = MutableStateFlow(false)
    /** Сейчас «ночь» по расписанию. */
    val night = MutableStateFlow(false)
    /** Цвет приложения на экране (для фона «как приложение»). */
    val appColor = MutableStateFlow<Int?>(null)
    /** Шаги за сегодня, заряд телефона, наушников — для пустой пилюли. */
    val steps = MutableStateFlow<Int?>(null)
    val battery = MutableStateFlow(0)
    val headsetBattery = MutableStateFlow<Int?>(null)
    /** Пропущенные звонки и непрочитанные сообщения (из уведомлений). */
    val missed = MutableStateFlow(0 to 0)
    /** Курс для пустой пилюли, уже отформатированный. */
    val rate = MutableStateFlow<String?>(null)
    /** Цвета обоев (основной, второй, третий) — для палитры «Как обои». */
    val wallColors = MutableStateFlow<Triple<Int, Int, Int>?>(null)
    /** Вспышка свечения вокруг камеры: цвет + метка времени. */
    val glow = MutableStateFlow<Pair<Int, Long>?>(null)
    /** Телефон заблокирован (экран блокировки). */
    val locked = MutableStateFlow(false)
    /** Фактическая ширина свёрнутой пилюли (dp) — сервис подгоняет под неё окно. */
    val compactWidthDp = MutableStateFlow(0f)
    // --- Диагностика ---
    val serviceAlive = MutableStateFlow(false)
    val listenerAlive = MutableStateFlow(false)
    val windowLarge = MutableStateFlow(false)
    val showBounds = MutableStateFlow(false)
    /** Счётчик кадров, отрисованных оверлеем (для расчёта частоты перерисовки). */
    @Volatile var frames = 0L
}
