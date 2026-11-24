package com.pixelisland.media

import android.content.ComponentName
import android.content.Context
import android.graphics.Bitmap
import android.media.MediaMetadata
import android.media.session.MediaController
import android.media.session.MediaSession
import android.media.session.MediaSessionManager
import android.media.session.PlaybackState
import android.os.Handler
import android.os.Looper
import androidx.core.graphics.scale
import androidx.palette.graphics.Palette
import com.pixelisland.core.IslandController
import com.pixelisland.core.MusicActivity
import com.pixelisland.core.launchPackage
import com.pixelisland.core.sendPending
import com.pixelisland.service.MediaListenerService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Следит за активными медиасессиями системы и выдаёт одну «главную» в [state].
 * Работает только когда пользователь выдал доступ к уведомлениям.
 */
object MediaRepository : IslandActions {
    private fun publish(st: MediaState?) {
        if (st == _state.value && (st == null || IslandController.visible.value.any { it is MusicActivity })) return
        _state.value = st
        if (st == null) IslandController.remove("music") else IslandController.set(MusicActivity(st))
    }

    private val _state = MutableStateFlow<MediaState?>(null)
    val state: StateFlow<MediaState?> = _state

    private val main = Handler(Looper.getMainLooper())
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    private var appContext: Context? = null
    private var manager: MediaSessionManager? = null
    private var controllers: List<MediaController> = emptyList()
    private val callbacks = mutableMapOf<MediaSession.Token, MediaController.Callback>()
    private var current: MediaController? = null

    private var lastPlaying = false
    private val accentCache = com.pixelisland.core.Lru<String, Int?>(64)

    private val sessionsListener =
        MediaSessionManager.OnActiveSessionsChangedListener { list -> onSessions(list.orEmpty()) }

    /** Можно вызывать сколько угодно раз — подписка создаётся один раз. */
    fun attach(context: Context) {
        if (manager != null) return
        val ctx = context.applicationContext
        val component = ComponentName(ctx, MediaListenerService::class.java)
        val msm = ctx.getSystemService(MediaSessionManager::class.java) ?: return
        try {
            msm.addOnActiveSessionsChangedListener(sessionsListener, component, main)
            appContext = ctx
            manager = msm
            onSessions(msm.getActiveSessions(component))
        } catch (_: SecurityException) {
            // Доступ к уведомлениям ещё не выдан — попробуем позже.
        }
    }

    fun detach() {
        manager?.removeOnActiveSessionsChangedListener(sessionsListener)
        manager = null
        onSessions(emptyList())
    }

    private fun onSessions(list: List<MediaController>) {
        controllers.forEach { c -> callbacks.remove(c.sessionToken)?.let(c::unregisterCallback) }
        controllers = list
        list.forEach { c ->
            val cb = object : MediaController.Callback() {
                override fun onPlaybackStateChanged(state: PlaybackState?) = pick()
                override fun onMetadataChanged(metadata: MediaMetadata?) = pick()
                override fun onSessionDestroyed() = pick()
            }
            c.registerCallback(cb, main)
            callbacks[c.sessionToken] = cb
        }
        pick()
    }

    private fun MediaController.isActive(): Boolean {
        val s = playbackState?.state ?: return false
        val hasTitle = !metadata?.getString(MediaMetadata.METADATA_KEY_TITLE).isNullOrBlank()
        return hasTitle && s in setOf(
            PlaybackState.STATE_PLAYING, PlaybackState.STATE_PAUSED,
            PlaybackState.STATE_BUFFERING, PlaybackState.STATE_CONNECTING,
        )
    }

    private fun MediaController.isPlaying() = playbackState?.state in setOf(
        PlaybackState.STATE_PLAYING, PlaybackState.STATE_BUFFERING,
    )

    private fun pick() {
        val candidates = controllers.filter { it.isActive() }
        val chosen = candidates.firstOrNull { it.isPlaying() } ?: candidates.firstOrNull()
        current = chosen
        if (chosen == null) {
            publish(null)
            return
        }
        val md = chosen.metadata!!
        val pb = chosen.playbackState!!
        val title = md.getString(MediaMetadata.METADATA_KEY_TITLE).orEmpty()
        val artist = md.getString(MediaMetadata.METADATA_KEY_ARTIST)
            ?: md.getString(MediaMetadata.METADATA_KEY_ALBUM_ARTIST)
            ?: md.getString(MediaMetadata.METADATA_KEY_DISPLAY_SUBTITLE).orEmpty()
        val key = "${chosen.packageName}|$title|$artist"
        // Обложка: картинка в метаданных → загруженная по ссылке → картинка из медиа-уведомления.
        val art: Bitmap? = md.getBitmap(MediaMetadata.METADATA_KEY_ALBUM_ART)
            ?: md.getBitmap(MediaMetadata.METADATA_KEY_ART)
            ?: md.getBitmap(MediaMetadata.METADATA_KEY_DISPLAY_ICON)
            ?: uriArt[key]
            ?: notificationArt[chosen.packageName]
        if (md.getBitmap(MediaMetadata.METADATA_KEY_ALBUM_ART) == null && md.getBitmap(MediaMetadata.METADATA_KEY_ART) == null) {
            loadUriArt(key, md)
        }
        val playing = chosen.isPlaying()

        // Смахнутый остров возвращается при возобновлении воспроизведения.
        if (playing && !lastPlaying) IslandController.undismiss("music")
        lastPlaying = playing

        val state = MediaState(
            key = key,
            packageName = chosen.packageName,
            appLabel = labelOf(chosen.packageName),
            title = title,
            artist = artist,
            art = art,
            accent = accentCache[key],
            isPlaying = playing,
            durationMs = md.getLong(MediaMetadata.METADATA_KEY_DURATION),
            positionMs = pb.position,
            positionUpdateTime = pb.lastPositionUpdateTime,
            speed = if (pb.playbackSpeed == 0f) 1f else pb.playbackSpeed,
            canSeek = pb.actions and PlaybackState.ACTION_SEEK_TO != 0L,
        )
        publish(state)

        if (art != null && !accentCache.containsKey(key)) {
            accentCache[key] = null
            scope.launch {
                val color = withContext(Dispatchers.Default) { accentFrom(art) }
                accentCache[key] = color
                if (_state.value?.key == key) publish(_state.value?.copy(accent = color))
            }
        }
    }

    private val uriArt = com.pixelisland.core.Lru<String, Bitmap?>(4)   // обложки — только последние несколько
    private val notificationArt = com.pixelisland.core.Lru<String, Bitmap>(4)

    /** Многие плееры отдают обложку только ссылкой (content:// или https://). */
    private fun loadUriArt(key: String, md: MediaMetadata) {
        if (uriArt.containsKey(key)) return
        val uri = listOf(
            MediaMetadata.METADATA_KEY_ALBUM_ART_URI, MediaMetadata.METADATA_KEY_ART_URI, MediaMetadata.METADATA_KEY_DISPLAY_ICON_URI,
        ).firstNotNullOfOrNull { md.getString(it)?.takeIf(String::isNotBlank) } ?: return
        uriArt[key] = null
        val ctx = appContext ?: return
        scope.launch {
            val bmp = withContext(Dispatchers.IO) {
                runCatching {
                    val u = android.net.Uri.parse(uri)
                    val stream = if (u.scheme == "http" || u.scheme == "https") java.net.URL(uri).openStream()
                    else ctx.contentResolver.openInputStream(u)
                    stream?.use { android.graphics.BitmapFactory.decodeStream(it) }
                }.getOrNull()
            }
            if (bmp != null) { uriArt[key] = bmp; pick() }
        }
    }

    fun wantsNotificationArt(pkg: String): Boolean {
        if (current?.packageName != pkg) return false
        val md = current?.metadata ?: return false
        return md.getBitmap(MediaMetadata.METADATA_KEY_ALBUM_ART) == null && md.getBitmap(MediaMetadata.METADATA_KEY_ART) == null &&
            uriArt[_state.value?.key ?: ""] == null
    }

    /** Запасной вариант: большая картинка из медиа-уведомления плеера. */
    fun onMediaNotificationArt(pkg: String, bmp: Bitmap?) {
        if (bmp == null || notificationArt[pkg]?.sameAs(bmp) == true) return
        notificationArt[pkg] = bmp
        if (current?.packageName == pkg && _state.value?.art == null) pick()
    }

    private fun accentFrom(bitmap: Bitmap): Int? = runCatching {
        val small = if (bitmap.width > 160) bitmap.scale(160, 160 * bitmap.height / bitmap.width) else bitmap
        val p = Palette.from(small).generate()
        (p.vibrantSwatch ?: p.lightVibrantSwatch ?: p.dominantSwatch)?.rgb
    }.getOrNull()

    private fun labelOf(pkg: String): String = runCatching {
        val pm = appContext!!.packageManager
        pm.getApplicationLabel(pm.getApplicationInfo(pkg, 0)).toString()
    }.getOrDefault(pkg)

    // --- IslandActions ---
    override fun playPause() {
        val c = current ?: return
        if (c.isPlaying()) c.transportControls.pause() else c.transportControls.play()
    }
    override fun next() { current?.transportControls?.skipToNext() }
    override fun previous() { current?.transportControls?.skipToPrevious() }
    override fun seekTo(ms: Long) { current?.transportControls?.seekTo(ms) }
    override fun openApp() {
        val c = current ?: return
        val ctx = appContext ?: return
        if (!ctx.sendPending(c.sessionActivity)) ctx.launchPackage(c.packageName)
    }
    override fun dismiss() {
        IslandController.visible.value.firstOrNull { it.id == "music" }?.let(IslandController::dismiss)
    }
}
