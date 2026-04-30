package com.fastpdf.data.tools

import android.content.Context
import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Exports PDF pages as JPG images.
 * Uses Android's native PdfRenderer for page rendering.
 */
object PdfToImageExporter {

    /**
     * Exports all pages of a PDF as JPG images.
     *
     * @param context App context
     * @param sourceUri PDF URI
     * @param quality JPEG quality (1-100)
     * @param onProgress Progress callback
     * @return List of exported image URIs, empty on failure
     */
    fun export(
        context: Context,
        sourceUri: Uri,
        quality: Int = 90,
        onProgress: (Float) -> Unit = {}
    ): List<Uri> {
        val outputDir = File(context.filesDir, "tools_output/images_${timestamp()}").apply { mkdirs() }
        val results = mutableListOf<Uri>()

        try {
            val pfd = context.contentResolver.openFileDescriptor(sourceUri, "r") ?: return emptyList()
            val renderer = PdfRenderer(pfd)

            for (i in 0 until renderer.pageCount) {
                val page = renderer.openPage(i)
                val bitmap = Bitmap.createBitmap(
                    page.width * 2, // 2x resolution
                    page.height * 2,
                    Bitmap.Config.ARGB_8888
                )
                bitmap.eraseColor(android.graphics.Color.WHITE)
                page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                page.close()

                val imageFile = File(outputDir, "Page_${i + 1}.jpg")
                FileOutputStream(imageFile).use { out ->
                    bitmap.compress(Bitmap.CompressFormat.JPEG, quality, out)
                }
                bitmap.recycle()

                val uri = FileProvider.getUriForFile(context, "${context.packageName}.provider", imageFile)
                results.add(uri)

                onProgress((i + 1).toFloat() / renderer.pageCount)
            }

            renderer.close()
            pfd.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return results
    }

    private fun timestamp(): String =
        SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
}
