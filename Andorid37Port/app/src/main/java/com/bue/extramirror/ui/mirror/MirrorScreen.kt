package com.bue.extramirror.ui.mirror

import android.text.format.DateFormat
import androidx.activity.compose.BackHandler
import androidx.activity.compose.LocalActivity
import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.repeatOnLifecycle
import com.bue.extramirror.R
import com.bue.extramirror.ads.AdsConsentManager
import com.bue.extramirror.ads.BannerAd
import com.bue.extramirror.data.AppSettings
import com.bue.extramirror.data.CameraLens
import com.bue.extramirror.trip.TripState
import com.bue.extramirror.ui.MainViewModel
import com.bue.extramirror.ui.camera.CameraMirror
import com.bue.extramirror.ui.camera.availableLenses
import com.bue.extramirror.ui.permissions.AppPermissionsState
import com.bue.extramirror.ui.permissions.rememberAppPermissionsState
import com.bue.extramirror.ui.settings.SettingsDialog
import com.bue.extramirror.util.MeasureUnit
import com.bue.extramirror.util.Units
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import kotlinx.coroutines.delay

private val ToggleSize = 60.dp
private val ControlsWidth = 132.dp
private val MenuWidth = 48.dp
private val ControlsHeight = 96.dp
private val AdHeight = 54.dp

@Composable
fun MirrorScreen(
    viewModel: MainViewModel,
    settings: AppSettings,
    adsConsentManager: AdsConsentManager,
    onOpenPrivacyPolicy: () -> Unit,
    onExit: () -> Unit,
) {
    val context = LocalContext.current
    val activity = LocalActivity.current
    val trip by viewModel.trip.collectAsStateWithLifecycle()
    val canShowAds by adsConsentManager.canShowAds.collectAsStateWithLifecycle()
    val privacyOptionsRequired by adsConsentManager.privacyOptionsRequired.collectAsStateWithLifecycle()
    val permissions = rememberAppPermissionsState()

    var showSettings by rememberSaveable { mutableStateOf(false) }
    var showExitConfirmation by rememberSaveable { mutableStateOf(false) }
    var showPermissionExplanation by rememberSaveable { mutableStateOf(false) }
    var previewBrightness by remember { mutableStateOf<Float?>(null) }
    var locationEnabled by remember { mutableStateOf(viewModel.gps.isLocationEnabled()) }
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        locationEnabled = viewModel.gps.isLocationEnabled()
    }

    MirrorWindowEffects(
        brightness = previewBrightness ?: settings.brightness,
        keepScreenOn = settings.keepScreenOn,
    )

    // Replaces the legacy onBackPressed() override, which is no longer called for apps
    // targeting Android 16+ (predictive back).
    BackHandler { showExitConfirmation = true }

    // Explain camera and location once, before the system permission dialogs.
    LaunchedEffect(settings.permissionsExplained) {
        if (!settings.permissionsExplained && permissions.hasMissing(settings.cameraEnabled)) {
            showPermissionExplanation = true
        }
    }

    // Trip clock and GPS run only while the screen is visible (no background location).
    val lifecycleOwner = LocalLifecycleOwner.current
    val useGps = permissions.fineLocationGranted
    LaunchedEffect(lifecycleOwner, useGps) {
        lifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
            viewModel.trackTrip(useGps)
        }
    }

    val lenses by produceState<Set<CameraLens>?>(initialValue = null) {
        value = availableLenses(context)
    }
    val lens = lenses?.let { available ->
        if (settings.lastLens in available) settings.lastLens else available.firstOrNull()
    }
    val cameraActive = settings.cameraEnabled && permissions.cameraGranted && lens != null
    val canSwitchCamera = cameraActive && lenses?.size == 2

    Box(
        Modifier
            .fillMaxSize()
            .background(Color.Black),
    ) {
        if (cameraActive && lens != null) {
            CameraMirror(lens = lens, modifier = Modifier.fillMaxSize())
        }

        MirrorOverlay(
            trip = trip,
            settings = settings,
            canShowAds = canShowAds,
            cameraIsMirror = lens == CameraLens.MIRROR_VIEW,
            canSwitchCamera = canSwitchCamera,
            onEngineToggle = viewModel::setEngine,
            onCameraToggle = { mirror ->
                viewModel.selectLens(if (mirror) CameraLens.MIRROR_VIEW else CameraLens.FRONT_VIEW)
            },
            statusMessages = {
                StatusMessages(
                    permissions = permissions,
                    trip = trip,
                    cameraWanted = settings.cameraEnabled && !lenses.isNullOrEmpty(),
                    hasGpsHardware = viewModel.gps.hasGpsHardware,
                    locationEnabled = locationEnabled,
                )
            },
            menu = { modifier ->
                OverflowMenu(
                    privacyOptionsRequired = privacyOptionsRequired,
                    onSettings = { showSettings = true },
                    onPrivacyPolicy = onOpenPrivacyPolicy,
                    onPrivacyChoices = { activity?.let(adsConsentManager::showPrivacyOptionsForm) },
                    onExit = onExit,
                    modifier = modifier,
                )
            },
        )
    }

    if (showSettings) {
        SettingsDialog(
            initial = settings,
            privacyOptionsRequired = privacyOptionsRequired,
            onBrightnessPreview = { previewBrightness = it },
            onCancel = {
                previewBrightness = null
                showSettings = false
            },
            onSave = { updated ->
                viewModel.saveSettings(updated)
                previewBrightness = null
                showSettings = false
            },
            onOpenPrivacyPolicy = {
                previewBrightness = null
                showSettings = false
                onOpenPrivacyPolicy()
            },
            onOpenPrivacyChoices = { activity?.let(adsConsentManager::showPrivacyOptionsForm) },
        )
    }

    if (showExitConfirmation) {
        AlertDialog(
            onDismissRequest = { showExitConfirmation = false },
            icon = { Icon(Icons.Filled.Warning, contentDescription = null) },
            title = { Text(stringResource(R.string.exit_title)) },
            text = { Text(stringResource(R.string.exit_message)) },
            confirmButton = { TextButton(onClick = onExit) { Text(stringResource(R.string.yes)) } },
            dismissButton = {
                TextButton(onClick = { showExitConfirmation = false }) { Text(stringResource(R.string.no)) }
            },
        )
    }

    if (showPermissionExplanation) {
        PermissionExplanationDialog(
            includeCamera = settings.cameraEnabled,
            onContinue = {
                showPermissionExplanation = false
                viewModel.markPermissionsExplained()
                permissions.requestMissing(includeCamera = settings.cameraEnabled)
            },
            onNotNow = {
                showPermissionExplanation = false
                viewModel.markPermissionsExplained()
            },
            onOpenPrivacyPolicy = onOpenPrivacyPolicy,
        )
    }
}

@Composable
private fun MirrorOverlay(
    trip: TripState,
    settings: AppSettings,
    canShowAds: Boolean,
    cameraIsMirror: Boolean,
    canSwitchCamera: Boolean,
    onEngineToggle: (Boolean) -> Unit,
    onCameraToggle: (Boolean) -> Unit,
    statusMessages: @Composable () -> Unit,
    menu: @Composable (Modifier) -> Unit,
) {
    val reading = SpeedometerReading(
        speedKmh = Units.msToKmh(trip.speedMs),
        distanceMeters = trip.distanceMeters,
        altitudeMeters = trip.altitudeMeters,
        standingMillis = trip.standingMillis,
        movingMillis = trip.movingMillis,
        motion = trip.motion,
        engineOn = trip.engineOn,
    )
    val labels = SpeedometerLabels(
        kmh = stringResource(R.string.unit_kmh),
        mph = stringResource(R.string.unit_mph),
        km = stringResource(R.string.unit_km),
        miles = stringResource(R.string.unit_mi),
        meters = stringResource(R.string.unit_m),
        feet = stringResource(R.string.unit_ft),
    )
    val metric = settings.measureUnit == MeasureUnit.METRIC
    val description = stringResource(
        R.string.speedometer_description,
        Units.displaySpeed(reading.speedKmh, settings.measureUnit).toString(),
        if (metric) labels.kmh else labels.mph,
        Units.formatDistance(reading.distanceMeters, settings.measureUnit),
        if (metric) labels.km else labels.miles,
        Units.formatDuration(reading.standingMillis),
        Units.formatDuration(reading.movingMillis),
    )

    // Draw inside the safe area (display cutouts, transient system bars).
    BoxWithConstraints(
        Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .padding(8.dp),
    ) {
        val landscape = maxWidth > maxHeight
        val adSpace = if (canShowAds) AdHeight else 0.dp
        val engineToggle = @Composable {
            EngineToggle(engineOn = trip.engineOn, onToggle = onEngineToggle)
        }
        val cameraToggle = @Composable {
            CameraToggle(isMirror = cameraIsMirror, enabled = canSwitchCamera, onToggle = onCameraToggle)
        }

        if (landscape) {
            // Legacy landscape layout: controls on the left, gauge next to them, menu on the right.
            val gaugeSize = minOf(maxHeight - adSpace, maxWidth - ControlsWidth - MenuWidth)
                .coerceAtLeast(0.dp)
            Row(Modifier.fillMaxSize().padding(bottom = adSpace)) {
                Column(
                    modifier = Modifier.width(ControlsWidth).fillMaxHeight(),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    engineToggle()
                    cameraToggle()
                    WatchText()
                }
                Speedometer(
                    reading = reading,
                    unit = settings.measureUnit,
                    mode = settings.speedometerMode,
                    contentDescription = description,
                    labels = labels,
                    modifier = Modifier.size(gaugeSize),
                )
                Column(
                    modifier = Modifier.weight(1f).padding(horizontal = 8.dp),
                    horizontalAlignment = Alignment.End,
                ) { statusMessages() }
            }
            menu(Modifier.align(Alignment.CenterEnd))
        } else {
            // Legacy portrait layout: gauge on top, camera switch + clock bottom left, engine
            // key bottom right.
            val gaugeSize = minOf(maxWidth - MenuWidth, maxHeight - adSpace - ControlsHeight)
                .coerceAtLeast(0.dp)
            Column(Modifier.fillMaxSize().padding(bottom = adSpace)) {
                Speedometer(
                    reading = reading,
                    unit = settings.measureUnit,
                    mode = settings.speedometerMode,
                    contentDescription = description,
                    labels = labels,
                    modifier = Modifier.size(gaugeSize),
                )
                Column(
                    modifier = Modifier.fillMaxWidth().weight(1f).padding(vertical = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) { statusMessages() }
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.Bottom) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        cameraToggle()
                        WatchText()
                    }
                    Spacer(Modifier.weight(1f))
                    engineToggle()
                }
            }
            menu(Modifier.align(Alignment.TopEnd))
        }

        if (canShowAds) {
            BannerAd(Modifier.align(Alignment.BottomCenter))
        }
    }
}

@Composable
private fun EngineToggle(engineOn: Boolean, onToggle: (Boolean) -> Unit) {
    ImageToggle(
        image = if (engineOn) R.drawable.ic_open_key else R.drawable.ic_close_key,
        description = stringResource(if (engineOn) R.string.engine_on else R.string.engine_off),
        checked = engineOn,
        enabled = true,
        onToggle = onToggle,
    )
}

@Composable
private fun CameraToggle(isMirror: Boolean, enabled: Boolean, onToggle: (Boolean) -> Unit) {
    ImageToggle(
        image = when {
            !enabled -> R.drawable.ic_camera_zero
            isMirror -> R.drawable.ic_camera_two
            else -> R.drawable.ic_camera_one
        },
        description = stringResource(
            when {
                !enabled -> R.string.camera_unavailable
                isMirror -> R.string.camera_mirror
                else -> R.string.camera_front
            },
        ),
        checked = isMirror,
        enabled = enabled,
        onToggle = onToggle,
    )
}

/** The legacy app used ToggleButtons with image selectors; same artwork here. */
@Composable
private fun ImageToggle(
    @DrawableRes image: Int,
    description: String,
    checked: Boolean,
    enabled: Boolean,
    onToggle: (Boolean) -> Unit,
) {
    Image(
        painter = painterResource(image),
        contentDescription = description,
        modifier = Modifier
            .size(ToggleSize)
            .clip(CircleShape)
            .toggleable(value = checked, enabled = enabled, role = Role.Switch, onValueChange = onToggle)
            .alpha(if (enabled) 1f else 0.6f),
    )
}

/** Clock line from the legacy layout ("time | date"), localized and 12/24h aware. */
@Composable
private fun WatchText() {
    val context = LocalContext.current
    val now by produceState(LocalDateTime.now()) {
        while (true) {
            delay(1_000L - System.currentTimeMillis() % 1_000L)
            value = LocalDateTime.now()
        }
    }
    val formatters = remember(context) {
        val locale = context.resources.configuration.locales[0]
        val skeleton = if (DateFormat.is24HourFormat(context)) "Hm" else "hm"
        DateTimeFormatter.ofPattern(DateFormat.getBestDateTimePattern(locale, skeleton), locale) to
            DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT).withLocale(locale)
    }
    Text(
        text = "${now.format(formatters.first)} | ${now.format(formatters.second)}",
        color = Color.White,
        style = MaterialTheme.typography.titleMedium.copy(
            fontWeight = FontWeight.SemiBold,
            shadow = Shadow(color = Color.Black, blurRadius = 6f),
        ),
    )
}

@Composable
private fun StatusMessages(
    permissions: AppPermissionsState,
    trip: TripState,
    cameraWanted: Boolean,
    hasGpsHardware: Boolean,
    locationEnabled: Boolean,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        if (cameraWanted && !permissions.cameraGranted) {
            StatusChip(
                text = stringResource(R.string.status_camera_needed),
                action = stringResource(
                    if (permissions.cameraBlocked) R.string.action_open_settings else R.string.action_allow,
                ),
                onAction = permissions::requestCamera,
            )
        }
        when {
            !hasGpsHardware -> StatusChip(stringResource(R.string.status_no_gps))
            !permissions.anyLocationGranted -> StatusChip(
                text = stringResource(R.string.status_location_needed),
                action = stringResource(
                    if (permissions.locationBlocked) R.string.action_open_settings else R.string.action_allow,
                ),
                onAction = permissions::requestLocation,
            )
            permissions.approximateLocationOnly -> StatusChip(
                text = stringResource(R.string.status_precise_needed),
                action = stringResource(
                    if (permissions.locationBlocked) R.string.action_open_settings else R.string.action_allow,
                ),
                onAction = permissions::requestLocation,
            )
            !locationEnabled -> StatusChip(
                text = stringResource(R.string.status_location_off),
                action = stringResource(R.string.action_turn_on),
                onAction = permissions::openLocationSettings,
            )
            !trip.hasFix -> StatusChip(stringResource(R.string.status_waiting_gps))
        }
    }
}

@Composable
private fun StatusChip(text: String, action: String? = null, onAction: (() -> Unit)? = null) {
    Surface(color = Color(0xCC000000), contentColor = Color.White, shape = RoundedCornerShape(12.dp)) {
        Row(
            modifier = Modifier.padding(start = 12.dp, end = 4.dp, top = 4.dp, bottom = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(text, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(vertical = 8.dp))
            if (action != null && onAction != null) {
                TextButton(onClick = onAction) { Text(action) }
            } else {
                Spacer(Modifier.width(8.dp))
            }
        }
    }
}

@Composable
private fun OverflowMenu(
    privacyOptionsRequired: Boolean,
    onSettings: () -> Unit,
    onPrivacyPolicy: () -> Unit,
    onPrivacyChoices: () -> Unit,
    onExit: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var expanded by remember { mutableStateOf(false) }
    Box(modifier) {
        IconButton(
            onClick = { expanded = true },
            colors = IconButtonDefaults.iconButtonColors(
                containerColor = Color(0x66000000),
                contentColor = Color.White,
            ),
        ) {
            Icon(Icons.Filled.MoreVert, contentDescription = stringResource(R.string.more_options))
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            DropdownMenuItem(
                text = { Text(stringResource(R.string.menu_settings)) },
                leadingIcon = { Icon(Icons.Filled.Settings, contentDescription = null) },
                onClick = {
                    expanded = false
                    onSettings()
                },
            )
            DropdownMenuItem(
                text = { Text(stringResource(R.string.privacy_policy)) },
                leadingIcon = { Icon(Icons.Filled.Info, contentDescription = null) },
                onClick = {
                    expanded = false
                    onPrivacyPolicy()
                },
            )
            if (privacyOptionsRequired) {
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.menu_privacy_choices)) },
                    leadingIcon = { Icon(Icons.Filled.Lock, contentDescription = null) },
                    onClick = {
                        expanded = false
                        onPrivacyChoices()
                    },
                )
            }
            DropdownMenuItem(
                text = { Text(stringResource(R.string.exit)) },
                leadingIcon = { Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = null) },
                onClick = {
                    expanded = false
                    onExit()
                },
            )
        }
    }
}

@Composable
private fun PermissionExplanationDialog(
    includeCamera: Boolean,
    onContinue: () -> Unit,
    onNotNow: () -> Unit,
    onOpenPrivacyPolicy: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onNotNow,
        title = { Text(stringResource(R.string.permissions_title)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                if (includeCamera) {
                    Text(stringResource(R.string.permissions_camera_reason))
                }
                Text(stringResource(R.string.permissions_location_reason))
                TextButton(onClick = onOpenPrivacyPolicy) { Text(stringResource(R.string.privacy_policy)) }
            }
        },
        confirmButton = {
            TextButton(onClick = onContinue) { Text(stringResource(R.string.permissions_continue)) }
        },
        dismissButton = {
            TextButton(onClick = onNotNow) { Text(stringResource(R.string.permissions_not_now)) }
        },
    )
}
