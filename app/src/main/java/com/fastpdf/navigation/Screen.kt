package com.fastpdf.navigation

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
        fun createRoute(fileId: String): String = "reader/$fileId"
    }

    /** Scanner — document scanning with ML Kit */
    data object Scanner : Screen("scanner")
}
