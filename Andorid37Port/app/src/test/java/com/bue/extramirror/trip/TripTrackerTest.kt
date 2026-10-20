package com.bue.extramirror.trip

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class TripTrackerTest {

    @Test
    fun `nothing is counted while the engine is off`() {
        val tracker = TripTracker()
        tracker.onLocation(speedMs = 20f, altitudeMeters = 100.0)
        tracker.tick(5_000)

        val trip = tracker.state.value
        assertEquals(MotionState.ENGINE_OFF, trip.motion)
        assertEquals(0L, trip.standingMillis)
        assertEquals(0L, trip.movingMillis)
        assertEquals(0f, trip.distanceMeters, 0f)
        assertEquals(20f, trip.speedMs, 0f)
        assertTrue(trip.hasFix)
    }

    @Test
    fun `engine on while stopped counts standing time`() {
        val tracker = TripTracker()
        tracker.setEngine(true)
        tracker.tick(3_000)

        val trip = tracker.state.value
        assertEquals(MotionState.STANDING, trip.motion)
        assertEquals(3_000L, trip.standingMillis)
        assertEquals(0L, trip.movingMillis)
    }

    @Test
    fun `moving counts moving time and integrates distance`() {
        val tracker = TripTracker()
        tracker.setEngine(true)
        tracker.onLocation(speedMs = 10f, altitudeMeters = null)
        repeat(4) { tracker.tick(1_000) }

        val trip = tracker.state.value
        assertEquals(MotionState.MOVING, trip.motion)
        assertEquals(4_000L, trip.movingMillis)
        assertEquals(40f, trip.distanceMeters, 0.001f)
    }

    @Test
    fun `gps jitter below the threshold is standing`() {
        val tracker = TripTracker()
        tracker.setEngine(true)
        tracker.onLocation(speedMs = 0.3f, altitudeMeters = null)
        tracker.tick(1_000)

        val trip = tracker.state.value
        assertEquals(MotionState.STANDING, trip.motion)
        assertEquals(1_000L, trip.standingMillis)
        assertEquals(0f, trip.distanceMeters, 0f)
    }

    @Test
    fun `stopping and turning the engine off freezes the counters`() {
        val tracker = TripTracker()
        tracker.setEngine(true)
        tracker.onLocation(speedMs = 15f, altitudeMeters = 50.0)
        tracker.tick(2_000)
        tracker.onLocation(speedMs = 0f, altitudeMeters = null)
        tracker.tick(1_000)
        tracker.setEngine(false)
        tracker.tick(10_000)

        val trip = tracker.state.value
        assertFalse(trip.engineOn)
        assertEquals(MotionState.ENGINE_OFF, trip.motion)
        assertEquals(2_000L, trip.movingMillis)
        assertEquals(1_000L, trip.standingMillis)
        assertEquals(30f, trip.distanceMeters, 0.001f)
        // A fix without altitude keeps the last known altitude.
        assertEquals(50.0, trip.altitudeMeters!!, 0.0)
    }

    @Test
    fun `turning the engine on while already driving starts in moving state`() {
        val tracker = TripTracker()
        tracker.onLocation(speedMs = 25f, altitudeMeters = null)
        tracker.setEngine(true)

        assertEquals(MotionState.MOVING, tracker.state.value.motion)
    }

    @Test
    fun `restored state keeps counting from where it was`() {
        val tracker = TripTracker(
            TripState(engineOn = true, motion = MotionState.STANDING, standingMillis = 60_000, distanceMeters = 1_000f),
        )
        tracker.tick(1_000)

        assertEquals(61_000L, tracker.state.value.standingMillis)
        assertEquals(1_000f, tracker.state.value.distanceMeters, 0f)
    }
}
