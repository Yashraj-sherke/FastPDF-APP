package com.fastpdf.data.ai

import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.generationConfig
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

/**
 * AI Service wrapper around Google Gemini API.
 *
 * Provides document intelligence features:
 * - Summarization
 * - Q&A about document content
 * - Key points extraction
 * - Smart categorization
 *
 * Uses Gemini 1.5 Flash (free tier: 15 RPM, 1M tokens/min).
 *
 * To use: Set your API key in AiService.initialize() or via BuildConfig.
 * Get a free key at: https://aistudio.google.com/app/apikey
 */
object AiService {

    private var apiKey: String = ""
    private var model: GenerativeModel? = null

    /**
     * Initialize with API key. Call this once in Application.onCreate() or on first use.
     */
    fun initialize(key: String) {
        apiKey = key
        model = GenerativeModel(
            modelName = "gemini-1.5-flash",
            apiKey = apiKey,
            generationConfig = generationConfig {
                temperature = 0.7f
                maxOutputTokens = 2048
            }
        )
    }

    val isInitialized: Boolean get() = model != null && apiKey.isNotEmpty()

    /**
     * Summarize document content.
     */
    suspend fun summarize(documentText: String, fileName: String = ""): String {
        val m = model ?: return "⚠️ AI not configured. Add your Gemini API key in Settings."

        val prompt = """
            You are a document analysis assistant. Summarize the following document concisely.
            
            Guidelines:
            - Provide a clear, structured summary (3-5 bullet points)
            - Highlight key facts, numbers, and conclusions
            - Keep it under 200 words
            - Use markdown formatting
            
            ${if (fileName.isNotEmpty()) "Document: $fileName\n" else ""}
            Content:
            $documentText
        """.trimIndent()

        return try {
            val response = m.generateContent(prompt)
            response.text ?: "Could not generate summary."
        } catch (e: Exception) {
            "❌ AI Error: ${e.message}"
        }
    }

    /**
     * Answer a question about document content.
     */
    suspend fun askQuestion(documentText: String, question: String): String {
        val m = model ?: return "⚠️ AI not configured. Add your Gemini API key in Settings."

        val prompt = """
            You are a helpful document assistant. Answer the user's question based on the document content below.
            
            Guidelines:
            - Answer based ONLY on the document content
            - If the answer isn't in the document, say so
            - Be concise and direct
            - Use markdown formatting for clarity
            
            Document Content:
            $documentText
            
            User Question: $question
        """.trimIndent()

        return try {
            val response = m.generateContent(prompt)
            response.text ?: "Could not generate answer."
        } catch (e: Exception) {
            "❌ AI Error: ${e.message}"
        }
    }

    /**
     * Extract key points from document.
     */
    suspend fun extractKeyPoints(documentText: String): String {
        val m = model ?: return "⚠️ AI not configured. Add your Gemini API key in Settings."

        val prompt = """
            Extract the key points from this document. Return as a numbered list.
            Focus on: main ideas, important data, action items, and conclusions.
            Keep each point to 1-2 sentences.
            
            Document:
            $documentText
        """.trimIndent()

        return try {
            val response = m.generateContent(prompt)
            response.text ?: "Could not extract key points."
        } catch (e: Exception) {
            "❌ AI Error: ${e.message}"
        }
    }

    /**
     * Stream a response for real-time display.
     */
    fun streamResponse(prompt: String): Flow<String> = flow {
        val m = model ?: run {
            emit("⚠️ AI not configured. Add your Gemini API key in Settings.")
            return@flow
        }

        try {
            val response = m.generateContentStream(prompt)
            response.collect { chunk ->
                chunk.text?.let { emit(it) }
            }
        } catch (e: Exception) {
            emit("❌ AI Error: ${e.message}")
        }
    }
}
