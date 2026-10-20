package com.bue.extramirror.ui.privacy

import androidx.activity.compose.BackHandler
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bue.extramirror.BuildConfig
import com.bue.extramirror.R
import com.bue.extramirror.ads.AdsConsentManager

private val Sections = listOf(
    R.string.privacy_camera_title to R.string.privacy_camera_body,
    R.string.privacy_location_title to R.string.privacy_location_body,
    R.string.privacy_settings_title to R.string.privacy_settings_body,
    R.string.privacy_ads_title to R.string.privacy_ads_body,
    R.string.privacy_analytics_title to R.string.privacy_analytics_body,
    R.string.privacy_permissions_title to R.string.privacy_permissions_body,
)

/**
 * In-app privacy policy (Google Play requires it to be reachable from inside the app when
 * the app uses sensitive permissions or ads), with a link to the full hosted policy.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrivacyPolicyScreen(
    adsConsentManager: AdsConsentManager,
    onBack: () -> Unit,
    onPrivacyChoices: () -> Unit,
) {
    BackHandler(onBack = onBack)
    val uriHandler = LocalUriHandler.current
    val privacyOptionsRequired by adsConsentManager.privacyOptionsRequired.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.privacy_policy)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
                    }
                },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(stringResource(R.string.privacy_intro), style = MaterialTheme.typography.bodyLarge)
            Sections.forEach { (title, body) -> PolicySection(title, body) }
            Button(
                onClick = {
                    // Fails only without a browser; the summary above is still shown then.
                    runCatching { uriHandler.openUri(BuildConfig.PRIVACY_POLICY_URL) }
                },
            ) {
                Text(stringResource(R.string.privacy_open_full))
            }
            if (privacyOptionsRequired) {
                OutlinedButton(onClick = onPrivacyChoices) {
                    Text(stringResource(R.string.menu_privacy_choices))
                }
            }
        }
    }
}

@Composable
private fun PolicySection(@StringRes title: Int, @StringRes body: Int) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            stringResource(title),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary,
        )
        Text(stringResource(body), style = MaterialTheme.typography.bodyMedium)
    }
}
