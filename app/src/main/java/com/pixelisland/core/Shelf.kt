package com.pixelisland.core

import android.content.ClipData
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Size
import com.pixelisland.settings.SettingsStore
import kotlinx.coroutines.flow.MutableStateFlow

/** Полка: файлы, ссылки и текст, брошенные на остров перетаскиванием. Живёт до перезапуска. */
object Shelf {
    val items = MutableStateFlow<List<ShelfItem>>(emptyList())
    /** Сейчас что-то тащат над островом. */
    val dragging = MutableStateFlow(false)
    private var seq = 0L

    fun addClip(c: Context, clip: ClipData) {
        val added = (0 until clip.itemCount).mapNotNull { i ->
            val it = clip.getItemAt(i)
            val uri = it.uri
            when {
                uri != null -> {
                    val mime = runCatching { c.contentResolver.getType(uri) }.getOrNull() ?: clip.description.getMimeType(0)
                    val thumb = if (mime?.startsWith("image/") == true || mime?.startsWith("video/") == true)
                        runCatching { c.contentResolver.loadThumbnail(uri, Size(200, 200), null) }.getOrNull() else null
                    ShelfItem(++seq, null, uri, mime, uri.lastPathSegment?.substringAfterLast('/') ?: "Файл", thumb)
                }
                !it.text.isNullOrBlank() -> {
                    val t = it.text.toString()
                    ShelfItem(++seq, t, null, "text/plain", if (t.startsWith("http")) Uri.parse(t).host ?: t else t.take(40))
                }
                else -> null
            }
        }
        if (added.isEmpty()) return
        items.value = (added + items.value).take(12)
        publish()
        com.pixelisland.core.EventLog.add("Полка", added.first().label)
    }

    fun addText(t: String) {
        items.value = (listOf(ShelfItem(++seq, t, null, "text/plain", t.take(40))) + items.value).take(12)
        publish()
    }

    fun remove(id: Long) { items.value = items.value.filter { it.id != id }; publish() }
    fun clear() { items.value = emptyList(); publish() }

    private fun publish() {
        if (items.value.isEmpty() || !SettingsStore.value.on(com.pixelisland.settings.EventType.SHELF)) IslandController.remove("shelf")
        else IslandController.set(ShelfActivity(items.value))
    }

    /** Поделиться элементом через системное меню. */
    fun share(c: Context, it: ShelfItem) {
        val send = Intent(Intent.ACTION_SEND).apply {
            if (it.uri != null) {
                type = it.mime ?: "*/*"
                putExtra(Intent.EXTRA_STREAM, it.uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            } else {
                type = "text/plain"; putExtra(Intent.EXTRA_TEXT, it.text)
            }
        }
        runCatching { c.startActivity(Intent.createChooser(send, null).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)) }
    }

    /** Открыть ссылку или файл. */
    fun open(c: Context, it: ShelfItem) {
        val i = when {
            it.uri != null -> Intent(Intent.ACTION_VIEW).setDataAndType(it.uri, it.mime).addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            it.text?.startsWith("http") == true -> Intent(Intent.ACTION_VIEW, Uri.parse(it.text.trim()))
            else -> { copy(c, it); return }
        }
        runCatching { c.startActivity(i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)) }
    }

    fun copy(c: Context, it: ShelfItem) {
        val cm = c.getSystemService(android.content.ClipboardManager::class.java)
        if (it.uri != null) cm.setPrimaryClip(ClipData.newUri(c.contentResolver, it.label, it.uri))
        else cm.setPrimaryClip(ClipData.newPlainText(it.label, it.text))
    }
}
