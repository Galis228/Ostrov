package com.pixelisland.service

import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import com.pixelisland.settings.SettingsStore

/** Плитка в быстрых настройках: включить или выключить остров. */
class IslandTileService : TileService() {
    override fun onStartListening() {
        SettingsStore.init(this)
        refresh()
    }

    override fun onClick() {
        SettingsStore.init(this)
        SettingsStore.update { copy(enabled = !enabled) }
        refresh()
    }

    private fun refresh() {
        val t = qsTile ?: return
        val on = SettingsStore.value.enabled
        t.state = if (on) Tile.STATE_ACTIVE else Tile.STATE_INACTIVE
        t.subtitle = if (on) "Вкл" else "Выкл"
        t.updateTile()
    }
}
