package com.pixelisland.core

import android.content.Context
import com.pixelisland.settings.EventType
import kotlinx.coroutines.flow.MutableStateFlow
import org.json.JSONObject
import java.util.Calendar

/** Сколько раз какие события показывались — по дням, последние 7 дней. */
object Stats {
    /** день (yyyyDDD) → тип → количество */
    val days = MutableStateFlow<Map<Int, Map<EventType, Int>>>(emptyMap())
    private var ctx: Context? = null
    private val saver = android.os.Handler(android.os.Looper.getMainLooper())
    private val save = Runnable {
        val o = JSONObject()
        days.value.forEach { (d, m) -> o.put(d.toString(), JSONObject().apply { m.forEach { (t, n) -> put(t.name, n) } }) }
        ctx?.getSharedPreferences("stats", Context.MODE_PRIVATE)?.edit()?.putString("d", o.toString())?.apply()
    }

    fun today(): Int = Calendar.getInstance().let { it.get(Calendar.YEAR) * 1000 + it.get(Calendar.DAY_OF_YEAR) }

    /** Ключи последних [n] дней, от старого к новому. */
    fun lastDays(n: Int = 7): List<Int> = (n - 1 downTo 0).map { back ->
        Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -back) }.let { it.get(Calendar.YEAR) * 1000 + it.get(Calendar.DAY_OF_YEAR) }
    }

    fun init(c: Context) {
        if (ctx != null) return
        ctx = c.applicationContext
        runCatching {
            val o = JSONObject(ctx!!.getSharedPreferences("stats", Context.MODE_PRIVATE).getString("d", "{}")!!)
            days.value = o.keys().asSequence().associate { d ->
                val m = o.getJSONObject(d)
                d.toInt() to m.keys().asSequence().mapNotNull { k -> EventType.entries.firstOrNull { it.name == k }?.let { it to m.getInt(k) } }.toMap()
            }
        }
    }

    fun hit(t: EventType) {
        val d = today()
        val keep = lastDays(7).toSet()
        val cur = days.value.filterKeys { it in keep }.toMutableMap()
        val m = (cur[d] ?: emptyMap()).toMutableMap()
        m[t] = (m[t] ?: 0) + 1
        cur[d] = m
        days.value = cur
        saver.removeCallbacks(save); saver.postDelayed(save, 5000)
    }

    fun clear() { days.value = emptyMap(); saver.post(save) }
}
