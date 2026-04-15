package com.fastpdf.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.fastpdf.ui.components.BottomNavBar
import com.fastpdf.ui.screens.FilesScreen
import com.fastpdf.ui.screens.HomeScreen
import com.fastpdf.ui.screens.ProfileScreen
import com.fastpdf.ui.screens.ReaderScreen
import com.fastpdf.ui.screens.ToolsScreen

/**
 * Main navigation graph for FastPDF.
 * Handles bottom nav + reader screen transitions.
 */
@Composable
fun NavGraph() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // Screens that show the bottom navigation bar
    val bottomBarRoutes = listOf(
        Screen.Home.route,
        Screen.Tools.route,
        Screen.Files.route,
        Screen.Profile.route
    )
    val showBottomBar = currentRoute in bottomBarRoutes

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                BottomNavBar(
                    currentRoute = currentRoute ?: Screen.Home.route,
                    onNavigate = { route ->
                        navController.navigate(route) {
                            // Pop up to start destination to avoid stacking
                            popUpTo(Screen.Home.route) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(innerPadding),
            enterTransition = {
                fadeIn(animationSpec = tween(300))
            },
            exitTransition = {
                fadeOut(animationSpec = tween(300))
            }
        ) {
            // ━━━ Bottom Nav Screens ━━━
            composable(Screen.Home.route) {
                HomeScreen(
                    onFileClick = { fileId ->
                        navController.navigate(Screen.Reader.createRoute(fileId))
                    }
                )
            }

            composable(Screen.Tools.route) {
                ToolsScreen()
            }

            composable(Screen.Files.route) {
                FilesScreen(
                    onFileClick = { fileId ->
                        navController.navigate(Screen.Reader.createRoute(fileId))
                    }
                )
            }

            composable(Screen.Profile.route) {
                ProfileScreen()
            }

            // ━━━ Full-Screen Routes ━━━
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
                val fileId = backStackEntry.arguments?.getString("fileId") ?: ""
                ReaderScreen(
                    fileId = fileId,
                    onBack = { navController.popBackStack() }
                )
            }
        }
    }
}
