package com.skinthesia.domain.model

enum class PhotoSource { CAMERA, GALLERY }

enum class LensFacing { FRONT, BACK }

/** A photo of the user's skin stored privately on the device. */
data class SkinPhoto(
    val id: String,
    /** Absolute path of the JPEG in app-private storage. */
    val filePath: String,
    val capturedAt: Long,
    val source: PhotoSource,
    val lensFacing: LensFacing? = null,
    /** True when the file was saved mirrored to match the selfie preview. */
    val mirrored: Boolean = false,
)
