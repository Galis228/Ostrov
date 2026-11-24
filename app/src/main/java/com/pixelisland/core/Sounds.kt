package com.pixelisland.core

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import android.os.Handler
import android.os.Looper
import com.pixelisland.settings.SettingsStore
import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.sin

/** Тихие синтезированные щелчки при раскрытии и сворачивании (без звуковых файлов). */
object Sounds {
    enum class Kind { EXPAND, COLLAPSE }

    private const val RATE = 44_100
    private val main = Handler(Looper.getMainLooper())
    private val cache = HashMap<Kind, ShortArray>()

    private fun chirp(f0: Double, f1: Double, ms: Int): ShortArray {
        val n = RATE * ms / 1000
        var phase = 0.0
        return ShortArray(n) { i ->
            val t = i.toDouble() / n
            val f = f0 + (f1 - f0) * t
            phase += 2 * PI * f / RATE
            val env = (1 - exp(-i / 60.0)) * exp(-t * 4.5)   // мягкая атака и затухание
            (sin(phase) * env * 0.22 * Short.MAX_VALUE).toInt().toShort()
        }
    }

    fun play(ctx: Context, kind: Kind) {
        val s = SettingsStore.value
        if (!s.sounds) return
        if (IslandRuntime.night.value && s.nightEnabled && s.nightQuiet) return
        val am = ctx.getSystemService(AudioManager::class.java) ?: return
        if (am.ringerMode != AudioManager.RINGER_MODE_NORMAL) return
        val pcm = cache.getOrPut(kind) {
            if (kind == Kind.EXPAND) chirp(880.0, 1480.0, 55) else chirp(1400.0, 820.0, 50)
        }
        runCatching {
            val track = AudioTrack.Builder()
                .setAudioAttributes(AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION).build())
                .setAudioFormat(AudioFormat.Builder().setEncoding(AudioFormat.ENCODING_PCM_16BIT).setSampleRate(RATE)
                    .setChannelMask(AudioFormat.CHANNEL_OUT_MONO).build())
                .setTransferMode(AudioTrack.MODE_STATIC).setBufferSizeInBytes(pcm.size * 2).build()
            track.write(pcm, 0, pcm.size)
            track.play()
            main.postDelayed({ runCatching { track.release() } }, 300)
        }
    }
}
