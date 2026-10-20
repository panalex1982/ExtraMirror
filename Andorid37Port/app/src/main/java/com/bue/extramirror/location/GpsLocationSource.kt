package com.bue.extramirror.location

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Build
import androidx.core.content.ContextCompat
import androidx.core.location.LocationListenerCompat
import androidx.core.location.LocationManagerCompat
import androidx.core.location.LocationRequestCompat
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.emptyFlow

data class GpsFix(val speedMs: Float, val altitudeMeters: Double?)

/**
 * GPS fixes from the platform [LocationManager], as the legacy app used. No Google Play
 * services location API is involved, so location never leaves the device.
 */
class GpsLocationSource(context: Context) {

    private val appContext = context.applicationContext
    private val locationManager = appContext.getSystemService(LocationManager::class.java)

    val hasGpsHardware: Boolean =
        appContext.packageManager.hasSystemFeature(PackageManager.FEATURE_LOCATION_GPS)

    fun isLocationEnabled(): Boolean =
        locationManager != null && LocationManagerCompat.isLocationEnabled(locationManager)

    /**
     * Emits a fix about once a second while collected. Requires ACCESS_FINE_LOCATION: with
     * approximate location only there is no usable speed, so nothing is emitted.
     */
    @SuppressLint("MissingPermission") // Checked right below; revocation kills the process.
    fun fixes(): Flow<GpsFix> {
        val manager = locationManager ?: return emptyFlow()
        if (!hasGpsHardware || !hasFineLocationPermission()) return emptyFlow()

        return callbackFlow {
            val listener = LocationListenerCompat { location -> trySend(location.toFix()) }
            val request = LocationRequestCompat.Builder(UPDATE_INTERVAL_MS)
                .setQuality(LocationRequestCompat.QUALITY_HIGH_ACCURACY)
                .setMinUpdateDistanceMeters(0f)
                .build()
            try {
                LocationManagerCompat.requestLocationUpdates(
                    manager,
                    LocationManager.GPS_PROVIDER,
                    request,
                    ContextCompat.getMainExecutor(appContext),
                    listener,
                )
            } catch (e: SecurityException) {
                close(e)
            } catch (e: IllegalArgumentException) {
                // No GPS provider on this device.
                close(e)
            }
            awaitClose { LocationManagerCompat.removeUpdates(manager, listener) }
        }
    }

    private fun hasFineLocationPermission() = ContextCompat.checkSelfPermission(
        appContext,
        Manifest.permission.ACCESS_FINE_LOCATION,
    ) == PackageManager.PERMISSION_GRANTED

    private fun Location.toFix() = GpsFix(
        speedMs = if (hasSpeed()) speed else 0f,
        altitudeMeters = when {
            // Mean-sea-level altitude (Android 14+) matches what people expect from an
            // altimeter better than the raw WGS84 ellipsoid height.
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE && hasMslAltitude() ->
                mslAltitudeMeters
            hasAltitude() -> altitude
            else -> null
        },
    )

    private companion object {
        const val UPDATE_INTERVAL_MS = 1_000L
    }
}
