package com.marujho.freshsnap.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Search
import androidx.compose.ui.graphics.vector.ImageVector

sealed class BottomNavItem(val route: String, val title: String, val icon: ImageVector) {
    object Home : BottomNavItem("home_screen", "Inicio", Icons.Default.Home)
    object Scanner : BottomNavItem("scanner_screen", "Escanear", Icons.Default.Search)
}