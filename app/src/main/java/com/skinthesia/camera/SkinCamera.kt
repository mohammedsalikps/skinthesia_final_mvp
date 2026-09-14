package com.skinthesia.camera

import android.content.Context
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import java.io.File
import java.util.concurrent.Executor
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/** Live light level in the viewfinder, used for calm, real-time guidance. */
enum class LiveLight { UNKNOWN, TOO_DARK, DIM, GOOD, BRIGHT }

/**
 * Thin wrapper around CameraX's [LifecycleCameraController]: preview, capture, lens
 * flipping and a lightweight luminance analyser for live lighting guidance. Frames are
 * analysed in memory only and never stored.
 */
class SkinCameraController(context: Context) {

    private val appContext = context.applicationContext
    private val mainExecutor: Executor = ContextCompat.getMainExecutor(appContext)
    private val analysisExecutor: ExecutorService = Executors.newSingleThreadExecutor()
    private val _liveLight = MutableStateFlow(LiveLight.UNKNOWN)
    val liveLight: StateFlow<LiveLight> = _liveLight.asStateFlow()
    private var lastAnalysisAt = 0L

    val controller: LifecycleCameraController = LifecycleCameraController(appContext).apply {
        setEnabledUseCases(CameraController.IMAGE_CAPTURE or CameraController.IMAGE_ANALYSIS)
        imageCaptureMode = ImageCapture.CAPTURE_MODE_MAXIMIZE_QUALITY
        imageAnalysisBackpressureStrategy = ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST
        cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA
        setImageAnalysisAnalyzer(analysisExecutor, ::analyse)
    }

    var lensFacing: LensFacing by mutableStateOf(LensFacing.FRONT)
        private set

    var hasFrontCamera: Boolean by mutableStateOf(true)
        private set

    var hasBackCamera: Boolean by mutableStateOf(true)
        private set

    var isReady: Boolean by mutableStateOf(false)
        private set

    val canFlip: Boolean get() = hasFrontCamera && hasBackCamera
    val hasAnyCamera: Boolean get() = hasFrontCamera || hasBackCamera

    fun bind(owner: LifecycleOwner) {
        controller.bindToLifecycle(owner)
        controller.initializationFuture.addListener({ refreshAvailability() }, mainExecutor)
    }

    fun release() {
        controller.clearImageAnalysisAnalyzer()
        controller.unbind()
        analysisExecutor.shutdown()
    }

    fun flip() {
        if (!canFlip) return
        lensFacing = if (lensFacing == LensFacing.FRONT) LensFacing.BACK else LensFacing.FRONT
        controller.cameraSelector = lensFacing.selector()
    }

    /** Captures a JPEG into [target]. The front lens is saved mirrored to match the preview. */
    suspend fun capture(target: File): File = suspendCancellableCoroutine { continuation ->
        val metadata = ImageCapture.Metadata().apply { isReversedHorizontal = lensFacing == LensFacing.FRONT }
        val options = ImageCapture.OutputFileOptions.Builder(target).setMetadata(metadata).build()
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

    /** Samples the luma plane a few times a second to classify the light. */
    private fun analyse(image: ImageProxy) {
        try {
            val now = System.currentTimeMillis()
            if (now - lastAnalysisAt < ANALYSIS_INTERVAL_MS) return
            lastAnalysisAt = now
            val plane = image.planes.firstOrNull() ?: return
            val buffer = plane.buffer
            val rowStride = plane.rowStride
            var sum = 0L
            var count = 0
            var y = 0
            while (y < image.height) {
                var x = 0
                while (x < image.width) {
                    val index = y * rowStride + x
                    if (index < buffer.limit()) {
                        sum += buffer.get(index).toInt() and 0xFF
                        count++
                    }
                    x += SAMPLE_STEP
                }
                y += SAMPLE_STEP
            }
            if (count == 0) return
            val mean = sum.toFloat() / count
            _liveLight.value = when {
                mean < 45f -> LiveLight.TOO_DARK
                mean < 80f -> LiveLight.DIM
                mean > 215f -> LiveLight.BRIGHT
                else -> LiveLight.GOOD
            }
        } finally {
            image.close()
        }
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

    private companion object {
        const val ANALYSIS_INTERVAL_MS = 400L
        const val SAMPLE_STEP = 16
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
        onDispose { controller.release() }
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
