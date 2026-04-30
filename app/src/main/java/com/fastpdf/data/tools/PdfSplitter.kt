package com.fastpdf.data.tools

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import com.itextpdf.kernel.pdf.PdfDocument
import com.itextpdf.kernel.pdf.PdfReader
import com.itextpdf.kernel.pdf.PdfWriter
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Splits a PDF by extracting specific page ranges into a new document.
 * Uses iText 7 copyPagesTo() for lossless page extraction.
 */
object PdfSplitter {

    /**
     * Splits a PDF into a new document containing only the specified pages.
     *
     * @param context App context
     * @param sourceUri URI of the source PDF
     * @param pageRanges Page ranges string (e.g., "1-3, 5, 7-10")
     * @return URI of the split PDF, or null on failure
     */
    fun split(
        context: Context,
        sourceUri: Uri,
        pageRanges: String
    ): Uri? {
        val outputDir = File(context.filesDir, "tools_output").apply { mkdirs() }
        val outputFile = File(outputDir, "Split_${timestamp()}.pdf")

        return try {
            val inputStream = context.contentResolver.openInputStream(sourceUri) ?: return null
            val reader = PdfReader(inputStream)
            val sourceDoc = PdfDocument(reader)

            val pages = parsePageRanges(pageRanges, sourceDoc.numberOfPages)
            if (pages.isEmpty()) {
                sourceDoc.close()
                return null
            }

            val writer = PdfWriter(FileOutputStream(outputFile))
            val outputDoc = PdfDocument(writer)

            sourceDoc.copyPagesTo(pages, outputDoc)

            outputDoc.close()
            sourceDoc.close()

            FileProvider.getUriForFile(context, "${context.packageName}.provider", outputFile)
        } catch (e: Exception) {
            e.printStackTrace()
            outputFile.delete()
            null
        }
    }

    /**
     * Returns total page count for the given PDF.
     */
    fun getPageCount(context: Context, uri: Uri): Int {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri) ?: return 0
            val reader = PdfReader(inputStream)
            val doc = PdfDocument(reader)
            val count = doc.numberOfPages
            doc.close()
            count
        } catch (e: Exception) {
            0
        }
    }

    /**
     * Parses a page range string like "1-3, 5, 7-10" into a list of page numbers.
     */
    fun parsePageRanges(input: String, maxPages: Int): List<Int> {
        val pages = mutableSetOf<Int>()
        val parts = input.split(",").map { it.trim() }

        for (part in parts) {
            if (part.contains("-")) {
                val range = part.split("-").map { it.trim().toIntOrNull() }
                if (range.size == 2 && range[0] != null && range[1] != null) {
                    val start = range[0]!!.coerceIn(1, maxPages)
                    val end = range[1]!!.coerceIn(1, maxPages)
                    for (i in start..end) pages.add(i)
                }
            } else {
                part.toIntOrNull()?.let { page ->
                    if (page in 1..maxPages) pages.add(page)
                }
            }
        }

        return pages.sorted()
    }

    private fun timestamp(): String =
        SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
}
