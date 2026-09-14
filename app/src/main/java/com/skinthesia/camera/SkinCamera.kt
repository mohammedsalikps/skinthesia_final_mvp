package com.skinthesia.camera

import android.content.Context
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.view.CameraController
import androidx.camera.view.LifecycleCameraController
import androidx.camera.view.PreviewView
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.skinthesia.domain.model.LensFacing
import kotlinx.coroutines.suspendCancellableCoroutine
import java.io.File
import java.util.concurrent.Executor
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * Thin wrapper around CameraX's [LifecycleCameraController] that exposes the
 * few things the photo screen needs: lens facing, availability and capture.
 */
class SkinCameraController(context: Context) {

    private val appContext = context.applicationContext
    private val mainExecutor: Executor = ContextCompat.getMainExecutor(appContext)

    val controller: LifecycleCameraController = LifecycleCameraController(appContext).apply {
        setEnabledUseCases(CameraController.IMAGE_CAPTURE)
        imageCaptureMode = ImageCapture.CAPTURE_MODE_MAXIMIZE_QUALITY
        cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA
    }

    var lensFacing: LensFacing by mutableStateOf(LensFacing.FRONT)
        private set

    var hasFrontCamera: Boolean by mutableStateOf(true)
        private set

    var hasBackCamera: Boolean by mutableStateOf(true)
        private set

    /** True once CameraX has initialised and reported which lenses exist. */
    var isReady: Boolean by mutableStateOf(false)
        private set

    val canFlip: Boolean get() = hasFrontCamera && hasBackCamera
    val hasAnyCamera: Boolean get() = hasFrontCamera || hasBackCamera

    fun bind(owner: LifecycleOwner) {
        controller.bindToLifecycle(owner)
        controller.initializationFuture.addListener({ refreshAvailability() }, mainExecutor)
    }

    fun unbind() {
        controller.unbind()
    }

    fun flip() {
        if (!canFlip) return
        lensFacing = if (lensFacing == LensFacing.FRONT) LensFacing.BACK else LensFacing.FRONT
        controller.cameraSelector = lensFacing.selector()
    }

    /** Captures a JPEG into [target]. The front lens is saved mirrored to match the preview. */
    suspend fun capture(target: File): File = suspendCancellableCoroutine { continuation ->
        val metadata = ImageCapture.Metadata().apply {
            isReversedHorizontal = lensFacing == LensFacing.FRONT
        }
        val options = ImageCapture.OutputFileOptions.Builder(target)
            .setMetadata(metadata)
            .build()
        controller.takePicture(
            options,
            mainExecutor,
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                    if (continuation.isActive) continuation.resume(target)
                }

                override fun onError(exception: ImageCaptureException) {
                    if (continuation.isActive) continuation.resumeWithException(exception)
                }
            },
        )
    }

    private fun refreshAvailability() {
        hasFrontCamera = runCatching { controller.hasCamera(CameraSelector.DEFAULT_FRONT_CAMERA) }.getOrDefault(false)
        hasBackCamera = runCatching { controller.hasCamera(CameraSelector.DEFAULT_BACK_CAMERA) }.getOrDefault(false)
        if (!hasFrontCamera && hasBackCamera) {
            lensFacing = LensFacing.BACK
            controller.cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
        }
        isReady = true
    }

    private fun LensFacing.selector(): CameraSelector = when (this) {
        LensFacing.FRONT -> CameraSelector.DEFAULT_FRONT_CAMERA
        LensFacing.BACK -> CameraSelector.DEFAULT_BACK_CAMERA
    }
}

/** Creates a controller bound to the current lifecycle and releases it on dispose. */
@Composable
fun rememberSkinCameraController(): SkinCameraController {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val controller = remember { SkinCameraController(context) }
    DisposableEffect(lifecycleOwner, controller) {
        controller.bind(lifecycleOwner)
        onDispose { controller.unbind() }
    }
    return controller
}

/** Live camera feed. Uses the TextureView-backed mode so Compose clipping applies. */
@Composable
fun CameraPreview(
    controller: SkinCameraController,
    modifier: Modifier = Modifier,
) {
    AndroidView(
        modifier = modifier,
        factory = { context ->
            PreviewView(context).apply {
                scaleType = PreviewView.ScaleType.FILL_CENTER
                implementationMode = PreviewView.ImplementationMode.COMPATIBLE
                this.controller = controller.controller
            }
        },
    )
}
