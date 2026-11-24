package com.pixelisland.service

import android.accessibilityservice.AccessibilityService
import android.app.KeyguardManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.res.Configuration
import android.graphics.PixelFormat
import android.graphics.Rect
import android.view.Gravity
import android.view.View
import android.view.WindowInsets
import android.view.WindowManager
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityWindowInfo
import androidx.compose.ui.platform.ComposeView
import com.pixelisland.core.IslandController
import com.pixelisland.core.Presentation
import com.pixelisland.core.isLarge
import com.pixelisland.core.touchable
import com.pixelisland.media.MediaRepository
import com.pixelisland.overlay.OverlayLifecycleOwner
import com.pixelisland.settings.SettingsStore
import com.pixelisland.sources.SystemEvents
import com.pixelisland.ui.IslandDims
import com.pixelisland.ui.IslandGeometry
import com.pixelisland.ui.IslandRoot
import com.pixelisland.core.IslandRuntime
import androidx.compose.ui.unit.dp
import com.pixelisland.ui.settleMs
import com.pixelisland.ui.theme.IslandTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.roundToInt
import kotlinx.coroutines.flow.MutableStateFlow

/**
 * Рисует остров окном TYPE_ACCESSIBILITY_OVERLAY.
 * Окно меняет размер только дважды за переход (перед ростом и после того,
 * как анимация сжатия закончилась), а не на каждом кадре — отсюда плавность.
 */
class IslandAccessibilityService : AccessibilityService() {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private val owner = OverlayLifecycleOwner()
    private val wm by lazy { getSystemService(WindowManager::class.java) }

    private var view: ComposeView? = null
    private var probe: View? = null
    private val geometry = MutableStateFlow(IslandGeometry.Fallback)
    private val render = MutableStateFlow<Presentation>(Presentation.Hidden)

    // Окно отрисовки НИКОГДА не двигается и не меняет размер — поэтому остров не может «скакать».
    // Касания по свёрнутой пилюле ловит отдельное невидимое окно ровно над ней.
    private var touchView: ComposeView? = null
    private var visualTouchable = false      // раскрытый вид / карточка — касания принимает само окно отрисовки
    private var touchActive = false          // свёрнутый вид — касания ловит окно касаний
    private var lastParams: String? = null
    private var lastTouchParams: String? = null
    private var blurNow = false
    private var focusable = false

    // Причины скрытия
    private var shadeOpen = false
    private var fullscreen = false
    private var sawStatusBar = false

    override fun onServiceConnected() {
        SettingsStore.init(this)
        com.pixelisland.ui.i18n.I18n.init(this)
        com.pixelisland.core.EventLog.init(this)
        com.pixelisland.core.OwnTimers.init(this)
        com.pixelisland.core.Stats.init(this)
        com.pixelisland.core.Automations.init(this)
        com.pixelisland.core.Profiles.init(this)
        com.pixelisland.core.Watchdog.schedule(this)
        instance = this
        IslandRuntime.serviceAlive.value = true
        MediaRepository.attach(applicationContext)
        SystemEvents.start(applicationContext)
        com.pixelisland.sources.Extras.start(applicationContext)
        owner.start()
        com.pixelisland.core.SystemPrefs.refresh(this)
        geometry.value = computeGeometry()

        val cv = ComposeView(this).apply {
            owner.attachTo(this)
            // Не гоняем оверлей на 120 Гц: 60 Гц хватает и для анимаций, и экономит GPU.
            if (android.os.Build.VERSION.SDK_INT >= 35) requestedFrameRate = View.REQUESTED_FRAME_RATE_CATEGORY_NORMAL
            setContent { IslandTheme { IslandRoot(geometry, render) } }
        }
        view = cv
        wm.addView(cv, params())
        val tv = ComposeView(this).apply {
            owner.attachTo(this)
            setContent { IslandTheme { com.pixelisland.ui.TouchLayer(render, geometry) } }
        }
        touchView = tv
        tv.setOnDragListener(dragListener)
        wm.addView(tv, touchParams())
        addProbe()

        scope.launch { IslandController.presentation.collect { apply(it) } }
        scope.launch { IslandController.replying.collect { focusable = it; updateWindow() } }
        scope.launch { IslandRuntime.compactWidthDp.collect { updateWindow() } }
        scope.launch { IslandController.visible.collect { updateWindow() } }
        scope.launch {
            SettingsStore.flow.collect {
                val g = computeGeometry()
                if (g != geometry.value) geometry.value = g
                updateWindow(); refreshHidden()
                SystemEvents.updateRotationWatch()   // включили/выключили «Поворот экрана» — сразу включить/выключить датчик
            }
        }

        registerReceiver(screenReceiver, IntentFilter().apply {
            addAction(Intent.ACTION_SCREEN_OFF); addAction(Intent.ACTION_USER_PRESENT); addAction(Intent.ACTION_SCREEN_ON)
        }, Context.RECEIVER_EXPORTED)
        refreshHidden()
    }

    private var lastExpanded = false
    private var windowBig = false
    private var shrinkJob: Job? = null

    private fun apply(p: Presentation) {
        val exp = p is Presentation.Expanded || p is Presentation.Peek
        if (exp != lastExpanded) {
            lastExpanded = exp
            com.pixelisland.core.Sounds.play(this, if (exp) com.pixelisland.core.Sounds.Kind.EXPAND else com.pixelisland.core.Sounds.Kind.COLLAPSE)
        }
        blurNow = exp
        // Окно рисования высокое только пока что-то крупное на экране. Верх и X не меняются,
        // поэтому смена высоты не сдвигает остров — зато системе не нужно каждый кадр
        // смешивать прозрачный прямоугольник в треть экрана.
        shrinkJob?.cancel()
        if (p.isLarge) windowBig = true
        else shrinkJob = scope.launch {
            delay(SettingsStore.value.settleMs() + 200)
            windowBig = false
            updateWindow()
        }
        visualTouchable = exp && p.touchable
        touchActive = p is Presentation.Compact
        render.value = p          // окно не меняется — рисуем сразу, без ожиданий и гонок
        updateWindow()
    }

    private fun dims() = IslandDims(geometry.value, SettingsStore.value, resources.displayMetrics.density)

    private fun params(): WindowManager.LayoutParams {
        val d = dims()
        val g = geometry.value
        var flags = (if (focusable) 0 else WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE) or
            WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
            WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS or
            WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED
        if (!visualTouchable) flags = flags or WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
        val blur = blurNow && SettingsStore.value.blurBehind
        if (blur) flags = flags or WindowManager.LayoutParams.FLAG_BLUR_BEHIND or WindowManager.LayoutParams.FLAG_DIM_BEHIND
        return WindowManager.LayoutParams(
            g.screenWidth,
            if (windowBig) d.largeWindowH else d.compactWindowH,
            WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
            flags,
            PixelFormat.TRANSLUCENT,
        ).apply {
            gravity = Gravity.TOP or Gravity.LEFT
            x = 0
            y = d.windowYPx
            layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_ALWAYS
            title = "PixelIsland"
            if (focusable) softInputMode = WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING or WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE
            if (blur) {
                blurBehindRadius = (20 * resources.displayMetrics.density).roundToInt()
                dimAmount = 0.12f
            }
        }
    }

    /** Окно касаний — ровно над свёрнутой пилюлей (и кружком второй активности). */
    private fun touchParams(): WindowManager.LayoutParams {
        val d = dims()
        val density = resources.displayMetrics.density
        val pillW = maxOf(IslandRuntime.compactWidthDp.value, d.compactW.value)
        val bubble = if (SettingsStore.value.splitPills && IslandController.visible.value.size > 1) (d.compactH.value + d.bubbleGap.value) else 0f
        val slop = 4f
        val wDp = pillW + 2 * (bubble + slop)
        val wPx = (wDp * density).roundToInt().coerceAtMost(geometry.value.screenWidth)
        val hPx = ((d.compactH.value + 2 * slop) * density).roundToInt()
        var flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
            WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
            WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        if (!touchActive) flags = flags or WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
        return WindowManager.LayoutParams(
            if (touchActive) wPx else 1, if (touchActive) hPx else 1,
            WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY, flags, PixelFormat.TRANSPARENT,
        ).apply {
            gravity = Gravity.TOP or Gravity.LEFT
            x = d.centerXPx - wPx / 2
            y = d.islandTopPx - (slop * density).roundToInt()
            layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_ALWAYS
            title = "PixelIslandTouch"
        }
    }

    private fun updateWindow() {
        val v = view ?: return
        val p = params()
        val sig = "${p.x},${p.y},${p.width},${p.height},${p.flags},${p.blurBehindRadius}"
        IslandRuntime.windowLarge.value = visualTouchable
        if (sig != lastParams) { lastParams = sig; runCatching { wm.updateViewLayout(v, p) } }
        val tv = touchView ?: return
        val tp = touchParams()
        val tsig = "${tp.x},${tp.y},${tp.width},${tp.height},${tp.flags}"
        if (tsig != lastTouchParams) { lastTouchParams = tsig; runCatching { wm.updateViewLayout(tv, tp) } }
    }

    // ---------- Скрытие: альбомная ориентация, шторка, полный экран, блокировка ----------

    private fun refreshHidden() {
        val s = SettingsStore.value
        val landscape = resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
        val locked = getSystemService(KeyguardManager::class.java).isKeyguardLocked
        IslandController.setHidden("landscape", s.hideLandscape && landscape)
        IslandController.setHidden("fullscreen", s.hideFullscreen && fullscreen)
        IslandController.setHidden("lock", locked && !s.showOnLockscreen)
        IslandController.setHidden("shade", s.hideShade && shadeOpen && !locked)
    }

    // ---------- Полка: перетаскивание на остров ----------
    private val dragListener = View.OnDragListener { v, e ->
        when (e.action) {
            android.view.DragEvent.ACTION_DRAG_STARTED -> {
                val d = e.clipDescription ?: return@OnDragListener false
                SettingsStore.value.on(com.pixelisland.settings.EventType.SHELF) && d.mimeTypeCount > 0
            }
            android.view.DragEvent.ACTION_DRAG_ENTERED -> {
                com.pixelisland.core.Shelf.dragging.value = true
                com.pixelisland.core.Haptics.play(v, com.pixelisland.core.Haptics.Kind.TICK)
                IslandRuntime.glow.value = 0xFFFFB25C.toInt() to System.currentTimeMillis()
                true
            }
            android.view.DragEvent.ACTION_DRAG_EXITED -> { com.pixelisland.core.Shelf.dragging.value = false; true }
            android.view.DragEvent.ACTION_DROP -> {
                com.pixelisland.core.Shelf.dragging.value = false
                e.clipData?.let { com.pixelisland.core.Shelf.addClip(applicationContext, it) }
                com.pixelisland.core.Haptics.play(v, com.pixelisland.core.Haptics.Kind.LONG)
                true
            }
            android.view.DragEvent.ACTION_DRAG_ENDED -> { com.pixelisland.core.Shelf.dragging.value = false; true }
            else -> true
        }
    }

    private val copyWords = Regex("^(копировать|скопировать|копіювати|скопіювати|copy|копировать ссылку|копіювати посилання|copy link|copy text|копировать текст)$", RegexOption.IGNORE_CASE)

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null) return
        val t = event.eventType
        if (t == AccessibilityEvent.TYPE_VIEW_CLICKED) {
            val label = (event.text.joinToString(" ").ifBlank { event.contentDescription?.toString().orEmpty() }).trim()
            if (label.length < 40 && copyWords.matches(label)) SystemEvents.copied()
            return
        }
        if (t == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) event.packageName?.toString()?.let(com.pixelisland.sources.Extras::onForegroundApp)
        if (t != AccessibilityEvent.TYPE_WINDOWS_CHANGED && t != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) return
        // Таких событий бывает десятки в секунду (анимации окон) — проверяем шторку не чаще раза в 150 мс.
        if (shadeCheckPending) return
        shadeCheckPending = true
        view?.postDelayed({ shadeCheckPending = false; checkShade() }, 150) ?: run { shadeCheckPending = false }
    }

    private var shadeCheckPending = false

    private fun checkShade() {
        val screenH = resources.displayMetrics.heightPixels
        val r = Rect()
        val open = runCatching {
            windows.any { w ->
                w.type == AccessibilityWindowInfo.TYPE_SYSTEM && run { w.getBoundsInScreen(r); r.height() > screenH / 2 }
            }
        }.getOrDefault(false)
        if (open != shadeOpen) { shadeOpen = open; refreshHidden() }
    }

    /** Невидимая полоска в 1px — по её инсетам понимаем, скрыт ли статус-бар (полноэкранный режим). */
    private fun addProbe() {
        val p = View(this)
        p.setOnApplyWindowInsetsListener { _, insets ->
            val visible = insets.isVisible(WindowInsets.Type.statusBars())
            if (visible) sawStatusBar = true
            val fs = sawStatusBar && !visible
            if (fs != fullscreen) { fullscreen = fs; refreshHidden() }
            insets
        }
        val lp = WindowManager.LayoutParams(
            1, WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
            PixelFormat.TRANSPARENT,
        ).apply { gravity = Gravity.TOP or Gravity.RIGHT }
        runCatching { wm.addView(p, lp); probe = p }
    }

    private val screenReceiver = object : BroadcastReceiver() {
        override fun onReceive(c: Context, i: Intent) { com.pixelisland.core.SystemPrefs.refresh(c); refreshHidden() }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        geometry.value = computeGeometry()
        com.pixelisland.core.SystemPrefs.refresh(this)
        lastParams = null; lastTouchParams = null
        updateWindow()
        refreshHidden()
    }

    private fun computeGeometry(): IslandGeometry {
        val metrics = wm.currentWindowMetrics
        val screenW = metrics.bounds.width()
        val d = resources.displayMetrics.density
        val rect = metrics.windowInsets.displayCutout?.boundingRectTop?.takeIf { !it.isEmpty }
        val auto = if (rect != null) {
            IslandGeometry(rect.centerX(), rect.top, rect.width(), rect.height(), screenW)
        } else {
            val size = (26 * d).roundToInt()
            IslandGeometry(screenW / 2, (8 * d).roundToInt(), size, size, screenW)
        }
        IslandRuntime.autoGeometry.value = auto
        val s = SettingsStore.value
        val portrait = resources.configuration.orientation != Configuration.ORIENTATION_LANDSCAPE
        val g = if (s.cutoutManual && s.cutW > 0 && portrait) IslandGeometry(s.cutX, s.cutY, s.cutW, s.cutH, screenW) else auto
        IslandRuntime.geometry.value = g
        return g
    }

    override fun onInterrupt() = Unit

    companion object {
        @Volatile var instance: IslandAccessibilityService? = null
        /** Глобальное действие системы: шторка, скриншот и т. п. */
        fun global(action: Int): Boolean = instance?.performGlobalAction(action) ?: false
    }

    override fun onDestroy() {
        instance = null
        IslandRuntime.serviceAlive.value = false
        scope.cancel()
        runCatching { unregisterReceiver(screenReceiver) }
        SystemEvents.stop()
        com.pixelisland.sources.Extras.stop()
        view?.let { runCatching { wm.removeView(it) } }
        probe?.let { runCatching { wm.removeView(it) } }
        touchView?.let { runCatching { wm.removeView(it) } }
        view = null; probe = null; touchView = null
        owner.stop()
        super.onDestroy()
    }
}
