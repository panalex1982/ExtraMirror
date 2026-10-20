package com.bue.extramirror.ui.permissions

import android.Manifest
import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import android.util.Log
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect

/**
 * Runtime permission state for the two dangerous permissions the app uses.
 *
 * The legacy app targeted API 19 and got CAMERA and ACCESS_FINE_LOCATION at install time.
 * Now both are requested at runtime, the user can grant approximate location only
 * (Android 12+), grant "only this time" or have them auto-reset (Android 11+), so the state
 * is re-read every time the screen resumes.
 */
@Stable
class AppPermissionsState internal constructor(private val context: Context) {

    var cameraGranted by mutableStateOf(false)
        private set
    var fineLocationGranted by mutableStateOf(false)
        private set
    var coarseLocationGranted by mutableStateOf(false)
        private set

    /** Denied with "don't ask again": only the system settings screen can grant it now. */
    var cameraBlocked by mutableStateOf(false)
        private set
    var locationBlocked by mutableStateOf(false)
        private set

    val anyLocationGranted: Boolean get() = fineLocationGranted || coarseLocationGranted

    /** The user picked "Approximate": there is no usable GPS speed. */
    val approximateLocationOnly: Boolean get() = coarseLocationGranted && !fineLocationGranted

    internal var activity: Activity? = null
    internal lateinit var launcher: ManagedActivityResultLauncher<Array<String>, Map<String, Boolean>>

    init {
        refresh()
    }

    fun refresh() {
        cameraGranted = isGranted(Manifest.permission.CAMERA)
        fineLocationGranted = isGranted(Manifest.permission.ACCESS_FINE_LOCATION)
        coarseLocationGranted = isGranted(Manifest.permission.ACCESS_COARSE_LOCATION)
    }

    fun hasMissing(includeCamera: Boolean): Boolean =
        !fineLocationGranted || (includeCamera && !cameraGranted)

    /** Requests everything still missing in a single system dialog sequence. */
    fun requestMissing(includeCamera: Boolean) {
        val missing = buildList {
            if (includeCamera && !cameraGranted) add(Manifest.permission.CAMERA)
            if (!fineLocationGranted) addAll(LOCATION_PERMISSIONS)
        }
        if (missing.isNotEmpty()) launcher.launch(missing.toTypedArray())
    }

    fun requestCamera() {
        if (cameraBlocked) openAppSettings() else launcher.launch(arrayOf(Manifest.permission.CAMERA))
    }

    /**
     * Since Android 12 FINE must be requested together with COARSE. If only approximate
     * location is granted, the same request asks the user to upgrade to precise.
     */
    fun requestLocation() {
        if (locationBlocked) openAppSettings() else launcher.launch(LOCATION_PERMISSIONS.toTypedArray())
    }

    fun openAppSettings() {
        val intent = Intent(
            Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
            Uri.fromParts("package", context.packageName, null),
        ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivitySafely(intent)
    }

    fun openLocationSettings() {
        startActivitySafely(
            Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
        )
    }

    internal fun onResult(result: Map<String, Boolean>) {
        refresh()
        val activity = activity ?: return
        if (Manifest.permission.CAMERA in result) {
            cameraBlocked = !cameraGranted && !ActivityCompat.shouldShowRequestPermissionRationale(
                activity,
                Manifest.permission.CAMERA,
            )
        }
        if (Manifest.permission.ACCESS_FINE_LOCATION in result) {
            locationBlocked = !fineLocationGranted && !ActivityCompat.shouldShowRequestPermissionRationale(
                activity,
                Manifest.permission.ACCESS_FINE_LOCATION,
            )
        }
    }

    private fun isGranted(permission: String) =
        ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED

    private fun startActivitySafely(intent: Intent) {
        try {
            context.startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            Log.w("AppPermissions", "No activity for ${intent.action}", e)
        }
    }

    private companion object {
        val LOCATION_PERMISSIONS = listOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
        )
    }
}

@Composable
fun rememberAppPermissionsState(): AppPermissionsState {
    val context = LocalContext.current
    val state = remember(context) { AppPermissionsState(context) }
    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions(),
        state::onResult,
    )
    state.activity = LocalActivity.current
    state.launcher = launcher
    // Permissions can change in system settings, or be auto-revoked, while we are paused.
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) { state.refresh() }
    return state
}
