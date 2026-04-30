package com.fastpdf.data.tools

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import com.itextpdf.kernel.pdf.CompressionConstants
import com.itextpdf.kernel.pdf.PdfDocument
import com.itextpdf.kernel.pdf.PdfReader
import com.itextpdf.kernel.pdf.PdfWriter
import com.itextpdf.kernel.pdf.WriterProperties
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Compresses PDF files to reduce size.
 * Uses iText 7 full compression + object stream merging.
 */
object PdfCompressor {

    data class CompressionResult(
        val outputUri: Uri,
        val originalBytes: Long,
        val compressedBytes: Long
    ) {
        val savedPercent: Int
            get() = if (originalBytes > 0) ((1 - compressedBytes.toDouble() / originalBytes) * 100).toInt() else 0
    }

    /**
     * Compresses a PDF file.
     *
     * @param context App context
     * @param sourceUri URI of the PDF to compress
     * @return CompressionResult with original/compressed sizes, or null on failure
     */
    fun compress(context: Context, sourceUri: Uri): CompressionResult? {
        val outputDir = File(context.filesDir, "tools_output").apply { mkdirs() }
        val outputFile = File(outputDir, "Compressed_${timestamp()}.pdf")

        return try {
            val inputStream = context.contentResolver.openInputStream(sourceUri) ?: return null

            // Get original file size
            val originalBytes = context.contentResolver.openFileDescriptor(sourceUri, "r")?.use {
                it.statSize
            } ?: 0L

            val reader = PdfReader(inputStream)
            val writerProps = WriterProperties()
                .setFullCompressionMode(true)
                .setCompressionLevel(CompressionConstants.BEST_COMPRESSION)

            val writer = PdfWriter(FileOutputStream(outputFile), writerProps)
            val sourceDoc = PdfDocument(reader)
            val outputDoc = PdfDocument(writer)

            // Copy all pages
            sourceDoc.copyPagesTo(1, sourceDoc.numberOfPages, outputDoc)

            outputDoc.close()
            sourceDoc.close()

            val compressedBytes = outputFile.length()
            val uri = FileProvider.getUriForFile(context, "${context.packageName}.provider", outputFile)

            CompressionResult(uri, originalBytes, compressedBytes)
        } catch (e: Exception) {
            e.printStackTrace()
            outputFile.delete()
            null
        }
    }

    private fun timestamp(): String =
        SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
}
