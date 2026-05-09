package com.fastpdf.navigation

import android.net.Uri
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.fastpdf.data.CurrentFile
import com.fastpdf.data.ThemePreferences
import com.fastpdf.domain.model.DocumentType
import com.fastpdf.ui.components.BottomNavBar
import com.fastpdf.ui.screens.FilesScreen
import com.fastpdf.ui.screens.HomeScreen
import com.fastpdf.ui.screens.AboutScreen
import com.fastpdf.ui.screens.AiSummaryScreen
import com.fastpdf.ui.screens.OnboardingScreen
import com.fastpdf.ui.screens.ProfileScreen
import com.fastpdf.ui.screens.ReaderScreen
import com.fastpdf.ui.screens.RecycleBinScreen
import com.fastpdf.ui.screens.ScannerScreen
import com.fastpdf.ui.screens.StorageManagerScreen
import com.fastpdf.ui.screens.ToolsScreen
import com.fastpdf.ui.screens.tools.CompressScreen
import com.fastpdf.ui.screens.tools.ConvertScreen
import com.fastpdf.ui.screens.tools.ImageToPdfScreen
import com.fastpdf.ui.screens.tools.MergeScreen
import com.fastpdf.ui.screens.tools.OcrScreen
import com.fastpdf.ui.screens.tools.PageReorderScreen
import com.fastpdf.ui.screens.tools.ProtectScreen
import com.fastpdf.ui.screens.tools.SplitScreen
import com.fastpdf.ui.screens.tools.WatermarkScreen
import kotlinx.coroutines.launch

/**
 * Main navigation graph for FastPDF.
 * Handles bottom nav + reader + scanner + tool screen transitions.
 *
 * Phase 9: Added Onboarding, RecycleBin, and PageReorder routes.
 */
@Composable
fun NavGraph() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // Check if this is the first launch
    val isFirstLaunch by ThemePreferences.isFirstLaunch(context).collectAsState(initial = false)

    // Determine start destination
    val startDestination = if (isFirstLaunch) Screen.Onboarding.route else Screen.Home.route

    // Screens that show the bottom navigation bar
    val bottomBarRoutes = listOf(
        Screen.Home.route,
        Screen.Tools.route,
        Screen.Files.route,
        Screen.Profile.route
    )
    val showBottomBar = currentRoute in bottomBarRoutes

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        bottomBar = {
            if (showBottomBar) {
                BottomNavBar(
                    currentRoute = currentRoute ?: Screen.Home.route,
                    onNavigate = { route ->
                        navController.navigate(route) {
                            popUpTo(Screen.Home.route) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    onScanClick = {
                        navController.navigate(Screen.Scanner.route)
                    }
                )
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier.padding(innerPadding),
            enterTransition = {
                fadeIn(animationSpec = tween(300))
            },
            exitTransition = {
                fadeOut(animationSpec = tween(300))
            }
        ) {
            // ━━━ Phase 9: Onboarding ━━━
            composable(Screen.Onboarding.route) {
                OnboardingScreen(
                    onComplete = {
                        scope.launch {
                            ThemePreferences.setOnboardingComplete(context)
                        }
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Onboarding.route) { inclusive = true }
                        }
                    }
                )
            }

            // ━━━ Bottom Nav Screens ━━━
            composable(Screen.Home.route) {
                HomeScreen(
                    onFileClick = { fileId ->
                        navController.navigate(Screen.Reader.createRoute(fileId))
                    },
                    onAiClick = {
                        navController.navigate(Screen.AiSummary.route)
                    },
                    onNavigate = { route ->
                        navController.navigate(route)
                    }
                )
            }

            composable(Screen.Tools.route) {
                ToolsScreen(
                    onScanClick = {
                        navController.navigate(Screen.Scanner.route)
                    },
                    onToolClick = { toolRoute ->
                        navController.navigate(toolRoute)
                    }
                )
            }

            composable(Screen.Files.route) {
                FilesScreen(
                    onFileClick = { fileId ->
                        navController.navigate(Screen.Reader.createRoute(fileId))
                    }
                )
            }

            composable(Screen.Profile.route) {
                ProfileScreen(
                    onNavigate = { route -> navController.navigate(route) }
                )
            }

            // ━━━ Full-Screen: Document Viewer ━━━
            composable(
                route = Screen.Reader.route,
                arguments = listOf(
                    navArgument("fileId") { type = NavType.StringType }
                ),
                enterTransition = {
                    slideIntoContainer(
                        towards = AnimatedContentTransitionScope.SlideDirection.Up,
                        animationSpec = tween(350)
                    )
                },
                exitTransition = {
                    slideOutOfContainer(
                        towards = AnimatedContentTransitionScope.SlideDirection.Down,
                        animationSpec = tween(350)
                    )
                }
            ) { backStackEntry ->
                val fileId = Uri.decode(backStackEntry.arguments?.getString("fileId") ?: "")
                ReaderScreen(
                    fileId = fileId,
                    onBack = { navController.popBackStack() }
                )
            }

            // ━━━ Full-Screen: Document Scanner ━━━
            composable(
                route = Screen.Scanner.route,
                enterTransition = {
                    slideIntoContainer(
                        towards = AnimatedContentTransitionScope.SlideDirection.Up,
                        animationSpec = tween(350)
                    )
                },
                exitTransition = {
                    slideOutOfContainer(
                        towards = AnimatedContentTransitionScope.SlideDirection.Down,
                        animationSpec = tween(350)
                    )
                }
            ) {
                ScannerScreen(
                    onBack = { navController.popBackStack() },
                    onOpenPdf = { pdfUri: Uri ->
                        // Set the scanned PDF as current file and open viewer
                        CurrentFile.set(
                            uri = pdfUri,
                            name = "Scanned Document.pdf",
                            mimeType = "application/pdf"
                        )
                        navController.navigate(Screen.Reader.createRoute("scanned")) {
                            popUpTo(Screen.Scanner.route) { inclusive = true }
                        }
                    }
                )
            }

            // ━━━ Phase 5: Tool Screens ━━━
            composable(
                route = Screen.Merge.route,
                enterTransition = { slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Up, tween(350)) },
                exitTransition = { slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Down, tween(350)) }
            ) {
                MergeScreen(onBack = { navController.popBackStack() })
            }

            composable(
                route = Screen.Split.route,
                enterTransition = { slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Up, tween(350)) },
                exitTransition = { slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Down, tween(350)) }
            ) {
                SplitScreen(onBack = { navController.popBackStack() })
            }

            composable(
                route = Screen.Compress.route,
                enterTransition = { slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Up, tween(350)) },
                exitTransition = { slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Down, tween(350)) }
            ) {
                CompressScreen(onBack = { navController.popBackStack() })
            }

            composable(
                route = Screen.ImageToPdf.route,
                enterTransition = { slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Up, tween(350)) },
                exitTransition = { slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Down, tween(350)) }
            ) {
                ImageToPdfScreen(onBack = { navController.popBackStack() })
            }

            composable(
                route = Screen.Ocr.route,
                enterTransition = { slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Up, tween(350)) },
                exitTransition = { slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Down, tween(350)) }
            ) {
                OcrScreen(onBack = { navController.popBackStack() })
            }

            composable(
                route = Screen.Protect.route,
                enterTransition = { slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Up, tween(350)) },
                exitTransition = { slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Down, tween(350)) }
            ) {
                ProtectScreen(onBack = { navController.popBackStack() })
            }

            composable(
                route = Screen.Watermark.route,
                enterTransition = { slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Up, tween(350)) },
                exitTransition = { slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Down, tween(350)) }
            ) {
                WatermarkScreen(onBack = { navController.popBackStack() })
            }

            composable(
                route = Screen.Convert.route,
                enterTransition = { slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Up, tween(350)) },
                exitTransition = { slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Down, tween(350)) }
            ) {
                ConvertScreen(onBack = { navController.popBackStack() })
            }

            // ━━━ Phase 6: AI Features ━━━
            composable(
                route = Screen.AiSummary.route,
                enterTransition = { slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Up, tween(350)) },
                exitTransition = { slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Down, tween(350)) }
            ) {
                AiSummaryScreen(onBack = { navController.popBackStack() })
            }

            // ━━━ Phase 9: Recycle Bin ━━━
            composable(
                route = Screen.RecycleBin.route,
                enterTransition = { slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Up, tween(350)) },
                exitTransition = { slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Down, tween(350)) }
            ) {
                RecycleBinScreen(onBack = { navController.popBackStack() })
            }

            // ━━━ Phase 9: Page Reorder Tool ━━━
            composable(
                route = Screen.PageReorder.route,
                enterTransition = { slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Up, tween(350)) },
                exitTransition = { slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Down, tween(350)) }
            ) {
                PageReorderScreen(onBack = { navController.popBackStack() })
            }

            // ━━━ Phase 10: StorageManager ━━━
            composable(
                route = Screen.StorageManager.route,
                enterTransition = { slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Up, tween(350)) },
                exitTransition = { slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Down, tween(350)) }
            ) {
                StorageManagerScreen(onBack = { navController.popBackStack() })
            }

            // ━━━ Phase 10: About ━━━
            composable(
                route = Screen.About.route,
                enterTransition = { slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Up, tween(350)) },
                exitTransition = { slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Down, tween(350)) }
            ) {
                AboutScreen(onBack = { navController.popBackStack() })
            }
        }
    }
}
