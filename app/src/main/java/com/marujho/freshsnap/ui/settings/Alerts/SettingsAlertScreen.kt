package com.marujho.freshsnap.ui.settings.Alerts

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.marujho.freshsnap.ui.theme.SoftRed
import com.marujho.freshsnap.ui.theme.Yellow
import androidx.compose.material3.TopAppBarDefaults

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsAlertScreen(
    navController: NavController,
    viewModel: SettingsAlertViewModel = viewModel()
) {
    val context = LocalContext.current
    val redDays by viewModel.redDays.collectAsState()
    val yellowDays by viewModel.yellowDays.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Alertas de caducidad") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = SoftRed.copy(alpha = 0.2f),
                    titleContentColor = SoftRed
                )
            )
        }
    ) { padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {

            Spacer(Modifier.height(8.dp))

            // Alerta roja
            Text(
                "Alerta Roja",
                style = MaterialTheme.typography.titleMedium,
                color = SoftRed
            )
            Spacer(Modifier.height(8.dp))
            Slider(
                value = redDays.toFloat(),
                onValueChange = { viewModel.updateRedDays(it.toInt()) },
                valueRange = 1f..10f,
                steps = 8,
                colors = SliderDefaults.colors(
                    thumbColor = SoftRed,
                    activeTrackColor = SoftRed,
                    inactiveTrackColor = SoftRed.copy(alpha = 0.3f)
                )
            )
            Text("$redDays días antes")

            Spacer(Modifier.height(32.dp))

            // Alerta amarilla
            Text(
                "Alerta Amarilla",
                style = MaterialTheme.typography.titleMedium,
                color = Yellow
            )
            Spacer(Modifier.height(8.dp))
            Slider(
                value = yellowDays.toFloat(),
                onValueChange = { viewModel.updateYellowDays(it.toInt()) },
                valueRange = 1f..15f,
                steps = 13,
                colors = SliderDefaults.colors(
                    thumbColor = Yellow,
                    activeTrackColor = Yellow,
                    inactiveTrackColor = Yellow.copy(alpha = 0.3f)
                )
            )
            Text("$yellowDays días antes")

            Spacer(Modifier.height(32.dp))

            Button(
                onClick = {
                    Toast.makeText(context, "¡Datos guardados!", Toast.LENGTH_SHORT).show()
                    navController.popBackStack()
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Guardar")
            }
        }
    }
}