package com.fastpdf.data.ai

import android.content.Context
import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import com.fastpdf.data.CurrentFile
import com.fastpdf.domain.model.DocumentType
import java.io.BufferedReader
import java.io.InputStreamReader

/**
 * Extracts readable text from any supported document type.
 * Used by AI features to feed document content to Gemini.
 */
object TextExtractor {

    /**
     * Extracts text content from the currently opened file.
     * Supports PDF (via page-by-page rendering description), text files, and basic info for others.
     *
     * @param context App context
     * @param maxChars Maximum characters to extract (Gemini has token limits)
     * @return Extracted text, or descriptive info if text can't be extracted
     */
    fun extractFromCurrentFile(context: Context, maxChars: Int = 15000): String {
        val uri = CurrentFile.uri ?: return "No file is currently open."
        val name = CurrentFile.name
        val type = CurrentFile.type

        return try {
            when (type) {
                DocumentType.TEXT -> extractTextFile(context, uri, maxChars)
                DocumentType.PDF -> extractPdfText(context, uri, maxChars)
                else -> "File: $name (${type.displayName} document, ${formatSize(CurrentFile.sizeBytes)}). " +
                        "This is a ${type.displayName} file which requires OCR for text extraction."
            }
        } catch (e: Exception) {
            "File: $name. Could not extract text: ${e.message}"
        }
    }

    /**
     * Extracts text from a specific URI (not necessarily the current file).
     */
    fun extractFromUri(context: Context, uri: Uri, mimeType: String, maxChars: Int = 15000): String {
        return try {
            when {
                mimeType.startsWith("text/") -> extractTextFile(context, uri, maxChars)
                mimeType == "application/pdf" -> extractPdfText(context, uri, maxChars)
                else -> "Binary file — text extraction not supported for this format."
            }
        } catch (e: Exception) {
            "Could not extract text: ${e.message}"
        }
    }

    private fun extractTextFile(context: Context, uri: Uri, maxChars: Int): String {
        val inputStream = context.contentResolver.openInputStream(uri) ?: return ""
        val reader = BufferedReader(InputStreamReader(inputStream))
        val text = reader.readText()
        reader.close()
        inputStream.close()
        return if (text.length > maxChars) text.take(maxChars) + "\n...[truncated]" else text
    }

    private fun extractPdfText(context: Context, uri: Uri, maxChars: Int): String {
        val pfd = context.contentResolver.openFileDescriptor(uri, "r") ?: return ""
        val renderer = PdfRenderer(pfd)
        val pageCount = renderer.pageCount

        // For PDFs without embedded text, describe structure
        val info = StringBuilder()
        info.append("PDF Document: ${CurrentFile.name}\n")
        info.append("Pages: $pageCount\n")
        info.append("Size: ${formatSize(CurrentFile.sizeBytes)}\n\n")

        // We can't extract text from PdfRenderer directly, so provide page info
        // Real text extraction would use OCR (already built in Phase 5)
        info.append("Note: This PDF has $pageCount pages. ")
        info.append("For full text content, use the OCR tool first, then feed the result to AI.")

        renderer.close()
        pfd.close()

        return info.toString()
    }

    private fun formatSize(bytes: Long): String = when {
        bytes <= 0 -> "Unknown size"
        bytes < 1024 -> "$bytes B"
        bytes < 1024 * 1024 -> "${bytes / 1024} KB"
        else -> "%.1f MB".format(bytes / (1024.0 * 1024.0))
    }
}
