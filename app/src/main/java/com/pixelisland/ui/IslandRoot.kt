package com.pixelisland.ui

import androidx.compose.foundation.Canvas

import android.content.Context
import android.content.Intent
import android.view.HapticFeedbackConstants
import android.view.View
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.Animatable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.pixelisland.core.ChargingActivity
import com.pixelisland.core.IslandController
import com.pixelisland.core.LiveActivity
import com.pixelisland.core.MusicActivity
import com.pixelisland.core.OngoingActivity
import com.pixelisland.core.Presentation
import com.pixelisland.core.PreviewActivity
import com.pixelisland.core.TorchActivity
import com.pixelisland.core.launchPackage
import com.pixelisland.core.sendPending
import com.pixelisland.media.MediaRepository
import com.pixelisland.settings.AppearStyle
import com.pixelisland.settings.GestureAction
import com.pixelisland.settings.IslandSettings
import com.pixelisland.settings.SettingsStore
import com.pixelisland.ui.content.ExpandedContent
import com.pixelisland.ui.content.CompactContent
import com.pixelisland.ui.content.PeekContent
import com.pixelisland.ui.content.ToastContent
import kotlinx.coroutines.flow.StateFlow
import kotlin.math.abs
import kotlin.math.roundToInt

private fun Presentation.key(): String = when (this) {
    Presentation.Hidden -> "hidden"
    is Presentation.Compact -> "c:" + (activity?.id ?: "idle")
    is Presentation.Expanded -> "e:" + activity.id
    is Presentation.Toast -> "t:" + event.id
    is Presentation.Peek -> "p:" + event.id
    Presentation.Calibrate -> "cal"
}

/**
 * Корень оверлея. Окно бывает двух размеров (компактное и широкое), но вся анимация
 * считается в абсолютных координатах экрана, поэтому смена окна не даёт скачков.
 */
@Composable
fun IslandRoot(geometryFlow: StateFlow<IslandGeometry>, renderFlow: StateFlow<Presentation>) {
    val g by geometryFlow.collectAsState()
    val p by renderFlow.collectAsState()
    val s by SettingsStore.flow.collectAsState()
    val d = LocalDensity.current
    val dims = remember(g, s, d.density) { IslandDims(g, s, d.density) }
    val colors = islandColors(s)

    CompositionLocalProvider(LocalIslandColors provides colors) {
        val bounds by com.pixelisland.core.IslandRuntime.showBounds.collectAsState()
        Box(Modifier.fillMaxSize().then(if (bounds) Modifier.border(1.dp, Color.Red) else Modifier)) {
            // Окно отрисовки всегда на всю ширину экрана и стоит на месте — координаты абсолютные.
            val originX = 0
            val outside = p is Presentation.Expanded || p is Presentation.Peek
            Box(
                Modifier.fillMaxSize().then(
                    if (outside) Modifier.pointerInput(p.key()) {
                        detectTapGestures {
                            if (p is Presentation.Peek) IslandController.clearToast() else IslandController.collapse()
                        }
                    } else Modifier,
                ),
            )
            NotifyGlow(dims)
            if (p == Presentation.Calibrate) CalibrationRing(dims, originX) else {
                IslandSurface(p, dims, s, originX)
                if (s.splitPills) SecondBubble(p, dims, originX)
            }
        }
    }
}

@Composable
private fun IslandSurface(p: Presentation, dims: IslandDims, s: IslandSettings, originX: Int) {
    val context = LocalContext.current
    val view = LocalView.current
    val d = LocalDensity.current
    val colors = LocalIslandColors.current

    // Последнее видимое состояние — чтобы было что рисовать во время исчезновения.
    var lastShown by remember { mutableStateOf<Presentation?>(null) }
    if (p != Presentation.Hidden) lastShown = p
    val shown = p != Presentation.Hidden
    val content = lastShown

    val replying by IslandController.replying.collectAsState()
    val highContrast by com.pixelisland.core.IslandRuntime.highContrast.collectAsState()
    // «Часть камеры»: пустая пилюля чуть больше самого выреза.
    val blend = p is Presentation.Compact && p.activity == null && s.cameraBlend &&
        s.idleContent == com.pixelisland.settings.IdleContent.EMPTY
    // Целевые размеры.
    val (tw, th, tc) = when (p) {
        is Presentation.Compact -> if (blend) Triple(dims.cutoutW + 4.dp, dims.cutoutH + 4.dp, (dims.cutoutH + 4.dp) / 2)
            else Triple(com.pixelisland.ui.content.pillWidth(p, dims), dims.compactH, dims.compactCorner)
        is Presentation.Expanded -> Triple(expandedWidth(p.activity, dims), expandedHeightOf(p.activity, dims), expandedCornerOf(p.activity, dims))
        is Presentation.Toast -> Triple(com.pixelisland.ui.content.pillWidth(p, dims), dims.compactH, dims.compactCorner)
        is Presentation.Peek -> Triple(dims.expandedW, dims.peekH + if (replying) 56.dp else 0.dp, dims.expandedCorner)
        Presentation.Hidden, Presentation.Calibrate -> when (s.appearStyle) {
            AppearStyle.FROM_CAMERA -> Triple(dims.cutoutW, dims.cutoutH, dims.cutoutH / 2)
            else -> Triple(dims.compactW, dims.compactH, dims.compactCorner)
        }
    }
    val screenCenter = dims.g.screenWidth / 2
    val targetCenter = when (p) {
        is Presentation.Expanded, is Presentation.Peek -> screenCenter
        else -> dims.centerXPx
    }

    // Единый переход: размер, углы, положение и прозрачность считаются из ОДНОГО прогресса,
    // поэтому форма меняется синхронно и одинаково в обе стороны.
    val padPx = with(d) { dims.pad.toPx() }
    val target = Geo(
        tw.value, th.value, tc.value, targetCenter.toFloat(),
        when {
            !shown && s.appearStyle == AppearStyle.FROM_CAMERA -> padPx   // прячемся ровно в камеру
            blend -> padPx - with(d) { 2.dp.toPx() }
            else -> 0f
        },
    )
    var from by remember { mutableStateOf(target) }
    var to by remember { mutableStateOf(target) }
    var wasShown by remember { mutableStateOf(shown) }
    var appearing by remember { mutableStateOf(true) } // текущий переход — появление/скрытие или смена формы
    val prog = remember { Animatable(1f) }
    LaunchedEffect(target, shown) {
        if (target == to && shown == wasShown) return@LaunchedEffect
        // Стартуем из видимого положения, но без «перелёта» по позиции: иначе частые прерывания
        // (громкость, таймер) накапливали смещение, и остров «уплывал» по экрану.
        val cur = lerpStable(from, to, prog.value)
        appearing = shown != wasShown
        wasShown = shown
        from = cur; to = target
        prog.snapTo(0f)
        val growing = target.w * target.h > cur.w * cur.h + 1f
        prog.animateTo(1f, if (growing && shown) s.motion() else s.motionNoBounce())
    }
    if (p is Presentation.Compact) {
        val wNow = tw.value
        androidx.compose.runtime.SideEffect { com.pixelisland.core.IslandRuntime.compactWidthDp.value = wNow }
    }
    val pr = prog.value
    val g = lerpStable(from, to, pr)
    val w = g.w.coerceAtLeast(0f).dp
    val h = g.h.coerceAtLeast(0f).dp
    val corner = g.c.coerceAtLeast(0f).dp
    val center = g.x
    val pc = pr.coerceIn(0f, 1f)
    val style = s.appearStyle
    val alpha = when {
        // Скрытый остров, у которого сменилась геометрия (поворот экрана, калибровка), должен оставаться невидимым.
        // Раньше здесь была 1 — и в альбомной ориентации посередине сверху появлялась чёрная точка.
        !shown && !appearing -> 0f
        !appearing -> 1f
        shown -> if (style == AppearStyle.FROM_CAMERA) (pc * 5f).coerceAtMost(1f) else pc
        else -> if (style == AppearStyle.FROM_CAMERA) (1f - (pc - 0.8f) / 0.2f).coerceIn(0f, 1f) else 1f - pc
    }
    val scale = if (appearing && style == AppearStyle.POP) (if (shown) 0.5f + 0.5f * pc else 1f - 0.5f * pc) else 1f
    val dropPx = with(d) { (dims.compactH + 40.dp).toPx() }
    val drop = if (appearing && style == AppearStyle.DROP) (if (shown) -dropPx * (1f - pc) else -dropPx * pc) else 0f
    // Содержимое проявляется во второй половине появления и гаснет в первой трети скрытия — симметрично.
    val contentAlpha = when {
        !shown && !appearing -> 0f
        !appearing -> 1f
        shown -> ((pc - 0.35f) / 0.45f).coerceIn(0f, 1f)
        else -> (1f - pc / 0.35f).coerceIn(0f, 1f)
    }
    val yOff = g.y

    if (content == null) return
    // Полностью скрыт — вообще ничего не рисуем (и не тратим кадры).
    if (!shown && alpha <= 0.001f) return

    val shape = RoundedCornerShape(corner)
    val shimmer = remember { Animatable(0f) }
    LaunchedEffect(p.key(), s.gradient) {
        if (s.gradient != com.pixelisland.settings.GradientStyle.ANIMATED) return@LaunchedEffect
        repeat(3) {
            shimmer.snapTo(0f)
            shimmer.animateTo(1f, tween(3200, easing = androidx.compose.animation.core.LinearEasing))
        }
    }
    val wPx = with(d) { w.roundToPx() }
    val pState by rememberUpdatedState(p)
    // Живой фон: анимируется ограниченное время (раскрыт — до минуты, свёрнут — 8 с), потом замирает.
    val fxT = remember { Animatable(0f) }
    val fxExpanded = p is Presentation.Expanded
    LaunchedEffect(p.key(), s.pillFx) {
        if (s.pillFx == com.pixelisland.settings.PillFx.NONE || com.pixelisland.core.IslandRuntime.reduceMotion.value) return@LaunchedEffect
        val ms = if (fxExpanded) 60_000 else 8_000
        fxT.snapTo(0f)
        fxT.animateTo(ms / 1000f, tween(ms, easing = androidx.compose.animation.core.LinearEasing))
    }
    val beatOn = (activityOf(p) as? MusicActivity)?.media?.isPlaying == true || activityOf(p) is PreviewActivity

    Box(
        Modifier
            .offset { IntOffset((center - originX - wPx / 2f).roundToInt(), dims.contentTopPx + yOff.roundToInt()) }
            .size(w.coerceAtLeast(0.dp), h.coerceAtLeast(0.dp))
            .drawWithContent { com.pixelisland.core.IslandRuntime.frames++; drawContent() }
            .graphicsLayer {
                this.alpha = alpha
                scaleX = scale; scaleY = scale
                translationY = drop
                clip = true
                this.shape = shape
                if (s.glow || s.shadow) {
                    shadowElevation = (if (s.glow) 18.dp else 10.dp).toPx()
                    val sc = if (s.glow) colors.accent else Color.Black
                    ambientShadowColor = sc; spotShadowColor = sc
                }
            }
            .then(
                if (colors.brush != null) Modifier.background(colors.brush, shape)
                else Modifier.background(colors.bg.copy(alpha = colors.bg.alpha * ((p as? Presentation.Expanded)?.let { s.style(it.activity.type).exOpacity } ?: 1f)), shape),
            )
            .then(if (s.gradient == com.pixelisland.settings.GradientStyle.ANIMATED) Modifier.drawBehind {
                // «Переливы»: блик проходит по пилюле ~10 с после появления и замирает (читается только при отрисовке).
                val t = shimmer.value
                val w0 = size.width
                drawRect(
                    Brush.linearGradient(
                        listOf(Color.Transparent, colors.accent.copy(alpha = 0.35f), Color(s.bgCustom2).copy(alpha = 0.3f), Color.Transparent),
                        start = androidx.compose.ui.geometry.Offset(-w0 + 2 * w0 * t, 0f),
                        end = androidx.compose.ui.geometry.Offset(w0 * t, size.height),
                    ),
                )
            } else Modifier)
            .then(if (s.pillFx != com.pixelisland.settings.PillFx.NONE) Modifier.drawBehind {
                drawPillFx(s.pillFx, fxT.value, colors.accent, Color(s.bgCustom2), beatOn)
            } else Modifier)
            .then(if (s.outline || highContrast) Modifier.border(1.dp, colors.accent.copy(alpha = 0.5f), shape) else Modifier)
            .pointerInput(s.gDoubleTap, s.gIdleDouble, p is Presentation.Compact && p.activity == null) {
                detectTapGestures(
                    onTap = { onTap(pState, context, view) },
                    onDoubleTap = if (doubleTapAction(pState) == GestureAction.NONE) null
                    else { _ -> gesture(doubleTapAction(pState), pState, context, view, 1) },
                    onLongPress = { if (!musicVolumeMode(pState)) onLongPress(pState, context, view) },
                )
            }
            // Удержание + вертикальный свайп по свёрнутой музыке — громкость; без движения — обычное удержание.
            .pointerInput(Unit) {
                var acc = 0f; var moved = 0f
                detectDragGesturesAfterLongPress(
                    onDragStart = { acc = 0f; moved = 0f },
                    onDrag = { c, d ->
                        if (!musicVolumeMode(pState)) return@detectDragGesturesAfterLongPress
                        c.consume(); acc += d.y; moved += kotlin.math.abs(d.y)
                        while (acc <= -28f) { acc += 28f; stepVolume(context, view, +1) }
                        while (acc >= 28f) { acc -= 28f; stepVolume(context, view, -1) }
                    },
                    onDragEnd = { if (musicVolumeMode(pState) && moved < 12f) onLongPress(pState, context, view) },
                )
            }
            .pointerInput(Unit) {
                var dx = 0f; var dy = 0f
                detectDragGestures(
                    onDragStart = { dx = 0f; dy = 0f },
                    onDrag = { c, delta -> c.consume(); dx += delta.x; dy += delta.y },
                    onDragEnd = { onSwipe(pState, dx, dy, view) },
                )
            },
    ) {
        // Содержимое кладём в полный целевой размер и «открываем» анимацией обрезки —
        // текст не перестраивается на каждом кадре, поэтому нет дёрганья.
        val cache = remember { mutableStateMapOf<String, Presentation>() }
        val key = content.key()
        cache[key] = content
        // Храним только текущее и предыдущее состояние (для перехода) — иначе кеш копил бы картинки.
        var prevKey by remember { mutableStateOf(key) }
        if (prevKey != key) {
            val keep = setOf(key, prevKey)
            cache.keys.filter { it !in keep }.forEach { cache.remove(it) }
            prevKey = key
        }
        Crossfade(targetState = key, animationSpec = tween(s.fadeMs()), label = "content", modifier = Modifier.graphicsLayer { this.alpha = contentAlpha }) { k ->
            val pres = cache[k] ?: return@Crossfade
            val (cw, ch) = sizeOf(pres, dims)
            Box(
                Modifier.fillMaxSize().wrapContentSize(Alignment.TopCenter, unbounded = true).then(
                    // Раскрытый вид сам определяет высоту по содержимому.
                    if (pres is Presentation.Expanded) Modifier.requiredWidth(cw).wrapContentHeight(Alignment.Top, unbounded = true)
                    else Modifier.requiredSize(cw, ch),
                ),
            ) {
                when (pres) {
                    is Presentation.Compact -> CompactContent(pres.activity, pres.index, pres.count, dims, cw)
                    is Presentation.Expanded -> ExpandedContent(pres.activity, pres.index, pres.count, dims) { h ->
                        if (expandedHeights[pres.activity.id] != h.value) expandedHeights[pres.activity.id] = h.value
                    }
                    is Presentation.Toast -> ToastContent(pres.event, dims, cw)
                    is Presentation.Peek -> PeekContent(pres.event, dims)
                    Presentation.Hidden, Presentation.Calibrate -> Unit
                }
            }
        }
    }
}

/** Рисунок живого фона по времени t (секунды). Только отрисовка — без перекомпоновки. */
internal fun androidx.compose.ui.graphics.drawscope.DrawScope.drawPillFx(
    fx: com.pixelisland.settings.PillFx, t: Float, accent: Color, second: Color, beat: Boolean,
) {
    val w = size.width; val h = size.height
    when (fx) {
        com.pixelisland.settings.PillFx.NONE -> Unit
        com.pixelisland.settings.PillFx.PARTICLES -> repeat(16) { i ->
            val seed = (i * 0.618f) % 1f
            val x = (seed * w + kotlin.math.sin(t * 0.8f + i) * 12f).mod(w)
            val y = h - ((t * (14f + i * 3f) + seed * h) % (h + 8f))
            drawCircle(accent.copy(alpha = 0.18f + 0.2f * seed), radius = 1.5f + 2.5f * seed, center = androidx.compose.ui.geometry.Offset(x, y))
        }
        com.pixelisland.settings.PillFx.WAVE -> {
            val path = androidx.compose.ui.graphics.Path()
            path.moveTo(0f, h)
            var x = 0f
            while (x <= w) {
                path.lineTo(x, h * 0.62f + kotlin.math.sin(x / w * 6.283f * 1.5f + t * 2.2f) * h * 0.12f)
                x += 6f
            }
            path.lineTo(w, h); path.close()
            drawPath(path, accent.copy(alpha = 0.22f))
        }
        com.pixelisland.settings.PillFx.AURORA -> {
            val cx1 = w * (0.5f + 0.4f * kotlin.math.sin(t * 0.6f))
            val cx2 = w * (0.5f + 0.4f * kotlin.math.cos(t * 0.45f))
            drawRect(Brush.radialGradient(listOf(accent.copy(alpha = 0.35f), Color.Transparent), androidx.compose.ui.geometry.Offset(cx1, h * 0.3f), w * 0.45f))
            drawRect(Brush.radialGradient(listOf(second.copy(alpha = 0.3f), Color.Transparent), androidx.compose.ui.geometry.Offset(cx2, h * 0.8f), w * 0.4f))
        }
        com.pixelisland.settings.PillFx.BEAT -> if (beat) {
            val k = kotlin.math.abs(kotlin.math.sin(t * 3.4f)) * (0.6f + 0.4f * kotlin.math.abs(kotlin.math.sin(t * 1.3f)))
            drawRect(Brush.horizontalGradient(listOf(accent.copy(alpha = 0.32f * k), Color.Transparent, accent.copy(alpha = 0.32f * k))))
        }
    }
}

/** Свечение вокруг камеры при новом уведомлении: два мягких импульса и тишина. */
@Composable
private fun NotifyGlow(dims: IslandDims) {
    val g by com.pixelisland.core.IslandRuntime.glow.collectAsState()
    val a = remember { Animatable(0f) }
    val cur = g ?: return
    LaunchedEffect(cur) {
        if (System.currentTimeMillis() - cur.second > 3000) return@LaunchedEffect
        repeat(2) {
            a.animateTo(1f, tween(420))
            a.animateTo(0f, tween(700))
        }
    }
    val color = Color(cur.first)
    val d = LocalDensity.current
    Canvas(Modifier.fillMaxSize()) {
        val k = a.value
        if (k <= 0.01f) return@Canvas
        // Ровно вокруг камеры (без пользовательского сдвига острова).
        val cx = dims.g.centerX.toFloat()
        val cy = (dims.g.top - dims.windowYPx).toFloat() + with(d) { dims.cutoutH.toPx() } / 2f
        val r = with(d) { (maxOf(dims.cutoutW, dims.cutoutH) / 2 + 5.dp).toPx() }
        for (i in 4 downTo 1) {
            drawCircle(color.copy(alpha = 0.16f * k / i), radius = r + i * 3.dp.toPx() * k, center = androidx.compose.ui.geometry.Offset(cx, cy),
                style = androidx.compose.ui.graphics.drawscope.Stroke(width = 4.dp.toPx() * i))
        }
        drawCircle(color.copy(alpha = 0.9f * k), radius = r, center = androidx.compose.ui.geometry.Offset(cx, cy),
            style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2.dp.toPx()))
    }
}

/** Геометрия острова: ширина/высота/угол в dp, центр X и сдвиг Y в px. */
private data class Geo(val w: Float, val h: Float, val c: Float, val x: Float, val y: Float)

/** Размер может слегка перелетать (отскок), а положение — никогда: оно всегда между стартом и целью. */
private fun lerpStable(a: Geo, b: Geo, t: Float): Geo {
    val ts = t.coerceIn(0f, 1.2f)
    val tp = t.coerceIn(0f, 1f)
    return Geo(
        a.w + (b.w - a.w) * ts, a.h + (b.h - a.h) * ts, a.c + (b.c - a.c) * ts,
        a.x + (b.x - a.x) * tp, a.y + (b.y - a.y) * tp,
    )
}

@Composable
private fun sizeOf(p: Presentation, dims: IslandDims): Pair<Dp, Dp> {
    val replying by IslandController.replying.collectAsState()
    return when (p) {
    is Presentation.Compact -> com.pixelisland.ui.content.pillWidth(p, dims) to dims.compactH
    is Presentation.Expanded -> expandedWidth(p.activity, dims) to expandedHeightOf(p.activity, dims)
    is Presentation.Toast -> com.pixelisland.ui.content.pillWidth(p, dims) to dims.compactH
    is Presentation.Peek -> dims.expandedW to dims.peekH + if (replying) 56.dp else 0.dp
    Presentation.Hidden, Presentation.Calibrate -> dims.compactW to dims.compactH
    }
}

fun haptic(view: View, strong: Boolean = false) =
    com.pixelisland.core.Haptics.play(view, if (strong) com.pixelisland.core.Haptics.Kind.LONG else com.pixelisland.core.Haptics.Kind.TAP)

private fun hapticKind(view: View, k: com.pixelisland.core.Haptics.Kind) = com.pixelisland.core.Haptics.play(view, k)

private fun activityOf(p: Presentation) = when (p) {
    is Presentation.Compact -> p.activity
    is Presentation.Expanded -> p.activity
    else -> null
}

/** Выполнить действие жеста из настроек. */
private fun gesture(action: GestureAction, p: Presentation, ctx: Context, view: View, dir: Int) {
    if (action == GestureAction.NONE) return
    if (globalGesture(action, ctx, view)) return
    val a = activityOf(p) ?: return
    if (action != GestureAction.EXPAND) haptic(view, strong = action == GestureAction.OPEN_APP)
    val music = a is MusicActivity
    when (action) {
        GestureAction.EXPAND -> { hapticKind(view, com.pixelisland.core.Haptics.Kind.EXPAND); IslandController.expand(a.id) }
        GestureAction.OPEN_APP -> { IslandController.collapse(); open(a, ctx) }
        GestureAction.DISMISS -> IslandController.dismiss(a)
        GestureAction.SWITCH -> IslandController.cycle(dir)
        GestureAction.PLAY_PAUSE -> if (music) MediaRepository.playPause() else if (a is PreviewActivity) IslandController.previewPlayPause()
        GestureAction.NEXT_TRACK -> if (music) MediaRepository.next() else if (a is PreviewActivity) IslandController.previewSkip(1)
        GestureAction.PREV_TRACK -> if (music) MediaRepository.previous() else if (a is PreviewActivity) IslandController.previewSkip(-1)
        else -> Unit
    }
}

/** Действия, которым не нужна активность: стопка, шторка, полка, фонарик и т. п. */
private fun globalGesture(action: GestureAction, ctx: Context, view: View): Boolean {
    val svc = com.pixelisland.service.IslandAccessibilityService
    when (action) {
        GestureAction.STACK -> { hapticKind(view, com.pixelisland.core.Haptics.Kind.EXPAND); IslandController.expand("stack") }
        GestureAction.QUICK -> { hapticKind(view, com.pixelisland.core.Haptics.Kind.EXPAND); IslandController.expand("quick") }
        GestureAction.SHELF -> { hapticKind(view, com.pixelisland.core.Haptics.Kind.EXPAND); IslandController.expand("shelf") }
        GestureAction.SHADE -> { haptic(view); svc.global(android.accessibilityservice.AccessibilityService.GLOBAL_ACTION_NOTIFICATIONS) }
        GestureAction.HIDE_10 -> { hapticKind(view, com.pixelisland.core.Haptics.Kind.COLLAPSE); IslandController.snooze(10) }
        GestureAction.TORCH -> { haptic(view); com.pixelisland.sources.SystemEvents.toggleTorch() }
        GestureAction.CAMERA -> { haptic(view); runCatching {
            ctx.startActivity(Intent(android.provider.MediaStore.INTENT_ACTION_STILL_IMAGE_CAMERA).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
        } }
        GestureAction.SCREENSHOT -> { haptic(view); view.postDelayed({
            svc.global(android.accessibilityservice.AccessibilityService.GLOBAL_ACTION_TAKE_SCREENSHOT)
        }, 300) }
        GestureAction.ASSISTANT -> { haptic(view, true); runCatching {
            ctx.startActivity(Intent(Intent.ACTION_VOICE_COMMAND).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
        } }
        else -> return false
    }
    return true
}

private fun idleOf(p: Presentation) = p is Presentation.Compact && p.activity == null

/** Двойное нажатие: своё для пустой пилюли и для пилюли с событием. */
private fun doubleTapAction(p: Presentation): GestureAction =
    if (idleOf(p)) SettingsStore.value.gIdleDouble else SettingsStore.value.gDoubleTap

private fun onTap(p: Presentation, ctx: Context, view: View) {
    when (p) {
        is Presentation.Compact -> if (p.activity == null) {
            val a = SettingsStore.value.gIdleTap
            if (a != GestureAction.QUICK || SettingsStore.value.quickActions) gesture(a, p, ctx, view, 1)
        } else if (pauseByTap(p.activity, ctx)) haptic(view)
        else gesture(SettingsStore.value.gTap, p, ctx, view, 1)
        is Presentation.Expanded -> {
            if (p.activity == com.pixelisland.core.QuickActivity || p.activity == com.pixelisland.core.StackActivity ||
                p.activity is com.pixelisland.core.ShelfActivity) { IslandController.collapse(); return }
            IslandController.touch()
            if (p.activity is PreviewActivity || p.activity is com.pixelisland.core.DemoActivity) IslandController.collapse()
            else when (SettingsStore.value.style(p.activity.type).exTap) {
                com.pixelisland.settings.ExTap.OPEN -> { haptic(view); IslandController.collapse(); open(p.activity, ctx) }
                com.pixelisland.settings.ExTap.COLLAPSE -> { hapticKind(view, com.pixelisland.core.Haptics.Kind.COLLAPSE); IslandController.collapse() }
                com.pixelisland.settings.ExTap.NOTHING -> Unit
            }
        }
        is Presentation.Peek -> if (!IslandController.replying.value) {
            haptic(view); IslandController.clearToast()
            val tap = p.event.onTap
            if (tap != null) tap() else ctx.sendPending(p.event.contentIntent)
        }
        else -> Unit
    }
}

private fun musicVolumeMode(p: Presentation) =
    SettingsStore.value.mediaGestures && p is Presentation.Compact && p.activity is MusicActivity

private fun stepVolume(ctx: Context, view: View, dir: Int) {
    val am = ctx.getSystemService(android.media.AudioManager::class.java) ?: return
    am.adjustStreamVolume(android.media.AudioManager.STREAM_MUSIC,
        if (dir > 0) android.media.AudioManager.ADJUST_RAISE else android.media.AudioManager.ADJUST_LOWER, 0)
    val max = am.getStreamMaxVolume(android.media.AudioManager.STREAM_MUSIC).coerceAtLeast(1)
    val pct = am.getStreamVolume(android.media.AudioManager.STREAM_MUSIC) * 100 / max
    com.pixelisland.core.Haptics.play(view, com.pixelisland.core.Haptics.Kind.TICK)
    IslandController.showToast(com.pixelisland.core.ToastEvent("volume", com.pixelisland.settings.EventType.VOLUME, "Медиа", "$pct%", pct / 100f), force = true)
}

/** Тап по таймеру — пауза/продолжение (свой таймер или кнопка паузы в уведомлении часов). */
private fun pauseByTap(a: LiveActivity, ctx: Context): Boolean {
    if (!SettingsStore.value.tapPausesTimers) return false
    if (a is com.pixelisland.core.OwnTimerActivity) { com.pixelisland.core.OwnTimers.togglePause(); return true }
    if (a is OngoingActivity && a.kind == com.pixelisland.core.OngoingKind.TIMER) {
        val act = a.actions.firstOrNull { Regex("пауз|pause|продолж|возобнов|resume|старт|start", RegexOption.IGNORE_CASE).containsMatchIn(it.title) }
            ?: return false
        return ctx.sendPending(act.intent)
    }
    return false
}

private fun onLongPress(p: Presentation, ctx: Context, view: View) {
    when (p) {
        is Presentation.Compact -> gesture(if (p.activity == null) SettingsStore.value.gIdleLong else SettingsStore.value.gLongPress, p, ctx, view, 1)
        is Presentation.Expanded -> { haptic(view, true); IslandController.collapse(); open(p.activity, ctx) }
        else -> Unit
    }
}

private fun onSwipe(p: Presentation, dx: Float, dy: Float, view: View) {
    val vertical = abs(dy) > abs(dx)
    val s = SettingsStore.value
    val ctx = view.context
    when (p) {
        is Presentation.Compact -> if (p.activity == null) when {
            vertical && dy > 30 -> gesture(s.gIdleSwipeDown, p, ctx, view, 1)
            vertical && dy < -30 -> gesture(s.gIdleSwipeUp, p, ctx, view, 1)
        } else when {
            !vertical && abs(dx) > 40 && s.mediaGestures && p.activity is MusicActivity -> {
                haptic(view); if (dx < 0) MediaRepository.next() else MediaRepository.previous()
            }
            vertical && dy > 30 -> gesture(s.gSwipeDown, p, ctx, view, 1)
            vertical && dy < -30 -> gesture(s.gSwipeUp, p, ctx, view, 1)
            !vertical && abs(dx) > 40 -> gesture(s.gSwipeSide, p, ctx, view, if (dx < 0) 1 else -1)
        }
        is Presentation.Expanded -> when {
            vertical && dy < -30 -> { hapticKind(view, com.pixelisland.core.Haptics.Kind.COLLAPSE); IslandController.collapse() }
            !vertical && abs(dx) > 60 && p.count > 1 -> { haptic(view); IslandController.collapse(); IslandController.cycle(if (dx < 0) 1 else -1) }
        }
        is Presentation.Peek -> if (dy < -30) IslandController.clearToast()
        else -> Unit
    }
}

/** Вторая активность — маленький кружок справа от основной пилюли. */
@Composable
private fun SecondBubble(p: Presentation, dims: IslandDims, originX: Int) {
    val list by IslandController.visible.collectAsState()
    val view = LocalView.current
    val colors = LocalIslandColors.current
    val second = (p as? Presentation.Compact)?.takeIf { it.count > 1 && it.activity != null }?.let { c ->
        list.getOrNull((c.index + 1) % c.count)
    }
    var last by remember { mutableStateOf<LiveActivity?>(null) }
    if (second != null) last = second
    val shown = second != null
    val scale by animateFloatAsState(if (shown) 1f else 0f, SettingsStore.value.motion(), label = "bubble")
    val a = last ?: return
    if (scale <= 0.01f && !shown) return
    val d = LocalDensity.current
    val sizePx = with(d) { dims.compactH.roundToPx() }
    val mainW by com.pixelisland.core.IslandRuntime.compactWidthDp.collectAsState()
    val x = dims.centerXPx + with(d) { (mainW.dp / 2 + dims.bubbleGap).roundToPx() } - originX
    Box(
        Modifier
            .offset { IntOffset(x, dims.contentTopPx) }
            .size(dims.compactH)
            .graphicsLayer { scaleX = scale; scaleY = scale; alpha = scale.coerceIn(0f, 1f) }
            .clip(androidx.compose.foundation.shape.CircleShape)
            .then(if (colors.brush != null) Modifier.background(colors.brush) else Modifier.background(colors.bg))
            .pointerInput(a.id) { detectTapGestures { haptic(view); IslandController.select(a.id) } },
        contentAlignment = Alignment.Center,
    ) {
        val st = SettingsStore.value.style(a.type)
        val data = com.pixelisland.ui.content.slotDataFor(a)
        val color = colors.resolve(st.color, data.auto)
        com.pixelisland.ui.content.AnimatedIcon(data, st, dims.slot, color)
    }
}

/** Красное кольцо ровно по вырезу — для калибровки. */
@Composable
private fun CalibrationRing(dims: IslandDims, originX: Int) {
    val g = dims.g
    val d = LocalDensity.current
    Box(
        Modifier
            .offset { IntOffset(g.centerX - g.cutoutWidth / 2 - originX, dims.contentTopPx + g.top - dims.islandTopPx) }
            .size(with(d) { g.cutoutWidth.toDp() }, with(d) { g.cutoutHeight.toDp() })
            .border(2.dp, Color(0xFFFF4D4D), RoundedCornerShape(50))
            .background(Color(0x33FF4D4D), RoundedCornerShape(50)),
    )
}

fun open(a: LiveActivity, ctx: Context) {
    when (a) {
        is MusicActivity -> MediaRepository.openApp()
        is OngoingActivity -> if (!ctx.sendPending(a.contentIntent)) ctx.launchPackage(a.packageName)
        is ChargingActivity -> runCatching {
            ctx.startActivity(Intent(Intent.ACTION_POWER_USAGE_SUMMARY).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
        }
        is TorchActivity, is PreviewActivity, is com.pixelisland.core.DemoActivity, com.pixelisland.core.QuickActivity,
        com.pixelisland.core.StackActivity, is com.pixelisland.core.ShelfActivity -> Unit
        is com.pixelisland.core.PrivacyActivity -> runCatching {
            ctx.startActivity(Intent(android.provider.Settings.ACTION_PRIVACY_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
        }
        is com.pixelisland.core.OwnTimerActivity -> Unit
        is com.pixelisland.core.CalendarActivity -> runCatching {
            ctx.startActivity(Intent(Intent.ACTION_VIEW, android.provider.CalendarContract.CONTENT_URI.buildUpon().appendPath("time")
                .appendPath(a.begin.toString()).build()).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
        }
        is com.pixelisland.core.AlarmSoonActivity -> runCatching {
            ctx.startActivity(Intent(android.provider.AlarmClock.ACTION_SHOW_ALARMS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
        }
    }
}


/**
 * Невидимое окно касаний над свёрнутой пилюлей. Оно ничего не рисует, поэтому может свободно
 * менять размер и положение — картинка острова от этого не дёргается.
 */
@Composable
fun TouchLayer(renderFlow: StateFlow<Presentation>, geometryFlow: StateFlow<IslandGeometry>) {
    val p by renderFlow.collectAsState()
    val s by SettingsStore.flow.collectAsState()
    val context = LocalContext.current
    val view = LocalView.current
    val pState by rememberUpdatedState(p)
    val pillW by com.pixelisland.core.IslandRuntime.compactWidthDp.collectAsState()
    val pillWState by rememberUpdatedState(pillW)
    Box(
        Modifier.fillMaxSize()
            .pointerInput(s.gDoubleTap, s.gIdleDouble, idleOf(p)) {
                fun inBubble(x: Float) = x > size.width / 2f + pillWState * density / 2f + 2 * density
                detectTapGestures(
                    onTap = { o -> if (inBubble(o.x)) selectSecond(pState, view) else onTap(pState, context, view) },
                    onDoubleTap = if (doubleTapAction(pState) == GestureAction.NONE) null
                    else { _ -> gesture(doubleTapAction(pState), pState, context, view, 1) },
                    onLongPress = { if (!musicVolumeMode(pState)) onLongPress(pState, context, view) },
                )
            }
            .pointerInput(Unit) {
                var acc = 0f; var moved = 0f
                detectDragGesturesAfterLongPress(
                    onDragStart = { acc = 0f; moved = 0f },
                    onDrag = { c, d ->
                        if (!musicVolumeMode(pState)) return@detectDragGesturesAfterLongPress
                        c.consume(); acc += d.y; moved += kotlin.math.abs(d.y)
                        while (acc <= -28f) { acc += 28f; stepVolume(context, view, +1) }
                        while (acc >= 28f) { acc -= 28f; stepVolume(context, view, -1) }
                    },
                    onDragEnd = { if (musicVolumeMode(pState) && moved < 12f) onLongPress(pState, context, view) },
                )
            }
            .pointerInput(Unit) {
                var dx = 0f; var dy = 0f
                detectDragGestures(
                    onDragStart = { dx = 0f; dy = 0f },
                    onDrag = { c, delta -> c.consume(); dx += delta.x; dy += delta.y },
                    onDragEnd = { onSwipe(pState, dx, dy, view) },
                )
            },
    )
}

private fun selectSecond(p: Presentation, view: View) {
    val c = p as? Presentation.Compact ?: return
    if (c.count < 2) return
    val list = IslandController.visible.value
    list.getOrNull((c.index + 1) % c.count)?.let { haptic(view); IslandController.select(it.id) }
}


/** Измеренные высоты раскрытого вида по id активности (dp). */
private val expandedHeights = androidx.compose.runtime.mutableStateMapOf<String, Float>()

fun expandedWidth(a: LiveActivity, dims: IslandDims): Dp = when (dims.s.style(a.type).exWidth) {
    com.pixelisland.settings.ExWidth.FULL -> dims.expandedW
    com.pixelisland.settings.ExWidth.MEDIUM -> dims.expandedW * 0.86f
    com.pixelisland.settings.ExWidth.CONTENT -> minOf(dims.expandedW, 330.dp)
}

fun expandedHeightOf(a: LiveActivity, dims: IslandDims): Dp =
    (expandedHeights[a.id]?.dp ?: dims.expandedHeight(a)).coerceIn(dims.compactH, dims.largeH - dims.margin)

private fun expandedCornerOf(a: LiveActivity, dims: IslandDims): Dp =
    dims.s.style(a.type).exCorner.takeIf { it > 0f }?.dp ?: dims.expandedCorner
