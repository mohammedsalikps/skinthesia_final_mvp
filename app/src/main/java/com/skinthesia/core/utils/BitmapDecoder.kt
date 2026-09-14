package com.skinthesia.core.utils

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.graphics.Matrix
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.exifinterface.media.ExifInterface
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import kotlin.math.max
import kotlin.math.roundToInt

/**
 * Decodes JPEGs from app storage into display-sized, correctly oriented bitmaps.
 * Uses ImageDecoder (which honours EXIF orientation) on API 28+ and a
 * BitmapFactory + ExifInterface fallback below that.
 */
object BitmapDecoder {

    /** Oriented pixel dimensions (width to height) without decoding the image. */
    suspend fun dimensions(path: String): Pair<Int, Int>? = withContext(Dispatchers.IO) {
        val file = File(path)
        if (!file.exists()) return@withContext null
        runCatching {
            val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            BitmapFactory.decodeFile(path, bounds)
            val rotation = ExifInterface(file).rotationDegrees
            if (rotation == 90 || rotation == 270) bounds.outHeight to bounds.outWidth else bounds.outWidth to bounds.outHeight
        }.getOrNull()?.takeIf { it.first > 0 && it.second > 0 }
    }

    suspend fun decode(path: String, maxDimension: Int): Bitmap? = withContext(Dispatchers.IO) {
        val file = File(path)
        if (!file.exists() || maxDimension <= 0) return@withContext null
        runCatching {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                modernDecode(file, maxDimension)
            } else {
                legacyDecode(file, maxDimension)
            }
        }.getOrNull()
    }

    @RequiresApi(Build.VERSION_CODES.P)
    private fun modernDecode(file: File, maxDimension: Int): Bitmap {
        val source = ImageDecoder.createSource(file)
        return ImageDecoder.decodeBitmap(source) { decoder, info, _ ->
            val width = info.size.width
            val height = info.size.height
            val scale = max(width, height).toFloat() / maxDimension
            if (scale > 1f) {
                decoder.setTargetSize((width / scale).roundToInt(), (height / scale).roundToInt())
            }
            decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
            decoder.isMutableRequired = false
        }
    }

    private fun legacyDecode(file: File, maxDimension: Int): Bitmap? {
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeFile(file.path, bounds)
        var sample = 1
        while (max(bounds.outWidth, bounds.outHeight) / (sample * 2) >= maxDimension) {
            sample *= 2
        }
        val options = BitmapFactory.Options().apply { inSampleSize = sample }
        val bitmap = BitmapFactory.decodeFile(file.path, options) ?: return null
        val exif = ExifInterface(file)
        val rotation = exif.rotationDegrees
        val flipped = exif.isFlipped
        if (rotation == 0 && !flipped) return bitmap
        val matrix = Matrix().apply {
            if (flipped) preScale(-1f, 1f)
            postRotate(rotation.toFloat())
        }
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }
}
