package com.marujho.freshsnap

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.marujho.freshsnap.ui.openFoodFacts.OpenFoodViewModel
import com.marujho.freshsnap.ui.splash.SplashViewModel
import com.marujho.freshsnap.ui.theme.FreshSnapTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val splashViewModel: SplashViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()

        super.onCreate(savedInstanceState)

        splashScreen.setKeepOnScreenCondition {
            splashViewModel.isLoading.value
        }

        enableEdgeToEdge()

        setContent {
            FreshSnapTheme {
                Surface(
                    modifier = Modifier.Companion.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val startDest by splashViewModel.startDestination.collectAsState()

                    if (startDest != null) {
                        AppNavigation(startDestination = startDest!!)
                    }
                }
            }
        }
    }
}