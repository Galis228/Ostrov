package com.pixelisland.service

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.pixelisland.R
import com.pixelisland.core.OwnTimers
import com.pixelisland.settings.SettingsStore
import com.pixelisland.sources.SystemEvents

/** Виджет на главный экран: вкл/выкл острова, таймер 5 мин, секундомер, фонарик. */
class IslandWidget : AppWidgetProvider() {
    override fun onUpdate(c: Context, m: AppWidgetManager, ids: IntArray) = refresh(c)

    override fun onReceive(c: Context, i: Intent) {
        super.onReceive(c, i)
        SettingsStore.init(c)
        OwnTimers.init(c)
        when (i.action) {
            ACT_TOGGLE -> SettingsStore.update { copy(enabled = !enabled) }
            ACT_TIMER -> OwnTimers.startTimer(5)
            ACT_STOPWATCH -> OwnTimers.startStopwatch()
            ACT_TORCH -> {
                val p = c.getSharedPreferences("widget", Context.MODE_PRIVATE)
                val on = !p.getBoolean("torch", false)
                p.edit().putBoolean("torch", on).apply()
                SystemEvents.setTorch(c, on)
            }
            else -> return
        }
        refresh(c)
    }

    companion object {
        private const val ACT_TOGGLE = "com.pixelisland.widget.TOGGLE"
        private const val ACT_TIMER = "com.pixelisland.widget.TIMER"
        private const val ACT_STOPWATCH = "com.pixelisland.widget.STOPWATCH"
        private const val ACT_TORCH = "com.pixelisland.widget.TORCH"

        fun refresh(c: Context) {
            SettingsStore.init(c)
            val m = AppWidgetManager.getInstance(c)
            val ids = m.getAppWidgetIds(ComponentName(c, IslandWidget::class.java))
            if (ids.isEmpty()) return
            val on = SettingsStore.value.enabled
            val v = RemoteViews(c.packageName, R.layout.widget_island)
            v.setTextViewText(R.id.w_toggle_label, if (on) "Включён" else "Выключен")
            v.setInt(R.id.w_toggle, "setBackgroundResource", if (on) R.drawable.widget_btn_on else R.drawable.widget_btn)
            v.setOnClickPendingIntent(R.id.w_toggle, pi(c, ACT_TOGGLE, 1))
            v.setOnClickPendingIntent(R.id.w_timer, pi(c, ACT_TIMER, 2))
            v.setOnClickPendingIntent(R.id.w_stopwatch, pi(c, ACT_STOPWATCH, 3))
            v.setOnClickPendingIntent(R.id.w_torch, pi(c, ACT_TORCH, 4))
            m.updateAppWidget(ids, v)
        }

        private fun pi(c: Context, action: String, code: Int) = PendingIntent.getBroadcast(
            c, code, Intent(c, IslandWidget::class.java).setAction(action), PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )
    }
}
