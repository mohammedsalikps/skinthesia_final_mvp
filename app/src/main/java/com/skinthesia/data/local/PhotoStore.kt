package com.skinthesia.data.local

import android.net.Uri
import java.io.File

/**
 * File-level contract for skin photos. Abstracted so view models can be unit
 * tested with an in-memory or temp-directory fake.
 */
interface PhotoStore {
    /** A fresh JPEG target for the camera to write into. */
    fun newCaptureFile(): File

    /** Copies a gallery image into private storage and returns the new file. */
    suspend fun importFromUri(uri: Uri): File

    suspend fun delete(path: String): Boolean

    /** Removes every stored photo except [keepPath]. */
    suspend fun pruneExcept(keepPath: String?)
}
