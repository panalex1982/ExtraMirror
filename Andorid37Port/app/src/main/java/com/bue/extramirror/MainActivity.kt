package com.bue.extramirror

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.bue.extramirror.ui.ExtraMirrorApp
import com.bue.extramirror.ui.theme.ExtraMirrorTheme

/**
 * Single activity replacing the legacy IntroActivity + MirrorActivity pair. Navigation
 * between the intro, mirror and privacy screens happens in Compose.
 */
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Mandatory for apps targeting Android 15+; the mirror screen additionally hides the
        // system bars (replaces the deprecated FLAG_FULLSCREEN).
        enableEdgeToEdge()

        // Every launch, as UMP requires; it only shows a form when one is actually needed.
        val app = application as ExtraMirrorApplication
        app.adsConsentManager.gatherConsent(this)

        setContent {
            ExtraMirrorTheme {
                ExtraMirrorApp(
                    adsConsentManager = app.adsConsentManager,
                    onExit = ::finish,
                )
            }
        }
    }
}
