package com.bue.extramirror.trip

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

enum class MotionState { ENGINE_OFF, STANDING, MOVING }

data class TripState(
    val engineOn: Boolean = false,
    val motion: MotionState = MotionState.ENGINE_OFF,
    val standingMillis: Long = 0L,
    val movingMillis: Long = 0L,
    val distanceMeters: Float = 0f,
    val speedMs: Float = 0f,
    val altitudeMeters: Double? = null,
    val hasFix: Boolean = false,
)

/**
 * Port of the legacy `TimerRunnable` plus the motion logic from `MirrorActivity`.
 *
 * The "engine" toggle starts the trip: from then on every second is counted as standing or
 * moving time depending on the GPS speed, and distance is integrated from the speed while
 * moving. Turning the engine off freezes the counters.
 *
 * Pure Kotlin so it can be unit tested on the JVM; the caller drives it with [tick] and
 * [onLocation].
 */
class TripTracker(initial: TripState = TripState()) {

    private val _state = MutableStateFlow(initial)
    val state: StateFlow<TripState> = _state.asStateFlow()

    fun setEngine(on: Boolean) = _state.update { trip ->
        when {
            on == trip.engineOn -> trip
            on -> trip.copy(engineOn = true, motion = motionFor(trip.speedMs))
            else -> trip.copy(engineOn = false, motion = MotionState.ENGINE_OFF)
        }
    }

    fun onLocation(speedMs: Float, altitudeMeters: Double?) = _state.update { trip ->
        val speed = speedMs.coerceAtLeast(0f)
        trip.copy(
            speedMs = speed,
            altitudeMeters = altitudeMeters ?: trip.altitudeMeters,
            hasFix = true,
            motion = if (trip.engineOn) motionFor(speed) else MotionState.ENGINE_OFF,
        )
    }

    /** Advances the trip clock by [elapsedMillis] of real time. */
    fun tick(elapsedMillis: Long) {
        if (elapsedMillis <= 0) return
        _state.update { trip ->
            when (trip.motion) {
                MotionState.ENGINE_OFF -> trip
                MotionState.STANDING -> trip.copy(standingMillis = trip.standingMillis + elapsedMillis)
                MotionState.MOVING -> trip.copy(
                    movingMillis = trip.movingMillis + elapsedMillis,
                    distanceMeters = trip.distanceMeters + trip.speedMs * elapsedMillis / 1000f,
                )
            }
        }
    }

    private fun motionFor(speedMs: Float) =
        if (speedMs >= MOVING_THRESHOLD_MS) MotionState.MOVING else MotionState.STANDING

    companion object {
        /**
         * GPS reports a few tenths of m/s of jitter while parked. The legacy app treated any
         * non-zero speed as moving; ~1.8 km/h avoids counting that noise as driving.
         */
        const val MOVING_THRESHOLD_MS = 0.5f
    }
}
