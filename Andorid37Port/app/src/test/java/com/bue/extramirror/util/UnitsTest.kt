package com.bue.extramirror.util

import java.util.Locale
import org.junit.AfterClass
import org.junit.Assert.assertEquals
import org.junit.BeforeClass
import org.junit.Test

class UnitsTest {

    @Test
    fun `durations use the legacy H-MM-SS format`() {
        assertEquals("0:00:00", Units.formatDuration(0))
        assertEquals("0:00:59", Units.formatDuration(59_999))
        assertEquals("1:02:05", Units.formatDuration(3_725_000))
        assertEquals("27:46:40", Units.formatDuration(100_000_000))
    }

    @Test
    fun `speed is shown in kmh or mph`() {
        assertEquals(100, Units.displaySpeed(100f, MeasureUnit.METRIC))
        assertEquals(62, Units.displaySpeed(100f, MeasureUnit.IMPERIAL))
        assertEquals(72, Units.displaySpeed(Units.msToKmh(20f), MeasureUnit.METRIC))
    }

    @Test
    fun `distance has one decimal in km or miles`() {
        assertEquals("12.3", Units.formatDistance(12_345f, MeasureUnit.METRIC))
        assertEquals("7.7", Units.formatDistance(12_345f, MeasureUnit.IMPERIAL))
        assertEquals("0", Units.formatDistance(0f, MeasureUnit.METRIC))
    }

    @Test
    fun `altitude is in meters or feet`() {
        assertEquals("100", Units.formatAltitude(100.0, MeasureUnit.METRIC))
        assertEquals("328.1", Units.formatAltitude(100.0, MeasureUnit.IMPERIAL))
    }

    @Test
    fun `mph scale lands on the kmh needle angle`() {
        assertEquals(160.9344f, Units.milesToKm(100f), 0.001f)
        assertEquals(100f, Units.kmToMiles(Units.milesToKm(100f)), 0.01f)
    }

    companion object {
        private lateinit var defaultLocale: Locale

        // DecimalFormat is locale dependent (as in the legacy app); pin it for the assertions.
        @BeforeClass
        @JvmStatic
        fun pinLocale() {
            defaultLocale = Locale.getDefault()
            Locale.setDefault(Locale.US)
        }

        @AfterClass
        @JvmStatic
        fun restoreLocale() {
            Locale.setDefault(defaultLocale)
        }
    }
}
