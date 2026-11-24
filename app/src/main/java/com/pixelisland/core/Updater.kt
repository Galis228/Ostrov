package com.pixelisland.core

import android.app.DownloadManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import androidx.core.content.FileProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.File
import java.net.HttpURLConnection
import java.net.URL

/** Новая версия на GitHub. */
data class UpdateInfo(val code: Long, val name: String, val apkUrl: String, val notes: List<String>)

/**
 * Проверка обновлений: файл apk/version.json в репозитории Galis228/ostrov.
 * Раз в сутки сама (из сторожа и при открытии приложения), плюс кнопка «Проверить».
 */
object Updater {
    private const val REPO = "Galis228/ostrov"
    private const val VERSION_URL = "https://raw.githubusercontent.com/$REPO/main/apk/version.json"
    const val PAGE_URL = "https://github.com/$REPO"
    private const val CHANNEL = "updates"
    private const val NOTIF_ID = 4343
    private const val DAY = 24 * 3600_000L

    /** Доступное обновление (null — нет или не проверяли). */
    val available = MutableStateFlow<UpdateInfo?>(null)
    /** Идёт проверка. */
    val checking = MutableStateFlow(false)
    /** Прогресс скачивания 0..1, null — не качаем. */
    val progress = MutableStateFlow<Float?>(null)
    /** Последняя ошибка (для подписи под кнопкой). */
    val error = MutableStateFlow<String?>(null)

    private val io = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private fun prefs(c: Context) = c.getSharedPreferences("updates", Context.MODE_PRIVATE)
    fun autoCheck(c: Context) = prefs(c).getBoolean("auto", true)
    fun setAutoCheck(c: Context, on: Boolean) = prefs(c).edit().putBoolean("auto", on).apply()
    fun lastCheck(c: Context) = prefs(c).getLong("last", 0L)

    fun currentCode(c: Context): Long = c.packageManager.getPackageInfo(c.packageName, 0).longVersionCode
    fun currentName(c: Context): String = c.packageManager.getPackageInfo(c.packageName, 0).versionName ?: "?"

    /** Проверить, если давно не проверяли (или [force]). [notify] — прислать уведомление о новой версии. */
    fun maybeCheck(c: Context, force: Boolean = false, notify: Boolean = true) {
        val app = c.applicationContext
        if (!force && (!autoCheck(app) || System.currentTimeMillis() - lastCheck(app) < DAY)) {
            restoreCached(app); return
        }
        if (checking.value) return
        checking.value = true
        error.value = null
        io.launch {
            val r = runCatching { fetch() }
            withContext(Dispatchers.Main) {
                checking.value = false
                r.onSuccess { info ->
                    prefs(app).edit().putLong("last", System.currentTimeMillis()).putString("cache", info?.let { encode(it) }).apply()
                    val newer = info?.takeIf { it.code > currentCode(app) }
                    available.value = newer
                    if (newer != null && notify && prefs(app).getLong("notified", 0) != newer.code) {
                        prefs(app).edit().putLong("notified", newer.code).apply()
                        notifyUpdate(app, newer)
                    }
                }.onFailure { error.value = "Не удалось проверить: нет связи с GitHub" }
            }
        }
    }

    /** После перезапуска — показать то, что нашли при прошлой проверке. */
    private fun restoreCached(c: Context) {
        if (available.value != null) return
        val txt = prefs(c).getString("cache", null) ?: return
        available.value = runCatching { decode(JSONObject(txt)) }.getOrNull()?.takeIf { it.code > currentCode(c) }
    }

    private fun fetch(): UpdateInfo? {
        val con = URL("$VERSION_URL?t=${System.currentTimeMillis() / 60_000}").openConnection() as HttpURLConnection
        con.connectTimeout = 10_000; con.readTimeout = 10_000
        con.setRequestProperty("Cache-Control", "no-cache")
        return con.inputStream.bufferedReader().use { decode(JSONObject(it.readText())) }
    }

    private fun decode(j: JSONObject) = UpdateInfo(
        j.getLong("versionCode"), j.getString("versionName"), j.getString("apk"),
        j.optJSONArray("notes")?.let { a -> (0 until a.length()).map { a.getString(it) } } ?: emptyList(),
    )

    private fun encode(u: UpdateInfo) = JSONObject().put("versionCode", u.code).put("versionName", u.name).put("apk", u.apkUrl)
        .put("notes", org.json.JSONArray(u.notes)).toString()

    private fun notifyUpdate(c: Context, u: UpdateInfo) {
        val nm = c.getSystemService(NotificationManager::class.java)
        nm.createNotificationChannel(NotificationChannel(CHANNEL, com.pixelisland.ui.i18n.tr("Обновления"), NotificationManager.IMPORTANCE_DEFAULT))
        val open = c.packageManager.getLaunchIntentForPackage(c.packageName) ?: return
        val pi = PendingIntent.getActivity(c, 3, open, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
        val n = android.app.Notification.Builder(c, CHANNEL)
            .setSmallIcon(com.pixelisland.R.drawable.ic_tile)
            .setContentTitle(com.pixelisland.ui.i18n.tr("Доступна новая версия") + " ${u.name}")
            .setContentText(u.notes.firstOrNull() ?: com.pixelisland.ui.i18n.tr("Нажмите, чтобы обновить"))
            .setStyle(android.app.Notification.BigTextStyle().bigText(u.notes.joinToString("\n") { "• $it" }))
            .setContentIntent(pi).setAutoCancel(true)
            .build()
        runCatching { nm.notify(NOTIF_ID, n) }
    }

    /** Можно ли сразу ставить APK (разрешение «Установка неизвестных приложений»). */
    fun canInstall(c: Context) = c.packageManager.canRequestPackageInstalls()

    fun openInstallPermission(c: Context) {
        runCatching {
            c.startActivity(Intent(android.provider.Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES, Uri.parse("package:${c.packageName}"))
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
        }
    }

    fun openInBrowser(c: Context, u: UpdateInfo?) {
        runCatching { c.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(u?.apkUrl ?: PAGE_URL)).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)) }
    }

    /** Скачать APK через системный загрузчик и открыть установщик. */
    fun downloadAndInstall(c: Context, u: UpdateInfo) {
        val app = c.applicationContext
        if (progress.value != null) return
        val dir = app.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS) ?: return
        val file = File(dir, "Ostrov-${u.name}.apk")
        dir.listFiles()?.filter { it.name.endsWith(".apk") }?.forEach { it.delete() }   // старые скачанные версии
        val dm = app.getSystemService(DownloadManager::class.java)
        val id = dm.enqueue(
            DownloadManager.Request(Uri.parse(u.apkUrl))
                .setTitle("Остров ${u.name}")
                .setMimeType("application/vnd.android.package-archive")
                .setDestinationUri(Uri.fromFile(file))
                .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE),
        )
        progress.value = 0f
        error.value = null
        io.launch {
            while (true) {
                delay(400)
                val q = dm.query(DownloadManager.Query().setFilterById(id))
                val (status, done, total) = q.use {
                    if (!it.moveToFirst()) Triple(DownloadManager.STATUS_FAILED, 0L, 0L)
                    else Triple(
                        it.getInt(it.getColumnIndexOrThrow(DownloadManager.COLUMN_STATUS)),
                        it.getLong(it.getColumnIndexOrThrow(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR)),
                        it.getLong(it.getColumnIndexOrThrow(DownloadManager.COLUMN_TOTAL_SIZE_BYTES)),
                    )
                }
                if (total > 0) progress.value = done.toFloat() / total
                when (status) {
                    DownloadManager.STATUS_SUCCESSFUL -> {
                        withContext(Dispatchers.Main) { progress.value = null; install(app, file) }
                        return@launch
                    }
                    DownloadManager.STATUS_FAILED -> {
                        withContext(Dispatchers.Main) { progress.value = null; error.value = "Не удалось скачать. Попробуйте открыть в браузере." }
                        return@launch
                    }
                }
            }
        }
    }

    private fun install(c: Context, file: File) {
        val uri = FileProvider.getUriForFile(c, "${c.packageName}.files", file)
        runCatching {
            c.startActivity(Intent(Intent.ACTION_VIEW).setDataAndType(uri, "application/vnd.android.package-archive")
                .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK))
        }.onFailure { error.value = "Не удалось открыть установщик" }
    }
}
