package com.bue.extramirror.util

import java.text.DecimalFormat
import kotlin.math.roundToInt

/** Persisted by ordinal, matching the legacy `MeasureUnit` preference (0 = metric, 1 = imperial). */
enum class MeasureUnit { METRIC, IMPERIAL }

/** Port of the legacy `Utilities` and `Clock` helpers. */
object Units {
    const val KMH_PER_MS = 3.6f
    const val MILES_PER_KM = 0.62137f
    const val KM_PER_MILE = 1.609344f
    const val FEET_PER_METER = 3.2808

    fun msToKmh(speedMs: Float): Float = speedMs * KMH_PER_MS

    fun kmToMiles(km: Float): Float = km * MILES_PER_KM

    fun milesToKm(miles: Float): Float = miles * KM_PER_MILE

    /** Speed for display, in km/h or mph depending on [unit]. */
    fun displaySpeed(speedKmh: Float, unit: MeasureUnit): Int = when (unit) {
        MeasureUnit.METRIC -> speedKmh
        MeasureUnit.IMPERIAL -> kmToMiles(speedKmh)
    }.roundToInt()

    /** Distance with one decimal, in km or miles (legacy `#.#` format). */
    fun formatDistance(distanceMeters: Float, unit: MeasureUnit): String {
        val km = distanceMeters / 1000f
        val value = if (unit == MeasureUnit.METRIC) km else kmToMiles(km)
        return DecimalFormat("#.#").format(value)
    }

    /** Altitude with one decimal, in meters or feet, without the unit suffix. */
    fun formatAltitude(altitudeMeters: Double, unit: MeasureUnit): String {
        val value = if (unit == MeasureUnit.METRIC) altitudeMeters else altitudeMeters * FEET_PER_METER
        return DecimalFormat("#.#").format(value)
    }

    /** `H:MM:SS`, as the legacy `Clock.convertTime`. */
    fun formatDuration(millis: Long): String {
        val totalSeconds = millis.coerceAtLeast(0) / 1000
        val hours = totalSeconds / 3600
        val minutes = (totalSeconds % 3600) / 60
        val seconds = totalSeconds % 60
        return "%d:%02d:%02d".format(hours, minutes, seconds)
    }
}
