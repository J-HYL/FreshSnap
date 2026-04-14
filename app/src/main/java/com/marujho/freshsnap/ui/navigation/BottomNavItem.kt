package com.marujho.freshsnap.ui.navigation

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.ui.graphics.vector.ImageVector
import com.marujho.freshsnap.R

sealed class BottomNavItem(val route: String, @StringRes val titleResId: Int, val icon: ImageVector) {
    object Home : BottomNavItem("home_screen", R.string.nav_home, Icons.Default.Home)
    object Scanner : BottomNavItem("scanner_screen", R.string.nav_scanner, Icons.Default.CameraAlt)
    object Shopping : BottomNavItem("shopping_screen", R.string.nav_shopping, Icons.Default.ShoppingCart)
    object Settings : BottomNavItem("settings_screen", R.string.nav_settings, Icons.Default.Settings)
}