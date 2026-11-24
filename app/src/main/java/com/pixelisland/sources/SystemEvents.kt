package com.pixelisland.sources

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.media.AudioDeviceCallback
import android.media.AudioDeviceInfo
import android.media.AudioManager
import android.app.NotificationManager
import android.os.BatteryManager
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import com.pixelisland.core.ChargingActivity
import com.pixelisland.core.IslandController
import com.pixelisland.core.ToastEvent
import com.pixelisland.core.PeekEvent
import com.pixelisland.settings.EventKind
import com.pixelisland.settings.EventType
import com.pixelisland.settings.EventType.*
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.os.PowerManager
import com.pixelisland.core.TorchActivity
import com.pixelisland.core.PrivacyActivity
import com.pixelisland.core.AlarmSoonActivity
import com.pixelisland.settings.SettingsStore

object Tints {
    const val BLUE = 0xFF6EA8FF
    const val GREEN = 0xFF5EDB8B
    const val RED = 0xFFFF6B6B
    const val ORANGE = 0xFFFFB25C
    const val YELLOW = 0xFFFFD866
    const val PURPLE = 0xFFB89CFF
    const val GREY = 0xFFB0B4BC
}

/** Системные события: Bluetooth, наушники, полёт, зарядка, фонарик, звук, «Не беспокоить». */
@android.annotation.SuppressLint("MissingPermission")
object SystemEvents {
    private var ctx: Context? = null
    private val main = Handler(Looper.getMainLooper())
    private val settings get() = SettingsStore.value

    private var cameraManager: CameraManager? = null
    private val torchKnown = mutableSetOf<String>()
    private var torchCamera: String? = null

    private var audioInitial = true
    private val recentAudio = mutableMapOf<String, Long>()
    private val headsetBattery = mutableMapOf<String, Int>()
    private var lastHeadset: String? = null

    private var fullNotified = false
    private var lastBatterySig = -1
    private var lastPlugged = -1
    private var lastLevel = -1

    fun shortTime(ms: Long): String {
        val m = (ms / 60_000).coerceAtLeast(1)
        return if (m >= 60) "${m / 60} ч ${m % 60} мин" else "$m мин"
    }

    fun start(context: Context) {
        if (ctx != null) return
        ctx = context
        val filter = IntentFilter().apply {
            addAction(BluetoothAdapter.ACTION_STATE_CHANGED)
            addAction(Intent.ACTION_AIRPLANE_MODE_CHANGED)
            addAction(Intent.ACTION_POWER_CONNECTED)
            addAction(Intent.ACTION_POWER_DISCONNECTED)
            addAction(Intent.ACTION_BATTERY_LOW)
            addAction(Intent.ACTION_BATTERY_CHANGED)
            addAction(AudioManager.RINGER_MODE_CHANGED_ACTION)
            addAction(NotificationManager.ACTION_INTERRUPTION_FILTER_CHANGED)
            addAction(ACTION_HEADSET_BATTERY)
            addAction(PowerManager.ACTION_POWER_SAVE_MODE_CHANGED)
            addAction(ACTION_VOLUME)
            addAction(ACTION_HOTSPOT)
            addAction(android.app.AlarmManager.ACTION_NEXT_ALARM_CLOCK_CHANGED)
            addAction(Intent.ACTION_USER_PRESENT)
            addAction(Intent.ACTION_SCREEN_ON)
            addAction(Intent.ACTION_SCREEN_OFF)
        }
        context.registerReceiver(receiver, filter, Context.RECEIVER_EXPORTED)

        context.getSystemService(AudioManager::class.java)?.let {
            audioInitial = true
            it.registerAudioDeviceCallback(audioCallback, main)
            main.postDelayed({ audioInitial = false }, 1500)
        }
        cameraManager = context.getSystemService(CameraManager::class.java)?.also {
            it.registerTorchCallback(torchCallback, main)
        }
        runCatching {
            wifiInitial = true
            context.getSystemService(ConnectivityManager::class.java).registerNetworkCallback(
                NetworkRequest.Builder().addTransportType(NetworkCapabilities.TRANSPORT_WIFI).build(), wifiCallback, main,
            )
            main.postDelayed({ wifiInitial = false }, 1500)
        }
        runCatching {
            vpnInitial = true
            context.getSystemService(ConnectivityManager::class.java).registerNetworkCallback(
                NetworkRequest.Builder().addTransportType(NetworkCapabilities.TRANSPORT_VPN)
                    .removeCapability(NetworkCapabilities.NET_CAPABILITY_NOT_VPN).build(), vpnCallback, main,
            )
            main.postDelayed({ vpnInitial = false }, 1500)
        }
        cameraManager?.registerAvailabilityCallback(cameraAvailability, main)
        context.getSystemService(AudioManager::class.java)?.let { am ->
            am.registerAudioRecordingCallback(recordingCallback, main)
            if (am.activeRecordingConfigurations.isNotEmpty()) IslandController.set(PrivacyActivity(MIC_IN_USE))
        }
        main.post(alarmTick)
        runCatching {
            netInitial = true
            context.getSystemService(ConnectivityManager::class.java).registerDefaultNetworkCallback(netCallback, main)
            main.postDelayed({ netInitial = false }, 2000)
        }
        runCatching {
            val cr = context.contentResolver
            cr.registerContentObserver(android.provider.Settings.System.getUriFor(android.provider.Settings.System.SCREEN_BRIGHTNESS), false, brightnessObserver)
            cr.registerContentObserver(android.provider.Settings.System.getUriFor(android.provider.Settings.System.ACCELEROMETER_ROTATION), false, rotationSettingObserver)
        }
        runCatching { context.getSystemService(android.content.ClipboardManager::class.java).addPrimaryClipChangedListener(clipListener) }
        updateRotationWatch()
        com.pixelisland.core.IslandRuntime.locked.value = context.getSystemService(android.app.KeyguardManager::class.java)?.isKeyguardLocked == true
    }

    // ---------- Сеть пропала / вернулась ----------
    private var netInitial = true
    private var netLost = false
    private val netLostCheck = Runnable {
        val c = ctx ?: return@Runnable
        if (c.getSystemService(ConnectivityManager::class.java).activeNetwork == null) {
            netLost = true
            toast(NET_LOST, "Нет сети", "Интернет пропал", id = "net")
        }
    }
    private val netCallback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            main.removeCallbacks(netLostCheck)
            if (netLost && !netInitial) {
                val caps = ctx?.getSystemService(ConnectivityManager::class.java)?.getNetworkCapabilities(network)
                val kind = when {
                    caps?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) == true -> "Wi‑Fi"
                    caps?.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) == true -> "Мобильная сеть"
                    else -> "Подключено"
                }
                toast(NET_BACK, "Сеть вернулась", kind, id = "net")
            }
            netLost = false
        }
        override fun onLost(network: Network) {
            if (netInitial) return
            // Переключение Wi‑Fi ↔ LTE даёт короткий «обрыв» — ждём 3 секунды.
            main.removeCallbacks(netLostCheck)
            main.postDelayed(netLostCheck, 3000)
        }
    }

    // ---------- Яркость ----------
    private var lastBrightness = -1
    private val brightnessObserver = object : android.database.ContentObserver(main) {
        override fun onChange(selfChange: Boolean) {
            val c = ctx ?: return
            if (!settings.on(BRIGHTNESS)) return
            val v = runCatching { android.provider.Settings.System.getInt(c.contentResolver, android.provider.Settings.System.SCREEN_BRIGHTNESS) }.getOrNull() ?: return
            val pct = brightnessPercent(v)
            if (pct == lastBrightness) return
            lastBrightness = pct
            toast(BRIGHTNESS, "Яркость", "$pct%", pct / 100f, id = "brightness")
        }
    }

    /** Как ползунок Android: яркость 0..255 в «гамма-проценты» (BrightnessUtils). */
    private fun brightnessPercent(v: Int): Int {
        val n = ((v - 1).coerceAtLeast(0) / 254f) * 12f
        val g = if (n <= 1f) 0.5f * kotlin.math.sqrt(n) else 0.17883277f * kotlin.math.ln(n - 0.28466892f) + 0.55991073f
        return (g * 100).toInt().coerceIn(0, 100)
    }

    // ---------- Поворот экрана ----------
    private var orientation: android.view.OrientationEventListener? = null
    private var sidewaysSince = 0L
    private var lastRotateHint = 0L
    private val rotationSettingObserver = object : android.database.ContentObserver(main) {
        override fun onChange(selfChange: Boolean) = updateRotationWatch()
    }

    private fun rotationLocked(c: Context) =
        runCatching { android.provider.Settings.System.getInt(c.contentResolver, android.provider.Settings.System.ACCELEROMETER_ROTATION) == 0 }.getOrDefault(false)

    /** Датчик слушаем, только когда событие включено, автоповорот выключен и экран горит. */
    fun updateRotationWatch(screenOn: Boolean = true) {
        val c = ctx ?: return
        val want = screenOn && settings.on(ROTATE) && rotationLocked(c)
        if (want && orientation == null) {
            orientation = object : android.view.OrientationEventListener(c, android.hardware.SensorManager.SENSOR_DELAY_NORMAL) {
                override fun onOrientationChanged(deg: Int) {
                    if (deg == ORIENTATION_UNKNOWN) return
                    val side = deg in 65..115 || deg in 245..295
                    val now = SystemClock.elapsedRealtime()
                    if (!side) { sidewaysSince = 0; return }
                    if (sidewaysSince == 0L) sidewaysSince = now
                    if (now - sidewaysSince > 1200 && now - lastRotateHint > 30_000) {
                        lastRotateHint = now
                        showRotateHint(c)
                    }
                }
            }.also { if (it.canDetectOrientation()) it.enable() }
        } else if (!want) {
            orientation?.disable(); orientation = null
        }
    }

    private fun showRotateHint(c: Context) {
        IslandController.showToast(
            PeekEvent(
                "rotate", "Поворот экрана", null, "Повернуть экран?", "Нажмите, чтобы включить автоповорот",
                null, null, null, type = ROTATE, actionLabel = "Включить",
                onTap = { enableAutoRotate(c) },
            ),
        )
    }

    fun enableAutoRotate(c: Context) {
        if (android.provider.Settings.System.canWrite(c)) {
            runCatching { android.provider.Settings.System.putInt(c.contentResolver, android.provider.Settings.System.ACCELEROMETER_ROTATION, 1) }
        } else runCatching {
            c.startActivity(
                Intent(android.provider.Settings.ACTION_MANAGE_WRITE_SETTINGS, android.net.Uri.parse("package:${c.packageName}"))
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
            )
        }
    }

    // ---------- Буфер обмена ----------
    private var lastCopy = 0L
    private val clipListener = android.content.ClipboardManager.OnPrimaryClipChangedListener { copied() }

    /** «Скопировано» — из слушателя буфера или из нажатия кнопки «Копировать» (сервис доступности). */
    fun copied(what: String = "") {
        val now = SystemClock.elapsedRealtime()
        if (now - lastCopy < 1500) return
        lastCopy = now
        toast(COPIED, "Скопировано", what, id = "copied")
    }

    /** Включить/выключить фонарик (для сценариев и виджета) — работает и без запущенного сервиса. */
    fun setTorch(c: Context, on: Boolean) {
        val cm = cameraManager ?: c.getSystemService(CameraManager::class.java) ?: return
        val id = torchCamera ?: runCatching {
            cm.cameraIdList.firstOrNull { cm.getCameraCharacteristics(it).get(CameraCharacteristics.FLASH_INFO_AVAILABLE) == true }
        }.getOrNull() ?: return
        runCatching { cm.setTorchMode(id, on) }
    }

    // ---------- Wi‑Fi ----------
    private var wifiInitial = true
    private var wifiUp = false
    private val wifiCallback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            if (wifiUp) return
            wifiUp = true
            if (!wifiInitial) toast(WIFI_ON, "Wi‑Fi", "Подключено", id = "wifi")
        }
        override fun onLost(network: Network) {
            if (!wifiUp) return
            wifiUp = false
            toast(WIFI_OFF, "Wi‑Fi", "Отключено", id = "wifi")
        }
    }

    fun stop() {
        val c = ctx ?: return
        runCatching { c.unregisterReceiver(receiver) }
        c.getSystemService(AudioManager::class.java)?.unregisterAudioDeviceCallback(audioCallback)
        cameraManager?.unregisterTorchCallback(torchCallback)
        runCatching { c.getSystemService(ConnectivityManager::class.java).unregisterNetworkCallback(wifiCallback) }
        runCatching { c.getSystemService(ConnectivityManager::class.java).unregisterNetworkCallback(vpnCallback) }
        cameraManager?.unregisterAvailabilityCallback(cameraAvailability)
        c.getSystemService(AudioManager::class.java)?.unregisterAudioRecordingCallback(recordingCallback)
        main.removeCallbacks(alarmTick)
        runCatching { c.getSystemService(ConnectivityManager::class.java).unregisterNetworkCallback(netCallback) }
        runCatching { c.contentResolver.unregisterContentObserver(brightnessObserver) }
        runCatching { c.contentResolver.unregisterContentObserver(rotationSettingObserver) }
        runCatching { c.getSystemService(android.content.ClipboardManager::class.java).removePrimaryClipChangedListener(clipListener) }
        orientation?.disable(); orientation = null
        main.removeCallbacks(netLostCheck)
        busyCameras.clear()
        listOf(MIC_IN_USE.name, CAMERA_IN_USE.name, "alarm_soon").forEach(IslandController::remove)
        torchKnown.clear()
        IslandController.remove("torch")
        IslandController.remove("charging")
        ctx = null
    }

    // ---------- Фонарик ----------
    private val torchCallback = object : CameraManager.TorchCallback() {
        override fun onTorchModeChanged(cameraId: String, enabled: Boolean) {
            val first = torchKnown.add(cameraId)
            if (enabled) {
                torchCamera = cameraId
                IslandController.set(torchActivity(cameraId))
            } else if (torchCamera == cameraId || first) {
                val wasOn = torchCamera == cameraId
                IslandController.remove("torch")
                if (wasOn && !first) {
                    toast(TORCH_OFF, "Фонарик", "Выкл")
                }
                if (wasOn) torchCamera = null
            }
        }

        override fun onTorchStrengthLevelChanged(cameraId: String, newStrengthLevel: Int) {
            if (cameraId == torchCamera) IslandController.set(torchActivity(cameraId))
        }
    }

    private fun torchActivity(id: String): TorchActivity {
        val cm = cameraManager
        var max = 1
        var cur = 1
        if (cm != null && Build.VERSION.SDK_INT >= 33) {
            runCatching {
                max = cm.getCameraCharacteristics(id).get(CameraCharacteristics.FLASH_INFO_STRENGTH_MAXIMUM_LEVEL) ?: 1
                cur = cm.getTorchStrengthLevel(id)
            }
        }
        return TorchActivity(id, cur, max)
    }

    /** Включить/выключить фонарик основной камеры (для быстрых действий). */
    fun toggleTorch() {
        val cm = cameraManager ?: return
        torchCamera?.let { runCatching { cm.setTorchMode(it, false) }; return }
        val id = runCatching {
            cm.cameraIdList.firstOrNull { cm.getCameraCharacteristics(it).get(CameraCharacteristics.FLASH_INFO_AVAILABLE) == true }
        }.getOrNull() ?: return
        runCatching { cm.setTorchMode(id, true) }
    }

    /** Звук → вибрация → без звука → звук. Без доступа к «Не беспокоить» беззвучный может быть недоступен. */
    fun cycleRinger(c: Context) {
        val am = c.getSystemService(AudioManager::class.java)
        val next = when (am.ringerMode) {
            AudioManager.RINGER_MODE_NORMAL -> AudioManager.RINGER_MODE_VIBRATE
            AudioManager.RINGER_MODE_VIBRATE -> AudioManager.RINGER_MODE_SILENT
            else -> AudioManager.RINGER_MODE_NORMAL
        }
        runCatching { am.ringerMode = next }.onFailure {
            if (next == AudioManager.RINGER_MODE_SILENT) runCatching { am.ringerMode = AudioManager.RINGER_MODE_NORMAL }
        }
    }

    fun torchOff() {
        val id = torchCamera ?: return
        runCatching { cameraManager?.setTorchMode(id, false) }
    }

    fun torchStrength(level: Int) {
        val id = torchCamera ?: return
        if (Build.VERSION.SDK_INT >= 33) runCatching { cameraManager?.turnOnTorchWithStrengthLevel(id, level) }
    }

    // ---------- Наушники ----------
    private val headphoneTypes = setOf(
        AudioDeviceInfo.TYPE_BLUETOOTH_A2DP, AudioDeviceInfo.TYPE_BLE_HEADSET,
        AudioDeviceInfo.TYPE_WIRED_HEADPHONES, AudioDeviceInfo.TYPE_WIRED_HEADSET,
        AudioDeviceInfo.TYPE_USB_HEADSET,
    )

    private val audioCallback = object : AudioDeviceCallback() {
        override fun onAudioDevicesAdded(added: Array<out AudioDeviceInfo>) = handleAudio(added, true)
        override fun onAudioDevicesRemoved(removed: Array<out AudioDeviceInfo>) = handleAudio(removed, false)
    }

    private fun handleAudio(devices: Array<out AudioDeviceInfo>, connected: Boolean) {
        if (audioInitial) return
        devices.filter { it.isSink && it.type in headphoneTypes }.forEach { d ->
            val name = d.productName?.toString()?.takeIf { it.isNotBlank() && it != Build.MODEL }
                ?: if (d.type == AudioDeviceInfo.TYPE_BLUETOOTH_A2DP || d.type == AudioDeviceInfo.TYPE_BLE_HEADSET) "Наушники" else "Проводные наушники"
            val k = "$name|$connected"
            val now = SystemClock.elapsedRealtime()
            if (now - (recentAudio[k] ?: 0) < 2000) return@forEach
            recentAudio[k] = now
            if (connected) {
                lastHeadset = name
                headphonesToast(name)
            } else {
                toast(HP_OFF, name, "Отключены", id = "hp:$name")
                com.pixelisland.core.IslandRuntime.headsetBattery.value = null
            }
        }
    }

    private fun headphonesToast(name: String) {
        val bat = headsetBattery[name]
        toast(HP_ON, name, bat?.let { "$it%" } ?: "Подключены", bat?.let { it / 100f }, id = "hp:$name")
    }

    private const val ACTION_VOLUME = "android.media.VOLUME_CHANGED_ACTION"
    private const val ACTION_HOTSPOT = "android.net.wifi.WIFI_AP_STATE_CHANGED"
    private var lastVolume = -1

    private fun onVolume(c: Context, i: Intent) {
        val stream = i.getIntExtra("android.media.EXTRA_VOLUME_STREAM_TYPE", -1)
        val v = i.getIntExtra("android.media.EXTRA_VOLUME_STREAM_VALUE", -1)
        val prev = i.getIntExtra("android.media.EXTRA_PREV_VOLUME_STREAM_VALUE", -1)
        if (v < 0 || v == prev) return
        val name = when (stream) {
            AudioManager.STREAM_MUSIC -> "Медиа"; AudioManager.STREAM_RING -> "Звонок"
            AudioManager.STREAM_ALARM -> "Будильник"; AudioManager.STREAM_VOICE_CALL -> "Разговор"
            else -> return
        }
        val am = c.getSystemService(AudioManager::class.java)
        val max = am.getStreamMaxVolume(stream).coerceAtLeast(1)
        val pct = v * 100 / max
        if (pct == lastVolume) return
        lastVolume = pct
        toast(VOLUME, name, "$pct%", pct / 100f, id = "volume")
    }

    // ---------- Будильник ----------
    private val alarmTick = object : Runnable {
        override fun run() { checkAlarm(); main.postDelayed(this, 30_000) }
    }

    private fun checkAlarm() {
        val c = ctx ?: return
        val next = c.getSystemService(android.app.AlarmManager::class.java)?.nextAlarmClock
        val left = (next?.triggerTime ?: 0L) - System.currentTimeMillis()
        if (next != null && left in 1..30 * 60_000L) {
            val time = java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault()).format(java.util.Date(next.triggerTime))
            IslandController.set(AlarmSoonActivity(next.triggerTime, time))
        } else IslandController.remove("alarm_soon")
    }

    // ---------- Камера и микрофон ----------
    private val busyCameras = mutableSetOf<String>()
    private val cameraAvailability = object : CameraManager.AvailabilityCallback() {
        override fun onCameraUnavailable(cameraId: String) {
            if (cameraId == torchCamera) return
            busyCameras += cameraId; updateCamera()
        }
        override fun onCameraAvailable(cameraId: String) { busyCameras -= cameraId; updateCamera() }
    }

    private fun updateCamera() {
        if (busyCameras.isNotEmpty()) IslandController.set(PrivacyActivity(CAMERA_IN_USE))
        else IslandController.remove(CAMERA_IN_USE.name)
    }

    private val recordingCallback = object : AudioManager.AudioRecordingCallback() {
        override fun onRecordingConfigChanged(configs: MutableList<android.media.AudioRecordingConfiguration>?) {
            if (configs.isNullOrEmpty()) IslandController.remove(MIC_IN_USE.name)
            else IslandController.set(PrivacyActivity(MIC_IN_USE))
        }
    }

    // ---------- VPN ----------
    private var vpnInitial = true
    private var vpnUp = false
    private val vpnCallback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            if (vpnUp) return
            vpnUp = true
            if (!vpnInitial) toast(VPN_ON, "VPN", "Подключён", id = "vpn")
        }
        override fun onLost(network: Network) {
            if (!vpnUp) return
            vpnUp = false
            toast(VPN_OFF, "VPN", "Отключён", id = "vpn")
        }
    }

    private const val ACTION_HEADSET_BATTERY = "android.bluetooth.device.action.BATTERY_LEVEL_CHANGED"
    private const val EXTRA_HEADSET_BATTERY = "android.bluetooth.device.extra.BATTERY_LEVEL"

    // ---------- Широковещательные события ----------
    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(c: Context, i: Intent) {
            val s = settings
            when (i.action) {
                BluetoothAdapter.ACTION_STATE_CHANGED -> when (i.getIntExtra(BluetoothAdapter.EXTRA_STATE, -1)) {
                    BluetoothAdapter.STATE_ON -> toast(BT_ON, "Bluetooth", "Вкл")
                    BluetoothAdapter.STATE_OFF -> toast(BT_OFF, "Bluetooth", "Выкл")
                }

                Intent.ACTION_AIRPLANE_MODE_CHANGED -> {
                    val on = i.getBooleanExtra("state", false)
                    toast(if (on) AIR_ON else AIR_OFF, "Режим полёта", if (on) "Вкл" else "Выкл", id = "airplane")
                }

                Intent.ACTION_POWER_CONNECTED -> main.postDelayed({
                    val (level, wireless) = battery(c)
                    val left = runCatching { c.getSystemService(BatteryManager::class.java).computeChargeTimeRemaining() }.getOrDefault(-1L)
                    val title = (if (wireless) "Беспроводная" else "Зарядка") + (if (left > 0) " · " + shortTime(left) else "")
                    toast(CHARGE_START, title, "$level%", level / 100f, id = "power")
                }, 400)

                Intent.ACTION_POWER_DISCONNECTED -> {
                    val (level, _) = battery(c)
                    toast(CHARGE_STOP, "Отключено", "$level%", level / 100f, id = "power")
                }

                Intent.ACTION_BATTERY_LOW -> {
                    val (level, _) = battery(c)
                    toast(BATTERY_LOW, "Низкий заряд", "$level%", level / 100f)
                }

                Intent.ACTION_BATTERY_CHANGED -> onBattery(c, i)

                PowerManager.ACTION_POWER_SAVE_MODE_CHANGED -> {
                    val on = c.getSystemService(PowerManager::class.java).isPowerSaveMode
                    toast(if (on) SAVER_ON else SAVER_OFF, "Экономия", if (on) "Вкл" else "Выкл", id = "saver")
                }

                Intent.ACTION_USER_PRESENT -> {
                    com.pixelisland.core.IslandRuntime.locked.value = false
                    toast(UNLOCK, "Разблокировано", "", id = "unlock"); Extras.onUnlock()
                }
                Intent.ACTION_SCREEN_OFF -> {
                    com.pixelisland.core.IslandRuntime.locked.value = true
                    updateRotationWatch(screenOn = false)
                }
                Intent.ACTION_SCREEN_ON -> {
                    com.pixelisland.core.IslandRuntime.locked.value =
                        c.getSystemService(android.app.KeyguardManager::class.java)?.isKeyguardLocked == true
                    updateRotationWatch(screenOn = true)
                }

                AudioManager.RINGER_MODE_CHANGED_ACTION -> if (!isInitialStickyBroadcast) {
                    when (i.getIntExtra(AudioManager.EXTRA_RINGER_MODE, -1)) {
                        AudioManager.RINGER_MODE_SILENT -> toast(RINGER_SILENT, "Без звука", "", id = "ringer")
                        AudioManager.RINGER_MODE_VIBRATE -> toast(RINGER_VIBRATE, "Вибрация", "", id = "ringer")
                        AudioManager.RINGER_MODE_NORMAL -> toast(RINGER_SOUND, "Звук", "", id = "ringer")
                    }
                }

                NotificationManager.ACTION_INTERRUPTION_FILTER_CHANGED -> {
                    val nm = c.getSystemService(NotificationManager::class.java)
                    val on = nm.currentInterruptionFilter != NotificationManager.INTERRUPTION_FILTER_ALL
                    toast(if (on) DND_ON else DND_OFF, "Не беспокоить", if (on) "Вкл" else "Выкл", id = "dnd")
                }

                ACTION_VOLUME -> onVolume(c, i)

                ACTION_HOTSPOT -> if (!isInitialStickyBroadcast) {
                    when (i.getIntExtra("wifi_state", -1)) {
                        13 -> toast(HOTSPOT_ON, "Точка доступа", "Вкл", id = "hotspot")
                        11 -> toast(HOTSPOT_OFF, "Точка доступа", "Выкл", id = "hotspot")
                    }
                }

                android.app.AlarmManager.ACTION_NEXT_ALARM_CLOCK_CHANGED -> checkAlarm()

                ACTION_HEADSET_BATTERY -> {
                    val level = i.getIntExtra(EXTRA_HEADSET_BATTERY, -1)
                    if (level < 0) return
                    val name = runCatching {
                        @Suppress("DEPRECATION")
                        (i.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE))?.name
                    }.getOrNull() ?: lastHeadset ?: return
                    val changed = headsetBattery[name] != level
                    headsetBattery[name] = level
                    com.pixelisland.core.IslandRuntime.headsetBattery.value = level
                    // Если тост о подключении ещё висит — дорисуем в нём заряд.
                    if (changed && lastHeadset == name) {
                        val now = SystemClock.elapsedRealtime()
                        if (now - (recentAudio["$name|true"] ?: 0) < 4000) headphonesToast(name)
                    }
                }
            }
        }
    }

    private fun onBattery(c: Context, i: Intent) {
        val level = i.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) * 100 / i.getIntExtra(BatteryManager.EXTRA_SCALE, 100).coerceAtLeast(1)
        val plugged = i.getIntExtra(BatteryManager.EXTRA_PLUGGED, 0)
        val status = i.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
        com.pixelisland.core.IslandRuntime.battery.value = level
        // Этот broadcast приходит очень часто (напряжение, температура) — реагируем только на смену процента/питания.
        val sig = level * 10 + plugged
        if (sig == lastBatterySig && (status != BatteryManager.BATTERY_STATUS_FULL || fullNotified)) return
        lastBatterySig = sig
        // Свои предупреждения о заряде: 20% и 10% (системное приходит не на всех прошивках).
        if (plugged == 0 && settings.lowBattery20 && lastLevel > 0) {
            val crossed = listOf(20, 10).firstOrNull { level <= it && lastLevel > it }
            if (crossed != null) toast(BATTERY_LOW, "Заряд $crossed%", "$level%", level / 100f, id = "lowbat")
        }
        lastLevel = level
        if (plugged != 0) {
            val bm = c.getSystemService(BatteryManager::class.java)
            val left = runCatching { bm.computeChargeTimeRemaining() }.getOrDefault(-1L)
            IslandController.set(ChargingActivity(level, plugged == BatteryManager.BATTERY_PLUGGED_WIRELESS, left))
            if (status == BatteryManager.BATTERY_STATUS_FULL && !fullNotified && lastPlugged != -1) {
                fullNotified = true
                toast(CHARGE_FULL, "Заряжено", "100%", 1f, id = "power")
            }
        } else {
            IslandController.remove("charging")
            fullNotified = false
        }
        lastPlugged = plugged
    }

    private fun battery(c: Context): Pair<Int, Boolean> {
        val i = c.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        val level = c.getSystemService(BatteryManager::class.java)
            .getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)
        val wireless = i?.getIntExtra(BatteryManager.EXTRA_PLUGGED, 0) == BatteryManager.BATTERY_PLUGGED_WIRELESS
        return level to wireless
    }

    private fun toast(t: EventType, title: String, value: String, ring: Float? = null, id: String = t.name) =
        IslandController.showToast(ToastEvent(id, t, title, value, ring))

    // ---------- Примеры для редактора ----------
    fun demo(t: EventType) {
        when (t.kind) {
            EventKind.LIVE -> IslandController.demoLive(t)
            EventKind.CARD -> IslandController.showToast(
                if (t == ROTATE) PeekEvent("rotate:demo", "Поворот экрана", null, "Повернуть экран?", "Нажмите, чтобы включить автоповорот",
                    null, null, null, type = ROTATE, actionLabel = "Включить")
                else if (t == SCREENSHOT) PeekEvent("shot:demo", "Скриншот", null, "Снимок экрана сохранён", "Нажмите, чтобы открыть", null, null, null, type = SCREENSHOT)
                else PeekEvent("peek:demo", "Сообщения", null, "Аня", "Ты уже дома? Я купила тот самый торт 🎂", null, null, null),
                force = true,
            )
            EventKind.TOAST -> {
                val (title, value, ring) = when (t) {
                    CHARGE_START -> Triple("Зарядка", "67%", 0.67f)
                    CHARGE_STOP -> Triple("Отключено", "67%", 0.67f)
                    CHARGE_FULL -> Triple("Заряжено", "100%", 1f)
                    BATTERY_LOW -> Triple("Низкий заряд", "14%", 0.14f)
                    SAVER_ON -> Triple("Экономия", "Вкл", null)
                    SAVER_OFF -> Triple("Экономия", "Выкл", null)
                    WEATHER -> Triple("Ясно", "+3°", null)
                    PAYMENT -> Triple("Monobank", "−245,00 ₴", null)
                    BT_ON -> Triple("Bluetooth", "Вкл", null)
                    BT_OFF -> Triple("Bluetooth", "Выкл", null)
                    HP_ON -> Triple("Pixel Buds Pro 2", "84%", 0.84f)
                    HP_OFF -> Triple("Pixel Buds Pro 2", "Отключены", null)
                    WIFI_ON -> Triple("Wi‑Fi", "Подключено", null)
                    WIFI_OFF -> Triple("Wi‑Fi", "Отключено", null)
                    AIR_ON -> Triple("Режим полёта", "Вкл", null)
                    AIR_OFF -> Triple("Режим полёта", "Выкл", null)
                    RINGER_SOUND -> Triple("Звук", "", null)
                    RINGER_VIBRATE -> Triple("Вибрация", "", null)
                    RINGER_SILENT -> Triple("Без звука", "", null)
                    DND_ON -> Triple("Не беспокоить", "Вкл", null)
                    DND_OFF -> Triple("Не беспокоить", "Выкл", null)
                    TORCH_OFF -> Triple("Фонарик", "Выкл", null)
                    VOLUME -> Triple("Медиа", "60%", 0.6f)
                    VPN_ON -> Triple("VPN", "Подключён", null)
                    VPN_OFF -> Triple("VPN", "Отключён", null)
                    HOTSPOT_ON -> Triple("Точка доступа", "Вкл", null)
                    HOTSPOT_OFF -> Triple("Точка доступа", "Выкл", null)
                    UNLOCK -> Triple("Разблокировано", "", null)
                    NOW_PLAYING -> Triple("Midnight City", "M83", null)
                    NET_LOST -> Triple("Нет сети", "Интернет пропал", null)
                    NET_BACK -> Triple("Сеть вернулась", "Wi‑Fi", null)
                    BRIGHTNESS -> Triple("Яркость", "70%", 0.7f)
                    COPIED -> Triple("Скопировано", "", null)
                    WEATHER_ALERT -> Triple("Дождь через 15 мин", "☂", null)
                    else -> Triple(t.title, "", null)
                }
                IslandController.showToast(ToastEvent("demo:${t.name}", t, title, value, ring), force = true)
            }
        }
    }

    /** Пример для превью в редакторе. */
    fun sample(t: EventType): ToastEvent {
        val e = when (t) {
            HP_ON -> ToastEvent("s", t, "Pixel Buds Pro 2", "84%", 0.84f)
            CHARGE_START, CHARGE_STOP -> ToastEvent("s", t, "Зарядка", "67%", 0.67f)
            BATTERY_LOW -> ToastEvent("s", t, "Низкий заряд", "14%", 0.14f)
            VOLUME -> ToastEvent("s", t, "Медиа", "60%", 0.6f)
            PAYMENT -> ToastEvent("s", t, "Monobank", "−245,00 ₴")
            BRIGHTNESS -> ToastEvent("s", t, "Яркость", "70%", 0.7f)
            NOW_PLAYING -> ToastEvent("s", t, "Midnight City", "M83")
            NET_LOST -> ToastEvent("s", t, "Нет сети", "Интернет пропал")
            NET_BACK -> ToastEvent("s", t, "Сеть вернулась", "Wi‑Fi")
            COPIED -> ToastEvent("s", t, "Скопировано", "")
            WEATHER_ALERT -> ToastEvent("s", t, "Дождь через 15 мин", "☂")
            UNLOCK -> ToastEvent("s", t, "Разблокировано", "")
            else -> ToastEvent("s", t, t.title.substringBefore(" вкл").substringBefore(" выкл"), if (t.name.endsWith("_OFF")) "Выкл" else "Вкл")
        }
        return e
    }
}
