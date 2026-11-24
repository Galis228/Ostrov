package com.pixelisland.core

import android.content.Context
import com.pixelisland.settings.SettingsStore
import kotlinx.coroutines.flow.MutableStateFlow
import org.json.JSONArray
import org.json.JSONObject
import java.util.Calendar

/** Профиль: снимок всех настроек + необязательное расписание (часы «с» и «до»). */
data class Profile(
    val id: Long,
    val name: String,
    val json: String,
    val scheduled: Boolean = false,
    val from: Int = 9,
    val to: Int = 18,
)

object Profiles {
    val list = MutableStateFlow<List<Profile>>(emptyList())
    /** Какой профиль применён последним (id), 0 — никакой. */
    val active = MutableStateFlow(0L)
    private var ctx: Context? = null
    private var lastAuto = 0L

    fun init(c: Context) {
        if (ctx != null) return
        ctx = c.applicationContext
        val p = prefs()
        list.value = runCatching { decode(p.getString("l", "[]")!!) }.getOrDefault(emptyList())
        active.value = p.getLong("active", 0L)
    }

    private fun prefs() = ctx!!.getSharedPreferences("profiles", Context.MODE_PRIVATE)

    private fun persist() {
        ctx ?: return
        prefs().edit().putString("l", encode(list.value)).putLong("active", active.value).apply()
    }

    fun saveCurrent(name: String) {
        val p = Profile(System.currentTimeMillis(), name.ifBlank { "Профиль ${list.value.size + 1}" }, SettingsStore.exportJson())
        list.value = list.value + p
        active.value = p.id
        persist()
    }

    /** Перезаписать профиль текущими настройками. */
    fun overwrite(id: Long) {
        list.value = list.value.map { if (it.id == id) it.copy(json = SettingsStore.exportJson()) else it }
        persist()
    }

    fun replaceAll(l: List<Profile>) { list.value = l; persist() }
    fun update(p: Profile) { list.value = list.value.map { if (it.id == p.id) p else it }; persist() }
    fun delete(id: Long) { list.value = list.value.filter { it.id != id }; if (active.value == id) active.value = 0; persist() }

    /** Применить: калибровку выреза и «включён ли остров» оставляем текущими. */
    fun apply(p: Profile) {
        val cur = SettingsStore.value
        val s = runCatching { SettingsStore.parseJson(p.json) }.getOrNull() ?: return
        SettingsStore.update {
            s.copy(enabled = cur.enabled, cutoutManual = cur.cutoutManual, cutX = cur.cutX, cutY = cur.cutY, cutW = cur.cutW, cutH = cur.cutH)
        }
        active.value = p.id
        persist()
    }

    /** Раз в 30 с из сервиса: включить профиль по расписанию, когда начинается его время. */
    fun tick() {
        ctx ?: return
        val h = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        val due = list.value.firstOrNull { it.scheduled && inRange(h, it.from, it.to) }
        if (due == null) { lastAuto = 0L; return }   // окно закончилось — завтра профиль снова включится сам
        if (due.id == active.value || due.id == lastAuto) return
        lastAuto = due.id
        apply(due)
        IslandController.showToast(ToastEvent("profile", com.pixelisland.settings.EventType.UNLOCK, "Профиль", due.name, iconName = "Layers"), force = true)
    }

    private fun inRange(h: Int, from: Int, to: Int) = if (from <= to) h in from until to else h >= from || h < to

    fun encode(l: List<Profile>): String = JSONArray().apply {
        l.forEach { put(JSONObject().put("id", it.id).put("n", it.name).put("j", it.json).put("s", it.scheduled).put("f", it.from).put("t", it.to)) }
    }.toString()

    fun decode(t: String): List<Profile> {
        val a = JSONArray(t)
        return (0 until a.length()).map { i ->
            val o = a.getJSONObject(i)
            Profile(o.getLong("id"), o.getString("n"), o.getString("j"), o.optBoolean("s"), o.optInt("f", 9), o.optInt("t", 18))
        }
    }
}
