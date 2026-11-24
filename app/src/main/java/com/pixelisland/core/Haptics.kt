package com.pixelisland.core

import android.content.Context
import android.os.VibrationEffect
import android.os.VibratorManager
import android.view.HapticFeedbackConstants
import android.view.View
import com.pixelisland.settings.SettingsStore

/** Тактильная отдача Pixel: примитивы композиции (как в системном интерфейсе), с запасным вариантом. */
object Haptics {
    enum class Kind { TAP, EXPAND, COLLAPSE, LONG, TICK }

    fun play(view: View, kind: Kind) {
        val st = SettingsStore.value
        if (!st.haptics) return
        if (IslandRuntime.night.value && st.nightEnabled && st.nightQuiet) return
        val vib = view.context.getSystemService(VibratorManager::class.java)?.defaultVibrator
        val (prim, scale) = when (kind) {
            Kind.TAP -> VibrationEffect.Composition.PRIMITIVE_CLICK to 0.7f
            Kind.EXPAND -> VibrationEffect.Composition.PRIMITIVE_QUICK_RISE to 0.6f
            Kind.COLLAPSE -> VibrationEffect.Composition.PRIMITIVE_QUICK_FALL to 0.6f
            Kind.LONG -> VibrationEffect.Composition.PRIMITIVE_THUD to 0.8f
            Kind.TICK -> VibrationEffect.Composition.PRIMITIVE_LOW_TICK to 0.6f
        }
        if (vib != null && vib.areAllPrimitivesSupported(prim)) {
            val c = VibrationEffect.startComposition().addPrimitive(prim, scale)
            if (kind == Kind.EXPAND && vib.areAllPrimitivesSupported(VibrationEffect.Composition.PRIMITIVE_TICK)) {
                c.addPrimitive(VibrationEffect.Composition.PRIMITIVE_TICK, 0.5f, 40)
            }
            runCatching { vib.vibrate(c.compose()) }
        } else {
            view.performHapticFeedback(
                if (kind == Kind.LONG) HapticFeedbackConstants.LONG_PRESS else HapticFeedbackConstants.CONTEXT_CLICK,
            )
        }
    }
}

/** Системные настройки, которые остров учитывает. */
object SystemPrefs {
    fun refresh(ctx: Context) {
        val scale = runCatching {
            android.provider.Settings.Global.getFloat(ctx.contentResolver, android.provider.Settings.Global.ANIMATOR_DURATION_SCALE, 1f)
        }.getOrDefault(1f)
        IslandRuntime.reduceMotion.value = scale == 0f
        if (android.os.Build.VERSION.SDK_INT >= 34) {
            val c = ctx.getSystemService(android.app.UiModeManager::class.java)?.contrast ?: 0f
            IslandRuntime.highContrast.value = c >= 0.5f
        }
    }
}
