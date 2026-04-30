package com.fastpdf.data.tools

import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.core.content.FileProvider
import com.itextpdf.io.image.ImageDataFactory
import com.itextpdf.kernel.geom.PageSize
import com.itextpdf.kernel.pdf.PdfDocument
import com.itextpdf.kernel.pdf.PdfWriter
import com.itextpdf.layout.Document
import com.itextpdf.layout.element.Image
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Converts images to PDF.
 * Uses iText 7 for high-quality image placement with proper page sizing.
 */
object ImageToPdfConverter {

    /**
     * Converts a list of images to a single PDF.
     *
     * @param context App context
     * @param imageUris List of image URIs
     * @param onProgress Progress callback (0f..1f)
     * @return URI of created PDF, or null on failure
     */
    fun convert(
        context: Context,
        imageUris: List<Uri>,
        onProgress: (Float) -> Unit = {}
    ): Uri? {
        if (imageUris.isEmpty()) return null

        val outputDir = File(context.filesDir, "tools_output").apply { mkdirs() }
        val outputFile = File(outputDir, "ImageToPDF_${timestamp()}.pdf")

        return try {
            val writer = PdfWriter(FileOutputStream(outputFile))
            val pdfDoc = PdfDocument(writer)
            val document = Document(pdfDoc)

            imageUris.forEachIndexed { index, imageUri ->
                val inputStream = context.contentResolver.openInputStream(imageUri) ?: return@forEachIndexed
                val bytes = inputStream.readBytes()
                inputStream.close()

                val imageData = ImageDataFactory.create(bytes)
                val image = Image(imageData)

                // Scale image to fit A4 page with margins
                val pageSize = PageSize.A4
                pdfDoc.addNewPage(pageSize)

                val pageWidth = pageSize.width - 72f  // 36pt margins each side
                val pageHeight = pageSize.height - 72f

                val scaleX = pageWidth / image.imageWidth
                val scaleY = pageHeight / image.imageHeight
                val scale = minOf(scaleX, scaleY)

                image.scale(scale, scale)
                image.setFixedPosition(
                    index + 1,
                    (pageSize.width - image.imageWidth * scale) / 2,
                    (pageSize.height - image.imageHeight * scale) / 2
                )

                document.add(image)
                onProgress((index + 1).toFloat() / imageUris.size)
            }

            document.close()
            FileProvider.getUriForFile(context, "${context.packageName}.provider", outputFile)
        } catch (e: Exception) {
            e.printStackTrace()
            outputFile.delete()
            null
        }
    }

    private fun timestamp(): String =
        SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
}
