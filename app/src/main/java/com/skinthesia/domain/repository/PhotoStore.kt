package com.skinthesia.domain.repository

import java.io.File

/**
 * File-level contract for skin photos, which are sensitive personal data. Photos
 * live only in app-private storage, are never logged, and can be deleted from
 * Data controls.
 */
interface PhotoStore {
    /** A fresh JPEG target for the camera to write into. */
    fun newCaptureFile(): File

    /** Copies an image chosen in the system picker ([uri] as a string) into private storage. */
    suspend fun importFromUri(uri: String): File

    suspend fun delete(path: String): Boolean

    /** Removes every stored photo. */
    suspend fun deleteAll()
}
