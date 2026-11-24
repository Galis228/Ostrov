package com.pixelisland.core

import android.content.Context
import android.media.AudioManager
import android.os.SystemClock
import android.view.KeyEvent
import com.pixelisland.settings.EventType
import kotlinx.coroutines.flow.MutableStateFlow
import org.json.JSONArray
import org.json.JSONObject

enum class AutoAction(val label: String, val needsArg: Boolean = false) {
    OPEN_APP("Открыть приложение", true),
    MEDIA_PLAY("Включить музыку"),
    MEDIA_PAUSE("Поставить на паузу"),
    TORCH_ON("Включить фонарик"),
    TORCH_OFF("Выключить фонарик"),
    TIMER("Запустить таймер", true),
    STOPWATCH("Запустить секундомер"),
    RINGER_VIBRATE("Режим вибрации"),
    RINGER_NORMAL("Включить звук"),
    MEDIA_VOLUME("Громкость медиа", true),
    WEATHER("Показать погоду"),
    HIDE_ISLAND("Скрыть остров на 10 минут"),
}

/** «Если — то»: событие острова запускает действие. */
data class Automation(
    val id: Long,
    val trigger: EventType,
    val action: AutoAction,
    val arg: String = "",       // пакет приложения, минуты таймера, громкость в %
    val argLabel: String = "",  // для показа: имя приложения
    val enabled: Boolean = true,
)

object Automations {
    val rules = MutableStateFlow<List<Automation>>(emptyList())
    private var ctx: Context? = null
    private val last = HashMap<Long, Long>()

    /** Какие события годятся как триггер (короткие события и начало живых). */
    val triggers: List<EventType> = listOf(
        EventType.HP_ON, EventType.HP_OFF, EventType.CHARGE_START, EventType.CHARGE_STOP, EventType.CHARGE_FULL,
        EventType.BATTERY_LOW, EventType.BT_ON, EventType.BT_OFF, EventType.WIFI_ON, EventType.WIFI_OFF,
        EventType.VPN_ON, EventType.VPN_OFF, EventType.AIR_ON, EventType.AIR_OFF, EventType.NET_LOST, EventType.NET_BACK,
        EventType.DND_ON, EventType.DND_OFF, EventType.RINGER_SILENT, EventType.RINGER_SOUND, EventType.UNLOCK,
        EventType.SAVER_ON, EventType.SAVER_OFF, EventType.CALL, EventType.NAVIGATION, EventType.ALARM_RING, EventType.FOCUS,
        EventType.SCREENSHOT, EventType.CALENDAR, EventType.HOTSPOT_ON,
    )

    fun init(c: Context) {
        if (ctx != null) return
        ctx = c.applicationContext
        rules.value = runCatching { decode(ctx!!.getSharedPreferences("auto", Context.MODE_PRIVATE).getString("r", "[]")!!) }.getOrDefault(emptyList())
    }

    fun save(list: List<Automation>) {
        rules.value = list
        ctx?.getSharedPreferences("auto", Context.MODE_PRIVATE)?.edit()?.putString("r", encode(list))?.apply()
    }

    fun add(a: Automation) = save(rules.value + a)
    fun remove(id: Long) = save(rules.value.filter { it.id != id })
    fun toggle(id: Long) = save(rules.value.map { if (it.id == id) it.copy(enabled = !it.enabled) else it })

    fun encode(list: List<Automation>): String = JSONArray().apply {
        list.forEach { put(JSONObject().put("id", it.id).put("t", it.trigger.name).put("a", it.action.name).put("arg", it.arg).put("al", it.argLabel).put("on", it.enabled)) }
    }.toString()

    fun decode(text: String): List<Automation> {
        val a = JSONArray(text)
        return (0 until a.length()).mapNotNull { i ->
            val o = a.getJSONObject(i)
            val t = EventType.entries.firstOrNull { it.name == o.optString("t") } ?: return@mapNotNull null
            val act = AutoAction.entries.firstOrNull { it.name == o.optString("a") } ?: return@mapNotNull null
            Automation(o.optLong("id"), t, act, o.optString("arg"), o.optString("al"), o.optBoolean("on", true))
        }
    }

    /** Вызывается при каждом событии острова. */
    fun fire(t: EventType) {
        val c = ctx ?: return
        val now = SystemClock.elapsedRealtime()
        rules.value.filter { it.enabled && it.trigger == t }.forEach { r ->
            if (now - (last[r.id] ?: 0L) < 3000) return@forEach   // не чаще раза в 3 секунды
            last[r.id] = now
            runCatching { run(c, r) }
        }
    }

    fun run(c: Context, r: Automation) {
        val am = c.getSystemService(AudioManager::class.java)
        when (r.action) {
            AutoAction.OPEN_APP -> c.launchPackage(r.arg)
            AutoAction.MEDIA_PLAY -> mediaKey(am, KeyEvent.KEYCODE_MEDIA_PLAY)
            AutoAction.MEDIA_PAUSE -> mediaKey(am, KeyEvent.KEYCODE_MEDIA_PAUSE)
            AutoAction.TORCH_ON -> com.pixelisland.sources.SystemEvents.setTorch(c, true)
            AutoAction.TORCH_OFF -> com.pixelisland.sources.SystemEvents.setTorch(c, false)
            AutoAction.TIMER -> OwnTimers.startTimer(r.arg.toIntOrNull() ?: 5)
            AutoAction.STOPWATCH -> OwnTimers.startStopwatch()
            AutoAction.RINGER_VIBRATE -> am.ringerMode = AudioManager.RINGER_MODE_VIBRATE
            AutoAction.RINGER_NORMAL -> am.ringerMode = AudioManager.RINGER_MODE_NORMAL
            AutoAction.MEDIA_VOLUME -> {
                val max = am.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
                am.setStreamVolume(AudioManager.STREAM_MUSIC, (max * (r.arg.toIntOrNull() ?: 50) / 100).coerceIn(0, max), AudioManager.FLAG_SHOW_UI)
            }
            AutoAction.WEATHER -> com.pixelisland.sources.Extras.showWeather()
            AutoAction.HIDE_ISLAND -> IslandController.snooze(10)
        }
    }

    private fun mediaKey(am: AudioManager, code: Int) {
        am.dispatchMediaKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, code))
        am.dispatchMediaKeyEvent(KeyEvent(KeyEvent.ACTION_UP, code))
    }

    fun describe(r: Automation): String = buildString {
        append(r.action.label)
        when (r.action) {
            AutoAction.OPEN_APP -> append(": ").append(r.argLabel.ifBlank { r.arg })
            AutoAction.TIMER -> append(": ").append(r.arg).append(" мин")
            AutoAction.MEDIA_VOLUME -> append(": ").append(r.arg).append("%")
            else -> Unit
        }
    }
}
