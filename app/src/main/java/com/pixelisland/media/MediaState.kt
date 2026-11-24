package com.pixelisland.media

import android.graphics.Bitmap

/** Снимок текущего медиа, который рисует остров. */
data class MediaState(
    val key: String,                 // пакет + трек, для отслеживания смены трека
    val packageName: String,
    val appLabel: String,
    val title: String,
    val artist: String,
    val art: Bitmap?,
    val accent: Int?,                // цвет из обложки (Palette), null → берём из Monet
    val isPlaying: Boolean,
    val durationMs: Long,
    val positionMs: Long,
    val positionUpdateTime: Long,    // SystemClock.elapsedRealtime() момента снимка позиции
    val speed: Float,
    val canSeek: Boolean,
) {
    fun positionAt(now: Long): Long {
        val p = if (!isPlaying || positionUpdateTime <= 0) positionMs
        else positionMs + ((now - positionUpdateTime) * speed).toLong()
        return if (durationMs > 0) p.coerceIn(0, durationMs) else p.coerceAtLeast(0)
    }
}

/** Действия, которые остров может выполнить. */
interface IslandActions {
    fun playPause()
    fun next()
    fun previous()
    fun seekTo(ms: Long)
    fun openApp()
    fun dismiss()
}
