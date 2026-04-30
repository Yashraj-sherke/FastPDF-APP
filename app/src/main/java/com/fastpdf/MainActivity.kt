package com.fastpdf

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.fastpdf.data.ThemePreferences
import com.fastpdf.navigation.NavGraph
import com.fastpdf.ui.theme.FastPDFTheme

/**
 * Main entry point for FastPDF.
 * Single-activity architecture with Compose navigation.
 *
 * Phase 8 additions:
 * - Splash screen via core-splashscreen API
 * - Dark mode from DataStore preferences
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        // Install splash screen BEFORE super.onCreate
        installSplashScreen()

        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            // Observe dark mode preference
            val isDarkMode by ThemePreferences.isDarkMode(this).collectAsState(initial = false)

            FastPDFTheme(darkTheme = isDarkMode) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    NavGraph()
                }
            }
        }
    }
}
