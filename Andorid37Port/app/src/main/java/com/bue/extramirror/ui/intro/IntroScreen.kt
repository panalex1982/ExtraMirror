package com.bue.extramirror.ui.intro

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bue.extramirror.R
import com.bue.extramirror.ads.AdsConsentManager
import com.bue.extramirror.ads.BannerAd
import kotlinx.coroutines.delay

/** Frames and durations of the legacy `intro_animation.xml` animation-list. */
private val IntroFrames = listOf(
    R.drawable.zoom_animation1 to 1000L,
    R.drawable.zoom_animation2 to 800L,
    R.drawable.zoom_animation3 to 500L,
    R.drawable.zoom_animation4 to 500L,
    R.drawable.zoom_animation5 to 1000L,
    R.drawable.zoom_animation6 to 500L,
    R.drawable.zoom_animation7 to 500L,
    R.drawable.zoom_animation8 to 500L,
)

/** Port of the legacy IntroActivity: zoom instructions animation and the start button. */
@Composable
fun IntroScreen(
    adsConsentManager: AdsConsentManager,
    onStart: () -> Unit,
    onOpenPrivacyPolicy: () -> Unit,
) {
    val canShowAds by adsConsentManager.canShowAds.collectAsStateWithLifecycle()
    var frame by remember { mutableIntStateOf(0) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(IntroFrames[frame].second)
            frame = (frame + 1) % IntroFrames.size
        }
    }

    BoxWithConstraints(
        Modifier
            .fillMaxSize()
            .background(Color.Black)
            .windowInsetsPadding(WindowInsets.safeDrawing),
    ) {
        val landscape = maxWidth > maxHeight
        Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
            Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                Image(
                    painter = painterResource(IntroFrames[frame].first),
                    contentDescription = stringResource(R.string.zoom_instructions_image),
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.fillMaxSize(),
                )
                // Landscape: the start button sits on top of the animation, as before.
                if (landscape) StartButton(onStart)
            }
            Text(
                text = stringResource(R.string.zoom_instructions),
                color = Color.White,
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp),
            )
            if (!landscape) {
                StartButton(onStart, Modifier.fillMaxWidth().padding(horizontal = 24.dp))
            }
            TextButton(onClick = onOpenPrivacyPolicy) {
                Text(stringResource(R.string.privacy_policy))
            }
            if (canShowAds) BannerAd()
        }
    }
}

@Composable
private fun StartButton(onStart: () -> Unit, modifier: Modifier = Modifier) {
    Button(onClick = onStart, modifier = modifier) {
        Text(stringResource(R.string.start_extra_mirror))
    }
}
