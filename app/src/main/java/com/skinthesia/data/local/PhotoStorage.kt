package com.skinthesia.data.local

import androidx.core.net.toUri
import android.content.Context
import android.net.Uri
import android.webkit.MimeTypeMap
import com.skinthesia.domain.repository.PhotoStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException

/**
 * Owns the app-private directory where skin photos live. Photos never leave the
 * device in the demonstration build, are excluded from logs, and can be deleted
 * from Data controls.
 */
class PhotoStorage(context: Context) : PhotoStore {

    private val appContext = context.applicationContext

    private val directory: File
        get() = File(appContext.filesDir, DIRECTORY).apply { mkdirs() }

    override fun newCaptureFile(): File = newFile(JPEG_EXTENSION)

    override suspend fun importFromUri(uri: String): File = withContext(Dispatchers.IO) {
        val parsed = uri.toUri()
        // Keep the source format's extension (a PNG stays .png); decoding sniffs content either way.
        val extension = appContext.contentResolver.getType(parsed)
            ?.let { MimeTypeMap.getSingleton().getExtensionFromMimeType(it) }
            ?: JPEG_EXTENSION
        val target = newFile(extension)
        val input = appContext.contentResolver.openInputStream(parsed)
            ?: throw IOException("Unable to open the selected image")
        input.use { source ->
            target.outputStream().use { sink -> source.copyTo(sink) }
        }
        if (target.length() == 0L) {
            target.delete()
            throw IOException("The selected image was empty")
        }
        target
    }

    override suspend fun delete(path: String): Boolean = withContext(Dispatchers.IO) {
        val file = File(path)
        // Only ever delete inside our own private photo directory.
        file.canonicalPath.startsWith(directory.canonicalPath) && file.delete()
    }

    override suspend fun deleteAll() {
        withContext(Dispatchers.IO) {
            directory.listFiles()?.forEach { it.delete() }
        }
    }

    private fun newFile(extension: String): File =
        File(directory, "skin_${System.currentTimeMillis()}.$extension")

    private companion object {
        const val DIRECTORY = "skin_photos"
        const val JPEG_EXTENSION = "jpg"
    }
}
