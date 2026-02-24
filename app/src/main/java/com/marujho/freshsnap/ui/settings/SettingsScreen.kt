package com.marujho.freshsnap.ui.settings

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController

@Composable
fun SettingsScreen(
    navController: NavController,
    viewModel: SettingsViewModel = viewModel()
) {

    val isDarkMode by viewModel.isDarkMode.collectAsState()

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp, vertical = 16.dp)
        ) {

            UserHeader()

            Spacer(Modifier.height(24.dp))

            Text(
                text = "Ajustes",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(Modifier.height(16.dp))

            SettingsItem(
                title = "Dark Theme / Light Theme",
                onClick = {},
                trailing = {
                    Switch(
                        checked = isDarkMode,
                        onCheckedChange = {
                            viewModel.toggleDarkMode(it)
                        }
                    )
                }
            )

            SettingsItem(
                title = "Mi Cuenta",
                subtitle = "Nombre, edad, sexo, idioma.",
                onClick = { navController.navigate("settings_account") }
            )

            SettingsItem(
                title = "Alergias",
                subtitle = "Leche, gluten, huevos.",
                onClick = { navController.navigate("settings_allergy") }
            )

            SettingsItem(
                title = "Alertas caducidad",
                subtitle = "Configura días rojo y amarillo",
                onClick = { navController.navigate("settings_alert") }
            )

            Spacer(Modifier.height(24.dp))

            Button(
                onClick = {
                    viewModel.logout { }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error
                )
            ) {
                Text("Cerrar sesión")
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun SettingsScreenPreview() {
    MaterialTheme {
        SettingsScreen(
            navController = rememberNavController()
        )
    }
}