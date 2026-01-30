package com.marujho.freshsnap

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.marujho.freshsnap.ui.detail.detailScreen
import com.marujho.freshsnap.ui.login.LoginScreen
import com.marujho.freshsnap.ui.main.MainAppScreen
import com.marujho.freshsnap.ui.main.MainScreen
import com.marujho.freshsnap.ui.splash.SplashScreen

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
            MainAppScreen(
                onLogout = {
                    navController.navigate("login_screen") {
                        popUpTo("main_screen") { inclusive = true }
                    }
                },
                onNavigateToDetail = {barcode ->
                    navController.navigate("detail_screen/$barcode")
                }
            )
        }

        composable("scanner_screen") {
            BarCodeScanScreen(
                onNavigateToDetail = { barcode ->
                    Log.d("OFF_TEST2","Quiere navegar")
                    navController.navigate("detail_screen/$barcode")
                }
            )
        }

        composable (route = "detail_screen/{barcode}",
            arguments = listOf(navArgument("barcode"){type = NavType.StringType})){
            detailScreen()
        }
    }
}