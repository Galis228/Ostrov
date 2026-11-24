package com.pixelisland.core

import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.content.FileProvider
import com.pixelisland.settings.SettingsStore
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/** Отчёт об ошибке одной кнопкой: состояние, журнал, настройки и лог приложения в один файл. */
object BugReport {
    fun build(c: Context): File {
        val f = SimpleDateFormat("dd.MM HH:mm:ss", Locale.getDefault())
        val pm = c.packageManager.getPackageInfo(c.packageName, 0)
        val text = buildString {
            appendLine("=== Остров ${pm.versionName} (${pm.longVersionCode}) ===")
            appendLine("Устройство: ${Build.MANUFACTURER} ${Build.MODEL}, Android ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT}), сборка ${Build.DISPLAY}")
            appendLine("Время: ${f.format(Date())}")
            appendLine()
            appendLine("--- Состояние ---")
            appendLine("Сервис острова: ${if (IslandRuntime.serviceAlive.value) "работает" else "не работает"} (в настройках: ${Watchdog.a11yEnabled(c)})")
            appendLine("Слушатель уведомлений: ${if (IslandRuntime.listenerAlive.value) "работает" else "не работает"} (доступ: ${Watchdog.listenerEnabled(c)})")
            appendLine("Геометрия: ${IslandRuntime.geometry.value}")
            appendLine("Проблемы: ${Watchdog.problems(c).ifEmpty { listOf("нет") }.joinToString("; ")}")
            appendLine()
            appendLine("--- Журнал (последние события) ---")
            EventLog.entries.value.take(80).forEach { appendLine("${f.format(Date(it.time))}  ${it.kind}: ${it.title} ${it.text}") }
            appendLine()
            appendLine("--- Настройки ---")
            appendLine(SettingsStore.exportJson())
            appendLine()
            appendLine("--- Лог приложения ---")
            appendLine(runCatching {
                val p = Runtime.getRuntime().exec(arrayOf("logcat", "-d", "-t", "600", "--pid=${android.os.Process.myPid()}"))
                p.inputStream.bufferedReader().readText().takeLast(120_000)
            }.getOrElse { "не удалось прочитать: ${it.message}" })
        }
        val dir = File(c.cacheDir, "reports").apply { mkdirs() }
        return File(dir, "ostrov-report.txt").apply { writeText(text) }
    }

    fun share(c: Context) {
        val file = build(c)
        val uri = FileProvider.getUriForFile(c, "${c.packageName}.files", file)
        val send = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, "Остров: отчёт об ошибке")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        c.startActivity(Intent.createChooser(send, "Отправить отчёт").addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
    }
}
