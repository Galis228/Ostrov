package com.pixelisland.core

import android.content.Context
import android.media.RingtoneManager
import android.os.Handler
import android.os.Looper
import com.pixelisland.settings.EventType

/** Свои таймеры острова: обычный, секундомер, помодоро (25/5). */
object OwnTimers {
    private val main = Handler(Looper.getMainLooper())
    private var ctx: Context? = null
    private var cur: OwnTimerActivity? = null
    private val done = Runnable { onFinished() }

    fun init(c: Context) { ctx = c.applicationContext }

    private fun now() = System.currentTimeMillis()

    private fun publish(a: OwnTimerActivity?) {
        cur = a
        main.removeCallbacks(done)
        if (a == null) { IslandController.remove("own_timer"); return }
        IslandController.set(a)
        if (a.running && a.mode != TimerMode.STOPWATCH) main.postDelayed(done, (a.anchor - now()).coerceAtLeast(0))
    }

    fun startTimer(minutes: Int) {
        val ms = minutes * 60_000L
        publish(OwnTimerActivity(TimerMode.TIMER, true, now() + ms, 0, ms))
        EventLog.add("Свой таймер", "Таймер на $minutes мин")
    }

    fun startStopwatch() = publish(OwnTimerActivity(TimerMode.STOPWATCH, true, now(), 0, 0))

    fun startPomodoro() {
        val ms = 25 * 60_000L
        publish(OwnTimerActivity(TimerMode.POMODORO, true, now() + ms, 0, ms, work = true, round = 1))
        EventLog.add("Свой таймер", "Помодоро: работа 25 мин")
    }

    fun togglePause() {
        val a = cur ?: return
        val next = if (a.running) {
            val frozen = if (a.mode == TimerMode.STOPWATCH) now() - a.anchor else a.anchor - now()
            a.copy(running = false, frozenMs = frozen.coerceAtLeast(0))
        } else {
            val anchor = if (a.mode == TimerMode.STOPWATCH) now() - a.frozenMs else now() + a.frozenMs
            a.copy(running = true, anchor = anchor)
        }
        publish(next)
    }

    fun addMinute() {
        val a = cur ?: return
        if (a.mode == TimerMode.STOPWATCH) return
        publish(if (a.running) a.copy(anchor = a.anchor + 60_000, totalMs = a.totalMs + 60_000)
        else a.copy(frozenMs = a.frozenMs + 60_000, totalMs = a.totalMs + 60_000))
    }

    fun stop() = publish(null)

    private fun onFinished() {
        val a = cur ?: return
        ring()
        if (a.mode == TimerMode.POMODORO) {
            val work = !a.work
            val ms = (if (work) 25 else 5) * 60_000L
            val round = if (work) a.round + 1 else a.round
            IslandController.showToast(ToastEvent("own_timer_done", EventType.OWN_TIMER, if (work) "За работу!" else "Перерыв", if (work) "25 мин" else "5 мин"), force = true)
            publish(OwnTimerActivity(TimerMode.POMODORO, true, now() + ms, 0, ms, work, round))
        } else {
            IslandController.showToast(ToastEvent("own_timer_done", EventType.OWN_TIMER, "Время вышло", ""), force = true)
            EventLog.add("Свой таймер", "Время вышло")
            publish(null)
        }
    }

    private fun ring() {
        val c = ctx ?: return
        runCatching {
            val uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            RingtoneManager.getRingtone(c, uri)?.play()
        }
        runCatching {
            c.getSystemService(android.os.VibratorManager::class.java)?.defaultVibrator
                ?.vibrate(android.os.VibrationEffect.createWaveform(longArrayOf(0, 120, 80, 120, 80, 240), -1))
        }
    }
}
