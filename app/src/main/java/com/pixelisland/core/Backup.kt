package com.pixelisland.core

import com.pixelisland.settings.SettingsStore
import org.json.JSONObject

/** Полная резервная копия: настройки + профили + сценарии. Понимает и старый формат (только настройки). */
object Backup {
    fun export(): String = JSONObject()
        .put("format", "ostrov-backup-1")
        .put("settings", JSONObject(SettingsStore.exportJson()))
        .put("profiles", org.json.JSONArray(Profiles.encode(Profiles.list.value)))
        .put("automations", org.json.JSONArray(Automations.encode(Automations.rules.value)))
        .toString(2)

    fun import(text: String): Boolean = runCatching {
        val j = JSONObject(text)
        if (j.optString("format") == "ostrov-backup-1") {
            SettingsStore.importJson(j.getJSONObject("settings").toString())
            j.optJSONArray("profiles")?.let { Profiles.replaceAll(Profiles.decode(it.toString())) }
            j.optJSONArray("automations")?.let { Automations.save(Automations.decode(it.toString())) }
            true
        } else SettingsStore.importJson(text)
    }.getOrDefault(false)
}
