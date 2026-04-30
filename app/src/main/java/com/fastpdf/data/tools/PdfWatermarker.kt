package com.fastpdf.data.tools

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import com.itextpdf.kernel.colors.ColorConstants
import com.itextpdf.kernel.colors.DeviceRgb
import com.itextpdf.kernel.font.PdfFontFactory
import com.itextpdf.kernel.pdf.PdfDocument
import com.itextpdf.kernel.pdf.PdfReader
import com.itextpdf.kernel.pdf.PdfWriter
import com.itextpdf.kernel.pdf.canvas.PdfCanvas
import com.itextpdf.kernel.pdf.extgstate.PdfExtGState
import com.itextpdf.layout.Canvas
import com.itextpdf.layout.element.Paragraph
import com.itextpdf.layout.properties.TextAlignment
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.sqrt

/**
 * Adds text watermark to PDF pages.
 * Uses iText 7 Canvas for diagonal watermark rendering.
 */
object PdfWatermarker {

    /**
     * Adds a text watermark to every page of a PDF.
     *
     * @param context App context
     * @param sourceUri PDF to watermark
     * @param watermarkText Text to display (e.g., "CONFIDENTIAL")
     * @param opacity Opacity 0f..1f
     * @param fontSize Font size in points
     * @return URI of watermarked PDF, or null on failure
     */
    fun watermark(
        context: Context,
        sourceUri: Uri,
        watermarkText: String,
        opacity: Float = 0.3f,
        fontSize: Float = 60f
    ): Uri? {
        val outputDir = File(context.filesDir, "tools_output").apply { mkdirs() }
        val outputFile = File(outputDir, "Watermarked_${timestamp()}.pdf")

        return try {
            val inputStream = context.contentResolver.openInputStream(sourceUri) ?: return null
            val reader = PdfReader(inputStream)
            val writer = PdfWriter(FileOutputStream(outputFile))
            val pdfDoc = PdfDocument(reader, writer)

            val font = PdfFontFactory.createFont()
            val gs = PdfExtGState().setFillOpacity(opacity)

            for (i in 1..pdfDoc.numberOfPages) {
                val page = pdfDoc.getPage(i)
                val pageSize = page.pageSize
                val pdfCanvas = PdfCanvas(page)

                pdfCanvas.saveState()
                pdfCanvas.setExtGState(gs)

                // Calculate center and rotation
                val centerX = pageSize.width / 2
                val centerY = pageSize.height / 2
                val diagonal = sqrt(
                    (pageSize.width * pageSize.width + pageSize.height * pageSize.height).toDouble()
                ).toFloat()

                val canvas = Canvas(pdfCanvas, pageSize)
                val paragraph = Paragraph(watermarkText)
                    .setFont(font)
                    .setFontSize(fontSize)
                    .setFontColor(DeviceRgb(180, 180, 180))
                    .setTextAlignment(TextAlignment.CENTER)

                canvas.showTextAligned(
                    paragraph,
                    centerX,
                    centerY,
                    i,
                    TextAlignment.CENTER,
                    com.itextpdf.layout.properties.VerticalAlignment.MIDDLE,
                    Math.toRadians(45.0).toFloat()
                )
                canvas.close()

                pdfCanvas.restoreState()
            }

            pdfDoc.close()
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
