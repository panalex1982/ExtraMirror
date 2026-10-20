package com.bue.extramirror.ui.camera

import android.content.Context
import android.widget.Toast
import androidx.camera.compose.CameraXViewfinder
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.core.SurfaceRequest
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.lifecycle.awaitInstance
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.bue.extramirror.R
import com.bue.extramirror.data.CameraLens
import kotlinx.coroutines.awaitCancellation

/** Lenses present on this device, mapped to the legacy "Front" / "Mirror" views. */
suspend fun availableLenses(context: Context): Set<CameraLens> {
    val provider = runCatching { ProcessCameraProvider.awaitInstance(context.applicationContext) }
        .getOrNull() ?: return emptySet()
    return buildSet {
        if (provider.hasCameraSafely(CameraSelector.DEFAULT_BACK_CAMERA)) add(CameraLens.FRONT_VIEW)
        if (provider.hasCameraSafely(CameraSelector.DEFAULT_FRONT_CAMERA)) add(CameraLens.MIRROR_VIEW)
    }
}

private fun ProcessCameraProvider.hasCameraSafely(selector: CameraSelector) =
    runCatching { hasCamera(selector) }.getOrDefault(false)

/**
 * CameraX replacement for the legacy `CameraPreview` SurfaceView + `android.hardware.Camera`.
 * CameraX handles rotation, aspect ratio and front-camera mirroring, and releases the camera
 * whenever the lifecycle stops (the camera is never used in the background).
 *
 * Swiping up zooms in and swiping down zooms out, as in the legacy app.
 * Requires the CAMERA permission to have been granted.
 */
@Composable
fun CameraMirror(lens: CameraLens, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var surfaceRequest by remember { mutableStateOf<SurfaceRequest?>(null) }
    var camera by remember { mutableStateOf<Camera?>(null) }
    val preview = remember {
        Preview.Builder().build().apply {
            setSurfaceProvider { request -> surfaceRequest = request }
        }
    }

    LaunchedEffect(lifecycleOwner, lens) {
        val provider = ProcessCameraProvider.awaitInstance(context.applicationContext)
        val selector = when (lens) {
            CameraLens.FRONT_VIEW -> CameraSelector.DEFAULT_BACK_CAMERA
            CameraLens.MIRROR_VIEW -> CameraSelector.DEFAULT_FRONT_CAMERA
        }
        try {
            provider.unbind(preview)
            camera = provider.bindToLifecycle(lifecycleOwner, selector, preview)
            awaitCancellation()
        } finally {
            provider.unbind(preview)
            camera = null
        }
    }

    val description = stringResource(R.string.camera_view)
    surfaceRequest?.let { request ->
        CameraXViewfinder(
            surfaceRequest = request,
            modifier = modifier
                .semantics { contentDescription = description }
                .swipeToZoom(camera, context),
        )
    }
}

private fun Modifier.swipeToZoom(camera: Camera?, context: Context): Modifier {
    if (camera == null) return this
    return pointerInput(camera) {
        var linearZoom = camera.cameraInfo.zoomState.value?.linearZoom ?: 0f
        var warnedNoZoom = false
        detectVerticalDragGestures { change, dragAmount ->
            change.consume()
            val zoomState = camera.cameraInfo.zoomState.value ?: return@detectVerticalDragGestures
            if (zoomState.maxZoomRatio <= zoomState.minZoomRatio) {
                if (!warnedNoZoom) {
                    Toast.makeText(context, R.string.no_zoom, Toast.LENGTH_LONG).show()
                    warnedNoZoom = true
                }
                return@detectVerticalDragGestures
            }
            // A full-height swipe covers 1.5x the zoom range; dragging up (negative y) zooms in.
            linearZoom = (linearZoom - dragAmount / size.height * 1.5f).coerceIn(0f, 1f)
            camera.cameraControl.setLinearZoom(linearZoom)
        }
    }
}
