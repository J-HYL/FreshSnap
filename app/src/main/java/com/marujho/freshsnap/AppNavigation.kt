package com.marujho.freshsnap

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.marujho.freshsnap.ui.login.LoginScreen

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "splash_screen") {

        composable("splash_screen") {
            SplashScreen(navController)
        }

        composable("login_screen") {
            LoginScreen(navController = navController)
        }

        composable("sign_up_screen") {
            SignUpScreen(navController = navController)
        }

        composable("main_screen") {
            MainScreen()
        }

        composable("scanner_screen") {
            BarCodeScanScreen()
        }
    }
}