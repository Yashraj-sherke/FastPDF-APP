package com.fastpdf.data.tools

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import com.itextpdf.kernel.pdf.PdfDocument
import com.itextpdf.kernel.pdf.PdfReader
import com.itextpdf.kernel.pdf.PdfWriter
import com.itextpdf.kernel.utils.PdfMerger as ITextMerger
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Merges multiple PDF files into a single document.
 * Uses iText 7 PdfMerger for reliable page merging.
 */
object PdfMerger {

    /**
     * Merges multiple PDFs into one.
     *
     * @param context App context
     * @param pdfUris List of PDF URIs to merge (in order)
     * @param onProgress Progress callback (0f..1f)
     * @return URI of merged PDF, or null on failure
     */
    fun merge(
        context: Context,
        pdfUris: List<Uri>,
        onProgress: (Float) -> Unit = {}
    ): Uri? {
        if (pdfUris.size < 2) return null

        val outputDir = File(context.filesDir, "tools_output").apply { mkdirs() }
        val outputFile = File(outputDir, "Merged_${timestamp()}.pdf")

        return try {
            val writer = PdfWriter(FileOutputStream(outputFile))
            val mergedDoc = PdfDocument(writer)
            val merger = ITextMerger(mergedDoc)

            pdfUris.forEachIndexed { index, uri ->
                val inputStream = context.contentResolver.openInputStream(uri)
                if (inputStream != null) {
                    val reader = PdfReader(inputStream)
                    val sourceDoc = PdfDocument(reader)
                    merger.merge(sourceDoc, 1, sourceDoc.numberOfPages)
                    sourceDoc.close()
                }
                onProgress((index + 1).toFloat() / pdfUris.size)
            }

            mergedDoc.close()

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
