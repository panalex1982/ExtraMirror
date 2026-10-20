package com.bue.extramirror.data

import com.bue.extramirror.util.MeasureUnit

enum class SpeedometerMode { ANALOG, DIGITAL }

/**
 * Which camera is shown. Named after the legacy UI: "Front" looks at the road through the
 * back lens, "Mirror" uses the selfie lens to look behind the driver.
 */
enum class CameraLens { FRONT_VIEW, MIRROR_VIEW }

data class AppSettings(
    val measureUnit: MeasureUnit = MeasureUnit.METRIC,
    val keepScreenOn: Boolean = true,
    val brightness: Float = DEFAULT_BRIGHTNESS,
    val speedometerMode: SpeedometerMode = SpeedometerMode.ANALOG,
    val showIntro: Boolean = true,
    val cameraEnabled: Boolean = true,
    val lastLens: CameraLens = CameraLens.FRONT_VIEW,
    /** Whether the one-time camera/location explanation has been shown. */
    val permissionsExplained: Boolean = false,
) {
    companion object {
        const val DEFAULT_BRIGHTNESS = 1f
        const val MIN_BRIGHTNESS = 0.2f
    }
}
