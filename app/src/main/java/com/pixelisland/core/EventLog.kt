package com.pixelisland.core

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import org.json.JSONArray
import org.json.JSONObject

/** Журнал всего, что показывал остров (последние 150 записей, переживает перезапуск). */
object EventLog {
    data class Entry(val time: Long, val kind: String, val title: String, val text: String)

    private const val MAX = 150
    val entries = MutableStateFlow<List<Entry>>(emptyList())
    private var ctx: Context? = null
    private val saver = android.os.Handler(android.os.Looper.getMainLooper())
    private val save = Runnable {
        val arr = JSONArray()
        entries.value.forEach { e ->
            arr.put(JSONObject().put("t", e.time).put("k", e.kind).put("ti", e.title).put("tx", e.text))
        }
        ctx?.getSharedPreferences("log", Context.MODE_PRIVATE)?.edit()?.putString("e", arr.toString())?.apply()
    }

    fun init(c: Context) {
        if (ctx != null) return
        ctx = c.applicationContext
        runCatching {
            val arr = JSONArray(ctx!!.getSharedPreferences("log", Context.MODE_PRIVATE).getString("e", "[]"))
            entries.value = (0 until arr.length()).map {
                val o = arr.getJSONObject(it)
                Entry(o.getLong("t"), o.getString("k"), o.getString("ti"), o.getString("tx"))
            }
        }
    }

    fun add(kind: String, title: String, text: String = "") {
        val list = entries.value
        // Не дублируем одно и то же подряд (например, обновления таймера).
        if (list.firstOrNull()?.let { it.kind == kind && it.title == title && System.currentTimeMillis() - it.time < 5000 } == true) return
        entries.value = (listOf(Entry(System.currentTimeMillis(), kind, title, text)) + list).take(MAX)
        saver.removeCallbacks(save); saver.postDelayed(save, 2000)
    }

    fun clear() { entries.value = emptyList(); saver.post(save) }
}
