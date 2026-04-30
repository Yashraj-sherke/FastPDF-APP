package com.fastpdf.data.tools

import android.content.Context
import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.tasks.await
import java.io.File
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * OCR Engine — extracts text from PDF or image files.
 * Uses ML Kit Text Recognition for accurate text extraction.
 */
object OcrEngine {

    /**
     * Extracts text from a PDF by rendering pages and running OCR.
     *
     * @param context App context
     * @param sourceUri PDF or image URI
     * @param isPdf Whether the source is a PDF (vs an image)
     * @param onProgress Progress callback
     * @return Extracted text, or null on failure
     */
    suspend fun extractText(
        context: Context,
        sourceUri: Uri,
        isPdf: Boolean = true,
        onProgress: (Float) -> Unit = {}
    ): String? {
        return try {
            if (isPdf) {
                extractFromPdf(context, sourceUri, onProgress)
            } else {
                extractFromImage(context, sourceUri)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private suspend fun extractFromPdf(
        context: Context,
        pdfUri: Uri,
        onProgress: (Float) -> Unit
    ): String {
        val pfd = context.contentResolver.openFileDescriptor(pdfUri, "r")
            ?: throw Exception("Cannot open PDF")
        val renderer = PdfRenderer(pfd)
        val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

        val allText = StringBuilder()

        for (i in 0 until renderer.pageCount) {
            val page = renderer.openPage(i)
            val bitmap = Bitmap.createBitmap(page.width * 2, page.height * 2, Bitmap.Config.ARGB_8888)
            bitmap.eraseColor(android.graphics.Color.WHITE)
            page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
            page.close()

            val inputImage = InputImage.fromBitmap(bitmap, 0)
            val result = recognizer.process(inputImage).await()

            if (allText.isNotEmpty()) allText.append("\n\n--- Page ${i + 1} ---\n\n")
            allText.append(result.text)

            bitmap.recycle()
            onProgress((i + 1).toFloat() / renderer.pageCount)
        }

        renderer.close()
        pfd.close()
        recognizer.close()

        return allText.toString()
    }

    private suspend fun extractFromImage(context: Context, imageUri: Uri): String {
        val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
        val inputImage = InputImage.fromFilePath(context, imageUri)
        val result = recognizer.process(inputImage).await()
        recognizer.close()
        return result.text
    }

    /**
     * Saves extracted text to a .txt file.
     */
    fun saveAsTextFile(context: Context, text: String, fileName: String = "OCR_${timestamp()}"): File? {
        return try {
            val outputDir = File(context.filesDir, "tools_output").apply { mkdirs() }
            val file = File(outputDir, "$fileName.txt")
            FileWriter(file).use { it.write(text) }
            file
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun timestamp(): String =
        SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
}
