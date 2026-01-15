package com.marujho.freshsnap

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "splash_screen") {

        composable("splash_screen") {
            SplashScreen(navController)
        }

        composable("login_screen") {
            LoginBox(
                onLoginClick = {
                    navController.navigate("main_screen") {
                        popUpTo("login_screen") { inclusive = true }
                    }
                },
                onSignUpClick = {
                    navController.navigate("sign_up_screen")
                }
            )
        }

        composable("sign_up_screen") {
            SignUpBox(
                onRegisterClick = {
                    navController.navigate("login_screen")
                },
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }

        composable("main_screen") {
            MainScreen()
        }
    }
}