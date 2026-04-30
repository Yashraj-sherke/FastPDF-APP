package com.fastpdf.navigation

import android.net.Uri

/**
 * Type-safe navigation routes for FastPDF.
 * Sealed class prevents invalid route strings at compile time.
 */
sealed class Screen(val route: String) {

    /** Home — recent files, favorites, AI summary */
    data object Home : Screen("home")

    /** Tools — document processing tools grid */
    data object Tools : Screen("tools")

    /** Files — browse all documents */
    data object Files : Screen("files")

    /** Profile — user settings and preferences */
    data object Profile : Screen("profile")

    /** Reader — document viewer with file ID argument */
    data object Reader : Screen("reader/{fileId}") {
        fun createRoute(fileId: String): String = "reader/${Uri.encode(fileId)}"
    }

    /** Scanner — document scanning with ML Kit */
    data object Scanner : Screen("scanner")

    // ━━━ Phase 5: Tool Screens ━━━

    /** Merge — combine multiple PDFs */
    data object Merge : Screen("tools/merge")

    /** Split — extract pages from PDF */
    data object Split : Screen("tools/split")

    /** Compress — reduce PDF file size */
    data object Compress : Screen("tools/compress")

    /** ImageToPdf — convert images to PDF */
    data object ImageToPdf : Screen("tools/image-to-pdf")

    /** Ocr — extract text from PDF/image */
    data object Ocr : Screen("tools/ocr")

    /** Protect — password protect PDF */
    data object Protect : Screen("tools/protect")

    /** Watermark — add text watermark to PDF */
    data object Watermark : Screen("tools/watermark")

    /** Convert — PDF to images */
    data object Convert : Screen("tools/convert")

    // ━━━ Phase 6: AI Features ━━━

    /** AiSummary — AI document intelligence */
    data object AiSummary : Screen("ai/summary")

    // ━━━ Phase 9: New Screens ━━━

    /** RecycleBin — soft-deleted files with restore */
    data object RecycleBin : Screen("recycle-bin")

    /** PageReorder — drag-and-drop PDF page reordering */
    data object PageReorder : Screen("tools/page-reorder")

    /** Onboarding — first-launch welcome carousel */
    data object Onboarding : Screen("onboarding")

    // ━━━ Phase 10: Polish & Release Prep ━━━

    /** StorageManager — visualize storage usage and clear cache */
    data object StorageManager : Screen("storage-manager")

    /** About — app info, credits, rate & share */
    data object About : Screen("about")
}
