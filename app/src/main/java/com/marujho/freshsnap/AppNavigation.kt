package com.marujho.freshsnap

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.marujho.freshsnap.ui.detail.detailScreen
import androidx.navigation.navArgument
import com.marujho.freshsnap.data.model.ScanType
import com.marujho.freshsnap.ui.detail.DetailViewModel
import com.marujho.freshsnap.ui.detail.detailScreen
import com.marujho.freshsnap.ui.login.LoginScreen
import com.marujho.freshsnap.ui.main.MainAppScreen
import com.marujho.freshsnap.ui.main.MainScreen
import com.marujho.freshsnap.ui.splash.SplashScreen

@Composable
fun AppNavigation(startDestination: String) {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = startDestination) {
        composable("login_screen") {
            LoginScreen(navController = navController)
        }

        composable("sign_up_screen") {
            SignUpScreen(navController = navController)
        }

        composable("main_screen") {
            MainAppScreen(
                onNavigateToDetail = {barcode ->
                    navController.navigate("detail_screen/$barcode")
                }
            )
        }


        composable( //Cojemos para saber si es en modo barcode que va a ser el pordefecto siempre o el de fecha de caducidad
            "scanner_screen?type={type}",
            arguments = listOf(
                navArgument("type") { defaultValue = ScanType.BARCODE.name }
            )) { backStackEntry ->
            val scanType = backStackEntry.arguments?.getString("type") ?: ScanType.BARCODE.name
            val currentScantype = try {
                ScanType.valueOf(scanType)
            } catch (e: IllegalArgumentException) {
                ScanType.BARCODE
            }

            BarCodeScanScreen(
                scanType = currentScantype,
                onNavigateToDetail = { barcode ->
                    Log.d("OFF_TEST2", "Quiere navegar")
                    navController.navigate("detail_screen/$barcode")
                },
                onDateScanned = { date ->
                    navController.previousBackStackEntry
                        ?.savedStateHandle
                        ?.set("scanned_date", date)
                    navController.popBackStack()
                }
            )
        }

        composable(
            route = "detail_screen/{barcode}",
            arguments = listOf(navArgument("barcode") { type = NavType.StringType })
        ) { backStackEntry ->
            val viewModel: DetailViewModel = hiltViewModel()
            val scannedDate = backStackEntry.savedStateHandle.getLiveData<String>("scanned_date")

            LaunchedEffect(scannedDate.value) {
                scannedDate.value?.let { date ->
                    viewModel.setExpirationFromScan(date)
                    backStackEntry.savedStateHandle.remove<String>("scanned_date")
                }
            }
            detailScreen(
                viewModel = viewModel,
                onNavigateMain = {
                    navController.navigate("main_screen") {
                        popUpTo("main_screen") { inclusive = true }
                    }
                },
                onNavigationToScanDate = {
                    navController.navigate("scanner_screen?type=${ScanType.EX_DATE.name}")
                }
            )
        }
    }
}