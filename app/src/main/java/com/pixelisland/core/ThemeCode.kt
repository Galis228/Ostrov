package com.pixelisland.core

import android.graphics.Bitmap
import android.util.Base64
import com.pixelisland.settings.IslandSettings
import com.pixelisland.settings.SettingsStore
import java.io.ByteArrayOutputStream
import java.util.zip.Deflater
import java.util.zip.Inflater

/** Тема острова одной строкой (и QR-кодом): только оформление, без включённых событий и калибровки. */
object ThemeCode {
    private const val PREFIX = "OSTROV1:"

    fun export(): String {
        val json = SettingsStore.exportJson().toByteArray()
        val d = Deflater(Deflater.BEST_COMPRESSION).apply { setInput(json); finish() }
        val out = ByteArrayOutputStream()
        val buf = ByteArray(4096)
        while (!d.finished()) out.write(buf, 0, d.deflate(buf))
        return PREFIX + Base64.encodeToString(out.toByteArray(), Base64.URL_SAFE or Base64.NO_WRAP)
    }

    /** Применяет оформление из кода; возвращает false, если код не распознан. */
    fun import(code: String): Boolean = runCatching {
        val raw = code.trim().removePrefix(PREFIX)
        val bytes = Base64.decode(raw, Base64.URL_SAFE)
        val inf = Inflater().apply { setInput(bytes) }
        val out = ByteArrayOutputStream()
        val buf = ByteArray(4096)
        while (!inf.finished()) { val n = inf.inflate(buf); if (n == 0 && inf.needsInput()) break; out.write(buf, 0, n) }
        val theme = SettingsStore.parseJson(out.toString(Charsets.UTF_8.name()))
        SettingsStore.update { mergeLook(this, theme) }
        true
    }.getOrDefault(false)

    private fun mergeLook(cur: IslandSettings, t: IslandSettings): IslandSettings {
        val events = cur.events.toMutableMap()
        t.events.forEach { (type, style) -> events[type] = style.copy(enabled = cur.style(type).enabled) }
        return cur.copy(
            pillPadDp = t.pillPadDp, sideWidthDp = t.sideWidthDp, roundness = t.roundness, contentScale = t.contentScale,
            toastSideDp = t.toastSideDp, autoMaxSideDp = t.autoMaxSideDp,
            expandedMarginDp = t.expandedMarginDp, expandedCornerDp = t.expandedCornerDp,
            bg = t.bg, bgOpacity = t.bgOpacity, outline = t.outline, shadow = t.shadow, glow = t.glow, blurBehind = t.blurBehind,
            gradient = t.gradient, bgCustom2 = t.bgCustom2, followSystemTheme = t.followSystemTheme,
            animStyle = t.animStyle, appearStyle = t.appearStyle, animSpeed = t.animSpeed, events = events,
        )
    }

    /** QR-код из строки (ZXing). */
    fun qr(text: String, size: Int = 720): Bitmap? = runCatching {
        val hints = mapOf(com.google.zxing.EncodeHintType.ERROR_CORRECTION to com.google.zxing.qrcode.decoder.ErrorCorrectionLevel.L,
            com.google.zxing.EncodeHintType.MARGIN to 1)
        val m = com.google.zxing.qrcode.QRCodeWriter().encode(text, com.google.zxing.BarcodeFormat.QR_CODE, size, size, hints)
        Bitmap.createBitmap(m.width, m.height, Bitmap.Config.ARGB_8888).apply {
            for (x in 0 until m.width) for (y in 0 until m.height) setPixel(x, y, if (m[x, y]) 0xFF000000.toInt() else 0xFFFFFFFF.toInt())
        }
    }.getOrNull()
}
