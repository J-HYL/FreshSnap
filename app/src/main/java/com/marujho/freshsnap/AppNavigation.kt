package com.marujho.freshsnap

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.google.firebase.auth.FirebaseAuth


@Composable
fun AppNavigation(startDestination: String) {
    val navController = rememberNavController()

    var currentUser by remember { mutableStateOf(FirebaseAuth.getInstance().currentUser) }

    LaunchedEffect(Unit) {
        FirebaseAuth.getInstance().addAuthStateListener {
            currentUser = it.currentUser
        }
    }
    val startDestination = if (currentUser != null) "main_screen" else "login_screen"

    NavHost(navController = navController, startDestination = startDestination) {
        composable("login_screen") {
            LoginScreen(navController = navController)
        }

        composable("sign_up_screen") {
            SignUpScreen(navController = navController)
        }

        composable("main_screen") {
            MainAppScreen(
                onNavigateToDetail = { barcode ->
                    navController.navigate("detail_screen/$barcode")
                }
            )
        }


        composable( //Cogemos para saber si es en modo barcode que va a ser el pordefecto siempre o el de fecha de caducidad
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
                },
                onBackClick = {
                    Log.d("NAV", "Quiere volver")
                    navController.popBackStack()
                }
            )
        }

        composable(
            route = "detail_screen/{barcode}?productId={productId}",
            arguments = listOf(
                navArgument("barcode") { type = NavType.StringType },
                navArgument("productId") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            )
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