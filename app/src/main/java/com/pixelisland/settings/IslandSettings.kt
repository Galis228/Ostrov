package com.pixelisland.settings

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.json.JSONObject

/** Правило для приложения: показывать ли, свой цвет (0 — нет) и своя иконка ("" — нет). */
data class AppRule(val show: Boolean = true, val color: Long = 0L, val icon: String = "")

enum class AnimStyle(val label: String, val hint: String) {
    SPRING("Пружина", "Живая, с лёгким отскоком"),
    JELLY("Желе", "Сильный отскок, упругая"),
    SMOOTH("Плавная", "Material Emphasized, без отскока"),
    SNAPPY("Резкая", "Очень быстрая, почти без отскока"),
    NONE("Без анимации", "Мгновенно"),
}

enum class GradientStyle(val label: String) { NONE("Нет"), ACCENT("С акцентом"), CUSTOM("Свой цвет"), DIAGONAL("Диагональ"), ANIMATED("Переливы") }

enum class AppearStyle(val label: String) {
    FROM_CAMERA("Из камеры"),
    POP("Всплытие"),
    FADE("Растворение"),
    DROP("Сверху"),
}

data class IslandSettings(
    val enabled: Boolean = true,
    // Вырез камеры
    val cutoutManual: Boolean = false,
    val cutX: Int = 0, val cutY: Int = 0, val cutW: Int = 0, val cutH: Int = 0, // px, центр по X
    // Пилюля
    val pillPadDp: Float = 4f,
    val sideWidthDp: Float = 26f,      // минимальная ширина «уха» (и «Узкая»)
    val roundness: Float = 1f,
    val contentScale: Float = 1f,
    val offsetXDp: Float = 0f,
    val offsetYDp: Float = 0f,
    val toastSideDp: Float = 110f,
    val autoMaxSideDp: Float = 150f,   // предел авто-ширины с каждой стороны
    // Развёрнутый
    val expandedMarginDp: Float = 10f,
    val expandedCornerDp: Float = 36f,
    // Фон и эффекты
    val bg: Long = BgChoice.BLACK,
    val bgOpacity: Float = 1f,
    val outline: Boolean = false,
    val shadow: Boolean = false,
    val glow: Boolean = false,
    val blurBehind: Boolean = false,
    val gradient: GradientStyle = GradientStyle.NONE,
    val followSystemTheme: Boolean = false,
    val bgCustom2: Long = 0xFF6EA8FF,
    // Взаимодействие
    val splitPills: Boolean = true,
    val quickActions: Boolean = true,
    val tapPausesTimers: Boolean = true,
    val mediaGestures: Boolean = true,
    val sounds: Boolean = false,
    val appRules: Map<String, AppRule> = emptyMap(),
    val peekBlocked: Set<String> = emptySet(),
    val idleContent: IdleContent = IdleContent.EMPTY,
    val cameraBlend: Boolean = false,
    // Ночь
    val nightEnabled: Boolean = false,
    val nightFrom: Int = 23,
    val nightTo: Int = 7,
    val nightQuiet: Boolean = true,
    val nightDim: Boolean = true,
    // Анимации
    val animStyle: AnimStyle = AnimStyle.SPRING,
    val appearStyle: AppearStyle = AppearStyle.FROM_CAMERA,
    val animSpeed: Float = 1f,
    // Поведение
    val haptics: Boolean = true,
    val autoCollapseSec: Float = 6f,
    val toastSec: Float = 2.5f,
    val showIdlePill: Boolean = false,
    val hideLandscape: Boolean = true,
    val hideFullscreen: Boolean = true,
    val hideShade: Boolean = true,
    val showOnLockscreen: Boolean = true,
    // Жесты (компактный вид)
    val gTap: GestureAction = GestureAction.EXPAND,
    val gDoubleTap: GestureAction = GestureAction.NONE,
    val gLongPress: GestureAction = GestureAction.STACK,
    val gSwipeDown: GestureAction = GestureAction.SHADE,
    val gSwipeUp: GestureAction = GestureAction.HIDE_10,
    val gSwipeSide: GestureAction = GestureAction.SWITCH,
    // Жесты пустой пилюли
    val gIdleTap: GestureAction = GestureAction.QUICK,
    val gIdleDouble: GestureAction = GestureAction.TORCH,
    val gIdleLong: GestureAction = GestureAction.SHELF,
    val gIdleSwipeDown: GestureAction = GestureAction.SHADE,
    val gIdleSwipeUp: GestureAction = GestureAction.HIDE_10,
    // Новое в 0.9
    val pillFx: PillFx = PillFx.NONE,
    val notifyGlow: Boolean = false,
    val glowAlways: Boolean = false,
    val rateKind: RateKind = RateKind.USD_UAH,
    val language: AppLanguage = AppLanguage.AUTO,
    val lowBattery20: Boolean = true,
    val statusStyle: StatusStyle = StatusStyle.TEXT,
    val ver: Int = 9,
    // События
    val events: Map<EventType, EventStyle> = emptyMap(),
) {
    fun style(t: EventType): EventStyle = events[t] ?: t.default()
    fun rule(pkg: String?): AppRule? = pkg?.let { appRules[it] }
    fun appShown(pkg: String?) = pkg == null || (rule(pkg)?.show != false && pkg !in peekBlocked)
    fun on(t: EventType) = style(t).enabled
    fun withStyle(t: EventType, s: EventStyle) = copy(events = events + (t to s))

    /** 0.9: новые жесты по умолчанию — удержание показывает стопку, свайп вниз открывает шторку, вверх прячет. */
    fun migrate(): IslandSettings = if (ver >= 9) this else copy(
        ver = 9,
        gLongPress = if (gLongPress == GestureAction.OPEN_APP) GestureAction.STACK else gLongPress,
        gSwipeDown = if (gSwipeDown == GestureAction.EXPAND) GestureAction.SHADE else gSwipeDown,
        gSwipeUp = if (gSwipeUp == GestureAction.DISMISS) GestureAction.HIDE_10 else gSwipeUp,
    )
}

/** SharedPreferences + StateFlow, общее для приложения и сервисов (один процесс). */
object SettingsStore {
    private const val PREFS = "island_settings_v3"
    private var prefs: SharedPreferences? = null
    private val _flow = MutableStateFlow(IslandSettings())
    val flow: StateFlow<IslandSettings> = _flow
    val value get() = _flow.value

    fun init(context: Context) {
        if (prefs != null) return
        val p = context.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        prefs = p
        _flow.value = runCatching { decode(JSONObject(p.getString("json", "{}")!!)) }.getOrDefault(IslandSettings())
    }

    private val saver = android.os.Handler(android.os.Looper.getMainLooper())
    private val saveTask = Runnable { prefs?.edit()?.putString("json", encode(_flow.value).toString())?.apply() }

    fun update(block: IslandSettings.() -> IslandSettings) {
        val new = _flow.value.block()
        if (new == _flow.value) return
        _flow.value = new
        // При перетаскивании ползунка обновлений десятки в секунду — пишем на диск не чаще раза в 400 мс.
        saver.removeCallbacks(saveTask)
        saver.postDelayed(saveTask, 400)
    }

    fun updateStyle(t: EventType, block: EventStyle.() -> EventStyle) = update { withStyle(t, style(t).block()) }
    fun reset() = update { IslandSettings() }

    fun exportJson(): String = encode(value).toString()
    fun parseJson(text: String): IslandSettings = decode(JSONObject(text))
    fun importJson(text: String): Boolean = runCatching {
        val s = decode(JSONObject(text))
        update { s }
    }.isSuccess

    // ----- JSON -----
    private fun encode(s: IslandSettings) = JSONObject().apply {
        put("enabled", s.enabled)
        put("cutoutManual", s.cutoutManual); put("cutX", s.cutX); put("cutY", s.cutY); put("cutW", s.cutW); put("cutH", s.cutH)
        put("pillPadDp", s.pillPadDp); put("sideWidthDp", s.sideWidthDp); put("roundness", s.roundness)
        put("contentScale", s.contentScale); put("offsetXDp", s.offsetXDp); put("offsetYDp", s.offsetYDp)
        put("toastSideDp", s.toastSideDp); put("autoMaxSideDp", s.autoMaxSideDp); put("expandedMarginDp", s.expandedMarginDp); put("expandedCornerDp", s.expandedCornerDp)
        put("bg", s.bg); put("bgOpacity", s.bgOpacity); put("outline", s.outline); put("shadow", s.shadow)
        put("glow", s.glow); put("blurBehind", s.blurBehind)
        put("gradient", s.gradient.name); put("bgCustom2", s.bgCustom2); put("followSystemTheme", s.followSystemTheme)
        put("splitPills", s.splitPills); put("quickActions", s.quickActions); put("tapPausesTimers", s.tapPausesTimers); put("mediaGestures", s.mediaGestures); put("sounds", s.sounds)
        put("appRules", JSONObject().apply {
            s.appRules.forEach { (pkg, r) -> put(pkg, JSONObject().put("show", r.show).put("color", r.color).put("icon", r.icon)) }
        })
        put("peekBlocked", org.json.JSONArray(s.peekBlocked.toList()))
        put("idleContent", s.idleContent.name); put("cameraBlend", s.cameraBlend)
        put("nightEnabled", s.nightEnabled); put("nightFrom", s.nightFrom); put("nightTo", s.nightTo)
        put("nightQuiet", s.nightQuiet); put("nightDim", s.nightDim)
        put("animStyle", s.animStyle.name); put("appearStyle", s.appearStyle.name); put("animSpeed", s.animSpeed)
        put("haptics", s.haptics); put("autoCollapseSec", s.autoCollapseSec); put("toastSec", s.toastSec)
        put("showIdlePill", s.showIdlePill); put("hideLandscape", s.hideLandscape); put("hideFullscreen", s.hideFullscreen)
        put("hideShade", s.hideShade); put("showOnLockscreen", s.showOnLockscreen)
        put("gTap", s.gTap.name); put("gDoubleTap", s.gDoubleTap.name); put("gLongPress", s.gLongPress.name)
        put("gSwipeDown", s.gSwipeDown.name); put("gSwipeUp", s.gSwipeUp.name); put("gSwipeSide", s.gSwipeSide.name)
        put("gIdleTap", s.gIdleTap.name); put("gIdleDouble", s.gIdleDouble.name); put("gIdleLong", s.gIdleLong.name)
        put("gIdleSwipeDown", s.gIdleSwipeDown.name); put("gIdleSwipeUp", s.gIdleSwipeUp.name)
        put("pillFx", s.pillFx.name); put("notifyGlow", s.notifyGlow); put("glowAlways", s.glowAlways)
        put("rateKind", s.rateKind.name); put("language", s.language.name); put("lowBattery20", s.lowBattery20); put("statusStyle", s.statusStyle.name); put("ver", s.ver)
        put("events", JSONObject().apply {
            s.events.forEach { (t, e) ->
                put(t.name, JSONObject().apply {
                    put("en", e.enabled); put("l", e.left.name); put("r", e.right.name); put("v", e.variant.name)
                    put("c", e.container.name); put("a", e.anim.name); put("col", e.color); put("wm", e.widthMode.name); put("sd", e.sideDp); put("al", e.align.name);
                    put("xl", e.exLayout.name); put("xh", e.exHeader); put("xi", e.exIcon.name); put("xp", e.exProgress.name)
                    put("xb", e.exButtons.name); put("xv", e.exBigValue); put("xbg", e.exBg.name); put("xbc", e.exBgColor)
                    put("xo", e.exOpacity); put("xw", e.exWidth.name); put("xc", e.exCorner); put("xa", e.exCollapse); put("xt", e.exTap.name); put("d", e.durationSec)
                    put("i", e.icon); put("tw", e.weight.name); put("mono", e.monoDigits); put("mq", e.marquee)
                    put("rs", e.ring.name); put("rt", e.ringThick)
                })
            }
        })
    }

    private inline fun <reified E : Enum<E>> JSONObject.enum(k: String, def: E): E =
        optString(k, def.name).let { n -> enumValues<E>().firstOrNull { it.name == n } ?: def }

    private fun JSONObject.f(k: String, def: Float) = optDouble(k, def.toDouble()).toFloat()

    private fun decode(j: JSONObject): IslandSettings {
        val d = IslandSettings()
        val ev = mutableMapOf<EventType, EventStyle>()
        j.optJSONObject("events")?.let { o ->
            o.keys().forEach { k ->
                val t = EventType.entries.firstOrNull { it.name == k } ?: return@forEach
                val e = o.getJSONObject(k)
                val def = t.default()
                ev[t] = EventStyle(
                    enabled = e.optBoolean("en", def.enabled),
                    left = e.enum("l", def.left), right = e.enum("r", def.right),
                    variant = e.enum("v", def.variant), container = e.enum("c", def.container),
                    anim = e.enum("a", def.anim), color = e.optLong("col", def.color),
                    widthMode = e.enum("wm", def.widthMode), sideDp = e.f("sd", def.sideDp), align = e.enum("al", def.align),
                    exLayout = e.enum("xl", def.exLayout), exHeader = e.optBoolean("xh", def.exHeader), exIcon = e.enum("xi", def.exIcon),
                    exProgress = e.enum("xp", def.exProgress), exButtons = e.enum("xb", def.exButtons), exBigValue = e.optBoolean("xv", def.exBigValue),
                    exBg = e.enum("xbg", def.exBg), exBgColor = e.optLong("xbc", def.exBgColor), exOpacity = e.f("xo", def.exOpacity),
                    exWidth = e.enum("xw", def.exWidth), exCorner = e.f("xc", def.exCorner), exCollapse = e.f("xa", def.exCollapse),
                    exTap = e.enum("xt", def.exTap), durationSec = e.f("d", def.durationSec),
                    icon = e.optString("i", ""), weight = e.enum("tw", def.weight),
                    monoDigits = e.optBoolean("mono", def.monoDigits), marquee = e.optBoolean("mq", def.marquee),
                    ring = e.enum("rs", def.ring), ringThick = e.f("rt", def.ringThick),
                )
            }
        }
        return IslandSettings(
            enabled = j.optBoolean("enabled", d.enabled),
            cutoutManual = j.optBoolean("cutoutManual", d.cutoutManual),
            cutX = j.optInt("cutX"), cutY = j.optInt("cutY"), cutW = j.optInt("cutW"), cutH = j.optInt("cutH"),
            pillPadDp = j.f("pillPadDp", d.pillPadDp), sideWidthDp = j.f("sideWidthDp", d.sideWidthDp),
            roundness = j.f("roundness", d.roundness), contentScale = j.f("contentScale", d.contentScale),
            offsetXDp = j.f("offsetXDp", d.offsetXDp), offsetYDp = j.f("offsetYDp", d.offsetYDp),
            toastSideDp = j.f("toastSideDp", d.toastSideDp), autoMaxSideDp = j.f("autoMaxSideDp", d.autoMaxSideDp),
            expandedMarginDp = j.f("expandedMarginDp", d.expandedMarginDp), expandedCornerDp = j.f("expandedCornerDp", d.expandedCornerDp),
            bg = j.optLong("bg", d.bg), bgOpacity = j.f("bgOpacity", d.bgOpacity),
            outline = j.optBoolean("outline", d.outline), shadow = j.optBoolean("shadow", d.shadow),
            glow = j.optBoolean("glow", d.glow), blurBehind = j.optBoolean("blurBehind", d.blurBehind),
            gradient = j.enum("gradient", d.gradient), bgCustom2 = j.optLong("bgCustom2", d.bgCustom2),
            followSystemTheme = j.optBoolean("followSystemTheme", d.followSystemTheme),
            splitPills = j.optBoolean("splitPills", d.splitPills), quickActions = j.optBoolean("quickActions", d.quickActions),
            tapPausesTimers = j.optBoolean("tapPausesTimers", d.tapPausesTimers),
            mediaGestures = j.optBoolean("mediaGestures", d.mediaGestures), sounds = j.optBoolean("sounds", d.sounds),
            appRules = j.optJSONObject("appRules")?.let { o ->
                o.keys().asSequence().associateWith { k ->
                    val r = o.getJSONObject(k)
                    AppRule(r.optBoolean("show", true), r.optLong("color", 0L), r.optString("icon", ""))
                }
            } ?: emptyMap(),
            idleContent = j.enum("idleContent", d.idleContent), cameraBlend = j.optBoolean("cameraBlend", d.cameraBlend),
            nightEnabled = j.optBoolean("nightEnabled", d.nightEnabled), nightFrom = j.optInt("nightFrom", d.nightFrom),
            nightTo = j.optInt("nightTo", d.nightTo), nightQuiet = j.optBoolean("nightQuiet", d.nightQuiet),
            nightDim = j.optBoolean("nightDim", d.nightDim),
            peekBlocked = j.optJSONArray("peekBlocked")?.let { a -> (0 until a.length()).map { a.getString(it) }.toSet() } ?: emptySet(),
            animStyle = j.enum("animStyle", d.animStyle), appearStyle = j.enum("appearStyle", d.appearStyle),
            animSpeed = j.f("animSpeed", d.animSpeed),
            haptics = j.optBoolean("haptics", d.haptics), autoCollapseSec = j.f("autoCollapseSec", d.autoCollapseSec),
            toastSec = j.f("toastSec", d.toastSec), showIdlePill = j.optBoolean("showIdlePill", d.showIdlePill),
            hideLandscape = j.optBoolean("hideLandscape", d.hideLandscape), hideFullscreen = j.optBoolean("hideFullscreen", d.hideFullscreen),
            hideShade = j.optBoolean("hideShade", d.hideShade), showOnLockscreen = j.optBoolean("showOnLockscreen", d.showOnLockscreen),
            gTap = j.enum("gTap", d.gTap), gDoubleTap = j.enum("gDoubleTap", d.gDoubleTap),
            gLongPress = j.enum("gLongPress", d.gLongPress), gSwipeDown = j.enum("gSwipeDown", d.gSwipeDown),
            gSwipeUp = j.enum("gSwipeUp", d.gSwipeUp), gSwipeSide = j.enum("gSwipeSide", d.gSwipeSide),
            gIdleTap = j.enum("gIdleTap", d.gIdleTap), gIdleDouble = j.enum("gIdleDouble", d.gIdleDouble),
            gIdleLong = j.enum("gIdleLong", d.gIdleLong), gIdleSwipeDown = j.enum("gIdleSwipeDown", d.gIdleSwipeDown),
            gIdleSwipeUp = j.enum("gIdleSwipeUp", d.gIdleSwipeUp),
            pillFx = j.enum("pillFx", d.pillFx), notifyGlow = j.optBoolean("notifyGlow", d.notifyGlow), glowAlways = j.optBoolean("glowAlways", d.glowAlways),
            rateKind = j.enum("rateKind", d.rateKind), language = j.enum("language", d.language),
            lowBattery20 = j.optBoolean("lowBattery20", d.lowBattery20),
            statusStyle = j.enum("statusStyle", d.statusStyle),
            ver = j.optInt("ver", 0),
            events = ev,
        ).migrate()
    }
}
