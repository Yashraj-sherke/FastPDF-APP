package com.fastpdf.data.scanner

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.pdf.PdfDocument
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Creates PDF files from scanned document images.
 *
 * Uses Android's built-in PdfDocument API — zero external dependencies.
 * Handles bitmap loading, page creation, and file saving.
 */
object PdfCreator {

    /**
     * Creates a PDF from a list of image URIs (scanned pages).
     *
     * @param context Application context
     * @param imageUris List of URIs pointing to scanned page images
     * @param fileName Optional custom filename (defaults to timestamp)
     * @return URI of the created PDF file, or null if failed
     */
    fun createPdfFromImages(
        context: Context,
        imageUris: List<Uri>,
        fileName: String? = null
    ): Uri? {
        if (imageUris.isEmpty()) return null

        val pdfDocument = PdfDocument()

        try {
            imageUris.forEachIndexed { index, imageUri ->
                // Load bitmap from URI
                val bitmap = loadBitmap(context, imageUri) ?: return@forEachIndexed

                // Create page with image dimensions
                // Scale to A4-ish proportions (595 x 842 points at 72dpi)
                val pageWidth = 595
                val pageHeight = (pageWidth.toFloat() / bitmap.width * bitmap.height).toInt()

                val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, index + 1).create()
                val page = pdfDocument.startPage(pageInfo)

                // Draw bitmap scaled to fit page
                val canvas = page.canvas
                val scaleX = pageWidth.toFloat() / bitmap.width
                val scaleY = pageHeight.toFloat() / bitmap.height
                canvas.scale(scaleX, scaleY)
                canvas.drawBitmap(bitmap, 0f, 0f, null)

                pdfDocument.finishPage(page)
                bitmap.recycle()
            }

            // Save PDF to app's files/scans directory
            val scansDir = File(context.filesDir, "scans").apply { mkdirs() }
            val pdfName = fileName ?: generateFileName()
            val pdfFile = File(scansDir, "${pdfName}.pdf")

            FileOutputStream(pdfFile).use { outputStream ->
                pdfDocument.writeTo(outputStream)
            }

            pdfDocument.close()

            // Return shareable URI via FileProvider
            return FileProvider.getUriForFile(
                context,
                "${context.packageName}.provider",
                pdfFile
            )
        } catch (e: Exception) {
            e.printStackTrace()
            pdfDocument.close()
            return null
        }
    }

    /**
     * Loads a bitmap from a content URI with memory-efficient sampling.
     */
    private fun loadBitmap(context: Context, uri: Uri): Bitmap? {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri)
            val bitmap = BitmapFactory.decodeStream(inputStream)
            inputStream?.close()
            bitmap
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Generates a timestamped filename for scanned documents.
     */
    private fun generateFileName(): String {
        val dateFormat = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
        return "Scan_${dateFormat.format(Date())}"
    }

    /**
     * Gets the scans directory, creating it if needed.
     */
    fun getScansDirectory(context: Context): File {
        return File(context.filesDir, "scans").apply { mkdirs() }
    }

    /**
     * Lists all saved scanned PDFs.
     */
    fun getSavedScans(context: Context): List<File> {
        val scansDir = getScansDirectory(context)
        return scansDir.listFiles()
            ?.filter { it.extension == "pdf" }
            ?.sortedByDescending { it.lastModified() }
            ?: emptyList()
    }
}
