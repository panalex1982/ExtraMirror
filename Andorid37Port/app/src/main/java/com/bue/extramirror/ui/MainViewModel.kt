package com.bue.extramirror.ui

import android.os.SystemClock
import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.bue.extramirror.ExtraMirrorApplication
import com.bue.extramirror.data.AppSettings
import com.bue.extramirror.data.CameraLens
import com.bue.extramirror.data.SettingsRepository
import com.bue.extramirror.location.GpsLocationSource
import com.bue.extramirror.trip.MotionState
import com.bue.extramirror.trip.TripState
import com.bue.extramirror.trip.TripTracker
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class MainViewModel(
    private val savedState: SavedStateHandle,
    private val settingsRepository: SettingsRepository,
    val gps: GpsLocationSource,
) : ViewModel() {

    /** `null` until the stored settings are loaded (decides whether the intro is shown). */
    val settings: StateFlow<AppSettings?> = settingsRepository.settings
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    private val tracker = TripTracker(restoreTrip())
    val trip: StateFlow<TripState> = tracker.state

    fun setEngine(on: Boolean) {
        tracker.setEngine(on)
        saveTrip()
    }

    fun saveSettings(settings: AppSettings) {
        viewModelScope.launch { settingsRepository.save(settings) }
    }

    fun selectLens(lens: CameraLens) {
        viewModelScope.launch { settingsRepository.setLastLens(lens) }
    }

    fun markPermissionsExplained() {
        viewModelScope.launch { settingsRepository.setPermissionsExplained() }
    }

    /**
     * Runs the trip clock and, if [useGps], the GPS until the calling coroutine is cancelled.
     * The mirror screen calls this only while it is visible: without a location foreground
     * service Android does not deliver location to a background app, so counting time with a
     * stale speed would produce wrong distances.
     */
    suspend fun trackTrip(useGps: Boolean) = coroutineScope {
        if (useGps) {
            launch {
                gps.fixes()
                    .catch { error -> Log.w(TAG, "GPS updates stopped", error) }
                    .collect { fix -> tracker.onLocation(fix.speedMs, fix.altitudeMeters) }
            }
        }
        var last = SystemClock.elapsedRealtime()
        while (isActive) {
            delay(TICK_MS)
            val now = SystemClock.elapsedRealtime()
            tracker.tick(now - last)
            last = now
            saveTrip()
        }
    }

    // The legacy activity saved the trip in onSaveInstanceState; SavedStateHandle does the
    // same and survives process death while the app is in the background.
    private fun saveTrip() {
        val trip = tracker.state.value
        savedState[KEY_ENGINE_ON] = trip.engineOn
        savedState[KEY_STANDING] = trip.standingMillis
        savedState[KEY_MOVING] = trip.movingMillis
        savedState[KEY_DISTANCE] = trip.distanceMeters
    }

    private fun restoreTrip(): TripState {
        val engineOn = savedState.get<Boolean>(KEY_ENGINE_ON) ?: false
        return TripState(
            engineOn = engineOn,
            motion = if (engineOn) MotionState.STANDING else MotionState.ENGINE_OFF,
            standingMillis = savedState.get<Long>(KEY_STANDING) ?: 0L,
            movingMillis = savedState.get<Long>(KEY_MOVING) ?: 0L,
            distanceMeters = savedState.get<Float>(KEY_DISTANCE) ?: 0f,
        )
    }

    companion object {
        private const val TAG = "MainViewModel"
        private const val TICK_MS = 1_000L
        private const val KEY_ENGINE_ON = "engineState"
        private const val KEY_STANDING = "idleTime"
        private const val KEY_MOVING = "drivingTime"
        private const val KEY_DISTANCE = "drivingDistance"

        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as ExtraMirrorApplication
                MainViewModel(
                    savedState = createSavedStateHandle(),
                    settingsRepository = app.settingsRepository,
                    gps = GpsLocationSource(app),
                )
            }
        }
    }
}
