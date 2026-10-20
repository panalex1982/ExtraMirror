package com.bue.extramirror.ui.mirror

import android.content.pm.ActivityInfo
import android.view.WindowManager
import androidx.activity.compose.LocalActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat

/**
 * Window behaviour of the legacy MirrorActivity, with today's APIs:
 * - immersive full screen through WindowInsetsController (FLAG_FULLSCREEN is deprecated and
 *   the app is always edge-to-edge when targeting Android 15+);
 * - per-window brightness and keep-screen-on (no WRITE_SETTINGS / WAKE_LOCK permission needed);
 * - orientation locked to the current one so the view does not flip in a car mount.
 *   Note: when targeting API 37 this lock is ignored on large screens (smallest width >=
 *   600dp), which is why the mirror layout adapts to any size.
 */
@Composable
fun MirrorWindowEffects(brightness: Float, keepScreenOn: Boolean) {
    val activity = LocalActivity.current ?: return
    val view = LocalView.current

    DisposableEffect(activity, view) {
        val controller = WindowCompat.getInsetsController(activity.window, view)
        controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        controller.hide(WindowInsetsCompat.Type.systemBars())

        val previousOrientation = activity.requestedOrientation
        activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LOCKED

        onDispose {
            controller.show(WindowInsetsCompat.Type.systemBars())
            activity.requestedOrientation = previousOrientation
            activity.window.attributes = activity.window.attributes.apply {
                screenBrightness = WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_NONE
            }
        }
    }

    DisposableEffect(activity, brightness) {
        activity.window.attributes = activity.window.attributes.apply { screenBrightness = brightness }
        onDispose { }
    }

    DisposableEffect(view, keepScreenOn) {
        view.keepScreenOn = keepScreenOn
        onDispose { view.keepScreenOn = false }
    }
}
