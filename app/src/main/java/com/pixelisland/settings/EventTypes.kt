package com.pixelisland.settings

enum class EventCategory(val title: String) {
    MEDIA("Музыка и активности"),
    BATTERY("Батарея"),
    CONNECT("Связь"),
    SOUND("Звук и экран"),
    PRIVACY("Приватность и время"),
    OTHER("Другое"),
}

enum class EventKind { TOAST, LIVE, CARD }

/** Что лежит в слоте слева или справа от камеры. */
enum class SlotContent(val label: String) {
    NONE("Пусто"),
    ICON("Иконка"),
    ART("Обложка"),
    EQ("Эквалайзер"),
    TITLE("Название"),
    ICON_TITLE("Иконка + текст"),
    VALUE("Значение"),
    RING("Кольцо"),
    VALUE_RING("Значение + кольцо"),
}

enum class IconVariant(val label: String) { ROUNDED("Округлая"), FILLED("Заливка"), OUTLINED("Контур"), SHARP("Острая"), TWOTONE("Двухтон") }

enum class IconContainer(val label: String) {
    TONAL("Круг"), SOLID("Сплошной"), RING("Обводка"), SQUIRCLE("Сквиркл"),
    COOKIE("Печенье"), FLOWER("Цветок"), CLOVER("Клевер"), SUNNY("Солнце"), BURST("Взрыв"),
    GEM("Самоцвет"), PENTAGON("Пятиугольник"), HEXAGON("Соты"), DIAMOND("Ромб"), HEART("Сердце"),
    GHOST("Призрак"), PIXEL("Пиксель"), ARCH("Арка"), PUFFY("Облачко"), DROP("Капля"), NONE("Без фона"),
}

enum class TextWeight(val label: String) { REGULAR("Обычный"), MEDIUM("Средний"), BOLD("Жирный"), BLACK("Чёрный") }

enum class RingStyle(val label: String) { RING("Кольцо"), BAR("Полоска"), FILL("Заливка фона"), DOTS("Точки") }

enum class IconAnim(val label: String) {
    POP("Всплытие"), STATIC("Статика"), PULSE("Пульс"), SHAKE("Покачивание"), SPIN("Оборот"), SLIDE("Выезд"),
    BOUNCE("Прыжок"), BLINK("Мигание"), HEARTBEAT("Сердцебиение"), FLIP("Переворот"), BLUR_IN("Из размытия"),
    MORPH("Перетекание"), SHAPE_SPIN("Вращение формы"),
}

enum class GestureAction(val label: String) {
    NONE("Ничего"),
    EXPAND("Развернуть"),
    OPEN_APP("Открыть приложение"),
    DISMISS("Скрыть"),
    SWITCH("Следующая активность"),
    PLAY_PAUSE("Пауза / играть"),
    NEXT_TRACK("Следующий трек"),
    PREV_TRACK("Предыдущий трек"),
    STACK("Все события списком"),
    SHADE("Открыть шторку"),
    HIDE_10("Скрыть на 10 минут"),
    QUICK("Быстрые действия"),
    SHELF("Полка"),
    TORCH("Фонарик"),
    CAMERA("Камера"),
    SCREENSHOT("Скриншот"),
    ASSISTANT("Ассистент"),
}

/** Жесты, которые имеют смысл для пустой пилюли (без активности). */
val IdleGestures = listOf(
    GestureAction.NONE, GestureAction.QUICK, GestureAction.STACK, GestureAction.SHELF, GestureAction.SHADE, GestureAction.HIDE_10,
    GestureAction.TORCH, GestureAction.CAMERA, GestureAction.SCREENSHOT, GestureAction.ASSISTANT,
)

/** Цвета: 0 — авто (цвет события / обложки), 1 — Monet, 2 — как текст, остальное — ARGB. */
object ColorChoice {
    const val AUTO = 0L
    const val MONET = 1L
    const val MONO = 2L
    val palette = listOf(
        0xFF6EA8FF, 0xFF5CE1E6, 0xFF5EDB8B, 0xFFC6E96B, 0xFFFFD866,
        0xFFFFB25C, 0xFFFF6B6B, 0xFFFF8AD8, 0xFFB89CFF, 0xFFE8E8EC,
    )
}

/** Фон острова: 0 — чёрный, 1/2/3 — тона Monet, остальное — ARGB. */
object BgChoice {
    const val BLACK = 0L
    const val MONET_DARK = 1L
    const val MONET_TONAL = 2L
    const val MONET_LIGHT = 3L
    const val APP_TINT = 4L   // оттенок приложения на экране
    const val WALLPAPER = 5L  // цвета обоев
    val palette = listOf(0xFF15171C, 0xFF1D2433, 0xFF14281F, 0xFF2E1622, 0xFF2A2116, 0xFFF3F3F6)
}

private val TOAST_SLOTS = listOf(
    SlotContent.NONE, SlotContent.ICON, SlotContent.TITLE, SlotContent.ICON_TITLE,
    SlotContent.VALUE, SlotContent.RING, SlotContent.VALUE_RING,
)
private val TEXT_TOAST_SLOTS = TOAST_SLOTS - SlotContent.RING - SlotContent.VALUE_RING

enum class EventType(
    val title: String,
    val hint: String,
    val cat: EventCategory,
    val kind: EventKind,
    val tint: Long,
    val defLeft: SlotContent,
    val defRight: SlotContent,
    val slots: List<SlotContent>,
    val defaultOn: Boolean = true,
) {
    // --- Живые активности ---
    MUSIC("Музыка", "Обложка, эквалайзер, управление", EventCategory.MEDIA, EventKind.LIVE, 0xFF6EA8FF,
        SlotContent.ART, SlotContent.EQ, listOf(SlotContent.NONE, SlotContent.ART, SlotContent.ICON, SlotContent.EQ, SlotContent.TITLE)),
    CALL("Звонок", "Имя, длительность, кнопки", EventCategory.MEDIA, EventKind.LIVE, 0xFF5EDB8B,
        SlotContent.ICON, SlotContent.VALUE, listOf(SlotContent.NONE, SlotContent.ICON, SlotContent.TITLE, SlotContent.VALUE)),
    TIMER("Таймер", "Таймеры и секундомеры", EventCategory.MEDIA, EventKind.LIVE, 0xFFFFB25C,
        SlotContent.ICON, SlotContent.VALUE, listOf(SlotContent.NONE, SlotContent.ICON, SlotContent.TITLE, SlotContent.VALUE)),
    LIVE_UPDATE("Live Updates", "Навигация, доставка, такси", EventCategory.MEDIA, EventKind.LIVE, 0xFF5CE1E6,
        SlotContent.ICON, SlotContent.VALUE, listOf(SlotContent.NONE, SlotContent.ICON, SlotContent.TITLE, SlotContent.VALUE, SlotContent.RING)),
    PROGRESS("Загрузки", "Уведомления с прогрессом", EventCategory.MEDIA, EventKind.LIVE, 0xFFB89CFF,
        SlotContent.ICON, SlotContent.RING, listOf(SlotContent.NONE, SlotContent.ICON, SlotContent.TITLE, SlotContent.VALUE, SlotContent.RING, SlotContent.VALUE_RING)),

    // --- Батарея ---
    CHARGE_START("Зарядка началась", "При подключении питания", EventCategory.BATTERY, EventKind.TOAST, 0xFF5EDB8B,
        SlotContent.ICON_TITLE, SlotContent.VALUE_RING, TOAST_SLOTS),
    CHARGE_STOP("Зарядка остановлена", "При отключении питания", EventCategory.BATTERY, EventKind.TOAST, 0xFFB0B4BC,
        SlotContent.ICON_TITLE, SlotContent.VALUE_RING, TOAST_SLOTS),
    CHARGE_FULL("Заряжено", "Батарея заполнена до 100%", EventCategory.BATTERY, EventKind.TOAST, 0xFF5EDB8B,
        SlotContent.ICON_TITLE, SlotContent.VALUE, TOAST_SLOTS),
    BATTERY_LOW("Низкий заряд", "Системное предупреждение", EventCategory.BATTERY, EventKind.TOAST, 0xFFFF6B6B,
        SlotContent.ICON_TITLE, SlotContent.VALUE_RING, TOAST_SLOTS),
    CHARGING_LIVE("Процент при зарядке", "Постоянно, пока заряжается", EventCategory.BATTERY, EventKind.LIVE, 0xFF5EDB8B,
        SlotContent.ICON, SlotContent.VALUE, TOAST_SLOTS - SlotContent.TITLE - SlotContent.ICON_TITLE, defaultOn = false),
    SAVER_ON("Энергосбережение вкл", "Включён режим экономии", EventCategory.BATTERY, EventKind.TOAST, 0xFFFFD866,
        SlotContent.ICON_TITLE, SlotContent.VALUE, TEXT_TOAST_SLOTS),
    SAVER_OFF("Энергосбережение выкл", "Режим экономии выключен", EventCategory.BATTERY, EventKind.TOAST, 0xFFB0B4BC,
        SlotContent.ICON_TITLE, SlotContent.VALUE, TEXT_TOAST_SLOTS),

    // --- Связь ---
    BT_ON("Bluetooth вкл", "Модуль включён", EventCategory.CONNECT, EventKind.TOAST, 0xFF6EA8FF,
        SlotContent.ICON_TITLE, SlotContent.VALUE, TEXT_TOAST_SLOTS),
    BT_OFF("Bluetooth выкл", "Модуль выключен", EventCategory.CONNECT, EventKind.TOAST, 0xFFB0B4BC,
        SlotContent.ICON_TITLE, SlotContent.VALUE, TEXT_TOAST_SLOTS),
    HP_ON("Наушники подключены", "Имя и заряд, если доступен", EventCategory.CONNECT, EventKind.TOAST, 0xFF6EA8FF,
        SlotContent.ICON_TITLE, SlotContent.VALUE_RING, TOAST_SLOTS),
    HP_OFF("Наушники отключены", "Проводные и беспроводные", EventCategory.CONNECT, EventKind.TOAST, 0xFFB0B4BC,
        SlotContent.ICON_TITLE, SlotContent.VALUE, TEXT_TOAST_SLOTS),
    WIFI_ON("Wi‑Fi подключён", "Подключение к сети", EventCategory.CONNECT, EventKind.TOAST, 0xFF5CE1E6,
        SlotContent.ICON_TITLE, SlotContent.VALUE, TEXT_TOAST_SLOTS, defaultOn = false),
    WIFI_OFF("Wi‑Fi отключён", "Потеря сети", EventCategory.CONNECT, EventKind.TOAST, 0xFFB0B4BC,
        SlotContent.ICON_TITLE, SlotContent.VALUE, TEXT_TOAST_SLOTS, defaultOn = false),
    AIR_ON("Режим полёта вкл", "Все радиомодули выключены", EventCategory.CONNECT, EventKind.TOAST, 0xFFFFB25C,
        SlotContent.ICON_TITLE, SlotContent.VALUE, TEXT_TOAST_SLOTS),
    AIR_OFF("Режим полёта выкл", "Связь снова доступна", EventCategory.CONNECT, EventKind.TOAST, 0xFFB0B4BC,
        SlotContent.ICON_TITLE, SlotContent.VALUE, TEXT_TOAST_SLOTS),
    VPN_ON("VPN подключён", "Защищённое соединение", EventCategory.CONNECT, EventKind.TOAST, 0xFF5EDB8B,
        SlotContent.ICON_TITLE, SlotContent.VALUE, TEXT_TOAST_SLOTS),
    VPN_OFF("VPN отключён", "Соединение без VPN", EventCategory.CONNECT, EventKind.TOAST, 0xFFFF6B6B,
        SlotContent.ICON_TITLE, SlotContent.VALUE, TEXT_TOAST_SLOTS),
    HOTSPOT_ON("Точка доступа вкл", "Раздача интернета", EventCategory.CONNECT, EventKind.TOAST, 0xFF5CE1E6,
        SlotContent.ICON_TITLE, SlotContent.VALUE, TEXT_TOAST_SLOTS),
    HOTSPOT_OFF("Точка доступа выкл", "Раздача выключена", EventCategory.CONNECT, EventKind.TOAST, 0xFFB0B4BC,
        SlotContent.ICON_TITLE, SlotContent.VALUE, TEXT_TOAST_SLOTS),

    // --- Звук ---
    RINGER_SOUND("Звук", "Обычный режим звонка", EventCategory.SOUND, EventKind.TOAST, 0xFF6EA8FF,
        SlotContent.ICON, SlotContent.TITLE, TEXT_TOAST_SLOTS),
    RINGER_VIBRATE("Вибрация", "Режим вибрации", EventCategory.SOUND, EventKind.TOAST, 0xFFFFB25C,
        SlotContent.ICON, SlotContent.TITLE, TEXT_TOAST_SLOTS),
    RINGER_SILENT("Без звука", "Беззвучный режим", EventCategory.SOUND, EventKind.TOAST, 0xFFFF6B6B,
        SlotContent.ICON, SlotContent.TITLE, TEXT_TOAST_SLOTS),
    DND_ON("Не беспокоить вкл", "Режим включён", EventCategory.SOUND, EventKind.TOAST, 0xFFB89CFF,
        SlotContent.ICON_TITLE, SlotContent.VALUE, TEXT_TOAST_SLOTS),
    DND_OFF("Не беспокоить выкл", "Режим выключен", EventCategory.SOUND, EventKind.TOAST, 0xFFB0B4BC,
        SlotContent.ICON_TITLE, SlotContent.VALUE, TEXT_TOAST_SLOTS),
    VOLUME("Громкость", "Уровень при нажатии кнопок", EventCategory.SOUND, EventKind.TOAST, 0xFF6EA8FF,
        SlotContent.ICON, SlotContent.VALUE_RING, TOAST_SLOTS, defaultOn = false),

    // --- Другое ---
    TORCH_ON("Фонарик", "Кнопка выключения и яркость", EventCategory.OTHER, EventKind.LIVE, 0xFFFFD866,
        SlotContent.ICON, SlotContent.VALUE, listOf(SlotContent.NONE, SlotContent.ICON, SlotContent.VALUE)),
    TORCH_OFF("Фонарик выключен", "Короткое уведомление", EventCategory.OTHER, EventKind.TOAST, 0xFFB0B4BC,
        SlotContent.ICON, SlotContent.VALUE, TEXT_TOAST_SLOTS),
    UNLOCK("Разблокировка", "После разблокировки устройства", EventCategory.OTHER, EventKind.TOAST, 0xFF5EDB8B,
        SlotContent.ICON, SlotContent.NONE, TEXT_TOAST_SLOTS, defaultOn = false),
    // --- Приватность и время ---
    RECORDING("Запись", "Запись экрана и диктофон", EventCategory.PRIVACY, EventKind.LIVE, 0xFFFF5252,
        SlotContent.ICON, SlotContent.VALUE, listOf(SlotContent.NONE, SlotContent.ICON, SlotContent.TITLE, SlotContent.VALUE)),
    MIC_IN_USE("Микрофон используется", "Точка, пока идёт запись звука", EventCategory.PRIVACY, EventKind.LIVE, 0xFFFFA14A,
        SlotContent.NONE, SlotContent.ICON, listOf(SlotContent.NONE, SlotContent.ICON, SlotContent.TITLE), defaultOn = false),
    CAMERA_IN_USE("Камера используется", "Точка, пока камера занята", EventCategory.PRIVACY, EventKind.LIVE, 0xFF5EDB8B,
        SlotContent.NONE, SlotContent.ICON, listOf(SlotContent.NONE, SlotContent.ICON, SlotContent.TITLE)),
    ALARM_SOON("Скоро будильник", "Отсчёт за 30 минут до будильника", EventCategory.PRIVACY, EventKind.LIVE, 0xFFB89CFF,
        SlotContent.ICON, SlotContent.VALUE, listOf(SlotContent.NONE, SlotContent.ICON, SlotContent.TITLE, SlotContent.VALUE), defaultOn = false),
    ALARM_RING("Будильник звонит", "Кнопки «Отложить» и «Выключить»", EventCategory.PRIVACY, EventKind.LIVE, 0xFFFFD866,
        SlotContent.ICON, SlotContent.TITLE, listOf(SlotContent.NONE, SlotContent.ICON, SlotContent.TITLE, SlotContent.VALUE)),

    CALENDAR("Встреча скоро", "Отсчёт за 15 минут до события календаря", EventCategory.PRIVACY, EventKind.LIVE, 0xFF5CE1E6,
        SlotContent.ICON, SlotContent.VALUE, listOf(SlotContent.NONE, SlotContent.ICON, SlotContent.TITLE, SlotContent.VALUE), defaultOn = false),
    WEATHER("Погода утром", "Температура после первой разблокировки утром", EventCategory.OTHER, EventKind.TOAST, 0xFFFFD866,
        SlotContent.ICON_TITLE, SlotContent.VALUE, TEXT_TOAST_SLOTS, defaultOn = false),
    SCREENSHOT("Скриншот", "Мини-превью снимка экрана", EventCategory.OTHER, EventKind.CARD, 0xFF5CE1E6,
        SlotContent.NONE, SlotContent.NONE, emptyList(), defaultOn = false),

    OWN_TIMER("Свой таймер", "Таймер, секундомер и помодоро прямо из острова", EventCategory.PRIVACY, EventKind.LIVE, 0xFFFFB25C,
        SlotContent.ICON, SlotContent.VALUE, listOf(SlotContent.NONE, SlotContent.ICON, SlotContent.TITLE, SlotContent.VALUE, SlotContent.RING)),
    DELIVERY("Доставка и такси", "Прогресс пути курьера или машины", EventCategory.MEDIA, EventKind.LIVE, 0xFF5EDB8B,
        SlotContent.ICON, SlotContent.VALUE, listOf(SlotContent.NONE, SlotContent.ICON, SlotContent.TITLE, SlotContent.VALUE, SlotContent.RING)),
    SCORE("Счёт матча", "Live Update со счётом игры", EventCategory.MEDIA, EventKind.LIVE, 0xFF6EA8FF,
        SlotContent.ICON, SlotContent.VALUE, listOf(SlotContent.NONE, SlotContent.ICON, SlotContent.TITLE, SlotContent.VALUE)),
    PAYMENT("Платежи", "Сумма из уведомлений банков и магазинов", EventCategory.OTHER, EventKind.TOAST, 0xFF5EDB8B,
        SlotContent.ICON_TITLE, SlotContent.VALUE, TEXT_TOAST_SLOTS, defaultOn = false),

    NAVIGATION("Навигация", "Google Maps: поворот, расстояние, прибытие", EventCategory.MEDIA, EventKind.LIVE, 0xFF5EDB8B,
        SlotContent.ICON, SlotContent.VALUE, listOf(SlotContent.NONE, SlotContent.ICON, SlotContent.TITLE, SlotContent.VALUE, SlotContent.RING)),
    VOICE("Голосовые", "Голосовые из Telegram, WhatsApp, Viber", EventCategory.MEDIA, EventKind.LIVE, 0xFF5CE1E6,
        SlotContent.ICON, SlotContent.EQ, listOf(SlotContent.NONE, SlotContent.ART, SlotContent.ICON, SlotContent.EQ, SlotContent.TITLE)),
    NOW_PLAYING("Сейчас играет", "Pixel распознал песню вокруг", EventCategory.MEDIA, EventKind.TOAST, 0xFFB89CFF,
        SlotContent.ICON_TITLE, SlotContent.VALUE, TEXT_TOAST_SLOTS),
    TRANSFER("Передача файлов", "Quick Share и обмен поблизости", EventCategory.CONNECT, EventKind.LIVE, 0xFF6EA8FF,
        SlotContent.ICON, SlotContent.RING, listOf(SlotContent.NONE, SlotContent.ICON, SlotContent.TITLE, SlotContent.VALUE, SlotContent.RING, SlotContent.VALUE_RING)),
    NET_LOST("Нет сети", "Интернет пропал совсем", EventCategory.CONNECT, EventKind.TOAST, 0xFFFF6B6B,
        SlotContent.ICON_TITLE, SlotContent.VALUE, TEXT_TOAST_SLOTS),
    NET_BACK("Сеть вернулась", "Интернет снова доступен", EventCategory.CONNECT, EventKind.TOAST, 0xFF5EDB8B,
        SlotContent.ICON_TITLE, SlotContent.VALUE, TEXT_TOAST_SLOTS),
    FOCUS("Сон и фокус", "Режим сна и фокуса Digital Wellbeing", EventCategory.PRIVACY, EventKind.LIVE, 0xFFB89CFF,
        SlotContent.ICON, SlotContent.TITLE, listOf(SlotContent.NONE, SlotContent.ICON, SlotContent.TITLE)),
    BRIGHTNESS("Яркость", "Шкала при ручной смене яркости", EventCategory.SOUND, EventKind.TOAST, 0xFFFFD866,
        SlotContent.ICON, SlotContent.VALUE_RING, TOAST_SLOTS, defaultOn = false),
    ROTATE("Поворот экрана", "Кнопка автоповорота, когда телефон боком", EventCategory.SOUND, EventKind.CARD, 0xFF5CE1E6,
        SlotContent.NONE, SlotContent.NONE, emptyList(), defaultOn = false),
    COPIED("Скопировано", "Когда нажимаете «Копировать»", EventCategory.OTHER, EventKind.TOAST, 0xFF6EA8FF,
        SlotContent.ICON_TITLE, SlotContent.VALUE, TEXT_TOAST_SLOTS),
    WEATHER_ALERT("Погода: предупреждения", "Дождь скоро, сильная жара или мороз", EventCategory.OTHER, EventKind.TOAST, 0xFF5CE1E6,
        SlotContent.ICON_TITLE, SlotContent.VALUE, TEXT_TOAST_SLOTS, defaultOn = false),
    SHELF("Полка", "Файлы и ссылки, брошенные на остров", EventCategory.OTHER, EventKind.LIVE, 0xFFFFB25C,
        SlotContent.ICON, SlotContent.VALUE, listOf(SlotContent.NONE, SlotContent.ICON, SlotContent.TITLE, SlotContent.VALUE)),

    NOTIF_PEEK("Уведомления приложений", "Краткий показ входящих уведомлений", EventCategory.OTHER, EventKind.CARD, 0xFF6EA8FF,
        SlotContent.NONE, SlotContent.NONE, emptyList(), defaultOn = false),
    ;

    fun default() = EventStyle(enabled = defaultOn, left = defLeft, right = defRight)
}

data class EventStyle(
    val enabled: Boolean = true,
    val left: SlotContent = SlotContent.ICON,
    val right: SlotContent = SlotContent.VALUE,
    val variant: IconVariant = IconVariant.ROUNDED,
    val container: IconContainer = IconContainer.TONAL,
    val anim: IconAnim = IconAnim.POP,
    val color: Long = ColorChoice.AUTO,
    val widthMode: WidthMode = WidthMode.AUTO,
    val sideDp: Float = 60f,      // своя ширина «уха» для режима CUSTOM
    val align: SlotAlign = SlotAlign.EDGES,
    // --- Раскрытый вид ---
    val exLayout: ExLayout = ExLayout.STANDARD,
    val exHeader: Boolean = true,
    val exIcon: ExIconSize = ExIconSize.MEDIUM,
    val exProgress: ExProgress = ExProgress.WAVE,
    val exButtons: ExButtons = ExButtons.TONAL,
    val exBigValue: Boolean = true,
    val exBg: ExBg = ExBg.SAME,
    val exBgColor: Long = 0xFF1D2433,
    val exOpacity: Float = 1f,
    val exWidth: ExWidth = ExWidth.FULL,
    val exCorner: Float = 0f,       // 0 — как в общих
    val exCollapse: Float = 0f,     // 0 — как в общих
    val exTap: ExTap = ExTap.OPEN,
    val durationSec: Float = 0f,   // 0 — как в общих настройках
    val icon: String = "",         // своя иконка из каталога, "" — стандартная
    val weight: TextWeight = TextWeight.MEDIUM,
    val monoDigits: Boolean = true,
    val marquee: Boolean = true,
    val ring: RingStyle = RingStyle.RING,
    val ringThick: Float = 3f,
)

enum class IdleContent(val label: String) {
    EMPTY("Пусто"), STEPS("Шаги"), BATTERY("Заряд"), HEADSET("Наушники"), TIME("Время"),
    MISSED("Пропущенные"), RATE("Курс"), NET_SPEED("Скорость сети"),
}

/** Курс для пустой пилюли. */
enum class RateKind(val label: String, val short: String) {
    USD_UAH("Доллар → гривна", "$"), EUR_UAH("Евро → гривна", "€"), USD_EUR("Доллар → евро", "$/€"),
    PLN_UAH("Злотый → гривна", "zł"), BTC_USD("Биткоин", "₿"), ETH_USD("Эфир", "Ξ"),
}

/** Живой фон пилюли. Анимируется, только пока остров раскрыт или сразу после смены содержимого. */
enum class PillFx(val label: String) { NONE("Нет"), PARTICLES("Частицы"), WAVE("Волна"), AURORA("Сияние"), BEAT("Пульс музыки") }

/** Как показывать «Вкл/Выкл», «Подключено/Отключено» в пилюле. */
enum class StatusStyle(val label: String) { TEXT("Словами"), CHECK("Галочка"), CHECK_CIRCLE("Галочка в круге") }

private val ON_WORDS = setOf("вкл", "включено", "включён", "включен", "подключено", "подключены", "подключён", "подключен", "on", "connected")
private val OFF_WORDS = setOf("выкл", "выключено", "выключен", "отключено", "отключены", "отключён", "отключен", "off", "disconnected")

/** true — «включено», false — «выключено», null — это не слово-статус. */
fun statusOf(value: String): Boolean? = value.trim().lowercase().let { if (it in ON_WORDS) true else if (it in OFF_WORDS) false else null }

enum class AppLanguage(val label: String) { AUTO("Как в системе"), RU("Русский"), EN("English") }

/** Как считать ширину пилюли события. */
enum class WidthMode(val label: String) { AUTO("По содержимому"), NARROW("Узкая"), WIDE("Широкая"), CUSTOM("Своя") }

/** Где стоит содержимое в «ухе» пилюли. */
enum class SlotAlign(val label: String) { EDGES("К краям"), CAMERA("К камере"), CENTER("По центру") }

enum class ExLayout(val label: String) { STANDARD("Стандарт"), LARGE("Крупный"), MINIMAL("Минимал"), PLAYER("Плеер") }
enum class ExIconSize(val label: String, val dp: Int) { SMALL("Маленькая", 40), MEDIUM("Средняя", 56), LARGE("Большая", 80) }
enum class ExProgress(val label: String) { WAVE("Волна"), LINE("Линия"), RING("Кольцо"), NONE("Скрыть") }
enum class ExButtons(val label: String) { FILLED("Залитые"), TONAL("Тональные"), ICONS("Только иконки"), NONE("Скрыть") }
enum class ExBg(val label: String) { SAME("Как пилюля"), ART_BLUR("Размытая обложка"), TINT("Оттенок"), CUSTOM("Свой цвет") }
enum class ExWidth(val label: String) { FULL("Во весь экран"), MEDIUM("Средняя"), CONTENT("Компактная") }
enum class ExTap(val label: String) { OPEN("Открыть приложение"), COLLAPSE("Свернуть"), NOTHING("Ничего") }
