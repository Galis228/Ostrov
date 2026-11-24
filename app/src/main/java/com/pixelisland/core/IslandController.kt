package com.pixelisland.core

import com.pixelisland.settings.IslandSettings
import com.pixelisland.settings.SettingsStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** Единый «мозг» острова: что показывать прямо сейчас. */
object IslandController {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    private val activities = MutableStateFlow<Map<String, LiveActivity>>(emptyMap())
    private val dismissed = MutableStateFlow<Map<String, String>>(emptyMap())
    private val selectedId = MutableStateFlow<String?>(null)
    private val expandedId = MutableStateFlow<String?>(null)
    private val currentToast = MutableStateFlow<Any?>(null)
    private val toastQueue = ArrayDeque<Any>()
    private var toastJob: Job? = null
    private var collapseJob: Job? = null

    private val hiddenReasons = MutableStateFlow<Set<String>>(emptySet())
    val previewMode = MutableStateFlow(false)
    private val preview = MutableStateFlow(PreviewActivity(playing = true, track = 0))

    val calibrating = MutableStateFlow(false)
    private var demoJob: Job? = null

    private fun IslandSettings.allows(a: LiveActivity) = a is PreviewActivity || a is DemoActivity ||
        (on(a.type) && appShown(pkgOf(a)))

    fun pkgOf(a: LiveActivity): String? = when (a) {
        is MusicActivity -> a.media.packageName
        is OngoingActivity -> a.packageName
        else -> null
    }

    val visible: StateFlow<List<LiveActivity>> = combine(
        activities, dismissed, previewMode, preview, SettingsStore.flow,
    ) { acts, dis, pm, pv, s ->
        val list = acts.values.filter { s.allows(it) && dis[it.id] != it.dismissKey }.toMutableList<LiveActivity>()
        if (pm) list += pv
        list.sortedByDescending { it.priority }
    }.stateIn(scope, SharingStarted.Eagerly, emptyList())

    private data class Inputs(
        val list: List<LiveActivity>, val selected: String?, val expanded: String?, val toast: Any?,
    )

    val presentation: StateFlow<Presentation> = combine(
        combine(visible, selectedId, expandedId, currentToast) { l, s, e, t -> Inputs(l, s, e, t) },
        hiddenReasons, SettingsStore.flow, calibrating,
    ) { inp, hidden, s, cal ->
        if (cal) return@combine Presentation.Calibrate
        if (!s.enabled || hidden.isNotEmpty()) return@combine Presentation.Hidden
        val list = inp.list
        if (inp.expanded == "quick") return@combine Presentation.Expanded(QuickActivity, 0, 1)
        if (inp.expanded == "stack") return@combine Presentation.Expanded(StackActivity, 0, 1)
        if (inp.expanded == "shelf" && list.none { it.id == "shelf" }) return@combine Presentation.Expanded(ShelfActivity(emptyList()), 0, 1)
        val exp = list.indexOfFirst { it.id == inp.expanded }
        if (exp >= 0) return@combine Presentation.Expanded(list[exp], exp, list.size)
        when (val t = inp.toast) {
            is ToastEvent -> return@combine Presentation.Toast(t)
            is PeekEvent -> return@combine Presentation.Peek(t)
        }
        if (list.isNotEmpty()) {
            val i = list.indexOfFirst { it.id == inp.selected }.coerceAtLeast(0)
            return@combine Presentation.Compact(list[i], i, list.size)
        }
        if (s.showIdlePill) Presentation.Compact(null, 0, 0) else Presentation.Hidden
    }.stateIn(scope, SharingStarted.Eagerly, Presentation.Hidden)

    // --- Источники данных ---

    fun set(activity: LiveActivity) {
        val old = activities.value
        val isNew = activity.id !in old
        activities.update { it + (activity.id to activity) }
        if (isNew) {
            if (activity !is PreviewActivity && activity !is DemoActivity) {
                EventLog.add(activity.type.title, logTitle(activity))
                Stats.hit(activity.type)
                Automations.fire(activity.type)
            }
            val cur = visible.value.firstOrNull { it.id == selectedId.value }
            if (cur == null || activity.priority > cur.priority) selectedId.value = activity.id
        }
    }

    private fun logTitle(a: LiveActivity) = when (a) {
        is MusicActivity -> "${a.media.title} — ${a.media.artist}"
        is OngoingActivity -> a.title.ifBlank { a.appLabel }
        is CalendarActivity -> a.title
        is AlarmSoonActivity -> a.label
        else -> a.type.title
    }

    fun remove(id: String) {
        if (id !in activities.value) return
        activities.update { it - id }
        dismissed.update { it - id }
        if (expandedId.value == id) collapse()
    }

    fun undismiss(id: String) = dismissed.update { it - id }

    fun showToast(event: Any, force: Boolean = false) {
        val st = SettingsStore.value
        if (!force) {
            val t = (event as? ToastEvent)?.type ?: (event as? PeekEvent)?.type
            if (t != null && !idOf(event).startsWith("demo")) Automations.fire(t)
        }
        if (!force) {
            if (hiddenReasons.value.isNotEmpty() || !st.enabled) return
            if (event is ToastEvent && !st.on(event.type)) return
            if (event is PeekEvent && !st.on(event.type)) return
            if (IslandRuntime.night.value && st.nightEnabled && st.nightQuiet && !isImportant(event)) return
        }
        val id = idOf(event)
        if (currentToast.value?.let(::idOf) == id) {
            currentToast.value = event
            restartToastTimer(event)
            return
        }
        toastQueue.removeAll { idOf(it) == id }
        toastQueue.addLast(event)
        if (!force) ((event as? ToastEvent)?.type ?: (event as? PeekEvent)?.type)?.let(Stats::hit)
        when (event) {
            is ToastEvent -> EventLog.add(event.type.title, event.title, event.value)
            is PeekEvent -> EventLog.add(event.appLabel, event.title, event.text)
        }
        pump()
    }

    private fun isImportant(e: Any) = e is ToastEvent && e.type in setOf(
        com.pixelisland.settings.EventType.BATTERY_LOW, com.pixelisland.settings.EventType.CHARGE_FULL,
    )

    /** Пользователь печатает ответ — карточку не убираем, окну нужен фокус для клавиатуры. */
    val replying = MutableStateFlow(false)

    fun startReply() { toastJob?.cancel(); replying.value = true }
    fun endReply() { replying.value = false; clearToast() }

    fun clearToast() {
        replying.value = false
        toastJob?.cancel()
        currentToast.value = null
        pump()
    }

    private fun idOf(e: Any) = when (e) { is ToastEvent -> e.id; is PeekEvent -> e.id; else -> "" }

    private fun pump() {
        if (currentToast.value != null || expandedId.value != null) return
        val next = toastQueue.removeFirstOrNull() ?: return
        currentToast.value = next
        restartToastTimer(next)
    }

    private fun restartToastTimer(e: Any) {
        toastJob?.cancel()
        val st = SettingsStore.value
        val own = when (e) {
            is ToastEvent -> st.style(e.type).durationSec
            is PeekEvent -> st.style(e.type).durationSec
            else -> 0f
        }
        val sec = if (own > 0f) own else st.toastSec.let { if (e is PeekEvent) maxOf(it, 4f) else it }
        toastJob = scope.launch {
            delay((sec * 1000).toLong())
            currentToast.value = null
            delay(120)
            pump()
        }
    }

    // --- Взаимодействие ---

    fun expand(id: String) {
        toastJob?.cancel()
        currentToast.value?.let { toastQueue.addFirst(it) }
        currentToast.value = null
        expandedId.value = id
        touch()
    }

    fun collapse() {
        collapseJob?.cancel()
        if (expandedId.value == null) return
        expandedId.value = null
        scope.launch { delay(200); pump() }
    }

    /** Любое касание продлевает жизнь развёрнутого острова. */
    fun touch() {
        collapseJob?.cancel()
        if (expandedId.value == null || expandedId.value == "preview") return
        if (expandedId.value == "shelf" && Shelf.dragging.value) return
        // Предохранитель: даже без автосворачивания раскрытый остров закроется через 2 минуты без касаний.
        val st = SettingsStore.value
        val own = visible.value.firstOrNull { it.id == expandedId.value }?.let { st.style(it.type).exCollapse } ?: 0f
        val sec = (if (own > 0f) own else st.autoCollapseSec).let { if (it <= 0f) 120f else it }
        collapseJob = scope.launch { delay((sec * 1000).toLong()); collapse() }
    }

    fun dismiss(activity: LiveActivity) {
        if (activity is PreviewActivity) return
        dismissed.update { it + (activity.id to activity.dismissKey) }
        if (expandedId.value == activity.id) collapse()
    }

    fun cycle(direction: Int) {
        val list = visible.value
        if (list.size < 2) return
        val i = list.indexOfFirst { it.id == selectedId.value }.coerceAtLeast(0)
        selectedId.value = list[(i + direction).mod(list.size)].id
    }

    fun select(id: String) { selectedId.value = id }

    fun setHidden(reason: String, hidden: Boolean) =
        hiddenReasons.update { if (hidden) it + reason else it - reason }

    private var snoozeJob: Job? = null
    /** Спрятать остров на [minutes] минут (жест «вверх»). */
    fun snooze(minutes: Int = 10) {
        collapse()
        setHidden("snooze", true)
        snoozeJob?.cancel()
        snoozeJob = scope.launch { delay(minutes * 60_000L); setHidden("snooze", false) }
    }
    fun unsnooze() { snoozeJob?.cancel(); setHidden("snooze", false) }
    val snoozed get() = "snooze" in hiddenReasons.value

    /** Показать живую активность-пример на 6 секунд (из редактора). */
    fun demoLive(type: com.pixelisland.settings.EventType) {
        demoJob?.cancel()
        remove("demo")
        set(DemoActivity(type))
        demoJob = scope.launch { delay(6_000); remove("demo") }
    }

    /** Приложение ушло в фон: убрать всё, что показывали только для настроек. */
    fun appHidden() {
        demoJob?.cancel(); demoJob = null
        remove("demo")
        if (expandedId.value == "preview" || expandedId.value == "demo") collapse()
    }

    // --- Предпросмотр для настроек ---
    fun previewPlayPause() = preview.update { it.copy(playing = !it.playing) }
    fun previewSkip(d: Int) = preview.update { it.copy(track = it.track + d) }
    fun togglePreviewExpanded() {
        if (expandedId.value == "preview") collapse() else expand("preview")
    }
}
