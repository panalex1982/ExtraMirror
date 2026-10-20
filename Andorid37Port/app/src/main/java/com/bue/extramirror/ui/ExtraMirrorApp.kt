package com.bue.extramirror.ui

import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.bue.extramirror.ads.AdsConsentManager
import com.bue.extramirror.ui.intro.IntroScreen
import com.bue.extramirror.ui.mirror.MirrorScreen
import com.bue.extramirror.ui.privacy.PrivacyPolicyScreen

private enum class Screen { Intro, Mirror, Privacy }

private val BackStackSaver = listSaver<List<Screen>, String>(
    save = { stack -> stack.map { it.name } },
    restore = { names -> names.map { Screen.valueOf(it) } },
)

/**
 * Three screens don't need a navigation library: a tiny saveable back stack is enough.
 * Intro -> Mirror replaces the stack, so Back on the mirror screen asks to close the app,
 * exactly like the legacy app did.
 */
@Composable
fun ExtraMirrorApp(adsConsentManager: AdsConsentManager, onExit: () -> Unit) {
    val viewModel: MainViewModel = viewModel(factory = MainViewModel.Factory)
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val activity = LocalActivity.current

    val loaded = settings
    if (loaded == null) {
        Box(Modifier.fillMaxSize().background(Color.Black))
        return
    }

    var backStack by rememberSaveable(stateSaver = BackStackSaver) {
        mutableStateOf(listOf(if (loaded.showIntro) Screen.Intro else Screen.Mirror))
    }
    val openPrivacyPolicy: () -> Unit = { backStack = backStack + Screen.Privacy }
    val goBack: () -> Unit = {
        if (backStack.size > 1) {
            backStack = backStack.dropLast(1)
        } else {
            onExit()
        }
    }

    when (backStack.last()) {
        Screen.Intro -> IntroScreen(
            adsConsentManager = adsConsentManager,
            onStart = { backStack = listOf(Screen.Mirror) },
            onOpenPrivacyPolicy = openPrivacyPolicy,
        )
        Screen.Mirror -> MirrorScreen(
            viewModel = viewModel,
            settings = loaded,
            adsConsentManager = adsConsentManager,
            onOpenPrivacyPolicy = openPrivacyPolicy,
            onExit = onExit,
        )
        Screen.Privacy -> PrivacyPolicyScreen(
            adsConsentManager = adsConsentManager,
            onBack = goBack,
            onPrivacyChoices = { activity?.let(adsConsentManager::showPrivacyOptionsForm) },
        )
    }
}
