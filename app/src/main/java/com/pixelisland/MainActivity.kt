package com.pixelisland

import android.Manifest
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Accessibility
import androidx.compose.material.icons.rounded.Animation
import androidx.compose.material.icons.rounded.Bluetooth
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.NotificationsActive
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import com.pixelisland.ui.i18n.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.compose.LifecycleResumeEffect
import com.pixelisland.core.IslandController
import com.pixelisland.media.MediaRepository
import com.pixelisland.service.IslandAccessibilityService
import com.pixelisland.service.MediaListenerService
import com.pixelisland.settings.SettingsStore
import com.pixelisland.settings.EventType
import com.pixelisland.ui.screens.SystemTab
import androidx.compose.material3.TopAppBar
import androidx.compose.foundation.layout.Box
import com.pixelisland.ui.screens.CalibrationScreen
import com.pixelisland.ui.screens.EventEditor
import com.pixelisland.ui.screens.EventsTab
import com.pixelisland.ui.screens.LookTab
import androidx.compose.material3.ShortNavigationBar
import androidx.compose.material3.ShortNavigationBarItem
import androidx.activity.compose.PredictiveBackHandler
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.ui.graphics.graphicsLayer
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.compose.material.icons.rounded.Widgets
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Call
import androidx.compose.material.icons.rounded.ScreenRotation
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.material.icons.rounded.BatteryChargingFull
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.Event
import androidx.compose.material.icons.rounded.DirectionsWalk
import androidx.compose.material.icons.rounded.Image
import androidx.compose.runtime.key
import com.pixelisland.ui.theme.AppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        SettingsStore.init(this)
        com.pixelisland.ui.i18n.I18n.init(this)
        com.pixelisland.core.EventLog.init(this)
        com.pixelisland.core.Stats.init(this)
        com.pixelisland.core.Automations.init(this)
        com.pixelisland.core.Profiles.init(this)
        com.pixelisland.core.Watchdog.schedule(this)
        if (intent?.action == "com.pixelisland.TOGGLE") {
            SettingsStore.update { copy(enabled = !enabled) }
            com.pixelisland.service.IslandWidget.refresh(this)
            android.widget.Toast.makeText(this, com.pixelisland.ui.i18n.tr(if (SettingsStore.value.enabled) "Остров включён" else "Остров выключен"), android.widget.Toast.LENGTH_SHORT).show()
            finish(); return
        }
        val start = intent?.action
        enableEdgeToEdge()
        setContent { AppTheme { App(start) } }
    }
}

private fun Context.hasNotificationAccess() =
    NotificationManagerCompat.getEnabledListenerPackages(this).contains(packageName)

private fun Context.hasAccessibility(): Boolean {
    val enabled = Settings.Secure.getString(contentResolver, Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES) ?: return false
    val me = ComponentName(this, IslandAccessibilityService::class.java)
    return enabled.split(':').any { ComponentName.unflattenFromString(it) == me }
}

private fun Context.hasBtPermission() =
    checkSelfPermission(Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED

private enum class Tab(val title: String, val icon: ImageVector) {
    HOME("Главная", Icons.Rounded.Home),
    EVENTS("События", Icons.Rounded.Widgets),
    LOOK("Вид", Icons.Rounded.Palette),
    FEATURES("Функции", Icons.Rounded.AutoAwesome),
    SYSTEM("Система", Icons.Rounded.Tune),
}

private sealed interface Sub {
    data class Editor(val type: EventType) : Sub
    data object Calibration : Sub
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun App(start: String?) {
    val ctx = LocalContext.current
    val prefs = remember { ctx.getSharedPreferences("app", Context.MODE_PRIVATE) }
    // Кто уже пользовался островом (настройки сохранены), мастер не видит.
    var onboarded by remember {
        mutableStateOf(prefs.getBoolean("onboarded", ctx.getSharedPreferences("island_settings_v3", Context.MODE_PRIVATE).contains("json")))
    }
    if (!onboarded) {
        com.pixelisland.ui.screens.Onboarding(
            permissions = { step -> OnboardingPermissions(step) },
        ) { prefs.edit().putBoolean("onboarded", true).apply(); onboarded = true }
        return
    }
    var tab by rememberSaveable { mutableStateOf(if (start == "com.pixelisland.PRESETS") Tab.LOOK else Tab.HOME) }
    var subStart by rememberSaveable { mutableIntStateOf(0) }
    var sub by remember { mutableStateOf<Sub?>(if (start == "com.pixelisland.CALIBRATE") Sub.Calibration else null) }
    BackHandler(sub == null && tab != Tab.HOME) { tab = Tab.HOME }
    // Предиктивный «назад»: подэкран уменьшается и отъезжает вслед за жестом.
    var backProgress by remember { mutableFloatStateOf(0f) }
    PredictiveBackHandler(enabled = sub != null) { events ->
        try {
            events.collect { backProgress = it.progress }
            sub = null
        } finally { backProgress = 0f }
    }

    var notif by remember { mutableStateOf(ctx.hasNotificationAccess()) }
    var a11y by remember { mutableStateOf(ctx.hasAccessibility()) }
    LifecycleResumeEffect(Unit) {
        notif = ctx.hasNotificationAccess(); a11y = ctx.hasAccessibility()
        if (notif) MediaRepository.attach(ctx)
        onPauseOrDispose { }
    }

    val go: (com.pixelisland.ui.screens.Dest) -> Unit = { d ->
        when (d) {
            is com.pixelisland.ui.screens.Dest.Tab -> { subStart = d.sub; tab = Tab.entries[d.tab] }
            is com.pixelisland.ui.screens.Dest.Event -> sub = Sub.Editor(d.type)
            com.pixelisland.ui.screens.Dest.Calibration -> sub = Sub.Calibration
        }
    }

    val title = when (val sc = sub) {
        is Sub.Editor -> sc.type.title
        Sub.Calibration -> "Калибровка выреза"
        null -> if (tab == Tab.HOME) "Остров" else tab.title
    }
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(title, style = if (tab == Tab.HOME && sub == null) MaterialTheme.typography.headlineMedium else MaterialTheme.typography.titleLarge) },
                navigationIcon = {
                    if (sub != null) IconButton(onClick = { sub = null }) { Icon(Icons.AutoMirrored.Rounded.ArrowBack, "Назад") }
                },
            )
        },
        bottomBar = {
            if (sub == null) ShortNavigationBar {
                Tab.entries.forEach { t ->
                    ShortNavigationBarItem(
                        selected = tab == t, onClick = { subStart = 0; tab = t },
                        icon = { Icon(t.icon, null) }, label = { Text(t.title) },
                    )
                }
            }
        },
    ) { pad ->
        key(tab, sub, subStart) {
            Box(
                Modifier.fillMaxSize().padding(pad).graphicsLayer {
                    val p = backProgress
                    scaleX = 1f - 0.08f * p; scaleY = 1f - 0.08f * p
                    translationX = p * 32.dp.toPx()
                    shape = RoundedCornerShape((p * 28).dp); clip = p > 0f
                },
            ) {
                when (val sc = sub) {
                    is Sub.Editor -> EventEditor(sc.type)
                    Sub.Calibration -> CalibrationScreen()
                    null -> when (tab) {
                        Tab.HOME -> com.pixelisland.ui.screens.HomeTab(go)
                        Tab.EVENTS -> EventsTab(!notif || !a11y, { subStart = 0; tab = Tab.SYSTEM }) { sub = Sub.Editor(it) }
                        Tab.LOOK -> LookTab(subStart) { sub = Sub.Calibration }
                        Tab.FEATURES -> com.pixelisland.ui.screens.FeaturesTab(subStart)
                        Tab.SYSTEM -> SystemTab(subStart) { Permissions(notif, a11y) }
                    }
                }
            }
        }
    }
}

/** Шаги мастера с разрешениями: 1 — спецвозможности, 2 — уведомления. */
@Composable
private fun OnboardingPermissions(step: Int) {
    val ctx = LocalContext.current
    var notif by remember { mutableStateOf(ctx.hasNotificationAccess()) }
    var a11y by remember { mutableStateOf(ctx.hasAccessibility()) }
    LifecycleResumeEffect(Unit) { notif = ctx.hasNotificationAccess(); a11y = ctx.hasAccessibility(); onPauseOrDispose { } }
    val post = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { }
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        if (step == 1) {
            PermissionCard(Icons.Rounded.Accessibility, "Специальные возможности", "Чтобы рисовать остров вокруг камеры.", a11y) { openA11y(ctx) }
            if (!a11y) RestrictedSettingsHint()
        } else {
            PermissionCard(Icons.Rounded.Notifications, "Доступ к уведомлениям", "Музыка, звонки, навигация, таймеры.", notif) { openListener(ctx) }
            var posted by remember { mutableStateOf(ctx.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) }
            PermissionCard(Icons.Rounded.NotificationsActive, "Уведомления от острова", "Сообщим, если Android его остановит.", posted) {
                post.launch(Manifest.permission.POST_NOTIFICATIONS); posted = true
            }
        }
    }
}

private fun openListener(ctx: Context) {
    val i = Intent(Settings.ACTION_NOTIFICATION_LISTENER_DETAIL_SETTINGS).putExtra(
        Settings.EXTRA_NOTIFICATION_LISTENER_COMPONENT_NAME,
        ComponentName(ctx, MediaListenerService::class.java).flattenToString(),
    )
    runCatching { ctx.startActivity(i) }.onFailure { ctx.startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)) }
}

private fun openA11y(ctx: Context) {
    val i = Intent("android.settings.ACCESSIBILITY_DETAILS_SETTINGS").putExtra(
        Intent.EXTRA_COMPONENT_NAME, ComponentName(ctx, IslandAccessibilityService::class.java).flattenToString(),
    )
    runCatching { ctx.startActivity(i) }.onFailure { ctx.startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)) }
}

@Composable
private fun Permissions(notif: Boolean, a11y: Boolean) {
    val ctx = LocalContext.current
    var bt by remember { mutableStateOf(ctx.hasBtPermission()) }
    val btLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { bt = it }
    PermissionCard(Icons.Rounded.Notifications, "Доступ к уведомлениям", "Музыка, звонки, навигация, таймеры, Live Updates.", notif) { openListener(ctx) }
    PermissionCard(Icons.Rounded.Accessibility, "Специальные возможности", "Чтобы рисовать остров поверх статус-бара.", a11y) { openA11y(ctx) }
    PermissionCard(Icons.Rounded.Bluetooth, "Устройства поблизости", "Необязательно: заряд Bluetooth-наушников.", bt) {
        btLauncher.launch(Manifest.permission.BLUETOOTH_CONNECT)
    }
    if (!a11y || !notif) RestrictedSettingsHint()
    BatteryCard()
    OptionalPermission(Manifest.permission.ACCESS_COARSE_LOCATION, Icons.Rounded.LocationOn, "Местоположение", "Для погоды утром (примерное).")
    OptionalPermission(Manifest.permission.READ_CALENDAR, Icons.Rounded.Event, "Календарь", "Отсчёт до встреч.")
    OptionalPermission(Manifest.permission.ACTIVITY_RECOGNITION, Icons.Rounded.DirectionsWalk, "Физическая активность", "Шаги в пустой пилюле.")
    OptionalPermission(Manifest.permission.READ_MEDIA_IMAGES, Icons.Rounded.Image, "Фото", "Превью скриншотов.")
    OptionalPermission(Manifest.permission.POST_NOTIFICATIONS, Icons.Rounded.NotificationsActive, "Уведомления от острова", "Сообщить, если Android остановит остров.")
    OptionalPermission(Manifest.permission.ANSWER_PHONE_CALLS, Icons.Rounded.Call, "Звонки", "Принять и сбросить звонок прямо из острова.")
    WriteSettingsCard()
}

/** Чтобы система не «усыпляла» остров. Сервисы спецвозможностей сами перезапускаются после перезагрузки. */
@android.annotation.SuppressLint("BatteryLife")
@Composable
private fun BatteryCard() {
    val ctx = LocalContext.current
    val pm = ctx.getSystemService(android.os.PowerManager::class.java)
    var ok by remember { mutableStateOf(pm.isIgnoringBatteryOptimizations(ctx.packageName)) }
    LifecycleResumeEffect(Unit) { ok = pm.isIgnoringBatteryOptimizations(ctx.packageName); onPauseOrDispose { } }
    PermissionCard(Icons.Rounded.BatteryChargingFull, "Без ограничений батареи", "Чтобы система не останавливала остров в фоне.", ok) {
        runCatching {
            ctx.startActivity(Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS, Uri.parse("package:${ctx.packageName}")))
        }.onFailure { ctx.startActivity(Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)) }
    }
}

/** Изменение системных настроек — для кнопки «Включить автоповорот». */
@Composable
private fun WriteSettingsCard() {
    val ctx = LocalContext.current
    var ok by remember { mutableStateOf(Settings.System.canWrite(ctx)) }
    LifecycleResumeEffect(Unit) { ok = Settings.System.canWrite(ctx); onPauseOrDispose { } }
    PermissionCard(Icons.Rounded.ScreenRotation, "Изменение настроек", "Включать автоповорот из острова.", ok) {
        runCatching { ctx.startActivity(Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS, Uri.parse("package:${ctx.packageName}"))) }
    }
}

@Composable
private fun OptionalPermission(perm: String, icon: ImageVector, title: String, text: String) {
    val ctx = LocalContext.current
    var ok by remember { mutableStateOf(ctx.checkSelfPermission(perm) == PackageManager.PERMISSION_GRANTED) }
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { ok = it }
    PermissionCard(icon, title, text, ok) { launcher.launch(perm) }
}

@Composable
internal fun PermissionCard(icon: ImageVector, title: String, text: String, granted: Boolean, onGrant: () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (granted) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surfaceContainer,
        ),
        shape = RoundedCornerShape(24.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(Modifier.padding(horizontal = 14.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(if (granted) Icons.Rounded.CheckCircle else icon, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleSmall)
                Text(text, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            if (!granted) {
                Spacer(Modifier.width(12.dp))
                Button(onClick = onGrant) { Text("Включить") }
            }
        }
    }
}

@Composable
internal fun RestrictedSettingsHint() {
    val ctx = LocalContext.current
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer),
        shape = RoundedCornerShape(24.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(Modifier.padding(18.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Rounded.Info, null, tint = MaterialTheme.colorScheme.onTertiaryContainer)
                Spacer(Modifier.width(10.dp))
                Text("Переключатель серый?", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onTertiaryContainer)
            }
            Spacer(Modifier.height(6.dp))
            Text(
                "Android блокирует эти разрешения для приложений из APK. Откройте «О приложении» → ⋮ → " +
                    "«Разрешить ограниченные настройки», затем вернитесь сюда.",
                style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onTertiaryContainer,
            )
            TextButton(
                onClick = { ctx.startActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:${ctx.packageName}"))) },
                contentPadding = PaddingValues(horizontal = 0.dp),
            ) { Text("Открыть «О приложении»") }
        }
    }
}
