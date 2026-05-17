package com.hasiru.usiru.mapper.util

import android.content.ContentResolver
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.camera.core.ImageCapture
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object BitmapUtils {
    fun createImageFile(context: Context): File {
        val name = "tree_${SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())}.jpg"
        val directory = File(context.filesDir, "tree_photos").apply { mkdirs() }
        return File(directory, name)
    }

    fun outputOptions(file: File): ImageCapture.OutputFileOptions = ImageCapture.OutputFileOptions.Builder(file).build()

    fun decodeFile(file: File): Bitmap = BitmapFactory.decodeFile(file.absolutePath)

    fun decodeUri(contentResolver: ContentResolver, uri: Uri): Bitmap? = contentResolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it) }

    fun persistBitmap(context: Context, bitmap: Bitmap): Uri {
        val file = createImageFile(context)
        FileOutputStream(file).use { output -> bitmap.compress(Bitmap.CompressFormat.JPEG, 92, output) }
        return Uri.fromFile(file)
    }
}
