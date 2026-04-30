package com.fastpdf.data.tools

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import com.itextpdf.kernel.pdf.EncryptionConstants
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
 * Adds password protection to PDF files.
 * Uses iText 7 AES-128 encryption.
 */
object PdfProtector {

    /**
     * Encrypts a PDF with a password.
     *
     * @param context App context
     * @param sourceUri PDF to protect
     * @param password User password to set
     * @param allowPrinting Whether to allow printing
     * @param allowCopying Whether to allow text copying
     * @return URI of protected PDF, or null on failure
     */
    fun protect(
        context: Context,
        sourceUri: Uri,
        password: String,
        allowPrinting: Boolean = true,
        allowCopying: Boolean = false
    ): Uri? {
        val outputDir = File(context.filesDir, "tools_output").apply { mkdirs() }
        val outputFile = File(outputDir, "Protected_${timestamp()}.pdf")

        return try {
            val inputStream = context.contentResolver.openInputStream(sourceUri) ?: return null

            var permissions = 0
            if (allowPrinting) permissions = permissions or EncryptionConstants.ALLOW_PRINTING
            if (allowCopying) permissions = permissions or EncryptionConstants.ALLOW_COPY

            val writerProps = WriterProperties()
                .setStandardEncryption(
                    password.toByteArray(),
                    password.toByteArray(),
                    permissions,
                    EncryptionConstants.ENCRYPTION_AES_128
                )

            val reader = PdfReader(inputStream)
            val writer = PdfWriter(FileOutputStream(outputFile), writerProps)
            val sourceDoc = PdfDocument(reader)
            val outputDoc = PdfDocument(writer)

            sourceDoc.copyPagesTo(1, sourceDoc.numberOfPages, outputDoc)

            outputDoc.close()
            sourceDoc.close()

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
