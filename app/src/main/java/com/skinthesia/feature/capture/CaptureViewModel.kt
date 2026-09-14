package com.skinthesia.feature.capture

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.skinthesia.domain.model.Ids
import com.skinthesia.domain.model.LensFacing
import com.skinthesia.domain.model.PhotoSource
import com.skinthesia.domain.model.SkinPhoto
import com.skinthesia.domain.repository.PhotoStore
import com.skinthesia.domain.usecase.AttachPhotoUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File

/**
 * Captures or imports a selfie into app-private storage and runs the quality
 * pipeline on it, then hands control back to the screen to navigate on.
 */
class CaptureViewModel(
    val assessmentId: String,
    private val photoStore: PhotoStore,
    private val attachPhoto: AttachPhotoUseCase,
    private val clock: () -> Long,
    val flow: String? = null,
) : ViewModel() {

    data class UiState(
        val processing: Boolean = false,
        val message: String = "Checking your photo…",
        val error: String? = null,
    )

    private val _state = MutableStateFlow(UiState())
    val state: StateFlow<UiState> = _state.asStateFlow()

    fun capture(lens: LensFacing, takePicture: suspend (File) -> Unit, onDone: () -> Unit) {
        if (_state.value.processing) return
        _state.update { it.copy(processing = true, error = null, message = "Capturing…") }
        viewModelScope.launch {
            val target = photoStore.newCaptureFile()
            val captured = runCatching { takePicture(target) }.isSuccess && target.exists()
            if (!captured) {
                target.delete()
                _state.update { it.copy(processing = false, error = "We couldn't save that photo. Please try again.") }
                return@launch
            }
            val photo = SkinPhoto(
                id = Ids.new("ph"),
                filePath = target.absolutePath,
                capturedAt = clock(),
                source = PhotoSource.CAMERA,
                lensFacing = lens,
                mirrored = lens == LensFacing.FRONT,
            )
            check(photo, onDone)
        }
    }

    fun importPhoto(uri: String, onDone: () -> Unit) {
        if (_state.value.processing) return
        _state.update { it.copy(processing = true, error = null, message = "Preparing your photo…") }
        viewModelScope.launch {
            val file = runCatching { photoStore.importFromUri(uri) }.getOrNull()
            if (file == null) {
                _state.update { it.copy(processing = false, error = "We couldn't open that image. Please try another one.") }
                return@launch
            }
            check(SkinPhoto(id = Ids.new("ph"), filePath = file.absolutePath, capturedAt = clock(), source = PhotoSource.GALLERY), onDone)
        }
    }

    private suspend fun check(photo: SkinPhoto, onDone: () -> Unit) {
        _state.update { it.copy(message = "Checking your photo…") }
        runCatching { attachPhoto(assessmentId, photo) }
            .onSuccess {
                _state.update { it.copy(processing = false) }
                onDone()
            }
            .onFailure {
                _state.update { it.copy(processing = false, error = "Something interrupted the photo check. Please try again.") }
            }
    }

    fun dismissError() = _state.update { it.copy(error = null) }
}
