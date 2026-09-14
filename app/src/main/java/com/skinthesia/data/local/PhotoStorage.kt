package com.skinthesia.data.local

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.webkit.MimeTypeMap
import com.skinthesia.core.utils.BitmapDecoder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException

/**
 * Owns the app-private directory where skin photos live. Photos never leave
 * the device in Phase 1; a sync layer can be added on top of this class.
 */
class PhotoStorage(context: Context) : PhotoStore {

    private val appContext = context.applicationContext

    private val directory: File
        get() = File(appContext.filesDir, DIRECTORY).apply { mkdirs() }

    override fun newCaptureFile(): File = newFile(JPEG_EXTENSION)

    override suspend fun importFromUri(uri: Uri): File = withContext(Dispatchers.IO) {
        // Keep the source format's extension (a PNG stays .png); decoding sniffs content either way.
        val extension = appContext.contentResolver.getType(uri)
            ?.let { MimeTypeMap.getSingleton().getExtensionFromMimeType(it) }
            ?: JPEG_EXTENSION
        val target = newFile(extension)
        val input = appContext.contentResolver.openInputStream(uri)
            ?: throw IOException("Unable to open image: $uri")
        input.use { source ->
            target.outputStream().use { sink -> source.copyTo(sink) }
        }
        if (target.length() == 0L) {
            target.delete()
            throw IOException("Imported image was empty: $uri")
        }
        target
    }

    override suspend fun delete(path: String): Boolean = withContext(Dispatchers.IO) {
        File(path).delete()
    }

    override suspend fun pruneExcept(keepPath: String?) {
        withContext(Dispatchers.IO) {
            directory.listFiles()
                ?.filter { it.absolutePath != keepPath }
                ?.forEach { it.delete() }
        }
    }

    suspend fun loadBitmap(path: String, maxDimension: Int = DEFAULT_MAX_DIMENSION): Bitmap? =
        BitmapDecoder.decode(path, maxDimension)

    private fun newFile(extension: String): File =
        File(directory, "skin_${System.currentTimeMillis()}.$extension")

    private companion object {
        const val DIRECTORY = "skin_photos"
        const val JPEG_EXTENSION = "jpg"
        const val DEFAULT_MAX_DIMENSION = 1600
    }
}
