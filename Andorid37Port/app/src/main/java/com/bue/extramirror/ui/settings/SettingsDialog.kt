package com.bue.extramirror.ui.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.bue.extramirror.R
import com.bue.extramirror.data.AppSettings
import com.bue.extramirror.data.SpeedometerMode
import com.bue.extramirror.util.MeasureUnit

/**
 * Port of the legacy `SettingsDialogFragment`. Brightness is previewed live while dragging and
 * reverted on Cancel, as before. Changing "Enable camera" now applies immediately (the legacy
 * app needed a restart).
 */
@Composable
fun SettingsDialog(
    initial: AppSettings,
    privacyOptionsRequired: Boolean,
    onBrightnessPreview: (Float) -> Unit,
    onCancel: () -> Unit,
    onSave: (AppSettings) -> Unit,
    onOpenPrivacyPolicy: () -> Unit,
    onOpenPrivacyChoices: () -> Unit,
) {
    var draft by remember(initial) { mutableStateOf(initial) }

    AlertDialog(
        onDismissRequest = onCancel,
        title = { Text(stringResource(R.string.menu_settings)) },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState())) {
                SectionLabel(stringResource(R.string.measure_units))
                RadioRow(
                    label = stringResource(R.string.units_metric),
                    selected = draft.measureUnit == MeasureUnit.METRIC,
                    onSelect = { draft = draft.copy(measureUnit = MeasureUnit.METRIC) },
                )
                RadioRow(
                    label = stringResource(R.string.units_imperial),
                    selected = draft.measureUnit == MeasureUnit.IMPERIAL,
                    onSelect = { draft = draft.copy(measureUnit = MeasureUnit.IMPERIAL) },
                )

                SectionLabel(stringResource(R.string.brightness))
                Slider(
                    value = draft.brightness,
                    onValueChange = { value ->
                        draft = draft.copy(brightness = value)
                        onBrightnessPreview(value)
                    },
                    valueRange = AppSettings.MIN_BRIGHTNESS..1f,
                    steps = 7,
                )
                CheckRow(
                    label = stringResource(R.string.screen_always_on),
                    checked = draft.keepScreenOn,
                    onCheckedChange = { draft = draft.copy(keepScreenOn = it) },
                )

                SectionLabel(stringResource(R.string.speedometer_mode))
                RadioRow(
                    label = stringResource(R.string.speedometer_analog),
                    selected = draft.speedometerMode == SpeedometerMode.ANALOG,
                    onSelect = { draft = draft.copy(speedometerMode = SpeedometerMode.ANALOG) },
                )
                RadioRow(
                    label = stringResource(R.string.speedometer_digital),
                    selected = draft.speedometerMode == SpeedometerMode.DIGITAL,
                    onSelect = { draft = draft.copy(speedometerMode = SpeedometerMode.DIGITAL) },
                )

                HorizontalDivider(Modifier.padding(vertical = 8.dp))
                CheckRow(
                    label = stringResource(R.string.show_intro),
                    checked = draft.showIntro,
                    onCheckedChange = { draft = draft.copy(showIntro = it) },
                )
                CheckRow(
                    label = stringResource(R.string.enable_camera),
                    checked = draft.cameraEnabled,
                    onCheckedChange = { draft = draft.copy(cameraEnabled = it) },
                )

                HorizontalDivider(Modifier.padding(vertical = 8.dp))
                TextButton(onClick = onOpenPrivacyPolicy) {
                    Text(stringResource(R.string.privacy_policy))
                }
                if (privacyOptionsRequired) {
                    TextButton(onClick = onOpenPrivacyChoices) {
                        Text(stringResource(R.string.menu_privacy_choices))
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onSave(draft) }) { Text(stringResource(R.string.ok)) }
        },
        dismissButton = {
            TextButton(onClick = onCancel) { Text(stringResource(R.string.cancel)) }
        },
    )
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(top = 12.dp, bottom = 4.dp),
    )
}

@Composable
private fun RadioRow(label: String, selected: Boolean, onSelect: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .selectable(selected = selected, onClick = onSelect, role = Role.RadioButton)
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        RadioButton(selected = selected, onClick = null)
        Text(label, Modifier.padding(start = 8.dp))
    }
}

@Composable
private fun CheckRow(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .toggleable(value = checked, onValueChange = onCheckedChange, role = Role.Checkbox)
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Checkbox(checked = checked, onCheckedChange = null)
        Text(label, Modifier.padding(start = 8.dp))
    }
}
