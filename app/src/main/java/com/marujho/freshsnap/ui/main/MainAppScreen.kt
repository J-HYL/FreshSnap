package com.marujho.freshsnap.ui.main

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.marujho.freshsnap.ui.navigation.BottomNavItem
import com.marujho.freshsnap.ui.scanner.BarCodeScanScreen
import com.marujho.freshsnap.ui.settings.Account.SettingsAccountScreen
import com.marujho.freshsnap.ui.settings.Alerts.SettingsAlertScreen
import com.marujho.freshsnap.ui.settings.Allergy.SettingsAllergyScreen
import com.marujho.freshsnap.ui.settings.SettingsScreen

@Composable
fun MainAppScreen(
    onNavigateToDetail: (String) -> Unit
) {
    val navController = rememberNavController()

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.onSurface,
//                tonalElevation = 8.dp
            ) {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination

                val items = listOf(
                    BottomNavItem.Home,
                    BottomNavItem.Scanner,
                    BottomNavItem.Settings
                )

                items.forEach { screen ->
                    val isSelected = currentDestination?.route == screen.route ||
                            (screen == BottomNavItem.Settings &&
                                    currentDestination?.route?.startsWith("settings_") == true)

                    NavigationBarItem(
                        icon = {
                            Icon(
                                imageVector = screen.icon,
                                contentDescription = stringResource(screen.titleResId)
                            )
                        },
                        label = null, // PREGUNTAR SI DEJAR LAS LABEL O QUITARLAS COMO EN EL DISEÑO ORIGINAL
                        alwaysShowLabel = false,
                        selected = isSelected,
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.onPrimary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            indicatorColor = MaterialTheme.colorScheme.primary,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        onClick = {
                            navController.navigate(screen.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = false
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->

        NavHost(
            navController = navController,
            startDestination = BottomNavItem.Home.route,
            modifier = Modifier.fillMaxSize()
        ) {
            composable(BottomNavItem.Home.route) {
                MainScreen(
                    navController = navController,
                    bottomBarPadding = innerPadding.calculateBottomPadding(),
                    onNavigateToDetail = onNavigateToDetail
                )
            }
            composable(BottomNavItem.Scanner.route) {
                BarCodeScanScreen(
                    onNavigateToDetail = onNavigateToDetail,
                    onBackClick = {
                        navController.navigate(BottomNavItem.Home.route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
            composable(BottomNavItem.Settings.route) {
                SettingsScreen(navController)
            }

            composable("settings_account") {
                SettingsAccountScreen()
            }

            composable("settings_alert") {
                SettingsAlertScreen(navController = navController)
            }

            composable("settings_allergy") {
                SettingsAllergyScreen()
            }

        }
    }
}