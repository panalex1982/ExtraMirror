package com.bue.extramirror.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.SharedPreferencesMigration
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.bue.extramirror.util.MeasureUnit
import java.io.IOException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

/** Name of the legacy SharedPreferences file (`ExtraMirrorSharedPreferences.PREFS_NAME`). */
private const val LEGACY_PREFS_NAME = "ExtraMirrorSharedPreferences"

/**
 * Settings live in Preferences DataStore. Users updating from the legacy app keep their
 * settings: the old SharedPreferences file is migrated once, and the key names and types
 * below are the legacy ones so the migrated values are picked up as they are.
 */
val Context.settingsDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "settings",
    produceMigrations = { context -> listOf(SharedPreferencesMigration(context, LEGACY_PREFS_NAME)) },
)

class SettingsRepository(private val dataStore: DataStore<Preferences>) {

    val settings: Flow<AppSettings> = dataStore.data
        .catch { error -> if (error is IOException) emit(emptyPreferences()) else throw error }
        .map { it.toAppSettings() }

    suspend fun save(settings: AppSettings) {
        dataStore.edit { prefs ->
            prefs[Keys.MEASURE_UNIT] = settings.measureUnit.ordinal
            prefs[Keys.ENERGY_SAVING] = !settings.keepScreenOn
            prefs[Keys.BRIGHTNESS] = settings.brightness
            prefs[Keys.SPEED_INDICATOR] = settings.speedometerMode.ordinal
            prefs[Keys.SHOW_INTRO] = settings.showIntro
            prefs[Keys.CAMERA_ENABLED] = settings.cameraEnabled
            prefs[Keys.ACTIVE_CAMERA] = settings.lastLens.ordinal
            prefs[Keys.PERMISSIONS_EXPLAINED] = settings.permissionsExplained
        }
    }

    suspend fun setLastLens(lens: CameraLens) {
        dataStore.edit { it[Keys.ACTIVE_CAMERA] = lens.ordinal }
    }

    suspend fun setPermissionsExplained() {
        dataStore.edit { it[Keys.PERMISSIONS_EXPLAINED] = true }
    }

    private fun Preferences.toAppSettings() = AppSettings(
        measureUnit = MeasureUnit.entries.getOrElse(this[Keys.MEASURE_UNIT] ?: 0) { MeasureUnit.METRIC },
        // Legacy "energy saving" meant "let the screen turn off".
        keepScreenOn = !(this[Keys.ENERGY_SAVING] ?: false),
        brightness = (this[Keys.BRIGHTNESS] ?: AppSettings.DEFAULT_BRIGHTNESS)
            .coerceIn(AppSettings.MIN_BRIGHTNESS, 1f),
        speedometerMode = SpeedometerMode.entries.getOrElse(this[Keys.SPEED_INDICATOR] ?: 0) {
            SpeedometerMode.ANALOG
        },
        showIntro = this[Keys.SHOW_INTRO] ?: true,
        cameraEnabled = this[Keys.CAMERA_ENABLED] ?: true,
        // The legacy app stored the hardware camera index here (usually 0 = back, 1 = front,
        // 2 = none), which lines up with the CameraLens ordinals for the common case.
        lastLens = if (this[Keys.ACTIVE_CAMERA] == CameraLens.MIRROR_VIEW.ordinal) {
            CameraLens.MIRROR_VIEW
        } else {
            CameraLens.FRONT_VIEW
        },
        permissionsExplained = this[Keys.PERMISSIONS_EXPLAINED] ?: false,
    )

    private object Keys {
        // Legacy key names from ExtraMirrorSharedPreferences.java.
        val MEASURE_UNIT = intPreferencesKey("MeasureUnit")
        val ENERGY_SAVING = booleanPreferencesKey("EnergySaving")
        val BRIGHTNESS = floatPreferencesKey("Brightness")
        val SPEED_INDICATOR = intPreferencesKey("SpeedIndicator")
        val SHOW_INTRO = booleanPreferencesKey("ShowIntro")
        val ACTIVE_CAMERA = intPreferencesKey("LastActiveCamera")
        val CAMERA_ENABLED = booleanPreferencesKey("isCameraEnabled")

        // New in the Compose port.
        val PERMISSIONS_EXPLAINED = booleanPreferencesKey("PermissionsExplained")
    }
}
