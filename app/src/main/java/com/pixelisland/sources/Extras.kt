package com.pixelisland.sources

import android.Manifest
import android.app.PendingIntent
import android.content.ContentUris
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.database.ContentObserver
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.LocationManager
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.provider.CalendarContract
import android.provider.MediaStore
import android.util.Size
import androidx.core.graphics.drawable.toBitmap
import androidx.palette.graphics.Palette
import com.pixelisland.core.CalendarActivity
import com.pixelisland.core.IslandController
import com.pixelisland.core.IslandRuntime
import com.pixelisland.core.PeekEvent
import com.pixelisland.core.ToastEvent
import com.pixelisland.settings.BgChoice
import com.pixelisland.settings.EventType
import com.pixelisland.settings.IdleContent
import com.pixelisland.settings.SettingsStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.URL
import java.util.Calendar

/** Погода, календарь, шаги, скриншоты, ночной режим и цвет приложения на экране. */
@android.annotation.SuppressLint("MissingPermission")
object Extras {
    private var ctx: Context? = null
    private val main = Handler(Looper.getMainLooper())
    private val io = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val s get() = SettingsStore.value

    private fun granted(p: String) = ctx?.checkSelfPermission(p) == PackageManager.PERMISSION_GRANTED

    fun start(c: Context) {
        if (ctx != null) return
        ctx = c
        main.post(tick)
        runCatching {
            c.contentResolver.registerContentObserver(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, true, screenshotObserver)
        }
        updateSteps()
        runCatching {
            val wm = android.app.WallpaperManager.getInstance(c)
            wm.addOnColorsChangedListener(wallListener, main)
            readWallColors(wm.getWallpaperColors(android.app.WallpaperManager.FLAG_SYSTEM))
        }
    }

    // ---------- Цвета обоев ----------
    private val wallListener = android.app.WallpaperManager.OnColorsChangedListener { colors, which ->
        if (which and android.app.WallpaperManager.FLAG_SYSTEM != 0) readWallColors(colors)
    }
    private fun readWallColors(w: android.app.WallpaperColors?) {
        w ?: return
        val p = w.primaryColor.toArgb()
        val sec = w.secondaryColor?.toArgb() ?: p
        val t = w.tertiaryColor?.toArgb() ?: sec
        IslandRuntime.wallColors.value = Triple(p, sec, t)
    }

    // ---------- Предупреждения о погоде ----------
    private var lastAlertCheck = 0L
    private fun checkWeatherAlerts() {
        val c = ctx ?: return
        if (!s.on(EventType.WEATHER_ALERT) || !granted(Manifest.permission.ACCESS_COARSE_LOCATION)) return
        val now = System.currentTimeMillis()
        if (now - lastAlertCheck < 30 * 60_000) return
        lastAlertCheck = now
        val lm = c.getSystemService(LocationManager::class.java)
        val loc = runCatching { lm.getProviders(true).mapNotNull { lm.getLastKnownLocation(it) }.maxByOrNull { it.time } }.getOrNull() ?: return
        val prefs = c.getSharedPreferences("extras", Context.MODE_PRIVATE)
        io.launch {
            val r = runCatching {
                val url = "https://api.open-meteo.com/v1/forecast?latitude=%.3f&longitude=%.3f&current=temperature_2m,precipitation&minutely_15=precipitation&forecast_minutely_15=4&timezone=auto"
                    .format(java.util.Locale.US, loc.latitude, loc.longitude)
                val j = JSONObject(URL(url).readText())
                val cur = j.getJSONObject("current")
                val pr = j.getJSONObject("minutely_15").getJSONArray("precipitation")
                Triple(cur.getDouble("temperature_2m"), cur.optDouble("precipitation", 0.0), (0 until pr.length()).map { pr.optDouble(it, 0.0) })
            }.getOrNull() ?: return@launch
            val (temp, nowRain, next) = r
            val day = Calendar.getInstance().let { it.get(Calendar.YEAR) * 1000 + it.get(Calendar.DAY_OF_YEAR) }
            var msg: Pair<String, String>? = null
            val idx = next.indexOfFirst { it >= 0.2 }
            if (nowRain < 0.1 && idx in 1..2 && now - prefs.getLong("rainAlert", 0) > 3 * 3600_000) {
                prefs.edit().putLong("rainAlert", now).apply()
                msg = "Дождь через ${idx * 15} мин" to "☂"
            } else if (temp >= 32 && prefs.getInt("heatDay", 0) != day) {
                prefs.edit().putInt("heatDay", day).apply()
                msg = "Сильная жара" to "+${Math.round(temp)}°"
            } else if (temp <= -15 && prefs.getInt("coldDay", 0) != day) {
                prefs.edit().putInt("coldDay", day).apply()
                msg = "Сильный мороз" to "${Math.round(temp)}°"
            }
            msg?.let { (t, v) ->
                withContext(Dispatchers.Main) {
                    IslandController.showToast(ToastEvent("walert", EventType.WEATHER_ALERT, t, v,
                        iconName = if (t.startsWith("Дождь")) "Umbrella" else if (temp > 0) "Whatshot" else "AcUnit"))
                }
            }
        }
    }

    // ---------- Курс валют и криптовалюты ----------
    private var lastRate = 0L
    private var lastRateKind: com.pixelisland.settings.RateKind? = null
    fun refreshRate(force: Boolean = false) {
        if (ctx == null) return
        val kind = s.rateKind
        val want = s.showIdlePill && s.idleContent == IdleContent.RATE
        if (!want && !force) return
        val now = System.currentTimeMillis()
        if (!force && kind == lastRateKind && now - lastRate < 30 * 60_000) return
        lastRate = now; lastRateKind = kind
        io.launch {
            val v = runCatching { fetchRate(kind) }.getOrNull() ?: return@launch
            withContext(Dispatchers.Main) { IslandRuntime.rate.value = v }
        }
    }

    private fun fetchRate(k: com.pixelisland.settings.RateKind): String {
        fun fiat(base: String, to: String): Double = JSONObject(URL("https://open.er-api.com/v6/latest/$base").readText()).getJSONObject("rates").getDouble(to)
        fun crypto(id: String): Double = JSONObject(URL("https://api.coingecko.com/api/v3/simple/price?ids=$id&vs_currencies=usd").readText()).getJSONObject(id).getDouble("usd")
        return when (k) {
            com.pixelisland.settings.RateKind.USD_UAH -> "%.2f".format(fiat("USD", "UAH"))
            com.pixelisland.settings.RateKind.EUR_UAH -> "%.2f".format(fiat("EUR", "UAH"))
            com.pixelisland.settings.RateKind.USD_EUR -> "%.3f".format(fiat("USD", "EUR"))
            com.pixelisland.settings.RateKind.PLN_UAH -> "%.2f".format(fiat("PLN", "UAH"))
            com.pixelisland.settings.RateKind.BTC_USD -> crypto("bitcoin").let { if (it >= 1000) "%.1fK".format(it / 1000) else "%.0f".format(it) }
            com.pixelisland.settings.RateKind.ETH_USD -> "%.0f".format(crypto("ethereum"))
        }
    }

    fun stop() {
        val c = ctx ?: return
        main.removeCallbacks(tick)
        runCatching { c.contentResolver.unregisterContentObserver(screenshotObserver) }
        stepsOff()
        runCatching { android.app.WallpaperManager.getInstance(c).removeOnColorsChangedListener(wallListener) }
        IslandController.remove("calendar")
        ctx = null
    }

    // ---------- Периодическая проверка: ночь и календарь ----------
    private val tick = object : Runnable {
        override fun run() {
            updateNight()
            checkCalendar()
            updateSteps()
            checkWeatherAlerts()
            refreshRate()
            com.pixelisland.core.Profiles.tick()
            main.postDelayed(this, 30_000)
        }
    }

    private fun updateNight() {
        val h = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        val from = s.nightFrom; val to = s.nightTo
        val inRange = if (from <= to) h in from until to else h >= from || h < to
        IslandRuntime.night.value = s.nightEnabled && inRange
    }

    private fun checkCalendar() {
        val c = ctx ?: return
        if (!s.on(EventType.CALENDAR) || !granted(Manifest.permission.READ_CALENDAR)) { IslandController.remove("calendar"); return }
        io.launch {
            val now = System.currentTimeMillis()
            val found = runCatching {
                val uri = CalendarContract.Instances.CONTENT_URI.buildUpon().also {
                    ContentUris.appendId(it, now - 60_000); ContentUris.appendId(it, now + 15 * 60_000)
                }.build()
                c.contentResolver.query(
                    uri, arrayOf(CalendarContract.Instances.TITLE, CalendarContract.Instances.BEGIN, CalendarContract.Instances.ALL_DAY),
                    null, null, CalendarContract.Instances.BEGIN + " ASC",
                )?.use { cur ->
                    var r: Pair<String, Long>? = null
                    while (cur.moveToNext() && r == null) {
                        val begin = cur.getLong(1)
                        if (cur.getInt(2) == 0 && begin > now - 60_000) r = (cur.getString(0) ?: "Событие") to begin
                    }
                    r
                }
            }.getOrNull()
            withContext(Dispatchers.Main) {
                if (found != null) IslandController.set(CalendarActivity(found.first, found.second)) else IslandController.remove("calendar")
            }
        }
    }

    // ---------- Погода (Open-Meteo, без ключа) ----------
    fun onUnlock() {
        val c = ctx ?: return
        if (!s.on(EventType.WEATHER)) return
        val cal = Calendar.getInstance()
        if (cal.get(Calendar.HOUR_OF_DAY) !in 5..11) return
        val day = cal.get(Calendar.YEAR) * 1000 + cal.get(Calendar.DAY_OF_YEAR)
        val prefs = c.getSharedPreferences("extras", Context.MODE_PRIVATE)
        if (prefs.getInt("weatherDay", 0) == day) return
        showWeather { prefs.edit().putInt("weatherDay", day).apply() }
    }

    /** Загрузить и показать погоду сейчас (для кнопки «Проверить»). */
    fun showWeather(onDone: () -> Unit = {}) {
        val c = ctx ?: return
        if (!granted(Manifest.permission.ACCESS_COARSE_LOCATION)) return
        val lm = c.getSystemService(LocationManager::class.java)
        @Suppress("MissingPermission")
        val loc = runCatching {
            lm.getProviders(true).mapNotNull { lm.getLastKnownLocation(it) }.maxByOrNull { it.time }
        }.getOrNull() ?: return
        io.launch {
            val w = runCatching {
                val url = "https://api.open-meteo.com/v1/forecast?latitude=%.3f&longitude=%.3f&current=temperature_2m,weather_code&timezone=auto"
                    .format(java.util.Locale.US, loc.latitude, loc.longitude)
                val j = JSONObject(URL(url).readText()).getJSONObject("current")
                j.getDouble("temperature_2m") to j.getInt("weather_code")
            }.getOrNull() ?: return@launch
            val t = Math.round(w.first).toInt()
            val (desc, icon) = weatherText(w.second)
            withContext(Dispatchers.Main) {
                IslandController.showToast(ToastEvent("weather", EventType.WEATHER, desc, (if (t > 0) "+" else "") + "$t°", iconName = icon))
                onDone()
            }
        }
    }

    fun weatherText(code: Int): Pair<String, String> = when (code) {
        0 -> "Ясно" to "WbSunny"
        1, 2 -> "Малооблачно" to "WbCloudy"
        3 -> "Пасмурно" to "CloudQueue"
        45, 48 -> "Туман" to "Air"
        in 51..67, in 80..82 -> "Дождь" to "Umbrella"
        in 71..77, 85, 86 -> "Снег" to "AcUnit"
        in 95..99 -> "Гроза" to "Thunderstorm"
        else -> "Погода" to "Thermostat"
    }

    // ---------- Шаги за сегодня ----------
    private var stepsListening = false
    private val stepListener = object : SensorEventListener {
        override fun onSensorChanged(e: SensorEvent) {
            val c = ctx ?: return
            val total = e.values[0].toInt()
            val p = c.getSharedPreferences("extras", Context.MODE_PRIVATE)
            val cal = Calendar.getInstance()
            val day = cal.get(Calendar.YEAR) * 1000 + cal.get(Calendar.DAY_OF_YEAR)
            var base = p.getInt("stepsBase", -1)
            if (p.getInt("stepsDay", 0) != day || base < 0 || total < base) {
                base = if (total < base) 0 else total
                p.edit().putInt("stepsDay", day).putInt("stepsBase", base).apply()
            }
            IslandRuntime.steps.value = total - base
        }
        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit
    }

    private fun updateSteps() {
        val c = ctx ?: return
        val want = s.showIdlePill && s.idleContent == IdleContent.STEPS && granted(Manifest.permission.ACTIVITY_RECOGNITION)
        if (want && !stepsListening) {
            val sm = c.getSystemService(SensorManager::class.java)
            val sensor = sm.getDefaultSensor(Sensor.TYPE_STEP_COUNTER) ?: return
            stepsListening = sm.registerListener(stepListener, sensor, SensorManager.SENSOR_DELAY_NORMAL, 10_000_000)
        } else if (!want && stepsListening) stepsOff()
    }

    private fun stepsOff() {
        ctx?.getSystemService(SensorManager::class.java)?.unregisterListener(stepListener)
        stepsListening = false
    }

    // ---------- Скриншоты ----------
    private var lastShot = -1L
    private val screenshotObserver = object : ContentObserver(main) {
        override fun onChange(selfChange: Boolean, uri: Uri?) {
            val c = ctx ?: return
            if (!s.on(EventType.SCREENSHOT) || !granted(Manifest.permission.READ_MEDIA_IMAGES)) return
            io.launch {
                val now = System.currentTimeMillis() / 1000
                val shot = runCatching {
                    c.contentResolver.query(
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                        arrayOf(MediaStore.Images.Media._ID, MediaStore.Images.Media.RELATIVE_PATH, MediaStore.Images.Media.DATE_ADDED),
                        "${MediaStore.Images.Media.DATE_ADDED} >= ?", arrayOf((now - 10).toString()),
                        "${MediaStore.Images.Media.DATE_ADDED} DESC",
                    )?.use { cur ->
                        if (cur.moveToFirst() && cur.getString(1).orEmpty().contains("Screenshot", ignoreCase = true)) cur.getLong(0) else null
                    }
                }.getOrNull() ?: return@launch
                if (shot == lastShot) return@launch
                lastShot = shot
                val u = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, shot)
                val thumb = runCatching { c.contentResolver.loadThumbnail(u, Size(360, 360), null) }.getOrNull()
                val open = PendingIntent.getActivity(
                    c, shot.toInt(),
                    Intent(Intent.ACTION_VIEW).setDataAndType(u, "image/*")
                        .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK),
                    PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
                )
                withContext(Dispatchers.Main) {
                    IslandController.showToast(
                        PeekEvent("shot:$shot", "Скриншот", null, "Снимок экрана сохранён", "Нажмите, чтобы открыть",
                            thumb, null, open, type = EventType.SCREENSHOT),
                    )
                }
            }
        }
    }

    // ---------- Цвет приложения на экране ----------
    private val appColors = com.pixelisland.core.Lru<String, Int?>(48)
    fun onForegroundApp(pkg: String) {
        val c = ctx ?: return
        if (s.bg != BgChoice.APP_TINT || pkg == "com.android.systemui" || pkg == c.packageName) return
        appColors[pkg]?.let { IslandRuntime.appColor.value = it; return }
        io.launch {
            val color = runCatching {
                val bmp = c.packageManager.getApplicationIcon(pkg).toBitmap(64, 64)
                val p = Palette.from(bmp).generate()
                (p.vibrantSwatch ?: p.dominantSwatch)?.rgb
            }.getOrNull()
            appColors[pkg] = color
            withContext(Dispatchers.Main) { IslandRuntime.appColor.value = color }
        }
    }
}
